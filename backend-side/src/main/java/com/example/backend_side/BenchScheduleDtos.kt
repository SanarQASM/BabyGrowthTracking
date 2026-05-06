package com.example.backend_side

import com.example.backend_side.entity.AdjustmentReason
import com.example.backend_side.entity.ScheduleStatus
import com.example.backend_side.entity.ShiftReason
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDate
import java.time.LocalDateTime

// ============================================================
// BABY BENCH ASSIGNMENT DTOs
// ============================================================

data class BabyBenchAssignRequest @JsonCreator constructor(
    @JsonProperty("babyId")
    @field:NotBlank(message = "Baby ID is required")
    val babyId: String,

    @JsonProperty("benchId")
    @field:NotBlank(message = "Bench ID is required")
    val benchId: String,

    @JsonProperty("notes")
    val notes: String? = null
)

data class BabyBenchAssignmentResponse(
    val assignmentId: String,
    val babyId      : String,
    val babyName    : String,
    val benchId     : String,
    val benchNameEn : String,
    val benchNameAr : String,
    val governorate : String,
    val assignedAt  : String,
    val isActive    : Boolean,
    val notes       : String? = null
)

// ============================================================
// BENCH HOLIDAY DTOs
// ============================================================

data class BenchHolidayCreateRequest @JsonCreator constructor(
    @JsonProperty("benchId")
    val benchId: String? = null,           // null = national holiday

    @JsonProperty("holidayDate")
    @field:NotNull(message = "Holiday date is required")
    val holidayDate: LocalDate,

    @JsonProperty("reason")
    @field:NotBlank(message = "Reason is required")
    val reason: String,

    @JsonProperty("isNational")
    val isNational: Boolean = false
)

data class BenchHolidayResponse(
    val holidayId  : String,
    val benchId    : String?        = null,
    val benchNameEn: String?        = null,
    val holidayDate: LocalDate,
    val reason     : String,
    val isNational : Boolean        = false,
    val createdAt  : LocalDateTime? = null
)

// ============================================================
// VACCINATION SCHEDULE DTOs
// ============================================================

data class VaccinationScheduleResponse(
    val scheduleId          : String,
    val babyId              : String,
    val babyName            : String,
    val benchId             : String,
    val benchNameEn         : String,
    val benchNameAr         : String,
    val vaccineId           : Int,
    val vaccineName         : String,
    val vaccineNameAr       : String?       = null,
    val vaccineNameKu       : String?       = null,
    val vaccineNameCkb      : String?       = null,
    val description         : String?       = null,
    val descriptionAr       : String?       = null,
    val descriptionKu       : String?       = null,
    val descriptionCkb      : String?       = null,
    val doseNumber          : Int,
    val recommendedAgeMonths: Int,
    val idealDate           : LocalDate,
    val scheduledDate       : LocalDate,
    val shiftReason         : ShiftReason,
    val shiftDays           : Int           = 0,
    val status              : ScheduleStatus,
    val completedDate       : LocalDate?    = null,
    val completedByName     : String?       = null,
    val isVisibleToParent   : Boolean       = true,
    val isVisibleToTeam     : Boolean       = true,
    val createdAt           : String?       = null,
    val updatedAt           : String?       = null
)

data class VaccinationScheduleUpdateRequest @JsonCreator constructor(
    @JsonProperty("status")
    val status: ScheduleStatus? = null,

    @JsonProperty("completedDate")
    val completedDate: LocalDate? = null,

    @JsonProperty("completedByUserId")
    val completedByUserId: String? = null,

    @JsonProperty("vaccinationId")
    val vaccinationId: String? = null
)

data class ScheduleAdjustmentRequest @JsonCreator constructor(
    @JsonProperty("scheduleId")
    val scheduleId: String = "",

    @JsonProperty("newDate")
    @field:NotNull(message = "New date is required")
    val newDate: LocalDate,

    @JsonProperty("reason")
    @field:NotNull(message = "Reason is required")
    val reason: AdjustmentReason,

    @JsonProperty("notes")
    val notes: String? = null
)

data class BenchDayScheduleResponse(
    val benchId    : String,
    val benchNameEn: String,
    val date       : LocalDate,
    val totalCount : Int,
    val schedules  : List<VaccinationScheduleResponse>
)

data class ScheduleAdjustmentLogResponse(
    val logId         : String,
    val scheduleId    : String,
    val babyId        : String,
    val babyName      : String,
    val oldDate       : LocalDate,
    val newDate       : LocalDate,
    val reason        : AdjustmentReason,
    val notes         : String?       = null,
    val adjustedByName: String?       = null,
    val adjustedAt    : LocalDateTime
)