package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model

import androidx.compose.runtime.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.*
import org.example.project.babygrowthtrackingapplication.data.network.ApiResult
import org.example.project.babygrowthtrackingapplication.data.network.ApiService
import org.example.project.babygrowthtrackingapplication.data.network.BabyResponse

// ─────────────────────────────────────────────────────────────────────────────
// UI State
// ─────────────────────────────────────────────────────────────────────────────

data class HealthRecordUiState(
    // Child selection
    val babies: List<BabyResponse> = emptyList(),
    val selectedBabyId: String? = null,

    // Branch assignment (per child)
    val assignment: BabyBenchAssignmentUi? = null,
    val assignmentLoading: Boolean = false,

    // Map screen
    val allBenches: List<VaccinationBenchUi> = emptyList(),
    val benchesLoading: Boolean = false,
    val mapFilter: BenchMapFilter = BenchMapFilter.ALL,
    val selectedBench: VaccinationBenchUi? = null,

    // Parent location (city-level from profile — no lat/lng)
    val parentGovernorate: String = "",
    val mapCenterLat: Double = 36.19, // Nineveh default
    val mapCenterLng: Double = 43.99,

    // Vaccination schedule
    val schedules: List<VaccinationScheduleUi> = emptyList(),
    val schedulesLoading: Boolean = false,
    val vaccinationFilter: VaccinationFilter = VaccinationFilter.ALL,

    // Health issues
    val healthIssues: List<HealthIssueUi> = emptyList(),
    val healthIssuesLoading: Boolean = false,
    val healthIssueFilter: HealthIssueFilter = HealthIssueFilter.ALL,

    // Appointments
    val appointments: List<AppointmentUi> = emptyList(),
    val appointmentsLoading: Boolean = false,
    val appointmentFilter: AppointmentFilter = AppointmentFilter.ALL,

    // Sub-tab
    val subTab: HealthRecordSubTab = HealthRecordSubTab.VACCINATIONS,

    // Errors / messages
    val error: String? = null,
    val successMessage: String? = null,

    // Reschedule confirmation
    val showRescheduleConfirm: Boolean = false,

    // Add dialogs
    val showAddHealthIssue: Boolean = false,
    val showAddAppointment: Boolean = false,

    // View-all detail navigation
    val selectedScheduleItem: VaccinationScheduleUi? = null,
    val selectedHealthIssue: HealthIssueUi? = null,
    val selectedAppointment: AppointmentUi? = null
)

// ─────────────────────────────────────────────────────────────────────────────
// ViewModel
// ─────────────────────────────────────────────────────────────────────────────

class HealthRecordViewModel(
    private val apiService: ApiService,
    private val preferencesManager: org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.data.PreferencesManager
) {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    var uiState by mutableStateOf(HealthRecordUiState())
        private set

    // ── Initialise with babies list ──────────────────────────────────────────

    fun init(babies: List<BabyResponse>) {
        uiState = uiState.copy(babies = babies)
        if (babies.isNotEmpty() && uiState.selectedBabyId == null) {
            selectBaby(babies.first().babyId)
        }
        loadBenches()
        resolveMapCenter()
    }

    fun selectBaby(babyId: String) {
        uiState = uiState.copy(selectedBabyId = babyId, assignment = null)
        loadAssignment(babyId)
        loadSchedules(babyId)
        loadHealthIssues(babyId)
        loadAppointments(babyId)
    }

    // ── Bench / Map ──────────────────────────────────────────────────────────

    fun loadBenches() {
        scope.launch {
            uiState = uiState.copy(benchesLoading = true)
            try {
                val result = apiService.getAllBenches()
                uiState = when (result) {
                    is ApiResult.Success -> uiState.copy(
                        allBenches = result.data,
                        benchesLoading = false
                    )
                    is ApiResult.Error   -> uiState.copy(
                        error = result.message, benchesLoading = false
                    )
                    else -> uiState.copy(benchesLoading = false)
                }
            } catch (e: Exception) {
                uiState = uiState.copy(benchesLoading = false, error = e.message)
            }
        }
    }

    fun setMapFilter(filter: BenchMapFilter) {
        uiState = uiState.copy(mapFilter = filter)
    }

    fun selectBenchOnMap(bench: VaccinationBenchUi) {
        uiState = uiState.copy(selectedBench = bench)
    }

    fun clearSelectedBench() {
        uiState = uiState.copy(selectedBench = null)
    }

    fun assignBench(babyId: String, benchId: String) {
        scope.launch {
            uiState = uiState.copy(assignmentLoading = true)
            try {
                val result = apiService.assignBench(babyId, benchId)
                uiState = when (result) {
                    is ApiResult.Success -> {
                        loadSchedules(babyId)
                        uiState.copy(
                            assignment = result.data,
                            assignmentLoading = false,
                            successMessage = "Health center assigned! Schedule generated."
                        )
                    }
                    is ApiResult.Error   -> uiState.copy(
                        error = result.message, assignmentLoading = false
                    )
                    else -> uiState.copy(assignmentLoading = false)
                }
            } catch (e: Exception) {
                uiState = uiState.copy(assignmentLoading = false, error = e.message)
            }
        }
    }

    // ── Assignment ───────────────────────────────────────────────────────────

    private fun loadAssignment(babyId: String) {
        scope.launch {
            uiState = uiState.copy(assignmentLoading = true)
            try {
                val result = apiService.getActiveAssignment(babyId)
                uiState = when (result) {
                    is ApiResult.Success -> uiState.copy(
                        assignment = result.data, assignmentLoading = false
                    )
                    else -> uiState.copy(assignment = null, assignmentLoading = false)
                }
            } catch (e: Exception) {
                uiState = uiState.copy(assignmentLoading = false, assignment = null)
            }
        }
    }

    // ── Vaccination Schedules ────────────────────────────────────────────────

    fun loadSchedules(babyId: String) {
        scope.launch {
            uiState = uiState.copy(schedulesLoading = true)
            try {
                val result = apiService.getScheduleForBaby(babyId)
                uiState = when (result) {
                    is ApiResult.Success -> uiState.copy(
                        schedules = result.data, schedulesLoading = false
                    )
                    is ApiResult.Error   -> uiState.copy(
                        error = result.message, schedulesLoading = false
                    )
                    else -> uiState.copy(schedulesLoading = false)
                }
            } catch (e: Exception) {
                uiState = uiState.copy(schedulesLoading = false, error = e.message)
            }
        }
    }

    fun setVaccinationFilter(filter: VaccinationFilter) {
        uiState = uiState.copy(vaccinationFilter = filter)
    }

    fun triggerReschedule() {
        uiState = uiState.copy(showRescheduleConfirm = true)
    }

    fun dismissReschedule() {
        uiState = uiState.copy(showRescheduleConfirm = false)
    }

    fun confirmReschedule() {
        val babyId = uiState.selectedBabyId ?: return
        val benchId = uiState.assignment?.benchId ?: return
        scope.launch {
            uiState = uiState.copy(showRescheduleConfirm = false, schedulesLoading = true)
            try {
                // Re-assign same bench → regenerates entire schedule
                val result = apiService.changeBench(babyId, benchId)
                uiState = when (result) {
                    is ApiResult.Success -> {
                        loadSchedules(babyId)
                        uiState.copy(successMessage = "Schedule regenerated successfully.")
                    }
                    is ApiResult.Error   -> uiState.copy(error = result.message, schedulesLoading = false)
                    else -> uiState.copy(schedulesLoading = false)
                }
            } catch (e: Exception) {
                uiState = uiState.copy(schedulesLoading = false, error = e.message)
            }
        }
    }

    fun selectScheduleItem(item: VaccinationScheduleUi) {
        uiState = uiState.copy(selectedScheduleItem = item)
    }

    fun clearSelectedScheduleItem() {
        uiState = uiState.copy(selectedScheduleItem = null)
    }

    // ── Health Issues ────────────────────────────────────────────────────────

    fun loadHealthIssues(babyId: String) {
        scope.launch {
            uiState = uiState.copy(healthIssuesLoading = true)
            try {
                val result = apiService.getHealthIssuesForBaby(babyId)
                uiState = when (result) {
                    is ApiResult.Success -> uiState.copy(
                        healthIssues = result.data, healthIssuesLoading = false
                    )
                    is ApiResult.Error   -> uiState.copy(
                        error = result.message, healthIssuesLoading = false
                    )
                    else -> uiState.copy(healthIssuesLoading = false)
                }
            } catch (e: Exception) {
                uiState = uiState.copy(healthIssuesLoading = false, error = e.message)
            }
        }
    }

    fun setHealthIssueFilter(filter: HealthIssueFilter) {
        uiState = uiState.copy(healthIssueFilter = filter)
    }

    fun showAddHealthIssue() { uiState = uiState.copy(showAddHealthIssue = true) }
    fun dismissAddHealthIssue() { uiState = uiState.copy(showAddHealthIssue = false) }

    fun addHealthIssue(babyId: String, title: String, description: String?, severity: String?, issueDate: String) {
        scope.launch {
            try {
                val result = apiService.createHealthIssue(babyId, title, description, severity, issueDate)
                if (result is ApiResult.Success) {
                    uiState = uiState.copy(showAddHealthIssue = false, successMessage = "Health issue added.")
                    loadHealthIssues(babyId)
                } else if (result is ApiResult.Error) {
                    uiState = uiState.copy(error = result.message)
                }
            } catch (e: Exception) {
                uiState = uiState.copy(error = e.message)
            }
        }
    }

    fun resolveHealthIssue(issueId: String) {
        scope.launch {
            try {
                val result = apiService.resolveHealthIssue(issueId)
                if (result is ApiResult.Success) {
                    val babyId = uiState.selectedBabyId ?: return@launch
                    loadHealthIssues(babyId)
                    uiState = uiState.copy(successMessage = "Issue marked as resolved.")
                }
            } catch (e: Exception) {
                uiState = uiState.copy(error = e.message)
            }
        }
    }

    fun selectHealthIssue(issue: HealthIssueUi) { uiState = uiState.copy(selectedHealthIssue = issue) }
    fun clearSelectedHealthIssue() { uiState = uiState.copy(selectedHealthIssue = null) }

    // ── Appointments ─────────────────────────────────────────────────────────

    fun loadAppointments(babyId: String) {
        scope.launch {
            uiState = uiState.copy(appointmentsLoading = true)
            try {
                val result = apiService.getAppointmentsForBaby(babyId)
                uiState = when (result) {
                    is ApiResult.Success -> uiState.copy(
                        appointments = result.data, appointmentsLoading = false
                    )
                    is ApiResult.Error   -> uiState.copy(
                        error = result.message, appointmentsLoading = false
                    )
                    else -> uiState.copy(appointmentsLoading = false)
                }
            } catch (e: Exception) {
                uiState = uiState.copy(appointmentsLoading = false, error = e.message)
            }
        }
    }

    fun setAppointmentFilter(filter: AppointmentFilter) {
        uiState = uiState.copy(appointmentFilter = filter)
    }

    fun showAddAppointment() { uiState = uiState.copy(showAddAppointment = true) }
    fun dismissAddAppointment() { uiState = uiState.copy(showAddAppointment = false) }

    fun addAppointment(
        babyId: String, type: String, date: String, time: String?,
        doctorName: String?, location: String?, notes: String?
    ) {
        scope.launch {
            try {
                val result = apiService.createAppointment(babyId, type, date, time, doctorName, location, notes)
                if (result is ApiResult.Success) {
                    uiState = uiState.copy(showAddAppointment = false, successMessage = "Appointment added.")
                    loadAppointments(babyId)
                } else if (result is ApiResult.Error) {
                    uiState = uiState.copy(error = result.message)
                }
            } catch (e: Exception) {
                uiState = uiState.copy(error = e.message)
            }
        }
    }

    fun cancelAppointment(appointmentId: String) {
        scope.launch {
            try {
                val result = apiService.cancelAppointment(appointmentId)
                if (result is ApiResult.Success) {
                    val babyId = uiState.selectedBabyId ?: return@launch
                    loadAppointments(babyId)
                    uiState = uiState.copy(successMessage = "Appointment cancelled.")
                }
            } catch (e: Exception) {
                uiState = uiState.copy(error = e.message)
            }
        }
    }

    fun selectAppointment(appointment: AppointmentUi) { uiState = uiState.copy(selectedAppointment = appointment) }
    fun clearSelectedAppointment() { uiState = uiState.copy(selectedAppointment = null) }

    // ── Sub tab ──────────────────────────────────────────────────────────────

    fun setSubTab(tab: HealthRecordSubTab) { uiState = uiState.copy(subTab = tab) }

    // ── Misc ─────────────────────────────────────────────────────────────────

    fun clearError() { uiState = uiState.copy(error = null) }
    fun clearSuccess() { uiState = uiState.copy(successMessage = null) }

    private fun resolveMapCenter() {
        // Use default based on parent's governorate (no lat/lng in parent profile)
        // Map common Iraqi governorates to approximate centers
        val governorateMap = mapOf(
            "nineveh"  to Pair(36.36, 43.18),
            "erbil"    to Pair(36.19, 44.01),
            "baghdad"  to Pair(33.34, 44.40),
            "basra"    to Pair(30.51, 47.78),
            "sulaymaniyah" to Pair(35.56, 45.44),
            "duhok"    to Pair(36.87, 42.99),
            "kirkuk"   to Pair(35.47, 44.39)
        )
        val key = uiState.parentGovernorate.lowercase().trim()
        val center = governorateMap[key] ?: Pair(36.19, 43.99)
        uiState = uiState.copy(mapCenterLat = center.first, mapCenterLng = center.second)
    }

    // Distance calculation (Haversine)
    fun computeDistances(userLat: Double, userLng: Double): List<VaccinationBenchUi> {
        return uiState.allBenches.map { bench ->
            val dist = haversineKm(userLat, userLng, bench.latitude, bench.longitude)
            bench.copy(distanceKm = dist)
        }.sortedBy { it.distanceKm }
    }

    private fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371.0
        val dLat = kotlin.math.PI / 180.0 * (lat2 - lat1)
        val dLon = kotlin.math.PI / 180.0 * (lon2 - lon1)
        val lat1Rad = kotlin.math.PI / 180.0 * lat1
        val lat2Rad = kotlin.math.PI / 180.0 * lat2
        val a = kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
                kotlin.math.cos(lat1Rad) * kotlin.math.cos(lat2Rad) *
                kotlin.math.sin(dLon / 2) * kotlin.math.sin(dLon / 2)
        return R * 2.0 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1.0 - a))
    }

    fun onDestroy() { scope.cancel() }
}