package com.example.backend_side

import com.example.backend_side.entity.AdjustmentReason
import com.example.backend_side.entity.ScheduleStatus
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDate

// ============================================================
// VACCINATION RESCHEDULE DTOs
// ============================================================

/**
 * Request to reschedule all vaccinations for a baby.
 * [shiftReason] is set by the user/team explaining why they are rescheduling.
 * [rescheduleOverdue] if true, overdue vaccines that are still within the
 * safe window will be rescheduled; otherwise they are skipped.
 */
data class VaccinationRescheduleRequest @JsonCreator constructor(
    @JsonProperty("shiftReason")
    @field:NotNull(message = "Shift reason is required")
    val shiftReason: AdjustmentReason,

    @JsonProperty("notes")
    val notes: String? = null,

    /**
     * Whether to attempt rescheduling overdue vaccinations.
     * Default true — the UI always sends true because the user explicitly
     * chose "reschedule all".
     */
    @JsonProperty("rescheduleOverdue")
    val rescheduleOverdue: Boolean = true
)

/**
 * Per-vaccine outcome item returned in [VaccinationRescheduleResponse].
 */
data class VaccinationRescheduleItemResult(
    val scheduleId           : String,
    val vaccineName          : String,
    val vaccineNameAr        : String? = null,
    val vaccineNameKu        : String? = null,
    val vaccineNameCkb       : String? = null,
    val recommendedAgeMonths : Int,
    val doseNumber           : Int,
    val oldScheduledDate     : LocalDate,
    val newScheduledDate     : LocalDate?,   // null when skipped / too late
    val status               : ScheduleStatus,
    val rescheduled          : Boolean,
    /** Human-readable reason why this vaccine was NOT rescheduled. */
    val skipReason           : String? = null
)

/**
 * Full response returned by the reschedule endpoint.
 */
data class VaccinationRescheduleResponse(
    val babyId          : String,
    val babyName        : String,
    val totalVaccines   : Int,
    val rescheduledCount: Int,
    val skippedCount    : Int,
    val tooLateCount    : Int,
    val results         : List<VaccinationRescheduleItemResult>,
    val message         : String
)