package com.example.backend_side.controllers

import com.example.backend_side.*
import com.example.backend_side.entity.User
import com.example.backend_side.repositories.UserRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/users")
@CrossOrigin(origins = ["*"])
class UserController(private val userRepository: UserRepository) {

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
}