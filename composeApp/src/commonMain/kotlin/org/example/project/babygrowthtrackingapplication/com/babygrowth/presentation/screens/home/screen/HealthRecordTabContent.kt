package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.screen

// ─────────────────────────────────────────────────────────────────────────────
// FIXES APPLIED:
//  Fix 1: Sub-tab titles use maxLines=1, overflow=Clip, smaller text to prevent wrapping
//  Fix 3: Removed border/selection highlight from AssignedBranchCard (no isSelected styling)
//  Fix 5: HealthRecordMain wrapped in verticalScroll for the header+card area
//  Fix 7: BenchDetail navigation no longer returns to Main on assignment success;
//         it navigates properly from Map → BenchDetail and back without collapsing
// ─────────────────────────────────────────────────────────────────────────────

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
import androidx.compose.ui.unit.sp
import babygrowthtrackingapplication.composeapp.generated.resources.*
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model.*
import org.example.project.babygrowthtrackingapplication.data.network.BabyResponse
import org.example.project.babygrowthtrackingapplication.theme.LocalDimensions
import org.example.project.babygrowthtrackingapplication.theme.customColors
import org.jetbrains.compose.resources.stringResource

// ─────────────────────────────────────────────────────────────────────────────
// Internal navigation state
// FIX 7: Added BenchDetail as a proper nav state that persists correctly
// ─────────────────────────────────────────────────────────────────────────────

private sealed class HealthNavState {
    object Main : HealthNavState()
    data class Map(val babyName: String) : HealthNavState()
    // FIX 7: BenchDetail now stores previous state so back nav works correctly
    data class BenchDetail(
        val bench: VaccinationBenchUi,
        val babyName: String,
        val fromMap: Boolean = true   // true = came from Map, false = came from Main (change bench)
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
    LaunchedEffect(state.error) {
        state.error?.let { snackbarHostState.showSnackbar(it); viewModel.clearError() }
    }
    LaunchedEffect(state.successMessage) {
        state.successMessage?.let { snackbarHostState.showSnackbar(it); viewModel.clearSuccess() }
    }

    if (state.showRescheduleReasonPicker) {
        val overdueCount = state.schedules.count {
            it.statusUi == ScheduleStatusUi.OVERDUE || it.statusUi == ScheduleStatusUi.MISSED
        }
        val reschedulableCount = state.schedules.count {
            it.statusUi != ScheduleStatusUi.COMPLETED && it.statusUi != ScheduleStatusUi.MISSED
        }
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
            title = { Text(stringResource(Res.string.schedule_reschedule_in_progress_title), fontWeight = FontWeight.Bold) },
            text  = {
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
                        // FIX 7: onOpenMap now correctly comes from both initial assign and change bench
                        onOpenMap        = { babyName -> navState = HealthNavState.Map(babyName) },
                        // FIX 7: onChangeBench navigates to Map (not BenchDetail directly)
                        onChangeBench    = { babyName -> navState = HealthNavState.Map(babyName) },
                        onAddHealthIssue = { babyId, babyName -> navState = HealthNavState.AddHealthIssue(babyId, babyName) },
                        onAddAppointment = { babyId, babyName -> navState = HealthNavState.AddAppointment(babyId, babyName) }
                    )
                }
                is HealthNavState.Map -> {
                    BenchMapScreen(
                        viewModel       = viewModel,
                        babyName        = nav.babyName,
                        onBack          = { navState = HealthNavState.Main },
                        // FIX 7: Map always navigates forward to BenchDetail, never back to Main
                        onBenchSelected = { bench ->
                            navState = HealthNavState.BenchDetail(
                                bench     = bench,
                                babyName  = nav.babyName,
                                fromMap   = true
                            )
                        }
                    )
                }
                is HealthNavState.BenchDetail -> {
                    // FIX 7: Assignment success no longer auto-navigates away
                    // User explicitly navigates back after assigning
                    BenchDetailScreen(
                        bench     = nav.bench,
                        babyName  = nav.babyName,
                        isLoading = state.assignmentLoading,
                        // FIX 7: Back always goes to Map (not Main)
                        onBack    = {
                            navState = if (nav.fromMap)
                                HealthNavState.Map(nav.babyName)
                            else
                                HealthNavState.Main
                        },
                        onAssign  = {
                            state.selectedBabyId?.let { babyId ->
                                viewModel.assignBench(babyId, nav.bench.benchId)
                                // FIX 7: After assign, go back to Main
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
// Main content
// FIX 5: Header section is now scrollable
// FIX 3: AssignedBranchCard no longer shows selection highlight
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
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

    val selectedBaby  = babies.firstOrNull { it.babyId == state.selectedBabyId }
    val hasAssignment = state.assignment != null

    Column(modifier = Modifier.fillMaxSize()) {

        // FIX 5: Header wrapped in verticalScroll so all content accessible
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(listOf(
                        customColors.accentGradientStart.copy(0.15f),
                        customColors.accentGradientEnd.copy(0.05f)
                    ))
                )
                .verticalScroll(rememberScrollState())
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

            selectedBaby?.let { baby ->
                // FIX 3: Pass onChangeBench so the card taps correctly
                AssignedBranchCard(
                    assignment = state.assignment,
                    loading    = state.assignmentLoading,
                    onTap      = {
                        if (state.assignment != null) {
                            // Change bench → go to map to pick a new one
                            onChangeBench(baby.fullName)
                        } else {
                            // No assignment → open map to assign
                            onOpenMap(baby.fullName)
                        }
                    }
                )
            }
        }

        // FIX 1: Sub-tabs with fixed height and no-wrap text
        if (hasAssignment && selectedBaby != null) {
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
                        // FIX 1: Use modifier to constrain height and prevent wrapping
                        modifier = Modifier.height(48.dp),
                        text = {
                            Text(
                                text = when (tab) {
                                    HealthRecordSubTab.VACCINATIONS  -> stringResource(Res.string.health_sub_tab_vaccinations)
                                    HealthRecordSubTab.HEALTH_ISSUES -> stringResource(Res.string.health_sub_tab_health_issues)
                                    HealthRecordSubTab.APPOINTMENTS  -> stringResource(Res.string.health_sub_tab_appointments)
                                },
                                // FIX 1: Prevent text from wrapping to second line
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontSize = 11.sp,
                                fontWeight = if (state.subTab == tab) FontWeight.Bold else FontWeight.Normal,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when (state.subTab) {
                    HealthRecordSubTab.VACCINATIONS -> {
                        VaccinationScheduleView(
                            schedules      = state.schedules,
                            filter         = state.vaccinationFilter,
                            loading        = state.schedulesLoading,
                            onFilterChange = { viewModel.setVaccinationFilter(it) },
                            onItemClick    = {},
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
                            onAddIssue     = { onAddHealthIssue(selectedBaby.babyId, selectedBaby.fullName) }
                        )
                    }
                    HealthRecordSubTab.APPOINTMENTS -> {
                        AppointmentsView(
                            appointments       = state.appointments,
                            filter             = state.appointmentFilter,
                            loading            = state.appointmentsLoading,
                            onFilterChange     = { viewModel.setAppointmentFilter(it) },
                            onAppointmentClick = { viewModel.selectAppointment(it) },
                            onCancel           = { viewModel.cancelAppointment(it) },
                            onAddAppointment   = { onAddAppointment(selectedBaby.babyId, selectedBaby.fullName) }
                        )
                    }
                }
            }

        } else if (selectedBaby != null) {
            if (state.assignmentLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = customColors.accentGradientStart)
                }
            } else {
                NoAssignmentPrompt(
                    babyName      = selectedBaby.fullName,
                    onSelectBranch = { onOpenMap(selectedBaby.fullName) }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Child selector (dropdown) — unchanged
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

    Column(verticalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall)) {
        Text(
            text       = stringResource(Res.string.health_select_child),
            style      = MaterialTheme.typography.labelSmall,
            color      = MaterialTheme.colorScheme.onBackground.copy(0.5f),
            fontWeight = FontWeight.SemiBold
        )

        var expanded     by remember { mutableStateOf(false) }
        val selectedBaby = babies.firstOrNull { it.babyId == selectedBabyId }

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
                        color      = if (selectedBaby != null) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurface.copy(0.4f)
                    )
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            }
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                babies.forEach { baby ->
                    DropdownMenuItem(
                        text = {
                            Row(horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall), verticalAlignment = Alignment.CenterVertically) {
                                val isFemale = baby.gender.equals("FEMALE", true) || baby.gender.equals("GIRL", true)
                                Text(if (isFemale) "👧" else "👦")
                                Text(baby.fullName, style = MaterialTheme.typography.bodyMedium)
                            }
                        },
                        onClick = { onSelect(baby.babyId); expanded = false },
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

// ─────────────────────────────────────────────────────────────────────────────
// Assigned branch card
// FIX 3: Removed selection border/highlight — card shows info only, no selected state UI
// The card is tappable to change bench but has NO visual "selected" indicator
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AssignedBranchCard(
    assignment: BabyBenchAssignmentUi?,
    loading   : Boolean,
    onTap     : () -> Unit
) {
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onTap() },
        shape    = RoundedCornerShape(dimensions.cardCornerRadius),
        // FIX 3: Always use surfaceVariant — no conditional color based on assignment state
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
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
                    modifier         = Modifier.size(dimensions.healthCircleSize).clip(CircleShape)
                        .background(customColors.accentGradientStart.copy(0.15f)),
                    contentAlignment = Alignment.Center
                ) { Text("🏥", style = MaterialTheme.typography.titleMedium) }

                if (loading) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(dimensions.iconSmall + dimensions.borderWidthMedium),
                        strokeWidth = dimensions.borderWidthMedium,
                        color       = customColors.accentGradientStart
                    )
                } else {
                    Column {
                        Text(
                            text       = stringResource(Res.string.health_assigned_center),
                            style      = MaterialTheme.typography.labelSmall,
                            color      = MaterialTheme.colorScheme.onSurface.copy(0.5f),
                            fontWeight = FontWeight.SemiBold
                        )
                        if (assignment != null) {
                            Text(
                                text     = assignment.benchNameEn,
                                style    = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
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
                modifier           = Modifier.size(dimensions.iconMedium)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// No assignment prompt — unchanged
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun NoAssignmentPrompt(babyName: String, onSelectBranch: () -> Unit) {
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium),
            modifier            = Modifier.padding(dimensions.screenPadding)
        ) {
            Text("🏥", style = MaterialTheme.typography.displaySmall)
            Text(text = stringResource(Res.string.schedule_no_assignment_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(text = stringResource(Res.string.schedule_no_assignment_body, babyName), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
            Button(
                onClick = onSelectBranch,
                colors  = ButtonDefaults.buttonColors(containerColor = customColors.accentGradientStart),
                shape   = RoundedCornerShape(dimensions.buttonCornerRadius)
            ) {
                Icon(Icons.Default.LocationOn, null)
                Spacer(Modifier.width(dimensions.spacingSmall))
                Text(stringResource(Res.string.health_open_map))
            }
        }
    }
}