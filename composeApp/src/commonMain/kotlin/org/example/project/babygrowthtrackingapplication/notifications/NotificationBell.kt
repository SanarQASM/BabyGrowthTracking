package org.example.project.babygrowthtrackingapplication.notifications

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import org.example.project.babygrowthtrackingapplication.theme.*
import babygrowthtrackingapplication.composeapp.generated.resources.Res
import babygrowthtrackingapplication.composeapp.generated.resources.notif_screen_title
import babygrowthtrackingapplication.composeapp.generated.resources.notif_unread_badge_max
import babygrowthtrackingapplication.composeapp.generated.resources.notif_empty_no_unread
import babygrowthtrackingapplication.composeapp.generated.resources.view_all_format
import org.jetbrains.compose.resources.stringResource

// ─────────────────────────────────────────────────────────────────────────────
// Animated notification bell with live badge
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun NotificationBell(
    unreadCount  : Long,
    onClick      : () -> Unit,
    onLongClick  : () -> Unit = {},
    customColors : CustomColors,
    dimensions   : Dimensions,
    modifier     : Modifier = Modifier
) {
    val previousCount = remember { mutableStateOf(unreadCount) }
    var shouldShake   by remember { mutableStateOf(false) }

    val badgeMax = stringResource(Res.string.notif_unread_badge_max)

    LaunchedEffect(unreadCount) {
        if (unreadCount > previousCount.value) {
            shouldShake = true
            kotlinx.coroutines.delay(600)
            shouldShake = false
        }
        previousCount.value = unreadCount
    }

    val rotation by animateFloatAsState(
        targetValue   = if (shouldShake) 15f else 0f,
        animationSpec = if (shouldShake)
            spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessHigh)
        else tween(200),
        label = "bell_rotation"
    )

    // Standard IconButton touch-target size
    val touchTarget = dimensions.avatarMedium  // 48dp across all sizes

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .size(touchTarget)
                .rotate(rotation)
                .combinedClickable(
                    onClick     = onClick,
                    onLongClick = onLongClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (unreadCount > 0) Icons.Default.Notifications
                else Icons.Default.NotificationsNone,
                contentDescription = null,
                tint     = if (unreadCount > 0) customColors.accentGradientStart
                else customColors.accentGradientStart.copy(0.7f),
                modifier = Modifier.size(dimensions.iconMedium)
            )
        }

        // Unread badge
        AnimatedVisibility(
            visible  = unreadCount > 0,
            enter    = scaleIn(transformOrigin = TransformOrigin(1f, 0f)) + fadeIn(),
            exit     = scaleOut(transformOrigin = TransformOrigin(1f, 0f)) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(
                    top = dimensions.spacingXSmall,
                    end = dimensions.spacingXSmall
                )
        ) {
            Box(
                modifier = Modifier
                    .sizeIn(
                        minWidth  = dimensions.iconSmall,
                        minHeight = dimensions.iconSmall
                    )
                    .background(MaterialTheme.colorScheme.error, CircleShape)
                    .border(
                        dimensions.borderWidthThin,
                        MaterialTheme.colorScheme.surface,
                        CircleShape
                    )
                    .padding(horizontal = dimensions.borderWidthThin + dimensions.borderWidthMedium),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text       = if (unreadCount > 99) badgeMax else unreadCount.toString(),
                    style      = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onError
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Mini notification preview panel (drop-down from bell long-press)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun NotificationPreviewPanel(
    notifications : List<AppNotification>,
    unreadCount   : Long,
    customColors  : CustomColors,
    dimensions    : Dimensions,
    onViewAll     : () -> Unit,
    onTapItem     : (AppNotification) -> Unit,
    onDismiss     : () -> Unit
) {
    val title        = stringResource(Res.string.notif_screen_title)
    val badgeMax     = stringResource(Res.string.notif_unread_badge_max)
    val emptyText    = stringResource(Res.string.notif_empty_no_unread)
    val emptyIcon    = "🔔"                      // decorative — not translatable

    // Panel width: derived from a landscape pane width token (320dp equivalent)
    val panelWidth = dimensions.landscapeNarrowPaneWidth + dimensions.spacingXLarge

    Card(
        modifier  = Modifier
            .width(panelWidth)
            .wrapContentHeight(),
        shape     = RoundedCornerShape(dimensions.cardCornerRadius),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = dimensions.cardElevation * 2)
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(customColors.accentGradientStart.copy(0.1f))
                    .padding(
                        horizontal = dimensions.spacingMedium,
                        vertical   = dimensions.spacingSmall
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
                ) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = null,
                        tint     = customColors.accentGradientStart,
                        modifier = Modifier.size(dimensions.iconSmall)
                    )
                    Text(
                        title,
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color      = customColors.accentGradientStart
                    )
                    if (unreadCount > 0) {
                        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.error) {
                            Text(
                                if (unreadCount > 9) "9+" else unreadCount.toString(),
                                style    = MaterialTheme.typography.labelSmall,
                                color    = MaterialTheme.colorScheme.onError,
                                modifier = Modifier.padding(
                                    horizontal = dimensions.spacingXSmall + dimensions.borderWidthThin,
                                    vertical   = dimensions.borderWidthThin
                                )
                            )
                        }
                    }
                }
                IconButton(
                    onClick  = onDismiss,
                    modifier = Modifier.size(dimensions.iconMedium)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = null,
                        tint     = MaterialTheme.colorScheme.onSurface.copy(0.4f),
                        modifier = Modifier.size(dimensions.iconSmall)
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            if (notifications.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(dimensions.spacingXLarge),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(emptyIcon, style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(dimensions.spacingSmall))
                        Text(
                            emptyText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(0.5f)
                        )
                    }
                }
            } else {
                Column {
                    notifications.take(5).forEach { notif ->
                        PreviewNotificationRow(
                            notification = notif,
                            customColors = customColors,
                            dimensions   = dimensions,
                            onTap        = { onTapItem(notif) }
                        )
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(0.5f)
                        )
                    }
                }
            }

            // View all button — label comes from the screen title resource
            TextButton(
                onClick  = onViewAll,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(Res.string.view_all_format, title),
                    style      = MaterialTheme.typography.labelMedium,
                    color      = customColors.accentGradientStart,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.width(dimensions.spacingXSmall))
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint     = customColors.accentGradientStart,
                    modifier = Modifier.size(dimensions.iconSmall - dimensions.borderWidthMedium)
                )
            }
        }
    }
}

@Composable
private fun PreviewNotificationRow(
    notification : AppNotification,
    customColors : CustomColors,
    dimensions   : Dimensions,
    onTap        : () -> Unit
) {
    val emoji = notificationCategoryEmoji(notification.category)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (!notification.isRead) customColors.accentGradientStart.copy(0.05f)
                else Color.Transparent
            )
            .clickable { onTap() }
            .padding(
                horizontal = dimensions.spacingMedium,
                vertical   = dimensions.spacingSmall
            ),
        horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall),
        verticalAlignment     = Alignment.Top
    ) {
        Text(emoji, style = MaterialTheme.typography.bodyMedium)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = notification.title,
                style      = MaterialTheme.typography.labelMedium,
                fontWeight = if (!notification.isRead) FontWeight.Bold else FontWeight.Normal,
                color      = MaterialTheme.colorScheme.onSurface,
                maxLines   = 1,
                overflow   = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Text(
                text     = notification.body,
                style    = MaterialTheme.typography.labelSmall,
                color    = MaterialTheme.colorScheme.onSurface.copy(0.6f),
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
        if (!notification.isRead) {
            Box(
                modifier = Modifier
                    .size(dimensions.spacingSmall - dimensions.borderWidthMedium)
                    .clip(CircleShape)
                    .background(customColors.accentGradientStart)
                    .padding(top = dimensions.spacingXSmall)
            )
        }
    }
}