package org.example.project.babygrowthtrackingapplication.team

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.*
import babygrowthtrackingapplication.composeapp.generated.resources.Res
import babygrowthtrackingapplication.composeapp.generated.resources.*
import org.example.project.babygrowthtrackingapplication.theme.*
import org.jetbrains.compose.resources.stringResource

enum class TeamTab { BABIES, SCHEDULE, REQUESTS }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamVaccinationScreen(
    viewModel          : TeamVaccinationViewModel,
    onNavigateToWelcome: () -> Unit = {}
) {
    val state        = viewModel.uiState
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors
    val snackbar     = remember { SnackbarHostState() }

    // FIX: getBenchId lambda now reads viewModel.uiState.benchId at call time,
    // not a captured snapshot of state. Previously `getBenchId = { state.benchId }`
    // captured the state value at composition time — when benchId loaded
    // asynchronously it was still "" inside the lambda, causing
    // getPendingRequestsForBench("") which errors silently.
    val requestsViewModel = remember {
        TeamRequestsViewModel(
            apiService = viewModel.apiService,
            getBenchId = { viewModel.uiState.benchId }
        )
    }

    // FIX: dispose requestsViewModel coroutine scope when this composable leaves
    // composition. Previously the scope was never cancelled — coroutine leak.
    DisposableEffect(Unit) {
        onDispose { requestsViewModel.onDestroy() }
    }

    var selectedTab      by remember { mutableStateOf(TeamTab.BABIES) }
    var selectedBaby     by remember { mutableStateOf<TeamBabyItem?>(null) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    // FIX: now also waits for benchId to be non-blank before loading requests,
    // because requestsViewModel.getBenchId() correctly returns the latest value.
    LaunchedEffect(selectedTab, state.benchId) {
        if (selectedTab == TeamTab.REQUESTS && state.benchId.isNotBlank()) {
            requestsViewModel.loadRequests()
        }
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { snackbar.showSnackbar(it); viewModel.clearError() }
    }
    LaunchedEffect(state.successMessage) {
        state.successMessage?.let { snackbar.showSnackbar(it); viewModel.clearSuccess() }
    }

    val reqState = requestsViewModel.uiState
    val reqSuccessStr = when (reqState.successMessage) {
        "request_accepted" -> stringResource(Res.string.success_request_accepted)
        "request_rejected" -> stringResource(Res.string.success_request_rejected)
        null               -> null
        else               -> reqState.successMessage
    }
    LaunchedEffect(reqState.successMessage) {
        reqSuccessStr?.let { snackbar.showSnackbar(it); requestsViewModel.clearSuccess() }
    }
    LaunchedEffect(reqState.errorMessage) {
        reqState.errorMessage?.let { snackbar.showSnackbar(it); requestsViewModel.clearError() }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(stringResource(Res.string.admin_settings_logout_title)) },
            text  = { Text(stringResource(Res.string.settings_logout_message)) },
            confirmButton = {
                Button(
                    onClick = { showLogoutDialog = false; onNavigateToWelcome() },
                    colors  = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text(stringResource(Res.string.settings_logout_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(stringResource(Res.string.btn_cancel))
                }
            }
        )
    }

    AnimatedContent(
        targetState  = selectedBaby,
        transitionSpec = {
            if (targetState != null) {
                slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
            } else {
                slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
            }
        },
        label = "team_detail_transition"
    ) { baby ->
        if (baby != null) {
            TeamBabyDetailScreen(
                baby      = baby,
                viewModel = viewModel,
                onBack    = { selectedBaby = null }
            )
        } else {
            Scaffold(
                snackbarHost   = { SnackbarHost(snackbar) },
                containerColor = MaterialTheme.colorScheme.background
            ) { innerPadding ->
                Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {

                    // ── Top bar ───────────────────────────────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(customColors.accentGradientStart, customColors.accentGradientEnd)
                                )
                            )
                            .padding(horizontal = dimensions.screenPadding, vertical = dimensions.spacingMedium)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(dimensions.iconXLarge)
                                    .background(customColors.glassOverlay, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    stringResource(Res.string.team_hospital_emoji),
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                            Spacer(Modifier.width(dimensions.spacingMedium))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text       = state.benchName.ifBlank { stringResource(Res.string.team_bench_fallback) },
                                    style      = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color      = MaterialTheme.colorScheme.onPrimary,
                                    maxLines   = 1,
                                    overflow   = TextOverflow.Ellipsis
                                )
                                Text(
                                    text  = state.teamMemberName.ifBlank { stringResource(Res.string.team_member_fallback) },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                                )
                            }

                            if (reqState.pendingRequests.isNotEmpty()) {
                                BadgedBox(badge = { Badge { Text("${reqState.pendingRequests.size}") } }) {
                                    IconButton(onClick = { selectedTab = TeamTab.REQUESTS }) {
                                        Icon(
                                            Icons.Default.Notifications,
                                            contentDescription = stringResource(Res.string.team_tab_requests),
                                            tint = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                }
                            }

                            // FIX: was Icons.AutoMirrored.Filled.ArrowBack — semantically wrong for logout.
                            // Back arrow implies navigation; logout needs a logout icon.
                            IconButton(onClick = { showLogoutDialog = true }) {
                                Icon(
                                    imageVector        = Icons.AutoMirrored.Filled.Logout,
                                    contentDescription = stringResource(Res.string.settings_logout),
                                    tint               = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }

                    // ── Loading / no bench states ──────────────────────────────
                    if (state.benchLoading) {
                        Box(Modifier.fillMaxSize(), Alignment.Center) {
                            CircularProgressIndicator(color = customColors.accentGradientStart)
                        }
                        return@Scaffold
                    }

                    if (state.noBenchAssigned) {
                        NoBenchAssignedState(
                            teamMemberName = state.teamMemberName,
                            customColors   = customColors,
                            dimensions     = dimensions,
                            onRetry        = { viewModel.loadTeamData() }
                        )
                        return@Scaffold
                    }

                    // ── Tab row ───────────────────────────────────────────────
                    Surface(
                        color           = MaterialTheme.colorScheme.surface,
                        shadowElevation = dimensions.cardElevationSmall
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = dimensions.screenPadding, vertical = dimensions.spacingXSmall)
                        ) {
                            data class TabInfo(
                                val tab       : TeamTab,
                                val emoji     : String,
                                val label     : String,
                                val badgeCount: Int = 0
                            )
                            val tabs = listOf(
                                TabInfo(TeamTab.BABIES,   stringResource(Res.string.team_tab_babies_emoji),   stringResource(Res.string.team_tab_babies)),
                                TabInfo(TeamTab.SCHEDULE, stringResource(Res.string.team_tab_schedule_emoji), stringResource(Res.string.team_tab_schedule)),
                                TabInfo(TeamTab.REQUESTS, stringResource(Res.string.team_tab_requests_emoji), stringResource(Res.string.team_tab_requests), reqState.pendingRequests.size)
                            )
                            tabs.forEach { info ->
                                val isSelected = selectedTab == info.tab
                                val bgColor by animateColorAsState(
                                    if (isSelected) customColors.accentGradientStart.copy(alpha = 0.12f)
                                    else Color.Transparent, label = "tab_bg"
                                )
                                val fgColor by animateColorAsState(
                                    if (isSelected) customColors.accentGradientStart
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                                    label = "tab_fg"
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(dimensions.buttonCornerRadius))
                                        .background(bgColor)
                                        .clickable { selectedTab = info.tab }
                                        .padding(vertical = dimensions.spacingSmall),
                                    contentAlignment = Alignment.Center
                                ) {
                                    BadgedBox(
                                        badge = {
                                            if (info.badgeCount > 0) {
                                                Badge {
                                                    Text(
                                                        "${info.badgeCount}",
                                                        style = MaterialTheme.typography.labelSmall
                                                    )
                                                }
                                            }
                                        }
                                    ) {
                                        Row(
                                            verticalAlignment     = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall)
                                        ) {
                                            Text(info.emoji, style = MaterialTheme.typography.bodyMedium)
                                            Text(
                                                info.label,
                                                style      = MaterialTheme.typography.labelMedium,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color      = fgColor
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ── Tab content ───────────────────────────────────────────
                    when (selectedTab) {
                        TeamTab.BABIES   -> TeamBabiesTab(
                            viewModel  = viewModel,
                            onBabyClick = { selectedBaby = it }
                        )
                        TeamTab.SCHEDULE -> TeamScheduleTab(viewModel = viewModel)
                        TeamTab.REQUESTS -> TeamRequestsTab(viewModel = requestsViewModel)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// NoBenchAssignedState
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun NoBenchAssignedState(
    teamMemberName: String,
    customColors  : CustomColors,
    dimensions    : Dimensions,
    onRetry       : () -> Unit
) {
    Box(Modifier.fillMaxSize(), Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium),
            modifier            = Modifier.padding(dimensions.screenPadding)
        ) {
            Text("🏥", style = MaterialTheme.typography.displayMedium)

            Text(
                text       = "No Health Center Assigned",
                style      = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text  = "Hello${if (teamMemberName.isNotBlank()) " $teamMemberName" else ""}! " +
                        "You haven't been assigned to a health center yet.\n\n" +
                        "Please ask the admin to assign you to a health center " +
                        "so you can start managing vaccinations.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f)
            )

            Spacer(Modifier.height(dimensions.spacingSmall))

            Surface(
                shape    = RoundedCornerShape(dimensions.cardCornerRadius),
                color    = customColors.accentGradientStart.copy(alpha = 0.08f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier              = Modifier.padding(dimensions.spacingMedium),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint     = customColors.accentGradientStart,
                        modifier = Modifier.size(dimensions.iconMedium)
                    )
                    Text(
                        text  = "Admin → Health Centers → select center → Assign Team Member",
                        style = MaterialTheme.typography.bodySmall,
                        color = customColors.accentGradientStart
                    )
                }
            }

            Button(
                onClick = onRetry,
                colors  = ButtonDefaults.buttonColors(containerColor = customColors.accentGradientStart)
            ) {
                Icon(Icons.Default.Refresh, null, modifier = Modifier.size(dimensions.iconSmall))
                Spacer(Modifier.width(dimensions.spacingXSmall))
                Text("Retry")
            }
        }
    }
}