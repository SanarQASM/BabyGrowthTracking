package com.example.backend_side.entity

import jakarta.persistence.*
import java.time.LocalDate

// ─────────────────────────────────────────────────────────────────────────────
// VaccinationSchedule entity
//
// Maps to: vaccination_schedules table
// One row per baby per vaccine dose.
//   ideal_date     = DOB + recommended_age_months (no adjustment)
//   scheduled_date = next valid bench vaccination day >= ideal_date,
//                    skipping holidays
//
// ─────────────────────────────────────────────────────────────────────────────
// BUG 3 FIX — ENUM case mismatch / missing @Convert:
//
//   The DB columns are defined with lowercase ENUM values:
//     ENUM('none','weekend','holiday',...)
//   The Kotlin enum constants are UPPERCASE:
//     ShiftReason.NONE, ShiftReason.WEEKEND, ...
//
//   Enums.kt already defines LowercaseEnumConverter subclasses
//   (ShiftReasonConverter, ScheduleStatusConverter) with @Converter(autoApply=true).
//   However, autoApply can silently fail for data class fields in Kotlin
//   when the annotation target is ambiguous — the converter ends up not
//   being applied, Hibernate falls back to ORDINAL mapping, and reading
//   a row back throws because ordinal 0 maps to "" not "none".
//
//   Fix: Add explicit @Convert(converter = ...) on both fields.
//   This guarantees the LowercaseEnumConverter is used regardless of
//   Hibernate version or Kotlin annotation target resolution quirks.
//   The DB column definitions (lowercase values) are CORRECT and unchanged.
// ─────────────────────────────────────────────────────────────────────────────

@Entity
@Table(
    name = "vaccination_schedules",
    indexes = [
        Index(name = "idx_vs_baby",          columnList = "baby_id"),
        Index(name = "idx_vs_bench",         columnList = "bench_id"),
        Index(name = "idx_vs_status",        columnList = "status"),
        Index(name = "idx_vs_scheduled_date",columnList = "scheduled_date"),
        Index(name = "idx_vs_baby_vaccine",  columnList = "baby_id,vaccine_id")
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

    // BUG 3 FIX: Added explicit @Convert to ensure ShiftReasonConverter
    // (LowercaseEnumConverter) is applied. Without this, Hibernate may ignore
    // autoApply and use ORDINAL mapping, causing deserialization failures
    // when reading rows back (ShiftReason.valueOf("none") throws because
    // the constant is named NONE not none).
    @Convert(converter = ShiftReasonConverter::class)
    @Column(
        name             = "shift_reason",
        columnDefinition = "ENUM('none','weekend','holiday','bench_closed','missed','rescheduled')"
    )
    var shiftReason: ShiftReason = ShiftReason.NONE,

    // How many days were added to reach a valid bench day
    @Column(name = "shift_days")
    var shiftDays: Int = 0,

    // BUG 3 FIX: Added explicit @Convert to ensure ScheduleStatusConverter
    // (LowercaseEnumConverter) is applied. Same root cause as shiftReason above —
    // without this annotation Hibernate falls back to ORDINAL, reads "upcoming"
    // as position 0 which maps to "" not a valid enum constant, and throws a
    // 500 on every getScheduleForBaby call.
    @Convert(converter = ScheduleStatusConverter::class)
    @Column(
        name             = "status",
        columnDefinition = "ENUM('upcoming','due_soon','overdue','completed','missed','rescheduled')"
    )
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
    var isVisibleToTeam: Boolean = true

    // createdAt and updatedAt are inherited from BaseEntity — do not redeclare here

) : BaseEntity()