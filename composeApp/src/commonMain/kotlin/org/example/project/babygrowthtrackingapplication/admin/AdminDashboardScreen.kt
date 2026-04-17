// File: composeApp/src/commonMain/kotlin/org/example/project/babygrowthtrackingapplication/admin/AdminDashboardScreen.kt

package org.example.project.babygrowthtrackingapplication.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import babygrowthtrackingapplication.composeapp.generated.resources.Res
import babygrowthtrackingapplication.composeapp.generated.resources.*
import org.example.project.babygrowthtrackingapplication.theme.LocalDimensions
import org.example.project.babygrowthtrackingapplication.theme.customColors
import org.jetbrains.compose.resources.stringResource

@Composable
fun AdminDashboardScreen(
    viewModel: AdminViewModel,
    modifier : Modifier = Modifier,
) {
    val state      = viewModel.uiState
    val stats      = state.stats
    val dimensions = LocalDimensions.current

    if (state.isLoading) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)
            ) {
                CircularProgressIndicator(color = MaterialTheme.customColors.accentGradientStart)
                Text(
                    text     = stringResource(Res.string.admin_loading_data),
                    style    = MaterialTheme.typography.bodyMedium,
                    color    = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        return
    }

    androidx.compose.foundation.lazy.LazyColumn(
        modifier            = modifier.fillMaxSize(),
        contentPadding      = PaddingValues(dimensions.screenPadding),
        verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)
    ) {
        // ── Header ─────────────────────────────────────────────────────────
        item {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text       = stringResource(Res.string.admin_dashboard_overview),
                        style      = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onBackground,
                        maxLines   = 1,
                        overflow   = TextOverflow.Ellipsis,
                    )
                    Text(
                        text     = state.adminName.ifBlank { state.adminEmail },
                        style    = MaterialTheme.typography.bodySmall,
                        color    = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                // Refresh button
                IconButton(
                    onClick = viewModel::refresh,
                    enabled = !state.isRefreshing
                ) {
                    if (state.isRefreshing) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(dimensions.iconMedium),
                            color       = MaterialTheme.customColors.accentGradientStart,
                            strokeWidth = dimensions.borderWidthMedium
                        )
                    } else {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = stringResource(Res.string.admin_refresh_cd),
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }

        // ── Users section ──────────────────────────────────────────────────
        item {
            AdminSectionHeader(title = stringResource(Res.string.admin_tab_users))
        }
        item {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
            ) {
                AdminStatCard(
                    label     = stringResource(Res.string.admin_stat_total_users),
                    value     = stats.totalUsers.toString(),
                    icon      = Icons.Default.People,
                    tintColor = MaterialTheme.customColors.accentGradientStart,
                    modifier  = Modifier.weight(1f)
                )
                AdminStatCard(
                    label     = stringResource(Res.string.admin_stat_parents),
                    value     = stats.totalParents.toString(),
                    icon      = Icons.Default.FamilyRestroom,
                    tintColor = MaterialTheme.colorScheme.tertiary,
                    modifier  = Modifier.weight(1f)
                )
                AdminStatCard(
                    label     = stringResource(Res.string.admin_stat_verified),
                    value     = stats.verifiedUsers.toString(),
                    icon      = Icons.Default.VerifiedUser,
                    tintColor = MaterialTheme.colorScheme.secondary,
                    modifier  = Modifier.weight(1f)
                )
            }
        }

        // ── Babies section ─────────────────────────────────────────────────
        item {
            AdminSectionHeader(title = stringResource(Res.string.admin_tab_babies))
        }
        item {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
            ) {
                AdminStatCard(
                    label     = stringResource(Res.string.admin_stat_total_babies),
                    value     = stats.totalBabies.toString(),
                    icon      = Icons.Default.ChildCare,
                    tintColor = MaterialTheme.colorScheme.primary,
                    modifier  = Modifier.weight(1f)
                )
                AdminStatCard(
                    label     = stringResource(Res.string.admin_stat_active_babies),
                    value     = stats.activeBabies.toString(),
                    icon      = Icons.Default.CheckCircle,
                    tintColor = MaterialTheme.colorScheme.secondary,
                    modifier  = Modifier.weight(1f)
                )
                AdminStatCard(
                    label     = stringResource(Res.string.admin_stat_archived_babies),
                    value     = stats.archivedBabies.toString(),
                    icon      = Icons.Default.Archive,
                    tintColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    modifier  = Modifier.weight(1f)
                )
            }
        }

        // ── Vaccinations section ───────────────────────────────────────────
        item {
            AdminSectionHeader(title = stringResource(Res.string.admin_tab_vaccinations))
        }
        item {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
            ) {
                AdminStatCard(
                    label     = stringResource(Res.string.admin_stat_overdue_vaccinations),
                    value     = stats.overdueVaccinations.toString(),
                    icon      = Icons.Default.Warning,
                    tintColor = MaterialTheme.colorScheme.error,
                    modifier  = Modifier.weight(1f)
                )
                AdminStatCard(
                    label     = stringResource(Res.string.admin_stat_total_admins),
                    value     = stats.totalAdmins.toString(),
                    icon      = Icons.Default.AdminPanelSettings,
                    tintColor = MaterialTheme.customColors.accentGradientEnd,
                    modifier  = Modifier.weight(1f)
                )
            }
        }

        // Bottom padding
        item { Spacer(Modifier.height(dimensions.spacingXLarge)) }
    }
}