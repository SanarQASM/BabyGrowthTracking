package com.example.backend_side.entity

import jakarta.persistence.*

// ─────────────────────────────────────────────────────────────────────────────
// FIX: StaleObjectStateException root cause
//
// ROOT CAUSE 1 — `data class` with @MapsId:
//   Hibernate's dirty-checking uses equals/hashCode. Kotlin `data class`
//   generates these from all constructor fields including `userId`. When
//   @MapsId derives `userId` from the associated `user` entity after persist,
//   Hibernate sees the object as "changed" (userId went from "" to the real UUID)
//   and tries to issue an UPDATE on a row that was just INSERTed — this is the
//   StaleObjectStateException.
//   FIX: Change to a plain `class` so Hibernate controls identity via its own
//   internal mechanisms (the persistence context / first-level cache).
//
// ROOT CAUSE 2 — default `userId: String = ""`:
//   With @MapsId the @Id field must NOT have a default value. A default of ""
//   makes Hibernate think this is a detached entity with id="", causing it to
//   attempt a MERGE instead of a INSERT. Hibernate then can't find a row with
//   id="" and throws StaleObjectStateException.
//   FIX: Remove the default. Let @MapsId populate it from the User entity.
//
// ROOT CAUSE 3 — UserNotificationPreferences constructor requires `user: User`
//   but callers sometimes build the object before the user is fully in-session.
//   FIX: Keep `user` as a required constructor param (correct), but ensure
//   the entity is only built inside a @Transactional boundary (fixed in
//   NotificationPreferencesController and PushNotificationScheduler).
// ─────────────────────────────────────────────────────────────────────────────

@Entity
@Table(name = "user_notification_preferences")
class UserNotificationPreferences(

    // @MapsId copies the value from user.userId into this @Id field after persist.
    // Do NOT give this a default — Hibernate must control it.
    @Id
    var userId: String,

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

) : BaseEntity() {

    // ── No-arg constructor required by Hibernate ──────────────────────────────
    // Hibernate needs a no-arg constructor to instantiate the entity when loading
    // from DB. It is protected so application code can't accidentally use it.
    protected constructor() : this(
        userId = "",
        user   = throw IllegalStateException("Hibernate no-arg constructor — do not use directly")
    )

    // ── Factory companion — always use this instead of the constructor directly ─
    companion object {
        /**
         * Create a new UserNotificationPreferences with all defaults.
         * The userId is intentionally left as "" here — @MapsId will overwrite
         * it with user.userId when the entity is persisted by JPA.
         */
        fun defaultsFor(user: User): UserNotificationPreferences =
            UserNotificationPreferences(
                userId = user.userId,   // pre-populate so @MapsId is consistent
                user   = user
            )
    }
}