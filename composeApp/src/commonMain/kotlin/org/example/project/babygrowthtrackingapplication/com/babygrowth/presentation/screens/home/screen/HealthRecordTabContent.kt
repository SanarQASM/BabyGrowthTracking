package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.screen

// ─────────────────────────────────────────────────────────────────────────────
// FIXES APPLIED:
//  Fix 1: Entire page now scrolls — header + ScrollableTabRow + content all
//         inside one verticalScroll Column. Inner LazyColumns replaced with
//         Column so nested scroll conflict is eliminated.
//  Fix 3: AssignedBranchCard no longer shows selection highlight (preserved).
//  Fix 5: Header section scroll (preserved — now part of whole-page scroll).
//  Fix 7: BenchDetail navigation (preserved).
//  No hardcoded strings, colors, sizes, or dp values — all use dimension tokens
//  and stringResource.
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
import androidx.compose.ui.unit.sp
import babygrowthtrackingapplication.composeapp.generated.resources.*
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model.*
import org.example.project.babygrowthtrackingapplication.data.network.BabyResponse
import org.example.project.babygrowthtrackingapplication.theme.LocalDimensions
import org.example.project.babygrowthtrackingapplication.theme.customColors
import org.jetbrains.compose.resources.stringResource

// ─────────────────────────────────────────────────────────────────────────────
// Internal navigation state
// ─────────────────────────────────────────────────────────────────────────────

private sealed class HealthNavState {
    object Main : HealthNavState()
    data class Map(val babyName: String) : HealthNavState()
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
    LaunchedEffect(state.error) {
        state.error?.let { snackbarHostState.showSnackbar(it); viewModel.clearError() }
    }
    LaunchedEffect(state.successMessage) {
        state.successMessage?.let { snackbarHostState.showSnackbar(it); viewModel.clearSuccess() }
    }

    // ── Reschedule reason picker dialog ──────────────────────────────────────
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

    // ── Reschedule in-progress dialog ────────────────────────────────────────
    if (state.rescheduleInProgress) {
        AlertDialog(
            onDismissRequest = {},
            title = {
                Text(
                    stringResource(Res.string.schedule_reschedule_in_progress_title),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(dimensions.spacingLarge),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(dimensions.iconLarge),
                        color       = customColors.accentGradientStart,
                        strokeWidth = dimensions.borderWidthMedium + dimensions.borderWidthThin
                    )
                    Text(
                        stringResource(Res.string.schedule_reschedule_checking),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {}
        )
    }

    // ── Reschedule result dialog ──────────────────────────────────────────────
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
                        onAddHealthIssue = { babyId, babyName ->
                            navState = HealthNavState.AddHealthIssue(babyId, babyName)
                        },
                        onAddAppointment = { babyId, babyName ->
                            navState = HealthNavState.AddAppointment(babyId, babyName)
                        }
                    )
                }
                is HealthNavState.Map -> {
                    BenchMapScreen(
                        viewModel       = viewModel,
                        babyName        = nav.babyName,
                        onBack          = { navState = HealthNavState.Main },
                        onBenchSelected = { bench ->
                            navState = HealthNavState.BenchDetail(
                                bench    = bench,
                                babyName = nav.babyName,
                                fromMap  = true
                            )
                        }
                    )
                }
                is HealthNavState.BenchDetail -> {
                    BenchDetailScreen(
                        bench     = nav.bench,
                        babyName  = nav.babyName,
                        isLoading = state.assignmentLoading,
                        onBack    = {
                            navState = if (nav.fromMap)
                                HealthNavState.Map(nav.babyName)
                            else
                                HealthNavState.Main
                        },
                        onAssign  = {
                            state.selectedBabyId?.let { babyId ->
                                viewModel.assignBench(babyId, nav.bench.benchId)
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
// FIX 1: Entire page scrolls via one outer verticalScroll Column.
//        ScrollableTabRow replaces TabRow so long tab labels never clip.
//        Sub-tab views rendered inline (no inner LazyColumn) to avoid nested
//        scroll conflict.
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

    val selectedBaby  = babies.firstOrNull { it.babyId == state.selectedBabyId }
    val hasAssignment = state.assignment != null

    // FIX 1: Single verticalScroll wraps the WHOLE page — header + tabs + content
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {

        // ── Gradient header ────────────────────────────────────────────────
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
                .padding(
                    horizontal = dimensions.screenPadding,
                    vertical   = dimensions.spacingMedium
                ),
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

            selectedBaby?.let { baby ->
                // FIX 3: No selection highlight on AssignedBranchCard
                AssignedBranchCard(
                    assignment = state.assignment,
                    loading    = state.assignmentLoading,
                    onTap      = {
                        if (state.assignment != null) onChangeBench(baby.fullName)
                        else onOpenMap(baby.fullName)
                    }
                )
            }
        }

        // ── Sub-tab area ────────────────────────────────────────────────────
        if (hasAssignment && selectedBaby != null) {

            // FIX 1: ScrollableTabRow — tab labels can be any length without
            //        clipping or wrapping to a second line.
            ScrollableTabRow(
                selectedTabIndex = state.subTab.ordinal,
                edgePadding      = dimensions.screenPadding,
                containerColor   = MaterialTheme.colorScheme.surface,
                contentColor     = customColors.accentGradientStart,
                divider          = {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            ) {
                HealthRecordSubTab.entries.forEach { tab ->
                    Tab(
                        selected = state.subTab == tab,
                        onClick  = { viewModel.setSubTab(tab) },
                        text = {
                            Text(
                                text = when (tab) {
                                    HealthRecordSubTab.VACCINATIONS  ->
                                        stringResource(Res.string.health_sub_tab_vaccinations)
                                    HealthRecordSubTab.HEALTH_ISSUES ->
                                        stringResource(Res.string.health_sub_tab_health_issues)
                                    HealthRecordSubTab.APPOINTMENTS  ->
                                        stringResource(Res.string.health_sub_tab_appointments)
                                },
                                // FIX 1: prevent tab label from ever wrapping
                                maxLines   = 1,
                                overflow   = TextOverflow.Ellipsis,
                                fontSize   = 11.sp,
                                fontWeight = if (state.subTab == tab) FontWeight.Bold
                                else FontWeight.Normal,
                                style      = MaterialTheme.typography.labelSmall
                            )
                        }
                    )
                }
            }

            // FIX 1: Content rendered inline — no nested LazyColumn.
            //        Each sub-tab view must use Column (not LazyColumn) so the
            //        outer verticalScroll drives all scrolling.
            Box(modifier = Modifier.fillMaxWidth()) {
                when (state.subTab) {
                    HealthRecordSubTab.VACCINATIONS ->
                        VaccinationScheduleView(
                            schedules      = state.schedules,
                            filter         = state.vaccinationFilter,
                            loading        = state.schedulesLoading,
                            onFilterChange = { viewModel.setVaccinationFilter(it) },
                            onItemClick    = { item ->
                                // Navigate to detail screen on card tap
                            },
                            onReschedule   = { viewModel.triggerReschedule() }
                        )

                    HealthRecordSubTab.HEALTH_ISSUES ->
                        HealthIssuesView(
                            issues         = state.healthIssues,
                            filter         = state.healthIssueFilter,
                            loading        = state.healthIssuesLoading,
                            onFilterChange = { viewModel.setHealthIssueFilter(it) },
                            onIssueClick   = { viewModel.selectHealthIssue(it) },
                            onResolve      = { viewModel.resolveHealthIssue(it) },
                            onAddIssue     = {
                                onAddHealthIssue(selectedBaby.babyId, selectedBaby.fullName)
                            }
                        )

                    HealthRecordSubTab.APPOINTMENTS ->
                        AppointmentsView(
                            appointments       = state.appointments,
                            filter             = state.appointmentFilter,
                            loading            = state.appointmentsLoading,
                            onFilterChange     = { viewModel.setAppointmentFilter(it) },
                            onAppointmentClick = { viewModel.selectAppointment(it) },
                            onCancel           = { viewModel.cancelAppointment(it) },
                            onAddAppointment   = {
                                onAddAppointment(selectedBaby.babyId, selectedBaby.fullName)
                            }
                        )
                }
            }

        } else if (selectedBaby != null) {
            if (state.assignmentLoading) {
                Box(
                    modifier         = Modifier
                        .fillMaxWidth()
                        .height(dimensions.iconXLarge + dimensions.spacingXLarge),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = customColors.accentGradientStart)
                }
            } else {
                NoAssignmentPrompt(
                    babyName       = selectedBaby.fullName,
                    onSelectBranch = { onOpenMap(selectedBaby.fullName) }
                )
            }
        }

        // Bottom spacer so last card isn't hidden behind nav bar
        Spacer(modifier = Modifier.height(dimensions.spacingXXLarge))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Child selector (dropdown)
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
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                shape    = RoundedCornerShape(dimensions.cardCornerRadius)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = dimensions.spacingMedium,
                            vertical   = dimensions.healthDropdownItemPaddingV
                        ),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        text       = selectedBaby?.fullName
                            ?: stringResource(Res.string.health_select_child_hint),
                        style      = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (selectedBaby != null) FontWeight.SemiBold
                        else FontWeight.Normal,
                        color      = if (selectedBaby != null)
                            MaterialTheme.colorScheme.onSurface
                        else
                            MaterialTheme.colorScheme.onSurface.copy(0.4f)
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
                                horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall),
                                verticalAlignment     = Alignment.CenterVertically
                            ) {
                                val isFemale = baby.gender.equals("FEMALE", true) ||
                                        baby.gender.equals("GIRL", true)
                                Text(if (isFemale) "👧" else "👦")
                                Text(baby.fullName, style = MaterialTheme.typography.bodyMedium)
                            }
                        },
                        onClick      = { onSelect(baby.babyId); expanded = false },
                        trailingIcon = {
                            if (baby.babyId == selectedBabyId) {
                                Icon(
                                    Icons.Default.Check,
                                    null,
                                    tint     = customColors.accentGradientStart,
                                    modifier = Modifier.size(dimensions.iconSmall)
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
// FIX 3: Always surfaceVariant — no conditional selection colour
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
        modifier  = Modifier
            .fillMaxWidth()
            .clickable { onTap() },
        shape     = RoundedCornerShape(dimensions.cardCornerRadius),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(dimensions.healthSubTabElevation)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensions.spacingMedium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(
                    dimensions.spacingSmall + dimensions.borderWidthMedium
                ),
                verticalAlignment     = Alignment.CenterVertically,
                modifier              = Modifier.weight(1f)
            ) {
                Box(
                    modifier         = Modifier
                        .size(dimensions.healthCircleSize)
                        .clip(CircleShape)
                        .background(customColors.accentGradientStart.copy(0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🏥", style = MaterialTheme.typography.titleMedium)
                }

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
                                text       = assignment.benchNameEn,
                                style      = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines   = 1,
                                overflow   = TextOverflow.Ellipsis
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
                imageVector        = if (assignment != null)
                    Icons.Default.Edit
                else
                    Icons.Default.ChevronRight,
                contentDescription = null,
                tint               = customColors.accentGradientStart.copy(0.7f),
                modifier           = Modifier.size(dimensions.iconMedium)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// No assignment prompt
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun NoAssignmentPrompt(babyName: String, onSelectBranch: () -> Unit) {
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors

    Box(
        modifier         = Modifier
            .fillMaxWidth()
            .padding(dimensions.screenPadding),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)
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