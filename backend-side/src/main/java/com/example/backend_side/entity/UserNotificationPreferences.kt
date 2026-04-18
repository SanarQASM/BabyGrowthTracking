package com.example.backend_side.entity

import jakarta.persistence.*

@Entity
@Table(name = "user_notification_preferences")
class UserNotificationPreferences(

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    var user: User

) : BaseEntity() {

    @Id
    var userId: String = ""

    @Column(name = "vaccination", nullable = false)
    var vaccination: Boolean = true

    @Column(name = "growth", nullable = false)
    var growth: Boolean = true

    @Column(name = "appointment", nullable = false)
    var appointment: Boolean = true

    @Column(name = "health", nullable = false)
    var health: Boolean = true

    @Column(name = "development", nullable = false)
    var development: Boolean = true

    @Column(name = "milestones", nullable = false)
    var milestones: Boolean = true

    @Column(name = "general", nullable = false)
    var general: Boolean = true

    /** How many days before an event the reminder fires (1..14). */
    @Column(name = "reminder_days_before", nullable = false)
    var reminderDaysBefore: Int = 3

    @Version
    @Column(name = "version", nullable = false)
    var version: Long = 0L

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UserNotificationPreferences) return false
        return userId == other.userId
    }

    override fun hashCode(): Int = userId.hashCode()

    override fun toString(): String =
        "UserNotificationPreferences(userId=$userId, vaccination=$vaccination, growth=$growth)"
}