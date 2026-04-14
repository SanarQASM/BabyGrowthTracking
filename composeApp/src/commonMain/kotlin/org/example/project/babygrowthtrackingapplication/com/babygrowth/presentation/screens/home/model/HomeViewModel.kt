package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model

import androidx.compose.runtime.*
import kotlinx.coroutines.*
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.data.PreferencesManager
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.util.DataCache
import org.example.project.babygrowthtrackingapplication.data.network.*
import org.example.project.babygrowthtrackingapplication.theme.GenderTheme
import kotlin.collections.get
import kotlin.time.Duration.Companion.minutes

// ─────────────────────────────────────────────────────────────────────────────
// UI State — unchanged
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
    val actionMessage        : String?                                  = null
) {
    val selectedBaby: BabyResponse?
        get() = babies.getOrNull(selectedBabyIndex)

    val genderTheme: GenderTheme
        get() = when {
            selectedBaby == null -> GenderTheme.NEUTRAL
            selectedBaby!!.gender.equals("FEMALE", ignoreCase = true) ||
                    selectedBaby!!.gender.equals("GIRL", ignoreCase = true) -> GenderTheme.GIRL
            else -> GenderTheme.BOY
        }

    val selectedBabyVaccinations: List<VaccinationResponse>
        get() = upcomingVaccinations[selectedBaby?.babyId] ?: emptyList()

    val nextVaccination: VaccinationResponse?
        get() = selectedBabyVaccinations
            .filter { it.status.equals("PENDING", ignoreCase = true) }
            .firstOrNull()
}

// ─────────────────────────────────────────────────────────────────────────────
// ViewModel — FIX: DataCache now wired in for babies, vaccinations, and growth
// ─────────────────────────────────────────────────────────────────────────────

class HomeViewModel(
    private val apiService        : ApiService,
    private val preferencesManager: PreferencesManager
) {
    var uiState by mutableStateOf(HomeUiState())
        private set

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // ── Caches ────────────────────────────────────────────────────────────────
    // FIX: DataCache was declared in the project but never instantiated anywhere.
    // These caches prevent redundant API calls when the user switches tabs and
    // returns to Home within the TTL window (5 min for babies/growth, 2 min for
    // vaccinations since they are more time-sensitive).

    private val babiesCache = DataCache<List<BabyResponse>>(ttl = 5.minutes)

    // Per-baby caches keyed by babyId
    private val vaccinationCaches  = mutableMapOf<String, DataCache<List<VaccinationResponse>>>()
    private val latestGrowthCaches = mutableMapOf<String, DataCache<GrowthRecordResponse>>()
    private val allGrowthCaches    = mutableMapOf<String, DataCache<List<GrowthRecordResponse>>>()

    private fun vaccinationCache(babyId: String)  =
        vaccinationCaches.getOrPut(babyId)  { DataCache(ttl = 2.minutes) }
    private fun latestGrowthCache(babyId: String) =
        latestGrowthCaches.getOrPut(babyId) { DataCache(ttl = 5.minutes) }
    private fun allGrowthCache(babyId: String)    =
        allGrowthCaches.getOrPut(babyId)    { DataCache(ttl = 5.minutes) }

    // ─────────────────────────────────────────────────────────────────────────
    private val AUTO_ARCHIVE_MONTHS = 72

    init { loadHomeData() }

    // ─────────────────────────────────────────────────────────────────────────
    // Initial load — uses cache when fresh, hits network when stale
    // ─────────────────────────────────────────────────────────────────────────

    fun loadHomeData(forceRefresh: Boolean = false) {
        scope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)

            val userId   = preferencesManager.getUserId()   ?: ""
            val userName = preferencesManager.getUserName() ?: ""
            uiState = uiState.copy(userId = userId, userName = userName)

            // FIX: use cached baby list when available and not forcing refresh
            val cachedBabies = if (!forceRefresh) babiesCache.get() else null
            if (cachedBabies != null) {
                uiState = uiState.copy(babies = cachedBabies, isLoading = false)
                cachedBabies.forEach { baby ->
                    launch { loadVaccinationsForBaby(baby.babyId, forceRefresh) }
                    launch { loadLatestGrowthForBaby(baby.babyId, forceRefresh) }
                    launch { loadAllGrowthForBaby(baby.babyId, forceRefresh) }
                }
                return@launch
            }

            when (val result = apiService.getBabiesByParent(userId)) {
                is ApiResult.Success -> {
                    val babies = result.data
                    babiesCache.set(babies) // FIX: store in cache
                    uiState = uiState.copy(babies = babies, isLoading = false)

                    babies
                        .filter { it.isActive && it.ageInMonths >= AUTO_ARCHIVE_MONTHS }
                        .forEach { baby -> launch { autoArchiveBaby(baby.babyId) } }

                    babies.forEach { baby ->
                        launch { loadVaccinationsForBaby(baby.babyId, forceRefresh) }
                        launch { loadLatestGrowthForBaby(baby.babyId, forceRefresh) }
                        launch { loadAllGrowthForBaby(baby.babyId, forceRefresh) }
                    }
                }
                is ApiResult.Error ->
                    uiState = uiState.copy(isLoading = false, errorMessage = result.message)
                else ->
                    uiState = uiState.copy(isLoading = false)
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Auto-archive — silent, no snackbar
    // ─────────────────────────────────────────────────────────────────────────

    private suspend fun autoArchiveBaby(babyId: String) {
        when (val result = apiService.updateBabyStatus(babyId, "ARCHIVED")) {
            is ApiResult.Success -> {
                val updated = uiState.babies.map { b ->
                    if (b.babyId == babyId) result.data else b
                }
                // FIX: invalidate cache after mutation
                babiesCache.invalidate()
                val newIndex = if (uiState.selectedBaby?.babyId == babyId) 0
                else uiState.selectedBabyIndex
                uiState = uiState.copy(babies = updated, selectedBabyIndex = newIndex)
            }
            else -> Unit
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Per-baby loaders — FIX: each checks its own DataCache before hitting API
    // ─────────────────────────────────────────────────────────────────────────

    private suspend fun loadVaccinationsForBaby(babyId: String, forceRefresh: Boolean = false) {
        val cache  = vaccinationCache(babyId)
        val cached = if (!forceRefresh) cache.get() else null
        if (cached != null) {
            val map = uiState.upcomingVaccinations.toMutableMap()
            map[babyId] = cached
            uiState = uiState.copy(upcomingVaccinations = map)
            return
        }
        when (val v = apiService.getUpcomingVaccinations(babyId)) {
            is ApiResult.Success -> {
                cache.set(v.data) // FIX: populate cache
                val map = uiState.upcomingVaccinations.toMutableMap()
                map[babyId] = v.data
                uiState = uiState.copy(upcomingVaccinations = map)
            }
            else -> Unit
        }
    }

    private suspend fun loadLatestGrowthForBaby(babyId: String, forceRefresh: Boolean = false) {
        val cache  = latestGrowthCache(babyId)
        val cached = if (!forceRefresh) cache.get() else null
        if (cached != null) {
            val map = uiState.latestGrowthRecords.toMutableMap()
            map[babyId] = cached
            uiState = uiState.copy(latestGrowthRecords = map)
            return
        }
        when (val g = apiService.getLatestGrowthRecord(babyId)) {
            is ApiResult.Success -> {
                cache.set(g.data) // FIX: populate cache
                val map = uiState.latestGrowthRecords.toMutableMap()
                map[babyId] = g.data
                uiState = uiState.copy(latestGrowthRecords = map)
            }
            else -> Unit
        }
    }

    private suspend fun loadAllGrowthForBaby(babyId: String, forceRefresh: Boolean = false) {
        val cache  = allGrowthCache(babyId)
        val cached = if (!forceRefresh) cache.get() else null
        if (cached != null) {
            val map = uiState.allGrowthRecords.toMutableMap()
            map[babyId] = cached
            uiState = uiState.copy(allGrowthRecords = map)
            return
        }
        when (val g = apiService.getGrowthRecords(babyId)) {
            is ApiResult.Success -> {
                val sorted = g.data.sortedBy { it.measurementDate }
                cache.set(sorted) // FIX: populate cache
                val map = uiState.allGrowthRecords.toMutableMap()
                map[babyId] = sorted
                uiState = uiState.copy(allGrowthRecords = map)
            }
            else -> Unit
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Refresh helpers — force-bypass cache so UI always gets fresh data after
    // a user action (e.g. adding a measurement).
    // ─────────────────────────────────────────────────────────────────────────

    fun refreshGrowthForBaby(babyId: String) {
        // FIX: invalidate the affected caches before re-fetching
        latestGrowthCaches[babyId]?.invalidate()
        allGrowthCaches[babyId]?.invalidate()
        scope.launch {
            loadLatestGrowthForBaby(babyId, forceRefresh = true)
            loadAllGrowthForBaby(babyId, forceRefresh = true)
        }
    }

    fun deleteGrowthRecord(babyId: String, recordId: String) {
        scope.launch {
            when (apiService.deleteGrowthRecord(recordId)) {
                is ApiResult.Success -> {
                    refreshGrowthForBaby(babyId)
                    uiState = uiState.copy(actionMessage = "Measurement deleted")
                }
                is ApiResult.Error ->
                    uiState = uiState.copy(actionMessage = "Failed to delete measurement")
                else -> Unit
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Child selection
    // ─────────────────────────────────────────────────────────────────────────

    fun selectBaby(index: Int) {
        uiState = uiState.copy(selectedBabyIndex = index)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Archive / Unarchive — optimistic update pattern
    // FIX: babiesCache is invalidated after every status mutation so the next
    // loadHomeData() fetches a fresh list from the backend.
    // ─────────────────────────────────────────────────────────────────────────

    fun archiveBaby(babyId: String) {
        val originalBabies = uiState.babies
        val optimistic = originalBabies.map { b ->
            if (b.babyId == babyId) b.copy(isActive = false) else b
        }
        val newIndex = if (uiState.selectedBaby?.babyId == babyId) 0
        else uiState.selectedBabyIndex
        uiState = uiState.copy(
            babies            = optimistic,
            selectedBabyIndex = newIndex,
            actionMessage     = "Child archived successfully"
        )

        scope.launch {
            when (val result = apiService.updateBabyStatus(babyId, "ARCHIVED")) {
                is ApiResult.Success -> {
                    babiesCache.invalidate() // FIX
                    val confirmed = uiState.babies.map { b ->
                        if (b.babyId == babyId) result.data else b
                    }
                    uiState = uiState.copy(babies = confirmed)
                }
                is ApiResult.Error -> {
                    uiState = uiState.copy(
                        babies        = originalBabies,
                        actionMessage = "Failed to archive: ${result.message}"
                    )
                }
                else -> Unit
            }
        }
    }

    fun unarchiveBaby(babyId: String) {
        val originalBabies = uiState.babies
        val optimistic = originalBabies.map { b ->
            if (b.babyId == babyId) b.copy(isActive = true) else b
        }
        uiState = uiState.copy(
            babies        = optimistic,
            actionMessage = "Child restored successfully"
        )

        scope.launch {
            when (val result = apiService.updateBabyStatus(babyId, "ACTIVE")) {
                is ApiResult.Success -> {
                    babiesCache.invalidate() // FIX
                    val confirmed = uiState.babies.map { b ->
                        if (b.babyId == babyId) result.data else b
                    }
                    uiState = uiState.copy(babies = confirmed)
                }
                is ApiResult.Error -> {
                    uiState = uiState.copy(
                        babies        = originalBabies,
                        actionMessage = "Failed to restore: ${result.message}"
                    )
                }
                else -> Unit
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Snackbar helpers
    // ─────────────────────────────────────────────────────────────────────────

    fun clearActionMessage() {
        uiState = uiState.copy(actionMessage = null)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Cleanup
    // ─────────────────────────────────────────────────────────────────────────

    fun onDestroy() { scope.cancel() }
}