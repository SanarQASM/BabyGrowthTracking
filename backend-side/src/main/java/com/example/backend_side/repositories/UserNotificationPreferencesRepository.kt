package com.example.backend_side.repositories

import com.example.backend_side.entity.UserNotificationPreferences
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface UserNotificationPreferencesRepository : JpaRepository<UserNotificationPreferences, String> {
    fun findByUser_UserId(userId: String): Optional<UserNotificationPreferences>
}