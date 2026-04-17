package com.example.backend_side

import com.example.backend_side.entity.*
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

// ============================================================
// Dtos.kt  — FIXED
//
// Every request DTO (used as @RequestBody) now has:
//   @JsonCreator constructor  — tells Jackson which constructor to use
//   @JsonProperty("fieldName") — maps the JSON key to the parameter
//
// This is a belt-and-suspenders fix that works correctly in every
// scenario (KotlinModule present or absent, unit tests, etc.).
// Response DTOs do NOT need @JsonCreator — Jackson serializes them
// fine because it only needs getters/fields, not a constructor.
// ============================================================

// ============================================================
// USER DTOs
// ============================================================

data class UserCreateRequest @JsonCreator constructor(
    @JsonProperty("fullName")
    @field:NotBlank(message = "Full name is required")
    val fullName: String,

    @JsonProperty("email")
    @field:Email(message = "Invalid email format")
    @field:NotBlank(message = "Email is required")
    val email: String,

    @JsonProperty("password")
    @field:Size(min = 8, message = "Password must be at least 8 characters")
    val password: String?,

    @JsonProperty("phone")           val phone          : String?   = null,
    @JsonProperty("address")         val address        : String?   = null,
    @JsonProperty("city")            val city           : String?   = null,
    @JsonProperty("profileImageUrl") val profileImageUrl: String?   = null,

    @JsonProperty("role")
    @field:NotNull(message = "Role is required")
    val role: UserRole = UserRole.PARENT
)

data class UserUpdateRequest @JsonCreator constructor(
    @JsonProperty("fullName")        val fullName       : String? = null,
    @JsonProperty("phone")           val phone          : String? = null,
    @JsonProperty("address")         val address        : String? = null,
    @JsonProperty("city")            val city           : String? = null,
    @JsonProperty("profileImageUrl") val profileImageUrl: String? = null
)

// Response DTOs — no @JsonCreator needed (serialization only)
data class UserResponse(
    val userId         : String,
    val fullName       : String,
    val email          : String,
    val phone          : String?        = null,
    val address        : String?        = null,
    val city           : String?        = null,
    val profileImageUrl: String?        = null,
    val role           : UserRole,
    val isActive       : Boolean        = true,
    val createdAt      : LocalDateTime? = null,
    val updatedAt      : LocalDateTime? = null
)

// ============================================================
// BABY DTOs
// ============================================================

data class BabyCreateRequest @JsonCreator constructor(
    @JsonProperty("fullName")
    @field:NotBlank(message = "Baby's full name is required")
    val fullName: String,

    @JsonProperty("dateOfBirth")
    @field:NotBlank(message = "Date of birth is required")
    val dateOfBirth: String,           // "YYYY-MM-DD" — parsed in service layer

    @JsonProperty("gender")
    @field:NotBlank(message = "Gender is required")
    val gender: String,                // "BOY" / "GIRL" — parsed in service layer

    @JsonProperty("birthWeight")           val birthWeight           : Double? = null,
    @JsonProperty("birthHeight")           val birthHeight           : Double? = null,
    @JsonProperty("birthHeadCircumference") val birthHeadCircumference: Double? = null,
    @JsonProperty("photoUrl")             val photoUrl              : String? = null
)

data class BabyUpdateRequest @JsonCreator constructor(
    @JsonProperty("fullName") val fullName: String?  = null,
    @JsonProperty("photoUrl") val photoUrl: String?  = null,
    @JsonProperty("isActive") val isActive: Boolean? = null
)

// Response DTO — no @JsonCreator needed
data class BabyResponse(
    val babyId                : String,
    val parentUserId          : String,
    val parentName            : String,
    val fullName              : String,
    val dateOfBirth           : String,
    val gender                : String,
    val birthWeight           : Double?  = null,
    val birthHeight           : Double?  = null,
    val birthHeadCircumference: Double?  = null,
    val photoUrl              : String?  = null,
    val ageInMonths           : Int,
    val ageInDays             : Long,
    val isActive              : Boolean  = true,
    val createdAt             : String?  = null,
    val updatedAt             : String?  = null
)

// ============================================================
// GROWTH RECORD DTOs
// ============================================================

data class GrowthRecordCreateRequest @com.fasterxml.jackson.annotation.JsonCreator constructor(
    @com.fasterxml.jackson.annotation.JsonProperty("babyId")
    @field:jakarta.validation.constraints.NotBlank(message = "Baby ID is required")
    val babyId: String,

    @com.fasterxml.jackson.annotation.JsonProperty("measurementDate")
    @field:jakarta.validation.constraints.NotNull(message = "Measurement date is required")
    val measurementDate: java.time.LocalDate,

    @com.fasterxml.jackson.annotation.JsonProperty("weight")
    @field:jakarta.validation.constraints.DecimalMin(value = "0.5", message = "Weight must be at least 0.5 kg")
    val weight: java.math.BigDecimal? = null,

    @com.fasterxml.jackson.annotation.JsonProperty("height")
    @field:jakarta.validation.constraints.DecimalMin(value = "30.0", message = "Height must be at least 30 cm")
    val height: java.math.BigDecimal? = null,

    @com.fasterxml.jackson.annotation.JsonProperty("headCircumference")
    @field:jakarta.validation.constraints.DecimalMin(value = "20.0", message = "Head circumference must be at least 20 cm")
    val headCircumference: java.math.BigDecimal? = null,

    @com.fasterxml.jackson.annotation.JsonProperty("weightPercentile")            val weightPercentile           : Byte?   = null,
    @com.fasterxml.jackson.annotation.JsonProperty("heightPercentile")            val heightPercentile           : Byte?   = null,
    @com.fasterxml.jackson.annotation.JsonProperty("headCircumferencePercentile") val headCircumferencePercentile: Byte?   = null,
    @com.fasterxml.jackson.annotation.JsonProperty("notes")                       val notes                      : String? = null
)

// Response DTO — no @JsonCreator needed
data class GrowthRecordResponse(
    val recordId                   : String,
    val babyId                     : String,
    val babyName                   : String,
    val measurementDate            : java.time.LocalDate,
    val ageInMonths                : Int,
    val ageInDays                  : Int?                = null,
    val weight                     : java.math.BigDecimal? = null,
    val height                     : java.math.BigDecimal? = null,
    val headCircumference          : java.math.BigDecimal? = null,
    val weightPercentile           : Byte?               = null,
    val heightPercentile           : Byte?               = null,
    val headCircumferencePercentile: Byte?               = null,

    val measuredByName             : String?             = null,

    val isTeamMeasurement          : Boolean             = false,

    val notes                      : String?             = null,
    val createdAt                  : String?             = null,
    val updatedAt                  : String?             = null
)

// ============================================================
// VACCINATION DTOs
// ============================================================

data class VaccinationCreateRequest @JsonCreator constructor(
    @JsonProperty("babyId")
    @field:NotBlank(message = "Baby ID is required")
    val babyId: String,

    @JsonProperty("vaccineId")
    @field:NotNull(message = "Vaccine type ID is required")
    val vaccineId: Int,

    @JsonProperty("scheduledDate")
    @field:NotNull(message = "Scheduled date is required")
    val scheduledDate: LocalDate,

    @JsonProperty("location") val location: String? = null,
    @JsonProperty("notes")    val notes   : String? = null
)

data class VaccinationUpdateRequest @JsonCreator constructor(
    @JsonProperty("administeredDate") val administeredDate: LocalDate?         = null,
    @JsonProperty("status")           val status          : VaccinationStatus? = null,
    @JsonProperty("certificateUrl")   val certificateUrl  : String?            = null,
    @JsonProperty("batchNumber")      val batchNumber     : String?            = null,
    @JsonProperty("location")         val location        : String?            = null,
    @JsonProperty("notes")            val notes           : String?            = null
)

// Response DTO — no @JsonCreator needed
data class VaccinationResponse(
    val vaccinationId    : String,
    val babyId           : String,
    val babyName         : String,
    val vaccineId        : Int,
    val vaccineName      : String,
    val recommendedAgeMonths: Int,
    val doseNumber       : Byte,
    val scheduledDate    : LocalDate,
    val administeredDate : LocalDate?      = null,
    val status           : VaccinationStatus,
    val administeredByName: String?        = null,
    val certificateUrl   : String?         = null,
    val batchNumber      : String?         = null,
    val location         : String?         = null,
    val notes            : String?         = null,
    val createdAt        : LocalDateTime?  = null
)

// ============================================================
// MEMORY DTOs
// ============================================================

data class MemoryCreateRequest @JsonCreator constructor(

    @JsonProperty("babyId")
    @field:NotBlank(message = "Baby ID is required")
    val babyId: String,

    @JsonProperty("title")
    @field:NotBlank(message = "Title is required")
    val title: String,

    @JsonProperty("description")
    val description: String? = null,

    @JsonProperty("memoryDate")
    @field:NotNull(message = "Memory date is required")
    val memoryDate: LocalDate,

    // ── Backward-compatible image URL list (kept from original) ───────────────
    @JsonProperty("imageUrls")
    val imageUrls: List<String> = emptyList(),

    @JsonProperty("imageCaptions")
    val imageCaptions: List<String?> = emptyList(),

    // ── NEW: local-storage image metadata sent by the Kotlin client ───────────
    @JsonProperty("imageCount")
    val imageCount: Int = 0,

    @JsonProperty("captions")
    val captions: List<String?>? = null
)
// Response DTOs — no @JsonCreator needed
data class MemoryResponse(
    val memoryId   : String,
    val babyId     : String,
    val babyName   : String,
    val parentName : String,
    val title      : String,
    val description: String?            = null,
    val memoryDate : LocalDate,
    val ageInMonths: Int?               = null,
    val ageInDays  : Int?               = null,
    val images     : List<MemoryImageResponse> = emptyList(),
    val createdAt  : LocalDateTime?     = null
)

data class MemoryImageResponse(
    val imageId  : Int,
    val imageUrl : String,
    val caption  : String? = null,
    val sortOrder: Int
)

// ============================================================
// APPOINTMENT DTOs
// ============================================================

data class AppointmentCreateRequest @JsonCreator constructor(
    @JsonProperty("babyId")
    @field:NotBlank(message = "Baby ID is required")
    val babyId: String,

    @JsonProperty("appointmentType")
    @field:NotNull(message = "Appointment type is required")
    val appointmentType: AppointmentType,

    @JsonProperty("scheduledDate")
    @field:NotNull(message = "Scheduled date is required")
    @field:Future(message = "Scheduled date must be in the future")
    val scheduledDate: LocalDate,

    @JsonProperty("scheduledTime")   val scheduledTime  : String? = null,
    @JsonProperty("durationMinutes") val durationMinutes: Int     = 30,
    @JsonProperty("doctorName")      val doctorName     : String? = null,
    @JsonProperty("location")        val location       : String? = null,
    @JsonProperty("notes")           val notes          : String? = null
)

data class AppointmentUpdateRequest @JsonCreator constructor(
    @JsonProperty("scheduledDate")   val scheduledDate  : LocalDate?        = null,
    @JsonProperty("scheduledTime")   val scheduledTime  : String?           = null,
    @JsonProperty("status")          val status         : AppointmentStatus? = null,
    @JsonProperty("doctorName")      val doctorName     : String?           = null,
    @JsonProperty("location")        val location       : String?           = null,
    @JsonProperty("notes")           val notes          : String?           = null
)

// Response DTO — no @JsonCreator needed
data class AppointmentResponse(
    val appointmentId  : String,
    val babyId         : String,
    val babyName       : String,
    val appointmentType: AppointmentType,
    val scheduledDate  : LocalDate,
    val scheduledTime  : String?           = null,
    val durationMinutes: Int,
    val status         : AppointmentStatus,
    val doctorName     : String?           = null,
    val location       : String?           = null,
    val notes          : String?           = null,
    val reminderSent   : Boolean           = false,
    val createdAt      : LocalDateTime?    = null
)

// ============================================================
// FAMILY HISTORY DTOs
// ============================================================

data class FamilyHistoryCreateRequest @JsonCreator constructor(
    @JsonProperty("babyId")
    @field:NotBlank(message = "Baby ID is required")
    val babyId: String,

    @JsonProperty("heredity")                 val heredity                : String? = null,
    @JsonProperty("bloodDiseases")            val bloodDiseases           : String? = null,
    @JsonProperty("cardiovascularDiseases")   val cardiovascularDiseases  : String? = null,
    @JsonProperty("metabolicDiseases")        val metabolicDiseases       : String? = null,
    @JsonProperty("appendicitis")             val appendicitis            : String? = null,
    @JsonProperty("tuberculosis")             val tuberculosis            : String? = null,
    @JsonProperty("parkinsonism")             val parkinsonism            : String? = null,
    @JsonProperty("allergies")                val allergies               : String? = null,
    @JsonProperty("others")                   val others                  : String? = null
)

// Response DTO — no @JsonCreator needed
data class FamilyHistoryResponse(
    val historyId              : String,
    val babyId                 : String,
    val heredity               : String?        = null,
    val bloodDiseases          : String?        = null,
    val cardiovascularDiseases : String?        = null,
    val metabolicDiseases      : String?        = null,
    val appendicitis           : String?        = null,
    val tuberculosis           : String?        = null,
    val parkinsonism           : String?        = null,
    val allergies              : String?        = null,
    val others                 : String?        = null,
    val createdAt              : LocalDateTime? = null,
    val updatedAt              : LocalDateTime? = null
)

data class ChildIllnessRequest(
    val babyId: String,
    val illnessName: String,
    val diagnosisDate: String? = null,
    val notes: String? = null,
    val isActive: Boolean = true
) {
    fun parsedDiagnosisDate(): LocalDate? {
        if (diagnosisDate.isNullOrBlank()) return null
        return try {
            LocalDate.parse(diagnosisDate.trim(), DateTimeFormatter.ISO_LOCAL_DATE)
        } catch (e: DateTimeParseException) {
            throw IllegalArgumentException(
                "Invalid diagnosisDate format '${diagnosisDate}'. Expected yyyy-MM-dd."
            )
        }
    }
}

// ← ADD THIS
data class ChildIllnessResponse(
    val illnessId     : String,
    val babyId        : String,
    val illnessName   : String,
    val diagnosisDate : LocalDate?     = null,
    val notes         : String?        = null,
    val isActive      : Boolean        = true,
    val createdAt     : LocalDateTime? = null,
    val updatedAt     : LocalDateTime? = null
)


// ============================================================
// HEALTH ISSUE DTOs
// ============================================================

data class HealthIssueCreateRequest @JsonCreator constructor(
    @JsonProperty("babyId")
    @field:NotBlank(message = "Baby ID is required")
    val babyId: String,

    @JsonProperty("issueDate")
    @field:NotNull(message = "Issue date is required")
    val issueDate: LocalDate,

    @JsonProperty("title")
    @field:NotBlank(message = "Title is required")
    val title: String,

    @JsonProperty("description") val description: String?   = null,
    @JsonProperty("severity")    val severity   : Severity? = null
)

// Response DTO — no @JsonCreator needed
data class HealthIssueResponse(
    val issueId       : String,
    val babyId        : String,
    val issueDate     : LocalDate,
    val title         : String,
    val description   : String?        = null,
    val severity      : Severity?      = null,
    val isResolved    : Boolean        = false,
    val resolutionDate: LocalDate?     = null,
    val resolvedNotes : String?        = null,
    val createdByName : String?        = null,
    val createdAt     : LocalDateTime? = null
)

// ============================================================
// NOTIFICATION DTOs
// ============================================================

data class NotificationCreateRequest @JsonCreator constructor(
    @JsonProperty("userId")
    @field:NotBlank(message = "User ID is required")
    val userId: String,

    @JsonProperty("babyId")        val babyId        : String?              = null,
    @JsonProperty("vaccinationId") val vaccinationId : String?              = null,
    @JsonProperty("appointmentId") val appointmentId : String?              = null,

    @JsonProperty("notificationType")
    @field:NotNull(message = "Notification type is required")
    val notificationType: NotificationType,

    @JsonProperty("title")
    @field:NotBlank(message = "Title is required")
    val title: String,

    @JsonProperty("message")
    @field:NotBlank(message = "Message is required")
    val message: String,

    @JsonProperty("scheduledSendTime") val scheduledSendTime: LocalDateTime?       = null,
    @JsonProperty("priority")          val priority         : NotificationPriority = NotificationPriority.MEDIUM
)

// Response DTO — no @JsonCreator needed
data class NotificationResponse(
    val notificationId  : String,
    val userId          : String,
    val babyId          : String?              = null,
    val notificationType: NotificationType?    = null,
    val title           : String,
    val message         : String,
    val scheduledSendTime: LocalDateTime?      = null,
    val sentAt          : LocalDateTime?       = null,
    val isSent          : Boolean              = false,
    val isRead          : Boolean              = false,
    val readAt          : LocalDateTime?       = null,
    val priority        : NotificationPriority = NotificationPriority.MEDIUM,
    val createdAt       : LocalDateTime?       = null
)

// ============================================================
// COMMON DTOs
// ============================================================

// ApiResponse is only ever serialized (sent to client), never deserialized —
// no @JsonCreator needed.
data class ApiResponse<T>(
    val success  : Boolean,
    val message  : String,
    val data     : T?           = null,
    val errors   : List<String>? = null,
    val timestamp: LocalDateTime = LocalDateTime.now()
)

data class PageResponse<T>(
    val content      : List<T>,
    val pageNumber   : Int,
    val pageSize     : Int,
    val totalElements: Long,
    val totalPages   : Int,
    val isLast       : Boolean
)

data class LoginResponse(
    val token       : String,
    val refreshToken: String,
    val user        : UserResponse,
    val expiresIn   : Long
)

data class FirebaseAuthRequest @JsonCreator constructor(
    @JsonProperty("firebaseToken")
    @field:NotBlank(message = "Firebase token is required")
    val firebaseToken: String,

    @JsonProperty("provider")
    @field:NotNull(message = "Provider is required")
    val provider: AuthProvider
)