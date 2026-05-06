package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model

import kotlinx.serialization.Serializable

// ─────────────────────────────────────────────────────────────────────────────
// Bench / Health Center
// ─────────────────────────────────────────────────────────────────────────────

@Serializable
data class VaccinationBenchUi(
    val benchId: String,
    val nameEn: String,
    val nameAr: String,
    val nameKuSorani: String = "",
    val nameKuBadini: String = "",
    val governorate: String,
    val district: String,
    val addressEn: String? = null,
    val addressAr: String? = null,
    val directionEn: String? = null,
    val latitude: Double,
    val longitude: Double,
    val phone: String? = null,
    val workingDays: List<String> = emptyList(),
    val workingHoursStart: String = "08:00",
    val workingHoursEnd: String = "14:00",
    val vaccinationDays: List<String> = emptyList(),
    val type: String = "Primary Health Center",
    val vaccinesAvailable: List<String> = emptyList(),
    val isActive: Boolean = true,
    val teamMemberId: String? = null,      // ADD THIS
    val teamMemberName: String? = null,    // ADD THIS
    val distanceKm: Double? = null
)

// Health Issue
// ─────────────────────────────────────────────────────────────────────────────

enum class SeverityUi { MILD, MODERATE, SEVERE }
enum class HealthIssueFilter { ALL, ONGOING, RESOLVED }

@Serializable
data class HealthIssueUi(
    val issueId: String,
    val babyId: String,
    val title: String,
    val description: String? = null,
    val issueDate: String,
    val severity: String? = null,
    val isResolved: Boolean = false,
    val resolutionDate: String? = null,
    val resolvedNotes: String? = null
) {
    val severityUi: SeverityUi?
        get() = when (severity?.uppercase()) {
            "MILD"     -> SeverityUi.MILD
            "MODERATE" -> SeverityUi.MODERATE
            "SEVERE"   -> SeverityUi.SEVERE
            else       -> null
        }
}

// ─────────────────────────────────────────────────────────────────────────────
// Appointment
// ─────────────────────────────────────────────────────────────────────────────

enum class AppointmentStatusUi { SCHEDULED, COMPLETED, CANCELLED, MISSED }
enum class AppointmentFilter { ALL, UPCOMING, PAST, CANCELLED }

@Serializable
data class AppointmentUi(
    val appointmentId: String,
    val babyId: String,
    val babyName: String,
    val appointmentType: String,
    val scheduledDate: String,
    val scheduledTime: String? = null,
    val durationMinutes: Int = 30,
    val status: String,
    val doctorName: String? = null,
    val location: String? = null,
    val notes: String? = null
) {
    val statusUi: AppointmentStatusUi
        get() = when (status.uppercase()) {
            "COMPLETED"  -> AppointmentStatusUi.COMPLETED
            "CANCELLED"  -> AppointmentStatusUi.CANCELLED
            "MISSED"     -> AppointmentStatusUi.MISSED
            else         -> AppointmentStatusUi.SCHEDULED
        }
}

// ─────────────────────────────────────────────────────────────────────────────
// Map filter
// ─────────────────────────────────────────────────────────────────────────────

enum class BenchMapFilter { ALL, NEAR }

// ─────────────────────────────────────────────────────────────────────────────
// Health record sub-tab
// ─────────────────────────────────────────────────────────────────────────────

enum class HealthRecordSubTab { VACCINATIONS, HEALTH_ISSUES, APPOINTMENTS }
enum class VaccinationFilter { ALL, UPCOMING, COMPLETED, OVERDUE }