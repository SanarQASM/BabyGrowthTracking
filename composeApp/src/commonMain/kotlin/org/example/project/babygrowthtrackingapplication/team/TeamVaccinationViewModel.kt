// composeApp/src/commonMain/kotlin/org/example/project/babygrowthtrackingapplication/team/TeamVaccinationViewModel.kt

package org.example.project.babygrowthtrackingapplication.team

import androidx.compose.runtime.*
import kotlinx.coroutines.*
import kotlinx.datetime.*
import kotlinx.datetime.number
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.data.PreferencesManager
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model.*
import org.example.project.babygrowthtrackingapplication.data.network.*
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

// ─────────────────────────────────────────────────────────────────────────────
// UI State
// ─────────────────────────────────────────────────────────────────────────────

data class TeamVaccinationUiState(
    val teamMemberName   : String = "",
    val teamMemberId     : String = "",
    val benchId          : String = "",
    val benchName        : String = "",

    // Babies tab
    val babies           : List<TeamBabyItem> = emptyList(),
    val babiesLoading    : Boolean = false,
    val searchQuery      : String = "",
    val showActiveOnly   : Boolean = true,

    // Schedule tab
    val scheduleItems    : List<TeamScheduleItem> = emptyList(),
    val scheduleLoading  : Boolean = false,
    val selectedDate     : String = "",

    // Baby detail
    val detailBaby            : TeamBabyItem? = null,
    val detailSchedules       : List<VaccinationScheduleUi> = emptyList(),
    val detailSchedulesLoading: Boolean = false,
    val detailGrowthRecords   : List<GrowthRecordResponse> = emptyList(),
    val detailGrowthLoading   : Boolean = false,
    val detailVacFilter       : VaccinationFilter = VaccinationFilter.ALL,

    // Complete vaccination dialog
    val completeForm        : CompleteVaccinationForm? = null,

    // Add measurement
    val showAddMeasurement  : Boolean = false,
    val measurementBabyId   : String = "",

    // Error / success
    val errorMessage        : String? = null,
    val successMessage      : String? = null,

    // True while we are still resolving the team member's bench
    val benchLoading        : Boolean = false,
    // True when the bench lookup succeeded but the team member has no bench
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
        val today = "${now.year}-${now.month.number.toString().padStart(2,'0')}-${now.day.toString().padStart(2,'0')}"
        uiState   = uiState.copy(selectedDate = today)
    }

    // ── Public entry points ───────────────────────────────────────────────────

    /** Called after a fresh VACCINATION_TEAM login. */
    fun loadTeamData() {
        refreshIdentity()
        initTodayDate()
        loadBenchAndBabies()
    }

    /** Called when the app resumes with a persisted session. */
    fun onSessionRestored() = loadTeamData()

    // ── Core: resolve bench → load babies ─────────────────────────────────────
    //
    // KEY FIX: previously we called getAllBenches().first() which gave the team
    // member a random bench.  Now we call getBenchByTeamMember(teamMemberId) so
    // each team member only sees the bench they are assigned to manage.

    private fun loadBenchAndBabies() {
        val teamMemberId = uiState.teamMemberId
        if (teamMemberId.isBlank()) {
            uiState = uiState.copy(noBenchAssigned = true, benchLoading = false)
            return
        }

        scope.launch {
            uiState = uiState.copy(benchLoading = true, babiesLoading = true, noBenchAssigned = false)

            try {
                // ── Step 1: resolve this team member's bench ───────────────
                val benchResult = apiService.getBenchByTeamMember(teamMemberId)

                val bench = when (benchResult) {
                    is ApiResult.Success -> benchResult.data
                    else                 -> null
                }

                if (bench == null) {
                    // Team member exists but has no bench assigned yet
                    uiState = uiState.copy(
                        benchLoading    = false,
                        babiesLoading   = false,
                        noBenchAssigned = true
                    )
                    return@launch
                }

                uiState = uiState.copy(
                    benchId     = bench.benchId,
                    benchName   = bench.nameEn,
                    benchLoading = false
                )

                // ── Step 2: load babies assigned to this bench ─────────────
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

            val babies = mutableListOf<TeamBabyItem>()

            for (assignment in assignResult.data) {
                val babyResult = apiService.getBaby(assignment.babyId)
                if (babyResult !is ApiResult.Success) continue
                val baby = babyResult.data

                val schedules = try {
                    val r = apiService.getScheduleForBaby(baby.babyId)
                    if (r is ApiResult.Success) r.data else emptyList()
                } catch (_: Exception) { emptyList() }

                val status   = determineVaccineStatus(schedules)
                val nextDate = schedules
                    .filter { it.status == "UPCOMING" || it.status == "DUE_SOON" }
                    .minByOrNull { it.scheduledDate }?.scheduledDate

                babies.add(
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
                )
            }

            uiState = uiState.copy(babies = babies, babiesLoading = false)
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
        scope.launch {
            uiState = uiState.copy(scheduleLoading = true)
            try {
                val items = mutableListOf<TeamScheduleItem>()
                for (baby in uiState.babies) {
                    val schedules = try {
                        val r = apiService.getScheduleForBaby(baby.babyId)
                        if (r is ApiResult.Success) r.data else emptyList()
                    } catch (_: Exception) { emptyList() }

                    schedules.filter { it.scheduledDate == date }.forEach { s ->
                        items.add(
                            TeamScheduleItem(
                                scheduleId    = s.scheduleId,
                                babyId        = baby.babyId,
                                babyName      = baby.fullName,
                                ageInMonths   = baby.ageInMonths,
                                gender        = baby.gender,
                                vaccineName   = s.vaccineName,
                                vaccineNameAr = s.vaccineNameAr,
                                doseNumber    = s.doseNumber,
                                scheduledDate = s.scheduledDate,
                                status        = s.status,
                                benchNameEn   = s.benchNameEn
                            )
                        )
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
            detailBaby             = baby,
            detailSchedulesLoading = true,
            detailGrowthLoading    = true,
            detailSchedules        = emptyList(),
            detailGrowthRecords    = emptyList()
        )
        scope.launch {
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
            launch {
                try {
                    val r = apiService.getGrowthRecords(baby.babyId)
                    uiState = if (r is ApiResult.Success)
                        uiState.copy(
                            detailGrowthRecords = r.data.sortedByDescending { it.measurementDate },
                            detailGrowthLoading = false
                        )
                    else
                        uiState.copy(detailGrowthLoading = false)
                } catch (e: Exception) {
                    uiState = uiState.copy(detailGrowthLoading = false, errorMessage = e.message)
                }
            }
        }
    }

    fun setDetailVacFilter(f: VaccinationFilter) { uiState = uiState.copy(detailVacFilter = f) }

    // ── Complete vaccination ───────────────────────────────────────────────────

    fun openCompleteDialog(scheduleId: String) {
        val now   = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val today = "${now.year}-${now.month.number.toString().padStart(2,'0')}-${now.day.toString().padStart(2,'0')}"
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
        val babyId = uiState.detailBaby?.babyId ?: return
        uiState    = uiState.copy(completeForm = form.copy(isLoading = true))
        scope.launch {
            try {
                val result = apiService.updateVaccinationScheduleStatus(
                    scheduleId = form.scheduleId,
                    status     = "COMPLETED"
                )
                if (result is ApiResult.Success) {
                    uiState = uiState.copy(completeForm = null, successMessage = "Vaccination marked as completed ✅")
                    val r = apiService.getScheduleForBaby(babyId)
                    if (r is ApiResult.Success) uiState = uiState.copy(detailSchedules = r.data)
                    if (uiState.benchId.isNotBlank()) loadScheduleForDate(uiState.benchId, uiState.selectedDate)
                } else {
                    uiState = uiState.copy(completeForm = form.copy(isLoading = false), errorMessage = "Failed to update")
                }
            } catch (e: Exception) {
                uiState = uiState.copy(completeForm = form.copy(isLoading = false), errorMessage = e.message)
            }
        }
    }

    fun markAsMissed(scheduleId: String) {
        val babyId = uiState.detailBaby?.babyId ?: return
        scope.launch {
            try {
                val result = apiService.updateVaccinationScheduleStatus(scheduleId = scheduleId, status = "MISSED")
                if (result is ApiResult.Success) {
                    uiState = uiState.copy(successMessage = "Marked as missed")
                    val r = apiService.getScheduleForBaby(babyId)
                    if (r is ApiResult.Success) uiState = uiState.copy(detailSchedules = r.data)
                } else {
                    uiState = uiState.copy(errorMessage = "Failed to update status")
                }
            } catch (e: Exception) {
                uiState = uiState.copy(errorMessage = e.message)
            }
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
                    val r = apiService.getGrowthRecords(babyId)
                    if (r is ApiResult.Success) {
                        uiState = uiState.copy(detailGrowthRecords = r.data.sortedByDescending { it.measurementDate })
                    }
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