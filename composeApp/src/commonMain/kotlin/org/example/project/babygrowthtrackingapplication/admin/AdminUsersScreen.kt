// File: composeApp/src/commonMain/kotlin/org/example/project/babygrowthtrackingapplication/admin/AdminUsersScreen.kt

package org.example.project.babygrowthtrackingapplication.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import babygrowthtrackingapplication.composeapp.generated.resources.Res
import babygrowthtrackingapplication.composeapp.generated.resources.*
import org.example.project.babygrowthtrackingapplication.data.network.UserResponse
import org.example.project.babygrowthtrackingapplication.theme.LocalDimensions
import org.example.project.babygrowthtrackingapplication.theme.customColors
import org.jetbrains.compose.resources.stringResource

@Composable
fun AdminUsersScreen(
    viewModel: AdminViewModel,
    modifier : Modifier = Modifier,
) {
    val state      = viewModel.uiState
    val dimensions = LocalDimensions.current

    var pendingDeleteUser by remember { mutableStateOf<UserResponse?>(null) }

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
                text     = stringResource(Res.string.admin_users_title),
                style    = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Text(
                text     = stringResource(Res.string.admin_users_count, state.filteredUsers.size),
                style    = MaterialTheme.typography.labelMedium,
                color    = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        // ── Search ─────────────────────────────────────────────────────────
        AdminSearchBar(
            query         = state.userSearchQuery,
            onQueryChange = viewModel::setUserSearchQuery,
            hint          = stringResource(Res.string.admin_users_search_hint)
        )

        // ── Filter tabs ────────────────────────────────────────────────────
        val userTabs = listOf(
            AdminUserFilterTab.ALL     to stringResource(Res.string.admin_user_tab_all),
            AdminUserFilterTab.PARENTS to stringResource(Res.string.admin_user_tab_parents),
            AdminUserFilterTab.ADMINS  to stringResource(Res.string.admin_user_tab_admins),
        )
        Row(Modifier.horizontalScroll(rememberScrollState())) {
            AdminFilterTabs(
                tabs        = userTabs,
                selectedTab = state.selectedUserTab,
                onTabSelect = viewModel::setUserTab
            )
        }

        // ── List ───────────────────────────────────────────────────────────
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.customColors.accentGradientStart)
            }
        } else if (state.filteredUsers.isEmpty()) {
            AdminEmptyState(
                icon    = Icons.Default.PersonOff,
                message = stringResource(Res.string.admin_user_no_results)
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall),
                contentPadding      = PaddingValues(vertical = dimensions.spacingSmall)
            ) {
                items(state.filteredUsers, key = { it.userId }) { user ->
                    AdminUserCard(
                        user     = user,
                        onDelete = { pendingDeleteUser = user }
                    )
                }
                item { Spacer(Modifier.height(dimensions.spacingXLarge)) }
            }
        }
    }

    // ── Delete confirmation dialog ─────────────────────────────────────────
    pendingDeleteUser?.let { user ->
        AdminConfirmDialog(
            title        = stringResource(Res.string.admin_user_delete_title),
            message      = stringResource(Res.string.admin_user_delete_message, user.fullName),
            confirmLabel = stringResource(Res.string.admin_action_delete),
            onConfirm    = {
                viewModel.deleteUser(user.userId)
                pendingDeleteUser = null
            },
            onDismiss    = { pendingDeleteUser = null }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// AdminUserCard
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AdminUserCard(
    user    : UserResponse,
    onDelete: () -> Unit,
) {
    val dimensions   = LocalDimensions.current
    val isParent     = user.role.equals("parent", ignoreCase = true)
    val roleColor    = if (isParent) MaterialTheme.colorScheme.primary
    else          MaterialTheme.customColors.accentGradientStart
    val roleLabel    = if (isParent) stringResource(Res.string.admin_user_role_parent)
    else          stringResource(Res.string.admin_user_role_admin)
    val verifiedLabel = if (user.isActive)
        stringResource(Res.string.admin_user_verified_badge)
    else
        stringResource(Res.string.admin_user_unverified_badge)
    val verifiedColor = if (user.isActive) MaterialTheme.colorScheme.secondary
    else               MaterialTheme.colorScheme.error

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
                color    = roleColor.copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text       = user.fullName.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                        style      = MaterialTheme.typography.titleMedium,
                        color      = roleColor,
                        fontWeight = FontWeight.Bold,
                        maxLines   = 1,
                    )
                }
            }

            Spacer(Modifier.width(dimensions.spacingMedium))

            // ── Info ───────────────────────────────────────────────────────
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = user.fullName,
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onSurface,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis,
                )
                Text(
                    text     = user.email,
                    style    = MaterialTheme.typography.bodySmall,
                    color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (user.phone?.isNotBlank() == true) {
                    Text(
                        text     = stringResource(Res.string.admin_user_phone_label, user.phone),
                        style    = MaterialTheme.typography.labelSmall,
                        color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(Modifier.height(dimensions.spacingXSmall))
                Row(horizontalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall)) {
                    AdminStatusBadge(label = roleLabel,    color = roleColor)
                    AdminStatusBadge(label = verifiedLabel, color = verifiedColor)
                }
            }

            // ── Delete ─────────────────────────────────────────────────────
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.DeleteOutline,
                    contentDescription = stringResource(Res.string.admin_delete_cd),
                    tint               = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }
        }
    }
}