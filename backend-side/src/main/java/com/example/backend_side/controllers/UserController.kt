package com.example.backend_side.controllers

import com.example.backend_side.*
import com.example.backend_side.entity.User
import com.example.backend_side.entity.UserNotificationPreferences
import com.example.backend_side.entity.UserRole
import com.example.backend_side.repositories.UserRepository
import jakarta.transaction.Transactional
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/v1/users")
@CrossOrigin(origins = ["*"])
class UserController(
    private val userRepository : UserRepository,
    private val passwordEncoder: org.springframework.security.crypto.password.PasswordEncoder,
    private val prefsRepository: com.example.backend_side.repositories.UserNotificationPreferencesRepository
) {

    @GetMapping
    fun getAllUsers(
        @RequestParam(defaultValue = "0")   page: Int,
        @RequestParam(defaultValue = "200") size: Int
    ): ResponseEntity<ApiResponse<PageResponse<UserResponse>>> {
        val pageable   = PageRequest.of(page, size, Sort.by("fullName").ascending())
        val resultPage = userRepository.findAll(pageable)
        val pageResponse = PageResponse(
            content       = resultPage.content.map { it.toResponse() },
            pageNumber    = resultPage.number,
            pageSize      = resultPage.size,
            totalElements = resultPage.totalElements,
            totalPages    = resultPage.totalPages,
            isLast        = resultPage.isLast
        )
        return ResponseEntity.ok(ApiResponse(true, "Users retrieved", pageResponse))
    }

    @GetMapping("/{userId}")
    fun getUserById(@PathVariable userId: String): ResponseEntity<ApiResponse<UserResponse>> =
        userRepository.findById(userId)
            .map { ResponseEntity.ok(ApiResponse(true, "User found", it.toResponse())) }
            .orElse(ResponseEntity.notFound().build())

    @GetMapping("/email/{email}")
    fun getUserByEmail(@PathVariable email: String): ResponseEntity<ApiResponse<UserResponse>> =
        userRepository.findByEmail(email)
            .map { ResponseEntity.ok(ApiResponse(true, "User found", it.toResponse())) }
            .orElse(ResponseEntity.notFound().build())

    // FIX ADDED: GET /v1/users/by-role/{role}
    // Previously missing — client's ApiService.getUsersByRole() calls this endpoint.
    // Without it every call to load team members in AdminBenchesScreen returned 404.
    @GetMapping("/by-role/{role}")
    fun getUsersByRole(
        @PathVariable role: String
    ): ResponseEntity<ApiResponse<List<UserResponse>>> {
        val userRole = runCatching {
            UserRole.valueOf(role.uppercase())
        }.getOrElse {
            throw BadRequestException("Invalid role '$role'. Valid values: PARENT, ADMIN, VACCINATION_TEAM")
        }
        val users = userRepository.findByRole(userRole).map { it.toResponse() }
        return ResponseEntity.ok(ApiResponse(true, "Users retrieved", users))
    }

    @PutMapping("/{userId}")
    fun updateUser(
        @PathVariable userId: String,
        @RequestBody request: UserUpdateRequest
    ): ResponseEntity<ApiResponse<UserResponse>> {
        val user = userRepository.findById(userId).orElse(null)
            ?: return ResponseEntity.notFound().build()

        request.fullName?.let        { user.fullName        = it }
        request.phone?.let           { user.phone           = it }
        request.address?.let         { user.address         = it }
        request.city?.let            { user.city            = it }
        request.profileImageUrl?.let { user.profileImageUrl = it }

        val saved = userRepository.save(user)
        return ResponseEntity.ok(ApiResponse(true, "User updated", saved.toResponse()))
    }

    @DeleteMapping("/{userId}")
    fun deleteUser(@PathVariable userId: String): ResponseEntity<ApiResponse<Unit>> =
        if (userRepository.existsById(userId)) {
            userRepository.deleteById(userId)
            ResponseEntity.ok(ApiResponse(true, "User deleted"))
        } else {
            ResponseEntity.notFound().build()
        }

    // FIX ADDED: PATCH /v1/users/{userId}/deactivate
    // Client calls apiService.deactivateUser(userId) which maps to this path.
    // Previously missing — caused 404 on every deactivate attempt.
    @PatchMapping("/{userId}/deactivate")
    @Transactional
    fun deactivateUser(@PathVariable userId: String): ResponseEntity<ApiResponse<UserResponse>> {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found: $userId") }
        user.isActive = false
        val saved = userRepository.save(user)
        return ResponseEntity.ok(ApiResponse(true, "User deactivated", saved.toResponse()))
    }

    // FIX ADDED: PATCH /v1/users/{userId}/activate
    // Client calls apiService.activateUser(userId) which maps to this path.
    // Previously missing — caused 404 on every activate attempt.
    @PatchMapping("/{userId}/activate")
    @Transactional
    fun activateUser(@PathVariable userId: String): ResponseEntity<ApiResponse<UserResponse>> {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found: $userId") }
        user.isActive = true
        val saved = userRepository.save(user)
        return ResponseEntity.ok(ApiResponse(true, "User activated", saved.toResponse()))
    }

    @PostMapping
    @Transactional
    fun createUser(
        @RequestBody request: UserCreateRequest
    ): ResponseEntity<ApiResponse<UserResponse>> {
        if (userRepository.existsByEmail(request.email)) {
            throw ResourceAlreadyExistsException("Email already registered: ${request.email}")
        }

        val role = runCatching {
            UserRole.valueOf(request.role.name.uppercase())
        }.getOrElse { request.role }

        val user = User(
            userId   = UUID.randomUUID().toString(),
            fullName = request.fullName,
            email    = request.email,
            password = passwordEncoder.encode(request.password ?: UUID.randomUUID().toString()),
            phone    = request.phone,
            city     = request.city,
            profileImageUrl = request.profileImageUrl,
            role     = role,
            isActive = true
        )
        val saved = userRepository.save(user)

        if (!prefsRepository.existsByUserId(saved.userId)) {
            prefsRepository.save(UserNotificationPreferences(userId = saved.userId))
        }

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse(true, "User created successfully", saved.toResponse()))
    }

    private fun User.toResponse() = UserResponse(
        userId          = userId,
        fullName        = fullName,
        email           = email,
        phone           = phone,
        address         = address,
        city            = city,
        profileImageUrl = profileImageUrl,
        role            = role,
        isActive        = isActive,
        createdAt       = createdAt,
        updatedAt       = updatedAt
    )
}