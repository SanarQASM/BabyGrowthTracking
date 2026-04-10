package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.screen

// ─────────────────────────────────────────────────────────────────────────────
// FIXES APPLIED:
//  Fix 4: Reschedule button/banner only shown when there are OVERDUE vaccinations.
//         If ALL overdue vaccinations are past the 2-month window (permanently MISSED),
//         show an informational message instead of the reschedule button.
//         Users are told which can/cannot be rescheduled before they proceed.
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import babygrowthtrackingapplication.composeapp.generated.resources.*
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model.*
import org.example.project.babygrowthtrackingapplication.theme.LocalDimensions
import org.example.project.babygrowthtrackingapplication.theme.customColors
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

// Maximum months a missed vaccination can still be rescheduled
// Must match backend: VaccinationRescheduleServiceImpl.MAX_OVERSHOOT_MONTHS = 2
private const val MAX_OVERSHOOT_MONTHS = 2

/**
 * Returns true if an OVERDUE vaccination is still within the reschedulable window.
 * Uses idealDate (DOB + recommendedAgeMonths) compared to today.
 * If idealDate is null or cannot be parsed, defaults to allowing reschedule (safe default).
 */
@OptIn(ExperimentalTime::class)
private fun VaccinationScheduleUi.canBeRescheduled(): Boolean {
    if (statusUi != ScheduleStatusUi.OVERDUE) return false
    return try {
        val ideal = LocalDate.parse(idealDate)
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

        // Manually calculate months between ideal and today
        val monthsOverdue = (today.year - ideal.year) * 12 + (today.month.number - ideal.month.number)

        monthsOverdue <= MAX_OVERSHOOT_MONTHS
    } catch (_: Exception) {
        true // safe default: allow if we can't parse
    }
}

@Composable
fun VaccinationScheduleView(
    schedules      : List<VaccinationScheduleUi>,
    filter         : VaccinationFilter,
    loading        : Boolean,
    onFilterChange : (VaccinationFilter) -> Unit,
    onItemClick    : (VaccinationScheduleUi) -> Unit,
    onReschedule   : () -> Unit
) {
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors

    val filtered = remember(schedules, filter) {
        when (filter) {
            VaccinationFilter.ALL       -> schedules
            VaccinationFilter.UPCOMING  -> schedules.filter {
                it.statusUi == ScheduleStatusUi.UPCOMING || it.statusUi == ScheduleStatusUi.DUE_SOON
            }
            VaccinationFilter.COMPLETED -> schedules.filter { it.statusUi == ScheduleStatusUi.COMPLETED }
            VaccinationFilter.OVERDUE   -> schedules.filter {
                it.statusUi == ScheduleStatusUi.OVERDUE || it.statusUi == ScheduleStatusUi.MISSED
            }
        }
    }

    val completedCount = schedules.count { it.statusUi == ScheduleStatusUi.COMPLETED }
    val totalCount     = schedules.size

    // FIX 4: Classify overdue vaccinations into reschedulable vs. permanently missed
    val overdueSchedules       = schedules.filter { it.statusUi == ScheduleStatusUi.OVERDUE }
    val permanentlyMissed      = schedules.filter { it.statusUi == ScheduleStatusUi.MISSED }
    val reschedulableOverdue   = overdueSchedules.filter { it.canBeRescheduled() }
    val tooLateOverdue         = overdueSchedules.filter { !it.canBeRescheduled() }

    // FIX 4: Show reschedule banner ONLY when there are actually reschedulable items
    val hasReschedulable       = reschedulableOverdue.isNotEmpty()
    // FIX 4: Show "cannot reschedule" info when there are overdue items but NONE can be rescheduled
    val hasOnlyTooLate         = overdueSchedules.isNotEmpty() && reschedulableOverdue.isEmpty()
    val overdueCount           = overdueSchedules.size
    val totalTooLate           = tooLateOverdue.size + permanentlyMissed.size

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
    ) {
        if (totalCount > 0) {
            VaccinationProgressBar(
                completed = completedCount,
                total     = totalCount,
                overdue   = overdueCount
            )
        }

        // FIX 4: Reschedule banner — only shown when reschedulable items exist
        if (hasReschedulable) {
            RescheduleBanner(
                overdueCount       = overdueCount,
                reschedulableCount = reschedulableOverdue.size,
                tooLateCount       = tooLateOverdue.size,
                onReschedule       = onReschedule
            )
        }

        // FIX 4: "Cannot reschedule" info card when all overdue are past the window
        if (hasOnlyTooLate) {
            CannotRescheduleBanner(
                tooLateCount = totalTooLate
            )
        }

        ScrollableTabRow(
            selectedTabIndex = filter.ordinal,
            edgePadding      = dimensions.screenPadding,
            containerColor   = Color.Transparent,
            contentColor     = customColors.accentGradientStart,
            divider          = {}
        ) {
            VaccinationFilter.entries.forEach { f ->
                Tab(
                    selected = filter == f,
                    onClick  = { onFilterChange(f) },
                    text = {
                        Text(
                            text = when (f) {
                                VaccinationFilter.ALL       -> stringResource(Res.string.bench_filter_all)
                                VaccinationFilter.UPCOMING  -> stringResource(Res.string.schedule_section_upcoming)
                                VaccinationFilter.COMPLETED -> stringResource(Res.string.schedule_section_completed)
                                VaccinationFilter.OVERDUE   -> stringResource(Res.string.schedule_status_overdue)
                            },
                            style      = MaterialTheme.typography.labelMedium,
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
            ) { CircularProgressIndicator(color = customColors.accentGradientStart) }
        } else if (filtered.isEmpty()) {
            Box(
                Modifier.fillMaxWidth().height(dimensions.iconXLarge + dimensions.spacingXLarge),
                Alignment.Center
            ) {
                Text(
                    text  = stringResource(Res.string.schedule_no_vaccinations_found),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.4f)
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall),
                contentPadding      = PaddingValues(
                    horizontal = dimensions.screenPadding,
                    vertical   = dimensions.spacingSmall
                )
            ) {
                items(filtered, key = { it.scheduleId }) { item ->
                    VaccinationScheduleCard(item = item, onClick = { onItemClick(item) })
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Progress bar — unchanged
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun VaccinationProgressBar(completed: Int, total: Int, overdue: Int) {
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors
    val progress     = if (total > 0) completed.toFloat() / total else 0f

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = dimensions.screenPadding),
        shape  = RoundedCornerShape(dimensions.cardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier            = Modifier.padding(dimensions.spacingMedium),
            verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
        ) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text       = stringResource(Res.string.schedule_progress),
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text       = "$completed / $total",
                    style      = MaterialTheme.typography.labelMedium,
                    color      = customColors.accentGradientStart,
                    fontWeight = FontWeight.Bold
                )
            }
            LinearProgressIndicator(
                progress   = { progress },
                modifier   = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(50))
                    .height(dimensions.vaccinationProgressBarHeight),
                color      = customColors.accentGradientStart,
                trackColor = customColors.accentGradientStart.copy(0.15f)
            )
            if (overdue > 0) {
                Text(
                    text  = stringResource(Res.string.schedule_overdue_count, overdue),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// FIX 4: Reschedule banner — now shows reschedulable count vs too-late count
// Only rendered when reschedulable items exist
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun RescheduleBanner(
    overdueCount      : Int,
    reschedulableCount: Int,
    tooLateCount      : Int,
    onReschedule      : () -> Unit
) {
    val dimensions = LocalDimensions.current

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = dimensions.screenPadding),
        shape  = RoundedCornerShape(dimensions.cardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(dimensions.spacingMedium)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text       = stringResource(Res.string.schedule_overdue_count, overdueCount),
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text  = stringResource(Res.string.schedule_reschedule_tap_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer.copy(0.7f)
                    )
                    // FIX 4: Show breakdown when some are too late
                    if (tooLateCount > 0) {
                        Spacer(Modifier.height(dimensions.spacingXSmall))
                        Text(
                            text  = "✅ $reschedulableCount can reschedule  •  ❌ $tooLateCount too late (past ${MAX_OVERSHOOT_MONTHS}mo window)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer.copy(0.8f)
                        )
                    }
                }
                TextButton(
                    onClick = onReschedule,
                    colors  = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(Res.string.schedule_reschedule_action), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// FIX 4: Info banner shown when ALL overdue vaccinations are past the window
// No reschedule button — informs user to consult a doctor
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CannotRescheduleBanner(tooLateCount: Int) {
    val dimensions = LocalDimensions.current

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = dimensions.screenPadding),
        shape  = RoundedCornerShape(dimensions.cardCornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth().padding(dimensions.spacingMedium),
            horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall),
            verticalAlignment     = Alignment.Top
        ) {
            Text("ℹ️", style = MaterialTheme.typography.titleMedium)
            Column {
                Text(
                    text       = "$tooLateCount vaccination(s) cannot be rescheduled",
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(dimensions.borderWidthMedium))
                Text(
                    text  = "These vaccinations are more than ${MAX_OVERSHOOT_MONTHS} months overdue and cannot be automatically rescheduled. Please consult a doctor about whether they can still be administered.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.7f)
                )
                Spacer(Modifier.height(dimensions.spacingSmall))
                Text(
                    text  = "💉 Upcoming vaccinations are unaffected and continue normally.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Individual vaccination card — unchanged
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun VaccinationScheduleCard(item: VaccinationScheduleUi, onClick: () -> Unit) {
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors

    val (statusColor, statusIcon, statusLabel) = when (item.statusUi) {
        ScheduleStatusUi.COMPLETED   -> Triple(Color(0xFF22C55E), "✅", stringResource(Res.string.schedule_status_completed_label))
        ScheduleStatusUi.OVERDUE     -> Triple(MaterialTheme.colorScheme.error, "⚠️", stringResource(Res.string.schedule_status_overdue_label))
        ScheduleStatusUi.MISSED      -> Triple(Color(0xFFEF4444), "❌", stringResource(Res.string.schedule_status_missed_label))
        ScheduleStatusUi.DUE_SOON    -> Triple(Color(0xFFF59E0B), "⏰", stringResource(Res.string.schedule_status_due_soon_label))
        ScheduleStatusUi.RESCHEDULED -> Triple(Color(0xFF8B5CF6), "🔄", stringResource(Res.string.schedule_status_rescheduled_label))
        ScheduleStatusUi.UPCOMING    -> Triple(customColors.accentGradientStart, "💉", stringResource(Res.string.schedule_status_upcoming_label))
    }

    Card(
        modifier  = Modifier.fillMaxWidth().clickable { onClick() },
        shape     = RoundedCornerShape(dimensions.cardCornerRadius),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(dimensions.healthSubTabElevation)
    ) {
        Row(
            modifier              = Modifier.padding(dimensions.spacingMedium),
            horizontalArrangement = Arrangement.spacedBy(dimensions.spacingMedium),
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Box(
                modifier         = Modifier
                    .size(dimensions.vaccinationCardIconSize)
                    .clip(CircleShape)
                    .background(statusColor.copy(0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(statusIcon, style = MaterialTheme.typography.titleMedium)
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(dimensions.borderWidthMedium)) {
                Text(text = item.vaccineName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(
                    text  = stringResource(Res.string.schedule_dose, item.doseNumber) +
                            " · ${stringResource(Res.string.schedule_age_months, item.recommendedAgeMonths)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                )
                Text(
                    text = when {
                        item.statusUi == ScheduleStatusUi.COMPLETED ->
                            stringResource(Res.string.schedule_completed_prefix, item.completedDate ?: item.scheduledDate)
                        else ->
                            stringResource(Res.string.schedule_scheduled_prefix, item.scheduledDate)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.5f)
                )
                if (item.shiftDays > 0) {
                    Text(
                        text  = stringResource(Res.string.schedule_shift_days, item.shiftDays),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF8B5CF6)
                    )
                }
                // FIX 4: Show "cannot reschedule" badge on individual cards too
                if (item.statusUi == ScheduleStatusUi.OVERDUE) {
                    val canReschedule = item.canBeRescheduled()
                    if (!canReschedule) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.errorContainer.copy(0.5f)
                        ) {
                            Text(
                                text     = "Past ${MAX_OVERSHOOT_MONTHS}mo window — consult doctor",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style    = MaterialTheme.typography.labelSmall,
                                color    = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            Surface(shape = RoundedCornerShape(50), color = statusColor.copy(0.12f)) {
                Text(
                    text       = statusLabel,
                    modifier   = Modifier.padding(horizontal = dimensions.spacingSmall, vertical = dimensions.spacingXSmall),
                    style      = MaterialTheme.typography.labelSmall,
                    color      = statusColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Reschedule dialogs — unchanged from original
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun RescheduleReasonPickerDialog(
    overdueCount      : Int,
    reschedulableCount: Int,
    isLoading         : Boolean,
    onConfirm         : (reason: RescheduleReason, notes: String) -> Unit,
    onDismiss         : () -> Unit
) {
    val dimensions     = LocalDimensions.current
    var selectedReason by remember { mutableStateOf<RescheduleReason?>(null) }
    var notes          by remember { mutableStateOf("") }
    val customColors   = MaterialTheme.customColors

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = {
            Column {
                Text(
                    stringResource(Res.string.reschedule_title),
                    style      = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(dimensions.spacingXSmall))
                Row(horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)) {
                    if (overdueCount > 0) {
                        Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.errorContainer) {
                            Text(
                                stringResource(Res.string.schedule_overdue_count, overdueCount),
                                modifier   = Modifier.padding(horizontal = dimensions.spacingSmall + dimensions.borderWidthMedium, vertical = dimensions.spacingXSmall - dimensions.borderWidthThin),
                                style      = MaterialTheme.typography.labelSmall,
                                color      = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Surface(shape = RoundedCornerShape(50), color = customColors.accentGradientStart.copy(0.12f)) {
                        Text(
                            stringResource(Res.string.schedule_reschedulable_count, reschedulableCount),
                            modifier   = Modifier.padding(horizontal = dimensions.spacingSmall + dimensions.borderWidthMedium, vertical = dimensions.spacingXSmall - dimensions.borderWidthThin),
                            style      = MaterialTheme.typography.labelSmall,
                            color      = customColors.accentGradientStart,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier            = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
            ) {
                Text(
                    text       = stringResource(Res.string.schedule_reschedule_reason_why),
                    style      = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onSurface
                )

                RescheduleReason.entries.forEach { reason ->
                    val isSelected = selectedReason == reason
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { selectedReason = reason },
                        shape  = RoundedCornerShape(dimensions.cardCornerRadius - dimensions.spacingXSmall),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) customColors.accentGradientStart.copy(0.12f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        border = if (isSelected) BorderStroke(dimensions.borderWidthThin + 0.5.dp, customColors.accentGradientStart) else null
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .padding(horizontal = dimensions.spacingMedium, vertical = dimensions.spacingSmall + dimensions.spacingXSmall),
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall + dimensions.borderWidthMedium)
                        ) {
                            Text(reason.emoji, style = MaterialTheme.typography.titleMedium)
                            Text(
                                text       = reason.displayEn,
                                style      = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color      = if (isSelected) customColors.accentGradientStart else MaterialTheme.colorScheme.onSurface,
                                modifier   = Modifier.weight(1f)
                            )
                            if (isSelected) {
                                Icon(Icons.Default.Check, null, tint = customColors.accentGradientStart, modifier = Modifier.size(dimensions.iconSmall + dimensions.borderWidthMedium))
                            }
                        }
                    }
                }

                Spacer(Modifier.height(dimensions.spacingXSmall))

                OutlinedTextField(
                    value         = notes,
                    onValueChange = { notes = it },
                    label         = { Text(stringResource(Res.string.schedule_reschedule_notes_label)) },
                    placeholder   = { Text(stringResource(Res.string.schedule_reschedule_notes_placeholder)) },
                    modifier      = Modifier.fillMaxWidth(),
                    maxLines      = 3,
                    shape         = RoundedCornerShape(dimensions.cardCornerRadius - dimensions.spacingXSmall)
                )

                Surface(
                    shape = RoundedCornerShape(dimensions.cardCornerRadius - dimensions.spacingXSmall - dimensions.borderWidthMedium),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(dimensions.spacingSmall + dimensions.spacingXSmall),
                        horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
                    ) {
                        Text("ℹ️", style = MaterialTheme.typography.bodySmall)
                        Text(
                            text  = stringResource(Res.string.schedule_reschedule_info),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick  = { selectedReason?.let { reason -> onConfirm(reason, notes) } },
                enabled  = selectedReason != null && !isLoading,
                colors   = ButtonDefaults.buttonColors(containerColor = customColors.accentGradientStart),
                shape    = RoundedCornerShape(dimensions.buttonCornerRadius)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(dimensions.iconSmall), strokeWidth = dimensions.borderWidthMedium, color = MaterialTheme.colorScheme.onPrimary)
                    Spacer(Modifier.width(dimensions.spacingSmall))
                    Text(stringResource(Res.string.schedule_rescheduling_in_progress))
                } else {
                    Icon(Icons.Default.Refresh, null, modifier = Modifier.size(dimensions.iconSmall + dimensions.borderWidthMedium))
                    Spacer(Modifier.width(dimensions.spacingXSmall + dimensions.borderWidthThin))
                    Text(stringResource(Res.string.schedule_reschedule_all), fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = { if (!isLoading) onDismiss() }) {
                Text(stringResource(Res.string.btn_cancel))
            }
        }
    )
}

@Composable
fun RescheduleResultDialog(result: RescheduleResultUi, onDismiss: () -> Unit) {
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(stringResource(Res.string.schedule_reschedule_complete_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(dimensions.spacingSmall))
                Row(horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)) {
                    SummaryChip(label = stringResource(Res.string.schedule_reschedule_status_rescheduled, result.rescheduledCount), color = Color(0xFF22C55E), emoji = "✅")
                    if (result.tooLateCount > 0) SummaryChip(label = stringResource(Res.string.schedule_reschedule_status_too_late, result.tooLateCount), color = MaterialTheme.colorScheme.error, emoji = "❌")
                    if (result.skippedCount > 0) SummaryChip(label = stringResource(Res.string.schedule_reschedule_status_skipped, result.skippedCount), color = MaterialTheme.colorScheme.onSurface.copy(0.5f), emoji = "⏭️")
                }
            }
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall),
                modifier = Modifier.heightIn(max = dimensions.avatarLarge * 4 + dimensions.spacingXLarge)
            ) {
                val rescheduled = result.items.filter { it.rescheduled }
                val tooLate     = result.items.filter { it.tooLate }
                val skipped     = result.items.filter { !it.rescheduled && !it.tooLate }

                if (rescheduled.isNotEmpty()) {
                    item { ResultSectionHeader(stringResource(Res.string.schedule_reschedule_status_rescheduled, rescheduled.size), Color(0xFF22C55E)) }
                    items(rescheduled) { item -> RescheduleResultItemRow(item, customColors.accentGradientStart) }
                }
                if (tooLate.isNotEmpty()) {
                    item { Spacer(Modifier.height(dimensions.spacingXSmall)); ResultSectionHeader(stringResource(Res.string.schedule_reschedule_status_too_late, tooLate.size), MaterialTheme.colorScheme.error) }
                    items(tooLate) { item -> RescheduleResultItemRow(item, MaterialTheme.colorScheme.error) }
                    item {
                        Surface(shape = RoundedCornerShape(dimensions.spacingSmall), color = MaterialTheme.colorScheme.errorContainer.copy(0.5f)) {
                            Text(text = stringResource(Res.string.schedule_too_late_medical_advice), modifier = Modifier.padding(dimensions.spacingSmall + dimensions.spacingXSmall), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }
                if (skipped.isNotEmpty()) {
                    item { Spacer(Modifier.height(dimensions.spacingXSmall)); ResultSectionHeader(stringResource(Res.string.schedule_reschedule_status_skipped, skipped.size), MaterialTheme.colorScheme.onSurface.copy(0.5f)) }
                    items(skipped) { item -> RescheduleResultItemRow(item, MaterialTheme.colorScheme.onSurface.copy(0.5f)) }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = customColors.accentGradientStart), shape = RoundedCornerShape(dimensions.buttonCornerRadius)) {
                Text(stringResource(Res.string.schedule_reschedule_done), fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun SummaryChip(label: String, color: Color, emoji: String) {
    val dimensions = LocalDimensions.current
    Surface(shape = RoundedCornerShape(50), color = color.copy(0.12f)) {
        Row(modifier = Modifier.padding(horizontal = dimensions.spacingSmall, vertical = dimensions.borderWidthThin + dimensions.borderWidthMedium), horizontalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall), verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, style = MaterialTheme.typography.labelSmall)
            Text(label, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun ResultSectionHeader(title: String, color: Color) {
    val dimensions = LocalDimensions.current
    Text(text = title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = color, modifier = Modifier.padding(vertical = dimensions.borderWidthMedium))
}

@Composable
private fun RescheduleResultItemRow(item: RescheduleItemUi, color: Color) {
    val dimensions = LocalDimensions.current
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(dimensions.cardCornerRadius - dimensions.spacingXSmall - dimensions.borderWidthMedium), colors = CardDefaults.cardColors(containerColor = color.copy(0.07f))) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = dimensions.spacingMedium, vertical = dimensions.spacingSmall + dimensions.borderWidthMedium), verticalArrangement = Arrangement.spacedBy(dimensions.borderWidthThin + dimensions.borderWidthMedium)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = item.vaccineName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                Text(text = stringResource(Res.string.schedule_dose, item.doseNumber) + " · ${item.recommendedAgeMonths}mo", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
            }
            if (item.rescheduled && item.newDate != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall)) {
                    Text("📅 ${item.oldDate}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(0.4f))
                    Text("→", style = MaterialTheme.typography.labelSmall, color = color)
                    Text(item.newDate, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.SemiBold)
                }
            } else if (item.skipReason != null) {
                Text(text = item.skipReason, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// VaccinationDetailScreen — unchanged
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaccinationDetailScreen(
    item        : VaccinationScheduleUi,
    onBack      : () -> Unit,
    onReschedule: () -> Unit
) {
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors

    val (statusColor, statusIcon) = when (item.statusUi) {
        ScheduleStatusUi.COMPLETED   -> Pair(Color(0xFF22C55E), "✅")
        ScheduleStatusUi.OVERDUE     -> Pair(MaterialTheme.colorScheme.error, "⚠️")
        ScheduleStatusUi.MISSED      -> Pair(Color(0xFFEF4444), "❌")
        ScheduleStatusUi.DUE_SOON    -> Pair(Color(0xFFF59E0B), "⏰")
        ScheduleStatusUi.RESCHEDULED -> Pair(Color(0xFF8B5CF6), "🔄")
        ScheduleStatusUi.UPCOMING    -> Pair(customColors.accentGradientStart, "💉")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(item.vaccineName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = customColors.accentGradientStart.copy(0.12f))
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(padding)) {
            Box(modifier = Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(statusColor.copy(0.15f), Color.Transparent))).padding(dimensions.spacingLarge), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)) {
                    Text(statusIcon, style = MaterialTheme.typography.displaySmall)
                    Text(item.vaccineName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(stringResource(Res.string.schedule_dose, item.doseNumber), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground.copy(0.6f))
                }
            }
            Spacer(Modifier.height(dimensions.spacingMedium))
            Column(modifier = Modifier.padding(horizontal = dimensions.screenPadding), verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)) {
                DetailInfoCard {
                    InfoRow(stringResource(Res.string.vax_detail_age_recommended), "${item.recommendedAgeMonths} months")
                    InfoRow(stringResource(Res.string.vax_detail_ideal_date), item.idealDate)
                    InfoRow(stringResource(Res.string.vax_detail_scheduled_date), item.scheduledDate)
                    if (item.shiftDays > 0) InfoRow(stringResource(Res.string.vax_detail_adjustment), stringResource(Res.string.vax_detail_adjustment_value, item.shiftDays, item.shiftReason.lowercase()))
                    item.completedDate?.let { InfoRow(stringResource(Res.string.vax_detail_completed_on), it) }
                    InfoRow(stringResource(Res.string.vax_detail_health_center), item.benchNameEn)
                    InfoRow(stringResource(Res.string.vax_detail_status), item.status.replace("_", " "))
                }
                // FIX 4: Reschedule button only for OVERDUE, and only if within window
                if (item.statusUi == ScheduleStatusUi.OVERDUE) {
                    val canReschedule = item.canBeRescheduled()
                    Spacer(Modifier.height(dimensions.spacingSmall))
                    if (canReschedule) {
                        Button(
                            onClick  = onReschedule,
                            modifier = Modifier.fillMaxWidth().height(dimensions.buttonHeight),
                            shape    = RoundedCornerShape(dimensions.buttonCornerRadius),
                            colors   = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Refresh, null, modifier = Modifier.size(dimensions.iconSmall + dimensions.borderWidthMedium))
                            Spacer(Modifier.width(dimensions.spacingSmall))
                            Text(stringResource(Res.string.schedule_reschedule_all), fontWeight = FontWeight.Bold)
                        }
                    } else {
                        // FIX 4: Show info card instead of button when cannot reschedule
                        Surface(
                            shape = RoundedCornerShape(dimensions.cardCornerRadius),
                            color = MaterialTheme.colorScheme.errorContainer.copy(0.5f)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(dimensions.spacingMedium),
                                horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text("⚠️", style = MaterialTheme.typography.bodyMedium)
                                Column {
                                    Text("Cannot Reschedule", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                                    Text("This vaccination is more than ${MAX_OVERSHOOT_MONTHS} months past its ideal date. Please consult a doctor.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(dimensions.spacingXXLarge))
        }
    }
}

@Composable
private fun DetailInfoCard(content: @Composable ColumnScope.() -> Unit) {
    val dimensions = LocalDimensions.current
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(dimensions.cardCornerRadius), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(dimensions.spacingMedium), verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)) { content() }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(0.55f))
        Text(text = value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
    }
}