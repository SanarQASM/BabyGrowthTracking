// File: composeApp/src/commonMain/kotlin/org/example/project/babygrowthtrackingapplication/chat/ChatScreen.kt
package org.example.project.babygrowthtrackingapplication.chat

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.babygrowthtrackingapplication.theme.*

// ─────────────────────────────────────────────────────────────────────────────
// ChatScreen
//
// Root composable for the group chat feature.
// Receives a fully-initialised ChatViewModel and renders:
//   • Top app bar with back navigation and group info
//   • Scrollable message list (newest at bottom via reverseLayout)
//   • Compose bar with send button and character counter
//   • Delete confirmation dialog
//   • Moderation / network error banners
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ChatScreen(
    viewModel : ChatViewModel,
    onBack    : () -> Unit
) {
    val state      = viewModel.uiState
    val dimensions = LocalDimensions.current
    val colors     = MaterialTheme.customColors
    val listState  = rememberLazyListState()

    // Trigger initial load when screen enters composition
    LaunchedEffect(Unit) {
        viewModel.onEnter()
    }

    // Stop polling when screen leaves composition
    DisposableEffect(Unit) {
        onDispose { viewModel.onExit() }
    }

    // Auto-scroll to bottom when a new message arrives at the top of the list
    LaunchedEffect(state.messages.firstOrNull()?.messageId) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    // ── Load more when the user scrolls near the top (index 0 = newest = bottom) ──
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible >= state.messages.size - 5 && state.hasMore && !state.isLoadingMore
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) viewModel.loadMoreHistory()
    }

    // ── Delete confirmation dialog ────────────────────────────────────────────
    state.pendingDeleteId?.let {
        DeleteConfirmDialog(
            onConfirm = viewModel::confirmDelete,
            onDismiss = viewModel::cancelDelete,
            dimensions = dimensions
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ChatTopBar(
                onBack     = onBack,
                colors     = colors,
                dimensions = dimensions
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Error / moderation banner ─────────────────────────────────────
            AnimatedVisibility(
                visible = state.errorMessage != null,
                enter   = slideInVertically() + fadeIn(),
                exit    = slideOutVertically() + fadeOut()
            ) {
                ErrorBanner(
                    message    = state.errorMessage ?: "",
                    onDismiss  = viewModel::clearError,
                    isWarning  = false,
                    dimensions = dimensions
                )
            }

            // ── Message list ──────────────────────────────────────────────────
            Box(modifier = Modifier.weight(1f)) {
                if (state.isLoadingHistory) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = colors.accentGradientStart)
                    }
                } else if (state.messages.isEmpty()) {
                    EmptyChat(colors = colors, dimensions = dimensions)
                } else {
                    LazyColumn(
                        state         = listState,
                        modifier      = Modifier.fillMaxSize(),
                        reverseLayout = true,           // newest message visible at bottom
                        contentPadding = PaddingValues(
                            horizontal = dimensions.spacingSmall,
                            vertical   = dimensions.spacingSmall
                        ),
                        verticalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall)
                    ) {
                        items(
                            items = state.messages,
                            key   = { it.messageId }
                        ) { msg ->
                            ChatBubble(
                                message    = msg,
                                onDelete   = { viewModel.requestDelete(msg.messageId) },
                                colors     = colors,
                                dimensions = dimensions
                            )
                        }

                        // Loading more indicator at the very bottom of the reversed list
                        if (state.isLoadingMore) {
                            item {
                                Box(
                                    modifier         = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = dimensions.spacingSmall),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier    = Modifier.size(dimensions.iconMedium),
                                        color       = colors.accentGradientStart,
                                        strokeWidth = dimensions.borderWidthMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── Compose bar ───────────────────────────────────────────────────
            ComposeBar(
                draft           = state.draftText,
                onDraftChange   = viewModel::onDraftChanged,
                onSend          = viewModel::sendMessage,
                isSending       = state.isSending,
                moderationError = state.moderationError,
                colors          = colors,
                dimensions      = dimensions
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ChatTopBar
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatTopBar(
    onBack     : () -> Unit,
    colors     : CustomColors,
    dimensions : Dimensions
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
            ) {
                // Group avatar
                Box(
                    modifier = Modifier
                        .size(dimensions.avatarSmall)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    colors.accentGradientStart.copy(alpha = 0.85f),
                                    colors.accentGradientEnd.copy(alpha = 0.85f)
                                )
                            ),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text     = "👪",
                        fontSize = (dimensions.avatarSmall.value * 0.5f).sp
                    )
                }
                Column {
                    Text(
                        text       = "Parents Community",
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text  = "Group · Text only",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint               = colors.accentGradientStart
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor    = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// ChatBubble
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ChatBubble(
    message    : ChatMessageUi,
    onDelete   : () -> Unit,
    colors     : CustomColors,
    dimensions : Dimensions
) {
    val isOwn  = message.isOwnMessage
    val isAdmin = message.isAdmin

    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isOwn) Arrangement.End else Arrangement.Start,
        verticalAlignment     = Alignment.Bottom
    ) {
        // ── Sender avatar (only for others' messages) ─────────────────────────
        if (!isOwn) {
            SenderAvatar(
                initials   = message.initials,
                avatarUrl  = message.senderAvatar,
                isAdmin    = isAdmin,
                size       = dimensions.avatarSmall,
                colors     = colors
            )
            Spacer(Modifier.width(dimensions.spacingXSmall))
        }

        // ── Bubble ────────────────────────────────────────────────────────────
        Column(
            modifier              = Modifier.widthIn(max = 280.dp),
            horizontalAlignment   = if (isOwn) Alignment.End else Alignment.Start
        ) {
            // Sender name (hidden for own messages)
            if (!isOwn) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall)
                ) {
                    Text(
                        text       = message.senderName,
                        style      = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color      = if (isAdmin) colors.warning
                        else       colors.accentGradientStart,
                        maxLines   = 1,
                        overflow   = TextOverflow.Ellipsis
                    )
                    if (isAdmin) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = colors.warning.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text     = "Admin",
                                style    = MaterialTheme.typography.labelSmall,
                                color    = colors.warning,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(2.dp))
            }

            // Bubble body + long-press delete menu
            Box {
                Surface(
                    shape  = bubbleShape(isOwn),
                    color  = if (isOwn) {
                        colors.accentGradientStart.copy(alpha = 0.90f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                    modifier = Modifier.combinedClickable(
                        onClick      = {},
                        onLongClick  = { showMenu = true }
                    )
                ) {
                    Text(
                        text     = message.content,
                        style    = MaterialTheme.typography.bodyMedium,
                        color    = if (isOwn) Color.White
                        else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(
                            horizontal = dimensions.spacingMedium,
                            vertical   = dimensions.spacingSmall
                        )
                    )
                }

                // Context menu for delete (only author can see on own, admin sees on all)
                DropdownMenu(
                    expanded         = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text  = "Delete message",
                                color = MaterialTheme.colorScheme.error
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        },
                        onClick = {
                            showMenu = false
                            onDelete()
                        }
                    )
                }
            }

            // Timestamp
            Text(
                text     = formatChatTime(message.sentAt),
                style    = MaterialTheme.typography.labelSmall,
                color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.40f),
                modifier = Modifier.padding(
                    top   = 2.dp,
                    start = if (isOwn) 0.dp else dimensions.spacingXSmall,
                    end   = if (isOwn) dimensions.spacingXSmall else 0.dp
                )
            )
        }

        // ── Own message avatar placeholder ────────────────────────────────────
        if (isOwn) {
            Spacer(Modifier.width(dimensions.spacingXSmall))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SenderAvatar
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SenderAvatar(
    initials  : String,
    avatarUrl : String?,
    isAdmin   : Boolean,
    size      : androidx.compose.ui.unit.Dp,
    colors    : CustomColors
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(
                if (isAdmin) colors.warning.copy(alpha = 0.20f)
                else         colors.accentGradientStart.copy(alpha = 0.15f)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text       = initials,
            style      = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color      = if (isAdmin) colors.warning else colors.accentGradientStart,
            fontSize   = (size.value * 0.38f).sp
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ComposeBar
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ComposeBar(
    draft           : String,
    onDraftChange   : (String) -> Unit,
    onSend          : () -> Unit,
    isSending       : Boolean,
    moderationError : String?,
    colors          : CustomColors,
    dimensions      : Dimensions
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(
                horizontal = dimensions.spacingSmall,
                vertical   = dimensions.spacingXSmall
            )
    ) {
        // Moderation / input error
        AnimatedVisibility(
            visible = moderationError != null,
            enter   = fadeIn() + expandVertically(),
            exit    = fadeOut() + shrinkVertically()
        ) {
            ErrorBanner(
                message    = moderationError ?: "",
                onDismiss  = { onDraftChange(draft) }, // keeps draft but clears error
                isWarning  = true,
                dimensions = dimensions
            )
        }

        Row(
            modifier          = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall)
        ) {
            // Text field
            OutlinedTextField(
                value         = draft,
                onValueChange = { if (it.length <= 1_000) onDraftChange(it) },
                placeholder   = {
                    Text(
                        "Type a message…",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                },
                modifier      = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp, max = 140.dp),
                maxLines      = 5,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction      = ImeAction.Default
                ),
                keyboardActions = KeyboardActions(
                    onSend = { onSend() }
                ),
                shape  = RoundedCornerShape(dimensions.buttonCornerRadius),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = colors.accentGradientStart,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedTextColor     = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor   = MaterialTheme.colorScheme.onSurface,
                    cursorColor          = colors.accentGradientStart
                )
            )

            // Send button
            val canSend = draft.isNotBlank() && !isSending
            IconButton(
                onClick  = { if (canSend) onSend() },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (canSend) colors.accentGradientStart
                        else         MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                    )
            ) {
                if (isSending) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(20.dp),
                        color       = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector        = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint               = if (canSend) Color.White
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                        modifier           = Modifier.size(dimensions.iconSmall)
                    )
                }
            }
        }

        // Character counter
        if (draft.length > 800) {
            Text(
                text      = "${draft.length}/1000",
                style     = MaterialTheme.typography.labelSmall,
                color     = if (draft.length >= 1000) MaterialTheme.colorScheme.error
                else       MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier  = Modifier
                    .fillMaxWidth()
                    .padding(end = dimensions.spacingXSmall),
                textAlign = TextAlign.End
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ErrorBanner
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ErrorBanner(
    message    : String,
    onDismiss  : () -> Unit,
    isWarning  : Boolean,
    dimensions : Dimensions
) {
    val bgColor   = if (isWarning) MaterialTheme.colorScheme.tertiaryContainer
    else           MaterialTheme.colorScheme.errorContainer
    val textColor = if (isWarning) MaterialTheme.colorScheme.onTertiaryContainer
    else           MaterialTheme.colorScheme.onErrorContainer
    val icon      = if (isWarning) Icons.Default.Warning else Icons.Default.Error

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = dimensions.spacingSmall,
                vertical   = dimensions.spacingXSmall
            ),
        shape = RoundedCornerShape(dimensions.spacingSmall),
        color = bgColor
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = dimensions.spacingSmall,
                    vertical   = dimensions.spacingXSmall
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall)
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = textColor,
                modifier           = Modifier.size(dimensions.iconSmall)
            )
            Text(
                text     = message,
                style    = MaterialTheme.typography.bodySmall,
                color    = textColor,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick  = onDismiss,
                modifier = Modifier.size(dimensions.iconMedium)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint               = textColor,
                    modifier           = Modifier.size(dimensions.iconSmall)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// DeleteConfirmDialog
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun DeleteConfirmDialog(
    onConfirm  : () -> Unit,
    onDismiss  : () -> Unit,
    dimensions : Dimensions
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Delete,
                contentDescription = null,
                tint     = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(dimensions.iconLarge)
            )
        },
        title = {
            Text(
                text       = "Delete message?",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text  = "This message will be permanently removed for everyone.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors  = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Delete", color = MaterialTheme.colorScheme.onError)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// EmptyChat
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun EmptyChat(
    colors     : CustomColors,
    dimensions : Dimensions
) {
    Column(
        modifier            = Modifier
            .fillMaxSize()
            .padding(dimensions.screenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("💬", fontSize = 56.sp)
        Spacer(Modifier.height(dimensions.spacingMedium))
        Text(
            text       = "No messages yet",
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color      = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(dimensions.spacingSmall))
        Text(
            text      = "Be the first to say hello to the parents community!",
            style     = MaterialTheme.typography.bodyMedium,
            color     = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
            textAlign = TextAlign.Center
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────────────────────────────────────

/** Rounded bubble corners — sharper on the sender's side */
private fun bubbleShape(isOwn: Boolean): RoundedCornerShape {
    val r = 18.dp
    val s = 4.dp
    return if (isOwn) {
        RoundedCornerShape(topStart = r, topEnd = r, bottomStart = r, bottomEnd = s)
    } else {
        RoundedCornerShape(topStart = s, topEnd = r, bottomStart = r, bottomEnd = r)
    }
}

/**
 * Format an ISO LocalDateTime string into a human-readable chat timestamp.
 * e.g. "2025-05-07T14:32:00" → "2:32 PM"
 * Falls back to the raw string on parse failure.
 */
fun formatChatTime(isoString: String): String {
    return try {
        // Take the time portion only (HH:mm:ss)
        val timePart = isoString.substringAfter("T").substringBefore(".")
        val parts    = timePart.split(":")
        if (parts.size < 2) return isoString
        val hour24   = parts[0].toInt()
        val minute   = parts[1]
        val amPm     = if (hour24 < 12) "AM" else "PM"
        val hour12   = when {
            hour24 == 0  -> 12
            hour24 > 12  -> hour24 - 12
            else         -> hour24
        }
        "$hour12:$minute $amPm"
    } catch (_: Exception) {
        isoString
    }
}