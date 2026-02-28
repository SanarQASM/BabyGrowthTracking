package com.example.backend_side.repositories

import com.example.backend_side.entity.User
import com.example.backend_side.entity.UserRole
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : JpaRepository<User, String> {

    fun findByEmail(email: String): Optional<User>

    fun existsByEmail(email: String): Boolean

    fun existsByPhone(phone: String): Boolean

    fun findByPhone(phone: String): Optional<User>

    fun findByRole(role: UserRole): List<User>

    fun findByIsActive(isActive: Boolean): List<User>

    fun findByCity(city: String): List<User>

    fun findByRoleAndIsActive(role: UserRole, isActive: Boolean): List<User>

    @Query("SELECT u FROM User u WHERE u.role = :role AND u.city = :city AND u.isActive = true")
    fun findActiveUsersByRoleAndCity(@Param("role") role: UserRole, @Param("city") city: String): List<User>

    @Query("SELECT u FROM User u WHERE LOWER(u.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    fun searchUsers(@Param("searchTerm") searchTerm: String): List<User>
}