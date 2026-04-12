package com.example.backend_side.repositories

import com.example.backend_side.entity.FcmToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FcmTokenRepository : JpaRepository<FcmToken, Long> {
    fun findByUserId(userId: String): List<FcmToken>
    fun findByUserIdAndPlatform(userId: String, platform: String): FcmToken?
    fun findByUserIdAndIsActive(userId: String, isActive: Boolean): List<FcmToken>
}