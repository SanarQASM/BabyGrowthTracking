// File: backend-side/src/main/java/com/example/backend_side/repositories/ChatMessageRepository.kt
package com.example.backend_side.repositories

import com.example.backend_side.entity.ChatMessage
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ChatMessageRepository : JpaRepository<ChatMessage, String> {

    // ── Paginated feed — most-recent first, soft-deleted messages excluded ────
    @Query("""
        SELECT m FROM ChatMessage m
        WHERE m.isDeleted = false
        ORDER BY m.sentAt DESC
    """)
    fun findActivePaged(pageable: Pageable): Page<ChatMessage>

    // ── Count of visible messages (for hasMore detection) ────────────────────
    fun countByIsDeletedFalse(): Long

    // ── Messages sent by a specific user (admin audit) ────────────────────────
    fun findBySender_UserIdAndIsDeletedFalseOrderBySentAtDesc(userId: String): List<ChatMessage>

    // ── Check authorship — used by delete endpoint ────────────────────────────
    @Query("""
        SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END
        FROM ChatMessage m
        WHERE m.messageId = :messageId
        AND m.sender.userId = :userId
        AND m.isDeleted = false
    """)
    fun existsByMessageIdAndSenderUserId(
        @Param("messageId") messageId: String,
        @Param("userId")    userId: String
    ): Boolean

    // ── Poll for new messages since a given ISO timestamp (long-poll support) ─
    @Query("""
        SELECT m FROM ChatMessage m
        WHERE m.isDeleted = false
        AND m.sentAt > :since
        ORDER BY m.sentAt ASC
    """)
    fun findNewMessagesSince(
        @Param("since") since: java.time.LocalDateTime
    ): List<ChatMessage>
}