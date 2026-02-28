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

private data class VerificationEntry(val code: String, val expiresAt: Instant)

// Signup verification codes
private val verificationCodes  = ConcurrentHashMap<String, VerificationEntry>()
// Password reset codes — separate map so flows never collide
private val passwordResetCodes = ConcurrentHashMap<String, VerificationEntry>()

@RestController
@RequestMapping("/v1/auth")
@CrossOrigin(origins = ["*"])
class AuthController(
    private val userRepository      : UserRepository,
    private val passwordEncoder     : PasswordEncoder,
    private val jwtUtil             : JwtUtil,
    private val notificationService : NotificationService
) {

    // ─── Register ─────────────────────────────────────────────────────────────

    @PostMapping("/register")
    fun register(@RequestBody request: RegisterRequest): ResponseEntity<AuthApiResponse<AuthResponse>> {
        logger.info { "Register attempt: ${request.email}" }
        return try {
            if (userRepository.existsByEmail(request.email)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    AuthApiResponse(success = false, message = "Email already registered")
                )
            }
            val user = User(
                userId = UUID.randomUUID().toString(), fullName = request.fullName,
                email = request.email, password = passwordEncoder.encode(request.password),
                phone = request.phone, city = request.city, address = request.address,
                profileImageUrl = request.profileImageUrl, role = UserRole.PARENT, isActive = false
            )
            val saved = userRepository.save(user)
            ResponseEntity.status(HttpStatus.CREATED).body(
                AuthApiResponse(success = true, message = "Registration successful. Please verify your account.",
                    data = AuthResponse(token = jwtUtil.generateToken(saved.email, saved.role.name), user = saved.toResponse()))
            )
        } catch (e: Exception) {
            logger.error(e) { "Registration failed" }
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                AuthApiResponse(success = false, message = e.message ?: "Registration failed")
            )
        }
    }

    // ─── Login ────────────────────────────────────────────────────────────────

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
                    AuthApiResponse(success = false, message = "Account not verified. Please verify your email first.")
                )
            }
            ResponseEntity.ok(
                AuthApiResponse(success = true, message = "Login successful",
                    data = AuthResponse(token = jwtUtil.generateToken(user.email, user.role.name), user = user.toResponse()))
            )
        } catch (e: Exception) {
            logger.error(e) { "Login failed" }
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                AuthApiResponse(success = false, message = e.message ?: "Login failed")
            )
        }
    }

    // ─── Google Auth ──────────────────────────────────────────────────────────

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
                userRepository.save(User(
                    userId = UUID.randomUUID().toString(), fullName = request.displayName,
                    email = request.email, password = passwordEncoder.encode(UUID.randomUUID().toString()),
                    profileImageUrl = request.photoUrl, role = UserRole.PARENT, isActive = true
                ))
            }
            if (isNewUser) notificationService.sendGoogleWelcomeEmail(user.email, user.fullName)
            ResponseEntity.ok(
                AuthApiResponse(success = true, message = "Google sign-in successful",
                    data = AuthResponse(token = jwtUtil.generateToken(user.email, user.role.name), user = user.toResponse()))
            )
        } catch (e: Exception) {
            logger.error(e) { "Google auth failed" }
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                AuthApiResponse(success = false, message = e.message ?: "Google auth failed")
            )
        }
    }

    // ─── Send Verification Code (signup flow) ─────────────────────────────────

    @PostMapping("/send-verification")
    fun sendVerificationCode(@RequestBody request: SendVerificationCodeRequest): ResponseEntity<AuthApiResponse<VerificationResponse>> {
        return try {
            val method = request.method.lowercase()
            val user: User? = when (method) {
                "email" -> userRepository.findByEmail(request.recipient).orElse(null)
                "sms"   -> userRepository.findByPhone(request.recipient).orElse(null)
                else    -> return ResponseEntity.badRequest().body(AuthApiResponse(success = false, message = "Invalid method"))
            }
            if (user == null) return ResponseEntity.badRequest().body(
                AuthApiResponse(success = false, message = "No account found for ${request.recipient}")
            )
            val code = (100000..999999).random().toString()
            verificationCodes[request.recipient] = VerificationEntry(code, Instant.now().plusSeconds(600))
            when (method) {
                "email" -> notificationService.sendVerificationEmail(request.recipient, user.fullName, code)
                "sms"   -> notificationService.sendVerificationSms(request.recipient, code)
            }
            ResponseEntity.ok(AuthApiResponse(success = true, message = "Code sent to ${request.recipient}",
                data = VerificationResponse("Code sent successfully", true)))
        } catch (e: Exception) {
            verificationCodes.remove(request.recipient)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                AuthApiResponse(success = false, message = e.message ?: "Failed to send code")
            )
        }
    }

    // ─── Verify Account (signup flow) ─────────────────────────────────────────

    @PostMapping("/verify-account")
    fun verifyAccount(@RequestBody request: VerifyAccountRequest): ResponseEntity<AuthApiResponse<VerificationResponse>> {
        return try {
            val matched = verificationCodes.entries.find { (_, e) ->
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
            verificationCodes.remove(matched.key)
            ResponseEntity.ok(AuthApiResponse(success = true, message = "Account verified successfully",
                data = VerificationResponse("Account verified successfully", true)))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                AuthApiResponse(success = false, message = "Verification failed: ${e.message}")
            )
        }
    }

    // =========================================================================
    // FORGOT PASSWORD — 3-step flow
    //
    // Step 1  POST /forgot-password      check email exists → send 6-digit code
    // Step 2  POST /verify-reset-code    validate the code
    // Step 3  POST /reset-password       save new password
    // =========================================================================

    // ─── Step 1 ───────────────────────────────────────────────────────────────

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
            logger.info { "Generated password reset code for ${request.email}" }

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

    // ─── Step 2 ───────────────────────────────────────────────────────────────

    @PostMapping("/verify-reset-code")
    fun verifyResetCode(
        @RequestBody request: VerifyResetCodeRequest
    ): ResponseEntity<AuthApiResponse<VerificationResponse>> {
        logger.info { "Verify reset code: ${request.email}" }
        return try {
            val entry = passwordResetCodes[request.email]

            if (entry == null || entry.code != request.code || Instant.now().isAfter(entry.expiresAt)) {
                return ResponseEntity.badRequest().body(
                    AuthApiResponse(success = false, message = "Invalid or expired reset code")
                )
            }

            // Code valid — keep it in map, Step 3 needs it to confirm the request
            ResponseEntity.ok(
                AuthApiResponse(
                    success = true,
                    message = "Code verified successfully",
                    data    = VerificationResponse("Code verified", true)
                )
            )
        } catch (e: Exception) {
            logger.error(e) { "Verify reset code failed" }
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                AuthApiResponse(success = false, message = "Verification failed: ${e.message}")
            )
        }
    }

    // ─── Step 3 ───────────────────────────────────────────────────────────────

    @PostMapping("/reset-password")
    fun resetPassword(
        @RequestBody request: ResetPasswordRequest
    ): ResponseEntity<AuthApiResponse<VerificationResponse>> {
        logger.info { "Reset password: ${request.email}" }
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
            passwordResetCodes.remove(request.email)   // single-use

            logger.info { "Password reset successfully: ${user.userId}" }

            ResponseEntity.ok(
                AuthApiResponse(
                    success = true,
                    message = "Password reset successfully",
                    data    = VerificationResponse("Password reset successfully", true)
                )
            )
        } catch (e: Exception) {
            logger.error(e) { "Reset password failed" }
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                AuthApiResponse(success = false, message = "Reset failed: ${e.message}")
            )
        }
    }

    // ─── Mapper ───────────────────────────────────────────────────────────────

    private fun User.toResponse() = UserResponse(
        userId = userId, fullName = fullName, email = email, phone = phone,
        city = city, address = address, profileImageUrl = profileImageUrl,
        role = role, isActive = isActive, createdAt = createdAt, updatedAt = updatedAt
    )
}

// =============================================================================
// DTOs
// =============================================================================

data class RegisterRequest(
    val fullName: String, val email: String, val password: String,
    val phone: String? = null, val city: String? = null,
    val address: String? = null, val profileImageUrl: String? = null
)
data class LoginRequest(val emailOrPhone: String, val password: String)
data class GoogleAuthRequest(val idToken: String, val email: String, val displayName: String, val photoUrl: String? = null)
data class SendVerificationCodeRequest(val recipient: String, val method: String)
data class VerifyAccountRequest(val code: String, val method: String)

// Forgot password DTOs
data class ForgotPasswordRequest(val email: String)
data class VerifyResetCodeRequest(val email: String, val code: String)
data class ResetPasswordRequest(val email: String, val code: String, val newPassword: String)

data class VerificationResponse(val message: String, val success: Boolean)
data class AuthResponse(val token: String, val user: UserResponse)
data class AuthApiResponse<T>(val success: Boolean, val message: String? = null, val data: T? = null)