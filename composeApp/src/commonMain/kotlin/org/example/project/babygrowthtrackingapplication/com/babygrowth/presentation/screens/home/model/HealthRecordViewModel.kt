package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model

import androidx.compose.runtime.*
import kotlinx.coroutines.*
import org.example.project.babygrowthtrackingapplication.data.network.ApiResult
import org.example.project.babygrowthtrackingapplication.data.network.ApiService
import org.example.project.babygrowthtrackingapplication.data.network.BabyResponse

// ─────────────────────────────────────────────────────────────────────────────
// UI State
// ─────────────────────────────────────────────────────────────────────────────

data class HealthRecordUiState(
    // Child selection
    val babies         : List<BabyResponse> = emptyList(),
    val selectedBabyId : String?            = null,

    // Branch assignment (per child)
    val assignment        : BabyBenchAssignmentUi? = null,
    val assignmentLoading : Boolean               = false,

    // Map screen
    val allBenches    : List<VaccinationBenchUi> = emptyList(),
    val benchesLoading: Boolean                  = false,
    val mapFilter     : BenchMapFilter           = BenchMapFilter.ALL,
    val selectedBench : VaccinationBenchUi?      = null,

    // Parent location (city-level from profile — no lat/lng)
    val parentGovernorate: String = "",
    val mapCenterLat     : Double = 36.19, // Nineveh default
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

    // ── RESCHEDULE ────────────────────────────────────────────────────────────
    /**
     * Step 1 — user taps "Reschedule" → show reason-picker dialog.
     */
    val showRescheduleReasonPicker: Boolean = false,

    /**
     * Step 2 — after the user picks a reason and confirms, we call the API.
     * While the call is in flight this is true.
     */
    val rescheduleInProgress: Boolean = false,

    /**
     * Step 3 — API responded; show per-vaccine result summary dialog.
     * Null when no result is available yet.
     */
    val rescheduleResult: RescheduleResultUi? = null,

    // Add dialogs
    val showAddHealthIssue: Boolean = false,
    val showAddAppointment: Boolean = false,

    // View-all detail navigation
    val selectedScheduleItem: VaccinationScheduleUi? = null,
    val selectedHealthIssue : HealthIssueUi?         = null,
    val selectedAppointment : AppointmentUi?         = null
)

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

    // Cancel-able schedule-load job — prevents stale responses overwriting fresh ones
    private var schedulesJob: Job? = null

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
                    is ApiResult.Success -> uiState.copy(allBenches = result.data, benchesLoading = false)
                    is ApiResult.Error   -> uiState.copy(error = result.message, benchesLoading = false)
                    else                 -> uiState.copy(benchesLoading = false)
                }
            } catch (e: Exception) {
                uiState = uiState.copy(benchesLoading = false, error = e.message)
            }
        }
    }

    fun setMapFilter(filter: BenchMapFilter)         { uiState = uiState.copy(mapFilter = filter) }
    fun selectBenchOnMap(bench: VaccinationBenchUi)  { uiState = uiState.copy(selectedBench = bench) }
    fun clearSelectedBench()                          { uiState = uiState.copy(selectedBench = null) }

    fun assignBench(babyId: String, benchId: String) {
        scope.launch {
            uiState = uiState.copy(assignmentLoading = true)
            try {
                val result = apiService.assignBench(babyId, benchId)
                when (result) {
                    is ApiResult.Success -> {
                        uiState = uiState.copy(
                            assignment        = result.data,
                            assignmentLoading = false,
                            successMessage    = "Health center assigned!"
                        )
                        loadSchedules(babyId)
                    }
                    is ApiResult.Error -> uiState = uiState.copy(
                        error = result.message, assignmentLoading = false
                    )
                    else -> uiState = uiState.copy(assignmentLoading = false)
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

    // ── Vaccination Schedules ────────────────────────────────────────────────

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

    /**
     * Step 1: user taps "Reschedule" banner or button.
     * Opens the reason-picker dialog.
     */
    fun triggerReschedule() {
        uiState = uiState.copy(showRescheduleReasonPicker = true)
    }

    /** Dismiss the reason-picker without doing anything. */
    fun dismissReschedule() {
        uiState = uiState.copy(showRescheduleReasonPicker = false)
    }

    /**
     * Step 2: user chose a reason and tapped "Reschedule".
     * Calls the new `/baby/{babyId}/reschedule` endpoint.
     */
    fun confirmReschedule(reason: RescheduleReason, notes: String? = null) {
        val babyId = uiState.selectedBabyId ?: return
        uiState = uiState.copy(
            showRescheduleReasonPicker = false,
            rescheduleInProgress       = true
        )
        scope.launch {
            val result = apiService.rescheduleAllVaccinations(
                babyId            = babyId,
                shiftReason       = reason.name,
                notes             = notes,
                rescheduleOverdue = true
            )
            when (result) {
                is ApiResult.Success -> {
                    val resultUi = result.data
                    uiState = uiState.copy(
                        rescheduleInProgress = false,
                        rescheduleResult     = resultUi,
                        // Reload the schedule so the list reflects the new dates
                    )
                    loadSchedules(babyId)
                }
                is ApiResult.Error -> {
                    uiState = uiState.copy(
                        rescheduleInProgress = false,
                        error                = result.message
                    )
                }
                else -> uiState = uiState.copy(rescheduleInProgress = false)
            }
        }
    }

    /** Step 3: user dismisses the result summary dialog. */
    fun dismissRescheduleResult() {
        uiState = uiState.copy(rescheduleResult = null)
    }

    // ── Health Issues ────────────────────────────────────────────────────────

    fun loadHealthIssues(babyId: String) {
        scope.launch {
            uiState = uiState.copy(healthIssuesLoading = true)
            try {
                val result = apiService.getHealthIssuesForBaby(babyId)
                uiState = when (result) {
                    is ApiResult.Success -> uiState.copy(
                        healthIssues        = result.data,
                        healthIssuesLoading = false
                    )
                    is ApiResult.Error   -> uiState.copy(
                        error               = result.message,
                        healthIssuesLoading = false
                    )
                    else -> uiState.copy(healthIssuesLoading = false)
                }
            } catch (e: Exception) {
                uiState = uiState.copy(healthIssuesLoading = false, error = e.message)
            }
        }
    }

    fun setHealthIssueFilter(filter: HealthIssueFilter) { uiState = uiState.copy(healthIssueFilter = filter) }
    fun selectHealthIssue(issue: HealthIssueUi)         { uiState = uiState.copy(selectedHealthIssue = issue) }

    fun resolveHealthIssue(issueId: String) {
        val babyId = uiState.selectedBabyId ?: return
        scope.launch {
            try {
                val result = apiService.resolveHealthIssue(issueId)
                if (result is ApiResult.Success) {
                    loadHealthIssues(babyId)
                    uiState = uiState.copy(successMessage = "Issue marked as resolved.")
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

    fun addHealthIssue(
        babyId     : String, title: String, description: String?,
        severity   : String?, issueDate: String
    ) {
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

    // ── Appointments ─────────────────────────────────────────────────────────

    fun loadAppointments(babyId: String) {
        scope.launch {
            uiState = uiState.copy(appointmentsLoading = true)
            try {
                val result = apiService.getAppointmentsForBaby(babyId)
                uiState = when (result) {
                    is ApiResult.Success -> uiState.copy(
                        appointments        = result.data,
                        appointmentsLoading = false
                    )
                    is ApiResult.Error   -> uiState.copy(
                        error               = result.message,
                        appointmentsLoading = false
                    )
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
                    uiState = uiState.copy(successMessage = "Appointment cancelled.")
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

    // ── Sub-tab ──────────────────────────────────────────────────────────────

    fun setSubTab(tab: HealthRecordSubTab) { uiState = uiState.copy(subTab = tab) }

    // ── Map center ───────────────────────────────────────────────────────────

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

    // ── Utilities ────────────────────────────────────────────────────────────

    fun clearError()   { uiState = uiState.copy(error = null) }
    fun clearSuccess() { uiState = uiState.copy(successMessage = null) }

    fun onDestroy() { scope.cancel() }
}