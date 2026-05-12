package org.example.project.babygrowthtrackingapplication.team

import androidx.compose.runtime.*
import kotlinx.coroutines.*
import kotlinx.datetime.*
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.data.PreferencesManager
import org.example.project.babygrowthtrackingapplication.data.network.ApiResult
import org.example.project.babygrowthtrackingapplication.data.network.ApiService
import org.example.project.babygrowthtrackingapplication.data.network.BabyBenchAssignmentUi
import org.example.project.babygrowthtrackingapplication.data.network.CreateGrowthRecordRequest
import org.example.project.babygrowthtrackingapplication.data.network.GrowthRecordResponse
import org.example.project.babygrowthtrackingapplication.data.network.VaccinationScheduleUi
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

// ─────────────────────────────────────────────────────────────────────────────
// Data Models
// ─────────────────────────────────────────────────────────────────────────────

data class TeamBabyItem(
    val babyId       : String,
    val fullName     : String,
    val dateOfBirth  : String,
    val ageInMonths  : Int,
    val gender       : String,
    val parentName   : String,
    val parentPhone  : String,
    val nextVacDate  : String?,
    val vaccineStatus: TeamVaccineStatus,
    val benchName    : String
)

enum class TeamVaccineStatus { UP_TO_DATE, DUE_SOON, OVERDUE, NO_SCHEDULE }

data class TeamScheduleItem(
    val scheduleId      : String,
    val babyId          : String,
    val babyName        : String,
    val ageInMonths     : Int,
    val gender          : String,
    val vaccineName     : String,
    val vaccineNameAr   : String?,
    val doseNumber      : Int,
    val scheduledDate   : String,
    val status          : String,
    val benchNameEn     : String
)

data class CompleteVaccinationForm(
    val scheduleId      : String,
    val administeredDate: String,
    val batchNumber     : String,
    val location        : String,
    val notes           : String,
    val isLoading       : Boolean = false
)

enum class TeamVaccinationFilter { ALL, UPCOMING, COMPLETED, OVERDUE }

// ─────────────────────────────────────────────────────────────────────────────
// UI State
// ─────────────────────────────────────────────────────────────────────────────

data class TeamVaccinationUiState(
    val teamMemberName   : String = "",
    val teamMemberId     : String = "",
    val benchId          : String = "",
    val benchName        : String = "",

    val babies           : List<TeamBabyItem> = emptyList(),
    val babiesLoading    : Boolean = false,
    val searchQuery      : String = "",
    val showActiveOnly   : Boolean = true,

    val scheduleItems    : List<TeamScheduleItem> = emptyList(),
    val scheduleLoading  : Boolean = false,
    val selectedDate     : String = "",

    val detailBaby            : TeamBabyItem? = null,
    val detailSchedules       : List<VaccinationScheduleUi> = emptyList(),
    val detailSchedulesLoading: Boolean = false,

    // ── Growth records — split into two separate lists ──────────────────────
    // teamGrowthRecords : records added BY team members (isTeamMeasurement=true)
    //   → shown in the Team vaccination screen
    // allGrowthRecords  : ALL records (parent + team combined)
    //   → NOT shown in team screen; parent app uses its own HomeViewModel
    val detailTeamGrowthRecords : List<GrowthRecordResponse> = emptyList(),
    val detailGrowthLoading     : Boolean = false,

    val detailVacFilter       : TeamVaccinationFilter = TeamVaccinationFilter.ALL,

    val completeForm        : CompleteVaccinationForm? = null,

    val showAddMeasurement  : Boolean = false,
    val measurementBabyId   : String = "",

    val errorMessage        : String? = null,
    val successMessage      : String? = null,

    val benchLoading        : Boolean = false,
    val noBenchAssigned     : Boolean = false
) {
    val filteredBabies: List<TeamBabyItem>
        get() = babies.filter { baby ->
            searchQuery.isBlank() || baby.fullName.contains(searchQuery, ignoreCase = true)
        }
}

// ─────────────────────────────────────────────────────────────────────────────
// ViewModel
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalTime::class)
class TeamVaccinationViewModel(
    val apiService         : ApiService,
    private val preferencesManager: PreferencesManager
) {
    var uiState by mutableStateOf(TeamVaccinationUiState())
        private set

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    init {
        refreshIdentity()
        initTodayDate()
        loadBenchAndBabies()
    }

    // ── Identity ──────────────────────────────────────────────────────────────

    private fun refreshIdentity() {
        val userId = preferencesManager.getUserId() ?: ""
        val name   = preferencesManager.getUserName() ?: ""
        uiState    = uiState.copy(teamMemberId = userId, teamMemberName = name)
    }

    private fun initTodayDate() {
        val now   = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val today = "${now.year}-${now.month.number.toString().padStart(2, '0')}-${now.day.toString().padStart(2, '0')}"
        uiState   = uiState.copy(selectedDate = today)
    }

    // ── Public entry points ───────────────────────────────────────────────────

    fun loadTeamData() {
        refreshIdentity()
        initTodayDate()
        loadBenchAndBabies()
    }

    fun onSessionRestored() = loadTeamData()

    // ── Core: resolve bench → load babies ─────────────────────────────────────

    private fun loadBenchAndBabies() {
        val teamMemberId = uiState.teamMemberId
        if (teamMemberId.isBlank()) {
            uiState = uiState.copy(noBenchAssigned = true, benchLoading = false)
            return
        }

        scope.launch {
            uiState = uiState.copy(benchLoading = true, babiesLoading = true, noBenchAssigned = false)

            try {
                val benchResult = apiService.getBenchByTeamMember(teamMemberId)
                val bench = when (benchResult) {
                    is ApiResult.Success -> benchResult.data
                    else                 -> null
                }

                if (bench == null) {
                    uiState = uiState.copy(
                        benchLoading    = false,
                        babiesLoading   = false,
                        noBenchAssigned = true
                    )
                    return@launch
                }

                uiState = uiState.copy(
                    benchId      = bench.benchId,
                    benchName    = bench.nameEn,
                    benchLoading = false
                )

                loadBabiesForBench(bench.benchId)
                loadScheduleForDate(bench.benchId, uiState.selectedDate)

            } catch (e: Exception) {
                uiState = uiState.copy(
                    benchLoading  = false,
                    babiesLoading = false,
                    errorMessage  = "Failed to load bench: ${e.message}"
                )
            }
        }
    }

    private suspend fun loadBabiesForBench(benchId: String) {
        try {
            val assignResult = apiService.getAssignmentsByBench(benchId)
            if (assignResult !is ApiResult.Success) {
                uiState = uiState.copy(babiesLoading = false)
                return
            }

            val results: List<TeamBabyItem> = coroutineScope {
                assignResult.data.map { assignment: BabyBenchAssignmentUi ->
                    async {
                        val babyResult = apiService.getBaby(assignment.babyId)
                        if (babyResult !is ApiResult.Success) return@async null
                        val baby = babyResult.data

                        val schedules: List<VaccinationScheduleUi> = try {
                            val r = apiService.getScheduleForBaby(baby.babyId)
                            if (r is ApiResult.Success) r.data else emptyList()
                        } catch (_: Exception) { emptyList() }

                        val status   = determineVaccineStatus(schedules)
                        val nextDate = schedules
                            .filter { s -> s.status == "UPCOMING" || s.status == "DUE_SOON" }
                            .minByOrNull { s -> s.scheduledDate }?.scheduledDate

                        TeamBabyItem(
                            babyId        = baby.babyId,
                            fullName      = baby.fullName,
                            dateOfBirth   = baby.dateOfBirth,
                            ageInMonths   = baby.ageInMonths,
                            gender        = baby.gender,
                            parentName    = baby.parentName,
                            parentPhone   = "",
                            nextVacDate   = nextDate,
                            vaccineStatus = status,
                            benchName     = assignment.benchNameEn
                        )
                    }
                }.awaitAll().filterNotNull()
            }

            uiState = uiState.copy(babies = results, babiesLoading = false)
        } catch (e: Exception) {
            uiState = uiState.copy(babiesLoading = false, errorMessage = e.message)
        }
    }

    private fun determineVaccineStatus(schedules: List<VaccinationScheduleUi>): TeamVaccineStatus {
        if (schedules.isEmpty()) return TeamVaccineStatus.NO_SCHEDULE
        return when {
            schedules.any { it.status == "OVERDUE" }  -> TeamVaccineStatus.OVERDUE
            schedules.any { it.status == "DUE_SOON" } -> TeamVaccineStatus.DUE_SOON
            else                                       -> TeamVaccineStatus.UP_TO_DATE
        }
    }

    // ── Schedule tab ──────────────────────────────────────────────────────────

    private fun loadScheduleForDate(benchId: String, date: String) {
        if (benchId.isBlank() || date.isBlank()) return
        scope.launch {
            uiState = uiState.copy(scheduleLoading = true)
            try {
                val items = mutableListOf<TeamScheduleItem>()

                val benchDayResult = apiService.getBenchDaySchedule(benchId, date)
                if (benchDayResult is ApiResult.Success) {
                    benchDayResult.data.forEach { schedUi ->
                        val cachedBaby = uiState.babies.find { baby -> baby.babyId == schedUi.babyId }
                        items.add(
                            TeamScheduleItem(
                                scheduleId    = schedUi.scheduleId,
                                babyId        = schedUi.babyId,
                                babyName      = cachedBaby?.fullName ?: "",
                                ageInMonths   = cachedBaby?.ageInMonths ?: 0,
                                gender        = cachedBaby?.gender ?: "",
                                vaccineName   = schedUi.vaccineName,
                                vaccineNameAr = schedUi.vaccineNameAr,
                                doseNumber    = schedUi.doseNumber,
                                scheduledDate = schedUi.scheduledDate,
                                status        = schedUi.status,
                                benchNameEn   = schedUi.benchNameEn
                            )
                        )
                    }
                    uiState = uiState.copy(scheduleItems = items, scheduleLoading = false)
                    return@launch
                }

                // Fallback: per-baby schedule
                uiState.babies.forEach { baby ->
                    val r = apiService.getScheduleForBaby(baby.babyId)
                    if (r is ApiResult.Success) {
                        r.data.filter { schedUi -> schedUi.scheduledDate == date }.forEach { schedUi ->
                            items.add(
                                TeamScheduleItem(
                                    scheduleId    = schedUi.scheduleId,
                                    babyId        = baby.babyId,
                                    babyName      = baby.fullName,
                                    ageInMonths   = baby.ageInMonths,
                                    gender        = baby.gender,
                                    vaccineName   = schedUi.vaccineName,
                                    vaccineNameAr = schedUi.vaccineNameAr,
                                    doseNumber    = schedUi.doseNumber,
                                    scheduledDate = schedUi.scheduledDate,
                                    status        = schedUi.status,
                                    benchNameEn   = schedUi.benchNameEn
                                )
                            )
                        }
                    }
                }
                uiState = uiState.copy(scheduleItems = items, scheduleLoading = false)
            } catch (e: Exception) {
                uiState = uiState.copy(scheduleLoading = false, errorMessage = e.message)
            }
        }
    }

    fun onDateSelected(date: String) {
        uiState = uiState.copy(selectedDate = date)
        if (uiState.benchId.isNotBlank()) loadScheduleForDate(uiState.benchId, date)
    }

    // ── Search ────────────────────────────────────────────────────────────────

    fun onSearchQueryChange(q: String) { uiState = uiState.copy(searchQuery = q) }
    fun toggleActiveFilter()           { uiState = uiState.copy(showActiveOnly = !uiState.showActiveOnly) }

    // ── Baby detail ───────────────────────────────────────────────────────────

    fun loadBabyDetail(baby: TeamBabyItem) {
        uiState = uiState.copy(
            detailBaby              = baby,
            detailSchedulesLoading  = true,
            detailGrowthLoading     = true,
            detailSchedules         = emptyList(),
            detailTeamGrowthRecords = emptyList()
        )
        scope.launch {
            // Load schedules
            launch {
                try {
                    val r = apiService.getScheduleForBaby(baby.babyId)
                    uiState = if (r is ApiResult.Success)
                        uiState.copy(detailSchedules = r.data, detailSchedulesLoading = false)
                    else
                        uiState.copy(detailSchedulesLoading = false)
                } catch (e: Exception) {
                    uiState = uiState.copy(detailSchedulesLoading = false, errorMessage = e.message)
                }
            }
            // Load TEAM-ONLY growth records
            // Team members only see records they themselves added (isTeamMeasurement=true)
            // The full combined view (parent+team) is only visible to the parent.
            launch {
                loadTeamGrowthRecords(baby.babyId)
            }
        }
    }

    // ── Team-only growth records ──────────────────────────────────────────────
    // Fetches all growth records then filters to isTeamMeasurement=true.
    // This means team members see only what their bench has recorded —
    // they cannot see measurements the parent added privately.

    private suspend fun loadTeamGrowthRecords(babyId: String) {
        uiState = uiState.copy(detailGrowthLoading = true)
        try {
            val r = apiService.getGrowthRecords(babyId)
            val teamOnly = if (r is ApiResult.Success)
                r.data.filter { it.addedByTeam }
                    .sortedByDescending { it.measurementDate }
            else
                emptyList()

            uiState = uiState.copy(
                detailTeamGrowthRecords = teamOnly,
                detailGrowthLoading     = false
            )
        } catch (e: Exception) {
            uiState = uiState.copy(detailGrowthLoading = false, errorMessage = e.message)
        }
    }

    fun setDetailVacFilter(f: TeamVaccinationFilter) { uiState = uiState.copy(detailVacFilter = f) }

    // ── Complete vaccination (via team-status endpoint) ───────────────────────

    fun openCompleteDialog(scheduleId: String) {
        val now   = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val today = "${now.year}-${now.month.number.toString().padStart(2, '0')}-${now.day.toString().padStart(2, '0')}"
        uiState   = uiState.copy(
            completeForm = CompleteVaccinationForm(
                scheduleId       = scheduleId,
                administeredDate = today,
                batchNumber      = "",
                location         = uiState.benchName,
                notes            = ""
            )
        )
    }

    fun dismissCompleteDialog() { uiState = uiState.copy(completeForm = null) }

    fun updateCompleteForm(update: (CompleteVaccinationForm) -> CompleteVaccinationForm) {
        uiState.completeForm?.let { uiState = uiState.copy(completeForm = update(it)) }
    }

    fun submitCompleteVaccination() {
        val form   = uiState.completeForm ?: return
        val babyId = uiState.detailBaby?.babyId
            ?: uiState.scheduleItems.find { item -> item.scheduleId == form.scheduleId }?.babyId
            ?: return
        uiState = uiState.copy(completeForm = form.copy(isLoading = true))
        scope.launch {
            try {
                // Use team-status route: enforces MISSED lock + only COMPLETED/SKIPPED
                val result = apiService.updateVaccinationScheduleStatus(
                    scheduleId   = form.scheduleId,
                    status       = "COMPLETED",
                    useTeamRoute = true
                )
                if (result is ApiResult.Success) {
                    uiState = uiState.copy(completeForm = null, successMessage = "Vaccination marked as completed ✅")
                    refreshDetailAfterStatusChange(babyId)
                } else {
                    val msg = (result as? ApiResult.Error)?.message ?: "Failed to update"
                    uiState = uiState.copy(completeForm = form.copy(isLoading = false), errorMessage = msg)
                }
            } catch (e: Exception) {
                uiState = uiState.copy(completeForm = form.copy(isLoading = false), errorMessage = e.message)
            }
        }
    }

    // ── Skip vaccination (NEW) ─────────────────────────────────────────────────
    // Routes through /team-status endpoint which enforces:
    //   - MISSED cannot be skipped (locked)
    //   - COMPLETED cannot be skipped (locked)
    //   - Only COMPLETED and SKIPPED are valid targets

    fun skipVaccination(scheduleId: String, babyId: String? = null) {
        val resolvedBabyId = babyId
            ?: uiState.detailBaby?.babyId
            ?: uiState.scheduleItems.find { it.scheduleId == scheduleId }?.babyId
            ?: return

        scope.launch {
            try {
                val result = apiService.updateVaccinationScheduleStatus(
                    scheduleId   = scheduleId,
                    status       = "SKIPPED",
                    useTeamRoute = true        // enforces MISSED lock server-side
                )
                if (result is ApiResult.Success) {
                    uiState = uiState.copy(successMessage = "Vaccination skipped")
                    refreshDetailAfterStatusChange(resolvedBabyId)
                } else {
                    val msg = (result as? ApiResult.Error)?.message ?: "Failed to skip"
                    uiState = uiState.copy(errorMessage = msg)
                }
            } catch (e: Exception) {
                uiState = uiState.copy(errorMessage = e.message)
            }
        }
    }

    // ── Mark as missed (existing — unchanged, goes through general /status) ───

    fun markAsMissed(scheduleId: String, babyId: String? = null) {
        val resolvedBabyId = babyId
            ?: uiState.detailBaby?.babyId
            ?: uiState.scheduleItems.find { item -> item.scheduleId == scheduleId }?.babyId
            ?: return

        scope.launch {
            try {
                // Note: markAsMissed uses the general /status route (admin/system action)
                // NOT the /team-status route, because MISSED is a system status set when
                // the scheduled date passes without completion.
                val result = apiService.updateVaccinationScheduleStatus(
                    scheduleId   = scheduleId,
                    status       = "MISSED",
                    useTeamRoute = false
                )
                if (result is ApiResult.Success) {
                    uiState = uiState.copy(successMessage = "Marked as missed")
                    refreshDetailAfterStatusChange(resolvedBabyId)
                } else {
                    uiState = uiState.copy(errorMessage = "Failed to update status")
                }
            } catch (e: Exception) {
                uiState = uiState.copy(errorMessage = e.message)
            }
        }
    }

    // ── Refresh detail data after any status change ───────────────────────────

    private suspend fun refreshDetailAfterStatusChange(babyId: String) {
        // Refresh schedule list
        if (uiState.detailBaby?.babyId == babyId) {
            val r = apiService.getScheduleForBaby(babyId)
            if (r is ApiResult.Success) uiState = uiState.copy(detailSchedules = r.data)
        }
        // Refresh schedule tab
        if (uiState.benchId.isNotBlank()) {
            loadScheduleForDate(uiState.benchId, uiState.selectedDate)
        }
    }

    // ── Add measurement ───────────────────────────────────────────────────────

    fun openAddMeasurement(babyId: String) {
        uiState = uiState.copy(showAddMeasurement = true, measurementBabyId = babyId)
    }

    fun dismissAddMeasurement() { uiState = uiState.copy(showAddMeasurement = false) }

    fun addMeasurement(babyId: String, weight: Double?, height: Double?, headCirc: Double?, date: String) {
        scope.launch {
            try {
                val result = apiService.createGrowthRecord(
                    userId  = uiState.teamMemberId,
                    request = CreateGrowthRecordRequest(
                        babyId            = babyId,
                        measurementDate   = date,
                        weight            = weight,
                        height            = height,
                        headCircumference = headCirc
                    )
                )
                if (result is ApiResult.Success) {
                    uiState = uiState.copy(showAddMeasurement = false, successMessage = "Measurement saved 📏")
                    // Reload team-only growth records after adding a new one
                    loadTeamGrowthRecords(babyId)
                } else {
                    uiState = uiState.copy(errorMessage = "Failed to save measurement")
                }
            } catch (e: Exception) {
                uiState = uiState.copy(errorMessage = e.message)
            }
        }
    }

    // ── Messages ──────────────────────────────────────────────────────────────

    fun clearError()   { uiState = uiState.copy(errorMessage = null) }
    fun clearSuccess() { uiState = uiState.copy(successMessage = null) }
    fun onDestroy()    { scope.cancel() }
}