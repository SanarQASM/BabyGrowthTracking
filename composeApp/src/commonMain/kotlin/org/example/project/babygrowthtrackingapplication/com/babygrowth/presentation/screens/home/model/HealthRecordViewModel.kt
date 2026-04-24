// File: composeApp/src/commonMain/kotlin/org/example/project/babygrowthtrackingapplication/com/babygrowth/presentation/screens/home/model/HealthRecordViewModel.kt
// UPDATED — replaces the existing file, adds bench request flow

package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model

import androidx.compose.runtime.*
import kotlinx.coroutines.*
import org.example.project.babygrowthtrackingapplication.data.network.ApiResult
import org.example.project.babygrowthtrackingapplication.data.network.ApiService
import org.example.project.babygrowthtrackingapplication.data.network.BabyResponse
import org.example.project.babygrowthtrackingapplication.data.network.BenchRequestUi
import org.example.project.babygrowthtrackingapplication.data.network.toUi

// ─────────────────────────────────────────────────────────────────────────────
// UI State  (extends previous with bench-request fields)
// ─────────────────────────────────────────────────────────────────────────────

data class HealthRecordUiState(
    // Child selection
    val babies         : List<BabyResponse> = emptyList(),
    val selectedBabyId : String?            = null,

    // Branch assignment (per child)
    val assignment        : BabyBenchAssignmentUi? = null,
    val assignmentLoading : Boolean               = false,

    // ── BENCH REQUEST FLOW ───────────────────────────────────────────────────
    /** Active pending or accepted request for the selected baby. */
    val activeBenchRequest     : BenchRequestUi? = null,
    val benchRequestLoading    : Boolean         = false,
    /** True while sendRequest / cancel is in flight. */
    val benchRequestSubmitting : Boolean         = false,
    /** After team rejects: user sees the reason and can pick another bench. */
    val showRejectionReason    : Boolean         = false,

    // Map screen
    val allBenches    : List<VaccinationBenchUi> = emptyList(),
    val benchesLoading: Boolean                  = false,
    val mapFilter     : BenchMapFilter           = BenchMapFilter.ALL,
    val selectedBench : VaccinationBenchUi?      = null,

    // Parent location
    val parentGovernorate: String = "",
    val mapCenterLat     : Double = 36.19,
    val mapCenterLng     : Double = 43.99,

    // Vaccination schedule
    val schedules        : List<VaccinationScheduleUi> = emptyList(),
    val schedulesLoading : Boolean                     = false,
    val vaccinationFilter: VaccinationFilter           = VaccinationFilter.ALL,

    // Health issues
    val healthIssues       : List<HealthIssueUi> = emptyList(),
    val healthIssuesLoading: Boolean             = false,
    val healthIssueFilter  : HealthIssueFilter   = HealthIssueFilter.ALL,

    // Appointments
    val appointments        : List<AppointmentUi> = emptyList(),
    val appointmentsLoading : Boolean             = false,
    val appointmentFilter   : AppointmentFilter   = AppointmentFilter.ALL,

    // Sub-tab
    val subTab: HealthRecordSubTab = HealthRecordSubTab.VACCINATIONS,

    // Errors / messages
    val error         : String? = null,
    val successMessage: String? = null,

    // ── RESCHEDULE ─────────────────────────────────────────────────────────
    val showRescheduleReasonPicker: Boolean          = false,
    val rescheduleInProgress      : Boolean          = false,
    val rescheduleResult          : RescheduleResultUi? = null,

    // Add dialogs
    val showAddHealthIssue: Boolean = false,
    val showAddAppointment: Boolean = false,

    // View-all detail navigation
    val selectedScheduleItem: VaccinationScheduleUi? = null,
    val selectedHealthIssue : HealthIssueUi?         = null,
    val selectedAppointment : AppointmentUi?         = null
)

// ─────────────────────────────────────────────────────────────────────────────
// Helper: does this state mean the baby is properly connected to a bench?
// ─────────────────────────────────────────────────────────────────────────────
val HealthRecordUiState.isConnectedToBench: Boolean
    get() = assignment != null

val HealthRecordUiState.hasPendingRequest: Boolean
    get() = activeBenchRequest?.status == org.example.project.babygrowthtrackingapplication.data.network.BenchRequestStatusUi.PENDING

val HealthRecordUiState.hasRejectedRequest: Boolean
    get() = activeBenchRequest?.status == org.example.project.babygrowthtrackingapplication.data.network.BenchRequestStatusUi.REJECTED

// ─────────────────────────────────────────────────────────────────────────────
// ViewModel
// ─────────────────────────────────────────────────────────────────────────────

class HealthRecordViewModel(
    private val apiService         : ApiService,
    private val preferencesManager : org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.data.PreferencesManager
) {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    var uiState by mutableStateOf(HealthRecordUiState())
        private set

    private var schedulesJob: Job? = null

    // ── Initialise ────────────────────────────────────────────────────────────

    fun init(babies: List<BabyResponse>) {
        uiState = uiState.copy(babies = babies)
        if (babies.isNotEmpty() && uiState.selectedBabyId == null) {
            selectBaby(babies.first().babyId)
        }
        loadBenches()
        resolveMapCenter()
    }

    fun selectBaby(babyId: String) {
        uiState = uiState.copy(selectedBabyId = babyId, assignment = null, activeBenchRequest = null)
        loadAssignment(babyId)
        loadActiveBenchRequest(babyId)
        loadSchedules(babyId)
        loadHealthIssues(babyId)
        loadAppointments(babyId)
    }

    // ── Bench / Map ───────────────────────────────────────────────────────────

    fun loadBenches() {
        scope.launch {
            uiState = uiState.copy(benchesLoading = true)
            try {
                val result = apiService.getAllBenches()
                uiState = when (result) {
                    is ApiResult.Success -> uiState.copy(allBenches = result.data, benchesLoading = false)
                    is ApiResult.Error   -> uiState.copy(error = result.message, benchesLoading = false)
                    else                 -> uiState.copy(benchesLoading = false)
                }
            } catch (e: Exception) {
                uiState = uiState.copy(benchesLoading = false, error = e.message)
            }
        }
    }

    fun setMapFilter(filter: BenchMapFilter) { uiState = uiState.copy(mapFilter = filter) }
    fun selectBenchOnMap(bench: VaccinationBenchUi) { uiState = uiState.copy(selectedBench = bench) }
    fun clearSelectedBench() { uiState = uiState.copy(selectedBench = null) }

    // ── BENCH REQUEST FLOW ────────────────────────────────────────────────────

    private fun loadActiveBenchRequest(babyId: String) {
        scope.launch {
            uiState = uiState.copy(benchRequestLoading = true)
            try {
                val result = apiService.getActiveBenchRequest(babyId)
                uiState = when (result) {
                    is ApiResult.Success -> uiState.copy(
                        activeBenchRequest  = result.data?.toUi(),
                        benchRequestLoading = false
                    )
                    else -> uiState.copy(activeBenchRequest = null, benchRequestLoading = false)
                }
            } catch (e: Exception) {
                uiState = uiState.copy(benchRequestLoading = false)
            }
        }
    }

    /**
     * Parent taps "Send Request" for a bench on the map.
     * Creates a PENDING request; UI shows a waiting screen until team accepts/rejects.
     */
    fun sendBenchRequest(babyId: String, benchId: String, notes: String? = null) {
        scope.launch {
            uiState = uiState.copy(benchRequestSubmitting = true)
            try {
                val result = apiService.sendBenchRequest(babyId, benchId, notes)
                when (result) {
                    is ApiResult.Success -> {
                        uiState = uiState.copy(
                            activeBenchRequest  = result.data.toUi(),
                            benchRequestSubmitting = false,
                            successMessage      = "request_sent"  // localised in UI
                        )
                    }
                    is ApiResult.Error -> uiState = uiState.copy(
                        benchRequestSubmitting = false,
                        error = result.message
                    )
                    else -> uiState = uiState.copy(benchRequestSubmitting = false)
                }
            } catch (e: Exception) {
                uiState = uiState.copy(benchRequestSubmitting = false, error = e.message)
            }
        }
    }

    /** Parent cancels a pending request (before team has reviewed it). */
    fun cancelBenchRequest(requestId: String) {
        scope.launch {
            uiState = uiState.copy(benchRequestSubmitting = true)
            try {
                val result = apiService.cancelBenchRequest(requestId)
                when (result) {
                    is ApiResult.Success -> {
                        uiState = uiState.copy(
                            activeBenchRequest     = null,
                            benchRequestSubmitting = false,
                            successMessage         = "request_cancelled"
                        )
                    }
                    is ApiResult.Error -> uiState = uiState.copy(
                        benchRequestSubmitting = false,
                        error = result.message
                    )
                    else -> uiState = uiState.copy(benchRequestSubmitting = false)
                }
            } catch (e: Exception) {
                uiState = uiState.copy(benchRequestSubmitting = false, error = e.message)
            }
        }
    }

    fun dismissRejectionReason() {
        uiState = uiState.copy(showRejectionReason = false, activeBenchRequest = null)
    }

    // ── Assignment ────────────────────────────────────────────────────────────

    private fun loadAssignment(babyId: String) {
        scope.launch {
            uiState = uiState.copy(assignmentLoading = true)
            try {
                val result = apiService.getActiveAssignment(babyId)
                uiState = when (result) {
                    is ApiResult.Success -> uiState.copy(
                        assignment        = result.data,
                        assignmentLoading = false
                    )
                    else -> uiState.copy(assignment = null, assignmentLoading = false)
                }
            } catch (e: Exception) {
                uiState = uiState.copy(assignmentLoading = false, assignment = null)
            }
        }
    }

    // ── Vaccination Schedules ─────────────────────────────────────────────────

    fun loadSchedules(babyId: String) {
        schedulesJob?.cancel()
        schedulesJob = scope.launch {
            uiState = uiState.copy(schedulesLoading = true)
            try {
                val result = apiService.getScheduleForBaby(babyId)
                uiState = when (result) {
                    is ApiResult.Success -> uiState.copy(
                        schedules        = result.data,
                        schedulesLoading = false
                    )
                    is ApiResult.Error   -> uiState.copy(
                        error            = result.message,
                        schedulesLoading = false
                    )
                    else -> uiState.copy(schedulesLoading = false)
                }
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    uiState = uiState.copy(schedulesLoading = false, error = e.message)
                }
            }
        }
    }

    fun setVaccinationFilter(filter: VaccinationFilter) {
        uiState = uiState.copy(vaccinationFilter = filter)
    }

    // ── RESCHEDULE FLOW ───────────────────────────────────────────────────────

    fun triggerReschedule() { uiState = uiState.copy(showRescheduleReasonPicker = true) }
    fun dismissReschedule()  { uiState = uiState.copy(showRescheduleReasonPicker = false) }

    fun confirmReschedule(reason: RescheduleReason, notes: String? = null) {
        val babyId = uiState.selectedBabyId ?: return
        uiState = uiState.copy(showRescheduleReasonPicker = false, rescheduleInProgress = true)
        scope.launch {
            val result = apiService.rescheduleAllVaccinations(
                babyId            = babyId,
                shiftReason       = reason.name,
                notes             = notes,
                rescheduleOverdue = true
            )
            when (result) {
                is ApiResult.Success -> {
                    uiState = uiState.copy(
                        rescheduleInProgress = false,
                        rescheduleResult     = result.data
                    )
                    loadSchedules(babyId)
                }
                is ApiResult.Error -> {
                    uiState = uiState.copy(rescheduleInProgress = false, error = result.message)
                }
                else -> uiState = uiState.copy(rescheduleInProgress = false)
            }
        }
    }

    fun dismissRescheduleResult() { uiState = uiState.copy(rescheduleResult = null) }

    // ── Health Issues ─────────────────────────────────────────────────────────

    fun loadHealthIssues(babyId: String) {
        scope.launch {
            uiState = uiState.copy(healthIssuesLoading = true)
            try {
                val result = apiService.getHealthIssuesForBaby(babyId)
                uiState = when (result) {
                    is ApiResult.Success -> uiState.copy(healthIssues = result.data, healthIssuesLoading = false)
                    is ApiResult.Error   -> uiState.copy(error = result.message, healthIssuesLoading = false)
                    else -> uiState.copy(healthIssuesLoading = false)
                }
            } catch (e: Exception) {
                uiState = uiState.copy(healthIssuesLoading = false, error = e.message)
            }
        }
    }

    fun setHealthIssueFilter(filter: HealthIssueFilter) { uiState = uiState.copy(healthIssueFilter = filter) }
    fun selectHealthIssue(issue: HealthIssueUi)          { uiState = uiState.copy(selectedHealthIssue = issue) }

    fun resolveHealthIssue(issueId: String) {
        val babyId = uiState.selectedBabyId ?: return
        scope.launch {
            try {
                val result = apiService.resolveHealthIssue(issueId)
                if (result is ApiResult.Success) {
                    loadHealthIssues(babyId)
                    uiState = uiState.copy(successMessage = "issue_resolved")
                } else if (result is ApiResult.Error) {
                    uiState = uiState.copy(error = result.message)
                }
            } catch (e: Exception) {
                uiState = uiState.copy(error = e.message)
            }
        }
    }

    fun showAddHealthIssue()    { uiState = uiState.copy(showAddHealthIssue = true) }
    fun dismissAddHealthIssue() { uiState = uiState.copy(showAddHealthIssue = false) }

    fun addHealthIssue(babyId: String, title: String, description: String?, severity: String?, issueDate: String) {
        scope.launch {
            try {
                val result = apiService.createHealthIssue(babyId, title, description, severity, issueDate)
                if (result is ApiResult.Success) {
                    uiState = uiState.copy(showAddHealthIssue = false, successMessage = "health_issue_added")
                    loadHealthIssues(babyId)
                } else if (result is ApiResult.Error) {
                    uiState = uiState.copy(error = result.message)
                }
            } catch (e: Exception) {
                uiState = uiState.copy(error = e.message)
            }
        }
    }

    // ── Appointments ──────────────────────────────────────────────────────────

    fun loadAppointments(babyId: String) {
        scope.launch {
            uiState = uiState.copy(appointmentsLoading = true)
            try {
                val result = apiService.getAppointmentsForBaby(babyId)
                uiState = when (result) {
                    is ApiResult.Success -> uiState.copy(appointments = result.data, appointmentsLoading = false)
                    is ApiResult.Error   -> uiState.copy(error = result.message, appointmentsLoading = false)
                    else -> uiState.copy(appointmentsLoading = false)
                }
            } catch (e: Exception) {
                uiState = uiState.copy(appointmentsLoading = false, error = e.message)
            }
        }
    }

    fun setAppointmentFilter(filter: AppointmentFilter) { uiState = uiState.copy(appointmentFilter = filter) }
    fun selectAppointment(appointment: AppointmentUi)   { uiState = uiState.copy(selectedAppointment = appointment) }

    fun cancelAppointment(appointmentId: String) {
        val babyId = uiState.selectedBabyId ?: return
        scope.launch {
            try {
                val result = apiService.cancelAppointment(appointmentId)
                if (result is ApiResult.Success) {
                    loadAppointments(babyId)
                    uiState = uiState.copy(successMessage = "appointment_cancelled")
                } else if (result is ApiResult.Error) {
                    uiState = uiState.copy(error = result.message)
                }
            } catch (e: Exception) {
                uiState = uiState.copy(error = e.message)
            }
        }
    }

    fun showAddAppointment()    { uiState = uiState.copy(showAddAppointment = true) }
    fun dismissAddAppointment() { uiState = uiState.copy(showAddAppointment = false) }

    fun addAppointment(babyId: String, type: String, date: String, time: String?, doctorName: String?, location: String?, notes: String?) {
        scope.launch {
            try {
                val result = apiService.createAppointment(babyId, type, date, time, doctorName, location, notes)
                if (result is ApiResult.Success) {
                    uiState = uiState.copy(showAddAppointment = false, successMessage = "appointment_added")
                    loadAppointments(babyId)
                } else if (result is ApiResult.Error) {
                    uiState = uiState.copy(error = result.message)
                }
            } catch (e: Exception) {
                uiState = uiState.copy(error = e.message)
            }
        }
    }

    // ── Sub-tab ───────────────────────────────────────────────────────────────

    fun setSubTab(tab: HealthRecordSubTab) { uiState = uiState.copy(subTab = tab) }

    // ── Map center ────────────────────────────────────────────────────────────

    private fun resolveMapCenter() {
        val governorateMap = mapOf(
            "nineveh"      to Pair(36.36, 43.18),
            "erbil"        to Pair(36.19, 44.01),
            "baghdad"      to Pair(33.34, 44.40),
            "basra"        to Pair(30.51, 47.78),
            "sulaymaniyah" to Pair(35.56, 45.44),
            "duhok"        to Pair(36.87, 42.99),
            "kirkuk"       to Pair(35.47, 44.39)
        )
        val key    = uiState.parentGovernorate.lowercase().trim()
        val center = governorateMap[key] ?: Pair(36.19, 43.99)
        uiState = uiState.copy(mapCenterLat = center.first, mapCenterLng = center.second)
    }

    // ── Utilities ─────────────────────────────────────────────────────────────

    fun clearError()   { uiState = uiState.copy(error = null) }
    fun clearSuccess() { uiState = uiState.copy(successMessage = null) }
    fun onDestroy()    { scope.cancel() }
}