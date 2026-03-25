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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import babygrowthtrackingapplication.composeapp.generated.resources.*
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model.*
import org.example.project.babygrowthtrackingapplication.theme.LocalDimensions
import org.example.project.babygrowthtrackingapplication.theme.customColors
import org.jetbrains.compose.resources.stringResource

// ─────────────────────────────────────────────────────────────────────────────
// Vaccination list view (embedded in HealthRecordTabContent sub-tab)
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
                it.statusUi == ScheduleStatusUi.UPCOMING || it.statusUi == ScheduleStatusUi.DUE_SOON
            }
            VaccinationFilter.COMPLETED -> schedules.filter { it.statusUi == ScheduleStatusUi.COMPLETED }
            VaccinationFilter.OVERDUE   -> schedules.filter {
                it.statusUi == ScheduleStatusUi.OVERDUE || it.statusUi == ScheduleStatusUi.MISSED
            }
        }
    }

    val overdueCount  = schedules.count {
        it.statusUi == ScheduleStatusUi.OVERDUE || it.statusUi == ScheduleStatusUi.MISSED
    }
    val completedCount = schedules.count { it.statusUi == ScheduleStatusUi.COMPLETED }
    val totalCount     = schedules.size

    Column(
        modifier = Modifier.fillMaxSize(),
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

        // Reschedule banner — visible whenever there are overdue OR upcoming vaccinations
        val reschedulableCount = schedules.count {
            it.statusUi != ScheduleStatusUi.COMPLETED && it.statusUi != ScheduleStatusUi.MISSED
        }
        if (reschedulableCount > 0) {
            RescheduleBanner(
                overdueCount      = overdueCount,
                reschedulableCount = reschedulableCount,
                onReschedule      = onReschedule
            )
        }

        // Filter tabs
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
                                VaccinationFilter.OVERDUE   -> stringResource(Res.string.schedule_section_overdue)
                            },
                            style      = MaterialTheme.typography.labelMedium,
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
        } else if (filtered.isEmpty()) {
            Box(Modifier.fillMaxWidth().height(120.dp), Alignment.Center) {
                Text(
                    text  = "No vaccinations found",
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier            = Modifier.padding(dimensions.spacingMedium),
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
                    .height(8.dp),
                color      = customColors.accentGradientStart,
                trackColor = customColors.accentGradientStart.copy(0.15f)
            )
            if (overdue > 0) {
                Text(
                    text  = "⚠️ $overdue overdue",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Reschedule banner  (shows for both overdue AND upcoming reschedulable items)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun RescheduleBanner(
    overdueCount      : Int,
    reschedulableCount: Int,
    onReschedule      : () -> Unit
) {
    val dimensions = LocalDimensions.current
    val isOverdue  = overdueCount > 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimensions.screenPadding),
        shape  = RoundedCornerShape(dimensions.cardCornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = if (isOverdue)
                MaterialTheme.colorScheme.errorContainer
            else
                MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(dimensions.spacingMedium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isOverdue)
                        "⚠️ $overdueCount vaccination${if (overdueCount > 1) "s" else ""} overdue"
                    else
                        "💉 $reschedulableCount vaccination${if (reschedulableCount > 1) "s" else ""} to reschedule",
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color      = if (isOverdue)
                        MaterialTheme.colorScheme.onErrorContainer
                    else
                        MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text  = "Tap to reschedule all pending vaccinations",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isOverdue)
                        MaterialTheme.colorScheme.onErrorContainer.copy(0.7f)
                    else
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(0.7f)
                )
            }
            TextButton(
                onClick = onReschedule,
                colors  = ButtonDefaults.textButtonColors(
                    contentColor = if (isOverdue)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Reschedule", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Individual vaccination card  (unchanged logic, same look)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun VaccinationScheduleCard(item: VaccinationScheduleUi, onClick: () -> Unit) {
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors

    val (statusColor, statusIcon, statusLabel) = when (item.statusUi) {
        ScheduleStatusUi.COMPLETED   -> Triple(Color(0xFF22C55E), "✅", "Completed")
        ScheduleStatusUi.OVERDUE     -> Triple(MaterialTheme.colorScheme.error, "⚠️", "Overdue")
        ScheduleStatusUi.MISSED      -> Triple(Color(0xFFEF4444), "❌", "Missed")
        ScheduleStatusUi.DUE_SOON    -> Triple(Color(0xFFF59E0B), "⏰", "Due Soon")
        ScheduleStatusUi.RESCHEDULED -> Triple(Color(0xFF8B5CF6), "🔄", "Rescheduled")
        ScheduleStatusUi.UPCOMING    -> Triple(customColors.accentGradientStart, "💉", "Upcoming")
    }

    Card(
        modifier  = Modifier.fillMaxWidth().clickable { onClick() },
        shape     = RoundedCornerShape(dimensions.cardCornerRadius),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier              = Modifier.padding(dimensions.spacingMedium),
            horizontalArrangement = Arrangement.spacedBy(dimensions.spacingMedium),
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Box(
                modifier         = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(statusColor.copy(0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(statusIcon, style = MaterialTheme.typography.titleMedium)
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text       = item.vaccineName,
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text  = stringResource(Res.string.schedule_dose, item.doseNumber) +
                            " · ${stringResource(Res.string.schedule_age_months, item.recommendedAgeMonths)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                )
                Text(
                    text = when {
                        item.statusUi == ScheduleStatusUi.COMPLETED ->
                            "Completed: ${item.completedDate ?: item.scheduledDate}"
                        else -> "Scheduled: ${item.scheduledDate}"
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
            }

            Surface(shape = RoundedCornerShape(50), color = statusColor.copy(0.12f)) {
                Text(
                    text      = statusLabel,
                    modifier  = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style     = MaterialTheme.typography.labelSmall,
                    color     = statusColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// RESCHEDULE REASON PICKER DIALOG
// Step 1 of 2 — user selects a reason for rescheduling
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun RescheduleReasonPickerDialog(
    overdueCount      : Int,
    reschedulableCount: Int,
    isLoading         : Boolean,
    onConfirm         : (reason: RescheduleReason, notes: String) -> Unit,
    onDismiss         : () -> Unit
) {
    val dimensions         = LocalDimensions.current
    var selectedReason     by remember { mutableStateOf<RescheduleReason?>(null) }
    var notes              by remember { mutableStateOf("") }
    val customColors       = MaterialTheme.customColors

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = {
            Column {
                Text(
                    "Reschedule Vaccinations",
                    style      = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                // Summary chips
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (overdueCount > 0) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.errorContainer
                        ) {
                            Text(
                                "⚠️ $overdueCount overdue",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                                style    = MaterialTheme.typography.labelSmall,
                                color    = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = customColors.accentGradientStart.copy(0.12f)
                    ) {
                        Text(
                            "💉 $reschedulableCount to reschedule",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                            style    = MaterialTheme.typography.labelSmall,
                            color    = customColors.accentGradientStart,
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
                    text  = "Why are you rescheduling?",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Reason options
                RescheduleReason.entries.forEach { reason ->
                    val isSelected = selectedReason == reason
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedReason = reason },
                        shape  = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected)
                                customColors.accentGradientStart.copy(0.12f)
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        ),
                        border = if (isSelected)
                            BorderStroke(1.5.dp, customColors.accentGradientStart)
                        else null
                    ) {
                        Row(
                            modifier          = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(reason.emoji, style = MaterialTheme.typography.titleMedium)
                            Text(
                                text       = reason.displayEn,
                                style      = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color      = if (isSelected)
                                    customColors.accentGradientStart
                                else
                                    MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint     = customColors.accentGradientStart,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))

                // Optional notes
                OutlinedTextField(
                    value         = notes,
                    onValueChange = { notes = it },
                    label         = { Text("Notes (optional)") },
                    placeholder   = { Text("e.g. baby was sick...") },
                    modifier      = Modifier.fillMaxWidth(),
                    maxLines      = 3,
                    shape         = RoundedCornerShape(12.dp)
                )

                // Info box
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("ℹ️", style = MaterialTheme.typography.bodySmall)
                        Text(
                            text  = "Overdue vaccinations that are still within the safe window " +
                                    "will be rescheduled. Those that are too late will be marked as Missed.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick  = {
                    selectedReason?.let { reason -> onConfirm(reason, notes) }
                },
                enabled  = selectedReason != null && !isLoading,
                colors   = ButtonDefaults.buttonColors(
                    containerColor = customColors.accentGradientStart
                ),
                shape = RoundedCornerShape(dimensions.buttonCornerRadius)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color       = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Rescheduling...")
                } else {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Reschedule All", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = { if (!isLoading) onDismiss() }) {
                Text("Cancel")
            }
        }
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// RESCHEDULE RESULT SUMMARY DIALOG
// Step 2 of 2 — show per-vaccine outcome after the API responds
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun RescheduleResultDialog(
    result   : RescheduleResultUi,
    onDismiss: () -> Unit
) {
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    "Reschedule Complete",
                    style      = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                // Summary row
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SummaryChip(
                        label = "${result.rescheduledCount} rescheduled",
                        color = Color(0xFF22C55E),
                        emoji = "✅"
                    )
                    if (result.tooLateCount > 0) {
                        SummaryChip(
                            label = "${result.tooLateCount} too late",
                            color = MaterialTheme.colorScheme.error,
                            emoji = "❌"
                        )
                    }
                    if (result.skippedCount > 0) {
                        SummaryChip(
                            label = "${result.skippedCount} skipped",
                            color = MaterialTheme.colorScheme.onSurface.copy(0.5f),
                            emoji = "⏭️"
                        )
                    }
                }
            }
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall),
                modifier            = Modifier.heightIn(max = 380.dp)
            ) {
                // Rescheduled items first
                val rescheduled = result.items.filter { it.rescheduled }
                val tooLate     = result.items.filter { it.tooLate }
                val skipped     = result.items.filter { !it.rescheduled && !it.tooLate }

                if (rescheduled.isNotEmpty()) {
                    item {
                        ResultSectionHeader("✅ Rescheduled (${rescheduled.size})", Color(0xFF22C55E))
                    }
                    items(rescheduled) { item ->
                        RescheduleResultItemRow(item, customColors.accentGradientStart)
                    }
                }

                if (tooLate.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(4.dp))
                        ResultSectionHeader(
                            "❌ Too Late — Marked as Missed (${tooLate.size})",
                            MaterialTheme.colorScheme.error
                        )
                    }
                    items(tooLate) { item ->
                        RescheduleResultItemRow(item, MaterialTheme.colorScheme.error)
                    }
                    item {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(0.5f)
                        ) {
                            Text(
                                text    = "These vaccinations are more than 2 months overdue. " +
                                        "Please consult a doctor about whether they can still be given.",
                                modifier = Modifier.padding(10.dp),
                                style   = MaterialTheme.typography.bodySmall,
                                color   = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                if (skipped.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(4.dp))
                        ResultSectionHeader(
                            "⏭️ Skipped (${skipped.size})",
                            MaterialTheme.colorScheme.onSurface.copy(0.5f)
                        )
                    }
                    items(skipped) { item ->
                        RescheduleResultItemRow(
                            item  = item,
                            color = MaterialTheme.colorScheme.onSurface.copy(0.5f)
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
                shape = RoundedCornerShape(dimensions.buttonCornerRadius)
            ) {
                Text("Done", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun SummaryChip(label: String, color: Color, emoji: String) {
    Surface(shape = RoundedCornerShape(50), color = color.copy(0.12f)) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text(emoji, style = MaterialTheme.typography.labelSmall)
            Text(
                label,
                style      = MaterialTheme.typography.labelSmall,
                color      = color,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ResultSectionHeader(title: String, color: Color) {
    Text(
        text       = title,
        style      = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color      = color,
        modifier   = Modifier.padding(vertical = 2.dp)
    )
}

@Composable
private fun RescheduleResultItemRow(item: RescheduleItemUi, color: Color) {
    val dimensions = LocalDimensions.current
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(10.dp),
        colors    = CardDefaults.cardColors(
            containerColor = color.copy(0.07f)
        )
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
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
                    text  = "Dose ${item.doseNumber} · ${item.recommendedAgeMonths}mo",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.5f)
                )
            }
            if (item.rescheduled && item.newDate != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "📅 ${item.oldDate}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.4f)
                    )
                    Text(
                        "→",
                        style = MaterialTheme.typography.labelSmall,
                        color = color
                    )
                    Text(
                        item.newDate,
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
// Vaccination Detail Screen (View All) — unchanged
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
                title = {
                    Text(
                        item.vaccineName,
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
                        item.vaccineName,
                        style      = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        stringResource(Res.string.schedule_dose, item.doseNumber),
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
                    InfoRow("Age recommended", "${item.recommendedAgeMonths} months")
                    InfoRow("Ideal date",      item.idealDate)
                    InfoRow("Scheduled date",  item.scheduledDate)
                    if (item.shiftDays > 0) {
                        InfoRow("Adjustment", "+${item.shiftDays} days (${item.shiftReason.lowercase()})")
                    }
                    item.completedDate?.let { InfoRow("Completed on", it) }
                    InfoRow("Health Center", item.benchNameEn)
                    InfoRow("Status", item.status.replace("_", " "))
                }

                if (item.statusUi == ScheduleStatusUi.OVERDUE ||
                    item.statusUi == ScheduleStatusUi.MISSED) {
                    Spacer(Modifier.height(dimensions.spacingSmall))
                    Button(
                        onClick  = onReschedule,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(dimensions.buttonHeight),
                        shape  = RoundedCornerShape(dimensions.buttonCornerRadius),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Reschedule All", fontWeight = FontWeight.Bold)
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) { content() }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
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
            fontWeight = FontWeight.SemiBold
        )
    }
}