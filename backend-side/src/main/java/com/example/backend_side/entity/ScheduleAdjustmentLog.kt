package com.example.backend_side.entity

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

// Location: entity/ScheduleAdjustmentLog.kt
//
// Maps to: schedule_adjustment_logs table
// Every time a scheduled_date changes, a row is inserted here.
// adjusted_by = NULL means the system auto-adjusted (holiday/weekend detection).

@Entity
@Table(
    name = "schedule_adjustment_logs",
    indexes = [
        Index(name = "idx_sal_schedule", columnList = "schedule_id"),
        Index(name = "idx_sal_baby", columnList = "baby_id")
    ]
)
data class ScheduleAdjustmentLog(

    @Id
    @Column(name = "log_id", columnDefinition = "VARCHAR(36)", updatable = false, nullable = false)
    var logId: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    var schedule: VaccinationSchedule? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "baby_id", nullable = false)
    var baby: Baby? = null,

    @Column(name = "old_date", nullable = false)
    var oldDate: LocalDate = LocalDate.now(),

    @Column(name = "new_date", nullable = false)
    var newDate: LocalDate = LocalDate.now(),

    @Column(name = "reason",
        columnDefinition = "ENUM('holiday','bench_closed','parent_missed','parent_rescheduled','team_rescheduled','bench_changed')")
    var reason: AdjustmentReason = AdjustmentReason.HOLIDAY,

    @Column(name = "notes", columnDefinition = "TEXT")
    var notes: String? = null,

    // NULL = auto-adjusted by system (schedule generator)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "adjusted_by")
    var adjustedBy: User? = null,

    @Column(name = "adjusted_at", nullable = false)
    var adjustedAt: LocalDateTime = LocalDateTime.now()

) : BaseEntity()