// File: composeApp/src/commonMain/kotlin/org/example/project/babygrowthtrackingapplication/admin/AdminTeamMembersScreen.kt

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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import babygrowthtrackingapplication.composeapp.generated.resources.Res
import babygrowthtrackingapplication.composeapp.generated.resources.*
import org.example.project.babygrowthtrackingapplication.data.network.UserResponse
import org.example.project.babygrowthtrackingapplication.theme.LocalDimensions
import org.example.project.babygrowthtrackingapplication.theme.customColors
import org.jetbrains.compose.resources.stringResource

// ─────────────────────────────────────────────────────────────────────────────
// AdminTeamMembersScreen
//
// Lists all vaccination_team accounts. The admin can:
//  • Search by name / email
//  • Filter All / Active / Inactive
//  • Add a new team member (navigates to AdminCreateTeamMemberScreen)
//  • Delete a team member
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun AdminTeamMembersScreen(
    viewModel : AdminViewModel,
    onAddClick: () -> Unit,
    modifier  : Modifier = Modifier,
) {
    val state      = viewModel.uiState
    val dimensions = LocalDimensions.current

    var pendingDeleteMember by remember { mutableStateOf<UserResponse?>(null) }

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
            Column {
                Text(
                    text       = stringResource(Res.string.admin_team_title),
                    style      = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text  = stringResource(Res.string.admin_team_count, state.filteredTeamMembers.size),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }

            // ── Add team member button ─────────────────────────────────────
            FilledIconButton(
                onClick = onAddClick,
                colors  = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.customColors.accentGradientStart
                )
            ) {
                Icon(
                    imageVector        = Icons.Default.PersonAdd,
                    contentDescription = stringResource(Res.string.admin_team_add_cd),
                    tint               = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        // ── Search ─────────────────────────────────────────────────────────
        AdminSearchBar(
            query         = state.teamSearchQuery,
            onQueryChange = viewModel::setTeamSearchQuery,
            hint          = stringResource(Res.string.admin_team_search_hint)
        )

        // ── Filter tabs ────────────────────────────────────────────────────
        val tabs = listOf(
            AdminTeamFilterTab.ALL      to stringResource(Res.string.admin_team_tab_all),
            AdminTeamFilterTab.ACTIVE   to stringResource(Res.string.admin_team_tab_active),
            AdminTeamFilterTab.INACTIVE to stringResource(Res.string.admin_team_tab_inactive),
        )
        Row(Modifier.horizontalScroll(rememberScrollState())) {
            AdminFilterTabs(
                tabs        = tabs,
                selectedTab = state.selectedTeamTab,
                onTabSelect = viewModel::setTeamTab
            )
        }

        // ── List ───────────────────────────────────────────────────────────
        when {
            state.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.customColors.accentGradientStart)
                }
            }

            state.filteredTeamMembers.isEmpty() -> {
                AdminEmptyState(
                    icon    = Icons.Default.MedicalServices,
                    message = stringResource(Res.string.admin_team_no_results)
                )
            }

            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall),
                    contentPadding      = PaddingValues(vertical = dimensions.spacingSmall)
                ) {
                    items(state.filteredTeamMembers, key = { it.userId }) { member ->
                        AdminTeamMemberCard(
                            member   = member,
                            onDelete = { pendingDeleteMember = member }
                        )
                    }
                    item { Spacer(Modifier.height(dimensions.spacingXLarge)) }
                }
            }
        }
    }

    // ── Delete confirmation dialog ─────────────────────────────────────────
    pendingDeleteMember?.let { member ->
        AdminConfirmDialog(
            title        = stringResource(Res.string.admin_team_delete_title),
            message      = stringResource(Res.string.admin_team_delete_message, member.fullName),
            confirmLabel = stringResource(Res.string.admin_action_delete),
            onConfirm    = {
                viewModel.deleteTeamMember(member.userId)
                pendingDeleteMember = null
            },
            onDismiss    = { pendingDeleteMember = null }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// AdminTeamMemberCard
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AdminTeamMemberCard(
    member  : UserResponse,
    onDelete: () -> Unit,
) {
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors

    val activeColor   = MaterialTheme.colorScheme.secondary
    val inactiveColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
    val statusColor   = if (member.isActive) activeColor else inactiveColor
    val statusLabel   = if (member.isActive)
        stringResource(Res.string.admin_team_status_active)
    else
        stringResource(Res.string.admin_team_status_inactive)

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
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ── Avatar ─────────────────────────────────────────────────────
            Surface(
                modifier = Modifier.size(dimensions.avatarSmall),
                shape    = RoundedCornerShape(dimensions.avatarSmall),
                color    = customColors.accentGradientStart.copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector        = Icons.Default.MedicalServices,
                        contentDescription = null,
                        tint               = customColors.accentGradientStart,
                        modifier           = Modifier.size(dimensions.iconMedium)
                    )
                }
            }

            Spacer(Modifier.width(dimensions.spacingMedium))

            // ── Info ───────────────────────────────────────────────────────
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = member.fullName,
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text  = member.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                member.phone?.takeIf { it.isNotBlank() }?.let { phone ->
                    Text(
                        text  = stringResource(Res.string.admin_user_phone_label, phone),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                member.city?.takeIf { it.isNotBlank() }?.let { city ->
                    Text(
                        text  = stringResource(Res.string.admin_user_city_label, city),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                Spacer(Modifier.height(dimensions.spacingXSmall))
                Row(horizontalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall)) {
                    AdminStatusBadge(
                        label = stringResource(Res.string.admin_team_role_label),
                        color = customColors.accentGradientStart
                    )
                    AdminStatusBadge(
                        label = statusLabel,
                        color = statusColor
                    )
                }
            }

            // ── Delete ─────────────────────────────────────────────────────
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector        = Icons.Default.DeleteOutline,
                    contentDescription = stringResource(Res.string.admin_delete_cd),
                    tint               = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }
        }
    }
}