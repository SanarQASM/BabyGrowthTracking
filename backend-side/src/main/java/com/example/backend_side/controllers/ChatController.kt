// File: backend-side/src/main/java/com/example/backend_side/controllers/ChatController.kt
package com.example.backend_side.controllers

import com.example.backend_side.*
import com.example.backend_side.entity.ChatMessage
import com.example.backend_side.entity.UserRole
import com.example.backend_side.repositories.ChatMessageRepository
import com.example.backend_side.repositories.UserRepository
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import java.util.UUID

// ─────────────────────────────────────────────────────────────────────────────
// ChatController
//
// Base path : /v1/chat
//
// Access rules (enforced at the service layer):
//   • GET  /messages         — PARENT or ADMIN
//   • POST /messages         — PARENT or ADMIN
//   • DELETE /messages/{id}  — author (any role) OR ADMIN
//   • GET  /messages/poll    — PARENT or ADMIN  (long-poll for new messages)
//
// The group is global — one room for all parents + admin.
// No images, videos, or audio; plain text only.
// Content passes through ContentModerationService before persistence.
// ─────────────────────────────────────────────────────────────────────────────

@RestController
@RequestMapping("/v1/chat")
@CrossOrigin(origins = ["*"])
@Tag(name = "Group Chat", description = "Parent group chat — text only, moderated")
class ChatController(
    private val chatMessageRepository  : ChatMessageRepository,
    private val userRepository         : UserRepository,
    private val contentModerationService: ContentModerationService
) {

    // ── GET /v1/chat/messages?page=0&size=40 ──────────────────────────────────
    // Returns messages newest-first (the client reverses for display).
    // Accessible by PARENT and ADMIN roles.
    @GetMapping("/messages")
    @Operation(summary = "Fetch paginated group-chat messages (newest first)")
    fun getMessages(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestParam(defaultValue = "0")  page: Int,
        @RequestParam(defaultValue = "40") size: Int
    ): ResponseEntity<ApiResponse<ChatPageResponse>> {
        val caller = resolveUser(userDetails) ?: return unauthorisedResponse()
        requireParentOrAdmin(caller.role.name) ?: return forbiddenResponse()

        val clampedSize = size.coerceIn(1, 100)
        val pageable    = PageRequest.of(page, clampedSize, Sort.by("sentAt").descending())
        val pageResult  = chatMessageRepository.findActivePaged(pageable)

        val response = ChatPageResponse(
            messages  = pageResult.content.map { it.toDto(caller.userId) },
            page      = pageResult.number,
            totalPages = pageResult.totalPages,
            hasMore   = !pageResult.isLast
        )
        return ResponseEntity.ok(ApiResponse(true, "Messages retrieved", response))
    }

    // ── POST /v1/chat/messages ────────────────────────────────────────────────
    // Accessible by PARENT and ADMIN.
    // Moderates content before saving.
    @PostMapping("/messages")
    @Transactional
    @Operation(summary = "Send a message to the group chat")
    fun sendMessage(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestBody request: SendChatMessageRequest
    ): ResponseEntity<ApiResponse<ChatMessageDto>> {
        val caller = resolveUser(userDetails) ?: return unauthorisedResponse()
        requireParentOrAdmin(caller.role.name) ?: return forbiddenResponse()

        // ── Content moderation ────────────────────────────────────────────────
        val modResult = contentModerationService.moderate(request.content)
        if (modResult.blocked) {
            val userFacingMessage = when (modResult.reason) {
                ModerationReason.PROFANITY -> "Your message contains inappropriate language and was not sent."
                ModerationReason.THREAT    -> "Your message contains threatening content and was not sent."
                ModerationReason.TOO_LONG  -> "Your message is too long. Please keep it under 1,000 characters."
                ModerationReason.SPAM      -> "Your message appears to be spam."
                ModerationReason.EMPTY     -> "Message cannot be empty."
                ModerationReason.CLEAN     -> "Message was blocked."
            }
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiResponse(false, userFacingMessage))
        }

        val message = ChatMessage(
            messageId = UUID.randomUUID().toString(),
            sender    = caller,
            content   = request.content.trim(),
            sentAt    = LocalDateTime.now()
        )
        val saved = chatMessageRepository.save(message)

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse(true, "Message sent", saved.toDto(caller.userId)))
    }

    // ── DELETE /v1/chat/messages/{messageId} ──────────────────────────────────
    // Only the original author OR an ADMIN may delete.
    // Soft-delete: sets isDeleted = true and records who deleted it.
    @DeleteMapping("/messages/{messageId}")
    @Transactional
    @Operation(summary = "Delete a chat message (author or admin only)")
    fun deleteMessage(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable messageId: String
    ): ResponseEntity<ApiResponse<Nothing>> {
        val caller  = resolveUser(userDetails) ?: return unauthorisedResponse()
        requireParentOrAdmin(caller.role.name) ?: return forbiddenResponse()

        val message = chatMessageRepository.findById(messageId).orElseThrow {
            ResourceNotFoundException("Message not found: $messageId")
        }

        if (message.isDeleted) {
            return ResponseEntity.ok(ApiResponse(true, "Message already deleted"))
        }

        val isAuthor = message.sender?.userId == caller.userId
        val isAdmin  = caller.role.name.equals("ADMIN", ignoreCase = true)

        if (!isAuthor && !isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse(false, "You can only delete your own messages."))
        }

        message.isDeleted = true
        message.deletedBy = caller
        message.deletedAt = LocalDateTime.now()
        chatMessageRepository.save(message)

        return ResponseEntity.ok(ApiResponse(true, "Message deleted"))
    }

    // ── GET /v1/chat/messages/poll?since=2025-01-01T10:00:00 ─────────────────
    // Lightweight long-poll endpoint. Returns only messages sent AFTER [since].
    // The client calls this every 3-5 seconds to simulate near-real-time chat.
    @GetMapping("/messages/poll")
    @Operation(summary = "Poll for new messages since a given timestamp")
    fun pollNewMessages(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) since: LocalDateTime
    ): ResponseEntity<ApiResponse<List<ChatMessageDto>>> {
        val caller = resolveUser(userDetails) ?: return unauthorisedResponse()
        requireParentOrAdmin(caller.role.name) ?: return forbiddenResponse()

        val newMessages = chatMessageRepository.findNewMessagesSince(since)
            .map { it.toDto(caller.userId) }

        return ResponseEntity.ok(ApiResponse(true, "Poll result", newMessages))
    }

    // ── GET /v1/chat/messages/count ───────────────────────────────────────────
    @GetMapping("/messages/count")
    @Operation(summary = "Total count of non-deleted messages")
    fun getMessageCount(
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<ApiResponse<Long>> {
        val caller = resolveUser(userDetails) ?: return unauthorisedResponse()
        requireParentOrAdmin(caller.role.name) ?: return forbiddenResponse()

        return ResponseEntity.ok(
            ApiResponse(true, "Count", chatMessageRepository.countByIsDeletedFalse())
        )
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────────────────────

    private fun resolveUser(userDetails: UserDetails) =
        userRepository.findByEmail(userDetails.username).orElse(null)

    /** Returns null (indicating forbidden) if role is VACCINATION_TEAM */
    private fun requireParentOrAdmin(role: String): Any? =
        if (role.equals("VACCINATION_TEAM", ignoreCase = true)) null else Unit

    private fun <T> unauthorisedResponse(): ResponseEntity<ApiResponse<T>> =
        ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse(false, "Unauthorised"))

    private fun <T> forbiddenResponse(): ResponseEntity<ApiResponse<T>> =
        ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ApiResponse(false, "Vaccination team members cannot access the parent chat."))

    /** Maps entity → DTO. [callerUserId] drives the `isOwnMessage` flag. */
    private fun ChatMessage.toDto(callerUserId: String) = ChatMessageDto(
        messageId    = messageId,
        senderId     = sender?.userId ?: "",
        senderName   = sender?.fullName ?: "",
        senderAvatar = sender?.profileImageUrl,
        senderRole   = sender?.role?.name ?: "PARENT",
        content      = content,
        sentAt       = sentAt.toString(),
        isOwnMessage = sender?.userId == callerUserId,
        isDeleted    = isDeleted
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// DTOs
// ─────────────────────────────────────────────────────────────────────────────

data class SendChatMessageRequest @JsonCreator constructor(
    @JsonProperty("content") val content: String
)

data class ChatMessageDto(
    val messageId    : String,
    val senderId     : String,
    val senderName   : String,
    val senderAvatar : String? = null,
    val senderRole   : String,          // "PARENT" | "ADMIN"
    val content      : String,
    val sentAt       : String,          // ISO-8601 LocalDateTime string
    val isOwnMessage : Boolean,
    val isDeleted    : Boolean = false
)

data class ChatPageResponse(
    val messages   : List<ChatMessageDto>,
    val page       : Int,
    val totalPages : Int,
    val hasMore    : Boolean
)