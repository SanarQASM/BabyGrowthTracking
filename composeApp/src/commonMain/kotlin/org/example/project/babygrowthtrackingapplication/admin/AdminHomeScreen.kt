// File: composeApp/src/commonMain/kotlin/org/example/project/babygrowthtrackingapplication/admin/AdminHomeScreen.kt
//
// Changes from original:
//  - AdminBenchesScreen now receives teamMembers list via apiService (no change needed here
//    since AdminBenchesScreen loads team members itself via apiService.getUsersByRole)
//  - AdminTabContent.BENCHES passes apiService correctly (was already correct)
//  - AdminTab.TEAM flow: after creating a team member the admin is prompted to
//    also assign them to a bench immediately (new TeamSubScreen.AssignBench state)

package org.example.project.babygrowthtrackingapplication.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import babygrowthtrackingapplication.composeapp.generated.resources.Res
import babygrowthtrackingapplication.composeapp.generated.resources.*
import org.example.project.babygrowthtrackingapplication.data.network.ApiResult
import org.example.project.babygrowthtrackingapplication.data.network.ApiService
import org.example.project.babygrowthtrackingapplication.data.network.UserResponse
import org.example.project.babygrowthtrackingapplication.theme.LocalDimensions
import org.example.project.babygrowthtrackingapplication.theme.customColors
import org.jetbrains.compose.resources.stringResource
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────────────────
// Team sub-screen states
// ─────────────────────────────────────────────────────────────────────────────

private sealed interface TeamSubScreen {
    data object List   : TeamSubScreen
    data object Create : TeamSubScreen
    // NEW: after creating a team member, offer to assign them to a bench
    data class AssignBench(val newMember: UserResponse) : TeamSubScreen
}

// ─────────────────────────────────────────────────────────────────────────────
// Nav items
// ─────────────────────────────────────────────────────────────────────────────

private data class AdminNavItem(
    val tab         : AdminTab,
    val labelRes    : @Composable () -> String,
    val icon        : ImageVector,
    val selectedIcon: ImageVector = icon,
)

@Composable
private fun adminNavItems(): List<AdminNavItem> = listOf(
    AdminNavItem(AdminTab.DASHBOARD,    { stringResource(Res.string.admin_tab_dashboard)    }, Icons.Default.Dashboard),
    AdminNavItem(AdminTab.USERS,        { stringResource(Res.string.admin_tab_users)        }, Icons.Default.Group),
    AdminNavItem(AdminTab.BABIES,       { stringResource(Res.string.admin_tab_babies)       }, Icons.Default.ChildCare),
    AdminNavItem(AdminTab.VACCINATIONS, { stringResource(Res.string.admin_tab_vaccinations) }, Icons.Default.Vaccines),
    AdminNavItem(AdminTab.TEAM,         { stringResource(Res.string.admin_tab_team)         }, Icons.Default.MedicalServices),
    AdminNavItem(AdminTab.BENCHES,      { stringResource(Res.string.admin_tab_benches)      }, Icons.Default.LocalHospital),
    AdminNavItem(AdminTab.SETTINGS,     { stringResource(Res.string.admin_tab_settings)     }, Icons.Default.Settings),
)

// ─────────────────────────────────────────────────────────────────────────────
// AdminHomeScreen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun AdminHomeScreen(
    viewModel        : AdminViewModel,
    apiService       : ApiService,
    onNavigateToLogin: () -> Unit,
    isWideLayout     : Boolean = false,
) {
    val state             = viewModel.uiState
    val dimensions        = LocalDimensions.current
    val navItems          = adminNavItems()
    val customColors      = MaterialTheme.customColors
    val snackbarHostState = remember { SnackbarHostState() }
    val scope             = kotlinx.coroutines.rememberCoroutineScope()

    var teamSubScreen by remember { mutableStateOf<TeamSubScreen>(TeamSubScreen.List) }
    var activeTab     by remember { mutableStateOf(AdminTab.DASHBOARD) }

    LaunchedEffect(state.navigateToWelcome) {
        if (state.navigateToWelcome) onNavigateToLogin()
    }

    LaunchedEffect(activeTab) {
        if (activeTab != AdminTab.TEAM) teamSubScreen = TeamSubScreen.List
    }

    val successLabel = when (state.successMessageKey) {
        "MSG_USER_DELETED"   -> stringResource(Res.string.admin_user_deleted_success)
        "MSG_BABY_DELETED"   -> stringResource(Res.string.admin_baby_deleted_success)
        "MSG_MEMBER_DELETED" -> stringResource(Res.string.admin_team_deleted_success)
        "MSG_TEAM_CREATED"   -> stringResource(Res.string.admin_team_created_success)
        else                 -> state.successMessageKey
    }
    val errorLabel = when (state.errorMessageKey) {
        null          -> null
        "ERR_GENERIC" -> stringResource(Res.string.admin_error_generic)
        else          -> state.errorMessageKey
    }

    LaunchedEffect(state.successMessageKey) {
        successLabel?.let { snackbarHostState.showSnackbar(it); viewModel.clearMessages() }
    }
    LaunchedEffect(state.errorMessageKey) {
        errorLabel?.let { snackbarHostState.showSnackbar(it); viewModel.clearMessages() }
    }

    val navContent: @Composable (Modifier) -> Unit = { modifier ->
        AdminTabContent(
            activeTab             = activeTab,
            viewModel             = viewModel,
            apiService            = apiService,
            teamSubScreen         = teamSubScreen,
            onTeamSubScreenChange = { teamSubScreen = it },
            modifier              = modifier
        )
    }

    if (isWideLayout) {
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
                        icon     = { Icon(if (selected) item.selectedIcon else item.icon, item.labelRes()) },
                        label    = { Text(item.labelRes(), style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        colors   = NavigationRailItemDefaults.colors(
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
            ) { padding -> navContent(Modifier.padding(padding)) }
        }
    } else {
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
                            selected        = selected,
                            onClick         = { activeTab = item.tab },
                            icon            = { Icon(if (selected) item.selectedIcon else item.icon, item.labelRes()) },
                            label           = { Text(item.labelRes(), style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            alwaysShowLabel = false,
                            colors          = NavigationBarItemDefaults.colors(
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
        ) { padding -> navContent(Modifier.padding(padding)) }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// AdminTabContent
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

        // ── Benches: AdminBenchesScreen loads team members itself via apiService ──
        AdminTab.BENCHES -> AdminBenchesScreen(apiService = apiService, modifier = modifier)

        // ── Team member management ─────────────────────────────────────────
        AdminTab.TEAM -> when (val sub = teamSubScreen) {

            // List of team members
            TeamSubScreen.List -> AdminTeamMembersScreen(
                viewModel  = viewModel,
                onAddClick = { onTeamSubScreenChange(TeamSubScreen.Create) },
                modifier   = modifier,
            )

            // Create new team member form
            TeamSubScreen.Create -> AdminCreateTeamMemberScreen(
                apiService  = apiService,
                onBackClick = { onTeamSubScreenChange(TeamSubScreen.List) },
                onCreated   = {
                    viewModel.loadTeamMembers()
                    onTeamSubScreenChange(TeamSubScreen.List)
                },
                modifier = modifier,
            )

            // NEW: after creating, offer immediate bench assignment
            is TeamSubScreen.AssignBench -> AdminAssignBenchToMemberScreen(
                apiService     = apiService,
                teamMember     = sub.newMember,
                onDone         = {
                    viewModel.loadTeamMembers()
                    onTeamSubScreenChange(TeamSubScreen.List)
                },
                onSkip         = {
                    viewModel.loadTeamMembers()
                    onTeamSubScreenChange(TeamSubScreen.List)
                },
                modifier       = modifier
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// AdminAssignBenchToMemberScreen
//
// Shown right after a team member is created so the admin can immediately
// link them to a health center without navigating to the Benches tab.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun AdminAssignBenchToMemberScreen(
    apiService : ApiService,
    teamMember : UserResponse,
    onDone     : () -> Unit,
    onSkip     : () -> Unit,
    modifier   : Modifier = Modifier
) {
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors
    val scope        = kotlinx.coroutines.rememberCoroutineScope()

    var benches       by remember { mutableStateOf<List<org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model.VaccinationBenchUi>>(emptyList()) }
    var isLoading     by remember { mutableStateOf(true) }
    var isAssigning   by remember { mutableStateOf(false) }
    var selectedBench by remember { mutableStateOf<org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model.VaccinationBenchUi?>(null) }
    var error         by remember { mutableStateOf<String?>(null) }
    var success       by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val result = apiService.getAllBenches()
        if (result is ApiResult.Success) {
            // Only show benches that don't already have a team member
            benches = result.data.filter { it.isActive && it.teamMemberId == null }
        }
        isLoading = false
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(dimensions.screenPadding),
        verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)
    ) {
        // Header
        Text(
            text       = "Assign to Health Center",
            style      = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text  = "Would you like to assign ${teamMember.fullName} to a health center now? " +
                    "They will be able to receive vaccination requests from parents at that center.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f)
        )

        if (isLoading) {
            Box(Modifier.fillMaxWidth(), Alignment.Center) {
                CircularProgressIndicator(color = customColors.accentGradientStart)
            }
        } else if (benches.isEmpty()) {
            Surface(
                shape    = RoundedCornerShape(dimensions.cardCornerRadius),
                color    = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text     = "All health centers already have a team member assigned, " +
                            "or no health centers exist yet. You can assign them later from the Benches tab.",
                    style    = MaterialTheme.typography.bodySmall,
                    color    = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(dimensions.spacingMedium)
                )
            }
        } else {
            Text(
                text  = "Select a health center (only unassigned centers shown):",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )

            androidx.compose.foundation.lazy.LazyColumn(
                modifier            = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
            ) {
                androidx.compose.foundation.lazy.items(benches) { bench ->
                    val isSelected = selectedBench?.benchId == bench.benchId
                    Card(
                        modifier  = Modifier.fillMaxWidth(),
                        shape     = RoundedCornerShape(dimensions.cardCornerRadius),
                        colors    = CardDefaults.cardColors(
                            containerColor = if (isSelected)
                                customColors.accentGradientStart.copy(alpha = 0.12f)
                            else
                                MaterialTheme.colorScheme.surface
                        ),
                        border    = if (isSelected)
                            androidx.compose.foundation.BorderStroke(
                                dimensions.borderWidthMedium,
                                customColors.accentGradientStart
                            )
                        else null,
                        onClick   = { selectedBench = bench }
                    ) {
                        Row(
                            modifier          = Modifier
                                .fillMaxWidth()
                                .padding(dimensions.spacingMedium),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick  = { selectedBench = bench },
                                colors   = RadioButtonDefaults.colors(
                                    selectedColor = customColors.accentGradientStart
                                )
                            )
                            Spacer(Modifier.width(dimensions.spacingSmall))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text       = bench.nameEn,
                                    style      = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text  = "${bench.governorate} · ${bench.district}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }
        }

        error?.let {
            Text(text = it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }

        if (success) {
            Surface(
                shape    = RoundedCornerShape(dimensions.cardCornerRadius),
                color    = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text     = "✅ ${teamMember.fullName} has been assigned to ${selectedBench?.nameEn}",
                    style    = MaterialTheme.typography.bodySmall,
                    color    = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(dimensions.spacingMedium)
                )
            }
        }

        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
        ) {
            OutlinedButton(
                onClick  = onSkip,
                modifier = Modifier.weight(1f),
                shape    = RoundedCornerShape(dimensions.buttonCornerRadius)
            ) {
                Text("Skip for now")
            }

            Button(
                onClick  = {
                    val bench = selectedBench ?: run {
                        error = "Please select a health center"
                        return@Button
                    }
                    scope.launch {
                        isAssigning = true
                        error       = null
                        val result  = apiService.assignTeamMemberToBench(bench.benchId, teamMember.userId)
                        isAssigning = false
                        if (result is ApiResult.Success) {
                            success = true
                            kotlinx.coroutines.delay(1200)
                            onDone()
                        } else {
                            error = "Failed to assign. Please try from the Benches tab."
                        }
                    }
                },
                enabled  = selectedBench != null && !isAssigning && !success,
                modifier = Modifier.weight(1f),
                shape    = RoundedCornerShape(dimensions.buttonCornerRadius),
                colors   = ButtonDefaults.buttonColors(containerColor = customColors.accentGradientStart)
            ) {
                if (isAssigning) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(dimensions.iconSmall),
                        color       = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = dimensions.borderWidthMedium
                    )
                } else {
                    Text("Assign")
                }
            }
        }
    }
}