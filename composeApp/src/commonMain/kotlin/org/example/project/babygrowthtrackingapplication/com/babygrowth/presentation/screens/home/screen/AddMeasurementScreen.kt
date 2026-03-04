package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.screen

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.*
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.data.PreferencesManager
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model.AddMeasurementViewModel
import org.example.project.babygrowthtrackingapplication.data.network.ApiService
import org.example.project.babygrowthtrackingapplication.theme.*
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

// ─────────────────────────────────────────────────────────────────────────────
// Screen
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun AddMeasurementScreen(
    babyId             : String,
    babyName           : String,
    apiService         : ApiService,
    preferencesManager : PreferencesManager,
    onBack             : () -> Unit,
    onSaved            : () -> Unit
) {
    val viewModel    = remember { AddMeasurementViewModel(apiService, preferencesManager) }
    val state        = viewModel.uiState
    val customColors = MaterialTheme.customColors
    val dimensions   = LocalDimensions.current

    var showDatePicker by remember { mutableStateOf(false) }

    // ✅ Trigger onSaved once after successful save, then destroy the ViewModel
    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            viewModel.onDestroy()
            onSaved()
        }
    }

    // ── Date picker ───────────────────────────────────────────────────────────
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = Clock.System.now().toEpochMilliseconds()
    )
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                Button(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val instant = Instant.fromEpochMilliseconds(millis)
                            val date    = instant.toLocalDateTime(TimeZone.UTC).date
                            val fmt     = "${date.year}-${date.month.number.toString().padStart(2, '0')}-${date.dayOfMonth.toString().padStart(2, '0')}"
                            viewModel.onDateChange(fmt)
                        }
                        showDatePicker = false
                    },
                    shape  = RoundedCornerShape(dimensions.buttonCornerRadius),
                    colors = ButtonDefaults.buttonColors(containerColor = customColors.accentGradientStart)
                ) { Text("OK", fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDatePicker = false },
                    shape   = RoundedCornerShape(dimensions.buttonCornerRadius)
                ) { Text("Cancel") }
            }
        ) { DatePicker(state = datePickerState, showModeToggle = true) }
    }

    BabyGrowthTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text       = "Add Measurement",
                                fontWeight = FontWeight.Bold,
                                style      = MaterialTheme.typography.titleMedium,
                                color      = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text  = babyName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onBackground.copy(0.6f)
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = customColors.accentGradientStart.copy(alpha = 0.15f)
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
                                customColors.accentGradientStart.copy(0.15f),
                                customColors.accentGradientEnd.copy(0.25f)
                            )
                        )
                    )
                    .padding(padding)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(Modifier.height(dimensions.spacingMedium))

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
                                    topStart = dimensions.cardCornerRadius,
                                    topEnd   = dimensions.cardCornerRadius
                                )
                            )
                            .padding(bottom = dimensions.spacingXLarge)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = dimensions.spacingLarge,
                                    vertical   = dimensions.spacingXLarge
                                ),
                            verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)
                        ) {

                            // ── Date field ────────────────────────────────────
                            MeasureSectionCard(title = "MEASUREMENT DATE") {
                                MeasureTextField(
                                    value         = state.measurementDate,
                                    onValueChange = {},
                                    placeholder   = "YYYY-MM-DD",
                                    readOnly      = true,
                                    trailingIcon  = {
                                        IconButton(onClick = { showDatePicker = true }) {
                                            Icon(
                                                Icons.Default.DateRange,
                                                contentDescription = "Pick date",
                                                tint     = customColors.accentGradientEnd,
                                                modifier = Modifier.size(dimensions.iconMedium)
                                            )
                                        }
                                    }
                                )
                            }

                            // ── Measurements ──────────────────────────────────
                            MeasureSectionCard(title = "MEASUREMENTS") {
                                MeasureTextField(
                                    value         = state.weight,
                                    onValueChange = viewModel::onWeightChange,
                                    placeholder   = "Weight",
                                    keyboardType  = KeyboardType.Decimal,
                                    trailingText  = "kg",
                                    leadingEmoji  = "⚖️"
                                )
                                Spacer(Modifier.height(dimensions.spacingSmall + dimensions.spacingXSmall))
                                MeasureTextField(
                                    value         = state.height,
                                    onValueChange = viewModel::onHeightChange,
                                    placeholder   = "Height",
                                    keyboardType  = KeyboardType.Decimal,
                                    trailingText  = "cm",
                                    leadingEmoji  = "📏"
                                )
                                Spacer(Modifier.height(dimensions.spacingSmall + dimensions.spacingXSmall))
                                MeasureTextField(
                                    value         = state.headCircumference,
                                    onValueChange = viewModel::onHeadChange,
                                    placeholder   = "Head Circumference",
                                    keyboardType  = KeyboardType.Decimal,
                                    trailingText  = "cm",
                                    leadingEmoji  = "🔵"
                                )
                            }

                            // ── Error message ─────────────────────────────────
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
                                        text       = msg,
                                        color      = MaterialTheme.colorScheme.error,
                                        style      = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Spacer(Modifier.height(dimensions.spacingSmall))

                            // ── Save button ───────────────────────────────────
                            Button(
                                // ✅ onClick guard: isLoading checked in ViewModel too,
                                //    but also disable the button in the UI for clarity
                                onClick        = { viewModel.saveMeasurement(babyId) },
                                enabled        = !state.isLoading,
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
                                            if (!state.isLoading)
                                                Brush.verticalGradient(
                                                    listOf(
                                                        customColors.accentGradientStart.copy(0.85f),
                                                        customColors.accentGradientEnd.copy(0.75f)
                                                    )
                                                )
                                            else
                                                Brush.verticalGradient(
                                                    listOf(
                                                        MaterialTheme.colorScheme.onSurface.copy(0.12f),
                                                        MaterialTheme.colorScheme.onSurface.copy(0.08f)
                                                    )
                                                ),
                                            RoundedCornerShape(dimensions.buttonCornerRadius)
                                        )
                                        .border(
                                            dimensions.spacingXSmall / 2,
                                            customColors.accentGradientStart.copy(
                                                if (!state.isLoading) 0.6f else 0.25f
                                            ),
                                            RoundedCornerShape(dimensions.buttonCornerRadius)
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
                                    if (state.isLoading) {
                                        Row(
                                            verticalAlignment     = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
                                        ) {
                                            CircularProgressIndicator(
                                                modifier    = Modifier.size(dimensions.iconMedium),
                                                color       = MaterialTheme.colorScheme.onPrimary,
                                                strokeWidth = dimensions.spacingXSmall / 2
                                            )
                                            Text(
                                                "Saving…",
                                                color      = MaterialTheme.colorScheme.onPrimary,
                                                fontWeight = FontWeight.SemiBold,
                                                style      = MaterialTheme.typography.labelLarge
                                            )
                                        }
                                    } else {
                                        Text(
                                            "💾  Save Measurement",
                                            color      = MaterialTheme.colorScheme.onPrimary,
                                            fontWeight = FontWeight.Bold,
                                            style      = MaterialTheme.typography.labelLarge
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(dimensions.spacingSmall))

                            // ── Cancel button ─────────────────────────────────
                            Button(
                                onClick        = onBack,
                                enabled        = !state.isLoading,
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
                                            Brush.verticalGradient(
                                                listOf(
                                                    MaterialTheme.colorScheme.surfaceVariant.copy(0.65f),
                                                    MaterialTheme.colorScheme.surfaceVariant.copy(0.45f)
                                                )
                                            ),
                                            RoundedCornerShape(dimensions.buttonCornerRadius)
                                        )
                                        .border(
                                            1.dp,
                                            MaterialTheme.colorScheme.onSurface.copy(0.18f),
                                            RoundedCornerShape(dimensions.buttonCornerRadius)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "Cancel",
                                        color      = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.SemiBold,
                                        style      = MaterialTheme.typography.labelLarge
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Section card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun MeasureSectionCard(
    title  : String,
    content: @Composable ColumnScope.() -> Unit
) {
    val customColors = MaterialTheme.customColors
    val dimensions   = LocalDimensions.current

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text          = title,
            style         = MaterialTheme.typography.labelLarge,
            fontWeight    = FontWeight.Bold,
            color         = MaterialTheme.colorScheme.onPrimary,
            letterSpacing = MaterialTheme.typography.labelLarge.letterSpacing,
            modifier      = Modifier.padding(bottom = dimensions.spacingSmall)
        )
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
                    vertical   = dimensions.spacingMedium
                )
        ) {
            Column(modifier = Modifier.fillMaxWidth()) { content() }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Text field
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun MeasureTextField(
    value        : String,
    onValueChange: (String) -> Unit,
    placeholder  : String,
    keyboardType : KeyboardType = KeyboardType.Text,
    trailingText : String? = null,
    trailingIcon : @Composable (() -> Unit)? = null,
    leadingEmoji : String? = null,
    readOnly     : Boolean = false
) {
    val customColors = MaterialTheme.customColors
    val dimensions   = LocalDimensions.current
    val textColor    = MaterialTheme.colorScheme.onSurface

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
            onValueChange = if (readOnly) ({}) else onValueChange,
            placeholder   = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    leadingEmoji?.let { Text(it, modifier = Modifier.padding(end = 6.dp)) }
                    Text(
                        placeholder,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.45f)
                    )
                }
            },
            leadingIcon  = leadingEmoji?.let { emoji ->
                { Text(emoji, style = MaterialTheme.typography.bodyLarge) }
            },
            trailingIcon = trailingIcon ?: trailingText?.let { label ->
                {
                    Text(
                        text       = label,
                        color      = customColors.accentGradientEnd,
                        style      = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        modifier   = Modifier.padding(end = dimensions.spacingXSmall * 2)
                    )
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = TextFieldDefaults.colors(
                focusedTextColor        = textColor,
                unfocusedTextColor      = textColor,
                disabledTextColor       = textColor,
                focusedContainerColor   = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor  = Color.Transparent,
                focusedIndicatorColor   = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor  = Color.Transparent,
                cursorColor             = customColors.accentGradientStart
            ),
            readOnly  = readOnly,
            modifier  = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = textColor)
        )
    }
}