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
    schedules: List<VaccinationScheduleUi>,
    filter: VaccinationFilter,
    loading: Boolean,
    onFilterChange: (VaccinationFilter) -> Unit,
    onItemClick: (VaccinationScheduleUi) -> Unit,
    onReschedule: () -> Unit
) {
    val dimensions = LocalDimensions.current
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

    val overdueCount = schedules.count {
        it.statusUi == ScheduleStatusUi.OVERDUE || it.statusUi == ScheduleStatusUi.MISSED
    }
    val completedCount = schedules.count { it.statusUi == ScheduleStatusUi.COMPLETED }
    val totalCount = schedules.size

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
    ) {
        // Progress bar
        if (totalCount > 0) {
            VaccinationProgressBar(
                completed = completedCount,
                total = totalCount,
                overdue = overdueCount
            )
        }

        // Reschedule banner
        if (overdueCount > 0) {
            RescheduleBanner(overdueCount = overdueCount, onReschedule = onReschedule)
        }

        // Filter tabs
        ScrollableTabRow(
            selectedTabIndex = filter.ordinal,
            edgePadding = dimensions.screenPadding,
            containerColor = Color.Transparent,
            contentColor = customColors.accentGradientStart,
            divider = {}
        ) {
            VaccinationFilter.entries.forEach { f ->
                Tab(
                    selected = filter == f,
                    onClick = { onFilterChange(f) },
                    text = {
                        Text(
                            text = when (f) {
                                VaccinationFilter.ALL       -> stringResource(Res.string.bench_filter_all)
                                VaccinationFilter.UPCOMING  -> stringResource(Res.string.schedule_section_upcoming)
                                VaccinationFilter.COMPLETED -> stringResource(Res.string.schedule_section_completed)
                                VaccinationFilter.OVERDUE   -> stringResource(Res.string.schedule_section_overdue)
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
        } else if (filtered.isEmpty()) {
            Box(Modifier.fillMaxWidth().height(120.dp), Alignment.Center) {
                Text(
                    text = "No vaccinations found",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.4f)
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall),
                contentPadding = PaddingValues(
                    horizontal = dimensions.screenPadding,
                    vertical = dimensions.spacingSmall
                )
            ) {
                items(filtered, key = { it.scheduleId }) { item ->
                    VaccinationScheduleCard(
                        item = item,
                        onClick = { onItemClick(item) }
                    )
                }
            }
        }
    }
}

@Composable
private fun VaccinationProgressBar(completed: Int, total: Int, overdue: Int) {
    val dimensions = LocalDimensions.current
    val customColors = MaterialTheme.customColors
    val progress = if (total > 0) completed.toFloat() / total else 0f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimensions.screenPadding),
        shape = RoundedCornerShape(dimensions.cardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(dimensions.spacingMedium),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(Res.string.schedule_progress),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "$completed / $total",
                    style = MaterialTheme.typography.labelMedium,
                    color = customColors.accentGradientStart,
                    fontWeight = FontWeight.Bold
                )
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(50)).height(8.dp),
                color = customColors.accentGradientStart,
                trackColor = customColors.accentGradientStart.copy(0.15f)
            )
            if (overdue > 0) {
                Text(
                    text = "⚠️ $overdue overdue",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun RescheduleBanner(overdueCount: Int, onReschedule: () -> Unit) {
    val dimensions = LocalDimensions.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimensions.screenPadding),
        shape = RoundedCornerShape(dimensions.cardCornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensions.spacingMedium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "⚠️ $overdueCount vaccination${if (overdueCount > 1) "s" else ""} overdue",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = "Tap to reschedule remaining vaccinations",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(0.7f)
                )
            }
            TextButton(
                onClick = onReschedule,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Reschedule", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun VaccinationScheduleCard(
    item: VaccinationScheduleUi,
    onClick: () -> Unit
) {
    val dimensions = LocalDimensions.current
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
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(dimensions.cardCornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(dimensions.spacingMedium),
            horizontalArrangement = Arrangement.spacedBy(dimensions.spacingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status dot
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(statusColor.copy(0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(statusIcon, style = MaterialTheme.typography.titleMedium)
            }

            // Info
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = item.vaccineName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(Res.string.schedule_dose, item.doseNumber) +
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
                        text = stringResource(Res.string.schedule_shift_days, item.shiftDays),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF8B5CF6)
                    )
                }
            }

            // Status badge
            Surface(
                shape = RoundedCornerShape(50),
                color = statusColor.copy(0.12f)
            ) {
                Text(
                    text = statusLabel,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = statusColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Vaccination Detail Screen (View All)
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaccinationDetailScreen(
    item: VaccinationScheduleUi,
    onBack: () -> Unit,
    onReschedule: () -> Unit
) {
    val dimensions = LocalDimensions.current
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
                        style = MaterialTheme.typography.titleMedium,
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
                        item.vaccineName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(Res.string.schedule_dose, item.doseNumber),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(0.6f)
                    )
                }
            }

            Spacer(Modifier.height(dimensions.spacingMedium))

            Column(
                modifier = Modifier.padding(horizontal = dimensions.screenPadding),
                verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
            ) {
                DetailInfoCard {
                    InfoRow("Age recommended", "${item.recommendedAgeMonths} months")
                    InfoRow("Ideal date", item.idealDate)
                    InfoRow("Scheduled date", item.scheduledDate)
                    if (item.shiftDays > 0) {
                        InfoRow("Adjustment", "+${item.shiftDays} days (${item.shiftReason.lowercase()})")
                    }
                    item.completedDate?.let { InfoRow("Completed on", it) }
                    InfoRow("Health Center", item.benchNameEn)
                    InfoRow("Status", item.status.replace("_", " "))
                }

                // Reschedule button if overdue/missed
                if (item.statusUi == ScheduleStatusUi.OVERDUE ||
                    item.statusUi == ScheduleStatusUi.MISSED) {
                    Spacer(Modifier.height(dimensions.spacingSmall))
                    Button(
                        onClick = onReschedule,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(dimensions.buttonHeight),
                        shape = RoundedCornerShape(dimensions.buttonCornerRadius),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Reschedule Remaining", fontWeight = FontWeight.Bold)
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
        shape = RoundedCornerShape(dimensions.cardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(dimensions.spacingMedium),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) { content() }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(0.55f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}