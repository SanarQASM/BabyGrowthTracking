package com.example.backend_side

// ============================================================
// APPEND THESE TO THE BOTTOM OF YOUR EXISTING Dtos.kt
// ============================================================

import com.example.backend_side.entity.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDate
import java.time.LocalDateTime

// ============================================================
// VACCINATION BENCH DTOs
// ============================================================

data class VaccinationBenchResponse(
    val benchId: String,
    val nameEn: String,
    val nameAr: String,
    val governorate: String,
    val district: String,
    val addressEn: String? = null,
    val addressAr: String? = null,
    val latitude: Double,
    val longitude: Double,
    val phone: String? = null,
    val workingDays: List<String>,         // parsed list e.g. ["Sunday","Monday",...]
    val workingHoursStart: String,
    val workingHoursEnd: String,
    val vaccinationDays: List<String>,     // parsed list e.g. ["Sunday","Tuesday","Thursday"]
    val type: BenchType,
    val vaccinesAvailable: List<String>,   // parsed list
    val isActive: Boolean,
    val createdAt: LocalDateTime? = null
)

data class VaccinationBenchCreateRequest(
    @field:NotBlank(message = "English name is required")
    val nameEn: String,

    @field:NotBlank(message = "Arabic name is required")
    val nameAr: String,

    @field:NotBlank(message = "Governorate is required")
    val governorate: String,

    val district: String = "",
    val addressEn: String? = null,
    val addressAr: String? = null,

    @field:NotNull(message = "Latitude is required")
    val latitude: Double,

    @field:NotNull(message = "Longitude is required")
    val longitude: Double,

    val phone: String? = null,

    // Sent as lists from client, joined to comma-string in service
    val workingDays: List<String> = listOf("Sunday","Monday","Tuesday","Wednesday","Thursday"),
    val workingHoursStart: String = "08:00",
    val workingHoursEnd: String = "14:00",
    val vaccinationDays: List<String> = listOf("Sunday","Tuesday","Thursday"),
    val type: BenchType = BenchType.PRIMARY_HEALTH_CENTER,
    val vaccinesAvailable: List<String> = emptyList()
)

data class VaccinationBenchUpdateRequest(
    val nameEn: String? = null,
    val nameAr: String? = null,
    val phone: String? = null,
    val workingDays: List<String>? = null,
    val workingHoursStart: String? = null,
    val workingHoursEnd: String? = null,
    val vaccinationDays: List<String>? = null,
    val vaccinesAvailable: List<String>? = null,
    val isActive: Boolean? = null
)

// ============================================================
// BABY BENCH ASSIGNMENT DTOs
// ============================================================

data class BabyBenchAssignRequest(
    @field:NotBlank(message = "Baby ID is required")
    val babyId: String,

    @field:NotBlank(message = "Bench ID is required")
    val benchId: String,

    val notes: String? = null
)

data class BabyBenchAssignmentResponse(
    val assignmentId: String,
    val babyId: String,
    val babyName: String,
    val benchId: String,
    val benchNameEn: String,
    val benchNameAr: String,
    val governorate: String,
    val assignedAt: LocalDateTime,
    val isActive: Boolean,
    val notes: String? = null
)

// ============================================================
// BENCH HOLIDAY DTOs
// ============================================================

data class BenchHolidayCreateRequest(
    val benchId: String? = null,   // NULL = national holiday

    @field:NotNull(message = "Holiday date is required")
    val holidayDate: LocalDate,

    @field:NotBlank(message = "Reason is required")
    val reason: String,

    val isNational: Boolean = false
)

data class BenchHolidayResponse(
    val holidayId: String,
    val benchId: String? = null,
    val benchNameEn: String? = null,
    val holidayDate: LocalDate,
    val reason: String,
    val isNational: Boolean,
    val createdAt: LocalDateTime? = null
)

// ============================================================
// VACCINATION SCHEDULE DTOs
// ============================================================

data class VaccinationScheduleResponse(
    val scheduleId: String,
    val babyId: String,
    val babyName: String,
    val benchId: String,
    val benchNameEn: String,
    val benchNameAr: String,
    val vaccineId: Int,
    val vaccineName: String,
    val doseNumber: Byte,
    val recommendedAgeMonths: Int,
    val idealDate: LocalDate,           // unadjusted date
    val scheduledDate: LocalDate,       // bench-adjusted date
    val shiftReason: ShiftReason,
    val shiftDays: Int,
    val status: ScheduleStatus,
    val completedDate: LocalDate? = null,
    val completedByName: String? = null,
    val isVisibleToParent: Boolean,
    val isVisibleToTeam: Boolean,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
)

data class VaccinationScheduleUpdateRequest(
    val status: ScheduleStatus? = null,
    val completedDate: LocalDate? = null,
    val completedByUserId: String? = null,
    val vaccinationId: String? = null,  // link to Vaccination record when done
    val notes: String? = null
)

// Team view: all babies coming to a bench on a specific date
data class BenchDayScheduleResponse(
    val benchId: String,
    val benchNameEn: String,
    val date: LocalDate,
    val totalBabies: Int,
    val items: List<VaccinationScheduleResponse>
)

// ============================================================
// SCHEDULE ADJUSTMENT LOG DTOs
// ============================================================

data class ScheduleAdjustmentRequest(
    @field:NotBlank(message = "Schedule ID is required")
    val scheduleId: String,

    @field:NotNull(message = "New date is required")
    val newDate: LocalDate,

    @field:NotNull(message = "Reason is required")
    val reason: AdjustmentReason,

    val notes: String? = null
)

data class ScheduleAdjustmentLogResponse(
    val logId: String,
    val scheduleId: String,
    val babyId: String,
    val babyName: String,
    val oldDate: LocalDate,
    val newDate: LocalDate,
    val reason: AdjustmentReason,
    val notes: String? = null,
    val adjustedByName: String? = null,   // NULL = system auto-adjusted
    val adjustedAt: LocalDateTime
)