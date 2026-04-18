package com.example.backend_side.controllers

import com.example.backend_side.*
import com.example.backend_side.entity.User
import com.example.backend_side.entity.UserRole
import com.example.backend_side.repositories.UserRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import java.util.UUID

private val logger = KotlinLogging.logger {}

// ─────────────────────────────────────────────────────────────────────────────
// AdminSetupController
//
// WHY THIS EXISTS:
//   The original flow was:
//     1. Register a normal account (role = PARENT)
//     2. Manually change role to 'admin' in the database
//     3. The JWT token still contains role=PARENT → StaleObjectStateException
//        and other Spring Security mismatches because the token and DB disagree.
//
// THE REAL FIX:
//   Never manually change roles in the database after a JWT is issued.
//   Instead, use these endpoints to:
//     - Create an admin account directly with role=ADMIN from the start
//     - Promote an existing user to admin (which also invalidates and reissues
//       their token so the role is consistent)
//     - Change any user's role properly through the API
//
// SECURITY:
//   These endpoints are protected by a secret setup key configured in
//   application.properties: app.admin.setup-key=your-secret-key-here
//   Set this to a long random string. Never expose it publicly.
//   In production, disable or remove this controller after initial setup.
// ─────────────────────────────────────────────────────────────────────────────

@RestController
@RequestMapping("/v1/admin-setup")
@CrossOrigin(origins = ["*"])
class AdminSetupController(
    private val userRepository : UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtUtil        : JwtUtil,
    @Value("\${app.admin.setup-key:CHANGE_ME_IN_PROPERTIES}")
    private val setupKey       : String
) {

    // ── POST /v1/admin-setup/create-admin ─────────────────────────────────────
    // Creates a brand-new user with role=ADMIN from the start.
    // No need to manually touch the database ever again.
    //
    // Body: { "setupKey": "...", "fullName": "...", "email": "...", "password": "..." }
    // Returns: JWT token with role=ADMIN already embedded
    @PostMapping("/create-admin")
    @Transactional
    fun createAdmin(
        @RequestBody request: CreateAdminRequest
    ): ResponseEntity<ApiResponse<AdminSetupResponse>> {

        if (request.setupKey != setupKey) {
            logger.warn { "Invalid setup key attempt for admin creation" }
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse(false, "Invalid setup key"))
        }

        if (userRepository.existsByEmail(request.email)) {
            // User already exists — just promote them and reissue token
            val existing = userRepository.findByEmail(request.email).get()
            existing.role     = UserRole.ADMIN
            existing.isActive = true
            val saved = userRepository.save(existing)

            // Issue a FRESH token with the correct role=ADMIN
            val newToken = jwtUtil.generateToken(saved.email, saved.role.name)

            logger.info { "Promoted existing user ${saved.email} to ADMIN and reissued token" }
            return ResponseEntity.ok(
                ApiResponse(
                    success = true,
                    message = "User promoted to ADMIN. Use the new token — discard any old tokens.",
                    data    = AdminSetupResponse(
                        userId   = saved.userId,
                        email    = saved.email,
                        fullName = saved.fullName,
                        role     = saved.role.name,
                        token    = newToken
                    )
                )
            )
        }

        // Create fresh admin account — role=ADMIN from the very beginning
        val admin = User(
            userId   = UUID.randomUUID().toString(),
            fullName = request.fullName,
            email    = request.email,
            password = passwordEncoder.encode(request.password),
            role     = UserRole.ADMIN,   // ← ADMIN from day one, no DB patching needed
            isActive = true
        )
        val saved = userRepository.save(admin)

        // Token issued with role=ADMIN — consistent from the start
        val token = jwtUtil.generateToken(saved.email, saved.role.name)

        logger.info { "Admin account created: ${saved.email}" }
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse(
                success = true,
                message = "Admin account created successfully.",
                data    = AdminSetupResponse(
                    userId   = saved.userId,
                    email    = saved.email,
                    fullName = saved.fullName,
                    role     = saved.role.name,
                    token    = token
                )
            )
        )
    }

    // ── POST /v1/admin-setup/change-role ──────────────────────────────────────
    // Properly changes a user's role AND tells you to get a new token.
    // This is the correct replacement for manually editing the DB.
    //
    // Body: { "setupKey": "...", "email": "...", "newRole": "ADMIN" }
    // After calling this, the user must log in again to get a token
    // with the updated role embedded. Old tokens keep the old role.
    @PostMapping("/change-role")
    @Transactional
    fun changeRole(
        @RequestBody request: ChangeRoleRequest
    ): ResponseEntity<ApiResponse<AdminSetupResponse>> {

        if (request.setupKey != setupKey) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse(false, "Invalid setup key"))
        }

        val user = userRepository.findByEmail(request.email).orElse(null)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse(false, "User not found: ${request.email}"))

        val newRole = runCatching { UserRole.valueOf(request.newRole.uppercase()) }
            .getOrElse {
                return ResponseEntity.badRequest()
                    .body(ApiResponse(false, "Invalid role '${request.newRole}'. Valid values: PARENT, ADMIN, VACCINATION_TEAM"))
            }

        user.role = newRole
        val saved = userRepository.save(user)

        // Issue a fresh token with the new role already inside
        val newToken = jwtUtil.generateToken(saved.email, saved.role.name)

        logger.info { "Role changed for ${saved.email}: → ${newRole.name}" }
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Role updated to ${newRole.name}. Use the new token — old tokens still carry the old role.",
                data    = AdminSetupResponse(
                    userId   = saved.userId,
                    email    = saved.email,
                    fullName = saved.fullName,
                    role     = saved.role.name,
                    token    = newToken    // ← give this token to the user immediately
                )
            )
        )
    }

    // ── GET /v1/admin-setup/verify?setupKey=... ───────────────────────────────
    // Quick check to verify your setup key is correct before doing anything
    @GetMapping("/verify")
    fun verify(@RequestParam setupKey: String): ResponseEntity<ApiResponse<Nothing>> {
        return if (setupKey == this.setupKey) {
            ResponseEntity.ok(ApiResponse(true, "Setup key is valid"))
        } else {
            ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse(false, "Invalid setup key"))
        }
    }
}

// ── DTOs ──────────────────────────────────────────────────────────────────────

data class CreateAdminRequest(
    val setupKey : String,
    val fullName : String  = "Admin",
    val email    : String,
    val password : String
)

data class ChangeRoleRequest(
    val setupKey : String,
    val email    : String,
    val newRole  : String   // "ADMIN", "PARENT", "VACCINATION_TEAM"
)

data class AdminSetupResponse(
    val userId   : String,
    val email    : String,
    val fullName : String,
    val role     : String,
    val token    : String   // use this token immediately — it has the correct role
)