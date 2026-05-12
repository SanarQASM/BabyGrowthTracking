// File: composeApp/src/commonMain/kotlin/org/example/project/babygrowthtrackingapplication/chat/ChatViewModel.kt
package org.example.project.babygrowthtrackingapplication.chat

import androidx.compose.runtime.*
import kotlinx.coroutines.*
import org.example.project.babygrowthtrackingapplication.data.network.ApiResult
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

// ─────────────────────────────────────────────────────────────────────────────
// ChatViewModel
//
// Responsibilities:
//   • Load paginated history (oldest displayed at bottom, newest at top)
//   • Send messages (with optimistic insert + rollback on failure)
//   • Soft-delete messages
//   • Long-poll for new messages every POLL_INTERVAL_MS milliseconds
//   • Expose ChatUiState to the composable layer
// ─────────────────────────────────────────────────────────────────────────────

class ChatViewModel(
    private val repository : ChatRepository,
    private val getUserId  : () -> String?,
    private val getUserRole: () -> String?   // "PARENT" | "ADMIN" | "VACCINATION_TEAM"
) {

    var uiState by mutableStateOf(ChatUiState())
        private set

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var pollJob: Job? = null

    companion object {
        private const val POLL_INTERVAL_MS = 4_000L
        private const val PAGE_SIZE        = 40
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Lifecycle
    // ─────────────────────────────────────────────────────────────────────────

    /** Call when the chat screen becomes visible. */
    fun onEnter() {
        loadHistory(refresh = true)
        startPolling()
    }

    /** Call when the chat screen is dismissed. */
    fun onExit() {
        pollJob?.cancel()
        pollJob = null
    }

    fun onDestroy() {
        scope.cancel()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Load history
    // ─────────────────────────────────────────────────────────────────────────

    fun loadHistory(refresh: Boolean = false) {
        if (refresh) {
            uiState = uiState.copy(
                isLoadingHistory = true,
                currentPage      = 0,
                errorMessage     = null
            )
        } else {
            if (uiState.isLoadingMore || !uiState.hasMore) return
            uiState = uiState.copy(isLoadingMore = true)
        }

        scope.launch {
            val page = if (refresh) 0 else uiState.currentPage + 1
            when (val result = repository.getMessages(page = page, size = PAGE_SIZE)) {
                is ApiResult.Success -> {
                    val incoming = result.data.messages.map { it.toUi() }
                    // History arrives newest-first from server; we maintain
                    // the list as newest-first and let LazyColumn reverseLayout
                    val merged = if (refresh) {
                        incoming
                    } else {
                        uiState.messages + incoming   // append older messages
                    }
                    uiState = uiState.copy(
                        messages         = merged,
                        isLoadingHistory = false,
                        isLoadingMore    = false,
                        hasMore          = result.data.hasMore,
                        currentPage      = page,
                        // Initialise poll timestamp from newest message if not set
                        lastPollTimestamp = uiState.lastPollTimestamp
                            ?: merged.firstOrNull()?.sentAt
                    )
                }
                is ApiResult.Error -> {
                    uiState = uiState.copy(
                        isLoadingHistory = false,
                        isLoadingMore    = false,
                        errorMessage     = result.message
                    )
                }
                else -> uiState = uiState.copy(isLoadingHistory = false, isLoadingMore = false)
            }
        }
    }

    fun loadMoreHistory() = loadHistory(refresh = false)

    // ─────────────────────────────────────────────────────────────────────────
    // Draft text
    // ─────────────────────────────────────────────────────────────────────────

    fun onDraftChanged(text: String) {
        uiState = uiState.copy(
            draftText       = text,
            moderationError = null  // clear previous error when user starts typing
        )
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Send
    // ─────────────────────────────────────────────────────────────────────────

    @OptIn(ExperimentalTime::class)
    fun sendMessage() {
        val text = uiState.draftText.trim()
        if (text.isBlank() || uiState.isSending) return

        // ── Optimistic insert ─────────────────────────────────────────────────
        val optimisticId = "optimistic_${Clock.System.now().toEpochMilliseconds()}"
        val optimistic   = ChatMessageUi(
            messageId    = optimisticId,
            senderId     = getUserId() ?: "",
            senderName   = "You",
            senderAvatar = null,
            senderRole   = getUserRole() ?: "PARENT",
            content      = text,
            sentAt       = nowIso(),
            isOwnMessage = true,
            isDeleted    = false
        )
        uiState = uiState.copy(
            messages  = listOf(optimistic) + uiState.messages,
            draftText = "",
            isSending = true
        )

        scope.launch {
            when (val result = repository.sendMessage(text)) {
                is ApiResult.Success -> {
                    // Replace optimistic with the confirmed message from server
                    val confirmed = result.data.toUi()
                    uiState = uiState.copy(
                        messages = uiState.messages.map {
                            if (it.messageId == optimisticId) confirmed else it
                        },
                        isSending        = false,
                        lastPollTimestamp = confirmed.sentAt
                    )
                }
                is ApiResult.Error -> {
                    // 422 = moderation block — show inline error, restore draft
                    val isModeration = result.code == 422
                    uiState = uiState.copy(
                        // Remove the optimistic message on failure
                        messages        = uiState.messages.filter { it.messageId != optimisticId },
                        draftText       = if (isModeration) text else "",
                        isSending       = false,
                        moderationError = if (isModeration) result.message else null,
                        errorMessage    = if (!isModeration) result.message else null
                    )
                }
                else -> uiState = uiState.copy(isSending = false)
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Delete
    // ─────────────────────────────────────────────────────────────────────────

    fun requestDelete(messageId: String) {
        uiState = uiState.copy(pendingDeleteId = messageId)
    }

    fun cancelDelete() {
        uiState = uiState.copy(pendingDeleteId = null)
    }

    fun confirmDelete() {
        val id = uiState.pendingDeleteId ?: return
        uiState = uiState.copy(pendingDeleteId = null)

        scope.launch {
            when (repository.deleteMessage(id)) {
                is ApiResult.Success -> {
                    // Remove from list (soft-deleted; no need to mark isDeleted=true in UI)
                    uiState = uiState.copy(
                        messages = uiState.messages.filter { it.messageId != id }
                    )
                }
                is ApiResult.Error -> {
                    uiState = uiState.copy(errorMessage = "Could not delete message. Please try again.")
                }
                else -> Unit
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Polling
    // ─────────────────────────────────────────────────────────────────────────

    private fun startPolling() {
        pollJob?.cancel()
        pollJob = scope.launch {
            while (isActive) {
                delay(POLL_INTERVAL_MS)
                pollNewMessages()
            }
        }
    }

    private suspend fun pollNewMessages() {
        val since = uiState.lastPollTimestamp ?: return
        when (val result = repository.pollSince(since)) {
            is ApiResult.Success -> {
                val newMsgs = result.data.map { it.toUi() }
                if (newMsgs.isEmpty()) return

                // De-duplicate — the sender's own optimistic message may already be in the list
                val existingIds = uiState.messages.map { it.messageId }.toSet()
                val fresh = newMsgs.filter { it.messageId !in existingIds }
                if (fresh.isEmpty()) return

                uiState = uiState.copy(
                    // Prepend newest messages (list is newest-first)
                    messages          = fresh + uiState.messages,
                    lastPollTimestamp = fresh.maxByOrNull { it.sentAt }?.sentAt ?: since
                )
            }
            else -> { /* silently ignore poll errors */ }
        }
    }

    fun clearError() {
        uiState = uiState.copy(errorMessage = null, moderationError = null)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helper — current time as ISO string (platform-independent)
    // ─────────────────────────────────────────────────────────────────────────

    @OptIn(ExperimentalTime::class)
    private fun nowIso(): String = Clock.System.now().toString()
}