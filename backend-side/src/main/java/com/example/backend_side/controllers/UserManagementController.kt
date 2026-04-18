package com.example.backend_side.controllers

import com.example.backend_side.*
import com.example.backend_side.entity.User
import com.example.backend_side.entity.UserNotificationPreferences
import com.example.backend_side.entity.UserRole
import com.example.backend_side.repositories.UserNotificationPreferencesRepository
import com.example.backend_side.repositories.UserRepository
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/v1/admin/users")
@Tag(name = "Admin User Management", description = "Admin endpoints for managing users and team members")
class UserManagementController(
    private val userRepository : UserRepository,
    private val prefsRepository: UserNotificationPreferencesRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtUtil        : JwtUtil
) {

    // ── POST /v1/admin/users ──────────────────────────────────────────────────
    // Create any user with any role — admin use only
    @PostMapping
    @Transactional
    @Operation(summary = "Create a new user — Admin only")
    fun createUser(
        @RequestBody request: AdminCreateUserRequest
    ): ResponseEntity<ApiResponse<UserResponse>> {

        if (userRepository.existsByEmail(request.email)) {
            throw ResourceAlreadyExistsException("Email already registered: ${request.email}")
        }

        val role = runCatching {
            UserRole.valueOf(request.role.uppercase())
        }.getOrElse {
            throw BadRequestException("Invalid role '${request.role}'. Valid: PARENT, ADMIN, VACCINATION_TEAM")
        }

        val user = User(
            userId   = UUID.randomUUID().toString(),
            fullName = request.fullName,
            email    = request.email,
            password = passwordEncoder.encode(request.password),
            phone    = request.phone,
            city     = request.city,
            role     = role,
            isActive = true
        )
        val saved = userRepository.save(user)

        // Always create preferences row with the user
        if (!prefsRepository.existsByUserId(saved.userId)) {
            prefsRepository.save(UserNotificationPreferences(userId = saved.userId))
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse(true, "User created successfully", saved.toResponse())
        )
    }

    // ── POST /v1/admin/users/vaccination-team ─────────────────────────────────
    // Convenience endpoint specifically for creating vaccination team members
    @PostMapping("/vaccination-team")
    @Transactional
    @Operation(summary = "Create a vaccination team member — Admin only")
    fun createVaccinationTeamMember(
        @RequestBody request: AdminCreateUserRequest
    ): ResponseEntity<ApiResponse<UserResponse>> {
        // Force role to VACCINATION_TEAM regardless of what was sent
        val forced = request.copy(role = "VACCINATION_TEAM")
        return createUser(forced)
    }

    // ── GET /v1/admin/users/vaccination-team ──────────────────────────────────
    // List all vaccination team members
    @GetMapping("/vaccination-team")
    @Operation(summary = "Get all vaccination team members")
    fun getVaccinationTeamMembers(): ResponseEntity<ApiResponse<List<UserResponse>>> {
        val members = userRepository.findByRole(UserRole.VACCINATION_TEAM)
            .map { it.toResponse() }
        return ResponseEntity.ok(ApiResponse(true, "Team members retrieved", members))
    }

    // ── GET /v1/admin/users/role/{role} ───────────────────────────────────────
    // List users by role
    @GetMapping("/role/{role}")
    @Operation(summary = "Get users by role")
    fun getUsersByRole(
        @PathVariable role: String
    ): ResponseEntity<ApiResponse<List<UserResponse>>> {
        val userRole = runCatching {
            UserRole.valueOf(role.uppercase())
        }.getOrElse {
            throw BadRequestException("Invalid role '$role'")
        }
        val users = userRepository.findByRole(userRole).map { it.toResponse() }
        return ResponseEntity.ok(ApiResponse(true, "Users retrieved", users))
    }

    // ── PATCH /v1/admin/users/{userId}/toggle-active ──────────────────────────
    // Activate or deactivate a user
    @PatchMapping("/{userId}/toggle-active")
    @Transactional
    @Operation(summary = "Toggle user active status")
    fun toggleActive(
        @PathVariable userId: String
    ): ResponseEntity<ApiResponse<UserResponse>> {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found: $userId") }
        user.isActive = !user.isActive
        val saved = userRepository.save(user)
        return ResponseEntity.ok(
            ApiResponse(true, "User ${if (saved.isActive) "activated" else "deactivated"}", saved.toResponse())
        )
    }

    // ── PATCH /v1/admin/users/{userId}/role ───────────────────────────────────
    // Change a user's role and return a fresh token
    @PatchMapping("/{userId}/role")
    @Transactional
    @Operation(summary = "Change user role — returns fresh JWT")
    fun changeRole(
        @PathVariable userId: String,
        @RequestBody request: ChangeUserRoleRequest
    ): ResponseEntity<ApiResponse<RoleChangeResponse>> {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found: $userId") }

        val newRole = runCatching {
            UserRole.valueOf(request.role.uppercase())
        }.getOrElse {
            throw BadRequestException("Invalid role '${request.role}'")
        }

        user.role = newRole
        val saved = userRepository.save(user)
        val newToken = jwtUtil.generateToken(saved.email, saved.role.name)

        return ResponseEntity.ok(
            ApiResponse(true, "Role updated to ${newRole.name}",
                RoleChangeResponse(
                    userId   = saved.userId,
                    email    = saved.email,
                    role     = saved.role.name,
                    newToken = newToken
                )
            )
        )
    }

    // ── DELETE /v1/admin/users/{userId} ───────────────────────────────────────
    @DeleteMapping("/{userId}")
    @Transactional
    @Operation(summary = "Delete a user — Admin only")
    fun deleteUser(
        @PathVariable userId: String
    ): ResponseEntity<ApiResponse<Nothing>> {
        if (!userRepository.existsById(userId)) {
            throw ResourceNotFoundException("User not found: $userId")
        }
        userRepository.deleteById(userId)
        return ResponseEntity.ok(ApiResponse(true, "User deleted"))
    }

    // ── Mapper ────────────────────────────────────────────────────────────────
    private fun User.toResponse() = UserResponse(
        userId          = userId,
        fullName        = fullName,
        email           = email,
        phone           = phone,
        city            = city,
        profileImageUrl = profileImageUrl,
        role            = role,
        isActive        = isActive,
        createdAt       = createdAt,
        updatedAt       = updatedAt
    )
}

// ── DTOs ──────────────────────────────────────────────────────────────────────

data class AdminCreateUserRequest @JsonCreator constructor(
    @JsonProperty("fullName")
    @field:NotBlank(message = "Full name is required")
    val fullName: String,

    @JsonProperty("email")
    @field:Email(message = "Invalid email")
    @field:NotBlank(message = "Email is required")
    val email: String,

    @JsonProperty("password")
    @field:Size(min = 6, message = "Password must be at least 6 characters")
    @field:NotBlank(message = "Password is required")
    val password: String,

    @JsonProperty("role")
    val role: String = "VACCINATION_TEAM",

    @JsonProperty("phone")
    val phone: String? = null,

    @JsonProperty("city")
    val city: String? = null
)

data class ChangeUserRoleRequest @JsonCreator constructor(
    @JsonProperty("role")
    @field:NotBlank(message = "Role is required")
    val role: String
)

data class RoleChangeResponse(
    val userId  : String,
    val email   : String,
    val role    : String,
    val newToken: String
)