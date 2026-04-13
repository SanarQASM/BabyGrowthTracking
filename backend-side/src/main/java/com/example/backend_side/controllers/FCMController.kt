
package com.example.backend_side.controllers

import com.example.backend_side.*
import com.example.backend_side.entity.FcmToken
import com.example.backend_side.entity.Notification
import com.example.backend_side.entity.NotificationPriority
import com.example.backend_side.entity.NotificationType
import com.example.backend_side.repositories.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID
import java.time.LocalDateTime

data class RegisterFcmTokenRequest(
    val userId   : String,
    val fcmToken : String,
    val platform : String
)

data class SendNotificationRequest(
    val userId   : String,
    val title    : String,
    val body     : String,
    val category : String = "GENERAL",
    val priority : String = "MEDIUM",
    val babyId   : String? = null,
    val deepLinkRoute : String? = null,
    val actionLabel   : String? = null,
    val actionRoute   : String? = null
)

@RestController
@RequestMapping("/v1/fcm")
@CrossOrigin(origins = ["*"])
class FCMController(
    private val fcmService             : FCMService,
    private val fcmTokenRepository     : FcmTokenRepository,
    private val notificationRepository : NotificationRepository,
    private val userRepository         : UserRepository
) {

    // ── Register device FCM token ─────────────────────────────────────────────

    @PostMapping("/register-token")
    fun registerToken(@RequestBody request: RegisterFcmTokenRequest): ResponseEntity<ApiResponse<Unit>> {
        return try {
            val existing = fcmTokenRepository.findByUserIdAndPlatform(request.userId, request.platform)
            if (existing != null) {
                existing.token    = request.fcmToken
                existing.isActive = true
                fcmTokenRepository.save(existing)
            } else {
                fcmTokenRepository.save(
                    FcmToken(
                        userId   = request.userId,
                        token    = request.fcmToken,
                        platform = request.platform
                    )
                )
            }
            ResponseEntity.ok(ApiResponse(true, "Token registered"))
        } catch (e: Exception) {
            ResponseEntity.ok(ApiResponse(false, "Failed: ${e.message}"))
        }
    }

    // ── Send notification to a specific user (admin/internal use) ─────────────

    @PostMapping("/send")
    fun sendNotification(@RequestBody request: SendNotificationRequest): ResponseEntity<ApiResponse<Unit>> {
        return try {
            val tokens = fcmTokenRepository.findByUserIdAndIsActive(request.userId, true)
            val data = buildMap<String, String> {
                put("category",     request.category)
                put("priority",     request.priority)
                request.babyId?.let        { put("babyId", it) }
                request.deepLinkRoute?.let { put("deepLinkRoute", it) }
                request.actionLabel?.let   { put("actionLabel", it) }
                request.actionRoute?.let   { put("actionRoute", it) }
            }

            var sent = 0
            tokens.forEach { tokenEntity ->
                if (fcmService.sendToDevice(
                        fcmToken = tokenEntity.token,
                        title    = request.title,
                        body     = request.body,
                        data     = data,
                        priority = request.priority
                    )) sent++
            }

            // Persist to notification history
            saveNotificationRecord(request)

            ResponseEntity.ok(ApiResponse(true, "Sent to $sent devices"))
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(ApiResponse(false, e.message ?: "Error"))
        }
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    private fun saveNotificationRecord(request: SendNotificationRequest) {
        val user = userRepository.findById(request.userId).orElse(null) ?: return
        val notif = Notification(
            notificationId   = UUID.randomUUID().toString(),
            user             = user,
            title            = request.title,
            message          = request.body,
            notificationType = runCatching {
                NotificationType.valueOf(request.category.uppercase().replace("-", "_"))
            }.getOrDefault(NotificationType.GENERAL),
            priority         = runCatching {
                NotificationPriority.valueOf(request.priority.uppercase())
            }.getOrDefault(NotificationPriority.MEDIUM),
            isSent           = true,
            sentAt           = LocalDateTime.now(),
            createdAt        = LocalDateTime.now()
        )
        notificationRepository.save(notif)
    }

    private fun Notification.toApiModel() = mapOf(
        "notificationId" to notificationId,
        "userId"         to (user?.userId ?: ""),
        "babyId"         to (baby?.babyId ?: ""),
        "babyName"       to (baby?.fullName ?: ""),
        "title"          to title,
        "body"           to message,
        "category"       to (notificationType?.name ?: "GENERAL"),
        "priority"       to priority.name,
        "isRead"         to isRead,
        "createdAt"      to createdAt.toString(),
        "deepLinkRoute"  to "",
        "actionLabel"    to "",
        "actionRoute"    to ""
    )
}