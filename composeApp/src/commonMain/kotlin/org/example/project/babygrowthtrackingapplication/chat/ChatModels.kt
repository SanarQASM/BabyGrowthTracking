// File: composeApp/src/commonMain/kotlin/org/example/project/babygrowthtrackingapplication/chat/ChatModels.kt
package org.example.project.babygrowthtrackingapplication.chat

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

// ─────────────────────────────────────────────────────────────────────────────
// Network DTOs — mirror ChatController response shapes exactly
// ─────────────────────────────────────────────────────────────────────────────

@Serializable
data class ChatMessageNet(
    @SerialName("messageId")    val messageId    : String,
    @SerialName("senderId")     val senderId     : String,
    @SerialName("senderName")   val senderName   : String,
    @SerialName("senderAvatar") val senderAvatar : String? = null,
    @SerialName("senderRole")   val senderRole   : String,
    @SerialName("content")      val content      : String,
    @SerialName("sentAt")       val sentAt       : String,
    @SerialName("isOwnMessage") val isOwnMessage : Boolean,
    @SerialName("isDeleted")    val isDeleted    : Boolean = false
)

@Serializable
data class ChatPageNet(
    @SerialName("messages")    val messages   : List<ChatMessageNet>,
    @SerialName("page")        val page       : Int,
    @SerialName("totalPages")  val totalPages : Int,
    @SerialName("hasMore")     val hasMore    : Boolean
)

@Serializable
data class SendMessageRequest(
    @SerialName("content") val content: String
)

// ─────────────────────────────────────────────────────────────────────────────
// UI model — what the composable layer consumes
// ─────────────────────────────────────────────────────────────────────────────

data class ChatMessageUi(
    val messageId    : String,
    val senderId     : String,
    val senderName   : String,
    val senderAvatar : String?,
    val senderRole   : String,      // "PARENT" | "ADMIN"
    val content      : String,
    val sentAt       : String,      // raw ISO string; formatted in the composable
    val isOwnMessage : Boolean,
    val isDeleted    : Boolean
) {
    val isAdmin: Boolean get() = senderRole.equals("ADMIN", ignoreCase = true)
    /** Initials shown in avatar when no photo URL is available. */
    val initials: String get() = senderName
        .split(" ")
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")
        .ifBlank { "?" }
}

// ─────────────────────────────────────────────────────────────────────────────
// UI state — drives the ChatScreen composable
// ─────────────────────────────────────────────────────────────────────────────

data class ChatUiState(
    val messages         : List<ChatMessageUi> = emptyList(),
    val draftText        : String              = "",
    val isSending        : Boolean             = false,
    val isLoadingHistory : Boolean             = false,
    val isLoadingMore    : Boolean             = false,
    val hasMore          : Boolean             = false,
    val currentPage      : Int                 = 0,
    val errorMessage     : String?             = null,
    val moderationError  : String?             = null,  // inline error under input
    val lastPollTimestamp: String?             = null,  // ISO; used by polling loop
    val pendingDeleteId  : String?             = null   // confirmation dialog target
)

// ─────────────────────────────────────────────────────────────────────────────
// Mapper
// ─────────────────────────────────────────────────────────────────────────────

fun ChatMessageNet.toUi() = ChatMessageUi(
    messageId    = messageId,
    senderId     = senderId,
    senderName   = senderName,
    senderAvatar = senderAvatar,
    senderRole   = senderRole,
    content      = content,
    sentAt       = sentAt,
    isOwnMessage = isOwnMessage,
    isDeleted    = isDeleted
)