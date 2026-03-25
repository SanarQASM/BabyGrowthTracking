package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import babygrowthtrackingapplication.composeapp.generated.resources.Res
import babygrowthtrackingapplication.composeapp.generated.resources.*
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model.HealthRecordViewModel
import org.example.project.babygrowthtrackingapplication.theme.*
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

// ─────────────────────────────────────────────────────────────────────────────
// Add Health Issue Screen
// A full-screen form consistent with AddMeasurementScreen / AddBabyScreen.
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun AddHealthIssueScreen(
    babyId    : String,
    babyName  : String,
    viewModel : HealthRecordViewModel,
    onBack    : () -> Unit,
    onSaved   : () -> Unit
) {
    val customColors = MaterialTheme.customColors
    val dimensions   = LocalDimensions.current

    // ── Form state ────────────────────────────────────────────────────────────
    var title       by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var severity    by remember { mutableStateOf("MILD") }
    var issueDate   by remember { mutableStateOf("") }
    var isResolved  by remember { mutableStateOf(false) }

    // Auto-fill today
    LaunchedEffect(Unit) {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        issueDate = "${now.year}-${now.month.number.toString().padStart(2, '0')}-${now.dayOfMonth.toString().padStart(2, '0')}"
    }

    var showDatePicker  by remember { mutableStateOf(false) }
    var titleError      by remember { mutableStateOf(false) }
    var isLoading       by remember { mutableStateOf(false) }

    val state = viewModel.uiState

    // Navigate back on success
    LaunchedEffect(state.successMessage) {
        if (state.successMessage?.contains("added") == true) {
            isLoading = false
            onSaved()
        }
    }
    LaunchedEffect(state.error) {
        if (isLoading && state.error != null) isLoading = false
    }

    // Date picker
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
                            val date = instant.toLocalDateTime(TimeZone.UTC).date
                            issueDate = "${date.year}-${date.month.number.toString().padStart(2, '0')}-${date.dayOfMonth.toString().padStart(2, '0')}"
                        }
                        showDatePicker = false
                    },
                    shape = RoundedCornerShape(dimensions.buttonCornerRadius),
                    colors = ButtonDefaults.buttonColors(containerColor = customColors.accentGradientStart)
                ) { Text(stringResource(Res.string.add_baby_date_ok), fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDatePicker = false }, shape = RoundedCornerShape(dimensions.buttonCornerRadius)) {
                    Text(stringResource(Res.string.add_baby_date_cancel))
                }
            }
        ) { DatePicker(state = datePickerState) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Log Health Issue",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = customColors.accentGradientStart
                        )
                        Text(
                            text = babyName,
                            style = MaterialTheme.typography.bodySmall,
                            color = customColors.accentGradientStart.copy(0.6f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.common_back),
                            tint = customColors.accentGradientStart
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = customColors.accentGradientStart.copy(alpha = 0.15f),
                    titleContentColor = customColors.accentGradientStart,
                    navigationIconContentColor = customColors.accentGradientStart
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
                                topEnd = dimensions.cardCornerRadius
                            )
                        )
                        .padding(bottom = dimensions.spacingXLarge)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = dimensions.spacingLarge, vertical = dimensions.spacingXLarge),
                        verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)
                    ) {

                        // ── Issue Date ────────────────────────────────────────
                        HISectionCard(title = "ISSUE DATE") {
                            HITextField(
                                value = issueDate,
                                onValueChange = {},
                                placeholder = "YYYY-MM-DD",
                                readOnly = true,
                                trailingIcon = {
                                    IconButton(onClick = { showDatePicker = true }) {
                                        Icon(
                                            Icons.Default.DateRange,
                                            contentDescription = "Pick date",
                                            tint = customColors.accentGradientEnd,
                                            modifier = Modifier.size(dimensions.iconMedium)
                                        )
                                    }
                                }
                            )
                        }

                        // ── Title ──────────────────────────────────────────────
                        HISectionCard(title = "TITLE") {
                            HITextField(
                                value = title,
                                onValueChange = { title = it; titleError = false },
                                placeholder = "e.g. Fever, Rash, Cough…",
                                isError = titleError,
                                leadingEmoji = "📋"
                            )
                            AnimatedVisibility(visible = titleError) {
                                Text(
                                    "Title is required",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(start = dimensions.spacingXSmall, top = 2.dp)
                                )
                            }
                        }

                        // ── Description ────────────────────────────────────────
                        HISectionCard(title = "DESCRIPTION (OPTIONAL)") {
                            HITextField(
                                value = description,
                                onValueChange = { description = it },
                                placeholder = "Describe symptoms, observations…",
                                minLines = 3,
                                maxLines = 5,
                                leadingEmoji = "📝"
                            )
                        }

                        // ── Severity ───────────────────────────────────────────
                        HISectionCard(title = "SEVERITY") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
                            ) {
                                listOf(
                                    Triple("MILD", "🟢", Color(0xFF22C55E)),
                                    Triple("MODERATE", "🟡", Color(0xFFF59E0B)),
                                    Triple("SEVERE", "🔴", MaterialTheme.colorScheme.error)
                                ).forEach { (value, emoji, color) ->
                                    val selected = severity == value
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(
                                                if (selected) color.copy(0.18f) else MaterialTheme.colorScheme.surface.copy(0.15f),
                                                RoundedCornerShape(dimensions.buttonCornerRadius - 4.dp)
                                            )
                                            .border(
                                                if (selected) dimensions.borderWidthMedium else dimensions.borderWidthThin,
                                                if (selected) color.copy(0.7f) else MaterialTheme.colorScheme.onPrimary.copy(0.2f),
                                                RoundedCornerShape(dimensions.buttonCornerRadius - 4.dp)
                                            )
                                            .clickable { severity = value }
                                            .padding(vertical = dimensions.spacingSmall + 2.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Text(emoji, style = MaterialTheme.typography.titleSmall)
                                            Text(
                                                value.lowercase().replaceFirstChar { it.uppercase() },
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (selected) color else MaterialTheme.colorScheme.onPrimary.copy(0.75f)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // ── Is Resolved ────────────────────────────────────────
                        HISectionCard(title = "STATUS") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isResolved = !isResolved }
                                    .padding(vertical = dimensions.spacingXSmall),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(if (isResolved) "😊" else "🤒", style = MaterialTheme.typography.titleMedium)
                                    Column {
                                        Text(
                                            if (isResolved) "Resolved" else "Ongoing",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                        Text(
                                            "Tap to toggle",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onPrimary.copy(0.55f)
                                        )
                                    }
                                }
                                Switch(
                                    checked = isResolved,
                                    onCheckedChange = { isResolved = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color(0xFF22C55E),
                                        checkedTrackColor = Color(0xFF22C55E).copy(0.3f)
                                    )
                                )
                            }
                        }

                        // ── Error message ─────────────────────────────────────
                        state.error?.let { msg ->
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
                                Text(msg, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                            }
                        }

                        Spacer(Modifier.height(dimensions.spacingSmall))

                        // ── Save button ───────────────────────────────────────
                        HIActionButton(
                            label = if (isLoading) "Saving…" else "Save Health Issue",
                            isLoading = isLoading,
                            enabled = !isLoading,
                            containerColor = customColors.accentGradientStart,
                            onClick = {
                                if (title.isBlank()) { titleError = true; return@HIActionButton }
                                isLoading = true
                                viewModel.addHealthIssue(
                                    babyId = babyId,
                                    title = title.trim(),
                                    description = description.ifBlank { null },
                                    severity = severity,
                                    issueDate = issueDate
                                )
                            }
                        )

                        Spacer(Modifier.height(dimensions.spacingXSmall))

                        // ── Cancel ────────────────────────────────────────────
                        HIActionButton(
                            label = "Cancel",
                            isLoading = false,
                            enabled = !isLoading,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.65f),
                            labelColor = MaterialTheme.colorScheme.onSurface,
                            borderColor = MaterialTheme.colorScheme.onSurface.copy(0.18f),
                            onClick = onBack
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Add Appointment Screen
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun AddAppointmentScreen(
    babyId   : String,
    babyName : String,
    viewModel: HealthRecordViewModel,
    onBack   : () -> Unit,
    onSaved  : () -> Unit
) {
    val customColors = MaterialTheme.customColors
    val dimensions   = LocalDimensions.current

    var appointmentType by remember { mutableStateOf("REGULAR_CHECKUP") }
    var scheduledDate   by remember { mutableStateOf("") }
    var scheduledTime   by remember { mutableStateOf("") }
    var doctorName      by remember { mutableStateOf("") }
    var location        by remember { mutableStateOf("") }
    var notes           by remember { mutableStateOf("") }
    var dateError       by remember { mutableStateOf(false) }
    var isLoading       by remember { mutableStateOf(false) }
    var showDatePicker  by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        scheduledDate = "${now.year}-${now.month.number.toString().padStart(2, '0')}-${now.dayOfMonth.toString().padStart(2, '0')}"
    }

    val state = viewModel.uiState
    LaunchedEffect(state.successMessage) {
        if (state.successMessage?.contains("added") == true) { isLoading = false; onSaved() }
    }
    LaunchedEffect(state.error) {
        if (isLoading && state.error != null) isLoading = false
    }

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
                            val date = instant.toLocalDateTime(TimeZone.UTC).date
                            scheduledDate = "${date.year}-${date.month.number.toString().padStart(2, '0')}-${date.dayOfMonth.toString().padStart(2, '0')}"
                            dateError = false
                        }
                        showDatePicker = false
                    },
                    shape = RoundedCornerShape(dimensions.buttonCornerRadius),
                    colors = ButtonDefaults.buttonColors(containerColor = customColors.accentGradientStart)
                ) { Text(stringResource(Res.string.add_baby_date_ok), fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDatePicker = false }, shape = RoundedCornerShape(dimensions.buttonCornerRadius)) {
                    Text(stringResource(Res.string.add_baby_date_cancel))
                }
            }
        ) { DatePicker(state = datePickerState) }
    }

    val appointmentTypes = listOf(
        Triple("REGULAR_CHECKUP", "🩺", "Checkup"),
        Triple("VACCINATION", "💉", "Vaccination"),
        Triple("CONSULTATION", "👨‍⚕️", "Consultation"),
        Triple("FOLLOW_UP", "🔁", "Follow-up"),
        Triple("EMERGENCY", "🚨", "Emergency")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Log Appointment",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = customColors.accentGradientStart
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
                            contentDescription = stringResource(Res.string.common_back),
                            tint = customColors.accentGradientStart
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = customColors.accentGradientStart.copy(alpha = 0.15f),
                    titleContentColor = customColors.accentGradientStart,
                    navigationIconContentColor = customColors.accentGradientStart
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
                            RoundedCornerShape(topStart = dimensions.cardCornerRadius, topEnd = dimensions.cardCornerRadius)
                        )
                        .padding(bottom = dimensions.spacingXLarge)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = dimensions.spacingLarge, vertical = dimensions.spacingXLarge),
                        verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)
                    ) {

                        // ── Appointment Type ──────────────────────────────────
                        HISectionCard(title = "APPOINTMENT TYPE") {
                            Column(verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)) {
                                appointmentTypes.chunked(3).forEach { row ->
                                    Row(horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)) {
                                        row.forEach { (value, emoji, label) ->
                                            val selected = appointmentType == value
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .background(
                                                        if (selected) customColors.accentGradientStart.copy(0.2f)
                                                        else MaterialTheme.colorScheme.surface.copy(0.15f),
                                                        RoundedCornerShape(dimensions.buttonCornerRadius - 4.dp)
                                                    )
                                                    .border(
                                                        if (selected) dimensions.borderWidthMedium else dimensions.borderWidthThin,
                                                        if (selected) customColors.accentGradientStart.copy(0.7f)
                                                        else MaterialTheme.colorScheme.onPrimary.copy(0.2f),
                                                        RoundedCornerShape(dimensions.buttonCornerRadius - 4.dp)
                                                    )
                                                    .clickable { appointmentType = value }
                                                    .padding(vertical = dimensions.spacingSmall + 2.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                    Text(emoji, style = MaterialTheme.typography.titleSmall)
                                                    Text(
                                                        label,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                                        color = if (selected) customColors.accentGradientStart
                                                        else MaterialTheme.colorScheme.onPrimary.copy(0.75f)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // ── Date ──────────────────────────────────────────────
                        HISectionCard(title = "DATE") {
                            HITextField(
                                value = scheduledDate,
                                onValueChange = {},
                                placeholder = "YYYY-MM-DD",
                                readOnly = true,
                                isError = dateError,
                                trailingIcon = {
                                    IconButton(onClick = { showDatePicker = true }) {
                                        Icon(
                                            Icons.Default.DateRange,
                                            contentDescription = "Pick date",
                                            tint = customColors.accentGradientEnd,
                                            modifier = Modifier.size(dimensions.iconMedium)
                                        )
                                    }
                                }
                            )
                            AnimatedVisibility(visible = dateError) {
                                Text("Date is required", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(start = dimensions.spacingXSmall, top = 2.dp))
                            }
                        }

                        // ── Time ──────────────────────────────────────────────
                        HISectionCard(title = "TIME (OPTIONAL)") {
                            HITextField(
                                value = scheduledTime,
                                onValueChange = { scheduledTime = it },
                                placeholder = "HH:MM  (e.g. 10:30)",
                                keyboardType = KeyboardType.Number,
                                leadingEmoji = "🕐"
                            )
                        }

                        // ── Doctor Name ───────────────────────────────────────
                        HISectionCard(title = "DOCTOR NAME (OPTIONAL)") {
                            HITextField(
                                value = doctorName,
                                onValueChange = { doctorName = it },
                                placeholder = "Dr. …",
                                leadingEmoji = "👨‍⚕️"
                            )
                        }

                        // ── Location ──────────────────────────────────────────
                        HISectionCard(title = "LOCATION (OPTIONAL)") {
                            HITextField(
                                value = location,
                                onValueChange = { location = it },
                                placeholder = "Hospital / Clinic name",
                                leadingEmoji = "🏥"
                            )
                        }

                        // ── Notes ─────────────────────────────────────────────
                        HISectionCard(title = "NOTES (OPTIONAL)") {
                            HITextField(
                                value = notes,
                                onValueChange = { notes = it },
                                placeholder = "Any additional notes…",
                                minLines = 2,
                                maxLines = 4,
                                leadingEmoji = "📝"
                            )
                        }

                        // ── Error ─────────────────────────────────────────────
                        state.error?.let { msg ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.error.copy(0.15f), RoundedCornerShape(dimensions.buttonCornerRadius - 4.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.error.copy(0.4f), RoundedCornerShape(dimensions.buttonCornerRadius - 4.dp))
                                    .padding(dimensions.spacingMedium)
                            ) {
                                Text(msg, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                            }
                        }

                        Spacer(Modifier.height(dimensions.spacingSmall))

                        HIActionButton(
                            label = if (isLoading) "Saving…" else "Save Appointment",
                            isLoading = isLoading,
                            enabled = !isLoading,
                            containerColor = customColors.accentGradientStart,
                            onClick = {
                                if (scheduledDate.isBlank()) { dateError = true; return@HIActionButton }
                                isLoading = true
                                viewModel.addAppointment(
                                    babyId = babyId,
                                    type = appointmentType,
                                    date = scheduledDate,
                                    time = scheduledTime.ifBlank { null },
                                    doctorName = doctorName.ifBlank { null },
                                    location = location.ifBlank { null },
                                    notes = notes.ifBlank { null }
                                )
                            }
                        )

                        Spacer(Modifier.height(dimensions.spacingXSmall))

                        HIActionButton(
                            label = "Cancel",
                            isLoading = false,
                            enabled = !isLoading,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.65f),
                            labelColor = MaterialTheme.colorScheme.onSurface,
                            borderColor = MaterialTheme.colorScheme.onSurface.copy(0.18f),
                            onClick = onBack
                        )
                    }
                }
            }
        }
    }
}

// =============================================================================
// Shared internal composables (used by both screens)
// =============================================================================

/** Section header + glass-card wrapper — same as MeasureSectionCard */
@Composable
private fun HISectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    val customColors = MaterialTheme.customColors
    val dimensions   = LocalDimensions.current
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary,
            letterSpacing = MaterialTheme.typography.labelLarge.letterSpacing,
            modifier = Modifier.padding(bottom = dimensions.spacingSmall)
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
                .padding(horizontal = dimensions.spacingMedium, vertical = dimensions.spacingMedium)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) { content() }
        }
    }
}

/** Glass-style text field — same aesthetic as MeasureTextField */
@Composable
private fun HITextField(
    value        : String,
    onValueChange: (String) -> Unit,
    placeholder  : String,
    keyboardType : KeyboardType = KeyboardType.Text,
    trailingIcon : @Composable (() -> Unit)? = null,
    leadingEmoji : String? = null,
    isError      : Boolean = false,
    readOnly     : Boolean = false,
    minLines     : Int = 1,
    maxLines     : Int = 1
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
                if (isError) dimensions.borderWidthMedium / 2 else dimensions.spacingXSmall / 4,
                if (isError) MaterialTheme.colorScheme.error.copy(0.6f)
                else customColors.accentGradientStart.copy(alpha = 0.35f),
                RoundedCornerShape(dimensions.buttonCornerRadius - 4.dp)
            )
    ) {
        TextField(
            value         = value,
            onValueChange = if (readOnly) ({}) else onValueChange,
            placeholder   = {
                Text(placeholder, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(0.45f))
            },
            leadingIcon = leadingEmoji?.let { emoji ->
                { Text(emoji, style = MaterialTheme.typography.bodyLarge) }
            },
            trailingIcon   = trailingIcon,
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
                errorIndicatorColor     = Color.Transparent,
                cursorColor             = customColors.accentGradientStart
            ),
            readOnly  = readOnly,
            minLines  = minLines,
            maxLines  = maxLines,
            modifier  = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = textColor)
        )
    }
}

/** Primary/secondary action button — same as buttons in AddMeasurementScreen */
@Composable
private fun HIActionButton(
    label          : String,
    isLoading      : Boolean,
    enabled        : Boolean,
    containerColor : Color,
    labelColor     : Color = MaterialTheme.colorScheme.onPrimary,
    borderColor    : Color = Color.Transparent,
    onClick        : () -> Unit
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
                    .background(customColors.glassOverlay, RoundedCornerShape(dimensions.buttonCornerRadius))
            )
            if (isLoading) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)) {
                    CircularProgressIndicator(modifier = Modifier.size(dimensions.iconMedium), color = labelColor, strokeWidth = dimensions.spacingXSmall / 2)
                    Text(label, color = labelColor, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
                }
            } else {
                Text(label, color = labelColor, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}