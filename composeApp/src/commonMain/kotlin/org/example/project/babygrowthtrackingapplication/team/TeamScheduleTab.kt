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
import babygrowthtrackingapplication.composeapp.generated.resources.Res
import babygrowthtrackingapplication.composeapp.generated.resources.*
import org.example.project.babygrowthtrackingapplication.theme.*
import org.jetbrains.compose.resources.stringResource

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
                        val days = (ms / 86_400_000L).toInt()
                        val d    = kotlinx.datetime.LocalDate.fromEpochDays(days)
                        val iso  = "${d.year}-${d.monthNumber.toString().padStart(2, '0')}-${d.dayOfMonth.toString().padStart(2, '0')}"
                        viewModel.onDateSelected(iso)
                    }
                    showDatePicker = false
                }) { Text(stringResource(Res.string.add_baby_date_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(Res.string.add_baby_date_cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {

        // ── Date selector ─────────────────────────────────────────────────────
        Surface(
            color           = MaterialTheme.colorScheme.surface,
            shadowElevation = dimensions.cardElevationSmall
        ) {
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimensions.screenPadding, vertical = dimensions.spacingSmall),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
            ) {
                Icon(
                    Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint     = customColors.accentGradientStart,
                    modifier = Modifier.size(dimensions.iconMedium)
                )
                Text(
                    text       = state.selectedDate.ifBlank { stringResource(Res.string.team_select_date_hint) },
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onSurface,
                    modifier   = Modifier.weight(1f)
                )
                OutlinedButton(
                    onClick        = { showDatePicker = true },
                    shape          = RoundedCornerShape(dimensions.buttonCornerRadius),
                    border         = BorderStroke(dimensions.borderWidthThin, customColors.accentGradientStart.copy(alpha = 0.5f)),
                    colors         = ButtonDefaults.outlinedButtonColors(contentColor = customColors.accentGradientStart),
                    contentPadding = PaddingValues(
                        horizontal = dimensions.spacingSmall,
                        vertical   = dimensions.spacingXSmall
                    )
                ) {
                    Icon(Icons.Default.EditCalendar, null, modifier = Modifier.size(dimensions.iconSmall))
                    Spacer(Modifier.width(dimensions.spacingXSmall))
                    Text(stringResource(Res.string.team_change_date), style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        // ── Summary chips ─────────────────────────────────────────────────────
        val total     = state.scheduleItems.size
        val completed = state.scheduleItems.count { it.status == "COMPLETED" }
        val remaining = total - completed

        if (!state.scheduleLoading && total > 0) {
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimensions.screenPadding, vertical = dimensions.spacingSmall),
                horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
            ) {
                ScheduleSummaryChip(
                    label      = stringResource(Res.string.team_schedule_stat_scheduled),
                    value      = total.toString(),
                    emoji      = stringResource(Res.string.team_emoji_vaccine),
                    color      = customColors.accentGradientStart,
                    dimensions = dimensions,
                    modifier   = Modifier.weight(1f)
                )
                ScheduleSummaryChip(
                    label      = stringResource(Res.string.team_schedule_stat_done),
                    value      = completed.toString(),
                    emoji      = stringResource(Res.string.team_emoji_completed),
                    color      = customColors.success,
                    dimensions = dimensions,
                    modifier   = Modifier.weight(1f)
                )
                ScheduleSummaryChip(
                    label      = stringResource(Res.string.team_schedule_stat_remaining),
                    value      = remaining.toString(),
                    emoji      = stringResource(Res.string.team_emoji_remaining),
                    color      = customColors.warning,
                    dimensions = dimensions,
                    modifier   = Modifier.weight(1f)
                )
            }
        }

        // ── Schedule list ─────────────────────────────────────────────────────
        when {
            state.scheduleLoading -> {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator(color = customColors.accentGradientStart)
                }
            }
            state.scheduleItems.isEmpty() -> {
                TeamEmptyState(
                    emoji      = stringResource(Res.string.team_emoji_scheduled),
                    title      = stringResource(Res.string.team_empty_schedule_title),
                    subtitle   = stringResource(Res.string.team_empty_schedule_subtitle),
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
            form         = form,
            onDismiss    = viewModel::dismissCompleteDialog,
            onChange     = viewModel::updateCompleteForm,
            onSubmit     = viewModel::submitCompleteVaccination,
            dimensions   = dimensions,
            customColors = customColors
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Summary Chip
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
        color    = color.copy(alpha = 0.08f),
        border   = BorderStroke(dimensions.borderWidthThin, color.copy(alpha = 0.2f))
    ) {
        Column(
            modifier            = Modifier.padding(dimensions.spacingSmall),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, style = MaterialTheme.typography.bodyMedium)
            Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
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
    val genderEmoji = if (isFemale) stringResource(Res.string.team_emoji_girl) else stringResource(Res.string.team_emoji_boy)
    val isCompleted = item.status == "COMPLETED"
    val isMissed    = item.status == "MISSED"
    val isDone      = isCompleted || isMissed

    val statusColor = when (item.status) {
        "COMPLETED" -> customColors.success
        "MISSED"    -> MaterialTheme.colorScheme.error
        "OVERDUE"   -> customColors.warning
        "DUE_SOON"  -> customColors.warning
        else        -> customColors.accentGradientStart
    }
    val statusLabel = when (item.status) {
        "COMPLETED" -> stringResource(Res.string.team_status_completed_label)
        "MISSED"    -> stringResource(Res.string.team_status_missed_label)
        "OVERDUE"   -> stringResource(Res.string.team_status_overdue_label)
        "DUE_SOON"  -> stringResource(Res.string.team_status_due_soon_label)
        else        -> stringResource(Res.string.team_status_scheduled_label)
    }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(dimensions.cardCornerRadius),
        colors    = CardDefaults.cardColors(
            containerColor = if (isDone) MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isDone) dimensions.cardElevationSmall * 0f else dimensions.cardElevationSmall
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
                Box(
                    modifier         = Modifier
                        .size(dimensions.avatarSmall + dimensions.spacingXSmall)
                        .background(customColors.accentGradientStart.copy(alpha = 0.12f), CircleShape),
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
                        color      = if (isDone) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.onSurface,
                        maxLines   = 1,
                        overflow   = TextOverflow.Ellipsis
                    )
                    Text(
                        text  = stringResource(Res.string.team_baby_age_card, item.ageInMonths),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(dimensions.filterTabCorner),
                    color = statusColor.copy(alpha = 0.1f)
                ) {
                    Text(
                        text       = statusLabel,
                        style      = MaterialTheme.typography.labelSmall,
                        color      = statusColor,
                        fontWeight = FontWeight.SemiBold,
                        modifier   = Modifier.padding(
                            horizontal = dimensions.spacingXSmall * 2,
                            vertical   = dimensions.spacingXSmall / 2
                        )
                    )
                }
            }

            Spacer(Modifier.height(dimensions.spacingSmall))
            HorizontalDivider(
                color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                thickness = dimensions.hairlineDividerThickness
            )
            Spacer(Modifier.height(dimensions.spacingSmall))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(Res.string.team_emoji_vaccine), style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.width(dimensions.spacingXSmall))
                Text(
                    text       = item.vaccineName,
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = if (isDone) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    else MaterialTheme.colorScheme.onSurface,
                    modifier   = Modifier.weight(1f)
                )
                Text(
                    text  = stringResource(Res.string.team_dose_label, item.doseNumber),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                )
            }

            if (!isDone) {
                Spacer(Modifier.height(dimensions.spacingSmall))
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
                ) {
                    Button(
                        onClick        = onMarkDone,
                        modifier       = Modifier.weight(1f),
                        colors         = ButtonDefaults.buttonColors(containerColor = customColors.success),
                        shape          = RoundedCornerShape(dimensions.buttonCornerRadius),
                        contentPadding = PaddingValues(vertical = dimensions.spacingXSmall)
                    ) {
                        Icon(Icons.Default.Check, null, modifier = Modifier.size(dimensions.iconSmall))
                        Spacer(Modifier.width(dimensions.spacingXSmall))
                        Text(stringResource(Res.string.team_action_complete), style = MaterialTheme.typography.labelSmall)
                    }

                    OutlinedButton(
                        onClick        = onMarkMissed,
                        modifier       = Modifier.weight(1f),
                        colors         = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        border         = BorderStroke(dimensions.borderWidthThin, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                        shape          = RoundedCornerShape(dimensions.buttonCornerRadius),
                        contentPadding = PaddingValues(vertical = dimensions.spacingXSmall)
                    ) {
                        Icon(Icons.Default.Close, null, modifier = Modifier.size(dimensions.iconSmall))
                        Spacer(Modifier.width(dimensions.spacingXSmall))
                        Text(stringResource(Res.string.team_action_missed), style = MaterialTheme.typography.labelSmall)
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
                Text(stringResource(Res.string.team_emoji_vaccine))
                Text(stringResource(Res.string.team_dialog_complete_vac_title), fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)) {
                OutlinedTextField(
                    value         = form.administeredDate,
                    onValueChange = { onChange { it.copy(administeredDate = it.administeredDate) } },
                    label         = { Text(stringResource(Res.string.team_field_date_hint)) },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                    colors        = OutlinedTextFieldDefaults.colors(focusedBorderColor = customColors.accentGradientStart)
                )
                OutlinedTextField(
                    value         = form.batchNumber,
                    onValueChange = { onChange { f -> f.copy(batchNumber = it) } },
                    label         = { Text(stringResource(Res.string.team_field_batch)) },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                    colors        = OutlinedTextFieldDefaults.colors(focusedBorderColor = customColors.accentGradientStart)
                )
                OutlinedTextField(
                    value         = form.location,
                    onValueChange = { onChange { f -> f.copy(location = it) } },
                    label         = { Text(stringResource(Res.string.team_field_location)) },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                    colors        = OutlinedTextFieldDefaults.colors(focusedBorderColor = customColors.accentGradientStart)
                )
                OutlinedTextField(
                    value         = form.notes,
                    onValueChange = { onChange { f -> f.copy(notes = it) } },
                    label         = { Text(stringResource(Res.string.team_field_notes)) },
                    singleLine    = false,
                    maxLines      = 3,
                    modifier      = Modifier.fillMaxWidth(),
                    colors        = OutlinedTextFieldDefaults.colors(focusedBorderColor = customColors.accentGradientStart)
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
                        color       = MaterialTheme.colorScheme.onPrimary,
                        modifier    = Modifier.size(dimensions.iconSmall),
                        strokeWidth = dimensions.borderWidthMedium
                    )
                } else {
                    Text(stringResource(Res.string.team_action_mark_completed))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.btn_cancel)) }
        }
    )
}