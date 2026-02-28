package com.example.backend_side.controllers

import com.example.backend_side.entity.User
import com.example.backend_side.entity.UserRole
import com.example.backend_side.repositories.UserRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/users")  // ✅ removed /api — context-path already adds it
@CrossOrigin(origins = ["*"])
class UserController(private val userRepository: UserRepository) {

    @GetMapping
    fun getAllUsers(): ResponseEntity<List<User>> =
        ResponseEntity.ok(userRepository.findAll())

    @GetMapping("/{userId}")
    fun getUserById(@PathVariable userId: String): ResponseEntity<User> =
        userRepository.findById(userId)
            .map { ResponseEntity.ok(it) }
            .orElse(ResponseEntity.notFound().build())

    @GetMapping("/email/{email}")
    fun getUserByEmail(@PathVariable email: String): ResponseEntity<User> =
        userRepository.findByEmail(email)
            .map { ResponseEntity.ok(it) }
            .orElse(ResponseEntity.notFound().build())

    @GetMapping("/role/{role}")
    fun getUsersByRole(@PathVariable role: UserRole): ResponseEntity<List<User>> =
        ResponseEntity.ok(userRepository.findByRole(role))

    @GetMapping("/city/{city}")
    fun getUsersByCity(@PathVariable city: String): ResponseEntity<List<User>> =
        ResponseEntity.ok(userRepository.findByCity(city))

    @GetMapping("/active")
    fun getActiveUsers(@RequestParam(defaultValue = "true") isActive: Boolean): ResponseEntity<List<User>> =
        ResponseEntity.ok(userRepository.findByIsActive(isActive))

    @GetMapping("/search")
    fun searchUsers(@RequestParam searchTerm: String): ResponseEntity<List<User>> =
        ResponseEntity.ok(userRepository.searchUsers(searchTerm))

    @PutMapping("/{userId}")
    fun updateUser(@PathVariable userId: String, @RequestBody user: User): ResponseEntity<User> =
        if (userRepository.existsById(userId)) {
            user.userId = userId
            ResponseEntity.ok(userRepository.save(user))
        } else {
            ResponseEntity.notFound().build()
        }

    @PatchMapping("/{userId}/activate")
    fun activateUser(@PathVariable userId: String): ResponseEntity<User> =
        userRepository.findById(userId).map { user ->
            user.isActive = true
            ResponseEntity.ok(userRepository.save(user))
        }.orElse(ResponseEntity.notFound().build())

    @PatchMapping("/{userId}/deactivate")
    fun deactivateUser(@PathVariable userId: String): ResponseEntity<User> =
        userRepository.findById(userId).map { user ->
            user.isActive = false
            ResponseEntity.ok(userRepository.save(user))
        }.orElse(ResponseEntity.notFound().build())

    @DeleteMapping("/{userId}")
    fun deleteUser(@PathVariable userId: String): ResponseEntity<Void> =
        if (userRepository.existsById(userId)) {
            userRepository.deleteById(userId)
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.notFound().build()
        }
}