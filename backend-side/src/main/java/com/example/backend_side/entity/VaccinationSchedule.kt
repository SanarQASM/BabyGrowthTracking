package com.example.backend_side.entity

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

// Location: entity/VaccinationSchedule.kt
//
// Maps to: vaccination_schedules table
// One row per baby per vaccine dose.
// ideal_date   = DOB + recommended_age_months (no adjustment)
// scheduled_date = next valid bench vaccination day >= ideal_date, skipping holidays

@Entity
@Table(
    name = "vaccination_schedules",
    indexes = [
        Index(name = "idx_vs_baby", columnList = "baby_id"),
        Index(name = "idx_vs_bench", columnList = "bench_id"),
        Index(name = "idx_vs_status", columnList = "status"),
        Index(name = "idx_vs_scheduled_date", columnList = "scheduled_date"),
        Index(name = "idx_vs_baby_vaccine", columnList = "baby_id,vaccine_id")
    ]
)
data class VaccinationSchedule(

    @Id
    @Column(name = "schedule_id", columnDefinition = "VARCHAR(36)", updatable = false, nullable = false)
    var scheduleId: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "baby_id", nullable = false)
    var baby: Baby? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bench_id", nullable = false)
    var bench: VaccinationBench? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vaccine_id", nullable = false)
    var vaccineType: VaccineType? = null,

    // Raw ideal date: DOB + recommended_age_months — no bench/holiday adjustment
    @Column(name = "ideal_date", nullable = false)
    var idealDate: LocalDate = LocalDate.now(),

    // Adjusted date: next valid bench vaccination day >= ideal_date, skipping holidays
    @Column(name = "scheduled_date", nullable = false)
    var scheduledDate: LocalDate = LocalDate.now(),

    // Why the scheduled_date differs from ideal_date
    @Column(name = "shift_reason",
        columnDefinition = "ENUM('none','weekend','holiday','bench_closed','missed','rescheduled')")
    var shiftReason: ShiftReason = ShiftReason.NONE,

    // How many days were added to reach a valid bench day
    @Column(name = "shift_days")
    var shiftDays: Int = 0,

    @Column(name = "status",
        columnDefinition = "ENUM('upcoming','due_soon','overdue','completed','missed','rescheduled')")
    var status: ScheduleStatus = ScheduleStatus.UPCOMING,

    // Filled when the vaccination is actually done
    @Column(name = "completed_date")
    var completedDate: LocalDate? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "completed_by")
    var completedBy: User? = null,

    // Link to the Vaccination record created when the vaccine is administered
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vaccination_id")
    var vaccination: Vaccination? = null,

    @Column(name = "is_visible_to_parent")
    var isVisibleToParent: Boolean = true,

    @Column(name = "is_visible_to_team")
    var isVisibleToTeam: Boolean = true,

    @OneToMany(mappedBy = "schedule", cascade = [CascadeType.ALL], orphanRemoval = true)
    var adjustmentLogs: MutableList<ScheduleAdjustmentLog> = mutableListOf()

) : BaseEntity() {

    @PreUpdate
    fun preUpdate() { updatedAt = LocalDateTime.now() }
}