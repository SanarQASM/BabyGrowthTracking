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
import androidx.compose.ui.unit.*
import org.example.project.babygrowthtrackingapplication.theme.*

// ─────────────────────────────────────────────────────────────────────────────
// Animated notification bell with live badge
// CHANGE: onLongClick parameter added so HomeTopBar can show preview panel
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun NotificationBell(
    unreadCount  : Long,
    onClick      : () -> Unit,
    onLongClick  : () -> Unit = {},    // ← NEW: triggers the preview panel
    customColors : CustomColors,
    dimensions   : Dimensions,
    modifier     : Modifier = Modifier
) {
    // Shake animation when new notifications arrive
    val previousCount = remember { mutableStateOf(unreadCount) }
    var shouldShake   by remember { mutableStateOf(false) }

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

    Box(modifier = modifier) {
        // ── Long-press support via combinedClickable ──────────────────────────
        Box(
            modifier = Modifier
                .size(48.dp)    // standard IconButton touch target
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
                contentDescription = "Notifications",
                tint     = if (unreadCount > 0) customColors.accentGradientStart
                else customColors.accentGradientStart.copy(0.7f),
                modifier = Modifier.size(dimensions.iconMedium)
            )
        }

        // Unread badge
        AnimatedVisibility(
            visible = unreadCount > 0,
            enter   = scaleIn(transformOrigin = TransformOrigin(1f, 0f)) + fadeIn(),
            exit    = scaleOut(transformOrigin = TransformOrigin(1f, 0f)) + fadeOut(),
            modifier = Modifier.align(Alignment.TopEnd).padding(top = 4.dp, end = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .sizeIn(minWidth = 16.dp, minHeight = 16.dp)
                    .background(MaterialTheme.colorScheme.error, CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.surface, CircleShape)
                    .padding(horizontal = 3.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text       = if (unreadCount > 99) "99+" else unreadCount.toString(),
                    style      = MaterialTheme.typography.labelSmall,
                    fontSize   = 9.sp,
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
    Card(
        modifier  = Modifier
            .width(320.dp)
            .wrapContentHeight(),
        shape     = RoundedCornerShape(dimensions.cardCornerRadius),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(customColors.accentGradientStart.copy(0.1f))
                    .padding(horizontal = dimensions.spacingMedium, vertical = dimensions.spacingSmall),
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
                        tint = customColors.accentGradientStart,
                        modifier = Modifier.size(dimensions.iconSmall)
                    )
                    Text(
                        "Notifications",
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
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurface.copy(0.4f),
                        modifier = Modifier.size(16.dp)
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
                        Text("🔔", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(dimensions.spacingSmall))
                        Text(
                            "No notifications",
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

            // View all button
            TextButton(
                onClick  = onViewAll,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "View all notifications",
                    style  = MaterialTheme.typography.labelMedium,
                    color  = customColors.accentGradientStart,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint     = customColors.accentGradientStart,
                    modifier = Modifier.size(14.dp)
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
    val emoji = when (notification.category) {
        NotificationCategory.VACCINATION  -> "💉"
        NotificationCategory.GROWTH       -> "📏"
        NotificationCategory.APPOINTMENT  -> "📅"
        NotificationCategory.HEALTH       -> "❤️"
        NotificationCategory.DEVELOPMENT  -> "🧠"
        NotificationCategory.BABY_PROFILE -> "👶"
        NotificationCategory.MEMORIES     -> "📸"
        NotificationCategory.ACCOUNT      -> "🔐"
        NotificationCategory.SYSTEM       -> "⚙️"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (!notification.isRead) customColors.accentGradientStart.copy(0.05f)
                else Color.Transparent
            )
            .clickable { onTap() }
            .padding(horizontal = dimensions.spacingMedium, vertical = dimensions.spacingSmall),
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
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(customColors.accentGradientStart)
                    .padding(top = 4.dp)
            )
        }
    }
}