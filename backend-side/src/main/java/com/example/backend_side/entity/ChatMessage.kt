// File: backend-side/src/main/java/com/example/backend_side/entity/ChatMessage.kt
package com.example.backend_side.entity

import jakarta.persistence.*
import java.time.LocalDateTime

// ─────────────────────────────────────────────────────────────────────────────
// ChatMessage — maps to the `chat_messages` table.
//
// ONE global group chat for all parents (and admin can also post).
// Only the author of a message can delete it (soft-delete via isDeleted flag).
// Admins may delete any message.
//
// Content moderation: the backend refuses to persist any message whose text
// is flagged by ContentModerationService (swearing, threats, harassment).
// ─────────────────────────────────────────────────────────────────────────────

@Entity
@Table(
    name    = "chat_messages",
    indexes = [
        Index(name = "idx_cm_sender",     columnList = "sender_id"),
        Index(name = "idx_cm_sent_at",    columnList = "sent_at"),
        Index(name = "idx_cm_is_deleted", columnList = "is_deleted")
    ]
)
data class ChatMessage(

    @Id
    @Column(name = "message_id", columnDefinition = "VARCHAR(36)", updatable = false, nullable = false)
    var messageId: String = "",

    // ── Sender — always a registered user (PARENT or ADMIN) ──────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    var sender: User? = null,

    // ── Plain-text content only — no attachments ──────────────────────────────
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    var content: String = "",

    // ── Soft-delete: only author or admin may set this to true ────────────────
    @Column(name = "is_deleted", nullable = false)
    var isDeleted: Boolean = false,

    // ── Deletion metadata (audit) ─────────────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deleted_by")
    var deletedBy: User? = null,

    @Column(name = "deleted_at")
    var deletedAt: LocalDateTime? = null,

    // ── Timestamp ────────────────────────────────────────────────────────────
    @Column(name = "sent_at", nullable = false, updatable = false)
    var sentAt: LocalDateTime = LocalDateTime.now()

    // NOTE: createdAt / updatedAt from BaseEntity are inherited — do NOT redeclare.
) : BaseEntity()