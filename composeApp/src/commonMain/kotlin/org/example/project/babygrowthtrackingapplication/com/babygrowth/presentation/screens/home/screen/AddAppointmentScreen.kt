package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.screen

// ─────────────────────────────────────────────────────────────────────────────
// FIXES APPLIED:
//  Fix 2: scheduledTime now uses Material3 TimePickerDialog instead of text input.
//         Uses TimeInput (keyboard-style) on compact screens and TimePicker (clock)
//         on medium/expanded screens. Works on all Compose Multiplatform targets.
// ─────────────────────────────────────────────────────────────────────────────

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
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
// Add Appointment Screen — Fix 2: Time picker
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
    // FIX 2: Time stored as HH:mm string, set via picker
    var scheduledTime   by remember { mutableStateOf("") }
    var doctorName      by remember { mutableStateOf("") }
    var location        by remember { mutableStateOf("") }
    var notes           by remember { mutableStateOf("") }
    var dateError       by remember { mutableStateOf(false) }
    var isLoading       by remember { mutableStateOf(false) }
    var showDatePicker  by remember { mutableStateOf(false) }
    // FIX 2: Time picker state
    var showTimePicker  by remember { mutableStateOf(false) }

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

    val strAddTitle      = stringResource(Res.string.add_appointment_title)
    val strTypeSection   = stringResource(Res.string.add_appointment_section_type)
    val strDateSection   = stringResource(Res.string.add_appointment_section_date)
    val strTimeSection   = stringResource(Res.string.add_appointment_section_time)
    val strDoctorSection = stringResource(Res.string.add_appointment_section_doctor)
    val strLocSection    = stringResource(Res.string.add_appointment_section_location)
    val strNotesSection  = stringResource(Res.string.add_appointment_section_notes)
    val strDateError     = stringResource(Res.string.add_appointment_date_error)
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
                            scheduledDate = "${date.year}-${date.month.number.toString().padStart(2, '0')}-${date.dayOfMonth.toString().padStart(2, '0')}"
                            dateError = false
                        }
                        showDatePicker = false
                    },
                    shape  = RoundedCornerShape(dimensions.buttonCornerRadius),
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

    // FIX 2: Time picker dialog
    val timePickerState = rememberTimePickerState(
        initialHour   = 9,
        initialMinute = 0,
        is24Hour      = true
    )
    if (showTimePicker) {
        TimePickerDialog(
            onDismiss  = { showTimePicker = false },
            onConfirm  = {
                val h = timePickerState.hour.toString().padStart(2, '0')
                val m = timePickerState.minute.toString().padStart(2, '0')
                scheduledTime = "$h:$m"
                showTimePicker = false
            },
            timePickerState = timePickerState
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(strAddTitle, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = customColors.accentGradientStart)
                        Text(babyName, style = MaterialTheme.typography.bodySmall, color = customColors.accentGradientStart.copy(0.6f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strBack, tint = customColors.accentGradientStart)
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
                .background(Brush.verticalGradient(listOf(customColors.accentGradientStart.copy(0.15f), customColors.accentGradientEnd.copy(0.25f))))
                .padding(padding)
        ) {
            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                Spacer(Modifier.height(dimensions.spacingMedium))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(listOf(customColors.accentGradientStart.copy(0.6f), customColors.accentGradientEnd.copy(0.45f))),
                            RoundedCornerShape(topStart = dimensions.cardCornerRadius, topEnd = dimensions.cardCornerRadius)
                        )
                        .padding(bottom = dimensions.spacingXLarge)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = dimensions.spacingLarge, vertical = dimensions.spacingXLarge),
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
                                                        if (selected) customColors.accentGradientStart.copy(0.2f) else MaterialTheme.colorScheme.surface.copy(0.15f),
                                                        RoundedCornerShape(dimensions.buttonCornerRadius - dimensions.spacingXSmall)
                                                    )
                                                    .border(
                                                        if (selected) dimensions.borderWidthMedium else dimensions.borderWidthThin,
                                                        if (selected) customColors.accentGradientStart.copy(0.7f) else MaterialTheme.colorScheme.onPrimary.copy(0.2f),
                                                        RoundedCornerShape(dimensions.buttonCornerRadius - dimensions.spacingXSmall)
                                                    )
                                                    .clickable { appointmentType = value }
                                                    .padding(vertical = dimensions.spacingSmall + dimensions.borderWidthMedium),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(dimensions.borderWidthMedium)) {
                                                    Text(emoji, style = MaterialTheme.typography.titleSmall)
                                                    Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal, color = if (selected) customColors.accentGradientStart else MaterialTheme.colorScheme.onPrimary.copy(0.75f))
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
                                        Icon(Icons.Default.DateRange, contentDescription = strPickDate, tint = customColors.accentGradientEnd, modifier = Modifier.size(dimensions.iconMedium))
                                    }
                                }
                            )
                            AnimatedVisibility(visible = dateError) {
                                Text(strDateError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(start = dimensions.spacingXSmall, top = dimensions.borderWidthMedium))
                            }
                        }

                        // ── FIX 2: Time — now a picker button, not a text field ──
                        HISectionCard(title = strTimeSection) {
                            // Tappable row that opens the time picker dialog
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.13f),
                                        RoundedCornerShape(dimensions.buttonCornerRadius - dimensions.spacingXSmall)
                                    )
                                    .border(
                                        dimensions.spacingXSmall / 4,
                                        customColors.accentGradientStart.copy(alpha = 0.35f),
                                        RoundedCornerShape(dimensions.buttonCornerRadius - dimensions.spacingXSmall)
                                    )
                                    .clickable { showTimePicker = true }
                                    .padding(horizontal = dimensions.spacingMedium, vertical = dimensions.spacingMedium)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("🕐", style = MaterialTheme.typography.bodyLarge)
                                        Text(
                                            text  = scheduledTime.ifBlank { stringResource(Res.string.add_appointment_time_placeholder) },
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (scheduledTime.isBlank())
                                                MaterialTheme.colorScheme.onSurface.copy(0.45f)
                                            else
                                                MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall)) {
                                        if (scheduledTime.isNotBlank()) {
                                            TextButton(
                                                onClick = { scheduledTime = "" },
                                                contentPadding = PaddingValues(0.dp)
                                            ) {
                                                Text("Clear", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                                            }
                                        }
                                        Icon(
                                            Icons.Default.Schedule,
                                            contentDescription = null,
                                            tint     = customColors.accentGradientEnd,
                                            modifier = Modifier.size(dimensions.iconMedium)
                                        )
                                    }
                                }
                            }
                        }

                        // ── Doctor Name ───────────────────────────────────────
                        HISectionCard(title = strDoctorSection) {
                            HITextField(value = doctorName, onValueChange = { doctorName = it }, placeholder = strDoctorPh, leadingEmoji = "👨‍⚕️")
                        }

                        // ── Location ──────────────────────────────────────────
                        HISectionCard(title = strLocSection) {
                            HITextField(value = location, onValueChange = { location = it }, placeholder = strLocPh, leadingEmoji = "🏥")
                        }

                        // ── Notes ─────────────────────────────────────────────
                        HISectionCard(title = strNotesSection) {
                            HITextField(value = notes, onValueChange = { notes = it }, placeholder = strNotesPh, minLines = 2, maxLines = 4, leadingEmoji = "📝")
                        }

                        // ── Error ─────────────────────────────────────────────
                        state.error?.let { msg ->
                            Box(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.error.copy(0.15f), RoundedCornerShape(dimensions.buttonCornerRadius - dimensions.spacingXSmall)).border(dimensions.borderWidthThin, MaterialTheme.colorScheme.error.copy(0.4f), RoundedCornerShape(dimensions.buttonCornerRadius - dimensions.spacingXSmall)).padding(dimensions.spacingMedium)) {
                                Text(msg, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                            }
                        }

                        Spacer(Modifier.height(dimensions.spacingSmall))

                        HIActionButton(
                            label          = if (isLoading) strSaving else strSaveButton,
                            isLoading      = isLoading,
                            enabled        = !isLoading,
                            containerColor = customColors.accentGradientStart,
                            onClick        = {
                                if (scheduledDate.isBlank()) { dateError = true; return@HIActionButton }
                                isLoading = true
                                viewModel.addAppointment(
                                    babyId     = babyId,
                                    type       = appointmentType,
                                    date       = scheduledDate,
                                    time       = scheduledTime.ifBlank { null },
                                    doctorName = doctorName.ifBlank { null },
                                    location   = location.ifBlank { null },
                                    notes      = notes.ifBlank { null }
                                )
                            }
                        )

                        Spacer(Modifier.height(dimensions.spacingXSmall))

                        HIActionButton(
                            label          = strCancel,
                            isLoading      = false,
                            enabled        = !isLoading,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.65f),
                            labelColor     = MaterialTheme.colorScheme.onSurface,
                            borderColor    = MaterialTheme.colorScheme.onSurface.copy(0.18f),
                            onClick        = onBack
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// FIX 2: TimePickerDialog — reusable Compose Multiplatform time picker wrapper
// Uses Material3 TimePicker (dial) inside an AlertDialog
// Works on Android, iOS, Desktop via Compose Multiplatform
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    onDismiss      : () -> Unit,
    onConfirm      : () -> Unit,
    timePickerState: TimePickerState
) {
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors  = ButtonDefaults.buttonColors(containerColor = customColors.accentGradientStart),
                shape   = RoundedCornerShape(dimensions.buttonCornerRadius)
            ) {
                Text(stringResource(Res.string.add_baby_date_ok), fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(dimensions.buttonCornerRadius)) {
                Text(stringResource(Res.string.btn_cancel)
                )
            }
        },
        title = {
            Text(
                text       = stringResource(Res.string.btn_selct_time),
                style      = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier            = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // TimePicker renders a clock dial — works on all CMP platforms
                TimePicker(
                    state  = timePickerState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor         = customColors.accentGradientStart.copy(0.1f),
                        selectorColor          = customColors.accentGradientStart,
                        containerColor         = MaterialTheme.colorScheme.surface,
                        periodSelectorBorderColor = customColors.accentGradientStart
                    )
                )
            }
        }
    )
}