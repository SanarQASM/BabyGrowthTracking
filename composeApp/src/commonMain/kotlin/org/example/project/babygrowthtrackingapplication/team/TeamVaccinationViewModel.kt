// composeApp/src/commonMain/kotlin/org/example/project/babygrowthtrackingapplication/team/TeamVaccinationViewModel.kt

package org.example.project.babygrowthtrackingapplication.team

import androidx.compose.runtime.*
import kotlinx.coroutines.*
import kotlinx.datetime.*
import kotlinx.datetime.LocalDate
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
    // Team member info
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
    val detailBaby           : TeamBabyItem? = null,
    val detailSchedules      : List<VaccinationScheduleUi> = emptyList(),
    val detailSchedulesLoading: Boolean = false,
    val detailGrowthRecords  : List<GrowthRecordResponse> = emptyList(),
    val detailGrowthLoading  : Boolean = false,
    val detailVacFilter      : VaccinationFilter = VaccinationFilter.ALL,

    // Complete vaccination dialog
    val completeForm        : CompleteVaccinationForm? = null,

    // Reschedule dialog
    val showRescheduleFor   : String? = null,  // scheduleId

    // Add measurement
    val showAddMeasurement  : Boolean = false,
    val measurementBabyId   : String = "",

    // Messages
    val errorMessage         : String? = null,
    val successMessage       : String? = null
) {
    val filteredBabies: List<TeamBabyItem>
        get() = babies.filter { baby ->
            (searchQuery.isBlank() || baby.fullName.contains(searchQuery, ignoreCase = true)) &&
                    (!showActiveOnly || baby.vaccineStatus != TeamVaccineStatus.NO_SCHEDULE)
        }
}

// ─────────────────────────────────────────────────────────────────────────────
// ViewModel
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalTime::class)
class TeamVaccinationViewModel(
    private val apiService         : ApiService,
    private val preferencesManager : PreferencesManager
) {
    var uiState by mutableStateOf(TeamVaccinationUiState())
        private set

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    init {
        val userId = preferencesManager.getUserId() ?: ""
        val name   = preferencesManager.getUserName() ?: ""
        uiState    = uiState.copy(teamMemberId = userId, teamMemberName = name)
        initTodayDate()
        loadBenchAndBabies()
    }

    private fun initTodayDate() {
        val now = kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val today = "${now.year}-${now.month.number.toString().padStart(2, '0')}-${now.day.toString().padStart(2, '0')}"
        uiState = uiState.copy(selectedDate = today)
    }

    // ── Load bench + babies ───────────────────────────────────────────────────

    private fun loadBenchAndBabies() {
        scope.launch {
            uiState = uiState.copy(babiesLoading = true)
            try {
                // Attempt to get benches to find this team member's bench
                val benchResult = apiService.getAllBenches()
                if (benchResult is ApiResult.Success && benchResult.data.isNotEmpty()) {
                    // Take the first bench or ideally filter by assignment
                    val bench = benchResult.data.first()
                    uiState = uiState.copy(benchId = bench.benchId, benchName = bench.nameEn)
                    loadBabiesForBench(bench.benchId)
                    loadScheduleForDate(bench.benchId, uiState.selectedDate)
                } else {
                    uiState = uiState.copy(babiesLoading = false)
                }
            } catch (e: Exception) {
                uiState = uiState.copy(babiesLoading = false, errorMessage = e.message)
            }
        }
    }

    private suspend fun loadBabiesForBench(benchId: String) {
        try {
            val assignResult = apiService.getAssignmentsByBench(benchId)
            if (assignResult is ApiResult.Success) {
                val assignments = assignResult.data
                val babies = mutableListOf<TeamBabyItem>()

                for (assignment in assignments) {
                    val babyResult = apiService.getBaby(assignment.babyId)
                    if (babyResult is ApiResult.Success) {
                        val baby = babyResult.data
                        // Load schedule to determine status
                        val schedules = try {
                            val r = apiService.getScheduleForBaby(baby.babyId)
                            if (r is ApiResult.Success) r.data else emptyList()
                        } catch (_: Exception) { emptyList() }

                        val status = determineVaccineStatus(schedules)
                        val nextDate = schedules
                            .filter { it.status == "UPCOMING" || it.status == "DUE_SOON" }
                            .minByOrNull { it.scheduledDate }
                            ?.scheduledDate

                        babies.add(
                            TeamBabyItem(
                                babyId       = baby.babyId,
                                fullName     = baby.fullName,
                                dateOfBirth  = baby.dateOfBirth,
                                ageInMonths  = baby.ageInMonths,
                                gender       = baby.gender,
                                parentName   = baby.parentName,
                                parentPhone  = "",
                                nextVacDate  = nextDate,
                                vaccineStatus= status,
                                benchName    = assignment.benchNameEn
                            )
                        )
                    }
                }
                uiState = uiState.copy(babies = babies, babiesLoading = false)
            } else {
                uiState = uiState.copy(babiesLoading = false)
            }
        } catch (e: Exception) {
            uiState = uiState.copy(babiesLoading = false, errorMessage = e.message)
        }
    }

    private fun determineVaccineStatus(schedules: List<VaccinationScheduleUi>): TeamVaccineStatus {
        if (schedules.isEmpty()) return TeamVaccineStatus.NO_SCHEDULE
        val hasOverdue  = schedules.any { it.status == "OVERDUE" }
        val hasDueSoon  = schedules.any { it.status == "DUE_SOON" }
        return when {
            hasOverdue -> TeamVaccineStatus.OVERDUE
            hasDueSoon -> TeamVaccineStatus.DUE_SOON
            else       -> TeamVaccineStatus.UP_TO_DATE
        }
    }

    // ── Schedule tab ──────────────────────────────────────────────────────────

    private fun loadScheduleForDate(benchId: String, date: String) {
        scope.launch {
            uiState = uiState.copy(scheduleLoading = true)
            try {
                // Get all babies and filter their schedules for the selected date
                val items = mutableListOf<TeamScheduleItem>()
                for (baby in uiState.babies) {
                    val schedules = try {
                        val r = apiService.getScheduleForBaby(baby.babyId)
                        if (r is ApiResult.Success) r.data else emptyList()
                    } catch (_: Exception) { emptyList() }

                    schedules
                        .filter { it.scheduledDate == date }
                        .forEach { s ->
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
        if (uiState.benchId.isNotBlank()) {
            loadScheduleForDate(uiState.benchId, date)
        }
    }

    // ── Search & filter ───────────────────────────────────────────────────────

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
            // Load schedules
            launch {
                try {
                    val r = apiService.getScheduleForBaby(baby.babyId)
                    if (r is ApiResult.Success) {
                        uiState = uiState.copy(detailSchedules = r.data, detailSchedulesLoading = false)
                    } else {
                        uiState = uiState.copy(detailSchedulesLoading = false)
                    }
                } catch (e: Exception) {
                    uiState = uiState.copy(detailSchedulesLoading = false, errorMessage = e.message)
                }
            }
            // Load growth records
            launch {
                try {
                    val r = apiService.getGrowthRecords(baby.babyId)
                    if (r is ApiResult.Success) {
                        uiState = uiState.copy(
                            detailGrowthRecords = r.data.sortedByDescending { it.measurementDate },
                            detailGrowthLoading = false
                        )
                    } else {
                        uiState = uiState.copy(detailGrowthLoading = false)
                    }
                } catch (e: Exception) {
                    uiState = uiState.copy(detailGrowthLoading = false, errorMessage = e.message)
                }
            }
        }
    }

    fun setDetailVacFilter(f: VaccinationFilter) { uiState = uiState.copy(detailVacFilter = f) }

    // ── Complete vaccination ───────────────────────────────────────────────────

    fun openCompleteDialog(scheduleId: String) {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val today = "${now.year}-${now.month.number.toString().padStart(2, '0')}-${now.day.toString().padStart(2, '0')}"
        uiState = uiState.copy(
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
                    // Reload detail schedules
                    val r = apiService.getScheduleForBaby(babyId)
                    if (r is ApiResult.Success) {
                        uiState = uiState.copy(detailSchedules = r.data)
                    }
                    // Also refresh bench schedule
                    if (uiState.benchId.isNotBlank()) {
                        loadScheduleForDate(uiState.benchId, uiState.selectedDate)
                    }
                } else {
                    uiState = uiState.copy(
                        completeForm = form.copy(isLoading = false),
                        errorMessage = "Failed to update"
                    )
                }
            } catch (e: Exception) {
                uiState = uiState.copy(
                    completeForm = form.copy(isLoading = false),
                    errorMessage = e.message
                )
            }
        }
    }

    fun markAsMissed(scheduleId: String) {
        val babyId = uiState.detailBaby?.babyId ?: return
        scope.launch {
            try {
                val result = apiService.updateVaccinationScheduleStatus(
                    scheduleId = scheduleId,
                    status     = "MISSED"
                )
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

    fun addMeasurement(
        babyId        : String,
        weight        : Double?,
        height        : Double?,
        headCirc      : Double?,
        date          : String
    ) {
        val userId = uiState.teamMemberId
        scope.launch {
            try {
                val request = CreateGrowthRecordRequest(
                    babyId              = babyId,
                    measurementDate     = date,
                    weight              = weight,
                    height              = height,
                    headCircumference   = headCirc
                )
                val result = apiService.createGrowthRecord(userId, request)
                if (result is ApiResult.Success) {
                    uiState = uiState.copy(showAddMeasurement = false, successMessage = "Measurement saved 📏")
                    // Reload growth records
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

    fun clearError()   { uiState = uiState.copy(errorMessage = null) }
    fun clearSuccess() { uiState = uiState.copy(successMessage = null) }
    fun onDestroy()    { scope.cancel() }
}