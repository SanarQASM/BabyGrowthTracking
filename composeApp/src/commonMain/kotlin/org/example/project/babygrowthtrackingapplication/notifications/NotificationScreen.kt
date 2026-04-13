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
import babygrowthtrackingapplication.composeapp.generated.resources.notif_back_cd
import babygrowthtrackingapplication.composeapp.generated.resources.notif_baby_badge
import babygrowthtrackingapplication.composeapp.generated.resources.notif_date_today
import babygrowthtrackingapplication.composeapp.generated.resources.notif_date_yesterday
import babygrowthtrackingapplication.composeapp.generated.resources.notif_delete_cd
import babygrowthtrackingapplication.composeapp.generated.resources.notif_empty_caught_up
import babygrowthtrackingapplication.composeapp.generated.resources.notif_empty_icon
import babygrowthtrackingapplication.composeapp.generated.resources.notif_empty_no_category
import babygrowthtrackingapplication.composeapp.generated.resources.notif_empty_no_notifications
import babygrowthtrackingapplication.composeapp.generated.resources.notif_empty_no_unread
import babygrowthtrackingapplication.composeapp.generated.resources.notif_error_icon
import babygrowthtrackingapplication.composeapp.generated.resources.notif_error_retry
import babygrowthtrackingapplication.composeapp.generated.resources.notif_filter_unread_only
import babygrowthtrackingapplication.composeapp.generated.resources.notif_mark_all_read
import babygrowthtrackingapplication.composeapp.generated.resources.notif_screen_title
import babygrowthtrackingapplication.composeapp.generated.resources.notif_unread_badge_max
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

    val screenTitle     = stringResource(Res.string.notif_screen_title)
    val badgeMax        = stringResource(Res.string.notif_unread_badge_max)
    val markAllRead     = stringResource(Res.string.notif_mark_all_read)
    val backCd          = stringResource(Res.string.notif_back_cd)

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
                            screenTitle,
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color      = customColors.accentGradientStart
                        )
                        if (state.unreadCount > 0) {
                            Badge(containerColor = customColors.accentGradientStart) {
                                Text(
                                    if (state.unreadCount > 99) badgeMax
                                    else state.unreadCount.toString(),
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
                            contentDescription = backCd,
                            tint = customColors.accentGradientStart
                        )
                    }
                },
                actions = {
                    if (state.unreadCount > 0) {
                        TextButton(onClick = viewModel::markAllAsRead) {
                            Text(
                                markAllRead,
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
    val unreadOnlyLabel = stringResource(Res.string.notif_filter_unread_only)

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
            label    = { Text(unreadOnlyLabel, style = MaterialTheme.typography.labelMedium) },
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
            modifier   = Modifier.fillMaxWidth(),
            color      = customColors.accentGradientStart,
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
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(dimensions.spacingMedium),
                    Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(dimensions.iconMedium),
                        color       = customColors.accentGradientStart,
                        strokeWidth = dimensions.borderWidthThin
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
    val todayLabel     = stringResource(Res.string.notif_date_today)
    val yesterdayLabel = stringResource(Res.string.notif_date_yesterday)

    val label = when (date) {
        getCurrentDate()   -> todayLabel
        getYesterdayDate() -> yesterdayLabel
        else               -> formatDisplayDate(date)
    }
    Text(
        text       = label,
        style      = MaterialTheme.typography.labelMedium,
        color      = MaterialTheme.colorScheme.onBackground.copy(0.5f),
        fontWeight = FontWeight.SemiBold,
        modifier   = Modifier.padding(
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

    val deleteCd = stringResource(Res.string.notif_delete_cd)

    SwipeToDismissBox(
        state                        = dismissState,
        enableDismissFromStartToEnd  = false,
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
                    contentDescription = deleteCd,
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
    val isUnread  = !notification.isRead
    val iconEmoji = notificationCategoryEmoji(notification.category)

    val priorityColor = when (notification.priority) {
        "URGENT" -> MaterialTheme.colorScheme.error
        "HIGH"   -> customColors.warning
        "MEDIUM" -> customColors.accentGradientStart
        "LOW"    -> MaterialTheme.colorScheme.onSurface.copy(0.4f)
        else     -> MaterialTheme.colorScheme.onSurface.copy(0.4f)
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
            defaultElevation = if (isUnread) dimensions.borderWidthMedium else dimensions.borderWidthThin
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
                    .width(dimensions.borderWidthMedium + dimensions.borderWidthThin)
                    .height(dimensions.avatarMedium)
                    .clip(RoundedCornerShape(50))
                    .background(if (isUnread) priorityColor else Color.Transparent)
            )

            // Emoji icon box
            Box(
                modifier = Modifier
                    .size(dimensions.addButtonSize + dimensions.spacingXSmall)
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
                                .padding(
                                    start = dimensions.spacingSmall,
                                    top   = dimensions.borderWidthMedium
                                )
                                .size(dimensions.spacingSmall)
                                .clip(CircleShape)
                                .background(customColors.accentGradientStart)
                        )
                    }
                }

                Spacer(Modifier.height(dimensions.borderWidthMedium))

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
                                // Pass the name directly into the stringResource function
                                text = stringResource(Res.string.notif_baby_badge, name),
                                modifier = Modifier.padding(
                                    horizontal = dimensions.spacingSmall,
                                    vertical   = dimensions.borderWidthMedium
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                color = customColors.accentGradientStart
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
                        Spacer(Modifier.width(dimensions.spacingXSmall))
                        Icon(
                            Icons.Default.ChevronRight,
                            null,
                            modifier = Modifier.size(dimensions.iconSmall - dimensions.borderWidthMedium)
                        )
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
        initialValue  = 0.15f,
        targetValue   = 0.35f,
        animationSpec = infiniteRepeatable(animation = tween(durationMillis = 900)),
        label         = "sk_alpha"
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(dimensions.avatarLarge + dimensions.spacingXLarge)
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
    val emptyIcon         = stringResource(Res.string.notif_empty_icon)
    val noUnread          = stringResource(Res.string.notif_empty_no_unread)
    val noNotifications   = stringResource(Res.string.notif_empty_no_notifications)
    val caughtUp          = stringResource(Res.string.notif_empty_caught_up)

    val headlineText = when {
        unreadOnly -> noUnread
        filter == NotificationFilter.ALL -> noNotifications
        else -> stringResource(
            Res.string.notif_empty_no_category,
            filter.label.lowercase()
        )
    }

    Box(Modifier.fillMaxSize(), Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emptyIcon, style = MaterialTheme.typography.displaySmall)
            Spacer(Modifier.height(dimensions.spacingMedium))
            Text(
                text       = headlineText,
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(dimensions.spacingSmall))
            Text(
                text  = caughtUp,
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
    val errorIcon  = stringResource(Res.string.notif_error_icon)
    val retryLabel = stringResource(Res.string.notif_error_retry)

    Box(Modifier.fillMaxSize(), Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(errorIcon, style = MaterialTheme.typography.displaySmall)
            Spacer(Modifier.height(dimensions.spacingMedium))
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(0.7f)
            )
            Spacer(Modifier.height(dimensions.spacingMedium))
            Button(onClick = onRetry) { Text(retryLabel) }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Shared helper — category → emoji mapping (single source of truth)
// ─────────────────────────────────────────────────────────────────────────────

internal fun notificationCategoryEmoji(category: String): String =
    when (category) {
        "VACCINATION"  -> "💉"
        "GROWTH"       -> "📏"
        "APPOINTMENT"  -> "📅"
        "HEALTH"       -> "❤️"
        "DEVELOPMENT"  -> "🧠"
        "BABY_PROFILE" -> "👶"
        "MEMORIES"     -> "📸"
        "ACCOUNT"      -> "🔐"
        "SYSTEM"       -> "⚙️"
        else           -> "🔔"
    }

// ─────────────────────────────────────────────────────────────────────────────
// Helpers — platform-specific date implementations expected
// ─────────────────────────────────────────────────────────────────────────────

expect fun getCurrentDate(): String
expect fun getYesterdayDate(): String
expect fun formatDisplayDate(iso: String): String
expect fun formatRelativeTime(isoDateTime: String, strings: RelativeTimeStrings): String

data class RelativeTimeStrings(
    val justNow  : String,
    val minsAgo  : String,   // use %1$d for the number
    val hoursAgo : String,
    val daysAgo  : String,
)