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

@RestController
@RequestMapping("/v1/notifications")
@CrossOrigin(origins = ["*"])
class NotificationController(private val notificationRepository: NotificationRepository) {

    @GetMapping
    fun getAllNotifications(): ResponseEntity<List<Notification>> {
        return ResponseEntity.ok(notificationRepository.findAll())
    }

    @GetMapping("/{notificationId}")
    fun getNotificationById(@PathVariable notificationId: String): ResponseEntity<Notification> {
        return notificationRepository.findById(notificationId)
            .map { ResponseEntity.ok(it) }
            .orElse(ResponseEntity.notFound().build())
    }

    @GetMapping("/user/{userId}")
    fun getNotificationsByUser(@PathVariable userId: String): ResponseEntity<List<Notification>> {
        return ResponseEntity.ok(notificationRepository.findByUser_UserIdOrderByCreatedAtDesc(userId))
    }

    @GetMapping("/user/{userId}/unread")
    fun getUnreadNotificationsByUser(@PathVariable userId: String): ResponseEntity<List<Notification>> {
        return ResponseEntity.ok(notificationRepository.findUnreadByUserId(userId))
    }

    @GetMapping("/user/{userId}/unread-count")
    fun countUnreadNotifications(@PathVariable userId: String): ResponseEntity<Map<String, Long>> {
        val count = notificationRepository.countUnreadByUserId(userId)
        return ResponseEntity.ok(mapOf("count" to count))
    }

    @GetMapping("/user/{userId}/type/{type}")
    fun getUnreadNotificationsByType(
        @PathVariable userId: String,
        @PathVariable type: NotificationType
    ): ResponseEntity<List<Notification>> {
        return ResponseEntity.ok(notificationRepository.findUnreadByUserIdAndType(userId, type))
    }

    @GetMapping("/baby/{babyId}")
    fun getNotificationsByBaby(@PathVariable babyId: String): ResponseEntity<List<Notification>> {
        return ResponseEntity.ok(notificationRepository.findByBaby_BabyId(babyId))
    }

    @GetMapping("/type/{type}")
    fun getNotificationsByType(@PathVariable type: NotificationType): ResponseEntity<List<Notification>> {
        return ResponseEntity.ok(notificationRepository.findByNotificationType(type))
    }

    @GetMapping("/priority/{priority}")
    fun getNotificationsByPriority(@PathVariable priority: NotificationPriority): ResponseEntity<List<Notification>> {
        return ResponseEntity.ok(notificationRepository.findByPriority(priority))
    }

    @GetMapping("/pending")
    fun getPendingNotifications(): ResponseEntity<List<Notification>> {
        return ResponseEntity.ok(notificationRepository.findPendingNotificationsToSend(LocalDateTime.now()))
    }

    @PostMapping
    fun createNotification(@RequestBody notification: Notification): ResponseEntity<Notification> {
        if (notification.notificationId.isEmpty()) {
            notification.notificationId = UUID.randomUUID().toString()
        }
        val savedNotification = notificationRepository.save(notification)
        return ResponseEntity.status(HttpStatus.CREATED).body(savedNotification)
    }

    @PutMapping("/{notificationId}")
    fun updateNotification(
        @PathVariable notificationId: String,
        @RequestBody notification: Notification
    ): ResponseEntity<Notification> {
        return if (notificationRepository.existsById(notificationId)) {
            notification.notificationId = notificationId
            ResponseEntity.ok(notificationRepository.save(notification))
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PatchMapping("/{notificationId}/read")
    fun markAsRead(@PathVariable notificationId: String): ResponseEntity<Notification> {
        return notificationRepository.findById(notificationId)
            .map { notification ->
                notification.isRead = true
                notification.readAt = LocalDateTime.now()
                ResponseEntity.ok(notificationRepository.save(notification))
            }
            .orElse(ResponseEntity.notFound().build())
    }

    @PatchMapping("/{notificationId}/sent")
    fun markAsSent(@PathVariable notificationId: String): ResponseEntity<Notification> {
        return notificationRepository.findById(notificationId)
            .map { notification ->
                notification.isSent = true
                notification.sentAt = LocalDateTime.now()
                ResponseEntity.ok(notificationRepository.save(notification))
            }
            .orElse(ResponseEntity.notFound().build())
    }

    @PatchMapping("/user/{userId}/mark-all-read")
    fun markAllAsRead(@PathVariable userId: String): ResponseEntity<Map<String, String>> {
        val unreadNotifications = notificationRepository.findUnreadByUserId(userId)
        unreadNotifications.forEach { notification ->
            notification.isRead = true
            notification.readAt = LocalDateTime.now()
        }
        notificationRepository.saveAll(unreadNotifications)
        return ResponseEntity.ok(mapOf("message" to "All notifications marked as read"))
    }

    @DeleteMapping("/{notificationId}")
    fun deleteNotification(@PathVariable notificationId: String): ResponseEntity<Void> {
        return if (notificationRepository.existsById(notificationId)) {
            notificationRepository.deleteById(notificationId)
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }
}