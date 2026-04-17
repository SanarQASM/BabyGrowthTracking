// File: composeApp/src/commonMain/kotlin/org/example/project/babygrowthtrackingapplication/admin/AdminHomeScreen.kt

package org.example.project.babygrowthtrackingapplication.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import babygrowthtrackingapplication.composeapp.generated.resources.Res
import babygrowthtrackingapplication.composeapp.generated.resources.*
import org.example.project.babygrowthtrackingapplication.data.network.ApiService
import org.example.project.babygrowthtrackingapplication.theme.LocalDimensions
import org.example.project.babygrowthtrackingapplication.theme.customColors
import org.jetbrains.compose.resources.stringResource

// ─────────────────────────────────────────────────────────────────────────────
// Sub-screen state for the Team tab
// ─────────────────────────────────────────────────────────────────────────────

private sealed interface TeamSubScreen {
    data object List   : TeamSubScreen
    data object Create : TeamSubScreen
}

// ─────────────────────────────────────────────────────────────────────────────
// Tab descriptor — maps AdminTab → label + icon (resolved at runtime)
// ─────────────────────────────────────────────────────────────────────────────

private data class AdminNavItem(
    val tab          : AdminTab,
    val labelRes     : @Composable () -> String,
    val icon         : ImageVector,
    val selectedIcon : ImageVector = icon,
)

@Composable
private fun adminNavItems(): List<AdminNavItem> = listOf(
    AdminNavItem(AdminTab.DASHBOARD,    { stringResource(Res.string.admin_tab_dashboard)    }, Icons.Default.Dashboard),
    AdminNavItem(AdminTab.USERS,        { stringResource(Res.string.admin_tab_users)        }, Icons.Default.Group),
    AdminNavItem(AdminTab.BABIES,       { stringResource(Res.string.admin_tab_babies)       }, Icons.Default.ChildCare),
    AdminNavItem(AdminTab.VACCINATIONS, { stringResource(Res.string.admin_tab_vaccinations) }, Icons.Default.Vaccines),
    AdminNavItem(AdminTab.TEAM,         { stringResource(Res.string.admin_tab_team)         }, Icons.Default.MedicalServices),
    AdminNavItem(AdminTab.SETTINGS,     { stringResource(Res.string.admin_tab_settings)     }, Icons.Default.Settings),
)

// ─────────────────────────────────────────────────────────────────────────────
// AdminHomeScreen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun AdminHomeScreen(
    viewModel         : AdminViewModel,
    apiService        : ApiService,          // ← needed by AdminCreateTeamMemberScreen
    onNavigateToLogin : () -> Unit,
    isWideLayout      : Boolean = false,
) {
    val state      = viewModel.uiState
    val dimensions = LocalDimensions.current
    val navItems   = adminNavItems()
    val customColors = MaterialTheme.customColors

    // ── Sub-screen state for the Team tab ──────────────────────────────────
    var teamSubScreen by remember { mutableStateOf<TeamSubScreen>(TeamSubScreen.List) }

    // ── Navigate to welcome on logout ──────────────────────────────────────
    LaunchedEffect(state.navigateToWelcome) {
        if (state.navigateToWelcome) onNavigateToLogin()
    }

    // ── Snackbar host ──────────────────────────────────────────────────────
    val snackbarHostState = remember { SnackbarHostState() }

    val successLabel = when (state.successMessageKey) {
        "MSG_USER_DELETED"   -> stringResource(Res.string.admin_user_deleted_success)
        "MSG_BABY_DELETED"   -> stringResource(Res.string.admin_baby_deleted_success)
        "MSG_MEMBER_DELETED" -> stringResource(Res.string.admin_team_deleted_success)
        else                 -> state.successMessageKey
    }
    val errorLabel = when (state.errorMessageKey) {
        null          -> null
        "ERR_GENERIC" -> stringResource(Res.string.admin_error_generic)
        else          -> state.errorMessageKey
    }

    LaunchedEffect(state.successMessageKey) {
        successLabel?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }
    LaunchedEffect(state.errorMessageKey) {
        errorLabel?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    // ── Active tab state ───────────────────────────────────────────────────
    var activeTab by remember { mutableStateOf(AdminTab.DASHBOARD) }

    // Reset team sub-screen when leaving the Team tab
    LaunchedEffect(activeTab) {
        if (activeTab != AdminTab.TEAM) teamSubScreen = TeamSubScreen.List
    }

    if (isWideLayout) {
        // ── Side navigation rail layout ────────────────────────────────────
        Row(modifier = Modifier.fillMaxSize()) {
            NavigationRail(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor   = MaterialTheme.colorScheme.onSurface
            ) {
                Spacer(Modifier.height(dimensions.spacingLarge))
                navItems.forEach { item ->
                    val selected = activeTab == item.tab
                    NavigationRailItem(
                        selected = selected,
                        onClick  = { activeTab = item.tab },
                        icon     = {
                            Icon(
                                if (selected) item.selectedIcon else item.icon,
                                contentDescription = item.labelRes()
                            )
                        },
                        label  = { Text(item.labelRes(), style = MaterialTheme.typography.labelSmall) },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor   = customColors.accentGradientStart,
                            selectedTextColor   = customColors.accentGradientStart,
                            indicatorColor      = customColors.accentGradientStart.copy(alpha = 0.15f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        )
                    )
                }
            }
            Scaffold(
                modifier       = Modifier.fillMaxSize().weight(1f),
                snackbarHost   = { SnackbarHost(snackbarHostState) },
                containerColor = MaterialTheme.colorScheme.background
            ) { padding ->
                AdminTabContent(
                    activeTab     = activeTab,
                    viewModel     = viewModel,
                    apiService    = apiService,
                    teamSubScreen = teamSubScreen,
                    onTeamSubScreenChange = { teamSubScreen = it },
                    modifier      = Modifier.padding(padding)
                )
            }
        }
    } else {
        // ── Bottom navigation layout ───────────────────────────────────────
        Scaffold(
            modifier       = Modifier.fillMaxSize(),
            snackbarHost   = { SnackbarHost(snackbarHostState) },
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor   = MaterialTheme.colorScheme.onSurface
                ) {
                    navItems.forEach { item ->
                        val selected = activeTab == item.tab
                        NavigationBarItem(
                            selected = selected,
                            onClick  = { activeTab = item.tab },
                            icon     = {
                                Icon(
                                    if (selected) item.selectedIcon else item.icon,
                                    contentDescription = item.labelRes()
                                )
                            },
                            label  = { Text(item.labelRes(), style = MaterialTheme.typography.labelSmall) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor   = customColors.accentGradientStart,
                                selectedTextColor   = customColors.accentGradientStart,
                                indicatorColor      = customColors.accentGradientStart.copy(alpha = 0.15f),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                                unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                            )
                        )
                    }
                }
            }
        ) { padding ->
            AdminTabContent(
                activeTab            = activeTab,
                viewModel            = viewModel,
                apiService           = apiService,
                teamSubScreen        = teamSubScreen,
                onTeamSubScreenChange = { teamSubScreen = it },
                modifier             = Modifier.padding(padding)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// AdminTabContent — routes to the correct tab screen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AdminTabContent(
    activeTab            : AdminTab,
    viewModel            : AdminViewModel,
    apiService           : ApiService,
    teamSubScreen        : TeamSubScreen,
    onTeamSubScreenChange: (TeamSubScreen) -> Unit,
    modifier             : Modifier = Modifier,
) {
    when (activeTab) {
        AdminTab.DASHBOARD    -> AdminDashboardScreen(viewModel = viewModel, modifier = modifier)
        AdminTab.USERS        -> AdminUsersScreen(viewModel = viewModel, modifier = modifier)
        AdminTab.BABIES       -> AdminBabiesScreen(viewModel = viewModel, modifier = modifier)
        AdminTab.VACCINATIONS -> AdminVaccinationsScreen(viewModel = viewModel, modifier = modifier)
        AdminTab.SETTINGS     -> AdminSettingsScreen(viewModel = viewModel, modifier = modifier)

        AdminTab.TEAM -> when (teamSubScreen) {
            // ── Team member list ───────────────────────────────────────────
            TeamSubScreen.List -> AdminTeamMembersScreen(
                viewModel  = viewModel,
                onAddClick = { onTeamSubScreenChange(TeamSubScreen.Create) },
                modifier   = modifier,
            )
            // ── Create team member form ────────────────────────────────────
            TeamSubScreen.Create -> AdminCreateTeamMemberScreen(
                apiService  = apiService,
                onBackClick = { onTeamSubScreenChange(TeamSubScreen.List) },
                onCreated   = {
                    // Refresh the team list then pop back
                    viewModel.loadTeamMembers()
                    onTeamSubScreenChange(TeamSubScreen.List)
                },
                modifier    = modifier,
            )
        }
    }
}