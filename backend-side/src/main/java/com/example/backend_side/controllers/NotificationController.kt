package com.example.backend_side.controllers

import com.example.backend_side.entity.Notification
import com.example.backend_side.entity.NotificationPriority
import com.example.backend_side.entity.NotificationType
import com.example.backend_side.repositories.NotificationRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import java.util.*

// ─────────────────────────────────────────────────────────────────────────────
// NotificationController — FIXED
//
// KEY FIX: The client's AppNotification @Serializable model has these fields:
//   notificationId, userId, babyId, babyName, title, BODY (not message),
//   CATEGORY (not notificationType), priority, isRead, createdAt,
//   deepLinkRoute, imageUrl, actionLabel, actionRoute
//
// The old controller returned raw Notification entities where the message field
// is named "message" and notificationType is an enum object — both mismatch.
//
// This version always returns AppNotificationDto (shaped to match the client)
// wrapped in the standard ApiResponse envelope.
// ─────────────────────────────────────────────────────────────────────────────

@RestController
@RequestMapping("/v1/notifications")
@CrossOrigin(origins = ["*"])
class NotificationController(private val notificationRepository: NotificationRepository) {

    // ── GET /v1/notifications/user/{userId} ───────────────────────────────────
    // Returns { success, notifications, unreadCount, totalCount }
    // Matches NotificationListResponse on the client.
    @GetMapping("/user/{userId}")
    fun getNotificationsByUser(@PathVariable userId: String): ResponseEntity<Map<String, Any>> {
        val notifications = notificationRepository.findByUser_UserIdOrderByCreatedAtDesc(userId)
        val unreadCount   = notificationRepository.countUnreadByUserId(userId)
        return ResponseEntity.ok(mapOf(
            "success"       to true,
            "notifications" to notifications.map { it.toDto() },
            "unreadCount"   to unreadCount,
            "totalCount"    to notifications.size
        ))
    }

    // ── GET /v1/notifications/user/{userId}/unread-count ──────────────────────
    @GetMapping("/user/{userId}/unread-count")
    fun countUnreadNotifications(@PathVariable userId: String): ResponseEntity<Map<String, Long>> =
        ResponseEntity.ok(mapOf("count" to notificationRepository.countUnreadByUserId(userId)))

    // ── GET /v1/notifications/user/{userId}/unread ────────────────────────────
    @GetMapping("/user/{userId}/unread")
    fun getUnreadNotifications(@PathVariable userId: String): ResponseEntity<List<AppNotificationDto>> =
        ResponseEntity.ok(notificationRepository.findUnreadByUserId(userId).map { it.toDto() })

    // ── GET /v1/notifications/{notificationId} ────────────────────────────────
    @GetMapping("/{notificationId}")
    fun getById(@PathVariable notificationId: String): ResponseEntity<AppNotificationDto> =
        notificationRepository.findById(notificationId)
            .map { ResponseEntity.ok(it.toDto()) }
            .orElse(ResponseEntity.notFound().build())

    // ── GET /v1/notifications/baby/{babyId} ───────────────────────────────────
    @GetMapping("/baby/{babyId}")
    fun getByBaby(@PathVariable babyId: String): ResponseEntity<List<AppNotificationDto>> =
        ResponseEntity.ok(notificationRepository.findByBaby_BabyId(babyId).map { it.toDto() })

    // ── POST /v1/notifications ────────────────────────────────────────────────
    @PostMapping
    fun createNotification(@RequestBody notification: Notification): ResponseEntity<AppNotificationDto> {
        if (notification.notificationId.isEmpty()) notification.notificationId = UUID.randomUUID().toString()
        return ResponseEntity.status(HttpStatus.CREATED).body(notificationRepository.save(notification).toDto())
    }

    // ── PATCH /v1/notifications/{notificationId}/read ─────────────────────────
    @PatchMapping("/{notificationId}/read")
    fun markAsRead(@PathVariable notificationId: String): ResponseEntity<AppNotificationDto> =
        notificationRepository.findById(notificationId).map { notification ->
            notification.isRead = true; notification.readAt = LocalDateTime.now()
            ResponseEntity.ok(notificationRepository.save(notification).toDto())
        }.orElse(ResponseEntity.notFound().build())

    // ── PATCH /v1/notifications/user/{userId}/mark-all-read ───────────────────
    @PatchMapping("/user/{userId}/mark-all-read")
    fun markAllAsRead(@PathVariable userId: String): ResponseEntity<Map<String, String>> {
        val unread = notificationRepository.findUnreadByUserId(userId)
        unread.forEach { it.isRead = true; it.readAt = LocalDateTime.now() }
        notificationRepository.saveAll(unread)
        return ResponseEntity.ok(mapOf("message" to "All notifications marked as read"))
    }

    // ── PATCH /v1/notifications/{notificationId}/sent ────────────────────────
    @PatchMapping("/{notificationId}/sent")
    fun markAsSent(@PathVariable notificationId: String): ResponseEntity<AppNotificationDto> =
        notificationRepository.findById(notificationId).map { notification ->
            notification.isSent = true; notification.sentAt = LocalDateTime.now()
            ResponseEntity.ok(notificationRepository.save(notification).toDto())
        }.orElse(ResponseEntity.notFound().build())

    // ── DELETE /v1/notifications/{notificationId} ─────────────────────────────
    @DeleteMapping("/{notificationId}")
    fun deleteNotification(@PathVariable notificationId: String): ResponseEntity<Void> =
        if (notificationRepository.existsById(notificationId)) {
            notificationRepository.deleteById(notificationId)
            ResponseEntity.noContent().build()
        } else ResponseEntity.notFound().build()

    // ── Mapper: Notification entity → AppNotificationDto ─────────────────────
    // Maps "message" → "body" and "notificationType" → "category" so the
    // client's AppNotification @Serializable class deserializes correctly.
    private fun Notification.toDto() = AppNotificationDto(
        notificationId = notificationId,
        userId         = user?.userId ?: "",
        babyId         = baby?.babyId,
        babyName       = baby?.fullName,
        title          = title,
        body           = message,   // ← field rename: "message" in DB → "body" for client
        category       = mapCategory(notificationType),  // ← enum → string category name
        priority       = mapPriority(priority),
        isRead         = isRead,
        createdAt      = createdAt.toString(),
        deepLinkRoute  = null,   // stored in FCM data payload, not in DB currently
        imageUrl       = null,
        actionLabel    = null,
        actionRoute    = null
    )

    private fun mapCategory(type: NotificationType?): String = when (type) {
        NotificationType.VACCINATION_REMINDER -> "VACCINATION"
        NotificationType.GROWTH_ALERT         -> "GROWTH"
        NotificationType.APPOINTMENT_REMINDER -> "APPOINTMENT"
        NotificationType.HEALTH_ALERT         -> "HEALTH"
        NotificationType.MILESTONE            -> "BABY_PROFILE"
        NotificationType.GENERAL              -> "GENERAL"
        null                                  -> "GENERAL"
    }

    private fun mapPriority(p: NotificationPriority?): String = when (p) {
        NotificationPriority.URGENT -> "URGENT"
        NotificationPriority.HIGH   -> "HIGH"
        NotificationPriority.LOW    -> "LOW"
        else                        -> "MEDIUM"
    }
}

// ── DTO matching AppNotification on the client ────────────────────────────────
data class AppNotificationDto(
    val notificationId: String,
    val userId        : String,
    val babyId        : String?  = null,
    val babyName      : String?  = null,
    val title         : String,
    val body          : String,           // client field is "body", not "message"
    val category      : String,           // client field is "category", not "notificationType"
    val priority      : String  = "MEDIUM",
    val isRead        : Boolean = false,
    val createdAt     : String,
    val deepLinkRoute : String?  = null,
    val imageUrl      : String?  = null,
    val actionLabel   : String?  = null,
    val actionRoute   : String?  = null
)