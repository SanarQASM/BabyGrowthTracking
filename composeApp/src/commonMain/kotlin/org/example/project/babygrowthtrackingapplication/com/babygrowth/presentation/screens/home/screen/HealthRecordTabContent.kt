package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.screen

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import babygrowthtrackingapplication.composeapp.generated.resources.*
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model.*
import org.example.project.babygrowthtrackingapplication.data.network.BabyResponse
import org.example.project.babygrowthtrackingapplication.theme.LocalDimensions
import org.example.project.babygrowthtrackingapplication.theme.customColors
import org.jetbrains.compose.resources.stringResource

// ─────────────────────────────────────────────────────────────────────────────
// Internal navigation state for health record feature
// ─────────────────────────────────────────────────────────────────────────────

private sealed class HealthNavState {
    object Main : HealthNavState()
    data class Map(val babyName: String) : HealthNavState()
    data class BenchDetail(val bench: VaccinationBenchUi, val babyName: String) : HealthNavState()
    data class VaccinationDetail(val item: VaccinationScheduleUi) : HealthNavState()
}

// ─────────────────────────────────────────────────────────────────────────────
// Entry point
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun HealthRecordTabContent(
    viewModel: HealthRecordViewModel,
    babies: List<BabyResponse>
) {
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors
    val state        = viewModel.uiState

    // Init
    LaunchedEffect(babies) { viewModel.init(babies) }

    // Internal nav stack
    var navState by remember { mutableStateOf<HealthNavState>(HealthNavState.Main) }

    // Snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.error) {
        state.error?.let { snackbarHostState.showSnackbar(it); viewModel.clearError() }
    }
    LaunchedEffect(state.successMessage) {
        state.successMessage?.let { snackbarHostState.showSnackbar(it); viewModel.clearSuccess() }
    }

    // Reschedule confirm dialog
    if (state.showRescheduleConfirm) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissReschedule() },
            title = { Text(stringResource(Res.string.reschedule_title), fontWeight = FontWeight.Bold) },
            text  = { Text(stringResource(Res.string.reschedule_confirm)) },
            confirmButton = {
                Button(onClick = { viewModel.confirmReschedule() }) {
                    Text(stringResource(Res.string.reschedule_action))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissReschedule() }) {
                    Text(stringResource(Res.string.bench_assign_cancel))
                }
            }
        )
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val nav = navState) {
                is HealthNavState.Main -> {
                    HealthRecordMain(
                        viewModel = viewModel,
                        state     = state,
                        babies    = babies,
                        onOpenMap = { babyName -> navState = HealthNavState.Map(babyName) }
                    )
                }

                is HealthNavState.Map -> {
                    BenchMapScreen(
                        viewModel        = viewModel,
                        babyName         = nav.babyName,
                        onBack           = { navState = HealthNavState.Main },
                        onBenchSelected  = { bench ->
                            navState = HealthNavState.BenchDetail(bench, nav.babyName)
                        }
                    )
                }

                is HealthNavState.BenchDetail -> {
                    // ─────────────────────────────────────────────────────────
                    // BUG 1 FIX:
                    //   Navigation to Main is now driven by observing
                    //   state.assignment becoming non-null — meaning the API
                    //   call has completed and the ViewModel has committed the
                    //   assignment. Previously onCreateSchedule() was called
                    //   synchronously alongside onAssign(), pushing
                    //   navState = Main before the coroutine even ran,
                    //   resulting in assignment=null on Main and the vaccination
                    //   tab being invisible.
                    // ─────────────────────────────────────────────────────────
                    LaunchedEffect(state.assignment) {
                        if (state.assignment != null && navState is HealthNavState.BenchDetail) {
                            navState = HealthNavState.Main
                        }
                    }

                    BenchDetailScreen(
                        bench     = nav.bench,
                        babyName  = nav.babyName,
                        isLoading = state.assignmentLoading,
                        onBack    = { navState = HealthNavState.Map(nav.babyName) },
                        onAssign  = {
                            state.selectedBabyId?.let { babyId ->
                                viewModel.assignBench(babyId, nav.bench.benchId)
                            }
                        }
                        // onCreateSchedule removed — handled by LaunchedEffect above
                    )
                }

                is HealthNavState.VaccinationDetail -> {
                    VaccinationDetailScreen(
                        item        = nav.item,
                        onBack      = { navState = HealthNavState.Main },
                        onReschedule = {
                            viewModel.triggerReschedule()
                            navState = HealthNavState.Main
                        }
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Main content (child selector + tab + content)
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HealthRecordMain(
    viewModel : HealthRecordViewModel,
    state     : HealthRecordUiState,
    babies    : List<BabyResponse>,
    onOpenMap : (babyName: String) -> Unit
) {
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors

    val selectedBaby  = babies.firstOrNull { it.babyId == state.selectedBabyId }
    val hasAssignment = state.assignment != null

    Column(modifier = Modifier.fillMaxSize()) {

        // ── Header ────────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            customColors.accentGradientStart.copy(0.15f),
                            customColors.accentGradientEnd.copy(0.05f)
                        )
                    )
                )
                .padding(
                    horizontal = dimensions.screenPadding,
                    vertical   = dimensions.spacingMedium
                )
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)) {
                // Title
                Text(
                    text       = stringResource(Res.string.health_record_title),
                    style      = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                // Child selector
                if (babies.isEmpty()) {
                    Text(
                        stringResource(Res.string.schedule_no_babies),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(0.5f)
                    )
                } else {
                    ChildSelector(
                        babies         = babies,
                        selectedBabyId = state.selectedBabyId,
                        onSelect       = { viewModel.selectBaby(it) }
                    )
                }

                // Assigned branch card OR tap-to-select prompt
                selectedBaby?.let { baby ->
                    AssignedBranchCard(
                        assignment = state.assignment,
                        loading    = state.assignmentLoading,
                        onTap      = { onOpenMap(baby.fullName) }
                    )
                }
            }
        }

        // ── Sub-tabs (only shown when a branch is assigned) ───────────────────
        if (hasAssignment) {
            TabRow(
                selectedTabIndex = state.subTab.ordinal,
                containerColor   = MaterialTheme.colorScheme.surface,
                contentColor     = customColors.accentGradientStart,
                divider          = { HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant) }
            ) {
                HealthRecordSubTab.entries.forEach { tab ->
                    Tab(
                        selected = state.subTab == tab,
                        onClick  = { viewModel.setSubTab(tab) },
                        text     = {
                            Text(
                                text = when (tab) {
                                    HealthRecordSubTab.VACCINATIONS  -> "Vaccinations"
                                    HealthRecordSubTab.HEALTH_ISSUES -> "Health Issues"
                                    HealthRecordSubTab.APPOINTMENTS  -> "Appointments"
                                },
                                style      = MaterialTheme.typography.labelMedium,
                                fontWeight = if (state.subTab == tab) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // ── Tab content ───────────────────────────────────────────────────
            Box(modifier = Modifier.fillMaxSize()) {
                when (state.subTab) {
                    HealthRecordSubTab.VACCINATIONS -> {
                        VaccinationScheduleView(
                            schedules      = state.schedules,
                            filter         = state.vaccinationFilter,
                            loading        = state.schedulesLoading,
                            onFilterChange = { viewModel.setVaccinationFilter(it) },
                            onItemClick    = { /* navigate detail handled inside */ },
                            onReschedule   = { viewModel.triggerReschedule() }
                        )
                    }
                    HealthRecordSubTab.HEALTH_ISSUES -> {
                        HealthIssuesView(
                            issues         = state.healthIssues,
                            filter         = state.healthIssueFilter,
                            loading        = state.healthIssuesLoading,
                            onFilterChange = { viewModel.setHealthIssueFilter(it) },
                            onIssueClick   = { viewModel.selectHealthIssue(it) },
                            onResolve      = { viewModel.resolveHealthIssue(it) },
                            onAddIssue     = { viewModel.showAddHealthIssue() }
                        )
                    }
                    HealthRecordSubTab.APPOINTMENTS -> {
                        AppointmentsView(
                            appointments      = state.appointments,
                            filter            = state.appointmentFilter,
                            loading           = state.appointmentsLoading,
                            onFilterChange    = { viewModel.setAppointmentFilter(it) },
                            onAppointmentClick = { viewModel.selectAppointment(it) },
                            onCancel          = { viewModel.cancelAppointment(it) },
                            onAddAppointment  = { viewModel.showAddAppointment() }
                        )
                    }
                }
            }

        } else if (selectedBaby != null) {
            // ─────────────────────────────────────────────────────────────────
            // BUG 2 FIX:
            //   Previously: `else if (selectedBaby != null && !state.assignmentLoading)`
            //   That condition made BOTH branches false when assignmentLoading=true
            //   AND assignment=null, leaving the content area completely blank
            //   with no visual feedback — the app appeared frozen.
            //
            //   Fix: Split into two explicit sub-branches:
            //     • Show CircularProgressIndicator while loading
            //     • Show NoAssignmentPrompt only when loading is done and
            //       there is genuinely no assignment
            // ─────────────────────────────────────────────────────────────────
            if (state.assignmentLoading) {
                Box(
                    modifier         = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = customColors.accentGradientStart)
                }
            } else {
                // No assignment and not loading — prompt user to select a branch
                NoAssignmentPrompt(
                    babyName       = selectedBaby.fullName,
                    onSelectBranch = { onOpenMap(selectedBaby.fullName) }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Child selector (dropdown)
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChildSelector(
    babies         : List<BabyResponse>,
    selectedBabyId : String?,
    onSelect       : (String) -> Unit
) {
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text       = stringResource(Res.string.health_select_child),
            style      = MaterialTheme.typography.labelSmall,
            color      = MaterialTheme.colorScheme.onBackground.copy(0.5f),
            fontWeight = FontWeight.SemiBold
        )

        var expanded    by remember { mutableStateOf(false) }
        val selectedBaby = babies.firstOrNull { it.babyId == selectedBabyId }

        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            OutlinedCard(
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                shape = RoundedCornerShape(dimensions.cardCornerRadius)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimensions.spacingMedium, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        text       = selectedBaby?.fullName ?: "Select child",
                        style      = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (selectedBaby != null) FontWeight.SemiBold else FontWeight.Normal,
                        color      = if (selectedBaby != null) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurface.copy(0.4f)
                    )
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            }
            ExposedDropdownMenu(
                expanded          = expanded,
                onDismissRequest  = { expanded = false }
            ) {
                babies.forEach { baby ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment     = Alignment.CenterVertically
                            ) {
                                val isFemale = baby.gender.equals("FEMALE", true) ||
                                        baby.gender.equals("GIRL", true)
                                Text(if (isFemale) "👧" else "👦")
                                Text(baby.fullName, style = MaterialTheme.typography.bodyMedium)
                            }
                        },
                        onClick = { onSelect(baby.babyId); expanded = false },
                        trailingIcon = {
                            if (baby.babyId == selectedBabyId) {
                                Icon(
                                    Icons.Default.Check, null,
                                    tint     = customColors.accentGradientStart,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Assigned branch card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AssignedBranchCard(
    assignment : BabyBenchAssignmentUi?,
    loading    : Boolean,
    onTap      : () -> Unit
) {
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onTap() },
        shape  = RoundedCornerShape(dimensions.cardCornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = if (assignment != null)
                customColors.accentGradientStart.copy(0.1f)
            else MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensions.spacingMedium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment     = Alignment.CenterVertically,
                modifier              = Modifier.weight(1f)
            ) {
                Box(
                    modifier         = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(customColors.accentGradientStart.copy(0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🏥", style = MaterialTheme.typography.titleMedium)
                }
                if (loading) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color       = customColors.accentGradientStart
                    )
                } else {
                    Column {
                        Text(
                            text  = stringResource(Res.string.health_assigned_center),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(0.5f),
                            fontWeight = FontWeight.SemiBold
                        )
                        if (assignment != null) {
                            Text(
                                text      = assignment.benchNameEn,
                                style     = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines  = 1,
                                overflow  = TextOverflow.Ellipsis
                            )
                            Text(
                                text  = assignment.governorate,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(0.5f)
                            )
                        } else {
                            Text(
                                text  = stringResource(Res.string.health_no_assignment),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(0.5f)
                            )
                        }
                    }
                }
            }
            Icon(
                imageVector        = if (assignment != null) Icons.Default.Edit else Icons.Default.ChevronRight,
                contentDescription = null,
                tint               = customColors.accentGradientStart.copy(0.7f),
                modifier           = Modifier.size(20.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// No assignment prompt
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun NoAssignmentPrompt(
    babyName      : String,
    onSelectBranch: () -> Unit
) {
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors

    Box(
        modifier         = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium),
            modifier            = Modifier.padding(dimensions.screenPadding)
        ) {
            Text("🏥", style = MaterialTheme.typography.displaySmall)
            Text(
                text       = stringResource(Res.string.schedule_no_assignment_title),
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text  = stringResource(Res.string.schedule_no_assignment_body, babyName),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
            )
            Button(
                onClick = onSelectBranch,
                colors  = ButtonDefaults.buttonColors(
                    containerColor = customColors.accentGradientStart
                ),
                shape = RoundedCornerShape(dimensions.buttonCornerRadius)
            ) {
                Icon(Icons.Default.LocationOn, null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(Res.string.health_open_map))
            }
        }
    }
}