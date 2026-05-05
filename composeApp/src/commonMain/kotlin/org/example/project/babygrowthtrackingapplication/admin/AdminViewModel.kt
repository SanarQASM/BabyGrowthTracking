package org.example.project.babygrowthtrackingapplication.admin

import androidx.compose.runtime.*
import kotlinx.coroutines.*
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.data.PreferencesManager
import org.example.project.babygrowthtrackingapplication.data.network.ApiResult
import org.example.project.babygrowthtrackingapplication.data.network.ApiService
import org.example.project.babygrowthtrackingapplication.data.network.BabyResponse
import org.example.project.babygrowthtrackingapplication.data.network.UserResponse

enum class AdminTab {
    DASHBOARD, USERS, BABIES, VACCINATIONS, TEAM, BENCHES, SETTINGS
}
enum class AdminUserFilterTab  { ALL, PARENTS, ADMINS }
enum class AdminBabyFilterTab  { ALL, ACTIVE, ARCHIVED }
enum class AdminVaxFilterTab   { ALL, OVERDUE, UPCOMING, COMPLETED }
enum class AdminTeamFilterTab  { ALL, ACTIVE, INACTIVE }

data class AdminDashboardStats(
    val totalUsers          : Int = 0,
    val totalBabies         : Int = 0,
    val activeBabies        : Int = 0,
    val archivedBabies      : Int = 0,
    val totalParents        : Int = 0,
    val totalAdmins         : Int = 0,
    val totalTeamMembers    : Int = 0,
    val verifiedUsers       : Int = 0,
    val overdueVaccinations : Int = 0,
    val todayAppointments   : Int = 0,
)

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

data class AdminUiState(
    val adminName         : String                  = "",
    val adminEmail        : String                  = "",
    val isLoading         : Boolean                 = true,
    val isRefreshing      : Boolean                 = false,
    val stats             : AdminDashboardStats      = AdminDashboardStats(),
    val allUsers          : List<UserResponse>       = emptyList(),
    val filteredUsers     : List<UserResponse>       = emptyList(),
    val userSearchQuery   : String                   = "",
    val selectedUserTab   : AdminUserFilterTab       = AdminUserFilterTab.ALL,
    val allBabies         : List<BabyResponse>       = emptyList(),
    val filteredBabies    : List<BabyResponse>       = emptyList(),
    val babySearchQuery   : String                   = "",
    val selectedBabyTab   : AdminBabyFilterTab       = AdminBabyFilterTab.ALL,
    val allVaxRecords     : List<AdminVaxRecord>     = emptyList(),
    val filteredVaxRecords: List<AdminVaxRecord>     = emptyList(),
    val vaxSearchQuery    : String                   = "",
    val selectedVaxTab    : AdminVaxFilterTab        = AdminVaxFilterTab.ALL,
    val allTeamMembers     : List<UserResponse>      = emptyList(),
    val filteredTeamMembers: List<UserResponse>      = emptyList(),
    val teamSearchQuery    : String                  = "",
    val selectedTeamTab    : AdminTeamFilterTab      = AdminTeamFilterTab.ALL,
    val isTeamLoading      : Boolean                 = false,
    val successMessageKey : String?                  = null,
    val errorMessageKey   : String?                  = null,
    val navigateToWelcome : Boolean                  = false,
)

class AdminViewModel(
    private val apiService        : ApiService,
    private val preferencesManager: PreferencesManager,
) {
    var uiState by mutableStateOf(AdminUiState())
        private set

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    init {
        loadAdminProfile()
        loadDashboardData()
    }

    private fun loadAdminProfile() {
        uiState = uiState.copy(
            adminName  = preferencesManager.getUserName()  ?: "",
            adminEmail = preferencesManager.getUserEmail() ?: "",
        )
    }

    fun loadDashboardData(forceRefresh: Boolean = false) {
        scope.launch {
            uiState = uiState.copy(isLoading = true, errorMessageKey = null)
            try {
                val usersResult = apiService.getAllUsers(page = 0, size = 200)
                if (usersResult is ApiResult.Success) {
                    val users   = usersResult.data.content
                    val parents = users.filter { it.role.equals("parent",           ignoreCase = true) }
                    val admins  = users.filter { it.role.equals("admin",            ignoreCase = true) }
                    val team    = users.filter { it.role.equals("vaccination_team", ignoreCase = true) }

                    // Fetch babies for all parents in parallel
                    val allBabies = mutableListOf<BabyResponse>()
                    val babyJobs = parents.take(50).map { parent ->
                        async {
                            val r = apiService.getBabiesByParent(parent.userId)
                            if (r is ApiResult.Success) r.data else emptyList()
                        }
                    }
                    babyJobs.awaitAll().forEach { allBabies.addAll(it) }

                    // FIX: build vax records in parallel (was sequential O(n) calls)
                    val vaxRecords = buildVaxRecordsParallel(allBabies)

                    val stats = AdminDashboardStats(
                        totalUsers          = users.size,
                        totalParents        = parents.size,
                        totalAdmins         = admins.size,
                        totalTeamMembers    = team.size,
                        totalBabies         = allBabies.size,
                        activeBabies        = allBabies.count { it.isActive },
                        archivedBabies      = allBabies.count { !it.isActive },
                        verifiedUsers       = users.count { it.isActive },
                        overdueVaccinations = vaxRecords.count {
                            it.status.equals("OVERDUE", ignoreCase = true)
                        },
                    )

                    uiState = uiState.copy(
                        isLoading           = false,
                        isRefreshing        = false,
                        stats               = stats,
                        allUsers            = users,
                        allBabies           = allBabies,
                        allVaxRecords       = vaxRecords,
                        allTeamMembers      = team,
                        filteredUsers       = applyUserFilter(users,     uiState.selectedUserTab, uiState.userSearchQuery),
                        filteredBabies      = applyBabyFilter(allBabies, uiState.selectedBabyTab, uiState.babySearchQuery),
                        filteredVaxRecords  = applyVaxFilter(vaxRecords, uiState.selectedVaxTab,  uiState.vaxSearchQuery),
                        filteredTeamMembers = applyTeamFilter(team,      uiState.selectedTeamTab, uiState.teamSearchQuery),
                    )
                } else if (usersResult is ApiResult.Error) {
                    uiState = uiState.copy(
                        isLoading       = false,
                        isRefreshing    = false,
                        errorMessageKey = usersResult.message.ifBlank { "ERR_GENERIC" },
                    )
                }
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading       = false,
                    isRefreshing    = false,
                    errorMessageKey = e.message ?: "ERR_GENERIC",
                )
            }
        }
    }

    // FIX: was private suspend fun doing sequential calls — one per baby.
    // Now fires all schedule requests in parallel with async/awaitAll.
    // For 30 babies with 500ms each: was 15s total, now ~500ms.
    private suspend fun buildVaxRecordsParallel(
        babies: List<BabyResponse>
    ): List<AdminVaxRecord> = coroutineScope {

        val jobs = babies.take(30).map { baby ->
            async {
                val result = apiService.getScheduleForBaby(baby.babyId)
                if (result is ApiResult.Success) {
                    result.data.map { schedule ->
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
                    }
                } else emptyList()
            }
        }

        jobs.awaitAll().flatten()
    }

    fun refresh() {
        uiState = uiState.copy(isRefreshing = true)
        loadDashboardData(forceRefresh = true)
    }

    // ── Team member management ────────────────────────────────────────────

    fun setTeamSearchQuery(query: String) {
        uiState = uiState.copy(
            teamSearchQuery     = query,
            filteredTeamMembers = applyTeamFilter(uiState.allTeamMembers, uiState.selectedTeamTab, query),
        )
    }

    fun setTeamTab(tab: AdminTeamFilterTab) {
        uiState = uiState.copy(
            selectedTeamTab     = tab,
            filteredTeamMembers = applyTeamFilter(uiState.allTeamMembers, tab, uiState.teamSearchQuery),
        )
    }

    fun deleteTeamMember(userId: String) {
        scope.launch {
            when (val result = apiService.deleteUser(userId)) {
                is ApiResult.Success -> {
                    val updatedTeam  = uiState.allTeamMembers.filter { it.userId != userId }
                    val updatedUsers = uiState.allUsers.filter      { it.userId != userId }
                    uiState = uiState.copy(
                        allTeamMembers      = updatedTeam,
                        filteredTeamMembers = applyTeamFilter(updatedTeam, uiState.selectedTeamTab, uiState.teamSearchQuery),
                        allUsers            = updatedUsers,
                        filteredUsers       = applyUserFilter(updatedUsers, uiState.selectedUserTab, uiState.userSearchQuery),
                        successMessageKey   = "MSG_TEAM_DELETED",
                    )
                    recalcStats(updatedUsers, uiState.allBabies)
                }
                is ApiResult.Error -> uiState = uiState.copy(errorMessageKey = result.message)
                else -> {}
            }
        }
    }

    fun loadTeamMembers() {
        scope.launch {
            uiState = uiState.copy(isTeamLoading = true, errorMessageKey = null)
            try {
                val result = apiService.getAllUsers(page = 0, size = 200)
                if (result is ApiResult.Success) {
                    val users = result.data.content
                    val team  = users.filter { it.role.equals("vaccination_team", ignoreCase = true) }
                    uiState = uiState.copy(
                        isTeamLoading       = false,
                        allTeamMembers      = team,
                        filteredTeamMembers = applyTeamFilter(team, uiState.selectedTeamTab, uiState.teamSearchQuery),
                        allUsers            = users,
                        filteredUsers       = applyUserFilter(users, uiState.selectedUserTab, uiState.userSearchQuery),
                    )
                    recalcStats(users, uiState.allBabies)
                } else if (result is ApiResult.Error) {
                    uiState = uiState.copy(
                        isTeamLoading   = false,
                        errorMessageKey = result.message.ifBlank { "ERR_GENERIC" },
                    )
                }
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isTeamLoading   = false,
                    errorMessageKey = e.message ?: "ERR_GENERIC",
                )
            }
        }
    }

    // FIX: this method is now actually called from AdminHomeScreen.onCreated
    // via AdminCreateTeamMemberScreen returning the new UserResponse.
    // Previously onCreated: () -> Unit discarded the created user so this
    // optimistic update path was dead code. Now wired correctly.
    fun onTeamMemberCreated(newMember: UserResponse) {
        val updatedTeam  = uiState.allTeamMembers + newMember
        val updatedUsers = uiState.allUsers       + newMember
        uiState = uiState.copy(
            allTeamMembers      = updatedTeam,
            filteredTeamMembers = applyTeamFilter(updatedTeam, uiState.selectedTeamTab, uiState.teamSearchQuery),
            allUsers            = updatedUsers,
            filteredUsers       = applyUserFilter(updatedUsers, uiState.selectedUserTab, uiState.userSearchQuery),
            successMessageKey   = "MSG_TEAM_CREATED",
        )
        recalcStats(updatedUsers, uiState.allBabies)
    }

    // ── User management ───────────────────────────────────────────────────

    fun setUserSearchQuery(query: String) {
        uiState = uiState.copy(
            userSearchQuery = query,
            filteredUsers   = applyUserFilter(uiState.allUsers, uiState.selectedUserTab, query),
        )
    }

    fun setUserTab(tab: AdminUserFilterTab) {
        uiState = uiState.copy(
            selectedUserTab = tab,
            filteredUsers   = applyUserFilter(uiState.allUsers, tab, uiState.userSearchQuery),
        )
    }

    fun deleteUser(userId: String) {
        scope.launch {
            when (val result = apiService.deleteUser(userId)) {
                is ApiResult.Success -> {
                    val updatedUsers = uiState.allUsers.filter { it.userId != userId }
                    val updatedTeam  = uiState.allTeamMembers.filter { it.userId != userId }
                    uiState = uiState.copy(
                        allUsers            = updatedUsers,
                        filteredUsers       = applyUserFilter(updatedUsers, uiState.selectedUserTab, uiState.userSearchQuery),
                        allTeamMembers      = updatedTeam,
                        filteredTeamMembers = applyTeamFilter(updatedTeam, uiState.selectedTeamTab, uiState.teamSearchQuery),
                        successMessageKey   = "MSG_USER_DELETED",
                    )
                    recalcStats(updatedUsers, uiState.allBabies)
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
            filteredBabies  = applyBabyFilter(uiState.allBabies, uiState.selectedBabyTab, query),
        )
    }

    fun setBabyTab(tab: AdminBabyFilterTab) {
        uiState = uiState.copy(
            selectedBabyTab = tab,
            filteredBabies  = applyBabyFilter(uiState.allBabies, tab, uiState.babySearchQuery),
        )
    }

    fun deleteBaby(babyId: String) {
        scope.launch {
            when (val result = apiService.deleteBaby(babyId)) {
                is ApiResult.Success -> {
                    val updated = uiState.allBabies.filter { it.babyId != babyId }
                    uiState = uiState.copy(
                        allBabies         = updated,
                        filteredBabies    = applyBabyFilter(updated, uiState.selectedBabyTab, uiState.babySearchQuery),
                        successMessageKey = "MSG_BABY_DELETED",
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
            filteredVaxRecords = applyVaxFilter(uiState.allVaxRecords, uiState.selectedVaxTab, query),
        )
    }

    fun setVaxTab(tab: AdminVaxFilterTab) {
        uiState = uiState.copy(
            selectedVaxTab     = tab,
            filteredVaxRecords = applyVaxFilter(uiState.allVaxRecords, tab, uiState.vaxSearchQuery),
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
            totalParents        = users.count { it.role.equals("parent",           ignoreCase = true) },
            totalAdmins         = users.count { it.role.equals("admin",            ignoreCase = true) },
            totalTeamMembers    = users.count { it.role.equals("vaccination_team", ignoreCase = true) },
            totalBabies         = babies.size,
            activeBabies        = babies.count { it.isActive },
            archivedBabies      = babies.count { !it.isActive },
            verifiedUsers       = users.count { it.isActive },
            overdueVaccinations = uiState.allVaxRecords.count {
                it.status.equals("OVERDUE", ignoreCase = true)
            },
        )
        uiState = uiState.copy(stats = stats)
    }

    private fun applyUserFilter(users: List<UserResponse>, tab: AdminUserFilterTab, query: String): List<UserResponse> {
        val byTab = when (tab) {
            AdminUserFilterTab.ALL     -> users
            AdminUserFilterTab.PARENTS -> users.filter { it.role.equals("parent", ignoreCase = true) }
            AdminUserFilterTab.ADMINS  -> users.filter { it.role.equals("admin",  ignoreCase = true) }
        }
        return if (query.isBlank()) byTab
        else byTab.filter {
            it.fullName.contains(query, ignoreCase = true) ||
                    it.email.contains(query,    ignoreCase = true)
        }
    }

    private fun applyBabyFilter(babies: List<BabyResponse>, tab: AdminBabyFilterTab, query: String): List<BabyResponse> {
        val byTab = when (tab) {
            AdminBabyFilterTab.ALL      -> babies
            AdminBabyFilterTab.ACTIVE   -> babies.filter { it.isActive }
            AdminBabyFilterTab.ARCHIVED -> babies.filter { !it.isActive }
        }
        return if (query.isBlank()) byTab
        else byTab.filter { it.fullName.contains(query, ignoreCase = true) }
    }

    private fun applyVaxFilter(records: List<AdminVaxRecord>, tab: AdminVaxFilterTab, query: String): List<AdminVaxRecord> {
        val byTab = when (tab) {
            AdminVaxFilterTab.ALL       -> records
            AdminVaxFilterTab.OVERDUE   -> records.filter { it.status.equals("OVERDUE",   ignoreCase = true) }
            AdminVaxFilterTab.UPCOMING  -> records.filter { it.status.equals("UPCOMING",  ignoreCase = true) || it.status.equals("DUE_SOON", ignoreCase = true) }
            AdminVaxFilterTab.COMPLETED -> records.filter { it.status.equals("COMPLETED", ignoreCase = true) }
        }
        return if (query.isBlank()) byTab
        else byTab.filter {
            it.babyName.contains(query,    ignoreCase = true) ||
                    it.vaccineName.contains(query, ignoreCase = true) ||
                    it.benchNameEn.contains(query, ignoreCase = true)
        }
    }

    private fun applyTeamFilter(members: List<UserResponse>, tab: AdminTeamFilterTab, query: String): List<UserResponse> {
        val byTab = when (tab) {
            AdminTeamFilterTab.ALL      -> members
            AdminTeamFilterTab.ACTIVE   -> members.filter { it.isActive }
            AdminTeamFilterTab.INACTIVE -> members.filter { !it.isActive }
        }
        return if (query.isBlank()) byTab
        else byTab.filter {
            it.fullName.contains(query, ignoreCase = true) ||
                    it.email.contains(query,    ignoreCase = true)
        }
    }
}