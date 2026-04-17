// File: composeApp/src/commonMain/kotlin/org/example/project/babygrowthtrackingapplication/admin/AdminBabiesScreen.kt

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
import androidx.compose.ui.text.style.TextOverflow
import babygrowthtrackingapplication.composeapp.generated.resources.Res
import babygrowthtrackingapplication.composeapp.generated.resources.*
import org.example.project.babygrowthtrackingapplication.data.network.BabyResponse
import org.example.project.babygrowthtrackingapplication.theme.LocalDimensions
import org.example.project.babygrowthtrackingapplication.theme.customColors
import org.jetbrains.compose.resources.stringResource

@Composable
fun AdminBabiesScreen(
    viewModel: AdminViewModel,
    modifier : Modifier = Modifier,
) {
    val state      = viewModel.uiState
    val dimensions = LocalDimensions.current

    var pendingDeleteBaby by remember { mutableStateOf<BabyResponse?>(null) }

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
                text       = stringResource(Res.string.admin_babies_title),
                style      = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis,
                modifier   = Modifier.weight(1f)
            )
            Text(
                text     = stringResource(Res.string.admin_babies_count, state.filteredBabies.size),
                style    = MaterialTheme.typography.labelMedium,
                color    = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        // ── Search ─────────────────────────────────────────────────────────
        AdminSearchBar(
            query         = state.babySearchQuery,
            onQueryChange = viewModel::setBabySearchQuery,
            hint          = stringResource(Res.string.admin_babies_search_hint)
        )

        // ── Filter tabs ────────────────────────────────────────────────────
        val babyTabs = listOf(
            AdminBabyFilterTab.ALL      to stringResource(Res.string.admin_baby_tab_all),
            AdminBabyFilterTab.ACTIVE   to stringResource(Res.string.admin_baby_tab_active),
            AdminBabyFilterTab.ARCHIVED to stringResource(Res.string.admin_baby_tab_archived),
        )
        Row(Modifier.horizontalScroll(rememberScrollState())) {
            AdminFilterTabs(
                tabs        = babyTabs,
                selectedTab = state.selectedBabyTab,
                onTabSelect = viewModel::setBabyTab
            )
        }

        // ── List ───────────────────────────────────────────────────────────
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.customColors.accentGradientStart)
            }
        } else if (state.filteredBabies.isEmpty()) {
            AdminEmptyState(
                icon    = Icons.Default.ChildCare,
                message = stringResource(Res.string.admin_baby_no_results)
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall),
                contentPadding      = PaddingValues(vertical = dimensions.spacingSmall)
            ) {
                items(state.filteredBabies, key = { it.babyId }) { baby ->
                    AdminBabyCard(
                        baby     = baby,
                        onDelete = { pendingDeleteBaby = baby }
                    )
                }
                item { Spacer(Modifier.height(dimensions.spacingXLarge)) }
            }
        }
    }

    // ── Delete confirmation dialog ─────────────────────────────────────────
    pendingDeleteBaby?.let { baby ->
        AdminConfirmDialog(
            title        = stringResource(Res.string.admin_baby_delete_title),
            message      = stringResource(Res.string.admin_baby_delete_message, baby.fullName),
            confirmLabel = stringResource(Res.string.admin_action_delete),
            onConfirm    = {
                viewModel.deleteBaby(baby.babyId)
                pendingDeleteBaby = null
            },
            onDismiss    = { pendingDeleteBaby = null }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// AdminBabyCard
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AdminBabyCard(
    baby    : BabyResponse,
    onDelete: () -> Unit,
) {
    val dimensions = LocalDimensions.current

    val isBoy        = baby.gender.equals("male", ignoreCase = true)
    val genderColor  = if (isBoy) MaterialTheme.colorScheme.primary
    else       MaterialTheme.colorScheme.tertiary
    val genderLabel  = if (isBoy) stringResource(Res.string.admin_baby_gender_boy)
    else       stringResource(Res.string.admin_baby_gender_girl)
    val genderEmoji  = if (isBoy) "👦" else "👧"

    val statusLabel  = if (baby.isActive)
        stringResource(Res.string.admin_baby_status_active)
    else
        stringResource(Res.string.admin_baby_status_archived)
    val statusColor  = if (baby.isActive) MaterialTheme.colorScheme.secondary
    else               MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)

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
                color    = genderColor.copy(alpha = 0.12f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(genderEmoji, style = MaterialTheme.typography.titleLarge)
                }
            }

            Spacer(Modifier.width(dimensions.spacingMedium))

            // ── Info ───────────────────────────────────────────────────────
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = baby.fullName,
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis,
                )
                Text(
                    text     = stringResource(Res.string.admin_baby_dob_label, baby.dateOfBirth),
                    style    = MaterialTheme.typography.bodySmall,
                    color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                baby.parentName?.let { parent ->
                    Text(
                        text     = stringResource(Res.string.admin_baby_parent_label, parent),
                        style    = MaterialTheme.typography.labelSmall,
                        color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(Modifier.height(dimensions.spacingXSmall))
                Row(horizontalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall)) {
                    AdminStatusBadge(label = genderLabel, color = genderColor)
                    AdminStatusBadge(label = statusLabel, color = statusColor)
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