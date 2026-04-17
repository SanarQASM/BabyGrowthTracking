// composeApp/src/commonMain/kotlin/org/example/project/babygrowthtrackingapplication/com/babygrowth/presentation/screens/home/screen/PreCheckInvestigationScreen.kt

package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model.PreCheckInvestigationViewModel
import org.example.project.babygrowthtrackingapplication.data.network.InvestigationStatusNet
import org.example.project.babygrowthtrackingapplication.theme.*
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

// ─────────────────────────────────────────────────────────────────────────────
// Root Screen
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun PreCheckInvestigationScreen(
    babyId    : String,
    babyName  : String,
    viewModel : PreCheckInvestigationViewModel,
    onBack    : () -> Unit
) {
    val state        = viewModel.uiState
    val customColors = MaterialTheme.customColors
    val dimensions   = LocalDimensions.current
    val isLandscape  = LocalIsLandscape.current
    val snackbar     = remember { SnackbarHostState() }

    // Resolve sentinel messages
    val msgSaved   = stringResource(Res.string.pre_check_success_saved)
    val msgDeleted = stringResource(Res.string.pre_check_success_deleted)
    val errDate    = stringResource(Res.string.pre_check_error_date_required)

    fun resolveMessage(raw: String?): String? = when {
        raw == null              -> null
        raw == "MSG_SAVED"       -> msgSaved
        raw == "MSG_DELETED"     -> msgDeleted
        raw == "ERR_DATE"        -> errDate
        raw.startsWith("ERR_SAVE:")   -> raw.removePrefix("ERR_SAVE:")
        raw.startsWith("ERR_DELETE:") -> raw.removePrefix("ERR_DELETE:")
        else                     -> raw
    }

    LaunchedEffect(babyId) { viewModel.load(babyId) }

    LaunchedEffect(state.successMessage) {
        resolveMessage(state.successMessage)?.let { snackbar.showSnackbar(it); viewModel.clearSuccess() }
    }
    LaunchedEffect(state.errorMessage) {
        resolveMessage(state.errorMessage)?.let { snackbar.showSnackbar(it); viewModel.clearError() }
    }

    // ── Date Picker ───────────────────────────────────────────────────────────
    if (state.showDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.checkDateEpoch
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
                }) { Text(stringResource(Res.string.add_baby_date_ok)) }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissDatePicker) {
                    Text(stringResource(Res.string.btn_cancel))
                }
            }
        ) {
            DatePicker(
                state = pickerState,
                showModeToggle = true,
                title = {
                    Text(
                        text     = stringResource(Res.string.pre_check_check_date),
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
            icon  = { Text("🗑️", style = MaterialTheme.typography.displaySmall) },
            title = {
                Text(
                    stringResource(Res.string.pre_check_confirm_delete_title),
                    fontWeight = FontWeight.Bold,
                    textAlign  = TextAlign.Center,
                    style      = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Text(
                    stringResource(Res.string.pre_check_confirm_delete_msg),
                    textAlign = TextAlign.Center,
                    style     = MaterialTheme.typography.bodyMedium,
                    color     = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick  = { viewModel.delete() },
                    colors   = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape    = RoundedCornerShape(dimensions.buttonCornerRadius),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        stringResource(Res.string.pre_check_delete),
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
                ) { Text(stringResource(Res.string.btn_cancel)) }
            },
            shape          = RoundedCornerShape(dimensions.cardCornerRadius),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    // ── Not-set alert ─────────────────────────────────────────────────────────
    if (state.showNotSetAlert) {
        AlertDialog(
            onDismissRequest = viewModel::dismissNotSetAlert,
            icon  = { Text("⚠️", style = MaterialTheme.typography.displaySmall) },
            title = {
                Text(
                    stringResource(Res.string.pre_check_not_set_title),
                    fontWeight = FontWeight.Bold,
                    textAlign  = TextAlign.Center,
                    style      = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Text(
                    stringResource(Res.string.pre_check_not_set_msg),
                    textAlign = TextAlign.Center,
                    style     = MaterialTheme.typography.bodyMedium,
                    color     = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick  = { viewModel.dismissNotSetAlert(); viewModel.startEditing() },
                    colors   = ButtonDefaults.buttonColors(containerColor = customColors.accentGradientStart),
                    shape    = RoundedCornerShape(dimensions.buttonCornerRadius),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        stringResource(Res.string.pre_check_add_info),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick  = viewModel::dismissNotSetAlert,
                    shape    = RoundedCornerShape(dimensions.buttonCornerRadius),
                    modifier = Modifier.fillMaxWidth()
                ) { Text(stringResource(Res.string.btn_cancel)) }
            },
            shape          = RoundedCornerShape(dimensions.cardCornerRadius),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    Scaffold(
        snackbarHost   = { SnackbarHost(snackbar) },
        topBar         = {
            PreCheckTopBar(
                babyName     = babyName,
                isEditing    = state.isEditing,
                isSet        = state.isSet,
                onBack       = onBack,
                onEdit       = viewModel::startEditing,
                onDelete     = viewModel::showDeleteConfirm,
                onCancel     = viewModel::cancelEditing,
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
                isLandscape -> PreCheckLandscapeLayout(state, viewModel, babyId, customColors, dimensions)
                else        -> PreCheckPortraitLayout(state, viewModel, babyId, customColors, dimensions)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Top Bar
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PreCheckTopBar(
    babyName    : String,
    isEditing   : Boolean,
    isSet       : Boolean,
    onBack      : () -> Unit,
    onEdit      : () -> Unit,
    onDelete    : () -> Unit,
    onCancel    : () -> Unit,
    customColors: CustomColors,
    dimensions  : Dimensions
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text       = stringResource(Res.string.pre_check_title),
                    fontWeight = FontWeight.Bold,
                    style      = MaterialTheme.typography.titleMedium,
                    color      = customColors.accentGradientStart
                )
                Text(
                    text  = babyName,
                    style = MaterialTheme.typography.bodySmall,
                    color = customColors.accentGradientStart.copy(0.6f)
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = if (isEditing) onCancel else onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(Res.string.common_back),
                    tint = customColors.accentGradientStart
                )
            }
        },
        actions = {
            if (!isEditing && isSet) {
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = stringResource(Res.string.pre_check_edit),
                        tint = customColors.accentGradientStart
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = stringResource(Res.string.pre_check_delete),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor             = customColors.accentGradientStart.copy(alpha = 0.12f),
            titleContentColor          = customColors.accentGradientStart,
            navigationIconContentColor = customColors.accentGradientStart
        )
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Portrait Layout
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PreCheckPortraitLayout(
    state        : org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model.PreCheckInvestigationUiState,
    viewModel    : PreCheckInvestigationViewModel,
    babyId       : String,
    customColors : CustomColors,
    dimensions   : Dimensions
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(dimensions.spacingSmall))

        PreCheckStatusCard(
            isSet        = state.isSet,
            checkDate    = state.checkDate,
            customColors = customColors,
            dimensions   = dimensions,
            onAddInfo    = viewModel::startEditing,
            modifier     = Modifier.padding(horizontal = dimensions.screenPadding)
        )

        Spacer(Modifier.height(dimensions.spacingMedium))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            customColors.accentGradientStart.copy(alpha = 0.55f),
                            customColors.accentGradientEnd.copy(alpha = 0.40f)
                        )
                    ),
                    RoundedCornerShape(
                        topStart = dimensions.cardCornerRadius,
                        topEnd   = dimensions.cardCornerRadius
                    )
                )
                .padding(
                    horizontal = dimensions.spacingLarge,
                    vertical   = dimensions.spacingXLarge
                )
        ) {
            if (!state.isSet && !state.isEditing) {
                PreCheckEmptyContent(customColors, dimensions, viewModel::startEditing)
            } else {
                PreCheckFormContent(
                    state        = state,
                    viewModel    = viewModel,
                    babyId       = babyId,
                    readOnly     = !state.isEditing,
                    customColors = customColors,
                    dimensions   = dimensions
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Landscape Layout
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PreCheckLandscapeLayout(
    state        : org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model.PreCheckInvestigationUiState,
    viewModel    : PreCheckInvestigationViewModel,
    babyId       : String,
    customColors : CustomColors,
    dimensions   : Dimensions
) {
    Row(modifier = Modifier.fillMaxSize()) {
        // Left pane
        Column(
            modifier = Modifier
                .width(260.dp)
                .fillMaxHeight()
                .background(customColors.accentGradientStart.copy(alpha = 0.55f))
                .verticalScroll(rememberScrollState())
                .padding(dimensions.spacingLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)
        ) {
            Spacer(Modifier.height(dimensions.spacingSmall))
            Text("🏥", style = MaterialTheme.typography.displaySmall)
            Text(
                stringResource(Res.string.pre_check_title),
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onPrimary,
                textAlign  = TextAlign.Center
            )
            Spacer(Modifier.height(dimensions.spacingSmall))
            Surface(
                shape = RoundedCornerShape(50),
                color = if (state.isSet) Color(0xFF22C55E).copy(0.2f)
                else MaterialTheme.colorScheme.error.copy(0.2f)
            ) {
                Text(
                    text = if (state.isSet)
                        stringResource(Res.string.pre_check_status_set)
                    else
                        stringResource(Res.string.pre_check_status_not_set),
                    modifier   = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style      = MaterialTheme.typography.labelMedium,
                    color      = if (state.isSet) Color(0xFF22C55E) else MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold
                )
            }
            if (!state.isEditing && !state.isSet) {
                Button(
                    onClick  = viewModel::startEditing,
                    shape    = RoundedCornerShape(dimensions.buttonCornerRadius),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onPrimary.copy(0.25f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(dimensions.iconSmall))
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(Res.string.pre_check_add_info), color = MaterialTheme.colorScheme.onPrimary)
                }
            }
            if (state.isSet && state.checkDate.isNotBlank()) {
                Spacer(Modifier.height(dimensions.spacingSmall))
                Text(
                    text  = stringResource(Res.string.pre_check_check_date),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(0.7f)
                )
                Text(
                    text       = state.checkDate,
                    style      = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        // Right pane
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(dimensions.spacingLarge),
            verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)
        ) {
            if (!state.isSet && !state.isEditing) {
                PreCheckEmptyContent(customColors, dimensions, viewModel::startEditing)
            } else {
                PreCheckFormContent(
                    state        = state,
                    viewModel    = viewModel,
                    babyId       = babyId,
                    readOnly     = !state.isEditing,
                    customColors = customColors,
                    dimensions   = dimensions
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Status Card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PreCheckStatusCard(
    isSet       : Boolean,
    checkDate   : String,
    customColors: CustomColors,
    dimensions  : Dimensions,
    onAddInfo   : () -> Unit,
    modifier    : Modifier = Modifier
) {
    Card(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(dimensions.cardCornerRadius),
        colors    = CardDefaults.cardColors(
            containerColor = if (isSet)
                Color(0xFF22C55E).copy(alpha = 0.1f)
            else
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensions.spacingMedium),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(dimensions.spacingSmall))
                    .background(
                        if (isSet) Color(0xFF22C55E).copy(0.18f)
                        else MaterialTheme.colorScheme.error.copy(0.15f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (isSet) "✅" else "⚠️",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = stringResource(Res.string.pre_check_title),
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isSet && checkDate.isNotBlank())
                        stringResource(Res.string.pre_check_date_recorded, checkDate)
                    else if (isSet)
                        stringResource(Res.string.pre_check_status_set)
                    else
                        stringResource(Res.string.pre_check_status_not_set),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSet) Color(0xFF22C55E)
                    else MaterialTheme.colorScheme.error
                )
            }
            if (!isSet) {
                TextButton(
                    onClick = onAddInfo,
                    colors  = ButtonDefaults.textButtonColors(contentColor = customColors.accentGradientStart)
                ) {
                    Text(
                        stringResource(Res.string.pre_check_add_info),
                        fontWeight = FontWeight.SemiBold,
                        style      = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Empty State
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PreCheckEmptyContent(
    customColors: CustomColors,
    dimensions  : Dimensions,
    onAddInfo   : () -> Unit
) {
    Column(
        modifier            = Modifier
            .fillMaxWidth()
            .padding(vertical = dimensions.spacingXLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)
    ) {
        Text("🏥", style = MaterialTheme.typography.displaySmall)
        Text(
            text       = stringResource(Res.string.pre_check_not_set_title),
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.onPrimary,
            textAlign  = TextAlign.Center
        )
        Text(
            text      = stringResource(Res.string.pre_check_not_set_msg),
            style     = MaterialTheme.typography.bodyMedium,
            color     = MaterialTheme.colorScheme.onPrimary.copy(0.75f),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(dimensions.spacingSmall))
        Button(
            onClick  = onAddInfo,
            shape    = RoundedCornerShape(dimensions.buttonCornerRadius),
            colors   = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f)
            ),
            modifier = Modifier.fillMaxWidth(0.7f)
        ) {
            Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(dimensions.iconMedium))
            Spacer(Modifier.width(6.dp))
            Text(
                stringResource(Res.string.pre_check_add_info),
                color      = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Form Content
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PreCheckFormContent(
    state        : org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model.PreCheckInvestigationUiState,
    viewModel    : PreCheckInvestigationViewModel,
    babyId       : String,
    readOnly     : Boolean,
    customColors : CustomColors,
    dimensions   : Dimensions
) {
    Column(verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)) {

        // ── Check Date ────────────────────────────────────────────────────────
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall),
                modifier              = Modifier.padding(bottom = dimensions.spacingXSmall)
            ) {
                Text("📅", style = MaterialTheme.typography.bodyLarge)
                Text(
                    stringResource(Res.string.pre_check_check_date),
                    style      = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onPrimary
                )
            }

            if (readOnly) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            customColors.glassOverlay.copy(alpha = 0.18f),
                            RoundedCornerShape(dimensions.buttonCornerRadius)
                        )
                        .border(
                            dimensions.spacingXSmall / 4,
                            customColors.glassOverlay.copy(alpha = 0.3f),
                            RoundedCornerShape(dimensions.buttonCornerRadius)
                        )
                        .padding(
                            horizontal = dimensions.spacingMedium,
                            vertical   = dimensions.spacingSmall + 4.dp
                        )
                ) {
                    Text(
                        text  = state.checkDate.ifBlank { "—" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (state.checkDate.isBlank())
                            MaterialTheme.colorScheme.onPrimary.copy(0.4f)
                        else
                            MaterialTheme.colorScheme.onPrimary
                    )
                }
            } else {
                OutlinedButton(
                    onClick  = viewModel::openDatePicker,
                    shape    = RoundedCornerShape(dimensions.buttonCornerRadius - 4.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors   = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (state.checkDate.isNotBlank())
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
                        text  = state.checkDate.ifBlank { stringResource(Res.string.pre_check_date_placeholder) },
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.weight(1f))
                }
            }
        }

        HorizontalDivider(
            color     = MaterialTheme.colorScheme.onPrimary.copy(0.15f),
            thickness = dimensions.hairlineDividerThickness
        )

        // ── Investigation Items ───────────────────────────────────────────────
        Text(
            text       = stringResource(Res.string.pre_check_items_section),
            style      = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.onPrimary.copy(0.75f)
        )

        data class InvItem(
            val emoji  : String,
            val label  : String,
            val value  : InvestigationStatusNet,
            val onChange: (InvestigationStatusNet) -> Unit
        )

        val items = listOf(
            InvItem("🟡", stringResource(Res.string.pre_check_jaundice),               state.jaundice,            viewModel::onJaundiceChange),
            InvItem("💨", stringResource(Res.string.pre_check_shortness_of_breath),    state.shortnessOfBreath,   viewModel::onShortnessOfBreathChange),
            InvItem("🔵", stringResource(Res.string.pre_check_turning_blue),           state.turningBlue,         viewModel::onTurningBlueChange),
            InvItem("♟️", stringResource(Res.string.pre_check_marble_heart),           state.marbleHeart,         viewModel::onMarbleHeartChange),
            InvItem("🫀", stringResource(Res.string.pre_check_inflammation_of_liver),  state.inflammationOfLiver, viewModel::onInflammationOfLiverChange),
            InvItem("🫁", stringResource(Res.string.pre_check_inflammation_of_spleen), state.inflammationOfSpleen,viewModel::onInflammationOfSpleenChange),
            InvItem("🩹", stringResource(Res.string.pre_check_hernia),                 state.hernia,              viewModel::onHerniaChange),
            InvItem("💧", stringResource(Res.string.pre_check_hydrocele_of_ear),       state.hydroceleOfEar,      viewModel::onHydroceleOfEarChange),
            InvItem("🦴", stringResource(Res.string.pre_check_hip_joint_dislocation),  state.hipJointDislocation, viewModel::onHipJointDislocationChange),
            InvItem("💪", stringResource(Res.string.pre_check_muscles_normal),         state.musclesNormal,       viewModel::onMusclesNormalChange),
            InvItem("⚡", stringResource(Res.string.pre_check_reactions_normal),       state.reactionsNormal,     viewModel::onReactionsNormalChange),
            InvItem("🧬", stringResource(Res.string.pre_check_nucleus_normal),         state.nucleusNormal,       viewModel::onNucleusNormalChange),
            InvItem("🔬", stringResource(Res.string.pre_check_genitals_normal),        state.genitalsNormal,      viewModel::onGenitalsNormalChange),
            InvItem("👁️", stringResource(Res.string.pre_check_eye_normal),             state.eyeNormal,           viewModel::onEyeNormalChange),
            InvItem("🔴", stringResource(Res.string.pre_check_red_reflex),             state.redReflex,           viewModel::onRedReflexChange),
            InvItem("🔊", stringResource(Res.string.pre_check_reaction_to_sound),      state.reactionToSound,     viewModel::onReactionToSoundChange),
        )

        items.forEach { item ->
            InvestigationItemRow(
                emoji    = item.emoji,
                label    = item.label,
                value    = item.value,
                readOnly = readOnly,
                onChange = item.onChange,
                customColors = customColors,
                dimensions   = dimensions
            )
        }

        // ── Others ────────────────────────────────────────────────────────────
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall),
                modifier              = Modifier.padding(bottom = dimensions.spacingXSmall)
            ) {
                Text("📋", style = MaterialTheme.typography.bodyLarge)
                Text(
                    stringResource(Res.string.pre_check_others),
                    style      = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onPrimary
                )
            }

            if (readOnly) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            customColors.glassOverlay.copy(alpha = 0.18f),
                            RoundedCornerShape(dimensions.buttonCornerRadius)
                        )
                        .border(
                            dimensions.spacingXSmall / 4,
                            customColors.glassOverlay.copy(alpha = 0.3f),
                            RoundedCornerShape(dimensions.buttonCornerRadius)
                        )
                        .padding(
                            horizontal = dimensions.spacingMedium,
                            vertical   = dimensions.spacingSmall + 4.dp
                        )
                ) {
                    Text(
                        text  = state.others.ifBlank { "—" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (state.others.isBlank())
                            MaterialTheme.colorScheme.onPrimary.copy(0.4f)
                        else
                            MaterialTheme.colorScheme.onPrimary
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.13f),
                            RoundedCornerShape(dimensions.buttonCornerRadius - 4.dp)
                        )
                        .border(
                            dimensions.spacingXSmall / 4,
                            customColors.accentGradientStart.copy(alpha = 0.35f),
                            RoundedCornerShape(dimensions.buttonCornerRadius - 4.dp)
                        )
                ) {
                    TextField(
                        value         = state.others,
                        onValueChange = viewModel::onOthersChange,
                        placeholder   = {
                            Text(
                                stringResource(Res.string.pre_check_others_placeholder),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                            )
                        },
                        colors    = TextFieldDefaults.colors(
                            focusedContainerColor   = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor   = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor             = customColors.accentGradientStart
                        ),
                        modifier  = Modifier.fillMaxWidth(),
                        maxLines  = 4,
                        minLines  = 2,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }
        }

        // ── Error message ─────────────────────────────────────────────────────
        state.errorMessage?.let { msg ->
            if (!msg.startsWith("ERR_") && !msg.startsWith("MSG_")) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.error.copy(0.15f),
                            RoundedCornerShape(dimensions.buttonCornerRadius - 4.dp)
                        )
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.error.copy(0.4f),
                            RoundedCornerShape(dimensions.buttonCornerRadius - 4.dp)
                        )
                        .padding(dimensions.spacingMedium)
                ) {
                    Text(
                        msg,
                        color      = MaterialTheme.colorScheme.error,
                        style      = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // ── Save / Cancel buttons (edit mode only) ────────────────────────────
        AnimatedVisibility(
            visible = !readOnly,
            enter   = expandVertically(),
            exit    = shrinkVertically()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)) {
                Spacer(Modifier.height(dimensions.spacingXSmall))
                PreCheckActionButton(
                    label          = if (state.isSaving)
                        stringResource(Res.string.add_baby_saving)
                    else
                        stringResource(Res.string.pre_check_save),
                    isLoading      = state.isSaving,
                    enabled        = !state.isSaving,
                    containerColor = customColors.accentGradientStart,
                    onClick        = { viewModel.save(babyId) }
                )
                PreCheckActionButton(
                    label          = stringResource(Res.string.btn_cancel),
                    isLoading      = false,
                    enabled        = !state.isSaving,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.65f),
                    labelColor     = MaterialTheme.colorScheme.onSurface,
                    borderColor    = MaterialTheme.colorScheme.onSurface.copy(0.18f),
                    onClick        = viewModel::cancelEditing
                )
            }
        }

        Spacer(Modifier.height(dimensions.spacingXXLarge))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Investigation Item Row — three-state selector (Yes / No / Not Known)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun InvestigationItemRow(
    emoji       : String,
    label       : String,
    value       : InvestigationStatusNet,
    readOnly    : Boolean,
    onChange    : (InvestigationStatusNet) -> Unit,
    customColors: CustomColors,
    dimensions  : Dimensions
) {
    val yesLabel = stringResource(Res.string.pre_check_status_yes)
    val noLabel  = stringResource(Res.string.pre_check_status_no)
    val nkLabel  = stringResource(Res.string.pre_check_status_not_known)

    Column(modifier = Modifier.fillMaxWidth()) {
        // Label row
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall),
            modifier              = Modifier.padding(bottom = dimensions.spacingXSmall / 2)
        ) {
            Text(emoji, style = MaterialTheme.typography.bodyMedium)
            Text(
                label,
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color      = MaterialTheme.colorScheme.onPrimary
            )
        }

        if (readOnly) {
            // View mode: coloured badge
            val (badgeColor, badgeText) = when (value) {
                InvestigationStatusNet.yes       -> Color(0xFFEF4444).copy(0.15f) to yesLabel
                InvestigationStatusNet.no        -> Color(0xFF22C55E).copy(0.15f) to noLabel
                InvestigationStatusNet.not_known -> MaterialTheme.colorScheme.surfaceVariant to nkLabel
            }
            val textColor = when (value) {
                InvestigationStatusNet.yes       -> Color(0xFFEF4444)
                InvestigationStatusNet.no        -> Color(0xFF22C55E)
                InvestigationStatusNet.not_known -> MaterialTheme.colorScheme.onSurface.copy(0.5f)
            }
            Box(
                modifier = Modifier
                    .background(badgeColor, RoundedCornerShape(dimensions.buttonCornerRadius / 2))
                    .padding(horizontal = dimensions.spacingSmall, vertical = 3.dp)
            ) {
                Text(
                    badgeText,
                    style      = MaterialTheme.typography.labelMedium,
                    color      = textColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
        } else {
            // Edit mode: three-button segmented selector
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall)
            ) {
                listOf(
                    InvestigationStatusNet.yes       to yesLabel,
                    InvestigationStatusNet.no        to noLabel,
                    InvestigationStatusNet.not_known to nkLabel
                ).forEach { (status, text) ->
                    val selected = value == status
                    val bgColor = when {
                        selected && status == InvestigationStatusNet.yes       -> Color(0xFFEF4444).copy(0.18f)
                        selected && status == InvestigationStatusNet.no        -> Color(0xFF22C55E).copy(0.18f)
                        selected                                                -> customColors.accentGradientStart.copy(0.18f)
                        else                                                    -> MaterialTheme.colorScheme.surface.copy(0.13f)
                    }
                    val contentColor = when {
                        selected && status == InvestigationStatusNet.yes       -> Color(0xFFEF4444)
                        selected && status == InvestigationStatusNet.no        -> Color(0xFF22C55E)
                        selected                                                -> customColors.accentGradientStart
                        else                                                    -> MaterialTheme.colorScheme.onSurface.copy(0.5f)
                    }
                    val borderColor = if (selected) contentColor.copy(0.5f)
                    else MaterialTheme.colorScheme.onSurface.copy(0.15f)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(dimensions.buttonCornerRadius - 4.dp))
                            .background(bgColor)
                            .border(dimensions.borderWidthThin, borderColor, RoundedCornerShape(dimensions.buttonCornerRadius - 4.dp))
                            .clickable(enabled = !readOnly) { onChange(status) }
                            .padding(vertical = dimensions.spacingSmall),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text       = text,
                            style      = MaterialTheme.typography.labelSmall,
                            color      = contentColor,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Action Button (Save / Cancel) — mirrors FHActionButton
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PreCheckActionButton(
    label         : String,
    isLoading     : Boolean,
    enabled       : Boolean,
    containerColor: Color,
    labelColor    : Color  = MaterialTheme.colorScheme.onPrimary,
    borderColor   : Color  = Color.Transparent,
    onClick       : () -> Unit
) {
    val customColors = MaterialTheme.customColors
    val dimensions   = LocalDimensions.current

    Button(
        onClick        = onClick,
        enabled        = enabled,
        shape          = RoundedCornerShape(dimensions.buttonCornerRadius),
        colors         = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(),
        elevation      = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp),
        modifier       = Modifier
            .fillMaxWidth()
            .height(dimensions.buttonHeight)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(listOf(containerColor, containerColor.copy(0.88f))),
                    RoundedCornerShape(dimensions.buttonCornerRadius)
                )
                .then(
                    if (borderColor != Color.Transparent)
                        Modifier.border(1.dp, borderColor, RoundedCornerShape(dimensions.buttonCornerRadius))
                    else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(customColors.glassOverlay, RoundedCornerShape(dimensions.buttonCornerRadius))
            )
            if (isLoading) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
                ) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(dimensions.iconMedium),
                        color       = labelColor,
                        strokeWidth = dimensions.spacingXSmall / 2
                    )
                    Text(label, color = labelColor, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
                }
            } else {
                Text(label, color = labelColor, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}