// File: composeApp/src/commonMain/kotlin/org/example/project/babygrowthtrackingapplication/com/babygrowth/presentation/screens/home/screen/HealthRecordTabContent_Updated.kt
//
// KEY CHANGES vs original:
//  1. BenchRequestStatusScreen shown when baby has PENDING or REJECTED request
//  2. "Send Join Request" replaces old "Assign bench" in the map flow
//  3. Success message sentinels resolved to localised strings
//  4. No hardcoded strings — all from stringResource()

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
import androidx.compose.ui.unit.sp
import babygrowthtrackingapplication.composeapp.generated.resources.Res
import babygrowthtrackingapplication.composeapp.generated.resources.*
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model.*
import org.example.project.babygrowthtrackingapplication.data.network.BabyResponse
import org.example.project.babygrowthtrackingapplication.data.network.BenchRequestStatusUi
import org.example.project.babygrowthtrackingapplication.theme.LocalDimensions
import org.example.project.babygrowthtrackingapplication.theme.customColors
import org.jetbrains.compose.resources.stringResource

// ─────────────────────────────────────────────────────────────────────────────
// Internal nav state
// ─────────────────────────────────────────────────────────────────────────────

private sealed class HealthNavState {
    object Main : HealthNavState()
    data class Map(val babyName: String)       : HealthNavState()
    data class BenchDetail(
        val bench    : VaccinationBenchUi,
        val babyName : String,
        val fromMap  : Boolean = true
    ) : HealthNavState()
    data class VaccinationDetail(val item: VaccinationScheduleUi) : HealthNavState()
    data class AddHealthIssue(val babyId: String, val babyName: String) : HealthNavState()
    data class AddAppointment(val babyId: String, val babyName: String) : HealthNavState()
}

// ─────────────────────────────────────────────────────────────────────────────
// Entry point
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun HealthRecordTabContent(
    viewModel: HealthRecordViewModel,
    babies   : List<BabyResponse>
) {
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors
    val state        = viewModel.uiState

    LaunchedEffect(babies) { viewModel.init(babies) }

    var navState by remember { mutableStateOf<HealthNavState>(HealthNavState.Main) }

    val snackbarHostState = remember { SnackbarHostState() }

    // ── Resolve sentinel success messages ─────────────────────────────────────
    val successResolved = when (state.successMessage) {
        "request_sent"            -> stringResource(Res.string.success_request_sent)
        "request_cancelled"       -> stringResource(Res.string.success_request_cancelled)
        "request_accepted"        -> stringResource(Res.string.success_request_accepted)
        "request_rejected"        -> stringResource(Res.string.success_request_rejected)
        "issue_resolved"          -> stringResource(Res.string.success_issue_resolved)
        "health_issue_added"      -> stringResource(Res.string.success_health_issue_added)
        "appointment_added"       -> stringResource(Res.string.success_appointment_added)
        "appointment_cancelled"   -> stringResource(Res.string.success_appointment_cancelled)
        null                      -> null
        else                      -> state.successMessage
    }
    val errorResolved = when (state.error) {
        "reject_reason_required"  -> stringResource(Res.string.error_reject_reason_required)
        null                      -> null
        else                      -> state.error
    }

    LaunchedEffect(state.error)          { errorResolved?.let { snackbarHostState.showSnackbar(it); viewModel.clearError() } }
    LaunchedEffect(state.successMessage) { successResolved?.let { snackbarHostState.showSnackbar(it); viewModel.clearSuccess() } }

    // ── Reschedule dialogs (unchanged from original) ──────────────────────────
    if (state.showRescheduleReasonPicker) {
        val overdueCount       = state.schedules.count { it.statusUi == ScheduleStatusUi.OVERDUE || it.statusUi == ScheduleStatusUi.MISSED }
        val reschedulableCount = state.schedules.count { it.statusUi != ScheduleStatusUi.COMPLETED && it.statusUi != ScheduleStatusUi.MISSED }
        RescheduleReasonPickerDialog(
            overdueCount       = overdueCount,
            reschedulableCount = reschedulableCount,
            isLoading          = state.rescheduleInProgress,
            onConfirm          = { reason, notes -> viewModel.confirmReschedule(reason, notes.ifBlank { null }) },
            onDismiss          = viewModel::dismissReschedule
        )
    }
    if (state.rescheduleInProgress) {
        AlertDialog(
            onDismissRequest = {},
            title            = { Text(stringResource(Res.string.schedule_reschedule_in_progress_title), fontWeight = FontWeight.Bold) },
            text = {
                Row(horizontalArrangement = Arrangement.spacedBy(dimensions.spacingLarge), verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(dimensions.iconLarge), color = customColors.accentGradientStart, strokeWidth = dimensions.borderWidthMedium + dimensions.borderWidthThin)
                    Text(stringResource(Res.string.schedule_reschedule_checking), style = MaterialTheme.typography.bodyMedium)
                }
            },
            confirmButton = {}
        )
    }
    state.rescheduleResult?.let { result ->
        RescheduleResultDialog(result = result, onDismiss = viewModel::dismissRescheduleResult)
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val nav = navState) {
                is HealthNavState.Main -> {
                    HealthRecordMain(
                        viewModel        = viewModel,
                        state            = state,
                        babies           = babies,
                        onOpenMap        = { babyName -> navState = HealthNavState.Map(babyName) },
                        onChangeBench    = { babyName -> navState = HealthNavState.Map(babyName) },
                        onAddHealthIssue = { babyId, babyName -> navState = HealthNavState.AddHealthIssue(babyId, babyName) },
                        onAddAppointment = { babyId, babyName -> navState = HealthNavState.AddAppointment(babyId, babyName) }
                    )
                }
                is HealthNavState.Map -> {
                    // Map now sends a REQUEST instead of directly assigning
                    BenchMapScreen(
                        viewModel       = viewModel,
                        babyName        = nav.babyName,
                        onBack          = { navState = HealthNavState.Main },
                        onBenchSelected = { bench ->
                            // Show bench detail which now has "Send Request" button
                            navState = HealthNavState.BenchDetail(bench = bench, babyName = nav.babyName, fromMap = true)
                        }
                    )
                }
                is HealthNavState.BenchDetail -> {
                    BenchRequestDetailScreen(
                        bench            = nav.bench,
                        babyName         = nav.babyName,
                        isSubmitting     = state.benchRequestSubmitting,
                        onBack           = {
                            navState = if (nav.fromMap) HealthNavState.Map(nav.babyName) else HealthNavState.Main
                        },
                        onSendRequest    = {
                            state.selectedBabyId?.let { babyId ->
                                viewModel.sendBenchRequest(babyId, nav.bench.benchId)
                                navState = HealthNavState.Main
                            }
                        }
                    )
                }
                is HealthNavState.VaccinationDetail -> {
                    VaccinationDetailScreen(
                        item         = nav.item,
                        onBack       = { navState = HealthNavState.Main },
                        onReschedule = { navState = HealthNavState.Main; viewModel.triggerReschedule() }
                    )
                }
                is HealthNavState.AddHealthIssue -> {
                    AddHealthIssueScreen(
                        babyId    = nav.babyId,
                        babyName  = nav.babyName,
                        viewModel = viewModel,
                        onBack    = { navState = HealthNavState.Main },
                        onSaved   = { navState = HealthNavState.Main }
                    )
                }
                is HealthNavState.AddAppointment -> {
                    AddAppointmentScreen(
                        babyId    = nav.babyId,
                        babyName  = nav.babyName,
                        viewModel = viewModel,
                        onBack    = { navState = HealthNavState.Main },
                        onSaved   = { navState = HealthNavState.Main }
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Main content — entire page scrolls via one Column
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun HealthRecordMain(
    viewModel       : HealthRecordViewModel,
    state           : HealthRecordUiState,
    babies          : List<BabyResponse>,
    onOpenMap       : (babyName: String) -> Unit,
    onChangeBench   : (babyName: String) -> Unit,
    onAddHealthIssue: (babyId: String, babyName: String) -> Unit,
    onAddAppointment: (babyId: String, babyName: String) -> Unit
) {
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors
    val selectedBaby = babies.firstOrNull { it.babyId == state.selectedBabyId }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {

        // ── Header ─────────────────────────────────────────────────────────────
        Column(
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
                .padding(horizontal = dimensions.screenPadding, vertical = dimensions.spacingMedium),
            verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)
        ) {
            Text(
                text       = stringResource(Res.string.health_record_title),
                style      = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            if (babies.isEmpty()) {
                Text(
                    text  = stringResource(Res.string.schedule_no_babies),
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

            // Branch status card (shows connected / pending / rejected / none)
            selectedBaby?.let { baby ->
                BranchStatusCard(
                    state    = state,
                    baby     = baby,
                    onOpenMap       = { onOpenMap(baby.fullName) },
                    onChangeBench   = { onChangeBench(baby.fullName) },
                    onCancelRequest = {
                        state.activeBenchRequest?.requestId?.let { viewModel.cancelBenchRequest(it) }
                    },
                    onPickOtherBench = { onOpenMap(baby.fullName) }
                )
            }
        }

        // ── Sub-tabs (only visible when connected to a bench) ─────────────────
        val showTabs = state.isConnectedToBench && selectedBaby != null

        if (showTabs) {
            ScrollableTabRow(
                selectedTabIndex = state.subTab.ordinal,
                edgePadding      = dimensions.screenPadding,
                containerColor   = MaterialTheme.colorScheme.surface,
                contentColor     = customColors.accentGradientStart,
                divider          = { HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant) }
            ) {
                HealthRecordSubTab.entries.forEach { tab ->
                    Tab(
                        selected = state.subTab == tab,
                        onClick  = { viewModel.setSubTab(tab) },
                        text = {
                            Text(
                                text = when (tab) {
                                    HealthRecordSubTab.VACCINATIONS  -> stringResource(Res.string.health_sub_tab_vaccinations)
                                    HealthRecordSubTab.HEALTH_ISSUES -> stringResource(Res.string.health_sub_tab_health_issues)
                                    HealthRecordSubTab.APPOINTMENTS  -> stringResource(Res.string.health_sub_tab_appointments)
                                },
                                maxLines   = 1,
                                overflow   = TextOverflow.Ellipsis,
                                fontSize   = 11.sp,
                                fontWeight = if (state.subTab == tab) FontWeight.Bold else FontWeight.Normal,
                                style      = MaterialTheme.typography.labelSmall
                            )
                        }
                    )
                }
            }

            Box(modifier = Modifier.fillMaxWidth()) {
                when (state.subTab) {
                    HealthRecordSubTab.VACCINATIONS -> VaccinationScheduleView(
                        schedules      = state.schedules,
                        filter         = state.vaccinationFilter,
                        loading        = state.schedulesLoading,
                        onFilterChange = { viewModel.setVaccinationFilter(it) },
                        onItemClick    = { /* navigate to detail */ },
                        onReschedule   = { viewModel.triggerReschedule() }
                    )
                    HealthRecordSubTab.HEALTH_ISSUES -> HealthIssuesView(
                        issues         = state.healthIssues,
                        filter         = state.healthIssueFilter,
                        loading        = state.healthIssuesLoading,
                        onFilterChange = { viewModel.setHealthIssueFilter(it) },
                        onIssueClick   = { viewModel.selectHealthIssue(it) },
                        onResolve      = { viewModel.resolveHealthIssue(it) },
                        onAddIssue     = {
                            selectedBaby?.let { onAddHealthIssue(it.babyId, it.fullName) }
                        }
                    )
                    HealthRecordSubTab.APPOINTMENTS -> AppointmentsView(
                        appointments       = state.appointments,
                        filter             = state.appointmentFilter,
                        loading            = state.appointmentsLoading,
                        onFilterChange     = { viewModel.setAppointmentFilter(it) },
                        onAppointmentClick = { viewModel.selectAppointment(it) },
                        onCancel           = { viewModel.cancelAppointment(it) },
                        onAddAppointment   = {
                            selectedBaby?.let { onAddAppointment(it.babyId, it.fullName) }
                        }
                    )
                }
            }
        } else if (selectedBaby != null && !state.isConnectedToBench) {
            // Show pending / rejected / no-assignment states
            if (!state.hasPendingRequest && !state.hasRejectedRequest) {
                if (state.assignmentLoading || state.benchRequestLoading) {
                    Box(
                        modifier         = Modifier.fillMaxWidth().height(dimensions.iconXLarge + dimensions.spacingXLarge),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.customColors.accentGradientStart)
                    }
                } else {
                    NoAssignmentPrompt(
                        babyName       = selectedBaby.fullName,
                        onSelectBranch = { onOpenMap(selectedBaby.fullName) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(dimensions.spacingXXLarge))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// BranchStatusCard — the card shown at the top of health record tab
// Shows different states: connected / pending / rejected / no assignment
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun BranchStatusCard(
    state           : HealthRecordUiState,
    baby            : BabyResponse,
    onOpenMap       : () -> Unit,
    onChangeBench   : () -> Unit,
    onCancelRequest : () -> Unit,
    onPickOtherBench: () -> Unit
) {
    val req = state.activeBenchRequest

    when {
        // Connected to a bench (assignment exists)
        state.isConnectedToBench -> {
            AssignedBranchCard(
                assignment = state.assignment,
                loading    = state.assignmentLoading,
                onTap      = onChangeBench
            )
        }
        // Pending request
        req != null && req.status == BenchRequestStatusUi.PENDING -> {
            BenchRequestStatusScreen(
                request         = req,
                babyName        = baby.fullName,
                isSubmitting    = state.benchRequestSubmitting,
                onCancelRequest = onCancelRequest,
                onPickOtherBench = onPickOtherBench
            )
        }
        // Rejected request
        req != null && req.status == BenchRequestStatusUi.REJECTED -> {
            BenchRequestStatusScreen(
                request          = req,
                babyName         = baby.fullName,
                isSubmitting     = state.benchRequestSubmitting,
                onCancelRequest  = onCancelRequest,
                onPickOtherBench = onPickOtherBench
            )
        }
        // No assignment, no pending request → show "find a bench" CTA
        else -> {
            AssignedBranchCard(
                assignment = null,
                loading    = state.assignmentLoading || state.benchRequestLoading,
                onTap      = onOpenMap
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// AssignedBranchCard, ChildSelector, NoAssignmentPrompt
// (Same as original HealthRecordTabContent.kt — included for completeness)
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChildSelector(
    babies        : List<BabyResponse>,
    selectedBabyId: String?,
    onSelect      : (String) -> Unit
) {
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors

    var expanded     by remember { mutableStateOf(false) }
    val selectedBaby = babies.firstOrNull { it.babyId == selectedBabyId }

    Column(verticalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall)) {
        Text(
            text       = stringResource(Res.string.health_select_child),
            style      = MaterialTheme.typography.labelSmall,
            color      = MaterialTheme.colorScheme.onBackground.copy(0.5f),
            fontWeight = FontWeight.SemiBold
        )
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            OutlinedCard(
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                shape    = RoundedCornerShape(dimensions.cardCornerRadius)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimensions.spacingMedium, vertical = dimensions.healthDropdownItemPaddingV),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        text       = selectedBaby?.fullName ?: stringResource(Res.string.health_select_child_hint),
                        style      = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (selectedBaby != null) FontWeight.SemiBold else FontWeight.Normal,
                        color      = if (selectedBaby != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(0.4f)
                    )
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            }
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                babies.forEach { baby ->
                    val isFemale = baby.gender.equals("FEMALE", true) || baby.gender.equals("GIRL", true)
                    DropdownMenuItem(
                        text = {
                            Row(horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall), verticalAlignment = Alignment.CenterVertically) {
                                Text(if (isFemale) "👧" else "👦")
                                Text(baby.fullName, style = MaterialTheme.typography.bodyMedium)
                            }
                        },
                        onClick      = { onSelect(baby.babyId); expanded = false },
                        trailingIcon = {
                            if (baby.babyId == selectedBabyId) {
                                Icon(Icons.Default.Check, null, tint = customColors.accentGradientStart, modifier = Modifier.size(dimensions.iconSmall))
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AssignedBranchCard(
    assignment: BabyBenchAssignmentUi?,
    loading   : Boolean,
    onTap     : () -> Unit
) {
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors

    Card(
        modifier  = Modifier.fillMaxWidth().clickable { onTap() },
        shape     = RoundedCornerShape(dimensions.cardCornerRadius),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(dimensions.healthSubTabElevation)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(dimensions.spacingMedium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall + dimensions.borderWidthMedium),
                verticalAlignment     = Alignment.CenterVertically,
                modifier              = Modifier.weight(1f)
            ) {
                Box(
                    modifier         = Modifier.size(dimensions.healthCircleSize).clip(CircleShape).background(customColors.accentGradientStart.copy(0.15f)),
                    contentAlignment = Alignment.Center
                ) { Text("🏥", style = MaterialTheme.typography.titleMedium) }

                if (loading) {
                    CircularProgressIndicator(modifier = Modifier.size(dimensions.iconSmall + dimensions.borderWidthMedium), strokeWidth = dimensions.borderWidthMedium, color = customColors.accentGradientStart)
                } else {
                    Column {
                        Text(text = stringResource(Res.string.health_assigned_center), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(0.5f), fontWeight = FontWeight.SemiBold)
                        if (assignment != null) {
                            Text(text = assignment.benchNameEn, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(text = assignment.governorate, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                        } else {
                            Text(text = stringResource(Res.string.health_no_assignment), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                        }
                    }
                }
            }
            Icon(
                imageVector        = if (assignment != null) Icons.Default.Edit else Icons.Default.ChevronRight,
                contentDescription = null,
                tint               = customColors.accentGradientStart.copy(0.7f),
                modifier           = Modifier.size(dimensions.iconMedium)
            )
        }
    }
}

@Composable
private fun NoAssignmentPrompt(babyName: String, onSelectBranch: () -> Unit) {
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors
    Box(modifier = Modifier.fillMaxWidth().padding(dimensions.screenPadding), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)) {
            Text("🏥", style = MaterialTheme.typography.displaySmall)
            Text(text = stringResource(Res.string.schedule_no_assignment_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(text = stringResource(Res.string.schedule_no_assignment_body, babyName), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
            Button(onClick = onSelectBranch, colors = ButtonDefaults.buttonColors(containerColor = customColors.accentGradientStart), shape = RoundedCornerShape(dimensions.buttonCornerRadius)) {
                Icon(Icons.Default.LocationOn, null)
                Spacer(Modifier.width(dimensions.spacingSmall))
                Text(stringResource(Res.string.health_open_map))
            }
        }
    }
}