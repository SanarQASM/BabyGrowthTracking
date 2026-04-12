package org.example.project.babygrowthtrackingapplication.notifications

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import babygrowthtrackingapplication.composeapp.generated.resources.Res
import babygrowthtrackingapplication.composeapp.generated.resources.relative_time_days
import babygrowthtrackingapplication.composeapp.generated.resources.relative_time_hours
import babygrowthtrackingapplication.composeapp.generated.resources.relative_time_just_now
import babygrowthtrackingapplication.composeapp.generated.resources.relative_time_minutes
import org.example.project.babygrowthtrackingapplication.theme.*
import org.jetbrains.compose.resources.stringResource

// ─────────────────────────────────────────────────────────────────────────────
// Full Notification Screen
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    viewModel  : NotificationViewModel,
    onBack     : () -> Unit,
    onNavigate : (String) -> Unit
) {
    val state        = viewModel.uiState
    val customColors = MaterialTheme.customColors
    val dimensions   = LocalDimensions.current
    val snackbar     = remember { SnackbarHostState() }

    // Handle deep-link navigation from tapped notification
    LaunchedEffect(state.pendingNavigateTo) {
        state.pendingNavigateTo?.let {
            onNavigate(it)
            viewModel.onNavigationHandled()
        }
    }

    LaunchedEffect(state.successMessage) {
        state.successMessage?.let { snackbar.showSnackbar(it); viewModel.clearMessages() }
    }

    LaunchedEffect(Unit) { viewModel.loadNotifications(refresh = true) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
                    ) {
                        Text(
                            "Notifications",
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color      = customColors.accentGradientStart
                        )
                        if (state.unreadCount > 0) {
                            Badge(containerColor = customColors.accentGradientStart) {
                                Text(
                                    if (state.unreadCount > 99) "99+" else state.unreadCount.toString(),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = customColors.accentGradientStart
                        )
                    }
                },
                actions = {
                    if (state.unreadCount > 0) {
                        TextButton(onClick = viewModel::markAllAsRead) {
                            Text(
                                "Mark all read",
                                style = MaterialTheme.typography.labelMedium,
                                color = customColors.accentGradientStart
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = customColors.accentGradientStart.copy(alpha = 0.12f)
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Filter chips ──────────────────────────────────────────────────
            NotificationFilterRow(
                selectedFilter = state.selectedFilter,
                showOnlyUnread = state.showOnlyUnread,
                onFilterChange = viewModel::setFilter,
                onToggleUnread = viewModel::toggleUnreadOnly,
                customColors   = customColors,
                dimensions     = dimensions
            )

            // ── Content ───────────────────────────────────────────────────────
            when {
                state.isLoading && state.notifications.isEmpty() -> {
                    NotificationLoadingState(dimensions)
                }
                state.errorMessage != null && state.notifications.isEmpty() -> {
                    NotificationErrorState(
                        message    = state.errorMessage,
                        onRetry    = { viewModel.loadNotifications(refresh = true) },
                        dimensions = dimensions
                    )
                }
                state.filteredNotifications.isEmpty() -> {
                    NotificationEmptyState(
                        filter     = state.selectedFilter,
                        unreadOnly = state.showOnlyUnread,
                        dimensions = dimensions
                    )
                }
                else -> {
                    NotificationList(
                        notifications = state.filteredNotifications,
                        isRefreshing  = state.isRefreshing,
                        hasMore       = state.hasMore,
                        customColors  = customColors,
                        dimensions    = dimensions,
                        onTap         = viewModel::onNotificationTapped,
                        onDelete      = viewModel::deleteNotification,
                        onLoadMore    = viewModel::loadMore
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Filter chips row
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun NotificationFilterRow(
    selectedFilter : NotificationFilter,
    showOnlyUnread : Boolean,
    onFilterChange : (NotificationFilter) -> Unit,
    onToggleUnread : () -> Unit,
    customColors   : CustomColors,
    dimensions     : Dimensions
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = dimensions.screenPadding, vertical = dimensions.spacingSmall),
        horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
    ) {
        // Unread toggle chip
        FilterChip(
            selected = showOnlyUnread,
            onClick  = onToggleUnread,
            label    = { Text("Unread only", style = MaterialTheme.typography.labelMedium) },
            shape    = RoundedCornerShape(50),
            colors   = FilterChipDefaults.filterChipColors(
                selectedContainerColor = customColors.accentGradientEnd,
                selectedLabelColor     = Color.White
            )
        )

        // Category filters
        NotificationFilter.entries.forEach { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick  = { onFilterChange(filter) },
                label    = { Text(filter.label, style = MaterialTheme.typography.labelMedium) },
                shape    = RoundedCornerShape(50),
                colors   = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = customColors.accentGradientStart,
                    selectedLabelColor     = Color.White
                )
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Notification list
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun NotificationList(
    notifications : List<AppNotification>,
    isRefreshing  : Boolean,
    hasMore       : Boolean,
    customColors  : CustomColors,
    dimensions    : Dimensions,
    onTap         : (AppNotification) -> Unit,
    onDelete      : (String) -> Unit,
    onLoadMore    : () -> Unit
) {
    val listState = rememberLazyListState()

    // Auto load-more when reaching end
    LaunchedEffect(listState.firstVisibleItemIndex) {
        val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
        if (lastVisible >= notifications.size - 3 && hasMore) {
            onLoadMore()
        }
    }

    if (isRefreshing) {
        LinearProgressIndicator(
            modifier = Modifier.fillMaxWidth(),
            color    = customColors.accentGradientStart,
            trackColor = customColors.accentGradientStart.copy(0.2f)
        )
    }

    LazyColumn(
        state           = listState,
        modifier        = Modifier.fillMaxSize(),
        contentPadding  = PaddingValues(
            horizontal = dimensions.screenPadding,
            vertical   = dimensions.spacingSmall
        ),
        verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
    ) {
        // Group by date
        val grouped = notifications.groupBy { notif ->
            notif.createdAt.take(10) // "YYYY-MM-DD"
        }

        grouped.entries.forEachIndexed { _, (date, group) ->
            item(key = "header_$date") {
                NotificationDateHeader(date = date, dimensions = dimensions)
            }
            items(group, key = { it.notificationId }) { notification ->
                NotificationCard(
                    notification = notification,
                    customColors = customColors,
                    dimensions   = dimensions,
                    onTap        = { onTap(notification) },
                    onDelete     = { onDelete(notification.notificationId) }
                )
            }
        }

        if (hasMore) {
            item {
                Box(Modifier.fillMaxWidth().padding(dimensions.spacingMedium), Alignment.Center) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(24.dp),
                        color       = customColors.accentGradientStart,
                        strokeWidth = 2.dp
                    )
                }
            }
        }

        item { Spacer(Modifier.height(dimensions.spacingXLarge)) }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Date group header
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun NotificationDateHeader(date: String, dimensions: Dimensions) {
    val label = when (date) {
        getCurrentDate()               -> "Today"
        getYesterdayDate()             -> "Yesterday"
        else                           -> formatDisplayDate(date)
    }
    Text(
        text     = label,
        style    = MaterialTheme.typography.labelMedium,
        color    = MaterialTheme.colorScheme.onBackground.copy(0.5f),
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(
            top    = dimensions.spacingMedium,
            bottom = dimensions.spacingXSmall
        )
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Single notification card with swipe-to-delete
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationCard(
    notification : AppNotification,
    customColors : CustomColors,
    dimensions   : Dimensions,
    onTap        : () -> Unit,
    onDelete     : () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) { onDelete(); true } else false
        }
    )

    SwipeToDismissBox(
        state            = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(dimensions.cardCornerRadius))
                    .background(MaterialTheme.colorScheme.error),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint     = Color.White,
                    modifier = Modifier.padding(end = dimensions.spacingLarge)
                )
            }
        }
    ) {
        NotificationCardContent(
            notification = notification,
            customColors = customColors,
            dimensions   = dimensions,
            onTap        = onTap
        )
    }
}

@Composable
private fun NotificationCardContent(
    notification : AppNotification,
    customColors : CustomColors,
    dimensions   : Dimensions,
    onTap        : () -> Unit
) {
    val isUnread = !notification.isRead
    val iconEmoji = when (notification.category) {
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

    val priorityColor = when (notification.priority) {
        NotificationPriorityLevel.URGENT -> MaterialTheme.colorScheme.error
        NotificationPriorityLevel.HIGH   -> Color(0xFFF59E0B)
        NotificationPriorityLevel.MEDIUM -> customColors.accentGradientStart
        NotificationPriorityLevel.LOW    -> MaterialTheme.colorScheme.onSurface.copy(0.4f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onTap() },
        shape  = RoundedCornerShape(dimensions.cardCornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnread)
                customColors.accentGradientStart.copy(alpha = 0.06f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isUnread) 2.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensions.spacingMedium),
            horizontalArrangement = Arrangement.spacedBy(dimensions.spacingMedium),
            verticalAlignment     = Alignment.Top
        ) {
            // Priority indicator bar
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(50))
                    .background(if (isUnread) priorityColor else Color.Transparent)
            )

            // Emoji icon box
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(dimensions.spacingSmall))
                    .background(priorityColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(iconEmoji, style = MaterialTheme.typography.titleMedium)
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.Top
                ) {
                    Text(
                        text       = notification.title,
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = if (isUnread) FontWeight.Bold else FontWeight.Medium,
                        color      = MaterialTheme.colorScheme.onSurface,
                        maxLines   = 2,
                        overflow   = TextOverflow.Ellipsis,
                        modifier   = Modifier.weight(1f)
                    )

                    if (isUnread) {
                        Box(
                            modifier = Modifier
                                .padding(start = dimensions.spacingSmall, top = 2.dp)
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(customColors.accentGradientStart)
                        )
                    }
                }

                Spacer(Modifier.height(2.dp))

                Text(
                    text     = notification.body,
                    style    = MaterialTheme.typography.bodySmall,
                    color    = MaterialTheme.colorScheme.onSurface.copy(
                        if (isUnread) 0.8f else 0.6f
                    ),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(dimensions.spacingXSmall))

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    // Baby name badge
                    notification.babyName?.let { name ->
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = customColors.accentGradientStart.copy(0.1f)
                        ) {
                            Text(
                                "👶 $name",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style    = MaterialTheme.typography.labelSmall,
                                color    = customColors.accentGradientStart
                            )
                        }
                    }

                    // Relative time
                    Text(
                        text = formatRelativeTime(
                            isoDateTime = notification.createdAt,
                            strings     = RelativeTimeStrings(
                                justNow  = stringResource(Res.string.relative_time_just_now),
                                minsAgo  = stringResource(Res.string.relative_time_minutes),
                                hoursAgo = stringResource(Res.string.relative_time_hours),
                                daysAgo  = stringResource(Res.string.relative_time_days)
                            )
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.4f)
                    )
                }

                // Action button if present
                notification.actionLabel?.let { label ->
                    Spacer(Modifier.height(dimensions.spacingXSmall))
                    TextButton(
                        onClick        = onTap,
                        contentPadding = PaddingValues(horizontal = 0.dp),
                        colors         = ButtonDefaults.textButtonColors(
                            contentColor = customColors.accentGradientStart
                        )
                    ) {
                        Text(
                            label,
                            style      = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.Default.ChevronRight, null, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Empty / Loading / Error states
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun NotificationLoadingState(dimensions: Dimensions) {
    Column(
        modifier            = Modifier.fillMaxSize().padding(dimensions.screenPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        repeat(5) {
            NotificationSkeletonCard(dimensions)
            Spacer(Modifier.height(dimensions.spacingSmall))
        }
    }
}

@Composable
private fun NotificationSkeletonCard(dimensions: Dimensions) {
    val shimmer = rememberInfiniteTransition(label = "sk")
    val alpha by shimmer.animateFloat(
        initialValue = 0.15f,
        targetValue  = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900)
        ),
        label = "sk_alpha"
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(88.dp)
            .clip(RoundedCornerShape(dimensions.cardCornerRadius))
            .background(MaterialTheme.colorScheme.onBackground.copy(alpha))
    )
}

@Composable
private fun NotificationEmptyState(
    filter     : NotificationFilter,
    unreadOnly : Boolean,
    dimensions : Dimensions
) {
    Box(Modifier.fillMaxSize(), Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🔔", style = MaterialTheme.typography.displaySmall)
            Spacer(Modifier.height(dimensions.spacingMedium))
            Text(
                text       = if (unreadOnly) "No unread notifications"
                else if (filter == NotificationFilter.ALL) "No notifications yet"
                else "No ${filter.label.lowercase()} notifications",
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(dimensions.spacingSmall))
            Text(
                text  = "You're all caught up!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(0.55f)
            )
        }
    }
}

@Composable
private fun NotificationErrorState(
    message    : String,
    onRetry    : () -> Unit,
    dimensions : Dimensions
) {
    Box(Modifier.fillMaxSize(), Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("⚠️", style = MaterialTheme.typography.displaySmall)
            Spacer(Modifier.height(dimensions.spacingMedium))
            Text(message, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(0.7f))
            Spacer(Modifier.height(dimensions.spacingMedium))
            Button(onClick = onRetry) { Text("Retry") }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Helpers — platform-specific date implementations expected
// ─────────────────────────────────────────────────────────────────────────────

expect fun getCurrentDate(): String
expect fun getYesterdayDate(): String
expect fun formatDisplayDate(iso: String): String
expect fun formatRelativeTime(isoDateTime: String, strings: RelativeTimeStrings): String

data class RelativeTimeStrings(
    val justNow : String,
    val minsAgo : String,   // use %1$d for the number
    val hoursAgo: String,
    val daysAgo : String,
)