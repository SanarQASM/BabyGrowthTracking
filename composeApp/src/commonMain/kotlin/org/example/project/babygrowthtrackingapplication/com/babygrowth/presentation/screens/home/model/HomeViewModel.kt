package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model

import androidx.compose.runtime.*
import kotlinx.coroutines.*
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.data.PreferencesManager
import org.example.project.babygrowthtrackingapplication.data.network.*
import org.example.project.babygrowthtrackingapplication.theme.GenderTheme
import kotlin.collections.get

// ─────────────────────────────────────────────────────────────────────────────
// UI State
// ─────────────────────────────────────────────────────────────────────────────

data class HomeUiState(
    val isLoading            : Boolean                                  = true,
    val userName             : String                                   = "",
    val userId               : String                                   = "",
    val babies               : List<BabyResponse>                      = emptyList(),
    val selectedBabyIndex    : Int                                      = 0,
    val upcomingVaccinations : Map<String, List<VaccinationResponse>>   = emptyMap(),
    val latestGrowthRecords  : Map<String, GrowthRecordResponse>        = emptyMap(),
    val allGrowthRecords     : Map<String, List<GrowthRecordResponse>>  = emptyMap(),
    val notificationCount    : Int                                      = 0,
    val errorMessage         : String?                                  = null,
    val actionMessage        : String?                                  = null  // snackbar feedback
) {
    val selectedBaby: BabyResponse?
        get() = babies.getOrNull(selectedBabyIndex)

    val genderTheme: GenderTheme
        get() = when {
            selectedBaby == null -> GenderTheme.NEUTRAL
            selectedBaby!!.gender.equals("FEMALE", ignoreCase = true) ||
                    selectedBaby!!.gender.equals("GIRL",   ignoreCase = true) ->
                GenderTheme.GIRL
            else ->
                GenderTheme.BOY
        }

    val selectedBabyVaccinations: List<VaccinationResponse>
        get() = upcomingVaccinations[selectedBaby?.babyId] ?: emptyList()

    val nextVaccination: VaccinationResponse?
        get() = selectedBabyVaccinations
            .filter { it.status.equals("PENDING", ignoreCase = true) }
            .firstOrNull()
}

// ─────────────────────────────────────────────────────────────────────────────
// ViewModel
// ─────────────────────────────────────────────────────────────────────────────

class HomeViewModel(
    private val apiService        : ApiService,
    private val preferencesManager: PreferencesManager
) {
    var uiState by mutableStateOf(HomeUiState())
        private set

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    init { loadHomeData() }

    // ── Initial load ──────────────────────────────────────────────────────────

    fun loadHomeData() {
        scope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)

            val userId   = preferencesManager.getUserId()   ?: ""
            val userName = preferencesManager.getUserName() ?: ""
            uiState = uiState.copy(userId = userId, userName = userName)

            when (val result = apiService.getBabiesByParent(userId)) {
                is ApiResult.Success -> {
                    uiState = uiState.copy(babies = result.data, isLoading = false)
                    // Load vaccinations + growth records in parallel for all babies
                    result.data.forEach { baby ->
                        launch { loadVaccinationsForBaby(baby.babyId) }
                        launch { loadLatestGrowthForBaby(baby.babyId) }
                        launch { loadAllGrowthForBaby(baby.babyId) }
                    }
                }
                is ApiResult.Error ->
                    uiState = uiState.copy(isLoading = false, errorMessage = result.message)
                else ->
                    uiState = uiState.copy(isLoading = false)
            }
        }
    }

    // ── Per-baby loaders ──────────────────────────────────────────────────────

    private suspend fun loadVaccinationsForBaby(babyId: String) {
        when (val v = apiService.getUpcomingVaccinations(babyId)) {
            is ApiResult.Success -> {
                val map = uiState.upcomingVaccinations.toMutableMap()
                map[babyId] = v.data
                uiState = uiState.copy(upcomingVaccinations = map)
            }
            else -> Unit
        }
    }

    private suspend fun loadLatestGrowthForBaby(babyId: String) {
        when (val g = apiService.getLatestGrowthRecord(babyId)) {
            is ApiResult.Success -> {
                val map = uiState.latestGrowthRecords.toMutableMap()
                map[babyId] = g.data
                uiState = uiState.copy(latestGrowthRecords = map)
            }
            else -> Unit
        }
    }

    private suspend fun loadAllGrowthForBaby(babyId: String) {
        when (val g = apiService.getGrowthRecords(babyId)) {
            is ApiResult.Success -> {
                val map = uiState.allGrowthRecords.toMutableMap()
                map[babyId] = g.data.sortedBy { it.measurementDate }
                uiState = uiState.copy(allGrowthRecords = map)
            }
            else -> Unit
        }
    }

    fun refreshGrowthForBaby(babyId: String) {
        scope.launch {
            loadLatestGrowthForBaby(babyId)
            loadAllGrowthForBaby(babyId)
        }
    }

    fun deleteGrowthRecord(babyId: String, recordId: String) {
        scope.launch {
            when (apiService.deleteGrowthRecord(recordId)) {
                is ApiResult.Success -> {
                    refreshGrowthForBaby(babyId)
                    uiState = uiState.copy(actionMessage = "Measurement deleted")
                }
                is ApiResult.Error   ->
                    uiState = uiState.copy(actionMessage = "Failed to delete measurement")
                else -> Unit
            }
        }
    }

    // ── Child selection ───────────────────────────────────────────────────────

    fun selectBaby(index: Int) {
        uiState = uiState.copy(selectedBabyIndex = index)
    }

    // ── Archive / Unarchive ───────────────────────────────────────────────────

    fun archiveBaby(babyId: String) {
        scope.launch {
            when (val result = apiService.updateBabyStatus(babyId, "ARCHIVED")) {
                is ApiResult.Success -> {
                    // Update the local list without a full reload
                    val updated = uiState.babies.map { b ->
                        if (b.babyId == babyId) result.data else b
                    }
                    // If the selected baby was archived, reset selection
                    val newIndex = if (uiState.selectedBaby?.babyId == babyId) 0
                    else uiState.selectedBabyIndex
                    uiState = uiState.copy(
                        babies            = updated,
                        selectedBabyIndex = newIndex,
                        actionMessage     = "Child archived successfully"
                    )
                }
                is ApiResult.Error ->
                    uiState = uiState.copy(actionMessage = "Failed to archive: ${result.message}")
                else -> Unit
            }
        }
    }

    fun unarchiveBaby(babyId: String) {
        scope.launch {
            when (val result = apiService.updateBabyStatus(babyId, "ACTIVE")) {
                is ApiResult.Success -> {
                    val updated = uiState.babies.map { b ->
                        if (b.babyId == babyId) result.data else b
                    }
                    uiState = uiState.copy(
                        babies        = updated,
                        actionMessage = "Child restored successfully"
                    )
                }
                is ApiResult.Error ->
                    uiState = uiState.copy(actionMessage = "Failed to restore: ${result.message}")
                else -> Unit
            }
        }
    }

    /** Clear snackbar message after it has been shown */
    fun clearActionMessage() {
        uiState = uiState.copy(actionMessage = null)
    }

    // ── Cleanup ───────────────────────────────────────────────────────────────

    fun onDestroy() { scope.cancel() }
}