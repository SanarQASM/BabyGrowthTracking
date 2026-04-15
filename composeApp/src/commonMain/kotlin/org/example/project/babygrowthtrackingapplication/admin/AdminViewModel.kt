// File: composeApp/src/commonMain/kotlin/org/example/project/babygrowthtrackingapplication/admin/AdminViewModel.kt

package org.example.project.babygrowthtrackingapplication.admin

import androidx.compose.runtime.*
import kotlinx.coroutines.*
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.data.PreferencesManager
import org.example.project.babygrowthtrackingapplication.data.network.ApiService
import org.example.project.babygrowthtrackingapplication.data.network.ApiResult
import org.example.project.babygrowthtrackingapplication.data.network.BabyResponse
import org.example.project.babygrowthtrackingapplication.data.network.UserResponse

// ─────────────────────────────────────────────────────────────────────────────
// Admin UI State
// ─────────────────────────────────────────────────────────────────────────────

data class AdminDashboardStats(
    val totalUsers       : Int = 0,
    val totalBabies      : Int = 0,
    val activeBabies     : Int = 0,
    val archivedBabies   : Int = 0,
    val totalParents     : Int = 0,
    val verifiedUsers    : Int = 0,
)

data class AdminUiState(
    val adminName         : String             = "",
    val adminEmail        : String             = "",
    val isLoading         : Boolean            = true,
    val isRefreshing      : Boolean            = false,
    val stats             : AdminDashboardStats = AdminDashboardStats(),
    val allUsers          : List<UserResponse>  = emptyList(),
    val allBabies         : List<BabyResponse>  = emptyList(),
    val filteredUsers     : List<UserResponse>  = emptyList(),
    val filteredBabies    : List<BabyResponse>  = emptyList(),
    val userSearchQuery   : String             = "",
    val babySearchQuery   : String             = "",
    val selectedUserTab   : AdminUserTab       = AdminUserTab.ALL,
    val selectedBabyTab   : AdminBabyTab       = AdminBabyTab.ALL,
    val errorMessage      : String?            = null,
    val successMessage    : String?            = null,
    val navigateToWelcome : Boolean            = false,
)

enum class AdminUserTab(val label: String) { ALL("All"), PARENTS("Parents"), ADMINS("Admins") }
enum class AdminBabyTab(val label: String) { ALL("All"), ACTIVE("Active"), ARCHIVED("Archived") }

// ─────────────────────────────────────────────────────────────────────────────
// Admin ViewModel
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

    // ── Profile ───────────────────────────────────────────────────────────────

    private fun loadAdminProfile() {
        val name  = preferencesManager.getUserName()  ?: "Admin"
        val email = preferencesManager.getUserEmail() ?: ""
        uiState = uiState.copy(adminName = name, adminEmail = email)
    }

    // ── Dashboard data ────────────────────────────────────────────────────────

    fun loadDashboardData(forceRefresh: Boolean = false) {
        scope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            try {
                val usersDeferred  = async { apiService.getAllUsers(page = 0, size = 200) }
                val usersResult = usersDeferred.await()

                if (usersResult is ApiResult.Success) {
                    val users = usersResult.data.content
                    val parents = users.filter { it.role.equals("parent", ignoreCase = true) }
                    val admins  = users.filter { it.role.equals("admin", ignoreCase = true) }
                    val verified = users.filter { it.isActive }

                    // Collect all babies across parents
                    val allBabies = mutableListOf<BabyResponse>()
                    parents.take(50).forEach { parent ->
                        val babiesResult = apiService.getBabiesByParent(parent.userId)
                        if (babiesResult is ApiResult.Success) {
                            allBabies.addAll(babiesResult.data)
                        }
                    }

                    val stats = AdminDashboardStats(
                        totalUsers     = users.size,
                        totalParents   = parents.size,
                        totalBabies    = allBabies.size,
                        activeBabies   = allBabies.count { it.isActive },
                        archivedBabies = allBabies.count { !it.isActive },
                        verifiedUsers  = verified.size
                    )

                    uiState = uiState.copy(
                        isLoading   = false,
                        isRefreshing = false,
                        stats       = stats,
                        allUsers    = users,
                        allBabies   = allBabies,
                        filteredUsers  = applyUserFilter(users, uiState.selectedUserTab, uiState.userSearchQuery),
                        filteredBabies = applyBabyFilter(allBabies, uiState.selectedBabyTab, uiState.babySearchQuery),
                    )
                } else if (usersResult is ApiResult.Error) {
                    uiState = uiState.copy(isLoading = false, isRefreshing = false, errorMessage = usersResult.message)
                }
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false, isRefreshing = false, errorMessage = e.message)
            }
        }
    }

    fun refresh() {
        uiState = uiState.copy(isRefreshing = true)
        loadDashboardData(forceRefresh = true)
    }

    // ── User management ───────────────────────────────────────────────────────

    fun setUserSearchQuery(query: String) {
        uiState = uiState.copy(
            userSearchQuery = query,
            filteredUsers   = applyUserFilter(uiState.allUsers, uiState.selectedUserTab, query)
        )
    }

    fun setUserTab(tab: AdminUserTab) {
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
                        allUsers       = updated,
                        filteredUsers  = applyUserFilter(updated, uiState.selectedUserTab, uiState.userSearchQuery),
                        successMessage = "User deleted successfully"
                    )
                    recalcStats(updated, uiState.allBabies)
                }
                is ApiResult.Error -> uiState = uiState.copy(errorMessage = result.message)
                else -> {}
            }
        }
    }

    // ── Baby management ───────────────────────────────────────────────────────

    fun setBabySearchQuery(query: String) {
        uiState = uiState.copy(
            babySearchQuery = query,
            filteredBabies  = applyBabyFilter(uiState.allBabies, uiState.selectedBabyTab, query)
        )
    }

    fun setBabyTab(tab: AdminBabyTab) {
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
                        allBabies      = updated,
                        filteredBabies = applyBabyFilter(updated, uiState.selectedBabyTab, uiState.babySearchQuery),
                        successMessage = "Baby deleted successfully"
                    )
                    recalcStats(uiState.allUsers, updated)
                }
                is ApiResult.Error -> uiState = uiState.copy(errorMessage = result.message)
                else -> {}
            }
        }
    }

    // ── Logout ────────────────────────────────────────────────────────────────

    fun logout() {
        preferencesManager.logout()
        uiState = uiState.copy(navigateToWelcome = true)
    }

    fun clearMessages() {
        uiState = uiState.copy(successMessage = null, errorMessage = null)
    }

    fun onDestroy() { scope.cancel() }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun recalcStats(users: List<UserResponse>, babies: List<BabyResponse>) {
        val stats = AdminDashboardStats(
            totalUsers     = users.size,
            totalParents   = users.count { it.role.equals("parent", ignoreCase = true) },
            totalBabies    = babies.size,
            activeBabies   = babies.count { it.isActive },
            archivedBabies = babies.count { !it.isActive },
            verifiedUsers  = users.count { it.isActive }
        )
        uiState = uiState.copy(stats = stats)
    }

    private fun applyUserFilter(users: List<UserResponse>, tab: AdminUserTab, query: String): List<UserResponse> {
        val byTab = when (tab) {
            AdminUserTab.ALL     -> users
            AdminUserTab.PARENTS -> users.filter { it.role.equals("parent", ignoreCase = true) }
            AdminUserTab.ADMINS  -> users.filter { it.role.equals("admin", ignoreCase = true) }
        }
        return if (query.isBlank()) byTab
        else byTab.filter {
            it.fullName.contains(query, ignoreCase = true) ||
                    it.email.contains(query, ignoreCase = true)
        }
    }

    private fun applyBabyFilter(babies: List<BabyResponse>, tab: AdminBabyTab, query: String): List<BabyResponse> {
        val byTab = when (tab) {
            AdminBabyTab.ALL      -> babies
            AdminBabyTab.ACTIVE   -> babies.filter { it.isActive }
            AdminBabyTab.ARCHIVED -> babies.filter { !it.isActive }
        }
        return if (query.isBlank()) byTab
        else byTab.filter { it.fullName.contains(query, ignoreCase = true) }
    }
}