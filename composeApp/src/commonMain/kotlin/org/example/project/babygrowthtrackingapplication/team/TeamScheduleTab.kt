// composeApp/src/commonMain/kotlin/org/example/project/babygrowthtrackingapplication/team/TeamScheduleTab.kt

package org.example.project.babygrowthtrackingapplication.team

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.dp
import org.example.project.babygrowthtrackingapplication.theme.*

// ─────────────────────────────────────────────────────────────────────────────
// Schedule Tab — Daily vaccination schedule view
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamScheduleTab(viewModel: TeamVaccinationViewModel) {
    val state        = viewModel.uiState
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors

    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { ms ->
                        val days  = (ms / 86_400_000L).toInt()
                        val d     = kotlinx.datetime.LocalDate.fromEpochDays(days)
                        val iso   = "${d.year}-${d.monthNumber.toString().padStart(2,'0')}-${d.dayOfMonth.toString().padStart(2,'0')}"
                        viewModel.onDateSelected(iso)
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // ── Date selector ─────────────────────────────────────────────────────
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = dimensions.cardElevationSmall
        ) {
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimensions.screenPadding, vertical = dimensions.spacingSmall),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
            ) {
                Icon(
                    Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint     = customColors.accentGradientStart,
                    modifier = Modifier.size(dimensions.iconMedium)
                )
                Text(
                    text       = state.selectedDate.ifBlank { "Select a date" },
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onSurface,
                    modifier   = Modifier.weight(1f)
                )
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    shape   = RoundedCornerShape(dimensions.buttonCornerRadius),
                    border  = BorderStroke(dimensions.borderWidthThin, customColors.accentGradientStart.copy(0.5f)),
                    colors  = ButtonDefaults.outlinedButtonColors(contentColor = customColors.accentGradientStart),
                    contentPadding = PaddingValues(
                        horizontal = dimensions.spacingSmall,
                        vertical   = dimensions.spacingXSmall
                    )
                ) {
                    Icon(
                        Icons.Default.EditCalendar, null,
                        modifier = Modifier.size(dimensions.iconSmall)
                    )
                    Spacer(Modifier.width(dimensions.spacingXSmall))
                    Text("Change", style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        // ── Summary card ──────────────────────────────────────────────────────
        val total      = state.scheduleItems.size
        val completed  = state.scheduleItems.count { it.status == "COMPLETED" }
        val remaining  = total - completed

        if (!state.scheduleLoading && total > 0) {
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimensions.screenPadding, vertical = dimensions.spacingSmall),
                horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
            ) {
                ScheduleSummaryChip(
                    label      = "Scheduled",
                    value      = total.toString(),
                    emoji      = "💉",
                    color      = customColors.accentGradientStart,
                    dimensions = dimensions,
                    modifier   = Modifier.weight(1f)
                )
                ScheduleSummaryChip(
                    label      = "Done",
                    value      = completed.toString(),
                    emoji      = "✅",
                    color      = customColors.success,
                    dimensions = dimensions,
                    modifier   = Modifier.weight(1f)
                )
                ScheduleSummaryChip(
                    label      = "Remaining",
                    value      = remaining.toString(),
                    emoji      = "⏳",
                    color      = customColors.warning,
                    dimensions = dimensions,
                    modifier   = Modifier.weight(1f)
                )
            }
        }

        // ── Schedule List ─────────────────────────────────────────────────────
        when {
            state.scheduleLoading -> {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator(color = customColors.accentGradientStart)
                }
            }
            state.scheduleItems.isEmpty() -> {
                TeamEmptyState(
                    emoji      = "📅",
                    title      = "No vaccinations scheduled",
                    subtitle   = "No babies are scheduled for this date at your bench",
                    dimensions = dimensions
                )
            }
            else -> {
                LazyColumn(
                    modifier        = Modifier.fillMaxSize(),
                    contentPadding  = PaddingValues(
                        horizontal = dimensions.screenPadding,
                        vertical   = dimensions.spacingSmall
                    ),
                    verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
                ) {
                    items(state.scheduleItems, key = { it.scheduleId }) { item ->
                        ScheduleItemCard(
                            item         = item,
                            onMarkDone   = { viewModel.openCompleteDialog(item.scheduleId) },
                            onMarkMissed = { viewModel.markAsMissed(item.scheduleId) },
                            customColors = customColors,
                            dimensions   = dimensions
                        )
                    }
                    item { Spacer(Modifier.height(dimensions.spacingXXLarge)) }
                }
            }
        }
    }

    // ── Complete dialog ───────────────────────────────────────────────────────
    state.completeForm?.let { form ->
        CompleteVaccinationDialog(
            form       = form,
            onDismiss  = viewModel::dismissCompleteDialog,
            onChange   = viewModel::updateCompleteForm,
            onSubmit   = viewModel::submitCompleteVaccination,
            dimensions = dimensions,
            customColors = customColors
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Schedule Summary Chip
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ScheduleSummaryChip(
    label     : String,
    value     : String,
    emoji     : String,
    color     : Color,
    dimensions: Dimensions,
    modifier  : Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape    = RoundedCornerShape(dimensions.cardCornerRadius),
        color    = color.copy(0.08f),
        border   = BorderStroke(dimensions.borderWidthThin, color.copy(0.2f))
    ) {
        Column(
            modifier            = Modifier.padding(dimensions.spacingSmall),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, style = MaterialTheme.typography.bodyMedium)
            Text(
                text       = value,
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color      = color
            )
            Text(
                text  = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(0.55f)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Schedule Item Card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ScheduleItemCard(
    item        : TeamScheduleItem,
    onMarkDone  : () -> Unit,
    onMarkMissed: () -> Unit,
    customColors: CustomColors,
    dimensions  : Dimensions
) {
    val isFemale    = item.gender.equals("GIRL", ignoreCase = true)
    val genderEmoji = if (isFemale) "👧" else "👦"
    val isCompleted = item.status == "COMPLETED"
    val isMissed    = item.status == "MISSED"
    val isDone      = isCompleted || isMissed

    val statusColor = when (item.status) {
        "COMPLETED"  -> customColors.success
        "MISSED"     -> MaterialTheme.colorScheme.error
        "OVERDUE"    -> customColors.warning
        "DUE_SOON"   -> customColors.warning
        else         -> customColors.accentGradientStart
    }
    val statusLabel = when (item.status) {
        "COMPLETED"  -> "✅ Completed"
        "MISSED"     -> "❌ Missed"
        "OVERDUE"    -> "⚠️ Overdue"
        "DUE_SOON"   -> "⏰ Due Soon"
        else         -> "📅 Scheduled"
    }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(dimensions.cardCornerRadius),
        colors    = CardDefaults.cardColors(
            containerColor = if (isDone)
                MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isDone) 0.dp else dimensions.cardElevationSmall
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensions.spacingMedium)
        ) {
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Baby avatar
                Box(
                    modifier         = Modifier
                        .size(dimensions.avatarSmall + dimensions.spacingXSmall)
                        .background(customColors.accentGradientStart.copy(0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(genderEmoji, style = MaterialTheme.typography.bodyMedium)
                }

                Spacer(Modifier.width(dimensions.spacingSmall))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text       = item.babyName,
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color      = if (isDone) MaterialTheme.colorScheme.onSurface.copy(0.5f)
                        else MaterialTheme.colorScheme.onSurface,
                        maxLines   = 1,
                        overflow   = TextOverflow.Ellipsis
                    )
                    Text(
                        text  = "${item.ageInMonths} months",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.5f)
                    )
                }

                // Status badge
                Surface(
                    shape = RoundedCornerShape(dimensions.filterTabCorner),
                    color = statusColor.copy(0.1f)
                ) {
                    Text(
                        text       = statusLabel,
                        style      = MaterialTheme.typography.labelSmall,
                        color      = statusColor,
                        fontWeight = FontWeight.SemiBold,
                        modifier   = Modifier.padding(
                            horizontal = dimensions.spacingXSmall * 2,
                            vertical   = 2.dp
                        )
                    )
                }
            }

            Spacer(Modifier.height(dimensions.spacingSmall))
            HorizontalDivider(
                color     = MaterialTheme.colorScheme.outlineVariant.copy(0.4f),
                thickness = dimensions.hairlineDividerThickness
            )
            Spacer(Modifier.height(dimensions.spacingSmall))

            // Vaccine info
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("💉", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.width(dimensions.spacingXSmall))
                Text(
                    text       = item.vaccineName,
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = if (isDone) MaterialTheme.colorScheme.onSurface.copy(0.5f)
                    else MaterialTheme.colorScheme.onSurface,
                    modifier   = Modifier.weight(1f)
                )
                Text(
                    text  = "Dose ${item.doseNumber}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.45f)
                )
            }

            // Action buttons
            if (!isDone) {
                Spacer(Modifier.height(dimensions.spacingSmall))
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
                ) {
                    Button(
                        onClick   = onMarkDone,
                        modifier  = Modifier.weight(1f),
                        colors    = ButtonDefaults.buttonColors(
                            containerColor = customColors.success
                        ),
                        shape     = RoundedCornerShape(dimensions.buttonCornerRadius),
                        contentPadding = PaddingValues(vertical = dimensions.spacingXSmall)
                    ) {
                        Icon(
                            Icons.Default.Check, null,
                            modifier = Modifier.size(dimensions.iconSmall)
                        )
                        Spacer(Modifier.width(dimensions.spacingXSmall))
                        Text("Complete", style = MaterialTheme.typography.labelSmall)
                    }

                    OutlinedButton(
                        onClick   = onMarkMissed,
                        modifier  = Modifier.weight(1f),
                        colors    = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border    = BorderStroke(dimensions.borderWidthThin, MaterialTheme.colorScheme.error.copy(0.5f)),
                        shape     = RoundedCornerShape(dimensions.buttonCornerRadius),
                        contentPadding = PaddingValues(vertical = dimensions.spacingXSmall)
                    ) {
                        Icon(
                            Icons.Default.Close, null,
                            modifier = Modifier.size(dimensions.iconSmall)
                        )
                        Spacer(Modifier.width(dimensions.spacingXSmall))
                        Text("Missed", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Complete Vaccination Dialog
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CompleteVaccinationDialog(
    form        : CompleteVaccinationForm,
    onDismiss   : () -> Unit,
    onChange    : ((CompleteVaccinationForm) -> CompleteVaccinationForm) -> Unit,
    onSubmit    : () -> Unit,
    dimensions  : Dimensions,
    customColors: CustomColors
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
            ) {
                Text("💉")
                Text("Complete Vaccination", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)) {
                OutlinedTextField(
                    value         = form.administeredDate,
                    onValueChange = { onChange { it.copy(administeredDate = it.administeredDate) } },
                    label         = { Text("Date (YYYY-MM-DD)") },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = customColors.accentGradientStart
                    )
                )
                OutlinedTextField(
                    value         = form.batchNumber,
                    onValueChange = { onChange { f -> f.copy(batchNumber = it) } },
                    label         = { Text("Batch Number (optional)") },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = customColors.accentGradientStart
                    )
                )
                OutlinedTextField(
                    value         = form.location,
                    onValueChange = { onChange { f -> f.copy(location = it) } },
                    label         = { Text("Location") },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = customColors.accentGradientStart
                    )
                )
                OutlinedTextField(
                    value         = form.notes,
                    onValueChange = { onChange { f -> f.copy(notes = it) } },
                    label         = { Text("Notes (optional)") },
                    singleLine    = false,
                    maxLines      = 3,
                    modifier      = Modifier.fillMaxWidth(),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = customColors.accentGradientStart
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick  = onSubmit,
                enabled  = !form.isLoading,
                colors   = ButtonDefaults.buttonColors(containerColor = customColors.success)
            ) {
                if (form.isLoading) {
                    CircularProgressIndicator(
                        color    = Color.White,
                        modifier = Modifier.size(dimensions.iconSmall),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Mark Completed ✅")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}