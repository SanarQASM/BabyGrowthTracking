package com.example.backend_side.entity

import jakarta.persistence.*

@Entity
@Table(name = "user_notification_preferences")
data class UserNotificationPreferences(

    @Id
    var userId: String = "",

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    var user: User,

    @Column(name = "vaccination", nullable = false)
    var vaccination: Boolean = true,

    @Column(name = "growth", nullable = false)
    var growth: Boolean = true,

    @Column(name = "appointment", nullable = false)
    var appointment: Boolean = true,

    @Column(name = "health", nullable = false)
    var health: Boolean = true,

    @Column(name = "development", nullable = false)
    var development: Boolean = true,

    @Column(name = "milestones", nullable = false)
    var milestones: Boolean = true,

    @Column(name = "general", nullable = false)
    var general: Boolean = true,

    /** How many days before an event the reminder fires (1..14). */
    @Column(name = "reminder_days_before", nullable = false)
    var reminderDaysBefore: Int = 3

) : BaseEntity()