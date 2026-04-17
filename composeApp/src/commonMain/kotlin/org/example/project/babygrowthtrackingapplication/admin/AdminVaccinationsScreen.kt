// File: composeApp/src/commonMain/kotlin/org/example/project/babygrowthtrackingapplication/admin/AdminVaccinationsScreen.kt

package org.example.project.babygrowthtrackingapplication.admin

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import babygrowthtrackingapplication.composeapp.generated.resources.Res
import babygrowthtrackingapplication.composeapp.generated.resources.*
import org.example.project.babygrowthtrackingapplication.theme.LocalDimensions
import org.example.project.babygrowthtrackingapplication.theme.customColors
import org.jetbrains.compose.resources.stringResource

@Composable
fun AdminVaccinationsScreen(
    viewModel: AdminViewModel,
    modifier : Modifier = Modifier,
) {
    val state      = viewModel.uiState
    val dimensions = LocalDimensions.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = dimensions.screenPadding),
        verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
    ) {
        Spacer(Modifier.height(dimensions.spacingSmall))

        // ── Title row ──────────────────────────────────────────────────────
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text(
                text       = stringResource(Res.string.admin_vax_title),
                style      = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis,
                modifier   = Modifier.weight(1f)
            )
            Text(
                text     = stringResource(Res.string.admin_vax_count, state.filteredVaxRecords.size),
                style    = MaterialTheme.typography.labelMedium,
                color    = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        // ── Overdue warning banner ─────────────────────────────────────────
        val overdueCount = state.stats.overdueVaccinations
        if (overdueCount > 0) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                shape = RoundedCornerShape(dimensions.cardCornerRadius)
            ) {
                Row(
                    modifier          = Modifier
                        .fillMaxWidth()
                        .padding(dimensions.spacingMedium),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text       = stringResource(Res.string.admin_vax_overdue_count, overdueCount),
                        style      = MaterialTheme.typography.bodyMedium,
                        color      = MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = FontWeight.Medium,
                        maxLines   = 1,
                        overflow   = TextOverflow.Ellipsis,
                        modifier   = Modifier.weight(1f)
                    )
                }
            }
        }

        // ── Search ─────────────────────────────────────────────────────────
        AdminSearchBar(
            query         = state.vaxSearchQuery,
            onQueryChange = viewModel::setVaxSearchQuery,
            hint          = stringResource(Res.string.admin_vax_search_hint)
        )

        // ── Filter tabs ────────────────────────────────────────────────────
        val vaxTabs = listOf(
            AdminVaxFilterTab.ALL       to stringResource(Res.string.admin_vax_tab_all),
            AdminVaxFilterTab.OVERDUE   to stringResource(Res.string.admin_vax_tab_overdue),
            AdminVaxFilterTab.UPCOMING  to stringResource(Res.string.admin_vax_tab_upcoming),
            AdminVaxFilterTab.COMPLETED to stringResource(Res.string.admin_vax_tab_completed),
        )
        Row(Modifier.horizontalScroll(rememberScrollState())) {
            AdminFilterTabs(
                tabs        = vaxTabs,
                selectedTab = state.selectedVaxTab,
                onTabSelect = viewModel::setVaxTab
            )
        }

        // ── List ───────────────────────────────────────────────────────────
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.customColors.accentGradientStart)
            }
        } else if (state.filteredVaxRecords.isEmpty()) {
            AdminEmptyState(
                icon    = Icons.Default.Vaccines,
                message = stringResource(Res.string.admin_vax_no_results)
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall),
                contentPadding      = PaddingValues(vertical = dimensions.spacingSmall)
            ) {
                items(state.filteredVaxRecords, key = { it.scheduleId }) { record ->
                    AdminVaxCard(record = record)
                }
                item { Spacer(Modifier.height(dimensions.spacingXLarge)) }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// AdminVaxCard
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AdminVaxCard(record: AdminVaxRecord) {
    val dimensions = LocalDimensions.current

    val (statusLabel, statusColor) = vaxStatusDecoration(record.status)

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(dimensions.cardCornerRadius),
        elevation = CardDefaults.cardElevation(defaultElevation = dimensions.cardElevationSmall),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(dimensions.spacingMedium),
            verticalAlignment = Alignment.Top
        ) {
            // ── Icon ───────────────────────────────────────────────────────
            Surface(
                modifier = Modifier.size(dimensions.iconLarge + dimensions.spacingSmall),
                shape    = RoundedCornerShape(dimensions.spacingSmall),
                color    = statusColor.copy(alpha = 0.12f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Vaccines,
                        contentDescription = null,
                        tint     = statusColor,
                        modifier = Modifier.size(dimensions.iconMedium)
                    )
                }
            }

            Spacer(Modifier.width(dimensions.spacingMedium))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.Top
                ) {
                    Text(
                        text       = record.vaccineName,
                        style      = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines   = 1,
                        overflow   = TextOverflow.Ellipsis,
                        modifier   = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(dimensions.spacingXSmall))
                    AdminStatusBadge(label = statusLabel, color = statusColor)
                }
                Text(
                    text     = stringResource(Res.string.admin_vax_dose_label, record.doseNumber),
                    style    = MaterialTheme.typography.labelSmall,
                    color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text     = stringResource(Res.string.admin_vax_baby_label, record.babyName),
                    style    = MaterialTheme.typography.bodySmall,
                    color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (record.scheduledDate.isNotBlank()) {
                    Text(
                        text     = stringResource(Res.string.admin_vax_scheduled_label, record.scheduledDate),
                        style    = MaterialTheme.typography.labelSmall,
                        color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (record.benchNameEn.isNotBlank()) {
                    Text(
                        text     = stringResource(Res.string.admin_vax_bench_label, record.benchNameEn),
                        style    = MaterialTheme.typography.labelSmall,
                        color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun vaxStatusDecoration(status: String): Pair<String, Color> = when {
    status.equals("OVERDUE",   ignoreCase = true) ->
        stringResource(Res.string.admin_vax_status_overdue)   to MaterialTheme.colorScheme.error
    status.equals("COMPLETED", ignoreCase = true) ->
        stringResource(Res.string.admin_vax_status_completed) to MaterialTheme.colorScheme.secondary
    status.equals("DUE_SOON",  ignoreCase = true) ->
        stringResource(Res.string.admin_vax_status_due_soon)  to MaterialTheme.colorScheme.tertiary
    status.equals("MISSED",    ignoreCase = true) ->
        stringResource(Res.string.admin_vax_status_missed)    to MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
    else ->
        stringResource(Res.string.admin_vax_status_upcoming)  to MaterialTheme.colorScheme.primary
}