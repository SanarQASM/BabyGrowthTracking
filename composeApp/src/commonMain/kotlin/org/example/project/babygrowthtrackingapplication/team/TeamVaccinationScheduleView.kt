package org.example.project.babygrowthtrackingapplication.team

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Forward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import babygrowthtrackingapplication.composeapp.generated.resources.Res
import babygrowthtrackingapplication.composeapp.generated.resources.*
import org.example.project.babygrowthtrackingapplication.data.network.VaccinationScheduleUi
import org.example.project.babygrowthtrackingapplication.theme.CustomColors
import org.example.project.babygrowthtrackingapplication.theme.Dimensions
import org.example.project.babygrowthtrackingapplication.theme.LocalDimensions
import org.example.project.babygrowthtrackingapplication.theme.customColors
import org.jetbrains.compose.resources.stringResource

// ─────────────────────────────────────────────────────────────────────────────
// TeamVaccinationScheduleView
//
// Rules (team-specific):
//   • MISSED    → locked card, no action buttons
//   • COMPLETED → locked card, no action buttons
//   • All other statuses → shows Complete + Skip buttons
//                          (no "Mark Missed" button for team members)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun TeamVaccinationScheduleView(
    schedules     : List<VaccinationScheduleUi>,
    filter        : TeamVaccinationFilter,
    loading       : Boolean,
    onFilterChange: (TeamVaccinationFilter) -> Unit,
    onComplete    : (String) -> Unit,
    onSkip        : (String) -> Unit,
    onItemClick   : (VaccinationScheduleUi) -> Unit,
    modifier      : Modifier = Modifier
) {
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors

    Column(modifier = modifier.fillMaxWidth()) {

        // ── Filter chips ──────────────────────────────────────────────────────
        VacFilterRow(
            selected       = filter,
            onSelect       = onFilterChange,
            customColors   = customColors,
            dimensions     = dimensions
        )

        // ── Content ───────────────────────────────────────────────────────────
        if (loading) {
            Box(
                modifier         = Modifier.fillMaxWidth().padding(vertical = dimensions.spacingXLarge),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = customColors.accentGradientStart)
            }
        } else {
            val displayed = when (filter) {
                TeamVaccinationFilter.ALL      -> schedules
                TeamVaccinationFilter.UPCOMING -> schedules.filter {
                    it.status == "UPCOMING" || it.status == "DUE_SOON"
                }
                TeamVaccinationFilter.COMPLETED -> schedules.filter { it.status == "COMPLETED" }
                TeamVaccinationFilter.OVERDUE   -> schedules.filter { it.status == "OVERDUE" || it.status == "MISSED" }
            }

            if (displayed.isEmpty()) {
                Box(
                    modifier         = Modifier.fillMaxWidth().padding(vertical = dimensions.spacingXLarge),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text  = stringResource(Res.string.team_empty_search_title),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            } else {
                Column(
                    modifier            = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
                ) {
                    displayed.forEach { schedule ->
                        VaccinationScheduleCard(
                            schedule     = schedule,
                            onComplete   = { onComplete(schedule.scheduleId) },
                            onSkip       = { onSkip(schedule.scheduleId) },
                            onClick      = { onItemClick(schedule) },
                            customColors = customColors,
                            dimensions   = dimensions
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Filter chip row
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun VacFilterRow(
    selected    : TeamVaccinationFilter,
    onSelect    : (TeamVaccinationFilter) -> Unit,
    customColors: CustomColors,
    dimensions  : Dimensions
) {
    data class FilterChipData(val filter: TeamVaccinationFilter, val emoji: String, val label: String)

    val chips = listOf(
        FilterChipData(TeamVaccinationFilter.ALL,       stringResource(Res.string.team_emoji_vaccine),   stringResource(Res.string.team_filter_all)),
        FilterChipData(TeamVaccinationFilter.UPCOMING,  stringResource(Res.string.team_emoji_due_soon),  stringResource(Res.string.team_filter_upcoming)),
        FilterChipData(TeamVaccinationFilter.COMPLETED, stringResource(Res.string.team_emoji_completed), stringResource(Res.string.team_filter_completed)),
        FilterChipData(TeamVaccinationFilter.OVERDUE,   stringResource(Res.string.team_emoji_overdue),   stringResource(Res.string.team_filter_overdue))
    )

    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimensions.screenPadding, vertical = dimensions.spacingSmall),
        horizontalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall)
    ) {
        chips.forEach { chip ->
            val isSelected = selected == chip.filter
            val color      = customColors.accentGradientStart

            Surface(
                shape    = RoundedCornerShape(dimensions.filterTabCorner),
                color    = if (isSelected) color.copy(alpha = 0.15f)
                else MaterialTheme.colorScheme.surface,
                border   = BorderStroke(
                    dimensions.borderWidthThin,
                    if (isSelected) color.copy(alpha = 0.5f)
                    else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                ),
                modifier = Modifier.weight(1f),
                onClick  = { onSelect(chip.filter) }
            ) {
                Column(
                    modifier            = Modifier.padding(
                        horizontal = dimensions.spacingXSmall,
                        vertical   = dimensions.spacingXSmall
                    ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(chip.emoji, style = MaterialTheme.typography.labelSmall)
                    Text(
                        text       = chip.label,
                        style      = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color      = if (isSelected) color
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                        maxLines   = 1,
                        overflow   = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Single schedule card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun VaccinationScheduleCard(
    schedule    : VaccinationScheduleUi,
    onComplete  : () -> Unit,
    onSkip      : () -> Unit,
    onClick     : () -> Unit,
    customColors: CustomColors,
    dimensions  : Dimensions
) {
    // MISSED and COMPLETED are locked — no action buttons shown for team
    val isLocked = schedule.status == "MISSED" || schedule.status == "COMPLETED"

    val statusColor = when (schedule.status) {
        "COMPLETED" -> customColors.success
        "MISSED"    -> MaterialTheme.colorScheme.error
        "OVERDUE"   -> customColors.warning
        "DUE_SOON"  -> customColors.warning
        "SKIPPED"   -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
        else        -> customColors.accentGradientStart   // UPCOMING / SCHEDULED
    }
    val statusLabel = when (schedule.status) {
        "COMPLETED" -> stringResource(Res.string.team_badge_up_to_date)
        "MISSED"    -> stringResource(Res.string.team_badge_overdue)
        "OVERDUE"   -> stringResource(Res.string.team_status_overdue_label)
        "DUE_SOON"  -> stringResource(Res.string.team_badge_due_soon)
        "SKIPPED"   -> stringResource(Res.string.team_status_scheduled_label)
        else        -> stringResource(Res.string.team_status_scheduled_label)
    }

    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimensions.screenPadding),
        shape     = RoundedCornerShape(dimensions.cardCornerRadius),
        colors    = CardDefaults.cardColors(
            containerColor = if (isLocked)
                MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isLocked) 0.dp else dimensions.cardElevationSmall
        ),
        onClick   = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensions.spacingMedium)
        ) {
            // ── Top row: vaccine name + status badge ──────────────────────────
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text  = stringResource(Res.string.team_emoji_vaccine),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.width(dimensions.spacingXSmall))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text       = schedule.vaccineName,
                        style      = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color      = if (isLocked)
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        else
                            MaterialTheme.colorScheme.onSurface,
                        maxLines   = 1,
                        overflow   = TextOverflow.Ellipsis
                    )
                    Text(
                        text  = stringResource(Res.string.team_dose_label, schedule.doseNumber),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                    )
                }
                // Status badge
                Surface(
                    shape = RoundedCornerShape(dimensions.filterTabCorner),
                    color = statusColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text       = statusLabel,
                        style      = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color      = statusColor,
                        modifier   = Modifier.padding(
                            horizontal = dimensions.spacingXSmall * 2,
                            vertical   = dimensions.spacingXSmall / 2
                        )
                    )
                }
            }

            // ── Scheduled date row ────────────────────────────────────────────
            Spacer(Modifier.height(dimensions.spacingXSmall))
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall / 2)
            ) {
                Text(
                    text  = stringResource(Res.string.team_emoji_birthday),
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    text  = stringResource(Res.string.team_next_vaccine_date, schedule.scheduledDate),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            // ── Action buttons — only for non-locked statuses ─────────────────
            if (!isLocked) {
                Spacer(Modifier.height(dimensions.spacingSmall))
                HorizontalDivider(
                    color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                    thickness = dimensions.hairlineDividerThickness
                )
                Spacer(Modifier.height(dimensions.spacingSmall))
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
                ) {
                    // Complete button
                    Button(
                        onClick        = onComplete,
                        modifier       = Modifier.weight(1f),
                        colors         = ButtonDefaults.buttonColors(containerColor = customColors.success),
                        shape          = RoundedCornerShape(dimensions.buttonCornerRadius),
                        contentPadding = PaddingValues(vertical = dimensions.spacingXSmall)
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(dimensions.iconSmall)
                        )
                        Spacer(Modifier.width(dimensions.spacingXSmall))
                        Text(
                            text  = stringResource(Res.string.team_action_complete),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    // Skip button (team does NOT get a "Mark Missed" button)
                    OutlinedButton(
                        onClick        = onSkip,
                        modifier       = Modifier.weight(1f),
                        colors         = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        ),
                        border         = BorderStroke(
                            dimensions.borderWidthThin,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                        ),
                        shape          = RoundedCornerShape(dimensions.buttonCornerRadius),
                        contentPadding = PaddingValues(vertical = dimensions.spacingXSmall)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Forward,
                            contentDescription = null,
                            modifier = Modifier.size(dimensions.iconSmall)
                        )
                        Spacer(Modifier.width(dimensions.spacingXSmall))
                        Text(
                            text  = stringResource(Res.string.team_action_skip),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}