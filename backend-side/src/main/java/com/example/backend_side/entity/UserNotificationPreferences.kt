package com.example.backend_side.entity

import jakarta.persistence.*

@Entity
@Table(
    name = "user_notification_preferences",
    uniqueConstraints = [UniqueConstraint(
        name        = "uq_prefs_user_id",
        columnNames = ["user_id"]
    )]
)
class UserNotificationPreferences(

    @Id
    @Column(name = "user_id", columnDefinition = "VARCHAR(36)", nullable = false)
    var userId: String = "",

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    var user: User? = null,

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

    @Column(name = "reminder_days_before", nullable = false)
    var reminderDaysBefore: Int = 3,

    @Version
    @Column(name = "version")
    var version: Long = 0


) : BaseEntity() {

    // Hibernate no-arg constructor — safe, no throw
    protected constructor() : this(userId = "")

    companion object {
        fun defaultsFor(user: User): UserNotificationPreferences =
            UserNotificationPreferences(userId = user.userId)
    }
}