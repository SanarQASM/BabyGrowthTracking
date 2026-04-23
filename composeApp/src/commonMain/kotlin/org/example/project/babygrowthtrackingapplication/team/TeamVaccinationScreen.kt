// File: composeApp/src/commonMain/kotlin/org/example/project/babygrowthtrackingapplication/team/TeamVaccinationScreen_Updated.kt
// KEY CHANGE: Adds a "Requests" tab so the team can accept/reject parent join requests.
// Add TeamTab.REQUESTS to the enum and wire TeamRequestsTab + TeamRequestsViewModel.

package org.example.project.babygrowthtrackingapplication.team

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
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

// Updated enum — adds REQUESTS tab
enum class TeamTab { BABIES, SCHEDULE, REQUESTS }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamVaccinationScreenUpdated(
    viewModel          : TeamVaccinationViewModel,
    onNavigateToWelcome: () -> Unit = {}
) {
    val state        = viewModel.uiState
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors
    val snackbar     = remember { SnackbarHostState() }

    // ── Requests ViewModel — scoped to bench ──────────────────────────────────
    val requestsViewModel = remember {
        TeamRequestsViewModel(
            apiService = viewModel.apiService,
            getBenchId = { state.benchId }
        )
    }

    var selectedTab      by remember { mutableStateOf(TeamTab.BABIES) }
    var selectedBaby     by remember { mutableStateOf<TeamBabyItem?>(null) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Load requests when requests tab first selected
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

    // Resolve request-related success messages
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
                Button(onClick = { showLogoutDialog = false; onNavigateToWelcome() }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                    Text(stringResource(Res.string.settings_logout_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text(stringResource(Res.string.btn_cancel)) }
            }
        )
    }

    AnimatedContent(
        targetState = selectedBaby,
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
            TeamBabyDetailScreen(baby = baby, viewModel = viewModel, onBack = { selectedBaby = null })
        } else {
            Scaffold(snackbarHost = { SnackbarHost(snackbar) }, containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
                Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {

                    // Top bar
                    Box(
                        modifier = Modifier.fillMaxWidth().background(
                            Brush.horizontalGradient(listOf(customColors.accentGradientStart, customColors.accentGradientEnd))
                        ).padding(horizontal = dimensions.screenPadding, vertical = dimensions.spacingMedium)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(dimensions.iconXLarge).background(customColors.glassOverlay, CircleShape), contentAlignment = Alignment.Center) {
                                Text(stringResource(Res.string.team_hospital_emoji), style = MaterialTheme.typography.titleLarge)
                            }
                            Spacer(Modifier.width(dimensions.spacingMedium))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(state.benchName.ifBlank { stringResource(Res.string.team_bench_fallback) }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(state.teamMemberName.ifBlank { stringResource(Res.string.team_member_fallback) }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f))
                            }

                            // Requests badge on top-bar icon
                            if (reqState.pendingRequests.isNotEmpty()) {
                                BadgedBox(badge = {
                                    Badge { Text("${reqState.pendingRequests.size}") }
                                }) {
                                    IconButton(onClick = { selectedTab = TeamTab.REQUESTS }) {
                                        Icon(
                                            Icons.Default.Notifications,
                                            contentDescription = stringResource(Res.string.team_tab_requests),
                                            tint = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                }
                            }

                            IconButton(onClick = { showLogoutDialog = true }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.settings_logout), tint = MaterialTheme.colorScheme.onPrimary)
                            }
                        }
                    }

                    // Tab row
                    Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = dimensions.cardElevationSmall) {
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = dimensions.screenPadding, vertical = dimensions.spacingXSmall)) {
                            data class TabInfo(val tab: TeamTab, val emoji: String, val label: String, val badgeCount: Int = 0)
                            val tabs = listOf(
                                TabInfo(TeamTab.BABIES,   stringResource(Res.string.team_tab_babies_emoji),    stringResource(Res.string.team_tab_babies)),
                                TabInfo(TeamTab.SCHEDULE, stringResource(Res.string.team_tab_schedule_emoji),  stringResource(Res.string.team_tab_schedule)),
                                TabInfo(TeamTab.REQUESTS, stringResource(Res.string.team_tab_requests_emoji),  stringResource(Res.string.team_tab_requests), reqState.pendingRequests.size)
                            )
                            tabs.forEach { info ->
                                val isSelected = selectedTab == info.tab
                                val bgColor by animateColorAsState(
                                    if (isSelected) customColors.accentGradientStart.copy(alpha = 0.12f) else Color.Transparent, label = "tab_bg"
                                )
                                val fgColor by animateColorAsState(
                                    if (isSelected) customColors.accentGradientStart else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f), label = "tab_fg"
                                )
                                Box(
                                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(dimensions.buttonCornerRadius)).background(bgColor).clickable { selectedTab = info.tab }.padding(vertical = dimensions.spacingSmall),
                                    contentAlignment = Alignment.Center
                                ) {
                                    BadgedBox(
                                        badge = {
                                            if (info.badgeCount > 0) {
                                                Badge { Text("${info.badgeCount}", style = MaterialTheme.typography.labelSmall) }
                                            }
                                        }
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall)) {
                                            Text(info.emoji, style = MaterialTheme.typography.bodyMedium)
                                            Text(info.label, style = MaterialTheme.typography.labelMedium, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = fgColor)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Content
                    when (selectedTab) {
                        TeamTab.BABIES   -> TeamBabiesTab(viewModel = viewModel, onBabyClick = { selectedBaby = it })
                        TeamTab.SCHEDULE -> TeamScheduleTab(viewModel = viewModel)
                        TeamTab.REQUESTS -> TeamRequestsTab(viewModel = requestsViewModel)
                    }
                }
            }
        }
    }
}