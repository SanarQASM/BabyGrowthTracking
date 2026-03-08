package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.screen

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
import androidx.compose.ui.unit.dp
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model.*
import org.example.project.babygrowthtrackingapplication.theme.LocalDimensions
import org.example.project.babygrowthtrackingapplication.theme.customColors

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
            HealthIssueFilter.ALL      -> issues
            HealthIssueFilter.ONGOING  -> issues.filter { !it.isResolved }
            HealthIssueFilter.RESOLVED -> issues.filter { it.isResolved }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
    ) {
        // Filter tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimensions.screenPadding),
            horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
        ) {
            HealthIssueFilter.entries.forEach { f ->
                FilterChip(
                    selected = filter == f,
                    onClick = { onFilterChange(f) },
                    label = {
                        Text(
                            text = when (f) {
                                HealthIssueFilter.ALL      -> "All"
                                HealthIssueFilter.ONGOING  -> "Ongoing"
                                HealthIssueFilter.RESOLVED -> "Resolved"
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
            Box(Modifier.fillMaxWidth().height(120.dp), Alignment.Center) {
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
                        onResolve = { onResolve(issue.issueId) }
                    )
                }

                item {
                    // Add New Issue button
                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAddIssue() },
                        shape = RoundedCornerShape(dimensions.cardCornerRadius),
                        border = BorderStroke(1.dp, customColors.accentGradientStart.copy(0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(dimensions.spacingMedium),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Add, null,
                                tint = customColors.accentGradientStart,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Add New Health Issue",
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
private fun HealthIssueCard(
    issue: HealthIssueUi,
    onClick: () -> Unit,
    onResolve: () -> Unit
) {
    val dimensions = LocalDimensions.current
    val customColors = MaterialTheme.customColors

    val severityColor = when (issue.severityUi) {
        SeverityUi.MILD     -> Color(0xFF22C55E)
        SeverityUi.MODERATE -> Color(0xFFF59E0B)
        SeverityUi.SEVERE   -> MaterialTheme.colorScheme.error
        null                -> MaterialTheme.colorScheme.onSurface.copy(0.4f)
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(dimensions.cardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(dimensions.spacingMedium)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
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
                // View all icon
                Icon(
                    Icons.Default.Edit, null,
                    tint = customColors.accentGradientStart.copy(0.5f),
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                issue.severity?.let {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = severityColor.copy(0.12f)
                    ) {
                        Text(
                            text = "Severity: ${it.lowercase().replaceFirstChar { c -> c.uppercase() }}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = severityColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                if (!issue.isResolved) {
                    TextButton(
                        onClick = onResolve,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color(0xFF22C55E)
                        )
                    ) {
                        Text("Resolved", fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.labelMedium)
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color(0xFF22C55E).copy(0.12f)
                    ) {
                        Text(
                            "Resolved",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF22C55E),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Add Health Issue Dialog
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Health Issue", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(dimensions.cardCornerRadius)
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(dimensions.cardCornerRadius)
                )
                OutlinedTextField(
                    value = issueDate,
                    onValueChange = { issueDate = it },
                    label = { Text("Issue Date (YYYY-MM-DD) *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(dimensions.cardCornerRadius)
                )
                Text("Severity", style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)) {
                    listOf("MILD", "MODERATE", "SEVERE").forEach { s ->
                        FilterChip(
                            selected = severity == s,
                            onClick = { severity = s },
                            label = { Text(s.lowercase().replaceFirstChar { it.uppercase() }) },
                            shape = RoundedCornerShape(50)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && issueDate.isNotBlank()) {
                        onConfirm(title, description.ifBlank { null }, severity, issueDate)
                    }
                },
                enabled = title.isNotBlank() && issueDate.isNotBlank()
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
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
            AppointmentFilter.ALL       -> appointments
            AppointmentFilter.UPCOMING  -> appointments.filter {
                it.statusUi == AppointmentStatusUi.SCHEDULED
            }
            AppointmentFilter.PAST      -> appointments.filter {
                it.statusUi == AppointmentStatusUi.COMPLETED
            }
            AppointmentFilter.CANCELLED -> appointments.filter {
                it.statusUi == AppointmentStatusUi.CANCELLED ||
                        it.statusUi == AppointmentStatusUi.MISSED
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
    ) {
        // Filter tabs
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
                                AppointmentFilter.ALL       -> "All"
                                AppointmentFilter.UPCOMING  -> "Upcoming"
                                AppointmentFilter.PAST      -> "Past"
                                AppointmentFilter.CANCELLED -> "Cancelled"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (filter == f) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        if (loading) {
            Box(Modifier.fillMaxWidth().height(120.dp), Alignment.Center) {
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
                        onCancel = { onCancel(appointment.appointmentId) }
                    )
                }

                item {
                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAddAppointment() },
                        shape = RoundedCornerShape(dimensions.cardCornerRadius),
                        border = BorderStroke(1.dp, customColors.accentGradientStart.copy(0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(dimensions.spacingMedium),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Add, null,
                                tint = customColors.accentGradientStart,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Add New Appointments",
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
private fun AppointmentCard(
    appointment: AppointmentUi,
    onClick: () -> Unit,
    onCancel: () -> Unit
) {
    val dimensions = LocalDimensions.current
    val customColors = MaterialTheme.customColors

    val (statusColor, statusLabel) = when (appointment.statusUi) {
        AppointmentStatusUi.COMPLETED  -> Pair(Color(0xFF22C55E), "Completed")
        AppointmentStatusUi.CANCELLED  -> Pair(MaterialTheme.colorScheme.error, "Cancelled")
        AppointmentStatusUi.MISSED     -> Pair(Color(0xFFF59E0B), "Missed")
        AppointmentStatusUi.SCHEDULED  -> Pair(customColors.accentGradientStart, "Upcoming")
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(dimensions.cardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(dimensions.spacingMedium)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(statusColor.copy(0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("📅", style = MaterialTheme.typography.titleSmall)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = appointment.appointmentType.replace("_", " ")
                                .lowercase().replaceFirstChar { it.uppercase() },
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
                    Icons.Default.Edit, null,
                    tint = customColors.accentGradientStart.copy(0.5f),
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Status: $statusLabel",
                    style = MaterialTheme.typography.labelSmall,
                    color = statusColor,
                    fontWeight = FontWeight.SemiBold
                )
                if (appointment.statusUi == AppointmentStatusUi.SCHEDULED) {
                    TextButton(
                        onClick = onCancel,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Canceled", style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Add Appointment Dialog
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

    val appointmentTypes = listOf(
        "REGULAR_CHECKUP", "VACCINATION", "SPECIALIST", "EMERGENCY", "FOLLOW_UP"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Appointment", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
            ) {
                // Type selection
                Text("Type", style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold)
                appointmentTypes.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        row.forEach { t ->
                            FilterChip(
                                selected = type == t,
                                onClick = { type = t },
                                label = {
                                    Text(
                                        t.replace("_", " ").lowercase()
                                            .replaceFirstChar { it.uppercase() },
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
                    value = date, onValueChange = { date = it },
                    label = { Text("Date (YYYY-MM-DD) *") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    shape = RoundedCornerShape(dimensions.cardCornerRadius)
                )
                OutlinedTextField(
                    value = time, onValueChange = { time = it },
                    label = { Text("Time (HH:MM)") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    shape = RoundedCornerShape(dimensions.cardCornerRadius)
                )
                OutlinedTextField(
                    value = doctorName, onValueChange = { doctorName = it },
                    label = { Text("Doctor Name") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    shape = RoundedCornerShape(dimensions.cardCornerRadius)
                )
                OutlinedTextField(
                    value = location, onValueChange = { location = it },
                    label = { Text("Location") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    shape = RoundedCornerShape(dimensions.cardCornerRadius)
                )
                OutlinedTextField(
                    value = notes, onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth(), minLines = 2, maxLines = 3,
                    shape = RoundedCornerShape(dimensions.cardCornerRadius)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (date.isNotBlank()) {
                        onConfirm(
                            type, date,
                            time.ifBlank { null },
                            doctorName.ifBlank { null },
                            location.ifBlank { null },
                            notes.ifBlank { null }
                        )
                    }
                },
                enabled = date.isNotBlank()
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}