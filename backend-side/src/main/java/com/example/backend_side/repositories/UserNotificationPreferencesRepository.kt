package com.example.backend_side.repositories

import com.example.backend_side.entity.UserNotificationPreferences
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface UserNotificationPreferencesRepository : JpaRepository<UserNotificationPreferences, String> {

    // userId IS the primary key now, so findById() works directly
    // These are kept for API compatibility with existing service calls
    fun findByUserId(userId: String): Optional<UserNotificationPreferences>

    fun existsByUserId(userId: String): Boolean

    @Query("SELECT p FROM UserNotificationPreferences p WHERE p.userId IN :userIds")
    fun findAllByUserIds(@Param("userIds") userIds: List<String>): List<UserNotificationPreferences>
}