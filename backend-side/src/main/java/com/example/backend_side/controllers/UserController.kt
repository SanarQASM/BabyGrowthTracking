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

    /**
     * FIX: PageResponse deserialization error on the client side.
     *
     * BUG: The client's PageResponse data class uses Jackson to deserialize the
     * response. Spring's Page<T>.map{} produces a JSON object whose pagination
     * fields are named "number" and "size" (Spring defaults), but the client DTO
     * has fields named "pageNumber" and "pageSize". This mismatch caused:
     *
     *   "Illegal input: Fields [size, number] are required for type with serial name
     *    'org.example.project...PageResponse', but they were missing at path: $.data"
     *
     * FIX: Build the PageResponse manually using the backend's own PageResponse DTO
     * (which already has pageNumber / pageSize field names) so the serialised JSON
     * always contains those exact field names. This keeps backend ↔ client in sync.
     *
     * Default page size raised to 200 to match AdminViewModel.loadDashboardData()
     * which calls getAllUsers(page=0, size=200).
     */
    @GetMapping
    fun getAllUsers(
        @RequestParam(defaultValue = "0")   page: Int,
        @RequestParam(defaultValue = "200") size: Int
    ): ResponseEntity<ApiResponse<PageResponse<UserResponse>>> {

        val pageable   = PageRequest.of(page, size, Sort.by("fullName").ascending())
        val resultPage = userRepository.findAll(pageable)

        // Build the DTO explicitly so field names match what the Kotlin client expects:
        //   pageNumber, pageSize, totalElements, totalPages, isLast, content
        val pageResponse = PageResponse(
            content       = resultPage.content.map { it.toResponse() },
            pageNumber    = resultPage.number,          // "pageNumber" in JSON ✓
            pageSize      = resultPage.size,            // "pageSize"   in JSON ✓
            totalElements = resultPage.totalElements,
            totalPages    = resultPage.totalPages,
            isLast        = resultPage.isLast
        )

        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Users retrieved",
                data    = pageResponse
            )
        )
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

    // ── Mapper ───────────────────────────────────────────────────────────────

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

        // Always create preferences row together with the user
        if (!prefsRepository.existsByUserId(saved.userId)) {
            prefsRepository.save(UserNotificationPreferences(userId = saved.userId))
        }

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse(true, "User created successfully", saved.toResponse()))
    }
}