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
// All hardcoded strings → stringResource, all hardcoded dims → dimension tokens
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

    LaunchedEffect(state.successMessage) {
        if (state.successMessage?.contains("added") == true) {
            isLoading = false
            onSaved()
        }
    }
    LaunchedEffect(state.error) {
        if (isLoading && state.error != null) isLoading = false
    }

    // ── Localised strings ─────────────────────────────────────────────────────
    val strAddTitle        = stringResource(Res.string.add_issue_title)
    val strDateSection     = stringResource(Res.string.add_issue_section_date)
    val strTitleSection    = stringResource(Res.string.add_issue_section_title_label)
    val strDescSection     = stringResource(Res.string.add_issue_section_description)
    val strSeveritySection = stringResource(Res.string.add_issue_section_severity)
    val strStatusSection   = stringResource(Res.string.add_issue_section_status)
    val strTitlePlaceholder = stringResource(Res.string.add_issue_title_placeholder)
    val strTitleError      = stringResource(Res.string.add_issue_title_error)
    val strDescPlaceholder = stringResource(Res.string.add_issue_description_placeholder)
    val strResolved        = stringResource(Res.string.add_issue_status_resolved)
    val strOngoing         = stringResource(Res.string.add_issue_status_ongoing)
    val strTapToggle       = stringResource(Res.string.add_issue_status_toggle_hint)
    val strSaveButton      = stringResource(Res.string.add_issue_save_button)
    val strSaving          = stringResource(Res.string.add_issue_saving)
    val strMild            = stringResource(Res.string.add_issue_severity_mild)
    val strModerate        = stringResource(Res.string.add_issue_severity_moderate)
    val strSevere          = stringResource(Res.string.add_issue_severity_severe)
    val strPickDate        = stringResource(Res.string.add_baby_field_dob_pick)
    val strDateOk          = stringResource(Res.string.add_baby_date_ok)
    val strDateCancel      = stringResource(Res.string.add_baby_date_cancel)
    val strBack            = stringResource(Res.string.common_back)
    val strCancel          = stringResource(Res.string.btn_cancel)

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
                ) { Text(strDateOk, fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDatePicker = false }, shape = RoundedCornerShape(dimensions.buttonCornerRadius)) {
                    Text(strDateCancel)
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
                            text = strAddTitle,
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
                            contentDescription = strBack,
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
                        HISectionCard(title = strDateSection) {
                            HITextField(
                                value = issueDate,
                                onValueChange = {},
                                placeholder = stringResource(Res.string.add_measure_date_placeholder),
                                readOnly = true,
                                trailingIcon = {
                                    IconButton(onClick = { showDatePicker = true }) {
                                        Icon(
                                            Icons.Default.DateRange,
                                            contentDescription = strPickDate,
                                            tint = customColors.accentGradientEnd,
                                            modifier = Modifier.size(dimensions.iconMedium)
                                        )
                                    }
                                }
                            )
                        }

                        // ── Title ──────────────────────────────────────────────
                        HISectionCard(title = strTitleSection) {
                            HITextField(
                                value = title,
                                onValueChange = { title = it; titleError = false },
                                placeholder = strTitlePlaceholder,
                                isError = titleError,
                                leadingEmoji = "📋"
                            )
                            AnimatedVisibility(visible = titleError) {
                                Text(
                                    strTitleError,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(start = dimensions.spacingXSmall, top = dimensions.borderWidthMedium)
                                )
                            }
                        }

                        // ── Description ────────────────────────────────────────
                        HISectionCard(title = strDescSection) {
                            HITextField(
                                value = description,
                                onValueChange = { description = it },
                                placeholder = strDescPlaceholder,
                                minLines = 3,
                                maxLines = 5,
                                leadingEmoji = "📝"
                            )
                        }

                        // ── Severity ───────────────────────────────────────────
                        HISectionCard(title = strSeveritySection) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
                            ) {
                                listOf(
                                    Triple("MILD",     "🟢", Color(0xFF22C55E)),
                                    Triple("MODERATE", "🟡", Color(0xFFF59E0B)),
                                    Triple("SEVERE",   "🔴", MaterialTheme.colorScheme.error)
                                ).forEachIndexed { idx, (value, emoji, color) ->
                                    val label = when (idx) {
                                        0 -> strMild
                                        1 -> strModerate
                                        else -> strSevere
                                    }
                                    val selected = severity == value
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(
                                                if (selected) color.copy(0.18f) else MaterialTheme.colorScheme.surface.copy(0.15f),
                                                RoundedCornerShape(dimensions.buttonCornerRadius - dimensions.spacingXSmall)
                                            )
                                            .border(
                                                if (selected) dimensions.borderWidthMedium else dimensions.borderWidthThin,
                                                if (selected) color.copy(0.7f) else MaterialTheme.colorScheme.onPrimary.copy(0.2f),
                                                RoundedCornerShape(dimensions.buttonCornerRadius - dimensions.spacingXSmall)
                                            )
                                            .clickable { severity = value }
                                            .padding(vertical = dimensions.spacingSmall + dimensions.borderWidthMedium),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(dimensions.borderWidthMedium)) {
                                            Text(emoji, style = MaterialTheme.typography.titleSmall)
                                            Text(
                                                label,
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
                        HISectionCard(title = strStatusSection) {
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
                                            if (isResolved) strResolved else strOngoing,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                        Text(
                                            strTapToggle,
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
                                        RoundedCornerShape(dimensions.buttonCornerRadius - dimensions.spacingXSmall)
                                    )
                                    .border(
                                        dimensions.borderWidthThin,
                                        MaterialTheme.colorScheme.error.copy(0.4f),
                                        RoundedCornerShape(dimensions.buttonCornerRadius - dimensions.spacingXSmall)
                                    )
                                    .padding(dimensions.spacingMedium)
                            ) {
                                Text(msg, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                            }
                        }

                        Spacer(Modifier.height(dimensions.spacingSmall))

                        // ── Save button ───────────────────────────────────────
                        HIActionButton(
                            label = if (isLoading) strSaving else strSaveButton,
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
                            label = strCancel,
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
// All hardcoded strings → stringResource, all hardcoded dims → dimension tokens
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

    // ── Localised strings ─────────────────────────────────────────────────────
    val strAddTitle      = stringResource(Res.string.add_appointment_title)
    val strTypeSection   = stringResource(Res.string.add_appointment_section_type)
    val strDateSection   = stringResource(Res.string.add_appointment_section_date)
    val strTimeSection   = stringResource(Res.string.add_appointment_section_time)
    val strDoctorSection = stringResource(Res.string.add_appointment_section_doctor)
    val strLocSection    = stringResource(Res.string.add_appointment_section_location)
    val strNotesSection  = stringResource(Res.string.add_appointment_section_notes)
    val strDateError     = stringResource(Res.string.add_appointment_date_error)
    val strTimePh        = stringResource(Res.string.add_appointment_time_placeholder)
    val strDoctorPh      = stringResource(Res.string.add_appointment_doctor_placeholder)
    val strLocPh         = stringResource(Res.string.add_appointment_location_placeholder)
    val strNotesPh       = stringResource(Res.string.add_appointment_notes_placeholder)
    val strSaveButton    = stringResource(Res.string.add_appointment_save_button)
    val strSaving        = stringResource(Res.string.add_appointment_saving)
    val strCheckup       = stringResource(Res.string.add_appointment_type_checkup)
    val strVaccination   = stringResource(Res.string.add_appointment_type_vaccination)
    val strConsultation  = stringResource(Res.string.add_appointment_type_consultation)
    val strFollowUp      = stringResource(Res.string.add_appointment_type_followup)
    val strEmergency     = stringResource(Res.string.add_appointment_type_emergency)
    val strDateOk        = stringResource(Res.string.add_baby_date_ok)
    val strDateCancel    = stringResource(Res.string.add_baby_date_cancel)
    val strPickDate      = stringResource(Res.string.add_baby_field_dob_pick)
    val strBack          = stringResource(Res.string.common_back)
    val strCancel        = stringResource(Res.string.btn_cancel)

    val appointmentTypes = listOf(
        Triple("REGULAR_CHECKUP", "🩺", strCheckup),
        Triple("VACCINATION",     "💉", strVaccination),
        Triple("CONSULTATION",    "👨‍⚕️", strConsultation),
        Triple("FOLLOW_UP",       "🔁", strFollowUp),
        Triple("EMERGENCY",       "🚨", strEmergency)
    )

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
                ) { Text(strDateOk, fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDatePicker = false }, shape = RoundedCornerShape(dimensions.buttonCornerRadius)) {
                    Text(strDateCancel)
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
                            strAddTitle,
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
                            contentDescription = strBack,
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
                        HISectionCard(title = strTypeSection) {
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
                                                        RoundedCornerShape(dimensions.buttonCornerRadius - dimensions.spacingXSmall)
                                                    )
                                                    .border(
                                                        if (selected) dimensions.borderWidthMedium else dimensions.borderWidthThin,
                                                        if (selected) customColors.accentGradientStart.copy(0.7f)
                                                        else MaterialTheme.colorScheme.onPrimary.copy(0.2f),
                                                        RoundedCornerShape(dimensions.buttonCornerRadius - dimensions.spacingXSmall)
                                                    )
                                                    .clickable { appointmentType = value }
                                                    .padding(vertical = dimensions.spacingSmall + dimensions.borderWidthMedium),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(dimensions.borderWidthMedium)) {
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
                        HISectionCard(title = strDateSection) {
                            HITextField(
                                value = scheduledDate,
                                onValueChange = {},
                                placeholder = stringResource(Res.string.add_measure_date_placeholder),
                                readOnly = true,
                                isError = dateError,
                                trailingIcon = {
                                    IconButton(onClick = { showDatePicker = true }) {
                                        Icon(
                                            Icons.Default.DateRange,
                                            contentDescription = strPickDate,
                                            tint = customColors.accentGradientEnd,
                                            modifier = Modifier.size(dimensions.iconMedium)
                                        )
                                    }
                                }
                            )
                            AnimatedVisibility(visible = dateError) {
                                Text(strDateError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(start = dimensions.spacingXSmall, top = dimensions.borderWidthMedium))
                            }
                        }

                        // ── Time ──────────────────────────────────────────────
                        HISectionCard(title = strTimeSection) {
                            HITextField(
                                value = scheduledTime,
                                onValueChange = { scheduledTime = it },
                                placeholder = strTimePh,
                                keyboardType = KeyboardType.Number,
                                leadingEmoji = "🕐"
                            )
                        }

                        // ── Doctor Name ───────────────────────────────────────
                        HISectionCard(title = strDoctorSection) {
                            HITextField(
                                value = doctorName,
                                onValueChange = { doctorName = it },
                                placeholder = strDoctorPh,
                                leadingEmoji = "👨‍⚕️"
                            )
                        }

                        // ── Location ──────────────────────────────────────────
                        HISectionCard(title = strLocSection) {
                            HITextField(
                                value = location,
                                onValueChange = { location = it },
                                placeholder = strLocPh,
                                leadingEmoji = "🏥"
                            )
                        }

                        // ── Notes ─────────────────────────────────────────────
                        HISectionCard(title = strNotesSection) {
                            HITextField(
                                value = notes,
                                onValueChange = { notes = it },
                                placeholder = strNotesPh,
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
                                    .background(MaterialTheme.colorScheme.error.copy(0.15f), RoundedCornerShape(dimensions.buttonCornerRadius - dimensions.spacingXSmall))
                                    .border(dimensions.borderWidthThin, MaterialTheme.colorScheme.error.copy(0.4f), RoundedCornerShape(dimensions.buttonCornerRadius - dimensions.spacingXSmall))
                                    .padding(dimensions.spacingMedium)
                            ) {
                                Text(msg, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                            }
                        }

                        Spacer(Modifier.height(dimensions.spacingSmall))

                        HIActionButton(
                            label = if (isLoading) strSaving else strSaveButton,
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
                            label = strCancel,
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
// Shared internal composables (used by both screens) — unchanged structure,
// all hardcoded dims now use dimension tokens
// =============================================================================

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
                RoundedCornerShape(dimensions.buttonCornerRadius - dimensions.spacingXSmall)
            )
            .border(
                if (isError) dimensions.borderWidthMedium / 2 else dimensions.spacingXSmall / 4,
                if (isError) MaterialTheme.colorScheme.error.copy(0.6f)
                else customColors.accentGradientStart.copy(alpha = 0.35f),
                RoundedCornerShape(dimensions.buttonCornerRadius - dimensions.spacingXSmall)
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
                        Modifier.border(dimensions.borderWidthThin, borderColor, RoundedCornerShape(dimensions.buttonCornerRadius))
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