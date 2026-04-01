package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.screen

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model.ChildIllnessesUiState
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model.ChildIllnessesViewModel
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model.ChildIllnessUiItem
import org.example.project.babygrowthtrackingapplication.theme.*
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

// ─────────────────────────────────────────────────────────────────────────────
// Root Screen
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun ChildIllnessesScreen(
    babyId    : String,
    babyName  : String,
    viewModel : ChildIllnessesViewModel,
    onBack    : () -> Unit
) {
    val state        = viewModel.uiState
    val customColors = MaterialTheme.customColors
    val dimensions   = LocalDimensions.current
    val isLandscape  = LocalIsLandscape.current
    val snackbar     = remember { SnackbarHostState() }

    // ── Resolve ViewModel sentinel messages into localized strings ────────────
    val msgUpdated   = stringResource(Res.string.child_illnesses_success_updated)
    val msgAdded     = stringResource(Res.string.child_illnesses_success_added)
    val msgDeleted   = stringResource(Res.string.child_illnesses_success_deleted)
    val msgResolved  = stringResource(Res.string.child_illnesses_success_resolved)
    val msgActivated = stringResource(Res.string.child_illnesses_success_activated)
    val errLoad      = stringResource(Res.string.child_illnesses_error_load)
    val errSave      = stringResource(Res.string.child_illnesses_error_save)
    val errDelete    = stringResource(Res.string.child_illnesses_error_delete)
    val errUpdate    = stringResource(Res.string.child_illnesses_error_update)

    fun resolveMessage(raw: String?): String? = when {
        raw == null          -> null
        raw == "MSG_UPDATED" -> msgUpdated
        raw == "MSG_ADDED"   -> msgAdded
        raw == "MSG_DELETED" -> msgDeleted
        raw == "MSG_RESOLVED"  -> msgResolved
        raw == "MSG_ACTIVATED" -> msgActivated
        raw.startsWith("ERR_LOAD:")   -> errLoad.replace("%1\$s", raw.removePrefix("ERR_LOAD:"))
        raw.startsWith("ERR_SAVE:")   -> errSave.replace("%1\$s", raw.removePrefix("ERR_SAVE:"))
        raw.startsWith("ERR_DELETE:") -> errDelete.replace("%1\$s", raw.removePrefix("ERR_DELETE:"))
        raw.startsWith("ERR_UPDATE:") -> errUpdate.replace("%1\$s", raw.removePrefix("ERR_UPDATE:"))
        else                 -> raw   // API error messages passed through verbatim
    }

    LaunchedEffect(babyId) { viewModel.loadIllnesses(babyId) }

    LaunchedEffect(state.successMessage) {
        resolveMessage(state.successMessage)?.let {
            snackbar.showSnackbar(it)
            viewModel.clearSuccess()
        }
    }
    LaunchedEffect(state.errorMessage) {
        resolveMessage(state.errorMessage)?.let {
            snackbar.showSnackbar(it)
            viewModel.clearError()
        }
    }

    // ── Date Picker ───────────────────────────────────────────────────────────
    if (state.showDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.formDiagnosisDateEpoch
                ?.let { it * 86_400_000L }
                ?: Clock.System.now().toEpochMilliseconds(),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) =
                    utcTimeMillis <= Clock.System.now().toEpochMilliseconds()
            }
        )

        DatePickerDialog(
            onDismissRequest = viewModel::dismissDatePicker,
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis
                        ?.let { millis -> viewModel.onDateSelected(millis / 86_400_000L) }
                        ?: viewModel.dismissDatePicker()
                }) {
                    Text(stringResource(Res.string.add_baby_date_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissDatePicker) {
                    Text(stringResource(Res.string.btn_cancel))
                }
            }
        ) {
            DatePicker(
                state          = pickerState,
                showModeToggle = true,
                title = {
                    // Localised title shown inside the picker header
                    Text(
                        text     = stringResource(Res.string.child_illnesses_field_date_label),
                        modifier = Modifier.padding(start = 24.dp, end = 12.dp, top = 16.dp)
                    )
                }
            )
        }
    }

    // ── Delete confirm ────────────────────────────────────────────────────────
    if (state.showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = viewModel::dismissDeleteConfirm,
            icon = {
                Text(
                    stringResource(Res.string.child_illnesses_emoji_hospital),
                    style = MaterialTheme.typography.displaySmall
                )
            },
            title = {
                Text(
                    stringResource(Res.string.child_illnesses_delete_title),
                    fontWeight = FontWeight.Bold,
                    textAlign  = TextAlign.Center,
                    style      = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Text(
                    stringResource(Res.string.child_illnesses_delete_message),
                    textAlign = TextAlign.Center,
                    style     = MaterialTheme.typography.bodyMedium,
                    color     = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick  = { viewModel.deleteIllness() },
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error),
                    shape    = RoundedCornerShape(dimensions.buttonCornerRadius),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        stringResource(Res.string.child_illnesses_delete_confirm),
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onError
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick  = viewModel::dismissDeleteConfirm,
                    shape    = RoundedCornerShape(dimensions.buttonCornerRadius),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(Res.string.btn_cancel))
                }
            },
            shape          = RoundedCornerShape(dimensions.cardCornerRadius),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    // ── Add / Edit dialog ─────────────────────────────────────────────────────
    if (state.showAddEditDialog) {
        AddEditIllnessDialog(
            state        = state,
            viewModel    = viewModel,
            babyId       = babyId,
            dimensions   = dimensions,
            customColors = customColors
        )
    }

    Scaffold(
        snackbarHost    = { SnackbarHost(snackbar) },
        topBar = {
            ChildIllnessesTopBar(
                babyName     = babyName,
                onBack       = onBack,
                onAdd        = { viewModel.startAdding() },
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
                    Brush.verticalGradient(
                        listOf(
                            customColors.accentGradientStart.copy(0.12f),
                            customColors.accentGradientEnd.copy(0.08f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
                .padding(padding)
        ) {
            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator(color = customColors.accentGradientStart)
                }
                isLandscape -> ChildIllnessesLandscapeLayout(
                    state, viewModel, babyId, customColors, dimensions)
                else -> ChildIllnessesPortraitLayout(
                    state, viewModel, babyId, customColors, dimensions)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Top Bar
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChildIllnessesTopBar(
    babyName    : String,
    onBack      : () -> Unit,
    onAdd       : () -> Unit,
    customColors: CustomColors,
    dimensions  : Dimensions
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    stringResource(Res.string.child_illnesses_title),
                    fontWeight = FontWeight.Bold,
                    style      = MaterialTheme.typography.titleMedium,
                    color      = customColors.accentGradientStart
                )
                Text(
                    babyName,
                    style = MaterialTheme.typography.bodySmall,
                    color = customColors.accentGradientStart.copy(0.6f)
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    stringResource(Res.string.common_back),
                    tint = customColors.accentGradientStart
                )
            }
        },
        actions = {
            IconButton(onClick = onAdd) {
                Icon(
                    Icons.Default.Add,
                    stringResource(Res.string.child_illnesses_add_cd),
                    tint = customColors.accentGradientStart
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor             = customColors.accentGradientStart.copy(0.12f),
            titleContentColor          = customColors.accentGradientStart,
            navigationIconContentColor = customColors.accentGradientStart
        )
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Layouts
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ChildIllnessesPortraitLayout(
    state       : ChildIllnessesUiState,
    viewModel   : ChildIllnessesViewModel,
    babyId      : String,
    customColors: CustomColors,
    dimensions  : Dimensions
) {
    Column(Modifier.fillMaxSize()) {
        Spacer(Modifier.height(dimensions.spacingSmall))
        IllnessSummaryCard(
            illnesses    = state.illnesses,
            customColors = customColors,
            dimensions   = dimensions,
            onAdd        = { viewModel.startAdding() },
            modifier     = Modifier.padding(horizontal = dimensions.screenPadding)
        )
        Spacer(Modifier.height(dimensions.spacingMedium))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(
                    Brush.verticalGradient(listOf(
                        customColors.accentGradientStart.copy(0.55f),
                        customColors.accentGradientEnd.copy(0.40f)
                    )),
                    RoundedCornerShape(
                        topStart = dimensions.cardCornerRadius,
                        topEnd   = dimensions.cardCornerRadius
                    )
                )
                .padding(
                    horizontal = dimensions.spacingLarge,
                    vertical   = dimensions.spacingMedium
                )
        ) {
            if (state.illnesses.isEmpty())
                EmptyIllnessesContent(customColors, dimensions) { viewModel.startAdding() }
            else
                IllnessListContent(state, viewModel, customColors, dimensions)
        }
    }
}

@Composable
private fun ChildIllnessesLandscapeLayout(
    state       : ChildIllnessesUiState,
    viewModel   : ChildIllnessesViewModel,
    babyId      : String,
    customColors: CustomColors,
    dimensions  : Dimensions
) {
    Row(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .width(260.dp).fillMaxHeight()
                .background(customColors.accentGradientStart.copy(0.55f))
                .verticalScroll(rememberScrollState())
                .padding(dimensions.spacingLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)
        ) {
            Spacer(Modifier.height(dimensions.spacingSmall))
            Text(
                stringResource(Res.string.child_illnesses_emoji_hospital),
                style = MaterialTheme.typography.displaySmall
            )
            Text(
                stringResource(Res.string.child_illnesses_title),
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onPrimary,
                textAlign  = TextAlign.Center
            )
            Spacer(Modifier.height(dimensions.spacingSmall))
            Surface(
                shape = RoundedCornerShape(50),
                color = if (state.illnesses.isNotEmpty()) Color(0xFF22C55E).copy(0.2f)
                else MaterialTheme.colorScheme.error.copy(0.2f)
            ) {
                Text(
                    if (state.illnesses.isNotEmpty())
                        stringResource(Res.string.child_illnesses_records_count, state.illnesses.size)
                    else stringResource(Res.string.child_illnesses_no_records),
                    modifier   = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style      = MaterialTheme.typography.labelMedium,
                    color      = if (state.illnesses.isNotEmpty()) Color(0xFF22C55E)
                    else MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Button(
                onClick  = { viewModel.startAdding() },
                shape    = RoundedCornerShape(dimensions.buttonCornerRadius),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onPrimary.copy(0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.Add, null,
                    tint     = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(dimensions.iconSmall)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    stringResource(Res.string.child_illnesses_landscape_add_button),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
        Column(
            modifier = Modifier
                .weight(1f).fillMaxHeight()
                .padding(dimensions.spacingLarge),
            verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)
        ) {
            if (state.illnesses.isEmpty())
                EmptyIllnessesContent(customColors, dimensions) { viewModel.startAdding() }
            else
                IllnessListContent(state, viewModel, customColors, dimensions)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Summary Card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun IllnessSummaryCard(
    illnesses   : List<ChildIllnessUiItem>,
    customColors: CustomColors,
    dimensions  : Dimensions,
    onAdd       : () -> Unit,
    modifier    : Modifier = Modifier
) {
    val activeCount = illnesses.count { it.isActive }
    val hasRecords  = illnesses.isNotEmpty()
    Card(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(dimensions.cardCornerRadius),
        colors    = CardDefaults.cardColors(
            containerColor = if (hasRecords) Color(0xFF22C55E).copy(0.1f)
            else MaterialTheme.colorScheme.errorContainer.copy(0.5f)
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(dimensions.spacingMedium),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(dimensions.spacingSmall))
                    .background(
                        if (hasRecords) Color(0xFF22C55E).copy(0.18f)
                        else MaterialTheme.colorScheme.error.copy(0.15f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (hasRecords) stringResource(Res.string.child_illnesses_emoji_hospital)
                    else stringResource(Res.string.child_illnesses_emoji_warning),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(Res.string.child_illnesses_summary_title),
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    if (hasRecords)
                        stringResource(
                            Res.string.child_illnesses_summary_active_total,
                            activeCount, illnesses.size
                        )
                    else stringResource(Res.string.child_illnesses_summary_empty),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (hasRecords) Color(0xFF22C55E)
                    else MaterialTheme.colorScheme.error
                )
            }
            TextButton(
                onClick = onAdd,
                colors  = ButtonDefaults.textButtonColors(
                    contentColor = customColors.accentGradientStart)
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    stringResource(Res.string.child_illnesses_add_label),
                    fontWeight = FontWeight.SemiBold,
                    style      = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Empty state
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun EmptyIllnessesContent(
    customColors: CustomColors,
    dimensions  : Dimensions,
    onAdd       : () -> Unit
) {
    Column(
        modifier            = Modifier.fillMaxWidth()
            .padding(vertical = dimensions.spacingXLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)
    ) {
        Text(
            stringResource(Res.string.child_illnesses_emoji_hospital),
            style = MaterialTheme.typography.displaySmall
        )
        Text(
            stringResource(Res.string.child_illnesses_empty_title),
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.onPrimary,
            textAlign  = TextAlign.Center
        )
        Text(
            stringResource(Res.string.child_illnesses_empty_desc),
            style     = MaterialTheme.typography.bodyMedium,
            color     = MaterialTheme.colorScheme.onPrimary.copy(0.75f),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(dimensions.spacingSmall))
        Button(
            onClick  = onAdd,
            shape    = RoundedCornerShape(dimensions.buttonCornerRadius),
            colors   = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.onPrimary.copy(0.25f)),
            modifier = Modifier.fillMaxWidth(0.7f)
        ) {
            Icon(
                Icons.Default.Add, null,
                tint     = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(dimensions.iconMedium)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                stringResource(Res.string.child_illnesses_add_first),
                color      = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Illness list
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun IllnessListContent(
    state       : ChildIllnessesUiState,
    viewModel   : ChildIllnessesViewModel,
    customColors: CustomColors,
    dimensions  : Dimensions
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall),
        modifier = Modifier.fillMaxSize()
    ) {
        items(state.illnesses, key = { it.illnessId }) { illness ->
            IllnessCard(
                illness        = illness,
                customColors   = customColors,
                dimensions     = dimensions,
                onEdit         = { viewModel.startEditing(illness) },
                onDelete       = { viewModel.showDeleteConfirm(illness.illnessId) },
                onToggleActive = { viewModel.toggleActive(illness) }
            )
        }
        item { Spacer(Modifier.height(dimensions.spacingXXLarge)) }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Illness Card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun IllnessCard(
    illness       : ChildIllnessUiItem,
    customColors  : CustomColors,
    dimensions    : Dimensions,
    onEdit        : () -> Unit,
    onDelete      : () -> Unit,
    onToggleActive: () -> Unit
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(dimensions.cardCornerRadius),
        colors    = CardDefaults.cardColors(
            containerColor = if (illness.isActive) customColors.glassBackground
            else MaterialTheme.colorScheme.surfaceVariant.copy(0.5f)
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(dimensions.spacingMedium)
        ) {
            // ── Header row ────────────────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
            ) {
                Box(
                    modifier = Modifier.size(10.dp).clip(RoundedCornerShape(50))
                        .background(
                            if (illness.isActive) Color(0xFF22C55E)
                            else MaterialTheme.colorScheme.onSurface.copy(0.3f)
                        )
                )
                Text(
                    illness.illnessName,
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onSurface,
                    modifier   = Modifier.weight(1f)
                )
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (illness.isActive) Color(0xFF22C55E).copy(0.15f)
                    else MaterialTheme.colorScheme.onSurface.copy(0.08f)
                ) {
                    Text(
                        if (illness.isActive)
                            stringResource(Res.string.child_illnesses_status_active)
                        else stringResource(Res.string.child_illnesses_status_resolved),
                        modifier   = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style      = MaterialTheme.typography.labelSmall,
                        color      = if (illness.isActive) Color(0xFF22C55E)
                        else MaterialTheme.colorScheme.onSurface.copy(0.5f),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.height(dimensions.spacingXSmall))

            // ── Diagnosis date — formatted using device locale ─────────────────
            illness.diagnosisDate?.let { isoDate ->
                val monthNames = listOf(
                    stringResource(Res.string.month_jan), stringResource(Res.string.month_feb),
                    stringResource(Res.string.month_mar), stringResource(Res.string.month_apr),
                    stringResource(Res.string.month_may), stringResource(Res.string.month_jun),
                    stringResource(Res.string.month_jul), stringResource(Res.string.month_aug),
                    stringResource(Res.string.month_sep), stringResource(Res.string.month_oct),
                    stringResource(Res.string.month_nov), stringResource(Res.string.month_dec)
                )

                val displayDate = remember(isoDate) {
                    try {
                        val d = LocalDate.parse(isoDate)
                        "${d.day} ${monthNames[d.month.number - 1]} ${d.year}"
                    } catch (_: Exception) { isoDate }
                }
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.CalendarToday, null,
                        modifier = Modifier.size(12.dp),
                        tint     = MaterialTheme.colorScheme.onSurface.copy(0.5f)
                    )
                    Text(
                        stringResource(Res.string.child_illnesses_diagnosed_prefix, displayDate),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                    )
                }
                Spacer(Modifier.height(dimensions.spacingXSmall))
            }

            // ── Notes ─────────────────────────────────────────────────────────
            illness.notes?.let { notes ->
                if (notes.isNotBlank()) {
                    Text(
                        notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.7f)
                    )
                    Spacer(Modifier.height(dimensions.spacingSmall))
                }
            }

            HorizontalDivider(
                color     = MaterialTheme.colorScheme.onSurface.copy(0.08f),
                thickness = 0.5.dp
            )
            Spacer(Modifier.height(dimensions.spacingXSmall))

            // ── Action row ────────────────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onToggleActive,
                    colors  = ButtonDefaults.textButtonColors(
                        contentColor = if (illness.isActive)
                            MaterialTheme.colorScheme.onSurface.copy(0.5f)
                        else Color(0xFF22C55E)
                    )
                ) {
                    Icon(
                        if (illness.isActive) Icons.Default.CheckCircle
                        else Icons.Default.RadioButtonUnchecked,
                        null, modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        if (illness.isActive)
                            stringResource(Res.string.child_illnesses_mark_resolved)
                        else stringResource(Res.string.child_illnesses_mark_active),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.Edit,
                        stringResource(Res.string.child_illnesses_edit_cd),
                        tint     = customColors.accentGradientStart,
                        modifier = Modifier.size(16.dp)
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.Delete,
                        stringResource(Res.string.child_illnesses_delete_cd),
                        tint     = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Add / Edit Dialog
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun AddEditIllnessDialog(
    state       : ChildIllnessesUiState,
    viewModel   : ChildIllnessesViewModel,
    babyId      : String,
    dimensions  : Dimensions,
    customColors: CustomColors
) {
    val isEditing = state.editingIllnessId != null

    // ── Build localized date display using strings.xml month names ────────────
    val monthNames = listOf(
        stringResource(Res.string.month_jan), stringResource(Res.string.month_feb),
        stringResource(Res.string.month_mar), stringResource(Res.string.month_apr),
        stringResource(Res.string.month_may), stringResource(Res.string.month_jun),
        stringResource(Res.string.month_jul), stringResource(Res.string.month_aug),
        stringResource(Res.string.month_sep), stringResource(Res.string.month_oct),
        stringResource(Res.string.month_nov), stringResource(Res.string.month_dec)
    )
    val diagnosisDateDisplay = state.formDiagnosisDateEpoch?.let {
        val d = LocalDate.fromEpochDays(it.toInt())
        "${d.day} ${monthNames[d.month.number - 1]} ${d.year}"
    }

    AlertDialog(
        onDismissRequest = viewModel::dismissAddEditDialog,
        title = {
            Text(
                if (isEditing) stringResource(Res.string.child_illnesses_dialog_edit_title)
                else stringResource(Res.string.child_illnesses_dialog_add_title),
                fontWeight = FontWeight.Bold,
                style      = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)) {

                // ── Illness name ──────────────────────────────────────────────
                OutlinedTextField(
                    value         = state.formIllnessName,
                    onValueChange = viewModel::onIllnessNameChange,
                    label         = { Text(stringResource(Res.string.child_illnesses_field_name_label)) },
                    placeholder   = { Text(stringResource(Res.string.child_illnesses_field_name_placeholder)) },
                    singleLine    = true,
                    isError       = state.formIllnessName.isBlank() && state.formSubmitted,
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = RoundedCornerShape(dimensions.buttonCornerRadius - 4.dp)
                )
                if (state.formIllnessName.isBlank() && state.formSubmitted) {
                    Text(
                        stringResource(Res.string.child_illnesses_field_name_error),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                // ── Diagnosis date — picker button ────────────────────────────
                OutlinedButton(
                    onClick  = viewModel::openDatePicker,
                    shape    = RoundedCornerShape(dimensions.buttonCornerRadius - 4.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors   = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (state.formDiagnosisDateEpoch != null)
                            MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurface.copy(0.5f)
                    )
                ) {
                    Icon(
                        Icons.Default.CalendarToday, null,
                        modifier = Modifier.size(18.dp),
                        tint     = customColors.accentGradientStart
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text  = diagnosisDateDisplay
                            ?: stringResource(Res.string.child_illnesses_field_date_placeholder),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.weight(1f))
                    if (state.formDiagnosisDateEpoch != null) {
                        IconButton(
                            onClick  = { viewModel.onDateSelected(-1L) },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                Icons.Default.Clear, null,
                                modifier = Modifier.size(14.dp),
                                tint     = MaterialTheme.colorScheme.onSurface.copy(0.5f)
                            )
                        }
                    }
                }

                // ── Notes ─────────────────────────────────────────────────────
                OutlinedTextField(
                    value         = state.formNotes,
                    onValueChange = viewModel::onNotesChange,
                    label         = { Text(stringResource(Res.string.child_illnesses_field_notes_label)) },
                    placeholder   = { Text(stringResource(Res.string.child_illnesses_field_notes_placeholder)) },
                    singleLine    = false,
                    minLines      = 3,
                    maxLines      = 5,
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = RoundedCornerShape(dimensions.buttonCornerRadius - 4.dp)
                )

                // ── Active toggle ─────────────────────────────────────────────
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
                ) {
                    Icon(
                        Icons.Default.HealthAndSafety, null,
                        tint     = customColors.accentGradientStart,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        stringResource(Res.string.child_illnesses_toggle_active_label),
                        style    = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked         = state.formIsActive,
                        onCheckedChange = viewModel::onIsActiveChange
                    )
                }

                state.errorMessage?.let {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick  = { viewModel.saveIllness(babyId) },
                enabled  = !state.isSaving,
                colors   = ButtonDefaults.buttonColors(
                    containerColor = customColors.accentGradientStart),
                shape    = RoundedCornerShape(dimensions.buttonCornerRadius)
            ) {
                if (state.isSaving)
                    CircularProgressIndicator(
                        modifier    = Modifier.size(16.dp),
                        color       = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                else
                    Text(
                        if (isEditing) stringResource(Res.string.child_illnesses_dialog_update)
                        else stringResource(Res.string.child_illnesses_dialog_save)
                    )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick  = viewModel::dismissAddEditDialog,
                enabled  = !state.isSaving,
                shape    = RoundedCornerShape(dimensions.buttonCornerRadius)
            ) {
                Text(stringResource(Res.string.btn_cancel))
            }
        },
        shape          = RoundedCornerShape(dimensions.cardCornerRadius),
        containerColor = MaterialTheme.colorScheme.surface
    )
}