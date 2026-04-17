// File: composeApp/src/commonMain/kotlin/org/example/project/babygrowthtrackingapplication/admin/AdminSettingsScreen.kt

package org.example.project.babygrowthtrackingapplication.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import org.example.project.babygrowthtrackingapplication.theme.LocalDimensions
import org.example.project.babygrowthtrackingapplication.theme.customColors
import org.jetbrains.compose.resources.stringResource

@Composable
fun AdminSettingsScreen(
    viewModel: AdminViewModel,
    modifier : Modifier = Modifier,
) {
    val state      = viewModel.uiState
    val dimensions = LocalDimensions.current

    var showLogoutDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier            = modifier.fillMaxSize(),
        contentPadding      = PaddingValues(dimensions.screenPadding),
        verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)
    ) {
        // ── Title ──────────────────────────────────────────────────────────
        item {
            Text(
                text       = stringResource(Res.string.admin_tab_settings),
                style      = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        // ── Profile card ───────────────────────────────────────────────────
        item {
            Card(
                modifier  = Modifier.fillMaxWidth(),
                shape     = RoundedCornerShape(dimensions.cardCornerRadius * 2),
                elevation = CardDefaults.cardElevation(defaultElevation = dimensions.cardElevation),
                colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier            = Modifier
                        .fillMaxWidth()
                        .padding(dimensions.spacingLarge),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
                ) {
                    // Shield avatar
                    Surface(
                        modifier = Modifier.size(dimensions.avatarLarge),
                        shape    = RoundedCornerShape(dimensions.avatarLarge),
                        color    = MaterialTheme.customColors.accentGradientStart.copy(alpha = 0.15f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint     = MaterialTheme.customColors.accentGradientStart,
                                modifier = Modifier.size(dimensions.iconXLarge)
                            )
                        }
                    }

                    Text(
                        text       = state.adminName.ifBlank { stringResource(Res.string.admin_role_label) },
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text  = state.adminEmail,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    Spacer(Modifier.height(dimensions.spacingXSmall))

                    // Role badge
                    AdminStatusBadge(
                        label = stringResource(Res.string.admin_role_label),
                        color = MaterialTheme.customColors.accentGradientStart
                    )
                }
            }
        }

        // ── Logout row ─────────────────────────────────────────────────────
        item {
            AdminSectionHeader(title = stringResource(Res.string.admin_settings_about_title))
        }
        item {
            Card(
                modifier  = Modifier.fillMaxWidth(),
                shape     = RoundedCornerShape(dimensions.cardCornerRadius),
                elevation = CardDefaults.cardElevation(defaultElevation = dimensions.cardElevationSmall),
                colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column {
                    // App version row
                    ListItem(
                        headlineContent = {
                            Text(
                                stringResource(Res.string.admin_settings_app_version),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        leadingContent = {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        },
                        trailingContent = {
                            Text(
                                text  = "1.0.0",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    // Logout row
                    ListItem(
                        headlineContent = {
                            Text(
                                stringResource(Res.string.admin_action_logout),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        },
                        leadingContent = {
                            Icon(
                                Icons.Default.Logout,
                                contentDescription = stringResource(Res.string.admin_logout_cd),
                                tint = MaterialTheme.colorScheme.error
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .run {
                                // Make the row tappable
                                this
                            }
                    )
                }
            }
        }

        // ── Logout button ──────────────────────────────────────────────────
        item {
            Button(
                onClick  = { showLogoutDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimensions.buttonHeight),
                shape  = RoundedCornerShape(dimensions.buttonCornerRadius),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
                ) {
                    Icon(
                        Icons.Default.Logout,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onError
                    )
                    Text(
                        text       = stringResource(Res.string.admin_action_logout),
                        color      = MaterialTheme.colorScheme.onError,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        item { Spacer(Modifier.height(dimensions.spacingXLarge)) }
    }

    // ── Logout confirmation dialog ─────────────────────────────────────────
    if (showLogoutDialog) {
        AdminConfirmDialog(
            title        = stringResource(Res.string.admin_settings_logout_title),
            message      = stringResource(Res.string.admin_settings_logout_message),
            confirmLabel = stringResource(Res.string.admin_settings_logout_confirm),
            onConfirm    = {
                showLogoutDialog = false
                viewModel.logout()
            },
            onDismiss    = { showLogoutDialog = false },
            confirmColor = MaterialTheme.colorScheme.error
        )
    }
}