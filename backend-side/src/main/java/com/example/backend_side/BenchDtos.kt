package com.example.backend_side

import com.example.backend_side.entity.*
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDate
import java.time.LocalDateTime

// ============================================================
// BenchDtos.kt  — FIXED
//
// All request DTOs now have @JsonCreator + @JsonProperty.
// VaccinationBenchCreateRequest, VaccinationBenchUpdateRequest,
// BenchHolidayCreateRequest, and VaccinationScheduleUpdateRequest
// were previously missing these annotations and would throw
// InvalidDefinitionException on any POST/PUT request.
//
// BabyBenchAssignRequest and ScheduleAdjustmentRequest already
// had the fix — they are preserved unchanged.
// ============================================================

// ============================================================
// VACCINATION BENCH DTOs
// ============================================================

// Response DTO — no @JsonCreator needed (serialization only)
data class VaccinationBenchResponse(
    val benchId            : String,
    val nameEn             : String,
    val nameAr             : String,
    val governorate        : String,
    val district           : String,
    val addressEn          : String?        = null,
    val addressAr          : String?        = null,
    val latitude           : Double,
    val longitude          : Double,
    val phone              : String?        = null,
    val workingDays        : List<String>,
    val workingHoursStart  : String,
    val workingHoursEnd    : String,
    val vaccinationDays    : List<String>,
    val type               : BenchType,
    val vaccinesAvailable  : List<String>,
    val isActive           : Boolean,
    val createdAt          : LocalDateTime? = null
)

// ✅ FIXED — was missing @JsonCreator
data class VaccinationBenchCreateRequest @JsonCreator constructor(
    @JsonProperty("nameEn")
    @field:NotBlank(message = "English name is required")
    val nameEn: String,

    @JsonProperty("nameAr")
    @field:NotBlank(message = "Arabic name is required")
    val nameAr: String,

    @JsonProperty("governorate")
    @field:NotBlank(message = "Governorate is required")
    val governorate: String,

    @JsonProperty("district")          val district         : String      = "",
    @JsonProperty("addressEn")         val addressEn        : String?     = null,
    @JsonProperty("addressAr")         val addressAr        : String?     = null,

    @JsonProperty("latitude")
    @field:NotNull(message = "Latitude is required")
    val latitude: Double,

    @JsonProperty("longitude")
    @field:NotNull(message = "Longitude is required")
    val longitude: Double,

    @JsonProperty("phone")             val phone            : String?     = null,
    @JsonProperty("workingDays")       val workingDays      : List<String> = listOf("Sunday","Monday","Tuesday","Wednesday","Thursday"),
    @JsonProperty("workingHoursStart") val workingHoursStart: String      = "08:00",
    @JsonProperty("workingHoursEnd")   val workingHoursEnd  : String      = "14:00",
    @JsonProperty("vaccinationDays")   val vaccinationDays  : List<String> = listOf("Sunday","Tuesday","Thursday"),
    @JsonProperty("type")              val type             : BenchType   = BenchType.PRIMARY_HEALTH_CENTER,
    @JsonProperty("vaccinesAvailable") val vaccinesAvailable: List<String> = emptyList()
)

// ✅ FIXED — was missing @JsonCreator
data class VaccinationBenchUpdateRequest @JsonCreator constructor(
    @JsonProperty("nameEn")            val nameEn           : String?      = null,
    @JsonProperty("nameAr")            val nameAr           : String?      = null,
    @JsonProperty("phone")             val phone            : String?      = null,
    @JsonProperty("workingDays")       val workingDays      : List<String>? = null,
    @JsonProperty("workingHoursStart") val workingHoursStart: String?      = null,
    @JsonProperty("workingHoursEnd")   val workingHoursEnd  : String?      = null,
    @JsonProperty("vaccinationDays")   val vaccinationDays  : List<String>? = null,
    @JsonProperty("vaccinesAvailable") val vaccinesAvailable: List<String>? = null,
    @JsonProperty("isActive")          val isActive         : Boolean?     = null
)

// ============================================================
// BABY BENCH ASSIGNMENT DTOs
// ============================================================

// ✅ Already correct — preserved unchanged
data class BabyBenchAssignRequest @JsonCreator constructor(
    @JsonProperty("babyId")  @field:NotBlank(message = "Baby ID is required")  val babyId : String,
    @JsonProperty("benchId") @field:NotBlank(message = "Bench ID is required") val benchId: String,
    @JsonProperty("notes")   val notes: String? = null
)

// Response DTO — no @JsonCreator needed
data class BabyBenchAssignmentResponse(
    val assignmentId: String,
    val babyId      : String,
    val babyName    : String,
    val benchId     : String,
    val benchNameEn : String,
    val benchNameAr : String,
    val governorate : String,
    val assignedAt  : String,    // String not LocalDateTime — avoids Jackson array bug
    val isActive    : Boolean,
    val notes       : String?    = null
)

// ============================================================
// BENCH HOLIDAY DTOs
// ============================================================

// ✅ FIXED — was missing @JsonCreator
data class BenchHolidayCreateRequest @JsonCreator constructor(
    @JsonProperty("benchId")    val benchId    : String? = null,

    @JsonProperty("holidayDate")
    @field:NotNull(message = "Holiday date is required")
    val holidayDate: LocalDate,

    @JsonProperty("reason")
    @field:NotBlank(message = "Reason is required")
    val reason: String,

    @JsonProperty("isNational") val isNational: Boolean = false
)

// Response DTO — no @JsonCreator needed
data class BenchHolidayResponse(
    val holidayId  : String,
    val benchId    : String?        = null,
    val benchNameEn: String?        = null,
    val holidayDate: LocalDate,
    val reason     : String,
    val isNational : Boolean,
    val createdAt  : LocalDateTime? = null
)

// ============================================================
// VACCINATION SCHEDULE DTOs
// ============================================================

// Response DTO — no @JsonCreator needed
data class VaccinationScheduleResponse(
    val scheduleId          : String,
    val babyId              : String,
    val babyName            : String,
    val benchId             : String,
    val benchNameEn         : String,
    val benchNameAr         : String,
    val vaccineId           : Int,
    val vaccineName         : String,
    val vaccineNameAr       : String?    = null,
    val vaccineNameKu       : String?    = null,
    val vaccineNameCkb      : String?    = null,
    val description         : String?    = null,
    val descriptionAr       : String?    = null,
    val descriptionKu       : String?    = null,
    val descriptionCkb      : String?    = null,
    val doseNumber          : Int,
    val recommendedAgeMonths: Int,
    val idealDate           : LocalDate,
    val scheduledDate       : LocalDate,
    val shiftReason         : ShiftReason,
    val shiftDays           : Int,
    val status              : ScheduleStatus,
    val completedDate       : LocalDate? = null,
    val completedByName     : String?    = null,
    val isVisibleToParent   : Boolean,
    val isVisibleToTeam     : Boolean,
    val createdAt           : String?    = null,
    val updatedAt           : String?    = null
)

// ✅ FIXED — was missing @JsonCreator
data class VaccinationScheduleUpdateRequest @JsonCreator constructor(
    @JsonProperty("status")            val status           : ScheduleStatus? = null,
    @JsonProperty("completedDate")     val completedDate    : LocalDate?      = null,
    @JsonProperty("completedByUserId") val completedByUserId: String?         = null,
    @JsonProperty("vaccinationId")     val vaccinationId    : String?         = null,
    @JsonProperty("notes")             val notes            : String?         = null
)

// Response DTO — no @JsonCreator needed
data class BenchDayScheduleResponse(
    val benchId    : String,
    val benchNameEn: String,
    val date       : LocalDate,
    val totalBabies: Int,
    val items      : List<VaccinationScheduleResponse>
)

// ============================================================
// SCHEDULE ADJUSTMENT LOG DTOs
// ============================================================

// ✅ Already correct — preserved unchanged
data class ScheduleAdjustmentRequest @JsonCreator constructor(
    @JsonProperty("scheduleId") @field:NotBlank(message = "Schedule ID is required") val scheduleId: String,
    @JsonProperty("newDate")    @field:NotNull(message = "New date is required")     val newDate   : LocalDate,
    @JsonProperty("reason")     @field:NotNull(message = "Reason is required")       val reason    : AdjustmentReason,
    @JsonProperty("notes")      val notes: String? = null
)

// Response DTO — no @JsonCreator needed
data class ScheduleAdjustmentLogResponse(
    val logId          : String,
    val scheduleId     : String,
    val babyId         : String,
    val babyName       : String,
    val oldDate        : LocalDate,
    val newDate        : LocalDate,
    val reason         : AdjustmentReason,
    val notes          : String?       = null,
    val adjustedByName : String?       = null,
    val adjustedAt     : LocalDateTime
)