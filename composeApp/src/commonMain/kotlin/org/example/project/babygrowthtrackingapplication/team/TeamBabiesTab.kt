// composeApp/src/commonMain/kotlin/org/example/project/babygrowthtrackingapplication/team/TeamBabiesTab.kt

package org.example.project.babygrowthtrackingapplication.team

import androidx.compose.animation.*
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
import androidx.compose.ui.text.style.TextOverflow
import babygrowthtrackingapplication.composeapp.generated.resources.Res
import babygrowthtrackingapplication.composeapp.generated.resources.*
import org.example.project.babygrowthtrackingapplication.theme.*
import org.example.project.babygrowthtrackingapplication.ui.components.*
import org.jetbrains.compose.resources.stringResource

// ─────────────────────────────────────────────────────────────────────────────
// Babies Tab — list of all babies at the bench
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun TeamBabiesTab(
    viewModel  : TeamVaccinationViewModel,
    onBabyClick: (TeamBabyItem) -> Unit
) {
    val state        = viewModel.uiState
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors

    Column(modifier = Modifier.fillMaxSize()) {

        // ── Stats header ──────────────────────────────────────────────────────
        TeamBabiesStatsRow(
            total        = state.babies.size,
            overdue      = state.babies.count { it.vaccineStatus == TeamVaccineStatus.OVERDUE },
            dueSoon      = state.babies.count { it.vaccineStatus == TeamVaccineStatus.DUE_SOON },
            customColors = customColors,
            dimensions   = dimensions
        )

        // ── Search bar ────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimensions.screenPadding, vertical = dimensions.spacingSmall),
            horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall),
            verticalAlignment     = Alignment.CenterVertically
        ) {
            SearchTextField(
                value         = state.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                placeholder   = stringResource(Res.string.team_search_babies_hint),
                modifier      = Modifier.weight(1f),
                leadingIcon   = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.size(dimensions.iconMedium)
                    )
                },
                trailingIcon = if (state.searchQuery.isNotEmpty()) {
                    {
                        IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                            Icon(
                                Icons.Default.Clear, null,
                                tint     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                modifier = Modifier.size(dimensions.iconMedium)
                            )
                        }
                    }
                } else null
            )
        }

        // ── Content ───────────────────────────────────────────────────────────
        when {
            state.babiesLoading -> {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator(color = customColors.accentGradientStart)
                }
            }
            state.filteredBabies.isEmpty() -> {
                TeamEmptyState(
                    emoji      = stringResource(Res.string.team_emoji_baby),
                    title      = if (state.searchQuery.isNotEmpty())
                        stringResource(Res.string.team_empty_search_title)
                    else stringResource(Res.string.team_empty_babies_title),
                    subtitle   = if (state.searchQuery.isNotEmpty())
                        stringResource(Res.string.team_empty_search_subtitle)
                    else stringResource(Res.string.team_empty_babies_subtitle),
                    dimensions = dimensions
                )
            }
            else -> {
                LazyColumn(
                    modifier            = Modifier.fillMaxSize(),
                    contentPadding      = PaddingValues(
                        horizontal = dimensions.screenPadding,
                        vertical   = dimensions.spacingSmall
                    ),
                    verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
                ) {
                    items(state.filteredBabies, key = { it.babyId }) { baby ->
                        TeamBabyCard(
                            baby         = baby,
                            onClick      = { onBabyClick(baby) },
                            customColors = customColors,
                            dimensions   = dimensions
                        )
                    }
                    item { Spacer(Modifier.height(dimensions.spacingXXLarge)) }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Stats Row
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TeamBabiesStatsRow(
    total       : Int,
    overdue     : Int,
    dueSoon     : Int,
    customColors: CustomColors,
    dimensions  : Dimensions
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = dimensions.screenPadding, vertical = dimensions.spacingSmall),
        horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
    ) {
        StatChip(
            label      = stringResource(Res.string.team_stat_total),
            value      = total.toString(),
            color      = customColors.accentGradientStart,
            emoji      = stringResource(Res.string.team_emoji_baby),
            dimensions = dimensions,
            modifier   = Modifier.weight(1f)
        )
        StatChip(
            label      = stringResource(Res.string.team_stat_overdue),
            value      = overdue.toString(),
            color      = MaterialTheme.colorScheme.error,
            emoji      = stringResource(Res.string.team_emoji_overdue),
            dimensions = dimensions,
            modifier   = Modifier.weight(1f)
        )
        StatChip(
            label      = stringResource(Res.string.team_stat_due_soon),
            value      = dueSoon.toString(),
            color      = customColors.warning,
            emoji      = stringResource(Res.string.team_emoji_due_soon),
            dimensions = dimensions,
            modifier   = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatChip(
    label     : String,
    value     : String,
    color     : Color,
    emoji     : String,
    dimensions: Dimensions,
    modifier  : Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape    = RoundedCornerShape(dimensions.cardCornerRadius),
        color    = color.copy(alpha = 0.08f),
        border   = BorderStroke(dimensions.borderWidthThin, color.copy(alpha = 0.25f))
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
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Baby Card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TeamBabyCard(
    baby        : TeamBabyItem,
    onClick     : () -> Unit,
    customColors: CustomColors,
    dimensions  : Dimensions
) {
    val isFemale    = baby.gender.equals("GIRL", ignoreCase = true) || baby.gender.equals("FEMALE", ignoreCase = true)
    val genderEmoji = if (isFemale) stringResource(Res.string.team_emoji_girl) else stringResource(Res.string.team_emoji_boy)

    val statusColor = when (baby.vaccineStatus) {
        TeamVaccineStatus.OVERDUE    -> MaterialTheme.colorScheme.error
        TeamVaccineStatus.DUE_SOON   -> customColors.warning
        TeamVaccineStatus.UP_TO_DATE -> customColors.success
        TeamVaccineStatus.NO_SCHEDULE-> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
    }
    val statusLabel = when (baby.vaccineStatus) {
        TeamVaccineStatus.OVERDUE    -> stringResource(Res.string.team_badge_overdue)
        TeamVaccineStatus.DUE_SOON   -> stringResource(Res.string.team_badge_due_soon)
        TeamVaccineStatus.UP_TO_DATE -> stringResource(Res.string.team_badge_up_to_date)
        TeamVaccineStatus.NO_SCHEDULE-> stringResource(Res.string.team_badge_no_schedule)
    }
    val statusEmoji = when (baby.vaccineStatus) {
        TeamVaccineStatus.OVERDUE    -> stringResource(Res.string.team_emoji_overdue)
        TeamVaccineStatus.DUE_SOON   -> stringResource(Res.string.team_emoji_due_soon)
        TeamVaccineStatus.UP_TO_DATE -> stringResource(Res.string.team_emoji_completed)
        TeamVaccineStatus.NO_SCHEDULE-> stringResource(Res.string.team_emoji_scheduled)
    }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(dimensions.cardCornerRadius),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = dimensions.cardElevationSmall),
        onClick   = onClick
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(dimensions.spacingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(dimensions.avatarMedium)
                    .background(
                        if (isFemale) customColors.accentGradientStart.copy(alpha = 0.15f)
                        else customColors.accentGradientEnd.copy(alpha = 0.15f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(genderEmoji, style = MaterialTheme.typography.titleLarge)
            }

            Spacer(Modifier.width(dimensions.spacingMedium))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        text       = baby.fullName,
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onSurface,
                        modifier   = Modifier.weight(1f),
                        maxLines   = 1,
                        overflow   = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.width(dimensions.spacingXSmall))
                    Surface(
                        shape = RoundedCornerShape(dimensions.filterTabCorner),
                        color = statusColor.copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier              = Modifier.padding(horizontal = dimensions.spacingXSmall * 2, vertical = dimensions.spacingXSmall / 2),
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall / 2)
                        ) {
                            Text(statusEmoji, style = MaterialTheme.typography.labelSmall)
                            Text(
                                text       = statusLabel,
                                style      = MaterialTheme.typography.labelSmall,
                                color      = statusColor,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(Modifier.height(dimensions.spacingXSmall))

                Row(horizontalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall / 2)
                    ) {
                        Text(stringResource(Res.string.team_emoji_birthday), style = MaterialTheme.typography.labelSmall)
                        Text(
                            text  = stringResource(Res.string.team_baby_age_card, baby.ageInMonths),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                        )
                    }
                    if (baby.parentName.isNotBlank()) {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall / 2)
                        ) {
                            Text(stringResource(Res.string.team_emoji_person), style = MaterialTheme.typography.labelSmall)
                            Text(
                                text     = baby.parentName,
                                style    = MaterialTheme.typography.bodySmall,
                                color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                baby.nextVacDate?.let { date ->
                    Spacer(Modifier.height(dimensions.spacingXSmall))
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall / 2)
                    ) {
                        Text(stringResource(Res.string.team_emoji_vaccine), style = MaterialTheme.typography.labelSmall)
                        Text(
                            text       = stringResource(Res.string.team_next_vaccine_date, date),
                            style      = MaterialTheme.typography.labelSmall,
                            color      = customColors.accentGradientStart,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                modifier = Modifier.size(dimensions.iconMedium)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Empty state helper (shared across team tabs)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun TeamEmptyState(
    emoji     : String,
    title     : String,
    subtitle  : String,
    dimensions: Dimensions
) {
    Box(Modifier.fillMaxSize(), Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall),
            modifier            = Modifier.padding(dimensions.spacingXLarge)
        ) {
            Text(emoji, style = MaterialTheme.typography.displaySmall)
            Text(
                text       = title,
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text  = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
        }
    }
}