package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.screen

// ─────────────────────────────────────────────────────────────────────────────
// CHANGES vs original:
//  • All hardcoded "All", "Upcoming", "Past", "Cancelled" inline strings
//    → already use stringResource in original — no change needed there
//  • "Add New Appointments" inline → stringResource(Res.string.add_appointment_button)
//    [already uses stringResource in original]
//  • "Status:" inline prefix → stringResource(Res.string.appointment_status_prefix)
//    [already uses stringResource in original]
//  • 36.dp box size → dimensions.iconLarge + dimensions.spacingXSmall  (already token)
//  • 8.dp padding → dimensions.spacingSmall (already token)
//  • Color constants (Color(0xFF22C55E), Color(0xFFF59E0B)) — these are
//    fixed semantic status colors, intentionally independent of gender theme.
//    They represent "healthy/green" and "warning/amber" which must not shift
//    with pink/blue palettes. Kept as-is with named comments.
// ─────────────────────────────────────────────────────────────────────────────

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model.*
import org.example.project.babygrowthtrackingapplication.theme.LocalDimensions
import org.example.project.babygrowthtrackingapplication.theme.customColors
import org.jetbrains.compose.resources.stringResource
import babygrowthtrackingapplication.composeapp.generated.resources.Res
import babygrowthtrackingapplication.composeapp.generated.resources.*

// Semantic status colors — intentionally fixed, not gender-themed
private val ColorHealthy = Color(0xFF22C55E)
private val ColorWarning = Color(0xFFF59E0B)
private val ColorCritical = Color(0xFFEF4444)
private val ColorTeam = Color(0xFF00ACC1)

// ═════════════════════════════════════════════════════════════════════════════
// HEALTH ISSUES VIEW
// ═════════════════════════════════════════════════════════════════════════════

@Composable
fun HealthIssuesView(
    issues: List<HealthIssueUi>,
    filter: HealthIssueFilter,
    loading: Boolean,
    onFilterChange: (HealthIssueFilter) -> Unit,
    onIssueClick: (HealthIssueUi) -> Unit,
    onResolve: (String) -> Unit,
    onAddIssue: () -> Unit
) {
    val dimensions = LocalDimensions.current
    val customColors = MaterialTheme.customColors

    val filtered = remember(issues, filter) {
        when (filter) {
            HealthIssueFilter.ALL -> issues
            HealthIssueFilter.ONGOING -> issues.filter { !it.isResolved }
            HealthIssueFilter.RESOLVED -> issues.filter { it.isResolved }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = dimensions.screenPadding),
            horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
        ) {
            HealthIssueFilter.entries.forEach { f ->
                FilterChip(
                    selected = filter == f,
                    onClick = { onFilterChange(f) },
                    label = {
                        Text(
                            text = when (f) {
                                HealthIssueFilter.ALL -> stringResource(Res.string.health_issue_filter_all_label)
                                HealthIssueFilter.ONGOING -> stringResource(Res.string.health_issue_filter_ongoing_label)
                                HealthIssueFilter.RESOLVED -> stringResource(Res.string.health_issue_filter_resolved_label)
                            },
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    shape = RoundedCornerShape(50),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = customColors.accentGradientStart,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        if (loading) {
            Box(
                Modifier.fillMaxWidth().height(dimensions.iconXLarge + dimensions.spacingXLarge),
                Alignment.Center
            ) {
                CircularProgressIndicator(color = customColors.accentGradientStart)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall),
                contentPadding = PaddingValues(
                    horizontal = dimensions.screenPadding,
                    vertical = dimensions.spacingSmall
                )
            ) {
                items(filtered, key = { it.issueId }) { issue ->
                    HealthIssueCard(
                        issue = issue,
                        onClick = { onIssueClick(issue) },
                        onResolve = { onResolve(issue.issueId) })
                }
                item {
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth().clickable { onAddIssue() },
                        shape = RoundedCornerShape(dimensions.cardCornerRadius),
                        border = BorderStroke(
                            dimensions.borderWidthThin,
                            customColors.accentGradientStart.copy(0.4f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(dimensions.spacingMedium),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Add,
                                null,
                                tint = customColors.accentGradientStart,
                                modifier = Modifier.size(dimensions.iconMedium)
                            )
                            Spacer(Modifier.width(dimensions.spacingSmall))
                            Text(
                                stringResource(Res.string.add_health_issue_button),
                                style = MaterialTheme.typography.labelLarge,
                                color = customColors.accentGradientStart,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HealthIssueCard(issue: HealthIssueUi, onClick: () -> Unit, onResolve: () -> Unit) {
    val dimensions = LocalDimensions.current
    val customColors = MaterialTheme.customColors

    val severityColor = when (issue.severityUi) {
        SeverityUi.MILD -> ColorHealthy
        SeverityUi.MODERATE -> ColorWarning
        SeverityUi.SEVERE -> MaterialTheme.colorScheme.error
        null -> MaterialTheme.colorScheme.onSurface.copy(0.4f)
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(dimensions.cardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(dimensions.healthSubTabElevation)
    ) {
        Column(modifier = Modifier.padding(dimensions.spacingMedium)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (issue.isResolved) "😊" else "🤒",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Column {
                        Text(
                            text = issue.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${issue.issueDate}${issue.resolutionDate?.let { " - $it" } ?: ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(0.5f)
                        )
                    }
                }
                Icon(
                    Icons.Default.Edit,
                    null,
                    tint = customColors.accentGradientStart.copy(0.5f),
                    modifier = Modifier.size(dimensions.iconSmall + dimensions.borderWidthMedium)
                )
            }

            Spacer(Modifier.height(dimensions.spacingSmall))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                issue.severity?.let {
                    Surface(shape = RoundedCornerShape(50), color = severityColor.copy(0.12f)) {
                        Text(
                            text = stringResource(
                                Res.string.health_issue_severity_label,
                                it.lowercase().replaceFirstChar { c -> c.uppercase() }),
                            modifier = Modifier.padding(
                                horizontal = dimensions.spacingSmall,
                                vertical = dimensions.borderWidthThin + dimensions.borderWidthMedium
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = severityColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                if (!issue.isResolved) {
                    TextButton(
                        onClick = onResolve,
                        colors = ButtonDefaults.textButtonColors(contentColor = ColorHealthy)
                    ) {
                        Text(
                            stringResource(Res.string.health_issue_resolved_button),
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                } else {
                    Surface(shape = RoundedCornerShape(50), color = ColorHealthy.copy(0.12f)) {
                        Text(
                            stringResource(Res.string.health_issue_resolved_button),
                            modifier = Modifier.padding(
                                horizontal = dimensions.spacingSmall,
                                vertical = dimensions.borderWidthThin + dimensions.borderWidthMedium
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = ColorHealthy,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Add Health Issue Dialog (lightweight version used inline)
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHealthIssueDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, description: String?, severity: String?, issueDate: String) -> Unit
) {
    val dimensions = LocalDimensions.current
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var severity by remember { mutableStateOf("MILD") }
    var issueDate by remember { mutableStateOf("") }

    val strMild = stringResource(Res.string.add_issue_severity_mild)
    val strModerate = stringResource(Res.string.add_issue_severity_moderate)
    val strSevere = stringResource(Res.string.add_issue_severity_severe)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.add_issue_title), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)) {
                OutlinedTextField(
                    value = title, onValueChange = { title = it },
                    label = { Text(stringResource(Res.string.add_issue_section_title_label)) },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    shape = RoundedCornerShape(dimensions.cardCornerRadius)
                )
                OutlinedTextField(
                    value = description, onValueChange = { description = it },
                    label = { Text(stringResource(Res.string.add_issue_section_description)) },
                    modifier = Modifier.fillMaxWidth(), minLines = 2, maxLines = 4,
                    shape = RoundedCornerShape(dimensions.cardCornerRadius)
                )
                OutlinedTextField(
                    value = issueDate, onValueChange = { issueDate = it },
                    label = { Text(stringResource(Res.string.add_issue_section_date)) },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    shape = RoundedCornerShape(dimensions.cardCornerRadius)
                )
                Text(
                    stringResource(Res.string.add_issue_section_severity),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)) {
                    listOf(
                        "MILD" to strMild,
                        "MODERATE" to strModerate,
                        "SEVERE" to strSevere
                    ).forEach { (value, label) ->
                        FilterChip(
                            selected = severity == value,
                            onClick = { severity = value },
                            label = { Text(label) },
                            shape = RoundedCornerShape(50)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (title.isNotBlank() && issueDate.isNotBlank()) {
                    onConfirm(title, description.ifBlank { null }, severity, issueDate)
                }
            }, enabled = title.isNotBlank() && issueDate.isNotBlank()) {
                Text(stringResource(Res.string.add_issue_save_button))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.btn_cancel)) } }
    )
}

// ═════════════════════════════════════════════════════════════════════════════
// APPOINTMENTS VIEW
// ═════════════════════════════════════════════════════════════════════════════

@Composable
fun AppointmentsView(
    appointments: List<AppointmentUi>,
    filter: AppointmentFilter,
    loading: Boolean,
    onFilterChange: (AppointmentFilter) -> Unit,
    onAppointmentClick: (AppointmentUi) -> Unit,
    onCancel: (String) -> Unit,
    onAddAppointment: () -> Unit
) {
    val dimensions = LocalDimensions.current
    val customColors = MaterialTheme.customColors

    val filtered = remember(appointments, filter) {
        when (filter) {
            AppointmentFilter.ALL -> appointments
            AppointmentFilter.UPCOMING -> appointments.filter { it.statusUi == AppointmentStatusUi.SCHEDULED }
            AppointmentFilter.PAST -> appointments.filter { it.statusUi == AppointmentStatusUi.COMPLETED }
            AppointmentFilter.CANCELLED -> appointments.filter { it.statusUi == AppointmentStatusUi.CANCELLED || it.statusUi == AppointmentStatusUi.MISSED }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
    ) {
        ScrollableTabRow(
            selectedTabIndex = filter.ordinal,
            edgePadding = dimensions.screenPadding,
            containerColor = Color.Transparent,
            contentColor = customColors.accentGradientStart,
            divider = {}
        ) {
            AppointmentFilter.entries.forEach { f ->
                Tab(
                    selected = filter == f,
                    onClick = { onFilterChange(f) },
                    text = {
                        Text(
                            text = when (f) {
                                AppointmentFilter.ALL -> stringResource(Res.string.appointment_filter_all_label)
                                AppointmentFilter.UPCOMING -> stringResource(Res.string.appointment_filter_upcoming_label)
                                AppointmentFilter.PAST -> stringResource(Res.string.appointment_filter_past_label)
                                AppointmentFilter.CANCELLED -> stringResource(Res.string.appointment_filter_cancelled_label)
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (filter == f) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        if (loading) {
            Box(
                Modifier.fillMaxWidth().height(dimensions.iconXLarge + dimensions.spacingXLarge),
                Alignment.Center
            ) {
                CircularProgressIndicator(color = customColors.accentGradientStart)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall),
                contentPadding = PaddingValues(
                    horizontal = dimensions.screenPadding,
                    vertical = dimensions.spacingSmall
                )
            ) {
                items(filtered, key = { it.appointmentId }) { appointment ->
                    AppointmentCard(
                        appointment = appointment,
                        onClick = { onAppointmentClick(appointment) },
                        onCancel = { onCancel(appointment.appointmentId) })
                }
                item {
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth().clickable { onAddAppointment() },
                        shape = RoundedCornerShape(dimensions.cardCornerRadius),
                        border = BorderStroke(
                            dimensions.borderWidthThin,
                            customColors.accentGradientStart.copy(0.4f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(dimensions.spacingMedium),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Add,
                                null,
                                tint = customColors.accentGradientStart,
                                modifier = Modifier.size(dimensions.iconMedium)
                            )
                            Spacer(Modifier.width(dimensions.spacingSmall))
                            Text(
                                stringResource(Res.string.add_appointment_button),
                                style = MaterialTheme.typography.labelLarge,
                                color = customColors.accentGradientStart,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppointmentCard(appointment: AppointmentUi, onClick: () -> Unit, onCancel: () -> Unit) {
    val dimensions = LocalDimensions.current
    val customColors = MaterialTheme.customColors

    val (statusColor, statusLabel) = when (appointment.statusUi) {
        AppointmentStatusUi.COMPLETED -> Pair(
            ColorHealthy,
            stringResource(Res.string.appointment_status_completed_label)
        )

        AppointmentStatusUi.CANCELLED -> Pair(
            MaterialTheme.colorScheme.error,
            stringResource(Res.string.appointment_status_cancelled_label)
        )

        AppointmentStatusUi.MISSED -> Pair(
            ColorWarning,
            stringResource(Res.string.appointment_status_missed_label)
        )

        AppointmentStatusUi.SCHEDULED -> Pair(
            customColors.accentGradientStart,
            stringResource(Res.string.appointment_status_upcoming_label)
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(dimensions.cardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(dimensions.healthSubTabElevation)
    ) {
        Column(modifier = Modifier.padding(dimensions.spacingMedium)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier.size(dimensions.iconLarge + dimensions.spacingXSmall)
                            .clip(RoundedCornerShape(dimensions.spacingSmall))
                            .background(statusColor.copy(0.12f)),
                        contentAlignment = Alignment.Center
                    ) { Text("📅", style = MaterialTheme.typography.titleSmall) }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = appointment.appointmentType.replace("_", " ").lowercase()
                                .replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${appointment.scheduledDate}${appointment.scheduledTime?.let { " · $it" } ?: ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                        )
                        appointment.doctorName?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(0.5f)
                            )
                        }
                    }
                }
                Icon(
                    Icons.Default.Edit,
                    null,
                    tint = customColors.accentGradientStart.copy(0.5f),
                    modifier = Modifier.size(dimensions.iconSmall + dimensions.borderWidthMedium)
                )
            }

            Spacer(Modifier.height(dimensions.spacingSmall))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.appointment_status_prefix, statusLabel),
                    style = MaterialTheme.typography.labelSmall,
                    color = statusColor,
                    fontWeight = FontWeight.SemiBold
                )
                if (appointment.statusUi == AppointmentStatusUi.SCHEDULED) {
                    TextButton(
                        onClick = onCancel,
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text(
                            stringResource(Res.string.appointment_cancel_button),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Add Appointment Dialog (lightweight version)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun AddAppointmentDialog(
    onDismiss: () -> Unit,
    onConfirm: (type: String, date: String, time: String?, doctorName: String?, location: String?, notes: String?) -> Unit
) {
    val dimensions = LocalDimensions.current
    var type by remember { mutableStateOf("REGULAR_CHECKUP") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var doctorName by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val appointmentTypeLabels = mapOf(
        "REGULAR_CHECKUP" to stringResource(Res.string.add_appointment_type_checkup),
        "VACCINATION" to stringResource(Res.string.add_appointment_type_vaccination),
        "SPECIALIST" to stringResource(Res.string.add_appointment_type_consultation),
        "EMERGENCY" to stringResource(Res.string.add_appointment_type_emergency),
        "FOLLOW_UP" to stringResource(Res.string.add_appointment_type_followup)
    )
    val appointmentTypes = appointmentTypeLabels.keys.toList()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(Res.string.add_appointment_title),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
            ) {
                Text(
                    stringResource(Res.string.add_appointment_section_type),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                appointmentTypes.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall)) {
                        row.forEach { t ->
                            FilterChip(
                                selected = type == t,
                                onClick = { type = t },
                                label = {
                                    Text(
                                        appointmentTypeLabels[t] ?: t,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                shape = RoundedCornerShape(50),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text(stringResource(Res.string.add_appointment_section_date)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(dimensions.cardCornerRadius)
                )
                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text(stringResource(Res.string.add_appointment_section_time)) },
                    placeholder = { Text(stringResource(Res.string.add_appointment_time_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(dimensions.cardCornerRadius)
                )
                OutlinedTextField(
                    value = doctorName,
                    onValueChange = { doctorName = it },
                    label = { Text(stringResource(Res.string.add_appointment_section_doctor)) },
                    placeholder = { Text(stringResource(Res.string.add_appointment_doctor_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(dimensions.cardCornerRadius)
                )
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text(stringResource(Res.string.add_appointment_section_location)) },
                    placeholder = { Text(stringResource(Res.string.add_appointment_location_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(dimensions.cardCornerRadius)
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(stringResource(Res.string.add_appointment_section_notes)) },
                    placeholder = { Text(stringResource(Res.string.add_appointment_notes_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3,
                    shape = RoundedCornerShape(dimensions.cardCornerRadius)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (date.isNotBlank()) {
                        onConfirm(
                            type,
                            date,
                            time.ifBlank { null },
                            doctorName.ifBlank { null },
                            location.ifBlank { null },
                            notes.ifBlank { null })
                    }
                },
                enabled = date.isNotBlank()
            ) { Text(stringResource(Res.string.add_appointment_save_button)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.btn_cancel)) } }
    )
}