package com.example.backend_side.repositories

import com.example.backend_side.entity.Notification
import com.example.backend_side.entity.NotificationPriority
import com.example.backend_side.entity.NotificationType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

// ─────────────────────────────────────────────────────────────────────────────
// NotificationRepository — FIXED
//
// BUG: There was no paginated query method. NotificationController called
//      findByUser_UserIdOrderByCreatedAtDesc(userId) which returns the full
//      history. For active users this grows unboundedly and makes every
//      polling call expensive.
//
// FIX: Added findPagedByUserId(userId, pageable) — a @Query method that
//      accepts a Pageable, enabling DB-level LIMIT/OFFSET pagination.
//      NotificationController now calls this with PageRequest.of(page, size).
// ─────────────────────────────────────────────────────────────────────────────

@Repository
interface NotificationRepository : JpaRepository<Notification, String> {

    fun findByUser_UserId(userId: String): List<Notification>

    fun findByUser_UserIdOrderByCreatedAtDesc(userId: String): List<Notification>

    // ── FIX: paginated version ─────────────────────────────────────────────────
    @Query("""
        SELECT n FROM Notification n
        WHERE n.user.userId = :userId
        ORDER BY n.createdAt DESC
    """)
    fun findPagedByUserId(
        @Param("userId") userId  : String,
        pageable                  : Pageable
    ): Page<Notification>

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

    @Query("""
        SELECT n FROM Notification n
        WHERE n.user.userId = :userId
        AND n.notificationType = :type
        AND n.isRead = false
    """)
    fun findUnreadByUserIdAndType(
        @Param("userId") userId: String,
        @Param("type")   type  : NotificationType
    ): List<Notification>

    @Query("""
        SELECT CASE WHEN COUNT(n) > 0 THEN true ELSE false END
        FROM Notification n
        WHERE n.user.userId = :userId
        AND n.dedupeKey = :dedupeKey
        AND n.isSent = true
        AND n.createdAt >= :since
    """)
    fun existsSentNotificationAfter(
        @Param("userId")    userId   : String,
        @Param("dedupeKey") dedupeKey: String,
        @Param("since")     since    : LocalDateTime
    ): Boolean
}