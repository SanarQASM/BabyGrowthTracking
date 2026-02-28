package com.example.backend_side.repositories

import com.example.backend_side.entity.Notification
import com.example.backend_side.entity.NotificationPriority
import com.example.backend_side.entity.NotificationType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface NotificationRepository : JpaRepository<Notification, String> {

    fun findByUser_UserId(userId: String): List<Notification>

    fun findByUser_UserIdOrderByCreatedAtDesc(userId: String): List<Notification>

    fun findByUser_UserIdAndIsRead(userId: String, isRead: Boolean): List<Notification>

    fun findByIsSent(isSent: Boolean): List<Notification>

    fun findByNotificationType(notificationType: NotificationType): List<Notification>

    fun findByPriority(priority: NotificationPriority): List<Notification>

    fun findByBaby_BabyId(babyId: String): List<Notification>

    @Query("SELECT n FROM Notification n WHERE n.user.userId = :userId AND n.isRead = false ORDER BY n.createdAt DESC")
    fun findUnreadByUserId(@Param("userId") userId: String): List<Notification>

    @Query("SELECT n FROM Notification n WHERE n.isSent = false AND n.scheduledSendTime <= :currentTime")
    fun findPendingNotificationsToSend(@Param("currentTime") currentTime: LocalDateTime): List<Notification>

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user.userId = :userId AND n.isRead = false")
    fun countUnreadByUserId(@Param("userId") userId: String): Long

    @Query("SELECT n FROM Notification n WHERE n.user.userId = :userId AND n.notificationType = :type AND n.isRead = false")
    fun findUnreadByUserIdAndType(
        @Param("userId") userId: String,
        @Param("type") type: NotificationType
    ): List<Notification>
}