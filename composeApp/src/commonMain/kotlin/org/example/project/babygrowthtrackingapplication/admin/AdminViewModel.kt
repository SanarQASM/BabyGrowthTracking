// File: composeApp/src/commonMain/kotlin/org/example/project/babygrowthtrackingapplication/admin/AdminViewModel.kt

package org.example.project.babygrowthtrackingapplication.admin

import androidx.compose.runtime.*
import kotlinx.coroutines.*
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.data.PreferencesManager
import org.example.project.babygrowthtrackingapplication.data.network.ApiResult
import org.example.project.babygrowthtrackingapplication.data.network.ApiService
import org.example.project.babygrowthtrackingapplication.data.network.BabyResponse
import org.example.project.babygrowthtrackingapplication.data.network.UserResponse

// ─────────────────────────────────────────────────────────────────────────────
// Enums (tab selections)
// ─────────────────────────────────────────────────────────────────────────────

enum class AdminTab   { DASHBOARD, USERS, BABIES, VACCINATIONS, SETTINGS }
enum class AdminUserFilterTab  { ALL, PARENTS, ADMINS }
enum class AdminBabyFilterTab  { ALL, ACTIVE, ARCHIVED }
enum class AdminVaxFilterTab   { ALL, OVERDUE, UPCOMING, COMPLETED }

// ─────────────────────────────────────────────────────────────────────────────
// Data classes
// ─────────────────────────────────────────────────────────────────────────────

data class AdminDashboardStats(
    val totalUsers           : Int  = 0,
    val totalBabies          : Int  = 0,
    val activeBabies         : Int  = 0,
    val archivedBabies       : Int  = 0,
    val totalParents         : Int  = 0,
    val totalAdmins          : Int  = 0,
    val verifiedUsers        : Int  = 0,
    val overdueVaccinations  : Int  = 0,
    val todayAppointments    : Int  = 0,
)

// Lightweight vaccination summary row shown in the admin list
data class AdminVaxRecord(
    val scheduleId    : String,
    val babyId        : String,
    val babyName      : String,
    val vaccineName   : String,
    val doseNumber    : Int,
    val scheduledDate : String,
    val status        : String,
    val benchNameEn   : String,
)

// ─────────────────────────────────────────────────────────────────────────────
// UI State
// ─────────────────────────────────────────────────────────────────────────────

data class AdminUiState(
    // ── Identity ──────────────────────────────────────────────────────────
    val adminName         : String                 = "",
    val adminEmail        : String                 = "",

    // ── Loading flags ──────────────────────────────────────────────────────
    val isLoading         : Boolean                = true,
    val isRefreshing      : Boolean                = false,

    // ── Dashboard ─────────────────────────────────────────────────────────
    val stats             : AdminDashboardStats     = AdminDashboardStats(),

    // ── Users ─────────────────────────────────────────────────────────────
    val allUsers          : List<UserResponse>      = emptyList(),
    val filteredUsers     : List<UserResponse>      = emptyList(),
    val userSearchQuery   : String                  = "",
    val selectedUserTab   : AdminUserFilterTab      = AdminUserFilterTab.ALL,

    // ── Babies ────────────────────────────────────────────────────────────
    val allBabies         : List<BabyResponse>      = emptyList(),
    val filteredBabies    : List<BabyResponse>      = emptyList(),
    val babySearchQuery   : String                  = "",
    val selectedBabyTab   : AdminBabyFilterTab      = AdminBabyFilterTab.ALL,

    // ── Vaccinations ──────────────────────────────────────────────────────
    val allVaxRecords     : List<AdminVaxRecord>    = emptyList(),
    val filteredVaxRecords: List<AdminVaxRecord>    = emptyList(),
    val vaxSearchQuery    : String                  = "",
    val selectedVaxTab    : AdminVaxFilterTab       = AdminVaxFilterTab.ALL,

    // ── Messages — sentinel keys resolved in the composable ───────────────
    // e.g. "MSG_USER_DELETED" → admin_user_deleted_success
    //      "MSG_BABY_DELETED" → admin_baby_deleted_success
    //      server text         → passed verbatim
    val successMessageKey : String?                 = null,
    val errorMessageKey   : String?                 = null,

    // ── Navigation ────────────────────────────────────────────────────────
    val navigateToWelcome : Boolean                 = false,
)

// ─────────────────────────────────────────────────────────────────────────────
// ViewModel
// ─────────────────────────────────────────────────────────────────────────────

class AdminViewModel(
    private val apiService        : ApiService,
    private val preferencesManager: PreferencesManager
) {
    var uiState by mutableStateOf(AdminUiState())
        private set

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    init {
        loadAdminProfile()
        loadDashboardData()
    }

    // ── Profile ───────────────────────────────────────────────────────────

    private fun loadAdminProfile() {
        uiState = uiState.copy(
            adminName  = preferencesManager.getUserName()  ?: "",
            adminEmail = preferencesManager.getUserEmail() ?: ""
        )
    }

    // ── Dashboard data ────────────────────────────────────────────────────

    fun loadDashboardData(forceRefresh: Boolean = false) {
        scope.launch {
            uiState = uiState.copy(isLoading = true, errorMessageKey = null)
            try {
                // Users
                val usersResult = apiService.getAllUsers(page = 0, size = 200)
                if (usersResult is ApiResult.Success) {
                    val users    = usersResult.data.content
                    val parents  = users.filter { it.role.equals("parent", ignoreCase = true) }
                    val admins   = users.filter { it.role.equals("admin",  ignoreCase = true) }
                    val verified = users.filter { it.isActive }

                    // Babies — fetch for first 50 parents to keep request count manageable
                    val allBabies = mutableListOf<BabyResponse>()
                    parents.take(50).forEach { parent ->
                        val babiesResult = apiService.getBabiesByParent(parent.userId)
                        if (babiesResult is ApiResult.Success) {
                            allBabies.addAll(babiesResult.data)
                        }
                    }

                    // Build vax records list for the vaccinations tab
                    val vaxRecords = buildVaxRecords(allBabies)

                    val stats = AdminDashboardStats(
                        totalUsers          = users.size,
                        totalParents        = parents.size,
                        totalAdmins         = admins.size,
                        totalBabies         = allBabies.size,
                        activeBabies        = allBabies.count { it.isActive },
                        archivedBabies      = allBabies.count { !it.isActive },
                        verifiedUsers       = verified.size,
                        overdueVaccinations = vaxRecords.count { it.status.equals("OVERDUE", true) },
                    )

                    uiState = uiState.copy(
                        isLoading          = false,
                        isRefreshing       = false,
                        stats              = stats,
                        allUsers           = users,
                        allBabies          = allBabies,
                        allVaxRecords      = vaxRecords,
                        filteredUsers      = applyUserFilter(users, uiState.selectedUserTab, uiState.userSearchQuery),
                        filteredBabies     = applyBabyFilter(allBabies, uiState.selectedBabyTab, uiState.babySearchQuery),
                        filteredVaxRecords = applyVaxFilter(vaxRecords, uiState.selectedVaxTab, uiState.vaxSearchQuery),
                    )
                } else if (usersResult is ApiResult.Error) {
                    uiState = uiState.copy(
                        isLoading    = false,
                        isRefreshing = false,
                        errorMessageKey = usersResult.message.ifBlank { "ERR_GENERIC" }
                    )
                }
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading       = false,
                    isRefreshing    = false,
                    errorMessageKey = e.message ?: "ERR_GENERIC"
                )
            }
        }
    }

    private suspend fun buildVaxRecords(babies: List<BabyResponse>): List<AdminVaxRecord> {
        val records = mutableListOf<AdminVaxRecord>()
        babies.take(30).forEach { baby ->
            val result = apiService.getScheduleForBaby(baby.babyId)
            if (result is ApiResult.Success) {
                result.data.forEach { schedule ->
                    records.add(
                        AdminVaxRecord(
                            scheduleId    = schedule.scheduleId,
                            babyId        = baby.babyId,
                            babyName      = baby.fullName,
                            vaccineName   = schedule.vaccineName,
                            doseNumber    = schedule.doseNumber,
                            scheduledDate = schedule.scheduledDate,
                            status        = schedule.status,
                            benchNameEn   = schedule.benchNameEn,
                        )
                    )
                }
            }
        }
        return records
    }

    fun refresh() {
        uiState = uiState.copy(isRefreshing = true)
        loadDashboardData(forceRefresh = true)
    }

    // ── User management ───────────────────────────────────────────────────

    fun setUserSearchQuery(query: String) {
        uiState = uiState.copy(
            userSearchQuery = query,
            filteredUsers   = applyUserFilter(uiState.allUsers, uiState.selectedUserTab, query)
        )
    }

    fun setUserTab(tab: AdminUserFilterTab) {
        uiState = uiState.copy(
            selectedUserTab = tab,
            filteredUsers   = applyUserFilter(uiState.allUsers, tab, uiState.userSearchQuery)
        )
    }

    fun deleteUser(userId: String) {
        scope.launch {
            when (val result = apiService.deleteUser(userId)) {
                is ApiResult.Success -> {
                    val updated = uiState.allUsers.filter { it.userId != userId }
                    uiState = uiState.copy(
                        allUsers           = updated,
                        filteredUsers      = applyUserFilter(updated, uiState.selectedUserTab, uiState.userSearchQuery),
                        successMessageKey  = "MSG_USER_DELETED"
                    )
                    recalcStats(updated, uiState.allBabies)
                }
                is ApiResult.Error -> uiState = uiState.copy(errorMessageKey = result.message)
                else -> {}
            }
        }
    }

    // ── Baby management ───────────────────────────────────────────────────

    fun setBabySearchQuery(query: String) {
        uiState = uiState.copy(
            babySearchQuery = query,
            filteredBabies  = applyBabyFilter(uiState.allBabies, uiState.selectedBabyTab, query)
        )
    }

    fun setBabyTab(tab: AdminBabyFilterTab) {
        uiState = uiState.copy(
            selectedBabyTab = tab,
            filteredBabies  = applyBabyFilter(uiState.allBabies, tab, uiState.babySearchQuery)
        )
    }

    fun deleteBaby(babyId: String) {
        scope.launch {
            when (val result = apiService.deleteBaby(babyId)) {
                is ApiResult.Success -> {
                    val updated = uiState.allBabies.filter { it.babyId != babyId }
                    uiState = uiState.copy(
                        allBabies          = updated,
                        filteredBabies     = applyBabyFilter(updated, uiState.selectedBabyTab, uiState.babySearchQuery),
                        successMessageKey  = "MSG_BABY_DELETED"
                    )
                    recalcStats(uiState.allUsers, updated)
                }
                is ApiResult.Error -> uiState = uiState.copy(errorMessageKey = result.message)
                else -> {}
            }
        }
    }

    // ── Vaccination management ────────────────────────────────────────────

    fun setVaxSearchQuery(query: String) {
        uiState = uiState.copy(
            vaxSearchQuery     = query,
            filteredVaxRecords = applyVaxFilter(uiState.allVaxRecords, uiState.selectedVaxTab, query)
        )
    }

    fun setVaxTab(tab: AdminVaxFilterTab) {
        uiState = uiState.copy(
            selectedVaxTab     = tab,
            filteredVaxRecords = applyVaxFilter(uiState.allVaxRecords, tab, uiState.vaxSearchQuery)
        )
    }

    // ── Logout ────────────────────────────────────────────────────────────

    fun logout() {
        preferencesManager.logout()
        uiState = uiState.copy(navigateToWelcome = true)
    }

    fun clearMessages() {
        uiState = uiState.copy(successMessageKey = null, errorMessageKey = null)
    }

    fun onDestroy() { scope.cancel() }

    // ── Private helpers ───────────────────────────────────────────────────

    private fun recalcStats(users: List<UserResponse>, babies: List<BabyResponse>) {
        val stats = AdminDashboardStats(
            totalUsers          = users.size,
            totalParents        = users.count { it.role.equals("parent", ignoreCase = true) },
            totalAdmins         = users.count { it.role.equals("admin",  ignoreCase = true) },
            totalBabies         = babies.size,
            activeBabies        = babies.count { it.isActive },
            archivedBabies      = babies.count { !it.isActive },
            verifiedUsers       = users.count { it.isActive },
            overdueVaccinations = uiState.allVaxRecords.count { it.status.equals("OVERDUE", true) },
        )
        uiState = uiState.copy(stats = stats)
    }

    private fun applyUserFilter(
        users: List<UserResponse>,
        tab  : AdminUserFilterTab,
        query: String
    ): List<UserResponse> {
        val byTab = when (tab) {
            AdminUserFilterTab.ALL     -> users
            AdminUserFilterTab.PARENTS -> users.filter { it.role.equals("parent", ignoreCase = true) }
            AdminUserFilterTab.ADMINS  -> users.filter { it.role.equals("admin",  ignoreCase = true) }
        }
        return if (query.isBlank()) byTab
        else byTab.filter {
            it.fullName.contains(query, ignoreCase = true) ||
                    it.email.contains(query, ignoreCase = true)
        }
    }

    private fun applyBabyFilter(
        babies: List<BabyResponse>,
        tab   : AdminBabyFilterTab,
        query : String
    ): List<BabyResponse> {
        val byTab = when (tab) {
            AdminBabyFilterTab.ALL      -> babies
            AdminBabyFilterTab.ACTIVE   -> babies.filter { it.isActive }
            AdminBabyFilterTab.ARCHIVED -> babies.filter { !it.isActive }
        }
        return if (query.isBlank()) byTab
        else byTab.filter { it.fullName.contains(query, ignoreCase = true) }
    }

    private fun applyVaxFilter(
        records: List<AdminVaxRecord>,
        tab    : AdminVaxFilterTab,
        query  : String
    ): List<AdminVaxRecord> {
        val byTab = when (tab) {
            AdminVaxFilterTab.ALL       -> records
            AdminVaxFilterTab.OVERDUE   -> records.filter { it.status.equals("OVERDUE",   ignoreCase = true) }
            AdminVaxFilterTab.UPCOMING  -> records.filter {
                it.status.equals("UPCOMING", ignoreCase = true) ||
                        it.status.equals("DUE_SOON", ignoreCase = true)
            }
            AdminVaxFilterTab.COMPLETED -> records.filter { it.status.equals("COMPLETED", ignoreCase = true) }
        }
        return if (query.isBlank()) byTab
        else byTab.filter {
            it.babyName.contains(query,     ignoreCase = true) ||
                    it.vaccineName.contains(query,  ignoreCase = true) ||
                    it.benchNameEn.contains(query,  ignoreCase = true)
        }
    }
}