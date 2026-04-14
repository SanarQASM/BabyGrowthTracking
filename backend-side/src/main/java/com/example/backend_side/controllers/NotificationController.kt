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
// FIX: mapCategory() previously mapped NotificationType.MILESTONE → "BABY_PROFILE"
// but the client's NotificationFilter enum has no BABY_PROFILE entry — it has
// DEVELOPMENT instead. This caused milestone notifications to silently fall into
// the GENERAL bucket and be un-filterable on the client.
//
// Corrected mapping:
//   MILESTONE → "DEVELOPMENT"   (matches NotificationFilter.DEVELOPMENT on client)
//
// Everything else is unchanged from the original.
// ─────────────────────────────────────────────────────────────────────────────

@RestController
@RequestMapping("/v1/notifications")
@CrossOrigin(origins = ["*"])
class NotificationController(private val notificationRepository: NotificationRepository) {

    // ── GET /v1/notifications/user/{userId} ───────────────────────────────────
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
        if (notification.notificationId.isEmpty())
            notification.notificationId = UUID.randomUUID().toString()
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(notificationRepository.save(notification).toDto())
    }

    // ── PATCH /v1/notifications/{notificationId}/read ─────────────────────────
    @PatchMapping("/{notificationId}/read")
    fun markAsRead(@PathVariable notificationId: String): ResponseEntity<AppNotificationDto> =
        notificationRepository.findById(notificationId).map { notification ->
            notification.isRead = true
            notification.readAt = LocalDateTime.now()
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
            notification.isSent = true
            notification.sentAt = LocalDateTime.now()
            ResponseEntity.ok(notificationRepository.save(notification).toDto())
        }.orElse(ResponseEntity.notFound().build())

    // ── DELETE /v1/notifications/{notificationId} ─────────────────────────────
    @DeleteMapping("/{notificationId}")
    fun deleteNotification(@PathVariable notificationId: String): ResponseEntity<Void> =
        if (notificationRepository.existsById(notificationId)) {
            notificationRepository.deleteById(notificationId)
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.notFound().build()
        }

    // ── Mapper: Notification entity → AppNotificationDto ─────────────────────
    private fun Notification.toDto() = AppNotificationDto(
        notificationId = notificationId,
        userId         = user?.userId ?: "",
        babyId         = baby?.babyId,
        babyName       = baby?.fullName,
        title          = title,
        body           = message,
        category       = mapCategory(notificationType),
        priority       = mapPriority(priority),
        isRead         = isRead,
        createdAt      = createdAt.toString(),
        deepLinkRoute  = null,
        imageUrl       = null,
        actionLabel    = null,
        actionRoute    = null
    )

    // FIX: MILESTONE was mapped to "BABY_PROFILE" which has no matching entry in
    // the client-side NotificationFilter enum. The client uses "DEVELOPMENT" for
    // the development/milestone filter chip. Corrected here so milestone
    // notifications are now visible under the Development filter tab.
    private fun mapCategory(type: NotificationType?): String = when (type) {
        NotificationType.VACCINATION_REMINDER -> "VACCINATION"
        NotificationType.GROWTH_ALERT         -> "GROWTH"
        NotificationType.APPOINTMENT_REMINDER -> "APPOINTMENT"
        NotificationType.HEALTH_ALERT         -> "HEALTH"
        NotificationType.MILESTONE            -> "DEVELOPMENT"   // FIX: was "BABY_PROFILE"
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
    val body          : String,
    val category      : String,
    val priority      : String   = "MEDIUM",
    val isRead        : Boolean  = false,
    val createdAt     : String,
    val deepLinkRoute : String?  = null,
    val imageUrl      : String?  = null,
    val actionLabel   : String?  = null,
    val actionRoute   : String?  = null
)