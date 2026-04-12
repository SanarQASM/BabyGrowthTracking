package com.example.backend_side.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "notifications")
data class Notification(
    @Id
    @Column(name = "notification_id", columnDefinition = "VARCHAR(36)", updatable = false, nullable = false)
    var notificationId: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "baby_id")
    var baby: Baby? = null,

    @Column(name = "notification_type", columnDefinition = "ENUM('vaccination_reminder','growth_alert','appointment_reminder','milestone','health_alert','general')")
    var notificationType: NotificationType? = null,

    @Column(name = "title", nullable = false)
    var title: String = "",

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    var message: String = "",

    @Column(name = "scheduled_send_time")
    var scheduledSendTime: LocalDateTime? = null,

    @Column(name = "sent_at")
    var sentAt: LocalDateTime? = null,

    @Column(name = "is_sent")
    var isSent: Boolean = false,

    @Column(name = "is_read")
    var isRead: Boolean = false,

    @Column(name = "read_at")
    var readAt: LocalDateTime? = null,

    @Column(name = "dedupe_key", length = 120)
    var dedupeKey: String? = null,

    @Column(name = "priority", columnDefinition = "ENUM('low','medium','high','urgent')")
    var priority: NotificationPriority = NotificationPriority.MEDIUM,

    // ✅ notifications table has no updated_at column — only created_at
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
)