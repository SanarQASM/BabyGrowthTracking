package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import babygrowthtrackingapplication.composeapp.generated.resources.Res
import babygrowthtrackingapplication.composeapp.generated.resources.*
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model.*
import org.example.project.babygrowthtrackingapplication.theme.*
import org.jetbrains.compose.resources.stringResource

// ─────────────────────────────────────────────────────────────────────────────
// VisionMotorScreen — بینین + جووڵە (Seeing + Moving)
// Screen 1 of Child Development Tracker
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisionMotorScreen(
    babyId       : String,
    babyName     : String,
    babyAgeMonths: Int,
    viewModel    : VisionMotorViewModel,
    onBack       : () -> Unit
) {
    val state        = viewModel.uiState
    val customColors = MaterialTheme.customColors
    val dimensions   = LocalDimensions.current
    val isLandscape  = LocalIsLandscape.current
    val snackbar     = remember { SnackbarHostState() }

    val msgSaved = stringResource(Res.string.child_dev_saved)
    val errGeneric = stringResource(Res.string.child_dev_error_generic)

    LaunchedEffect(babyId) { viewModel.load(babyId, babyAgeMonths) }

    LaunchedEffect(state.successMessage) {
        state.successMessage?.let {
            snackbar.showSnackbar(if (it == "MSG_SAVED") msgSaved else it)
            viewModel.clearSuccess()
        }
    }
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbar.showSnackbar(it.ifBlank { errGeneric })
            viewModel.clearError()
        }
    }

    // Edit panel slide-in overlay
    if (state.editingMonth != null && state.editingState != null) {
        VisionMotorEditPanel(
            state        = state,
            viewModel    = viewModel,
            babyId       = babyId,
            customColors = customColors,
            dimensions   = dimensions
        )
        return
    }

    Scaffold(
        snackbarHost   = { SnackbarHost(snackbar) },
        topBar         = {
            ChildDevTopBar(
                title        = stringResource(Res.string.child_dev_vision_motor_title),
                subtitle     = babyName,
                emoji        = stringResource(Res.string.child_dev_vision_emoji),
                onBack       = onBack,
                customColors = customColors,
                dimensions   = dimensions
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(listOf(
                        customColors.accentGradientStart.copy(0.12f),
                        customColors.accentGradientEnd.copy(0.06f),
                        MaterialTheme.colorScheme.background
                    ))
                )
                .padding(padding)
        ) {
            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator(color = customColors.accentGradientStart)
                }
            } else {
                if (isLandscape) {
                    VisionMotorLandscapeLayout(state, viewModel, customColors, dimensions)
                } else {
                    VisionMotorPortraitLayout(state, viewModel, customColors, dimensions)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Portrait Layout — milestone cards stacked vertically
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun VisionMotorPortraitLayout(
    state       : VisionMotorUiState,
    viewModel   : VisionMotorViewModel,
    customColors: CustomColors,
    dimensions  : Dimensions
) {
    LazyColumn(
        contentPadding      = PaddingValues(
            horizontal = dimensions.screenPadding,
            vertical   = dimensions.spacingMedium
        ),
        verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium),
        modifier            = Modifier.fillMaxSize()
    ) {
        // Header card
        item {
            ChildDevHeaderCard(
                title        = stringResource(Res.string.child_dev_vision_motor_title),
                subtitle     = stringResource(Res.string.child_dev_vision_motor_subtitle),
                emoji        = stringResource(Res.string.child_dev_vision_emoji),
                customColors = customColors,
                dimensions   = dimensions
            )
        }

        // Milestone cards for each month
        VISION_MOTOR_MILESTONE_MONTHS.forEach { month ->
            item {
                VisionMotorMilestoneCard(
                    month        = month,
                    state        = state,
                    viewModel    = viewModel,
                    customColors = customColors,
                    dimensions   = dimensions
                )
            }
        }

        item { Spacer(Modifier.height(dimensions.spacingXXLarge)) }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Landscape Layout — side panel + content
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun VisionMotorLandscapeLayout(
    state       : VisionMotorUiState,
    viewModel   : VisionMotorViewModel,
    customColors: CustomColors,
    dimensions  : Dimensions
) {
    Row(Modifier.fillMaxSize()) {
        // Left panel summary
        Column(
            modifier = Modifier
                .width(220.dp)
                .fillMaxHeight()
                .background(customColors.accentGradientStart.copy(0.55f))
                .verticalScroll(rememberScrollState())
                .padding(dimensions.spacingMedium),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
        ) {
            Spacer(Modifier.height(dimensions.spacingSmall))
            Text(stringResource(Res.string.child_dev_vision_emoji),
                style = MaterialTheme.typography.displaySmall)
            Text(
                stringResource(Res.string.child_dev_vision_motor_title),
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onPrimary,
                textAlign  = TextAlign.Center
            )
            Spacer(Modifier.height(dimensions.spacingSmall))
            // Progress chips
            VISION_MOTOR_MILESTONE_MONTHS.forEach { month ->
                val enabled  = viewModel.isMonthEnabled(month)
                val hasData  = state.savedRecords.containsKey(month)
                val color    = when {
                    !enabled -> MaterialTheme.colorScheme.onSurface.copy(0.3f)
                    hasData  -> Color(0xFF22C55E)
                    else     -> customColors.accentGradientEnd
                }
                Surface(
                    shape = RoundedCornerShape(50),
                    color = color.copy(0.2f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(Res.string.child_dev_month_label, month),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                        style    = MaterialTheme.typography.labelSmall,
                        color    = if (!enabled)
                            MaterialTheme.colorScheme.onSurface.copy(0.4f)
                        else color,
                        fontWeight = FontWeight.SemiBold,
                        textAlign  = TextAlign.Center
                    )
                }
            }
        }

        // Right content
        LazyColumn(
            contentPadding      = PaddingValues(dimensions.spacingMedium),
            verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium),
            modifier            = Modifier.weight(1f).fillMaxHeight()
        ) {
            VISION_MOTOR_MILESTONE_MONTHS.forEach { month ->
                item {
                    VisionMotorMilestoneCard(
                        month        = month,
                        state        = state,
                        viewModel    = viewModel,
                        customColors = customColors,
                        dimensions   = dimensions
                    )
                }
            }
            item { Spacer(Modifier.height(dimensions.spacingXXLarge)) }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Milestone Card — one per month (1, 3, 6, 9, 12)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun VisionMotorMilestoneCard(
    month       : Int,
    state       : VisionMotorUiState,
    viewModel   : VisionMotorViewModel,
    customColors: CustomColors,
    dimensions  : Dimensions
) {
    val isEnabled = viewModel.isMonthEnabled(month)
    val record    = state.savedRecords[month]
    val hasData   = record != null
    val statusColor = when {
        !isEnabled -> MaterialTheme.colorScheme.onSurface.copy(0.3f)
        hasData    -> Color(0xFF22C55E)
        else       -> customColors.warning
    }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(dimensions.cardCornerRadius),
        colors    = CardDefaults.cardColors(
            containerColor = if (isEnabled) customColors.glassBackground
            else MaterialTheme.colorScheme.surfaceVariant.copy(0.3f)
        ),
        elevation = CardDefaults.cardElevation(if (isEnabled) 2.dp else 0.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(dimensions.spacingMedium)) {
            // ── Header row ────────────────────────────────────────────────────
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall),
                modifier              = Modifier.fillMaxWidth()
            ) {
                // Month badge
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(statusColor.copy(0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text  = "$month",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isEnabled) statusColor
                        else MaterialTheme.colorScheme.onSurface.copy(0.4f)
                    )
                }

                Column(Modifier.weight(1f)) {
                    Text(
                        text = stringResource(Res.string.child_dev_month_label, month),
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color      = if (isEnabled) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurface.copy(0.4f)
                    )
                    Text(
                        text = when {
                            !isEnabled -> stringResource(Res.string.child_dev_locked_hint)
                            hasData    -> stringResource(Res.string.child_dev_status_recorded)
                            else       -> stringResource(Res.string.child_dev_status_not_recorded)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = statusColor
                    )
                }

                if (!isEnabled) {
                    Icon(
                        Icons.Default.Lock, null,
                        tint     = MaterialTheme.colorScheme.onSurface.copy(0.3f),
                        modifier = Modifier.size(dimensions.iconSmall)
                    )
                } else {
                    IconButton(
                        onClick  = { viewModel.startEditing(month) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            if (hasData) Icons.Default.Edit else Icons.Default.Add,
                            contentDescription = null,
                            tint     = customColors.accentGradientStart,
                            modifier = Modifier.size(dimensions.iconMedium)
                        )
                    }
                }
            }

            // ── Checklist preview (read-only) ──────────────────────────────
            if (isEnabled && hasData) {
                Spacer(Modifier.height(dimensions.spacingSmall))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(0.07f))
                Spacer(Modifier.height(dimensions.spacingSmall))
                VisionMotorChecklistPreview(month = month, record = record!!, dimensions = dimensions)
            }

            // ── Locked hint ────────────────────────────────────────────────
            if (!isEnabled) {
                Spacer(Modifier.height(dimensions.spacingXSmall))
                Text(
                    text  = stringResource(Res.string.child_dev_locked_desc, month),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.35f),
                    modifier = Modifier.padding(start = 48.dp)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Checklist Preview (read-only summary dots)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun VisionMotorChecklistPreview(
    month     : Int,
    record    : VisionMotorMonthState,
    dimensions: Dimensions
) {
    val items: List<Pair<String, Boolean?>> = when (month) {
        1  -> listOf(
            "m1_1" to record.m1HeadMovesFollowsLight,
            "m1_2" to record.m1TracksPeopleObjects,
            "m1_3" to record.m1FollowsFlashlight,
        )
        3  -> listOf(
            "m3_1" to record.m3Head180Tracking,
            "m3_2" to record.m3AttentiveFaceTracking,
            "m3_3" to record.m3WatchesOwnHands,
            "m3_4" to record.m3RecognizesMother,
            "m3_5" to record.m3HandsOpenReflex,
        )
        6  -> listOf(
            "m6_1" to record.m6EyesHeadFullRange,
            "m6_2" to record.m6FollowsPersonAcrossRoom,
            "m6_3" to record.m6SmilesAtMirror,
            "m6_4" to record.m6ReachesForDroppedObject,
            "m6_5" to record.m6TransfersObjects,
        )
        9  -> listOf(
            "m9_1" to record.m9KeenVisualAttention,
            "m9_2" to record.m9PincerGrasp,
            "m9_3" to record.m9ReachesDesiredObjects,
            "m9_4" to record.m9AttentionSpan,
        )
        12 -> listOf(
            "m12_1" to record.m12NeatPincerGrasp,
            "m12_2" to record.m12PlaysWithToys,
            "m12_3" to record.m12ReleasesObjects,
            "m12_4" to record.m12RecognizesFamiliarPeople,
            "m12_5" to record.m12GetsAttentionByTugging,
        )
        else -> emptyList()
    }

    val checkedCount = items.count { it.second == true }
    val total        = items.size

    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall),
        modifier              = Modifier.fillMaxWidth()
    ) {
        // Progress dots
        items.forEach { (_, value) ->
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        when (value) {
                            true  -> Color(0xFF22C55E)
                            false -> MaterialTheme.colorScheme.error.copy(0.7f)
                            null  -> MaterialTheme.colorScheme.onSurface.copy(0.2f)
                        }
                    )
            )
        }
        Spacer(Modifier.weight(1f))
        Text(
            text  = "$checkedCount / $total",
            style = MaterialTheme.typography.labelSmall,
            color = if (checkedCount == total) Color(0xFF22C55E)
            else MaterialTheme.colorScheme.onSurface.copy(0.5f),
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Edit Panel — full-screen overlay with checkboxes
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VisionMotorEditPanel(
    state       : VisionMotorUiState,
    viewModel   : VisionMotorViewModel,
    babyId      : String,
    customColors: CustomColors,
    dimensions  : Dimensions
) {
    val month     = state.editingMonth ?: return
    val editing   = state.editingState ?: return
    val snackbar  = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            stringResource(Res.string.child_dev_vision_motor_title),
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color      = customColors.accentGradientStart
                        )
                        Text(
                            stringResource(Res.string.child_dev_month_label, month),
                            style = MaterialTheme.typography.bodySmall,
                            color = customColors.accentGradientStart.copy(0.6f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = viewModel::cancelEditing) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null,
                            tint = customColors.accentGradientStart)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = customColors.accentGradientStart.copy(0.12f)
                )
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(
                    customColors.accentGradientStart.copy(0.12f),
                    MaterialTheme.colorScheme.background
                )))
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Instructions card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(listOf(
                                customColors.accentGradientStart.copy(0.6f),
                                customColors.accentGradientEnd.copy(0.45f)
                            )),
                            RoundedCornerShape(
                                bottomStart = dimensions.cardCornerRadius,
                                bottomEnd   = dimensions.cardCornerRadius
                            )
                        )
                        .padding(
                            horizontal = dimensions.spacingLarge,
                            vertical   = dimensions.spacingMedium
                        )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)) {
                        Text(stringResource(Res.string.child_dev_vision_emoji),
                            style = MaterialTheme.typography.titleLarge)
                        Column {
                            Text(
                                stringResource(Res.string.child_dev_vision_motor_subtitle),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimary.copy(0.9f)
                            )
                            Text(
                                stringResource(Res.string.child_dev_instruction),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary.copy(0.7f)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(dimensions.spacingMedium))

                // Checklist items
                Column(
                    modifier = Modifier.padding(horizontal = dimensions.screenPadding),
                    verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
                ) {
                    when (month) {
                        1  -> VisionMotorMonth1Fields(editing, viewModel, customColors, dimensions)
                        3  -> VisionMotorMonth3Fields(editing, viewModel, customColors, dimensions)
                        6  -> VisionMotorMonth6Fields(editing, viewModel, customColors, dimensions)
                        9  -> VisionMotorMonth9Fields(editing, viewModel, customColors, dimensions)
                        12 -> VisionMotorMonth12Fields(editing, viewModel, customColors, dimensions)
                    }

                    Spacer(Modifier.height(dimensions.spacingMedium))

                    // Save / Cancel buttons
                    ChildDevSaveButton(
                        isSaving     = state.isSaving,
                        onSave       = { viewModel.save(babyId) },
                        onCancel     = viewModel::cancelEditing,
                        customColors = customColors,
                        dimensions   = dimensions
                    )

                    Spacer(Modifier.height(dimensions.spacingXXLarge))
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Month-specific checkbox fields
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun VisionMotorMonth1Fields(
    s: VisionMotorMonthState, vm: VisionMotorViewModel,
    cc: CustomColors, d: Dimensions
) {
    DevCheckItem(
        label    = stringResource(Res.string.vm_m1_1),
        checked  = s.m1HeadMovesFollowsLight,
        onToggle = { vm.updateField { it.copy(m1HeadMovesFollowsLight = it.m1HeadMovesFollowsLight?.not() ?: true) } },
        cc = cc, d = d
    )
    DevCheckItem(
        label    = stringResource(Res.string.vm_m1_2),
        checked  = s.m1TracksPeopleObjects,
        onToggle = { vm.updateField { it.copy(m1TracksPeopleObjects = it.m1TracksPeopleObjects?.not() ?: true) } },
        cc = cc, d = d
    )
    DevCheckItem(
        label    = stringResource(Res.string.vm_m1_3),
        checked  = s.m1FollowsFlashlight,
        onToggle = { vm.updateField { it.copy(m1FollowsFlashlight = it.m1FollowsFlashlight?.not() ?: true) } },
        cc = cc, d = d
    )
}

@Composable
private fun VisionMotorMonth3Fields(
    s: VisionMotorMonthState, vm: VisionMotorViewModel,
    cc: CustomColors, d: Dimensions
) {
    DevCheckItem(stringResource(Res.string.vm_m3_1), s.m3Head180Tracking,
        { vm.updateField { it.copy(m3Head180Tracking = it.m3Head180Tracking?.not() ?: true) } }, cc, d)
    DevCheckItem(stringResource(Res.string.vm_m3_2), s.m3AttentiveFaceTracking,
        { vm.updateField { it.copy(m3AttentiveFaceTracking = it.m3AttentiveFaceTracking?.not() ?: true) } }, cc, d)
    DevCheckItem(stringResource(Res.string.vm_m3_3), s.m3WatchesOwnHands,
        { vm.updateField { it.copy(m3WatchesOwnHands = it.m3WatchesOwnHands?.not() ?: true) } }, cc, d)
    DevCheckItem(stringResource(Res.string.vm_m3_4), s.m3RecognizesMother,
        { vm.updateField { it.copy(m3RecognizesMother = it.m3RecognizesMother?.not() ?: true) } }, cc, d)
    DevCheckItem(stringResource(Res.string.vm_m3_5), s.m3HandsOpenReflex,
        { vm.updateField { it.copy(m3HandsOpenReflex = it.m3HandsOpenReflex?.not() ?: true) } }, cc, d)
}

@Composable
private fun VisionMotorMonth6Fields(
    s: VisionMotorMonthState, vm: VisionMotorViewModel,
    cc: CustomColors, d: Dimensions
) {
    DevCheckItem(stringResource(Res.string.vm_m6_1), s.m6EyesHeadFullRange,
        { vm.updateField { it.copy(m6EyesHeadFullRange = it.m6EyesHeadFullRange?.not() ?: true) } }, cc, d)
    DevCheckItem(stringResource(Res.string.vm_m6_2), s.m6FollowsPersonAcrossRoom,
        { vm.updateField { it.copy(m6FollowsPersonAcrossRoom = it.m6FollowsPersonAcrossRoom?.not() ?: true) } }, cc, d)
    DevCheckItem(stringResource(Res.string.vm_m6_3), s.m6SmilesAtMirror,
        { vm.updateField { it.copy(m6SmilesAtMirror = it.m6SmilesAtMirror?.not() ?: true) } }, cc, d)
    DevCheckItem(stringResource(Res.string.vm_m6_4), s.m6ReachesForDroppedObject,
        { vm.updateField { it.copy(m6ReachesForDroppedObject = it.m6ReachesForDroppedObject?.not() ?: true) } }, cc, d)
    DevCheckItem(stringResource(Res.string.vm_m6_5), s.m6TransfersObjects,
        { vm.updateField { it.copy(m6TransfersObjects = it.m6TransfersObjects?.not() ?: true) } }, cc, d)
}

@Composable
private fun VisionMotorMonth9Fields(
    s: VisionMotorMonthState, vm: VisionMotorViewModel,
    cc: CustomColors, d: Dimensions
) {
    DevCheckItem(stringResource(Res.string.vm_m9_1), s.m9KeenVisualAttention,
        { vm.updateField { it.copy(m9KeenVisualAttention = it.m9KeenVisualAttention?.not() ?: true) } }, cc, d)
    DevCheckItem(stringResource(Res.string.vm_m9_2), s.m9PincerGrasp,
        { vm.updateField { it.copy(m9PincerGrasp = it.m9PincerGrasp?.not() ?: true) } }, cc, d)
    DevCheckItem(stringResource(Res.string.vm_m9_3), s.m9ReachesDesiredObjects,
        { vm.updateField { it.copy(m9ReachesDesiredObjects = it.m9ReachesDesiredObjects?.not() ?: true) } }, cc, d)
    DevCheckItem(stringResource(Res.string.vm_m9_4), s.m9AttentionSpan,
        { vm.updateField { it.copy(m9AttentionSpan = it.m9AttentionSpan?.not() ?: true) } }, cc, d)
}

@Composable
private fun VisionMotorMonth12Fields(
    s: VisionMotorMonthState, vm: VisionMotorViewModel,
    cc: CustomColors, d: Dimensions
) {
    DevCheckItem(stringResource(Res.string.vm_m12_1), s.m12NeatPincerGrasp,
        { vm.updateField { it.copy(m12NeatPincerGrasp = it.m12NeatPincerGrasp?.not() ?: true) } }, cc, d)
    DevCheckItem(stringResource(Res.string.vm_m12_2), s.m12PlaysWithToys,
        { vm.updateField { it.copy(m12PlaysWithToys = it.m12PlaysWithToys?.not() ?: true) } }, cc, d)
    DevCheckItem(stringResource(Res.string.vm_m12_3), s.m12ReleasesObjects,
        { vm.updateField { it.copy(m12ReleasesObjects = it.m12ReleasesObjects?.not() ?: true) } }, cc, d)
    DevCheckItem(stringResource(Res.string.vm_m12_4), s.m12RecognizesFamiliarPeople,
        { vm.updateField { it.copy(m12RecognizesFamiliarPeople = it.m12RecognizesFamiliarPeople?.not() ?: true) } }, cc, d)
    DevCheckItem(stringResource(Res.string.vm_m12_5), s.m12GetsAttentionByTugging,
        { vm.updateField { it.copy(m12GetsAttentionByTugging = it.m12GetsAttentionByTugging?.not() ?: true) } }, cc, d)
}