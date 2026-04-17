// backend-side/src/main/java/com/example/backend_side/controllers/AuthController.kt
package com.example.backend_side.controllers

import com.example.backend_side.NotificationService
import com.example.backend_side.UserResponse
import com.example.backend_side.entity.*
import com.example.backend_side.repositories.UserRepository
import com.example.backend_side.JwtUtil
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.time.Instant

private val logger = KotlinLogging.logger {}

// ─── Verification entry (shared by signup OTP and password-reset OTP) ─────────
private data class VerificationEntry(val code: String, val expiresAt: Instant)

// ─── Pending registration data — held in memory until OTP is confirmed ─────────
// KEY  = email (the unique identifier chosen before the user exists)
// This map never touches the database — the user row is created ONLY after
// a successful call to /complete-registration.
private data class PendingRegistration(
    val fullName       : String,
    val email          : String,
    val hashedPassword : String,      // already BCrypt-hashed so it's safe in RAM
    val phone          : String?,
    val city           : String?,
    val address        : String?,
    val profileImageUrl: String?,
    val expiresAt      : Instant      // registration attempt TTL (30 minutes)
)

private val pendingRegistrations = ConcurrentHashMap<String, PendingRegistration>()

// ─── OTP maps ────────────────────────────────────────────────────────────────
// Separate maps so signup flow and password-reset flow never collide.
private val signupVerificationCodes = ConcurrentHashMap<String, VerificationEntry>()
private val passwordResetCodes      = ConcurrentHashMap<String, VerificationEntry>()

@RestController
@RequestMapping("/v1/auth")
@CrossOrigin(origins = ["*"])
class AuthController(
    private val userRepository      : UserRepository,
    private val passwordEncoder     : PasswordEncoder,
    private val jwtUtil             : JwtUtil,
    private val notificationService : NotificationService
) {

    // =========================================================================
    // SIGNUP — 3-step flow (user is NEVER written to DB until step 3 succeeds)
    //
    // Step 1  POST /pre-register          validate + store pending data in RAM,
    //                                     send 6-digit email OTP
    // Step 2  POST /verify-signup-code    validate the OTP
    // Step 3  POST /complete-registration  create the user row (isActive = true)
    // =========================================================================

    // ─── Step 1: pre-register ────────────────────────────────────────────────
    /**
     * Validates the submitted fields and sends an OTP to the provided email
     * WITHOUT creating any database row.
     *
     * If the user abandons the app after this call, nothing is left in the DB.
     */
    @PostMapping("/pre-register")
    fun preRegister(@RequestBody request: RegisterRequest): ResponseEntity<AuthApiResponse<PreRegisterResponse>> {
        logger.info { "Pre-register attempt: ${request.email}" }
        return try {
            // Reject if a fully-active account already exists
            val existing = userRepository.findByEmail(request.email).orElse(null)
            if (existing != null && existing.isActive) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    AuthApiResponse(success = false, message = "Email already registered")
                )
            }

            // Hash the password now so plain-text is never kept in RAM
            val hashed = passwordEncoder.encode(request.password)

            // Generate OTP
            val code = (100000..999999).random().toString()
            val expiresAt = Instant.now().plusSeconds(600)   // 10 minutes

            // Store pending data — overwrites any previous attempt for this email
            pendingRegistrations[request.email] = PendingRegistration(
                fullName        = request.fullName,
                email           = request.email,
                hashedPassword  = hashed,
                phone           = request.phone,
                city            = request.city,
                address         = request.address,
                profileImageUrl = request.profileImageUrl,
                expiresAt       = Instant.now().plusSeconds(1800)  // 30-min TTL
            )

            // Store OTP separately (shorter TTL)
            signupVerificationCodes[request.email] = VerificationEntry(code, expiresAt)

            // Send OTP email
            notificationService.sendVerificationEmail(request.email, request.fullName, code)

            logger.info { "Pre-register OTP sent to ${request.email}" }
            ResponseEntity.ok(
                AuthApiResponse(
                    success = true,
                    message = "Verification code sent to ${request.email}",
                    data    = PreRegisterResponse(email = request.email, codeSent = true)
                )
            )
        } catch (e: Exception) {
            logger.error(e) { "Pre-register failed for ${request.email}" }
            pendingRegistrations.remove(request.email)
            signupVerificationCodes.remove(request.email)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                AuthApiResponse(success = false, message = e.message ?: "Pre-registration failed")
            )
        }
    }

    // ─── Step 2: verify signup OTP ───────────────────────────────────────────
    /**
     * Validates the 6-digit code sent in Step 1.
     * Does NOT create a user row — that only happens in Step 3.
     */
    @PostMapping("/verify-signup-code")
    fun verifySignupCode(
        @RequestBody request: VerifySignupCodeRequest
    ): ResponseEntity<AuthApiResponse<VerificationResponse>> {
        logger.info { "Verify signup code for ${request.email}" }
        return try {
            val entry = signupVerificationCodes[request.email]

            if (entry == null || entry.code != request.code || Instant.now().isAfter(entry.expiresAt)) {
                return ResponseEntity.badRequest().body(
                    AuthApiResponse(success = false, message = "Invalid or expired verification code")
                )
            }

            // Confirm the pending registration still exists
            if (!pendingRegistrations.containsKey(request.email)) {
                return ResponseEntity.badRequest().body(
                    AuthApiResponse(success = false, message = "Registration session expired. Please sign up again.")
                )
            }

            // Mark OTP as used — remove it so it cannot be replayed
            signupVerificationCodes.remove(request.email)

            ResponseEntity.ok(
                AuthApiResponse(
                    success = true,
                    message = "Code verified. Please complete registration.",
                    data    = VerificationResponse("Code verified successfully", true)
                )
            )
        } catch (e: Exception) {
            logger.error(e) { "Verify signup code failed for ${request.email}" }
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                AuthApiResponse(success = false, message = "Verification failed: ${e.message}")
            )
        }
    }

    // ─── Step 3: complete registration ───────────────────────────────────────
    /**
     * Creates the user row — called ONLY after OTP is confirmed.
     * Returns a JWT token so the client can navigate straight to Home.
     */
    @PostMapping("/complete-registration")
    fun completeRegistration(
        @RequestBody request: CompleteRegistrationRequest
    ): ResponseEntity<AuthApiResponse<AuthResponse>> {
        logger.info { "Complete registration for ${request.email}" }
        return try {
            val pending = pendingRegistrations[request.email]
                ?: return ResponseEntity.badRequest().body(
                    AuthApiResponse(success = false, message = "Registration session expired. Please sign up again.")
                )

            if (Instant.now().isAfter(pending.expiresAt)) {
                pendingRegistrations.remove(request.email)
                return ResponseEntity.badRequest().body(
                    AuthApiResponse(success = false, message = "Registration session expired. Please sign up again.")
                )
            }

            // Double-check: in the rare case a concurrent request already created this user
            if (userRepository.existsByEmail(request.email)) {
                pendingRegistrations.remove(request.email)
                return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    AuthApiResponse(success = false, message = "Account already exists. Please log in.")
                )
            }

            // Create the user — isActive = true because OTP was already confirmed
            val user = User(
                userId          = UUID.randomUUID().toString(),
                fullName        = pending.fullName,
                email           = pending.email,
                password        = pending.hashedPassword,
                phone           = pending.phone,
                city            = pending.city,
                address         = pending.address,
                profileImageUrl = pending.profileImageUrl,
                role            = UserRole.PARENT,
                isActive        = true            // ← active from the start
            )
            val saved = userRepository.save(user)

            // Clean up in-memory state
            pendingRegistrations.remove(request.email)
            signupVerificationCodes.remove(request.email)   // safety cleanup

            logger.info { "User created successfully: ${saved.userId}" }

            ResponseEntity.status(HttpStatus.CREATED).body(
                AuthApiResponse(
                    success = true,
                    message = "Registration complete. Welcome!",
                    data    = AuthResponse(
                        token = jwtUtil.generateToken(saved.email, saved.role.name),
                        user  = saved.toResponse()
                    )
                )
            )
        } catch (e: Exception) {
            logger.error(e) { "Complete registration failed for ${request.email}" }
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                AuthApiResponse(success = false, message = e.message ?: "Registration failed")
            )
        }
    }

    // ─── Resend signup OTP ───────────────────────────────────────────────────
    @PostMapping("/resend-signup-code")
    fun resendSignupCode(
        @RequestBody request: ResendSignupCodeRequest
    ): ResponseEntity<AuthApiResponse<VerificationResponse>> {
        return try {
            val pending = pendingRegistrations[request.email]
                ?: return ResponseEntity.badRequest().body(
                    AuthApiResponse(success = false, message = "No pending registration found. Please sign up again.")
                )

            val code      = (100000..999999).random().toString()
            val expiresAt = Instant.now().plusSeconds(600)
            signupVerificationCodes[request.email] = VerificationEntry(code, expiresAt)
            notificationService.sendVerificationEmail(request.email, pending.fullName, code)

            ResponseEntity.ok(
                AuthApiResponse(
                    success = true,
                    message = "New verification code sent to ${request.email}",
                    data    = VerificationResponse("Code resent successfully", true)
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                AuthApiResponse(success = false, message = e.message ?: "Failed to resend code")
            )
        }
    }

    // ─── KEEP old /register for backward compat (but redirect logic) ─────────
    /**
     * DEPRECATED — kept so existing clients don't break hard.
     * New clients should use /pre-register → /verify-signup-code → /complete-registration.
     *
     * This endpoint now REJECTS creation and tells the client to use the new flow.
     */
    @PostMapping("/register")
    fun register(@RequestBody request: RegisterRequest): ResponseEntity<AuthApiResponse<AuthResponse>> {
        logger.warn { "Deprecated /register called for ${request.email} — redirecting to new flow" }
        return ResponseEntity.status(HttpStatus.GONE).body(
            AuthApiResponse(
                success = false,
                message = "Please use the new signup flow: /pre-register → /verify-signup-code → /complete-registration"
            )
        )
    }

    // =========================================================================
    // LOGIN
    // =========================================================================

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<AuthApiResponse<AuthResponse>> {
        logger.info { "Login attempt: ${request.emailOrPhone}" }
        return try {
            val isEmail = request.emailOrPhone.contains("@")
            val user: User? = if (isEmail) userRepository.findByEmail(request.emailOrPhone).orElse(null)
            else userRepository.findByPhone(request.emailOrPhone).orElse(null)

            if (user == null || !passwordEncoder.matches(request.password, user.password)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    AuthApiResponse(success = false, message = "Invalid email/phone or password")
                )
            }
            if (!user.isActive) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    AuthApiResponse(
                        success = false,
                        message = "Account not verified. Please complete signup verification."
                    )
                )
            }
            ResponseEntity.ok(
                AuthApiResponse(
                    success = true,
                    message = "Login successful",
                    data    = AuthResponse(
                        token = jwtUtil.generateToken(user.email, user.role.name),
                        user  = user.toResponse()
                    )
                )
            )
        } catch (e: Exception) {
            logger.error(e) { "Login failed" }
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                AuthApiResponse(success = false, message = e.message ?: "Login failed")
            )
        }
    }

    // =========================================================================
    // GOOGLE AUTH
    // =========================================================================

    @PostMapping("/google")
    fun googleAuth(@RequestBody request: GoogleAuthRequest): ResponseEntity<AuthApiResponse<AuthResponse>> {
        logger.info { "Google auth: ${request.email}" }
        return try {
            val existing  = userRepository.findByEmail(request.email)
            val isNewUser = !existing.isPresent
            val user = if (existing.isPresent) {
                val u = existing.get()
                request.photoUrl?.let { u.profileImageUrl = it }
                if (!u.isActive) u.isActive = true
                userRepository.save(u)
            } else {
                userRepository.save(
                    User(
                        userId          = UUID.randomUUID().toString(),
                        fullName        = request.displayName,
                        email           = request.email,
                        password        = passwordEncoder.encode(UUID.randomUUID().toString()),
                        profileImageUrl = request.photoUrl,
                        role            = UserRole.PARENT,
                        isActive        = true   // Google accounts are pre-verified
                    )
                )
            }
            if (isNewUser) notificationService.sendGoogleWelcomeEmail(user.email, user.fullName)
            ResponseEntity.ok(
                AuthApiResponse(
                    success = true,
                    message = "Google sign-in successful",
                    data    = AuthResponse(
                        token = jwtUtil.generateToken(user.email, user.role.name),
                        user  = user.toResponse()
                    )
                )
            )
        } catch (e: Exception) {
            logger.error(e) { "Google auth failed" }
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                AuthApiResponse(success = false, message = e.message ?: "Google auth failed")
            )
        }
    }

    // =========================================================================
    // SEND VERIFICATION CODE (account-verify flow for existing users)
    // =========================================================================

    @PostMapping("/send-verification")
    fun sendVerificationCode(
        @RequestBody request: SendVerificationCodeRequest
    ): ResponseEntity<AuthApiResponse<VerificationResponse>> {
        return try {
            val method = request.method.lowercase()
            val user: User? = when (method) {
                "email" -> userRepository.findByEmail(request.recipient).orElse(null)
                "sms"   -> userRepository.findByPhone(request.recipient).orElse(null)
                else    -> return ResponseEntity.badRequest().body(
                    AuthApiResponse(success = false, message = "Invalid method")
                )
            }
            if (user == null) return ResponseEntity.badRequest().body(
                AuthApiResponse(success = false, message = "No account found for ${request.recipient}")
            )
            val code = (100000..999999).random().toString()
            signupVerificationCodes[request.recipient] =
                VerificationEntry(code, Instant.now().plusSeconds(600))
            when (method) {
                "email" -> notificationService.sendVerificationEmail(request.recipient, user.fullName, code)
                "sms"   -> notificationService.sendVerificationSms(request.recipient, code)
            }
            ResponseEntity.ok(
                AuthApiResponse(
                    success = true,
                    message = "Code sent to ${request.recipient}",
                    data    = VerificationResponse("Code sent successfully", true)
                )
            )
        } catch (e: Exception) {
            signupVerificationCodes.remove(request.recipient)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                AuthApiResponse(success = false, message = e.message ?: "Failed to send code")
            )
        }
    }

    // =========================================================================
    // VERIFY ACCOUNT (for existing users — e.g. re-verification)
    // =========================================================================

    @PostMapping("/verify-account")
    fun verifyAccount(
        @RequestBody request: VerifyAccountRequest
    ): ResponseEntity<AuthApiResponse<VerificationResponse>> {
        return try {
            val matched = signupVerificationCodes.entries.find { (_, e) ->
                e.code == request.code && Instant.now().isBefore(e.expiresAt)
            } ?: return ResponseEntity.badRequest().body(
                AuthApiResponse(success = false, message = "Invalid or expired verification code")
            )
            val user: User? = when (request.method.lowercase()) {
                "email" -> userRepository.findByEmail(matched.key).orElse(null)
                "sms"   -> userRepository.findByPhone(matched.key).orElse(null)
                else    -> null
            }
            if (user == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                AuthApiResponse(success = false, message = "User not found")
            )
            user.isActive = true
            userRepository.save(user)
            signupVerificationCodes.remove(matched.key)
            ResponseEntity.ok(
                AuthApiResponse(
                    success = true,
                    message = "Account verified successfully",
                    data    = VerificationResponse("Account verified successfully", true)
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                AuthApiResponse(success = false, message = "Verification failed: ${e.message}")
            )
        }
    }

    // =========================================================================
    // FORGOT PASSWORD — 3-step flow
    // =========================================================================

    @PostMapping("/forgot-password")
    fun forgotPassword(
        @RequestBody request: ForgotPasswordRequest
    ): ResponseEntity<AuthApiResponse<VerificationResponse>> {
        logger.info { "Forgot password: ${request.email}" }
        return try {
            val user = userRepository.findByEmail(request.email).orElse(null)
                ?: return ResponseEntity.badRequest().body(
                    AuthApiResponse(success = false, message = "No account found with this email address")
                )
            val code = (100000..999999).random().toString()
            passwordResetCodes[request.email] = VerificationEntry(code, Instant.now().plusSeconds(600))
            notificationService.sendPasswordResetEmail(
                toEmail  = request.email,
                userName = user.fullName,
                code     = code
            )
            ResponseEntity.ok(
                AuthApiResponse(
                    success = true,
                    message = "Password reset code sent to ${request.email}",
                    data    = VerificationResponse("Reset code sent successfully", true)
                )
            )
        } catch (e: Exception) {
            logger.error(e) { "Forgot password failed" }
            passwordResetCodes.remove(request.email)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                AuthApiResponse(success = false, message = e.message ?: "Failed to send reset code")
            )
        }
    }

    @PostMapping("/verify-reset-code")
    fun verifyResetCode(
        @RequestBody request: VerifyResetCodeRequest
    ): ResponseEntity<AuthApiResponse<VerificationResponse>> {
        return try {
            val entry = passwordResetCodes[request.email]
            if (entry == null || entry.code != request.code || Instant.now().isAfter(entry.expiresAt)) {
                return ResponseEntity.badRequest().body(
                    AuthApiResponse(success = false, message = "Invalid or expired reset code")
                )
            }
            ResponseEntity.ok(
                AuthApiResponse(
                    success = true,
                    message = "Code verified successfully",
                    data    = VerificationResponse("Code verified", true)
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                AuthApiResponse(success = false, message = "Verification failed: ${e.message}")
            )
        }
    }

    @PostMapping("/reset-password")
    fun resetPassword(
        @RequestBody request: ResetPasswordRequest
    ): ResponseEntity<AuthApiResponse<VerificationResponse>> {
        return try {
            val entry = passwordResetCodes[request.email]
            if (entry == null || entry.code != request.code || Instant.now().isAfter(entry.expiresAt)) {
                return ResponseEntity.badRequest().body(
                    AuthApiResponse(success = false, message = "Invalid or expired reset code")
                )
            }
            if (request.newPassword.length < 6) {
                return ResponseEntity.badRequest().body(
                    AuthApiResponse(success = false, message = "Password must be at least 6 characters")
                )
            }
            val user = userRepository.findByEmail(request.email).orElse(null)
                ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    AuthApiResponse(success = false, message = "User not found")
                )
            user.password = passwordEncoder.encode(request.newPassword)
            userRepository.save(user)
            passwordResetCodes.remove(request.email)
            ResponseEntity.ok(
                AuthApiResponse(
                    success = true,
                    message = "Password reset successfully",
                    data    = VerificationResponse("Password reset successfully", true)
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                AuthApiResponse(success = false, message = "Reset failed: ${e.message}")
            )
        }
    }

    // ─── Mapper ───────────────────────────────────────────────────────────────

    private fun User.toResponse() = UserResponse(
        userId          = userId,
        fullName        = fullName,
        email           = email,
        phone           = phone,
        city            = city,
        address         = address,
        profileImageUrl = profileImageUrl,
        role            = role,
        isActive        = isActive,
        createdAt       = createdAt,
        updatedAt       = updatedAt
    )
}

// =============================================================================
// DTOs
// =============================================================================

data class RegisterRequest(
    val fullName      : String,
    val email         : String,
    val password      : String,
    val phone         : String? = null,
    val city          : String? = null,
    val address       : String? = null,
    val profileImageUrl: String? = null
)

/** Step 2 DTO — verify the OTP sent during pre-register */
data class VerifySignupCodeRequest(val email: String, val code: String)

/** Step 3 DTO — finalize account creation after OTP confirmed */
data class CompleteRegistrationRequest(val email: String)

/** Resend OTP during signup flow */
data class ResendSignupCodeRequest(val email: String)

data class PreRegisterResponse(val email: String, val codeSent: Boolean)

data class LoginRequest(val emailOrPhone: String, val password: String)
data class GoogleAuthRequest(
    val idToken     : String,
    val email       : String,
    val displayName : String,
    val photoUrl    : String? = null
)
data class SendVerificationCodeRequest(val recipient: String, val method: String)
data class VerifyAccountRequest(val code: String, val method: String)
data class ForgotPasswordRequest(val email: String)
data class VerifyResetCodeRequest(val email: String, val code: String)
data class ResetPasswordRequest(val email: String, val code: String, val newPassword: String)
data class VerificationResponse(val message: String, val success: Boolean)
data class AuthResponse(val token: String, val user: UserResponse)
data class AuthApiResponse<T>(
    val success: Boolean,
    val message: String? = null,
    val data   : T?      = null
)