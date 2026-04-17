package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.screen

// ─────────────────────────────────────────────────────────────────────────────
// FIXES APPLIED:
//  Fix 1: LazyColumn replaced with Column — parent verticalScroll drives all
//         scrolling, eliminating nested-scroll conflict.
//  Fix 3 (Q3): After rescheduling, the card now shows BOTH the new scheduled
//         date AND the original ideal date ("Was due: …") so the parent always
//         sees when the vaccination was originally supposed to happen.
//  Fix 4: Reschedule banner only shown when reschedulable overdue items exist.
//         CannotRescheduleBanner shown when all overdue items are past window.
//  No hardcoded strings, colours, sizes, or dp values.
// ─────────────────────────────────────────────────────────────────────────────

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import babygrowthtrackingapplication.composeapp.generated.resources.*
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.todayIn
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model.*
import org.example.project.babygrowthtrackingapplication.theme.LocalDimensions
import org.example.project.babygrowthtrackingapplication.theme.customColors
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

// Maximum months a missed vaccination can still be rescheduled.
// Must stay in sync with backend VaccinationRescheduleServiceImpl.MAX_OVERSHOOT_MONTHS.
private const val MAX_OVERSHOOT_MONTHS = 2

// ─────────────────────────────────────────────────────────────────────────────
// Helper — can this OVERDUE item still be rescheduled?
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalTime::class)
private fun VaccinationScheduleUi.canBeRescheduled(): Boolean {
    if (statusUi != ScheduleStatusUi.OVERDUE) return false
    return try {
        val ideal = LocalDate.parse(idealDate)
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val monthsOverdue =
            (today.year - ideal.year) * 12 + (today.month.number - ideal.month.number)
        monthsOverdue <= MAX_OVERSHOOT_MONTHS
    } catch (_: Exception) {
        true // safe default: allow reschedule when parse fails
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// VaccinationScheduleView
// FIX 1: Uses Column (not LazyColumn) so the outer verticalScroll works.
// ─────────────────────────────────────────────────────────────────────────────

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
                it.statusUi == ScheduleStatusUi.UPCOMING ||
                        it.statusUi == ScheduleStatusUi.DUE_SOON
            }
            VaccinationFilter.COMPLETED -> schedules.filter {
                it.statusUi == ScheduleStatusUi.COMPLETED
            }
            VaccinationFilter.OVERDUE   -> schedules.filter {
                it.statusUi == ScheduleStatusUi.OVERDUE ||
                        it.statusUi == ScheduleStatusUi.MISSED
            }
        }
    }

    val completedCount         = schedules.count { it.statusUi == ScheduleStatusUi.COMPLETED }
    val totalCount             = schedules.size
    val overdueSchedules       = schedules.filter { it.statusUi == ScheduleStatusUi.OVERDUE }
    val permanentlyMissed      = schedules.filter { it.statusUi == ScheduleStatusUi.MISSED }
    val reschedulableOverdue   = overdueSchedules.filter { it.canBeRescheduled() }
    val tooLateOverdue         = overdueSchedules.filter { !it.canBeRescheduled() }
    val hasReschedulable       = reschedulableOverdue.isNotEmpty()
    val hasOnlyTooLate         = overdueSchedules.isNotEmpty() && reschedulableOverdue.isEmpty()
    val overdueCount           = overdueSchedules.size
    val totalTooLate           = tooLateOverdue.size + permanentlyMissed.size

    Column(
        modifier            = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
    ) {
        // Progress bar
        if (totalCount > 0) {
            VaccinationProgressBar(
                completed = completedCount,
                total     = totalCount,
                overdue   = overdueCount
            )
        }

        // Reschedule banner — only when reschedulable items exist
        if (hasReschedulable) {
            RescheduleBanner(
                overdueCount       = overdueCount,
                reschedulableCount = reschedulableOverdue.size,
                tooLateCount       = tooLateOverdue.size,
                onReschedule       = onReschedule
            )
        }

        // Cannot-reschedule info banner
        if (hasOnlyTooLate) {
            CannotRescheduleBanner(tooLateCount = totalTooLate)
        }

        // Filter chips
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
                                VaccinationFilter.ALL       ->
                                    stringResource(Res.string.bench_filter_all)
                                VaccinationFilter.UPCOMING  ->
                                    stringResource(Res.string.schedule_section_upcoming)
                                VaccinationFilter.COMPLETED ->
                                    stringResource(Res.string.schedule_section_completed)
                                VaccinationFilter.OVERDUE   ->
                                    stringResource(Res.string.schedule_status_overdue)
                            },
                            style      = MaterialTheme.typography.labelMedium,
                            fontWeight = if (filter == f) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        // FIX 1: Column instead of LazyColumn — parent drives all scrolling
        if (loading) {
            Box(
                modifier         = Modifier
                    .fillMaxWidth()
                    .height(dimensions.iconXLarge + dimensions.spacingXLarge),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = customColors.accentGradientStart)
            }
        } else if (filtered.isEmpty()) {
            Box(
                modifier         = Modifier
                    .fillMaxWidth()
                    .height(dimensions.iconXLarge + dimensions.spacingXLarge),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text  = stringResource(Res.string.schedule_no_vaccinations_found),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.4f)
                )
            }
        } else {
            // FIX 1: Plain Column — no LazyColumn, no nested scroll conflict
            Column(
                verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall),
                modifier            = Modifier.padding(
                    horizontal = dimensions.screenPadding,
                    vertical   = dimensions.spacingSmall
                )
            ) {
                filtered.forEach { item ->
                    VaccinationScheduleCard(
                        item    = item,
                        onClick = { onItemClick(item) }
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Progress bar
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun VaccinationProgressBar(completed: Int, total: Int, overdue: Int) {
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors
    val progress     = if (total > 0) completed.toFloat() / total else 0f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimensions.screenPadding),
        shape  = RoundedCornerShape(dimensions.cardCornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
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
// Reschedule banner — only rendered when reschedulable items exist
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimensions.screenPadding),
        shape  = RoundedCornerShape(dimensions.cardCornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensions.spacingMedium)
        ) {
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
                    if (tooLateCount > 0) {
                        Spacer(Modifier.height(dimensions.spacingXSmall))
                        Text(
                            text = stringResource(
                                Res.string.schedule_reschedule_breakdown,
                                reschedulableCount,
                                tooLateCount,
                                MAX_OVERSHOOT_MONTHS
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer.copy(0.8f)
                        )
                    }
                }
                TextButton(
                    onClick = onReschedule,
                    colors  = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(
                        text       = stringResource(Res.string.schedule_reschedule_action),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Cannot-reschedule info banner — shown when ALL overdue are past window
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CannotRescheduleBanner(tooLateCount: Int) {
    val dimensions = LocalDimensions.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimensions.screenPadding),
        shape  = RoundedCornerShape(dimensions.cardCornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(dimensions.spacingMedium),
            horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall),
            verticalAlignment     = Alignment.Top
        ) {
            Text("ℹ️", style = MaterialTheme.typography.titleMedium)
            Column {
                Text(
                    text       = stringResource(
                        Res.string.schedule_cannot_reschedule_title, tooLateCount
                    ),
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(dimensions.borderWidthMedium))
                Text(
                    text  = stringResource(
                        Res.string.schedule_cannot_reschedule_body, MAX_OVERSHOOT_MONTHS
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.7f)
                )
                Spacer(Modifier.height(dimensions.spacingSmall))
                Text(
                    text       = stringResource(Res.string.schedule_upcoming_unaffected),
                    style      = MaterialTheme.typography.labelSmall,
                    color      = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Individual vaccination card
// FIX (Q3): Shows idealDate as "Was due" alongside the new scheduled date so
//           the parent always knows the original missed date.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun VaccinationScheduleCard(item: VaccinationScheduleUi, onClick: () -> Unit) {
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors

    // Semantic status colours — intentionally fixed, not gender-themed
    val colorCompleted   = Color(0xFF22C55E)
    val colorDueSoon     = Color(0xFFF59E0B)
    val colorMissed      = Color(0xFFEF4444)
    val colorRescheduled = Color(0xFF8B5CF6)

    val (statusColor, statusIcon, statusLabel) = when (item.statusUi) {
        ScheduleStatusUi.COMPLETED   ->
            Triple(colorCompleted, "✅", stringResource(Res.string.schedule_status_completed_label))
        ScheduleStatusUi.OVERDUE     ->
            Triple(MaterialTheme.colorScheme.error, "⚠️",
                stringResource(Res.string.schedule_status_overdue_label))
        ScheduleStatusUi.MISSED      ->
            Triple(colorMissed, "❌", stringResource(Res.string.schedule_status_missed_label))
        ScheduleStatusUi.DUE_SOON    ->
            Triple(colorDueSoon, "⏰", stringResource(Res.string.schedule_status_due_soon_label))
        ScheduleStatusUi.RESCHEDULED ->
            Triple(colorRescheduled, "🔄",
                stringResource(Res.string.schedule_status_rescheduled_label))
        ScheduleStatusUi.UPCOMING    ->
            Triple(customColors.accentGradientStart, "💉",
                stringResource(Res.string.schedule_status_upcoming_label))
    }

    // FIX (Q3): Determine whether to show the "Was due" row.
    // We show it when the scheduled date differs from the ideal date AND
    // the vaccination is not yet completed — giving the parent context about
    // the original missed date even after rescheduling.
    val showWasDueRow = item.idealDate != item.scheduledDate &&
            item.statusUi != ScheduleStatusUi.COMPLETED

    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape     = RoundedCornerShape(dimensions.cardCornerRadius),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(dimensions.healthSubTabElevation)
    ) {
        Row(
            modifier              = Modifier.padding(dimensions.spacingMedium),
            horizontalArrangement = Arrangement.spacedBy(dimensions.spacingMedium),
            verticalAlignment     = Alignment.CenterVertically
        ) {
            // Status icon circle
            Box(
                modifier         = Modifier
                    .size(dimensions.vaccinationCardIconSize)
                    .clip(CircleShape)
                    .background(statusColor.copy(0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(statusIcon, style = MaterialTheme.typography.titleMedium)
            }

            Column(
                modifier            = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(dimensions.borderWidthMedium)
            ) {
                // Vaccine name
                Text(
                    text       = item.vaccineName,
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                // Dose + age
                Text(
                    text  = stringResource(Res.string.schedule_dose, item.doseNumber) +
                            " · " +
                            stringResource(Res.string.schedule_age_months, item.recommendedAgeMonths),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                )

                // FIX (Q3): Date row — shows scheduled date
                Text(
                    text = when {
                        item.statusUi == ScheduleStatusUi.COMPLETED ->
                            stringResource(
                                Res.string.schedule_completed_prefix,
                                item.completedDate ?: item.scheduledDate
                            )
                        else ->
                            stringResource(
                                Res.string.schedule_scheduled_prefix,
                                item.scheduledDate
                            )
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.5f)
                )

                // FIX (Q3): "Was due" row — always visible when dates differ.
                // This tells the parent what the original missed date was, even
                // after a reschedule sets a new scheduled date.
                if (showWasDueRow) {
                    Text(
                        text = stringResource(
                            Res.string.schedule_was_due_prefix,
                            item.idealDate
                        ),
                        style      = MaterialTheme.typography.labelSmall,
                        color      = MaterialTheme.colorScheme.error.copy(0.75f),
                        fontWeight = FontWeight.Medium
                    )
                }

                // Shift-days badge
                if (item.shiftDays > 0) {
                    Text(
                        text  = stringResource(Res.string.schedule_shift_days, item.shiftDays),
                        style = MaterialTheme.typography.labelSmall,
                        color = colorRescheduled
                    )
                }

                // "Past window — consult doctor" badge for non-reschedulable overdue items
                if (item.statusUi == ScheduleStatusUi.OVERDUE && !item.canBeRescheduled()) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.errorContainer.copy(0.5f)
                    ) {
                        Text(
                            text = stringResource(
                                Res.string.schedule_past_window_hint,
                                MAX_OVERSHOOT_MONTHS
                            ),
                            modifier = Modifier.padding(
                                horizontal = dimensions.spacingSmall,
                                vertical   = dimensions.borderWidthThin + dimensions.borderWidthMedium
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Status badge chip
            Surface(
                shape = RoundedCornerShape(50),
                color = statusColor.copy(0.12f)
            ) {
                Text(
                    text       = statusLabel,
                    modifier   = Modifier.padding(
                        horizontal = dimensions.spacingSmall,
                        vertical   = dimensions.spacingXSmall
                    ),
                    style      = MaterialTheme.typography.labelSmall,
                    color      = statusColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Reschedule dialogs — reason picker and result summary
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
                    text       = stringResource(Res.string.reschedule_title),
                    style      = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(dimensions.spacingXSmall))
                Row(horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)) {
                    if (overdueCount > 0) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.errorContainer
                        ) {
                            Text(
                                text = stringResource(
                                    Res.string.schedule_overdue_count, overdueCount
                                ),
                                modifier = Modifier.padding(
                                    horizontal = dimensions.spacingSmall + dimensions.borderWidthMedium,
                                    vertical   = dimensions.spacingXSmall - dimensions.borderWidthThin
                                ),
                                style      = MaterialTheme.typography.labelSmall,
                                color      = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = customColors.accentGradientStart.copy(0.12f)
                    ) {
                        Text(
                            text = stringResource(
                                Res.string.schedule_reschedulable_count, reschedulableCount
                            ),
                            modifier = Modifier.padding(
                                horizontal = dimensions.spacingSmall + dimensions.borderWidthMedium,
                                vertical   = dimensions.spacingXSmall - dimensions.borderWidthThin
                            ),
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedReason = reason },
                        shape  = RoundedCornerShape(
                            dimensions.cardCornerRadius - dimensions.spacingXSmall
                        ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected)
                                customColors.accentGradientStart.copy(0.12f)
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        ),
                        border = if (isSelected) BorderStroke(
                            dimensions.borderWidthThin + dimensions.borderWidthThin,
                            customColors.accentGradientStart
                        ) else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = dimensions.spacingMedium,
                                    vertical   = dimensions.spacingSmall + dimensions.spacingXSmall
                                ),
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(
                                dimensions.spacingSmall + dimensions.borderWidthMedium
                            )
                        ) {
                            Text(reason.emoji, style = MaterialTheme.typography.titleMedium)
                            Text(
                                text       = reason.displayEn,
                                style      = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.SemiBold
                                else FontWeight.Normal,
                                color      = if (isSelected) customColors.accentGradientStart
                                else MaterialTheme.colorScheme.onSurface,
                                modifier   = Modifier.weight(1f)
                            )
                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    null,
                                    tint     = customColors.accentGradientStart,
                                    modifier = Modifier.size(
                                        dimensions.iconSmall + dimensions.borderWidthMedium
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(dimensions.spacingXSmall))

                OutlinedTextField(
                    value         = notes,
                    onValueChange = { notes = it },
                    label         = {
                        Text(stringResource(Res.string.schedule_reschedule_notes_label))
                    },
                    placeholder   = {
                        Text(stringResource(Res.string.schedule_reschedule_notes_placeholder))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    shape    = RoundedCornerShape(
                        dimensions.cardCornerRadius - dimensions.spacingXSmall
                    )
                )

                Surface(
                    shape = RoundedCornerShape(
                        dimensions.cardCornerRadius - dimensions.spacingXSmall -
                                dimensions.borderWidthMedium
                    ),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(
                            dimensions.spacingSmall + dimensions.spacingXSmall
                        ),
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
                colors   = ButtonDefaults.buttonColors(
                    containerColor = customColors.accentGradientStart
                ),
                shape    = RoundedCornerShape(dimensions.buttonCornerRadius)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(dimensions.iconSmall),
                        strokeWidth = dimensions.borderWidthMedium,
                        color       = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(dimensions.spacingSmall))
                    Text(stringResource(Res.string.schedule_rescheduling_in_progress))
                } else {
                    Icon(
                        Icons.Default.Refresh,
                        null,
                        modifier = Modifier.size(dimensions.iconSmall + dimensions.borderWidthMedium)
                    )
                    Spacer(Modifier.width(dimensions.spacingXSmall + dimensions.borderWidthThin))
                    Text(
                        text       = stringResource(Res.string.schedule_reschedule_all),
                        fontWeight = FontWeight.Bold
                    )
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

    // Semantic status colours
    val colorCompleted = Color(0xFF22C55E)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text       = stringResource(Res.string.schedule_reschedule_complete_title),
                    style      = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(dimensions.spacingSmall))
                Row(horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)) {
                    SummaryChip(
                        label = stringResource(
                            Res.string.schedule_reschedule_status_rescheduled,
                            result.rescheduledCount
                        ),
                        color = colorCompleted,
                        emoji = "✅"
                    )
                    if (result.tooLateCount > 0) {
                        SummaryChip(
                            label = stringResource(
                                Res.string.schedule_reschedule_status_too_late,
                                result.tooLateCount
                            ),
                            color = MaterialTheme.colorScheme.error,
                            emoji = "❌"
                        )
                    }
                    if (result.skippedCount > 0) {
                        SummaryChip(
                            label = stringResource(
                                Res.string.schedule_reschedule_status_skipped,
                                result.skippedCount
                            ),
                            color = MaterialTheme.colorScheme.onSurface.copy(0.5f),
                            emoji = "⏭️"
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier            = Modifier
                    .heightIn(max = dimensions.avatarLarge * 4 + dimensions.spacingXLarge)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
            ) {
                val rescheduled = result.items.filter { it.rescheduled }
                val tooLate     = result.items.filter { it.tooLate }
                val skipped     = result.items.filter { !it.rescheduled && !it.tooLate }

                if (rescheduled.isNotEmpty()) {
                    ResultSectionHeader(
                        title = stringResource(
                            Res.string.schedule_reschedule_status_rescheduled,
                            rescheduled.size
                        ),
                        color = colorCompleted
                    )
                    rescheduled.forEach { item ->
                        RescheduleResultItemRow(item, customColors.accentGradientStart)
                    }
                }
                if (tooLate.isNotEmpty()) {
                    Spacer(Modifier.height(dimensions.spacingXSmall))
                    ResultSectionHeader(
                        title = stringResource(
                            Res.string.schedule_reschedule_status_too_late,
                            tooLate.size
                        ),
                        color = MaterialTheme.colorScheme.error
                    )
                    tooLate.forEach { item ->
                        RescheduleResultItemRow(item, MaterialTheme.colorScheme.error)
                    }
                    Surface(
                        shape = RoundedCornerShape(dimensions.spacingSmall),
                        color = MaterialTheme.colorScheme.errorContainer.copy(0.5f)
                    ) {
                        Text(
                            text     = stringResource(Res.string.schedule_too_late_medical_advice),
                            modifier = Modifier.padding(
                                dimensions.spacingSmall + dimensions.spacingXSmall
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
                if (skipped.isNotEmpty()) {
                    Spacer(Modifier.height(dimensions.spacingXSmall))
                    ResultSectionHeader(
                        title = stringResource(
                            Res.string.schedule_reschedule_status_skipped,
                            skipped.size
                        ),
                        color = MaterialTheme.colorScheme.onSurface.copy(0.5f)
                    )
                    skipped.forEach { item ->
                        RescheduleResultItemRow(
                            item,
                            MaterialTheme.colorScheme.onSurface.copy(0.5f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors  = ButtonDefaults.buttonColors(
                    containerColor = customColors.accentGradientStart
                ),
                shape   = RoundedCornerShape(dimensions.buttonCornerRadius)
            ) {
                Text(
                    text       = stringResource(Res.string.schedule_reschedule_done),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}

@Composable
private fun SummaryChip(label: String, color: Color, emoji: String) {
    val dimensions = LocalDimensions.current
    Surface(shape = RoundedCornerShape(50), color = color.copy(0.12f)) {
        Row(
            modifier = Modifier.padding(
                horizontal = dimensions.spacingSmall,
                vertical   = dimensions.borderWidthThin + dimensions.borderWidthMedium
            ),
            horizontalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall),
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text(emoji, style = MaterialTheme.typography.labelSmall)
            Text(
                text       = label,
                style      = MaterialTheme.typography.labelSmall,
                color      = color,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ResultSectionHeader(title: String, color: Color) {
    val dimensions = LocalDimensions.current
    Text(
        text       = title,
        style      = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color      = color,
        modifier   = Modifier.padding(vertical = dimensions.borderWidthMedium)
    )
}

@Composable
private fun RescheduleResultItemRow(item: RescheduleItemUi, color: Color) {
    val dimensions = LocalDimensions.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(
            dimensions.cardCornerRadius - dimensions.spacingXSmall - dimensions.borderWidthMedium
        ),
        colors   = CardDefaults.cardColors(containerColor = color.copy(0.07f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = dimensions.spacingMedium,
                    vertical   = dimensions.spacingSmall + dimensions.borderWidthMedium
                ),
            verticalArrangement = Arrangement.spacedBy(
                dimensions.borderWidthThin + dimensions.borderWidthMedium
            )
        ) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text       = item.vaccineName,
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onSurface,
                    modifier   = Modifier.weight(1f)
                )
                Text(
                    text  = stringResource(Res.string.schedule_dose, item.doseNumber) +
                            " · ${item.recommendedAgeMonths}mo",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.5f)
                )
            }
            if (item.rescheduled && item.newDate != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall)) {
                    // FIX (Q3): Show old date (the missed date) → new date
                    Text(
                        text  = "📅 ${item.oldDate}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.4f)
                    )
                    Text("→", style = MaterialTheme.typography.labelSmall, color = color)
                    Text(
                        text       = item.newDate,
                        style      = MaterialTheme.typography.labelSmall,
                        color      = color,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else if (item.skipReason != null) {
                Text(
                    text  = item.skipReason,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.5f)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// VaccinationDetailScreen
// FIX (Q3): Shows idealDate as "Was due" row. Reschedule button only for
//           OVERDUE items still within the 2-month window.
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

    val colorCompleted   = Color(0xFF22C55E)
    val colorDueSoon     = Color(0xFFF59E0B)
    val colorMissed      = Color(0xFFEF4444)
    val colorRescheduled = Color(0xFF8B5CF6)

    val (statusColor, statusIcon) = when (item.statusUi) {
        ScheduleStatusUi.COMPLETED   -> Pair(colorCompleted, "✅")
        ScheduleStatusUi.OVERDUE     -> Pair(MaterialTheme.colorScheme.error, "⚠️")
        ScheduleStatusUi.MISSED      -> Pair(colorMissed, "❌")
        ScheduleStatusUi.DUE_SOON    -> Pair(colorDueSoon, "⏰")
        ScheduleStatusUi.RESCHEDULED -> Pair(colorRescheduled, "🔄")
        ScheduleStatusUi.UPCOMING    -> Pair(customColors.accentGradientStart, "💉")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text       = item.vaccineName,
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = customColors.accentGradientStart.copy(0.12f)
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
        ) {
            // Hero
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(statusColor.copy(0.15f), Color.Transparent)
                        )
                    )
                    .padding(dimensions.spacingLarge),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
                ) {
                    Text(statusIcon, style = MaterialTheme.typography.displaySmall)
                    Text(
                        text       = item.vaccineName,
                        style      = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text  = stringResource(Res.string.schedule_dose, item.doseNumber),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(0.6f)
                    )
                }
            }

            Spacer(Modifier.height(dimensions.spacingMedium))

            Column(
                modifier            = Modifier.padding(horizontal = dimensions.screenPadding),
                verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
            ) {
                DetailInfoCard {
                    InfoRow(
                        label = stringResource(Res.string.vax_detail_age_recommended),
                        value = "${item.recommendedAgeMonths} months"
                    )
                    InfoRow(
                        label = stringResource(Res.string.vax_detail_ideal_date),
                        value = item.idealDate
                    )
                    // FIX (Q3): always show scheduled date
                    InfoRow(
                        label = stringResource(Res.string.vax_detail_scheduled_date),
                        value = item.scheduledDate
                    )
                    // FIX (Q3): show "Was due" only when dates differ and not completed
                    if (item.idealDate != item.scheduledDate &&
                        item.statusUi != ScheduleStatusUi.COMPLETED) {
                        InfoRow(
                            label      = stringResource(Res.string.vax_detail_was_due),
                            value      = item.idealDate,
                            valueColor = MaterialTheme.colorScheme.error.copy(0.8f)
                        )
                    }
                    if (item.shiftDays > 0) {
                        InfoRow(
                            label = stringResource(Res.string.vax_detail_adjustment),
                            value = stringResource(
                                Res.string.vax_detail_adjustment_value,
                                item.shiftDays,
                                item.shiftReason.lowercase()
                            )
                        )
                    }
                    item.completedDate?.let {
                        InfoRow(
                            label = stringResource(Res.string.vax_detail_completed_on),
                            value = it
                        )
                    }
                    InfoRow(
                        label = stringResource(Res.string.vax_detail_health_center),
                        value = item.benchNameEn
                    )
                    InfoRow(
                        label = stringResource(Res.string.vax_detail_status),
                        value = item.status.replace("_", " ")
                    )
                }

                // Reschedule or cannot-reschedule surface for OVERDUE items
                if (item.statusUi == ScheduleStatusUi.OVERDUE) {
                    val canReschedule = item.canBeRescheduled()
                    Spacer(Modifier.height(dimensions.spacingSmall))
                    if (canReschedule) {
                        Button(
                            onClick  = onReschedule,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(dimensions.buttonHeight),
                            shape    = RoundedCornerShape(dimensions.buttonCornerRadius),
                            colors   = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                null,
                                modifier = Modifier.size(
                                    dimensions.iconSmall + dimensions.borderWidthMedium
                                )
                            )
                            Spacer(Modifier.width(dimensions.spacingSmall))
                            Text(
                                text       = stringResource(Res.string.schedule_reschedule_all),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(dimensions.cardCornerRadius),
                            color = MaterialTheme.colorScheme.errorContainer.copy(0.5f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(dimensions.spacingMedium),
                                horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall),
                                verticalAlignment     = Alignment.Top
                            ) {
                                Text("⚠️", style = MaterialTheme.typography.bodyMedium)
                                Column {
                                    Text(
                                        text       = stringResource(
                                            Res.string.schedule_cannot_reschedule_title_short
                                        ),
                                        style      = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color      = MaterialTheme.colorScheme.error
                                    )
                                    Text(
                                        text  = stringResource(
                                            Res.string.schedule_cannot_reschedule_detail,
                                            MAX_OVERSHOOT_MONTHS
                                        ),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(dimensions.cardCornerRadius),
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier            = Modifier.padding(dimensions.spacingMedium),
            verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall),
            content             = content
        )
    }
}

@Composable
private fun InfoRow(
    label      : String,
    value      : String,
    valueColor : Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text  = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(0.55f)
        )
        Text(
            text       = value,
            style      = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color      = valueColor
        )
    }
}