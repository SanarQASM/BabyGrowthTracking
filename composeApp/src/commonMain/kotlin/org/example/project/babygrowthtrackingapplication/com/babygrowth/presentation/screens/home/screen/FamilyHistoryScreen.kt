package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model.FamilyHistoryViewModel
import org.example.project.babygrowthtrackingapplication.theme.*
import org.jetbrains.compose.resources.stringResource

// ─────────────────────────────────────────────────────────────────────────────
// FamilyHistoryScreen
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyHistoryScreen(
    babyId    : String,
    babyName  : String,
    viewModel : FamilyHistoryViewModel,
    onBack    : () -> Unit
) {
    val state        = viewModel.uiState
    val customColors = MaterialTheme.customColors
    val dimensions   = LocalDimensions.current
    val isLandscape  = LocalIsLandscape.current
    val snackbar     = remember { SnackbarHostState() }

    // Load data on first launch
    LaunchedEffect(babyId) { viewModel.loadFamilyHistory(babyId) }

    // Snackbar messages
    LaunchedEffect(state.successMessage) {
        state.successMessage?.let { snackbar.showSnackbar(it); viewModel.clearSuccess() }
    }
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { snackbar.showSnackbar(it); viewModel.clearError() }
    }

    // ── Delete confirmation dialog ────────────────────────────────────────────
    if (state.showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = viewModel::dismissDeleteConfirm,
            icon = { Text("🗑️", style = MaterialTheme.typography.displaySmall) },
            title = {
                Text(
                    stringResource(Res.string.family_history_confirm_delete_title),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Text(
                    stringResource(Res.string.family_history_confirm_delete_msg),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.delete() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(dimensions.buttonCornerRadius),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        stringResource(Res.string.family_history_delete),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onError
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = viewModel::dismissDeleteConfirm,
                    shape = RoundedCornerShape(dimensions.buttonCornerRadius),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(Res.string.btn_cancel))
                }
            },
            shape = RoundedCornerShape(dimensions.cardCornerRadius),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    // ── Not-set alert dialog ──────────────────────────────────────────────────
    if (state.showNotSetAlert) {
        AlertDialog(
            onDismissRequest = viewModel::dismissNotSetAlert,
            icon = { Text("⚠️", style = MaterialTheme.typography.displaySmall) },
            title = {
                Text(
                    stringResource(Res.string.family_history_not_set_alert_title),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Text(
                    stringResource(Res.string.family_history_not_set_alert_msg),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.dismissNotSetAlert(); viewModel.startEditing() },
                    colors = ButtonDefaults.buttonColors(containerColor = customColors.accentGradientStart),
                    shape = RoundedCornerShape(dimensions.buttonCornerRadius),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        stringResource(Res.string.family_history_add_info),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = viewModel::dismissNotSetAlert,
                    shape = RoundedCornerShape(dimensions.buttonCornerRadius),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(Res.string.btn_cancel))
                }
            },
            shape = RoundedCornerShape(dimensions.cardCornerRadius),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            FamilyHistoryTopBar(
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
                state.isLoading -> {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        CircularProgressIndicator(color = customColors.accentGradientStart)
                    }
                }
                isLandscape -> {
                    FamilyHistoryLandscapeLayout(
                        state        = state,
                        viewModel    = viewModel,
                        babyId       = babyId,
                        customColors = customColors,
                        dimensions   = dimensions
                    )
                }
                else -> {
                    FamilyHistoryPortraitLayout(
                        state        = state,
                        viewModel    = viewModel,
                        babyId       = babyId,
                        customColors = customColors,
                        dimensions   = dimensions
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Top App Bar
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FamilyHistoryTopBar(
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
                    text       = stringResource(Res.string.family_history_title),
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
                        contentDescription = stringResource(Res.string.family_history_edit),
                        tint = customColors.accentGradientStart
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = stringResource(Res.string.family_history_delete),
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
private fun FamilyHistoryPortraitLayout(
    state        : org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model.FamilyHistoryUiState,
    viewModel    : FamilyHistoryViewModel,
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

        // Status card at top
        FamilyHistoryStatusCard(
            isSet        = state.isSet,
            customColors = customColors,
            dimensions   = dimensions,
            onAddInfo    = viewModel::startEditing,
            modifier     = Modifier.padding(horizontal = dimensions.screenPadding)
        )

        Spacer(Modifier.height(dimensions.spacingMedium))

        // Form / view content
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
                EmptyFamilyHistoryContent(
                    customColors = customColors,
                    dimensions   = dimensions,
                    onAddInfo    = viewModel::startEditing
                )
            } else {
                FamilyHistoryFormContent(
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
private fun FamilyHistoryLandscapeLayout(
    state        : org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model.FamilyHistoryUiState,
    viewModel    : FamilyHistoryViewModel,
    babyId       : String,
    customColors : CustomColors,
    dimensions   : Dimensions
) {
    Row(modifier = Modifier.fillMaxSize()) {
        // Left pane: status + summary
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
            Text(
                "🏥",
                style = MaterialTheme.typography.displaySmall
            )
            Text(
                stringResource(Res.string.family_history_title),
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onPrimary,
                textAlign  = TextAlign.Center
            )

            Spacer(Modifier.height(dimensions.spacingSmall))

            // Status chip
            Surface(
                shape = RoundedCornerShape(50),
                color = if (state.isSet)
                    Color(0xFF22C55E).copy(alpha = 0.2f)
                else
                    MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
            ) {
                Text(
                    text = if (state.isSet)
                        stringResource(Res.string.family_history_status_set)
                    else
                        stringResource(Res.string.family_history_status_not_set),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style    = MaterialTheme.typography.labelMedium,
                    color    = if (state.isSet) Color(0xFF22C55E) else MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (!state.isEditing && !state.isSet) {
                Button(
                    onClick  = viewModel::startEditing,
                    shape    = RoundedCornerShape(dimensions.buttonCornerRadius),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(dimensions.iconSmall))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        stringResource(Res.string.family_history_add_info),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

        // Right pane: form
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(dimensions.spacingLarge),
            verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)
        ) {
            if (!state.isSet && !state.isEditing) {
                EmptyFamilyHistoryContent(
                    customColors = customColors,
                    dimensions   = dimensions,
                    onAddInfo    = viewModel::startEditing
                )
            } else {
                FamilyHistoryFormContent(
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
private fun FamilyHistoryStatusCard(
    isSet       : Boolean,
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
                    text       = stringResource(Res.string.family_history_title),
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isSet)
                        stringResource(Res.string.family_history_status_set)
                    else
                        stringResource(Res.string.family_history_status_not_set),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSet)
                        Color(0xFF22C55E)
                    else
                        MaterialTheme.colorScheme.error
                )
            }
            if (!isSet) {
                TextButton(
                    onClick = onAddInfo,
                    colors  = ButtonDefaults.textButtonColors(contentColor = customColors.accentGradientStart)
                ) {
                    Text(
                        stringResource(Res.string.family_history_add_info),
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
private fun EmptyFamilyHistoryContent(
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
            text       = stringResource(Res.string.family_history_not_set_alert_title),
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.onPrimary,
            textAlign  = TextAlign.Center
        )

        Text(
            text      = stringResource(Res.string.family_history_not_set_alert_msg),
            style     = MaterialTheme.typography.bodyMedium,
            color     = MaterialTheme.colorScheme.onPrimary.copy(0.75f),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(dimensions.spacingSmall))

        Button(
            onClick = onAddInfo,
            shape   = RoundedCornerShape(dimensions.buttonCornerRadius),
            colors  = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f)
            ),
            modifier = Modifier.fillMaxWidth(0.7f)
        ) {
            Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(dimensions.iconMedium))
            Spacer(Modifier.width(6.dp))
            Text(
                stringResource(Res.string.family_history_add_info),
                color      = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Form Content (used for both view and edit modes)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun FamilyHistoryFormContent(
    state        : org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model.FamilyHistoryUiState,
    viewModel    : FamilyHistoryViewModel,
    babyId       : String,
    readOnly     : Boolean,
    customColors : CustomColors,
    dimensions   : Dimensions
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)
    ) {
        // Section header
        FHSectionCard(
            title    = stringResource(Res.string.fh_heredity),
            emoji    = "🧬",
            readOnly = readOnly,
            value    = state.heredity,
            onChange = viewModel::onHeredityChange,
            placeholder = stringResource(Res.string.fh_placeholder_heredity),
            customColors = customColors,
            dimensions   = dimensions
        )

        FHSectionCard(
            title    = stringResource(Res.string.fh_blood_diseases),
            emoji    = "🩸",
            readOnly = readOnly,
            value    = state.bloodDiseases,
            onChange = viewModel::onBloodDiseasesChange,
            placeholder = stringResource(Res.string.fh_placeholder_blood),
            customColors = customColors,
            dimensions   = dimensions
        )

        FHSectionCard(
            title    = stringResource(Res.string.fh_cardiovascular),
            emoji    = "❤️",
            readOnly = readOnly,
            value    = state.cardiovascularDiseases,
            onChange = viewModel::onCardiovascularChange,
            placeholder = stringResource(Res.string.fh_placeholder_cardiovascular),
            customColors = customColors,
            dimensions   = dimensions
        )

        FHSectionCard(
            title    = stringResource(Res.string.fh_metabolic),
            emoji    = "⚗️",
            readOnly = readOnly,
            value    = state.metabolicDiseases,
            onChange = viewModel::onMetabolicChange,
            placeholder = stringResource(Res.string.fh_placeholder_metabolic),
            customColors = customColors,
            dimensions   = dimensions
        )

        FHSectionCard(
            title    = stringResource(Res.string.fh_appendicitis),
            emoji    = "🫀",
            readOnly = readOnly,
            value    = state.appendicitis,
            onChange = viewModel::onAppendicitisChange,
            placeholder = stringResource(Res.string.fh_placeholder_appendicitis),
            customColors = customColors,
            dimensions   = dimensions
        )

        FHSectionCard(
            title    = stringResource(Res.string.fh_tuberculosis),
            emoji    = "🫁",
            readOnly = readOnly,
            value    = state.tuberculosis,
            onChange = viewModel::onTuberculosisChange,
            placeholder = stringResource(Res.string.fh_placeholder_tuberculosis),
            customColors = customColors,
            dimensions   = dimensions
        )

        FHSectionCard(
            title    = stringResource(Res.string.fh_parkinsonism),
            emoji    = "🧠",
            readOnly = readOnly,
            value    = state.parkinsonism,
            onChange = viewModel::onParkinsonismChange,
            placeholder = stringResource(Res.string.fh_placeholder_parkinsonism),
            customColors = customColors,
            dimensions   = dimensions
        )

        FHSectionCard(
            title    = stringResource(Res.string.fh_allergies),
            emoji    = "🤧",
            readOnly = readOnly,
            value    = state.allergies,
            onChange = viewModel::onAllergiesChange,
            placeholder = stringResource(Res.string.fh_placeholder_allergies),
            customColors = customColors,
            dimensions   = dimensions
        )

        FHSectionCard(
            title    = stringResource(Res.string.fh_others),
            emoji    = "📋",
            readOnly = readOnly,
            value    = state.others,
            onChange = viewModel::onOthersChange,
            placeholder = stringResource(Res.string.fh_placeholder_others),
            customColors = customColors,
            dimensions   = dimensions
        )

        // Error message
        state.errorMessage?.let { msg ->
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

        // Save button (only in edit mode)
        AnimatedVisibility(
            visible = !readOnly,
            enter   = expandVertically(),
            exit    = shrinkVertically()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)) {
                Spacer(Modifier.height(dimensions.spacingXSmall))

                // Save
                FHActionButton(
                    label          = if (state.isSaving)
                        stringResource(Res.string.add_baby_saving)
                    else
                        stringResource(Res.string.family_history_save),
                    isLoading      = state.isSaving,
                    enabled        = !state.isSaving,
                    containerColor = customColors.accentGradientStart,
                    onClick        = { viewModel.save(babyId) }
                )

                // Cancel
                FHActionButton(
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
// Section Card (single field row)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun FHSectionCard(
    title       : String,
    emoji       : String,
    readOnly    : Boolean,
    value       : String,
    onChange    : (String) -> Unit,
    placeholder : String,
    customColors: CustomColors,
    dimensions  : Dimensions
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall),
            modifier = Modifier.padding(bottom = dimensions.spacingXSmall)
        ) {
            Text(emoji, style = MaterialTheme.typography.bodyLarge)
            Text(
                title,
                style      = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onPrimary,
                letterSpacing = MaterialTheme.typography.labelLarge.letterSpacing
            )
        }

        if (readOnly) {
            // View mode: show card with value or dash
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
                    text  = value.ifBlank { "—" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (value.isBlank())
                        MaterialTheme.colorScheme.onPrimary.copy(0.4f)
                    else
                        MaterialTheme.colorScheme.onPrimary
                )
            }
        } else {
            // Edit mode: show text field
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
                    value         = value,
                    onValueChange = onChange,
                    placeholder   = {
                        Text(
                            placeholder,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor        = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor      = MaterialTheme.colorScheme.onSurface,
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
}

// ─────────────────────────────────────────────────────────────────────────────
// Action Button (Save / Cancel)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun FHActionButton(
    label         : String,
    isLoading     : Boolean,
    enabled       : Boolean,
    containerColor: Color,
    labelColor    : Color = MaterialTheme.colorScheme.onPrimary,
    borderColor   : Color = Color.Transparent,
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
        modifier       = Modifier.fillMaxWidth().height(dimensions.buttonHeight)
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
                    .background(
                        customColors.glassOverlay,
                        RoundedCornerShape(dimensions.buttonCornerRadius)
                    )
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