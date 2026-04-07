package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.screen

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
// HearingSpeechScreen
//
// REFACTORED:
//  • 220.dp landscape left pane → dimensions.landscapeNarrowPaneWidth
//  • 36.dp edit button size     → dimensions.devEditButtonSize
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HearingSpeechScreen(
    babyId: String,
    babyName: String,
    babyAgeMonths: Int,
    viewModel: HearingSpeechViewModel,
    onBack: () -> Unit
) {
    val state = viewModel.uiState
    val customColors = MaterialTheme.customColors
    val dimensions = LocalDimensions.current
    val isLandscape = LocalIsLandscape.current
    val snackbar = remember { SnackbarHostState() }

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

    if (state.editingMonth != null && state.editingState != null) {
        HearingSpeechEditPanel(
            state = state,
            viewModel = viewModel,
            babyId = babyId,
            customColors = customColors,
            dimensions = dimensions
        )
        return
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            ChildDevTopBar(
                title = stringResource(Res.string.child_dev_hearing_speech_title),
                subtitle = babyName,
                emoji = stringResource(Res.string.child_dev_hearing_emoji),
                onBack = onBack,
                customColors = customColors,
                dimensions = dimensions
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
                            customColors.accentGradientEnd.copy(0.06f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
                .padding(padding)
        ) {
            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator(color = customColors.accentGradientStart)
                }
            } else {
                if (isLandscape) {
                    HearingSpeechLandscapeLayout(state, viewModel, customColors, dimensions)
                } else {
                    HearingSpeechPortraitLayout(state, viewModel, customColors, dimensions)
                }
            }
        }
    }
}

@Composable
private fun HearingSpeechPortraitLayout(
    state: HearingSpeechUiState, viewModel: HearingSpeechViewModel,
    customColors: CustomColors, dimensions: Dimensions
) {
    LazyColumn(
        contentPadding = PaddingValues(
            horizontal = dimensions.screenPadding,
            vertical = dimensions.spacingMedium
        ),
        verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            ChildDevHeaderCard(
                title = stringResource(Res.string.child_dev_hearing_speech_title),
                subtitle = stringResource(Res.string.child_dev_hearing_speech_subtitle),
                emoji = stringResource(Res.string.child_dev_hearing_emoji),
                customColors = customColors,
                dimensions = dimensions
            )
        }
        HEARING_SPEECH_MILESTONE_MONTHS.forEach { month ->
            item {
                HearingSpeechMilestoneCard(month, state, viewModel, customColors, dimensions)
            }
        }
        item { Spacer(Modifier.height(dimensions.spacingXXLarge)) }
    }
}

@Composable
private fun HearingSpeechLandscapeLayout(
    state: HearingSpeechUiState, viewModel: HearingSpeechViewModel,
    customColors: CustomColors, dimensions: Dimensions
) {
    Row(Modifier.fillMaxSize()) {
        // CHANGED: 220.dp → dimensions.landscapeNarrowPaneWidth
        Column(
            modifier = Modifier
                .width(dimensions.landscapeNarrowPaneWidth)
                .fillMaxHeight()
                .background(customColors.accentGradientStart.copy(0.55f))
                .verticalScroll(rememberScrollState())
                .padding(dimensions.spacingMedium),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
        ) {
            Spacer(Modifier.height(dimensions.spacingSmall))
            Text(
                stringResource(Res.string.child_dev_hearing_emoji),
                style = MaterialTheme.typography.displaySmall
            )
            Text(
                stringResource(Res.string.child_dev_hearing_speech_title),
                style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary, textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(dimensions.spacingSmall))
            HEARING_SPEECH_MILESTONE_MONTHS.forEach { month ->
                val enabled = viewModel.isMonthEnabled(month)
                val hasData = state.savedRecords.containsKey(month)
                val color = when {
                    !enabled -> MaterialTheme.colorScheme.onSurface.copy(0.3f)
                    hasData -> Color(0xFF22C55E)
                    else -> customColors.accentGradientEnd
                }
                Surface(
                    shape = RoundedCornerShape(50), color = color.copy(0.2f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(Res.string.child_dev_month_label, month),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (!enabled) MaterialTheme.colorScheme.onSurface.copy(0.4f) else color,
                        fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center
                    )
                }
            }
        }
        LazyColumn(
            contentPadding = PaddingValues(dimensions.spacingMedium),
            verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium),
            modifier = Modifier.weight(1f).fillMaxHeight()
        ) {
            HEARING_SPEECH_MILESTONE_MONTHS.forEach { month ->
                item {
                    HearingSpeechMilestoneCard(
                        month,
                        state,
                        viewModel,
                        customColors,
                        dimensions
                    )
                }
            }
            item { Spacer(Modifier.height(dimensions.spacingXXLarge)) }
        }
    }
}

@Composable
private fun HearingSpeechMilestoneCard(
    month: Int, state: HearingSpeechUiState, viewModel: HearingSpeechViewModel,
    customColors: CustomColors, dimensions: Dimensions
) {
    val isEnabled = viewModel.isMonthEnabled(month)
    val record = state.savedRecords[month]
    val hasData = record != null
    val statusColor = when {
        !isEnabled -> MaterialTheme.colorScheme.onSurface.copy(0.3f)
        hasData -> Color(0xFF22C55E)
        else -> customColors.warning
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(dimensions.cardCornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = if (isEnabled) customColors.glassBackground
            else MaterialTheme.colorScheme.surfaceVariant.copy(0.3f)
        ),
        elevation = CardDefaults.cardElevation(if (isEnabled) 2.dp else 0.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(dimensions.spacingMedium)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall),
                modifier = Modifier.fillMaxWidth()
            ) {
                // CHANGED: 40.dp → dimensions.devMonthBadgeSize
                Box(
                    modifier = Modifier.size(dimensions.devMonthBadgeSize).clip(CircleShape)
                        .background(statusColor.copy(0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$month",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isEnabled) statusColor
                        else MaterialTheme.colorScheme.onSurface.copy(0.4f)
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        text = stringResource(Res.string.child_dev_month_label, month),
                        style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold,
                        color = if (isEnabled) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurface.copy(0.4f)
                    )
                    Text(
                        text = when {
                            !isEnabled -> stringResource(Res.string.child_dev_locked_hint)
                            hasData -> stringResource(Res.string.child_dev_status_recorded)
                            else -> stringResource(Res.string.child_dev_status_not_recorded)
                        },
                        style = MaterialTheme.typography.bodySmall, color = statusColor
                    )
                }
                if (!isEnabled) {
                    Icon(
                        Icons.Default.Lock, null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(0.3f),
                        modifier = Modifier.size(dimensions.iconSmall)
                    )
                } else {
                    // CHANGED: 36.dp → dimensions.devEditButtonSize
                    IconButton(
                        onClick = { viewModel.startEditing(month) },
                        modifier = Modifier.size(dimensions.devEditButtonSize)
                    ) {
                        Icon(
                            if (hasData) Icons.Default.Edit else Icons.Default.Add,
                            contentDescription = null,
                            tint = customColors.accentGradientStart,
                            modifier = Modifier.size(dimensions.iconMedium)
                        )
                    }
                }
            }

            if (isEnabled && hasData) {
                Spacer(Modifier.height(dimensions.spacingSmall))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(0.07f))
                Spacer(Modifier.height(dimensions.spacingSmall))
                HearingSpeechChecklistPreview(
                    month = month,
                    record = record!!,
                    dimensions = dimensions
                )
            }

            if (!isEnabled) {
                Spacer(Modifier.height(dimensions.spacingXSmall))
                Text(
                    text = stringResource(Res.string.child_dev_locked_desc, month),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.35f),
                    modifier = Modifier.padding(start = 48.dp)
                )
            }
        }
    }
}

@Composable
private fun HearingSpeechChecklistPreview(
    month: Int, record: HearingSpeechMonthState, dimensions: Dimensions
) {
    val items: List<Boolean?> = when (month) {
        1 -> listOf(
            record.m1StartlesFixatesAttentive, record.m1TurnsToSoundBrief,
            record.m1CriesHungerDiscomfort, record.m1PrefersVoicesOverSounds
        )

        3 -> listOf(
            record.m3CalmWithLoudSound,
            record.m3CalmsWithMothersVoice,
            record.m3LocalizesSoundSource,
            record.m3VocalDuringFeeding,
            record.m3RespondsToNearbySound
        )

        6 -> listOf(
            record.m6LocatesMothersVoice,
            record.m6VocalizesSoundsBabbles,
            record.m6SmilesImitateSpeech
        )

        9 -> listOf(
            record.m9AwareOfDailySounds,
            record.m9AttemptsReciprocalTalking,
            record.m9CallsForAttention,
            record.m9ReduplicatedBabble,
            record.m9RespondsToSimpleQuestions
        )

        12 -> listOf(
            record.m12RespondsToOwnName, record.m12MeaningfulWords,
            record.m12UnderstandsSimpleCommands, record.m12GivesTakesOnRequest
        )

        else -> emptyList()
    }
    val checkedCount = items.count { it == true }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall),
        modifier = Modifier.fillMaxWidth()
    ) {
        items.forEach { value ->
            Box(
                modifier = Modifier.size(dimensions.devIndicatorDotSize).clip(CircleShape)
                    .background(
                        when (value) {
                            true -> Color(0xFF22C55E)
                            false -> MaterialTheme.colorScheme.error.copy(0.7f)
                            null -> MaterialTheme.colorScheme.onSurface.copy(0.2f)
                        }
                    )
            )
        }
        Spacer(Modifier.weight(1f))
        Text(
            text = "$checkedCount / ${items.size}",
            style = MaterialTheme.typography.labelSmall,
            color = if (checkedCount == items.size) Color(0xFF22C55E)
            else MaterialTheme.colorScheme.onSurface.copy(0.5f),
            fontWeight = FontWeight.SemiBold
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HearingSpeechEditPanel(
    state: HearingSpeechUiState, viewModel: HearingSpeechViewModel,
    babyId: String, customColors: CustomColors, dimensions: Dimensions
) {
    val month = state.editingMonth ?: return
    val editing = state.editingState ?: return

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            stringResource(Res.string.child_dev_hearing_speech_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = customColors.accentGradientStart
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
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, null,
                            tint = customColors.accentGradientStart
                        )
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
                .background(
                    Brush.verticalGradient(
                        listOf(
                            customColors.accentGradientStart.copy(0.12f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
                .padding(padding)
        ) {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    customColors.accentGradientStart.copy(0.6f),
                                    customColors.accentGradientEnd.copy(0.45f)
                                )
                            ),
                            RoundedCornerShape(
                                bottomStart = dimensions.cardCornerRadius,
                                bottomEnd = dimensions.cardCornerRadius
                            )
                        )
                        .padding(
                            horizontal = dimensions.spacingLarge,
                            vertical = dimensions.spacingMedium
                        )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
                    ) {
                        Text(
                            stringResource(Res.string.child_dev_hearing_emoji),
                            style = MaterialTheme.typography.titleLarge
                        )
                        Column {
                            Text(
                                stringResource(Res.string.child_dev_hearing_speech_subtitle),
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

                Column(
                    modifier = Modifier.padding(horizontal = dimensions.screenPadding),
                    verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
                ) {
                    when (month) {
                        1 -> HearingSpeechMonth1Fields(editing, viewModel, customColors, dimensions)
                        3 -> HearingSpeechMonth3Fields(editing, viewModel, customColors, dimensions)
                        6 -> HearingSpeechMonth6Fields(editing, viewModel, customColors, dimensions)
                        9 -> HearingSpeechMonth9Fields(editing, viewModel, customColors, dimensions)
                        12 -> HearingSpeechMonth12Fields(
                            editing,
                            viewModel,
                            customColors,
                            dimensions
                        )
                    }

                    Spacer(Modifier.height(dimensions.spacingMedium))

                    ChildDevSaveButton(
                        isSaving = state.isSaving,
                        onSave = { viewModel.save(babyId) },
                        onCancel = viewModel::cancelEditing,
                        customColors = customColors,
                        dimensions = dimensions
                    )

                    Spacer(Modifier.height(dimensions.spacingXXLarge))
                }
            }
        }
    }
}

@Composable
private fun HearingSpeechMonth1Fields(
    s: HearingSpeechMonthState,
    vm: HearingSpeechViewModel,
    cc: CustomColors,
    d: Dimensions
) {
    DevCheckItem(
        stringResource(Res.string.hs_m1_1), s.m1StartlesFixatesAttentive,
        {
            vm.updateField {
                it.copy(
                    m1StartlesFixatesAttentive = it.m1StartlesFixatesAttentive?.not() ?: true
                )
            }
        }, cc, d
    )
    DevCheckItem(
        stringResource(Res.string.hs_m1_2),
        s.m1TurnsToSoundBrief,
        { vm.updateField { it.copy(m1TurnsToSoundBrief = it.m1TurnsToSoundBrief?.not() ?: true) } },
        cc,
        d
    )
    DevCheckItem(
        stringResource(Res.string.hs_m1_3), s.m1CriesHungerDiscomfort,
        {
            vm.updateField {
                it.copy(
                    m1CriesHungerDiscomfort = it.m1CriesHungerDiscomfort?.not() ?: true
                )
            }
        }, cc, d
    )
    DevCheckItem(
        stringResource(Res.string.hs_m1_4), s.m1PrefersVoicesOverSounds,
        {
            vm.updateField {
                it.copy(
                    m1PrefersVoicesOverSounds = it.m1PrefersVoicesOverSounds?.not() ?: true
                )
            }
        }, cc, d
    )
}

@Composable
private fun HearingSpeechMonth3Fields(
    s: HearingSpeechMonthState,
    vm: HearingSpeechViewModel,
    cc: CustomColors,
    d: Dimensions
) {
    DevCheckItem(
        stringResource(Res.string.hs_m3_1),
        s.m3CalmWithLoudSound,
        { vm.updateField { it.copy(m3CalmWithLoudSound = it.m3CalmWithLoudSound?.not() ?: true) } },
        cc,
        d
    )
    DevCheckItem(
        stringResource(Res.string.hs_m3_2), s.m3CalmsWithMothersVoice,
        {
            vm.updateField {
                it.copy(
                    m3CalmsWithMothersVoice = it.m3CalmsWithMothersVoice?.not() ?: true
                )
            }
        }, cc, d
    )
    DevCheckItem(
        stringResource(Res.string.hs_m3_3), s.m3LocalizesSoundSource,
        {
            vm.updateField {
                it.copy(
                    m3LocalizesSoundSource = it.m3LocalizesSoundSource?.not() ?: true
                )
            }
        }, cc, d
    )
    DevCheckItem(
        stringResource(Res.string.hs_m3_4), s.m3VocalDuringFeeding,
        {
            vm.updateField {
                it.copy(
                    m3VocalDuringFeeding = it.m3VocalDuringFeeding?.not() ?: true
                )
            }
        }, cc, d
    )
    DevCheckItem(
        stringResource(Res.string.hs_m3_5), s.m3RespondsToNearbySound,
        {
            vm.updateField {
                it.copy(
                    m3RespondsToNearbySound = it.m3RespondsToNearbySound?.not() ?: true
                )
            }
        }, cc, d
    )
}

@Composable
private fun HearingSpeechMonth6Fields(
    s: HearingSpeechMonthState,
    vm: HearingSpeechViewModel,
    cc: CustomColors,
    d: Dimensions
) {
    DevCheckItem(
        stringResource(Res.string.hs_m6_1), s.m6LocatesMothersVoice,
        {
            vm.updateField {
                it.copy(
                    m6LocatesMothersVoice = it.m6LocatesMothersVoice?.not() ?: true
                )
            }
        }, cc, d
    )
    DevCheckItem(
        stringResource(Res.string.hs_m6_2), s.m6VocalizesSoundsBabbles,
        {
            vm.updateField {
                it.copy(
                    m6VocalizesSoundsBabbles = it.m6VocalizesSoundsBabbles?.not() ?: true
                )
            }
        }, cc, d
    )
    DevCheckItem(
        stringResource(Res.string.hs_m6_3), s.m6SmilesImitateSpeech,
        {
            vm.updateField {
                it.copy(
                    m6SmilesImitateSpeech = it.m6SmilesImitateSpeech?.not() ?: true
                )
            }
        }, cc, d
    )
}

@Composable
private fun HearingSpeechMonth9Fields(
    s: HearingSpeechMonthState,
    vm: HearingSpeechViewModel,
    cc: CustomColors,
    d: Dimensions
) {
    DevCheckItem(
        stringResource(Res.string.hs_m9_1), s.m9AwareOfDailySounds,
        {
            vm.updateField {
                it.copy(
                    m9AwareOfDailySounds = it.m9AwareOfDailySounds?.not() ?: true
                )
            }
        }, cc, d
    )
    DevCheckItem(
        stringResource(Res.string.hs_m9_2), s.m9AttemptsReciprocalTalking,
        {
            vm.updateField {
                it.copy(
                    m9AttemptsReciprocalTalking = it.m9AttemptsReciprocalTalking?.not() ?: true
                )
            }
        }, cc, d
    )
    DevCheckItem(
        stringResource(Res.string.hs_m9_3),
        s.m9CallsForAttention,
        { vm.updateField { it.copy(m9CallsForAttention = it.m9CallsForAttention?.not() ?: true) } },
        cc,
        d
    )
    DevCheckItem(
        stringResource(Res.string.hs_m9_4), s.m9ReduplicatedBabble,
        {
            vm.updateField {
                it.copy(
                    m9ReduplicatedBabble = it.m9ReduplicatedBabble?.not() ?: true
                )
            }
        }, cc, d
    )
    DevCheckItem(
        stringResource(Res.string.hs_m9_5), s.m9RespondsToSimpleQuestions,
        {
            vm.updateField {
                it.copy(
                    m9RespondsToSimpleQuestions = it.m9RespondsToSimpleQuestions?.not() ?: true
                )
            }
        }, cc, d
    )
}

@Composable
private fun HearingSpeechMonth12Fields(
    s: HearingSpeechMonthState,
    vm: HearingSpeechViewModel,
    cc: CustomColors,
    d: Dimensions
) {
    DevCheckItem(
        stringResource(Res.string.hs_m12_1), s.m12RespondsToOwnName,
        {
            vm.updateField {
                it.copy(
                    m12RespondsToOwnName = it.m12RespondsToOwnName?.not() ?: true
                )
            }
        }, cc, d
    )
    DevCheckItem(
        stringResource(Res.string.hs_m12_2),
        s.m12MeaningfulWords,
        { vm.updateField { it.copy(m12MeaningfulWords = it.m12MeaningfulWords?.not() ?: true) } },
        cc,
        d
    )
    DevCheckItem(
        stringResource(Res.string.hs_m12_3), s.m12UnderstandsSimpleCommands,
        {
            vm.updateField {
                it.copy(
                    m12UnderstandsSimpleCommands = it.m12UnderstandsSimpleCommands?.not() ?: true
                )
            }
        }, cc, d
    )
    DevCheckItem(
        stringResource(Res.string.hs_m12_4), s.m12GivesTakesOnRequest,
        {
            vm.updateField {
                it.copy(
                    m12GivesTakesOnRequest = it.m12GivesTakesOnRequest?.not() ?: true
                )
            }
        }, cc, d
    )
}