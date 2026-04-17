package com.example.backend_side.controllers

import com.example.backend_side.entity.Notification
import com.example.backend_side.entity.NotificationPriority
import com.example.backend_side.entity.NotificationType
import com.example.backend_side.repositories.NotificationRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import java.util.*

// ─────────────────────────────────────────────────────────────────────────────
// NotificationController — FIXED
//
// BUG: getNotificationsByUser() accepted page and size query params but the
//      repository call findByUser_UserIdOrderByCreatedAtDesc(userId) ignores them.
//      For users with hundreds of notifications this returns the entire history
//      on every poll, wastes bandwidth, and makes client-side pagination wrong
//      (hasMore never triggers because the backend always sends everything).
//
// FIX:
//  • Use PageRequest.of(page, size, Sort.by("createdAt").descending()) so the
//    query is actually paginated at the DB level.
//  • Add a findByUser_UserIdOrderByCreatedAtDesc(userId, pageable) overload in
//    NotificationRepository (see NotificationRepository.kt fix file).
//  • Return unreadCount and totalCount in the same envelope so the client's
//    NotificationListResponse deserializes correctly.
//  • All other endpoints (mark-read, delete, etc.) are unchanged.
// ─────────────────────────────────────────────────────────────────────────────

@RestController
@RequestMapping("/v1/notifications")
@CrossOrigin(origins = ["*"])
class NotificationController(private val notificationRepository: NotificationRepository) {

    // ── GET /v1/notifications/user/{userId}?page=0&size=50 ────────────────────
    // FIX: Now actually pages at DB level. Default page=0, size=50 matches
    //      the client's loadNotifications() call.
    @GetMapping("/user/{userId}")
    fun getNotificationsByUser(
        @PathVariable userId: String,
        @RequestParam(defaultValue = "0")  page: Int,
        @RequestParam(defaultValue = "50") size: Int
    ): ResponseEntity<Map<String, Any>> {
        val pageable      = PageRequest.of(page, size, Sort.by("createdAt").descending())
        val pageResult    = notificationRepository.findPagedByUserId(userId, pageable)
        val unreadCount   = notificationRepository.countUnreadByUserId(userId)

        return ResponseEntity.ok(mapOf(
            "success"       to true,
            "notifications" to pageResult.content.map { it.toDto() },
            "unreadCount"   to unreadCount,
            "totalCount"    to pageResult.totalElements.toInt()
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
        } else ResponseEntity.notFound().build()

    // ── Mapper ────────────────────────────────────────────────────────────────
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

// ── DTO ───────────────────────────────────────────────────────────────────────
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