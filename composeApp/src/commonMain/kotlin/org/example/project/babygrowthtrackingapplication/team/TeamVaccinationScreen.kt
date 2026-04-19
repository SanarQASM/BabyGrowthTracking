// composeApp/src/commonMain/kotlin/org/example/project/babygrowthtrackingapplication/team/TeamVaccinationScreen.kt

package org.example.project.babygrowthtrackingapplication.team

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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

// ─────────────────────────────────────────────────────────────────────────────
// TeamVaccinationScreen — Main entry for vaccination team members
// ─────────────────────────────────────────────────────────────────────────────

enum class TeamTab { BABIES, SCHEDULE }

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

    var selectedTab      by remember { mutableStateOf(TeamTab.BABIES) }
    var selectedBaby     by remember { mutableStateOf<TeamBabyItem?>(null) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { snackbar.showSnackbar(it); viewModel.clearError() }
    }
    LaunchedEffect(state.successMessage) {
        state.successMessage?.let { snackbar.showSnackbar(it); viewModel.clearSuccess() }
    }

    // ── Logout confirmation dialog ────────────────────────────────────────────
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

    // ── Baby detail / list transition ─────────────────────────────────────────
    AnimatedContent(
        targetState = selectedBaby,
        transitionSpec = {
            if (targetState != null) {
                slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
            } else {
                slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
            }
        },
        label = "team_baby_detail_transition"
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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    TeamTopBar(
                        benchName      = state.benchName,
                        teamMemberName = state.teamMemberName,
                        onLogout       = { showLogoutDialog = true },
                        customColors   = customColors,
                        dimensions     = dimensions
                    )
                    TeamTabRow(
                        selectedTab  = selectedTab,
                        onTabSelect  = { selectedTab = it },
                        customColors = customColors,
                        dimensions   = dimensions
                    )
                    when (selectedTab) {
                        TeamTab.BABIES   -> TeamBabiesTab(viewModel = viewModel, onBabyClick = { selectedBaby = it })
                        TeamTab.SCHEDULE -> TeamScheduleTab(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Top Bar
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TeamTopBar(
    benchName     : String,
    teamMemberName: String,
    onLogout      : () -> Unit,
    customColors  : CustomColors,
    dimensions    : Dimensions
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    listOf(customColors.accentGradientStart, customColors.accentGradientEnd)
                )
            )
            .padding(
                horizontal = dimensions.screenPadding,
                vertical   = dimensions.spacingMedium
            )
    ) {
        Row(
            modifier          = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
                    text       = benchName.ifBlank { stringResource(Res.string.team_bench_fallback) },
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onPrimary,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis
                )
                Text(
                    text  = teamMemberName.ifBlank { stringResource(Res.string.team_member_fallback) },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                )
            }

            IconButton(onClick = onLogout) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(Res.string.settings_logout),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Tab Row
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TeamTabRow(
    selectedTab : TeamTab,
    onTabSelect : (TeamTab) -> Unit,
    customColors: CustomColors,
    dimensions  : Dimensions
) {
    Surface(
        color           = MaterialTheme.colorScheme.surface,
        shadowElevation = dimensions.cardElevationSmall
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = dimensions.screenPadding,
                    vertical   = dimensions.spacingXSmall
                )
        ) {
            TeamTabItem(
                emoji        = stringResource(Res.string.team_tab_babies_emoji),
                label        = stringResource(Res.string.team_tab_babies),
                isSelected   = selectedTab == TeamTab.BABIES,
                onClick      = { onTabSelect(TeamTab.BABIES) },
                customColors = customColors,
                dimensions   = dimensions,
                modifier     = Modifier.weight(1f)
            )
            TeamTabItem(
                emoji        = stringResource(Res.string.team_tab_schedule_emoji),
                label        = stringResource(Res.string.team_tab_schedule),
                isSelected   = selectedTab == TeamTab.SCHEDULE,
                onClick      = { onTabSelect(TeamTab.SCHEDULE) },
                customColors = customColors,
                dimensions   = dimensions,
                modifier     = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TeamTabItem(
    emoji       : String,
    label       : String,
    isSelected  : Boolean,
    onClick     : () -> Unit,
    customColors: CustomColors,
    dimensions  : Dimensions,
    modifier    : Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue   = if (isSelected) customColors.accentGradientStart.copy(alpha = 0.12f)
        else Color.Transparent,
        label         = "tab_bg"
    )
    val contentColor by animateColorAsState(
        targetValue   = if (isSelected) customColors.accentGradientStart
        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
        label         = "tab_fg"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(dimensions.buttonCornerRadius))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(vertical = dimensions.spacingSmall),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall)
        ) {
            Text(emoji, style = MaterialTheme.typography.bodyMedium)
            Text(
                text       = label,
                style      = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color      = contentColor
            )
        }
    }
}