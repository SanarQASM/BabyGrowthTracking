package com.example.backend_side

import com.example.backend_side.entity.*
import jakarta.validation.constraints.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

// ============================================================
// USER DTOs
// ============================================================

data class UserCreateRequest(
    @field:NotBlank(message = "Full name is required")
    val fullName: String,

    @field:Email(message = "Invalid email format")
    @field:NotBlank(message = "Email is required")
    val email: String,

    @field:Size(min = 8, message = "Password must be at least 8 characters")
    val password: String?,

    val phone: String? = null,
    val address: String? = null,
    val city: String? = null,
    val profileImageUrl: String? = null,

    @field:NotNull(message = "Role is required")
    val role: UserRole = UserRole.PARENT
)

data class UserUpdateRequest(
    val fullName: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val city: String? = null,
    val profileImageUrl: String? = null
)

data class UserResponse(
    val userId: String,
    val fullName: String,
    val email: String,
    val phone: String? = null,
    val address: String? = null,
    val city: String? = null,
    val profileImageUrl: String? = null,
    val role: UserRole,
    val isActive: Boolean = true,           // ✅ default prevents Android crash if missing
    val createdAt: LocalDateTime? = null,   // ✅ nullable — safe for Android deserializer
    val updatedAt: LocalDateTime? = null    // ✅ nullable — safe for Android deserializer
)

// ============================================================
// BABY DTOs
// ============================================================

data class BabyCreateRequest(
    @field:NotBlank(message = "Baby's full name is required")
    val fullName: String,

    // Android sends "YYYY-MM-DD" string — parsed to LocalDate in the service layer
    @field:NotBlank(message = "Date of birth is required")
    val dateOfBirth: String,

    // Android sends "BOY" or "GIRL" string — parsed to Gender enum in the service layer
    @field:NotBlank(message = "Gender is required")
    val gender: String,

    val birthWeight: Double? = null,
    val birthHeight: Double? = null,
    val birthHeadCircumference: Double? = null,

    val photoUrl: String? = null
)

data class BabyUpdateRequest(
    val fullName: String? = null,
    val photoUrl: String? = null,
    val isActive: Boolean? = null
)

data class BabyResponse(
    val babyId: String,
    val parentUserId: String,
    val parentName: String,
    val fullName: String,
    val dateOfBirth: String,               // ISO "YYYY-MM-DD" — matches Android String
    val gender: String,                    // "BOY"/"GIRL" — matches Android String
    val birthWeight: Double? = null,       // matches Android Double?
    val birthHeight: Double? = null,       // matches Android Double?
    val birthHeadCircumference: Double? = null, // matches Android Double?
    val photoUrl: String? = null,
    val ageInMonths: Int,
    val ageInDays: Long,
    val isActive: Boolean = true,
    val createdAt: String? = null,         // ISO datetime string — matches Android String?
    val updatedAt: String? = null          // ISO datetime string — matches Android String?
)

// ============================================================
// GROWTH RECORD DTOs
// ============================================================

data class GrowthRecordCreateRequest(
    @field:NotBlank(message = "Baby ID is required")
    val babyId: String,

    @field:NotNull(message = "Measurement date is required")
    val measurementDate: LocalDate,

    @field:DecimalMin(value = "0.5", message = "Weight must be at least 0.5 kg")
    val weight: BigDecimal? = null,

    @field:DecimalMin(value = "30.0", message = "Height must be at least 30 cm")
    val height: BigDecimal? = null,

    @field:DecimalMin(value = "20.0", message = "Head circumference must be at least 20 cm")
    val headCircumference: BigDecimal? = null,

    val weightPercentile: Byte? = null,
    val heightPercentile: Byte? = null,
    val headCircumferencePercentile: Byte? = null,
    val notes: String? = null
)

data class GrowthRecordResponse(
    val recordId: String,
    val babyId: String,
    val babyName: String,
    val measurementDate: LocalDate,
    val ageInMonths: Int,
    val ageInDays: Int? = null,
    val weight: BigDecimal? = null,
    val height: BigDecimal? = null,
    val headCircumference: BigDecimal? = null,
    val weightPercentile: Byte? = null,
    val heightPercentile: Byte? = null,
    val headCircumferencePercentile: Byte? = null,
    val measuredByName: String? = null,
    val notes: String? = null,
    val createdAt: LocalDateTime? = null
)

// ============================================================
// VACCINATION DTOs
// ============================================================

data class VaccinationCreateRequest(
    @field:NotBlank(message = "Baby ID is required")
    val babyId: String,

    @field:NotNull(message = "Vaccine type ID is required")
    val vaccineId: Int,

    @field:NotNull(message = "Scheduled date is required")
    val scheduledDate: LocalDate,

    val location: String? = null,
    val notes: String? = null
)

data class VaccinationUpdateRequest(
    val administeredDate: LocalDate? = null,
    val status: VaccinationStatus? = null,
    val certificateUrl: String? = null,
    val batchNumber: String? = null,
    val location: String? = null,
    val notes: String? = null
)

data class VaccinationResponse(
    val vaccinationId: String,
    val babyId: String,
    val babyName: String,
    val vaccineId: Int,
    val vaccineName: String,
    val recommendedAgeMonths: Int,
    val doseNumber: Byte,
    val scheduledDate: LocalDate,
    val administeredDate: LocalDate? = null,
    val status: VaccinationStatus,
    val administeredByName: String? = null,
    val certificateUrl: String? = null,
    val batchNumber: String? = null,
    val location: String? = null,
    val notes: String? = null,
    val createdAt: LocalDateTime? = null
)

// ============================================================
// MEMORY DTOs
// ============================================================

data class MemoryCreateRequest(
    @field:NotBlank(message = "Baby ID is required")
    val babyId: String,

    @field:NotBlank(message = "Title is required")
    val title: String,

    val description: String? = null,

    @field:NotNull(message = "Memory date is required")
    val memoryDate: LocalDate,

    val imageUrls: List<String> = emptyList(),
    val imageCaptions: List<String?> = emptyList()
)

data class MemoryResponse(
    val memoryId: String,
    val babyId: String,
    val babyName: String,
    val parentName: String,
    val title: String,
    val description: String? = null,
    val memoryDate: LocalDate,
    val ageInMonths: Int? = null,
    val ageInDays: Int? = null,
    val images: List<MemoryImageResponse> = emptyList(),
    val createdAt: LocalDateTime? = null
)

data class MemoryImageResponse(
    val imageId: Int,
    val imageUrl: String,
    val caption: String? = null,
    val sortOrder: Int
)

// ============================================================
// APPOINTMENT DTOs
// ============================================================

data class AppointmentCreateRequest(
    @field:NotBlank(message = "Baby ID is required")
    val babyId: String,

    @field:NotNull(message = "Appointment type is required")
    val appointmentType: AppointmentType,

    @field:NotNull(message = "Scheduled date is required")
    @field:Future(message = "Scheduled date must be in the future")
    val scheduledDate: LocalDate,

    val scheduledTime: String? = null,
    val durationMinutes: Int = 30,
    val doctorName: String? = null,
    val location: String? = null,
    val notes: String? = null
)

data class AppointmentUpdateRequest(
    val scheduledDate: LocalDate? = null,
    val scheduledTime: String? = null,
    val status: AppointmentStatus? = null,
    val doctorName: String? = null,
    val location: String? = null,
    val notes: String? = null
)

data class AppointmentResponse(
    val appointmentId: String,
    val babyId: String,
    val babyName: String,
    val appointmentType: AppointmentType,
    val scheduledDate: LocalDate,
    val scheduledTime: String? = null,
    val durationMinutes: Int,
    val status: AppointmentStatus,
    val doctorName: String? = null,
    val location: String? = null,
    val notes: String? = null,
    val reminderSent: Boolean = false,
    val createdAt: LocalDateTime? = null
)

// ============================================================
// FAMILY HISTORY DTOs
// ============================================================

data class FamilyHistoryCreateRequest(
    @field:NotBlank(message = "Baby ID is required")
    val babyId: String,
    val heredity: String? = null,
    val bloodDiseases: String? = null,
    val cardiovascularDiseases: String? = null,
    val metabolicDiseases: String? = null,
    val appendicitis: String? = null,
    val tuberculosis: String? = null,
    val parkinsonism: String? = null,
    val allergies: String? = null,
    val others: String? = null
)

data class FamilyHistoryResponse(
    val historyId: String,
    val babyId: String,
    val heredity: String? = null,
    val bloodDiseases: String? = null,
    val cardiovascularDiseases: String? = null,
    val metabolicDiseases: String? = null,
    val appendicitis: String? = null,
    val tuberculosis: String? = null,
    val parkinsonism: String? = null,
    val allergies: String? = null,
    val others: String? = null,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
)

// ============================================================
// HEALTH ISSUE DTOs
// ============================================================

data class HealthIssueCreateRequest(
    @field:NotBlank(message = "Baby ID is required")
    val babyId: String,

    @field:NotNull(message = "Issue date is required")
    val issueDate: LocalDate,

    @field:NotBlank(message = "Title is required")
    val title: String,

    val description: String? = null,
    val severity: Severity? = null
)

data class HealthIssueResponse(
    val issueId: String,
    val babyId: String,
    val issueDate: LocalDate,
    val title: String,
    val description: String? = null,
    val severity: Severity? = null,
    val isResolved: Boolean = false,
    val resolutionDate: LocalDate? = null,
    val resolvedNotes: String? = null,
    val createdByName: String? = null,
    val createdAt: LocalDateTime? = null
)

// ============================================================
// NOTIFICATION DTOs
// ============================================================

data class NotificationCreateRequest(
    @field:NotBlank(message = "User ID is required")
    val userId: String,
    val babyId: String? = null,
    val vaccinationId: String? = null,
    val appointmentId: String? = null,
    @field:NotNull(message = "Notification type is required")
    val notificationType: NotificationType,
    @field:NotBlank(message = "Title is required")
    val title: String,
    @field:NotBlank(message = "Message is required")
    val message: String,
    val scheduledSendTime: LocalDateTime? = null,
    val priority: NotificationPriority = NotificationPriority.MEDIUM
)

data class NotificationResponse(
    val notificationId: String,
    val userId: String,
    val babyId: String? = null,
    val notificationType: NotificationType? = null,
    val title: String,
    val message: String,
    val scheduledSendTime: LocalDateTime? = null,
    val sentAt: LocalDateTime? = null,
    val isSent: Boolean = false,
    val isRead: Boolean = false,
    val readAt: LocalDateTime? = null,
    val priority: NotificationPriority = NotificationPriority.MEDIUM,
    val createdAt: LocalDateTime? = null
)

// ============================================================
// COMMON DTOs
// ============================================================

data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null,
    val errors: List<String>? = null,
    val timestamp: LocalDateTime = LocalDateTime.now()
)

data class PageResponse<T>(
    val content: List<T>,
    val pageNumber: Int,
    val pageSize: Int,
    val totalElements: Long,
    val totalPages: Int,
    val isLast: Boolean
)

data class LoginResponse(
    val token: String,
    val refreshToken: String,
    val user: UserResponse,
    val expiresIn: Long
)

data class FirebaseAuthRequest(
    @field:NotBlank(message = "Firebase token is required")
    val firebaseToken: String,

    @field:NotNull(message = "Provider is required")
    val provider: AuthProvider
)