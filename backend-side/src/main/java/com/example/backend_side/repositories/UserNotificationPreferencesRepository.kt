package com.example.backend_side.repositories

import com.example.backend_side.entity.UserNotificationPreferences
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface UserNotificationPreferencesRepository : JpaRepository<UserNotificationPreferences, String> {

    fun findByUser_UserId(userId: String): Optional<UserNotificationPreferences>

    // ── Existence check avoids loading the full entity just to test presence ──
    fun existsByUser_UserId(userId: String): Boolean

    // ── Used by scheduler to load prefs for multiple users efficiently ─────────
    @Query("SELECT p FROM UserNotificationPreferences p WHERE p.user.userId IN :userIds")
    fun findAllByUserIds(@Param("userIds") userIds: List<String>): List<UserNotificationPreferences>
}