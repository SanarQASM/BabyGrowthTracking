package com.example.backend_side

import com.example.backend_side.entity.GuideFeedback
import com.example.backend_side.entity.GuideType
import com.example.backend_side.entity.VoteType
import com.example.backend_side.repositories.GuideFeedbackRepository
import com.example.backend_side.repositories.UserRepository
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

// ═══════════════════════════════════════════════════════════════════════════
// GuideFeedbackDtos.kt
// ═══════════════════════════════════════════════════════════════════════════

// ── Request ────────────────────────────────────────────────────────────────

/**
 * Body sent by the client when a user taps Useful / Useless.
 * The userId is resolved from the JWT — it is NOT sent in the body.
 */
data class GuideFeedbackRequest @JsonCreator constructor(
    @JsonProperty("contentId")
    @field:NotBlank(message = "contentId is required")
    val contentId: String,

    @JsonProperty("guideType")
    @field:NotNull(message = "guideType is required  — SLEEP or FEEDING")
    val guideType: GuideType,   // "SLEEP" | "FEEDING"

    @JsonProperty("vote")
    @field:NotNull(message = "vote is required — USEFUL or USELESS")
    val vote: VoteType          // "USEFUL" | "USELESS"
)

// ── Responses ──────────────────────────────────────────────────────────────

/** Returned after a vote is cast or changed. */
data class GuideFeedbackResponse(
    val feedbackId  : String,
    val contentId   : String,
    val guideType   : GuideType,
    val vote        : VoteType,
    val usefulCount : Long,      // current total USEFUL count for this content
    val uselessCount: Long,      // current total USELESS count (informational)
    val votedAt     : LocalDateTime
)

/**
 * Returned by the GET /counts endpoint.
 * The client sends a list of contentIds and receives back the vote counts
 * for each, plus the current user's vote (if any).
 */
data class GuideContentCountsResponse(
    val counts: List<ContentVoteCount>
)

data class ContentVoteCount(
    val contentId   : String,
    val usefulCount : Long,
    val uselessCount: Long,
    val userVote    : VoteType?  // null if the user has not voted yet
)

// ═══════════════════════════════════════════════════════════════════════════
// GuideFeedbackService
// ═══════════════════════════════════════════════════════════════════════════

interface GuideFeedbackService {

    /**
     * Record or update a vote.
     * - If the user has not voted yet → INSERT.
     * - If the user already cast the same vote → no-op (idempotent).
     * - If the user flips from Useful ↔ Useless → UPDATE.
     */
    fun castVote(userId: String, request: GuideFeedbackRequest): GuideFeedbackResponse

    /**
     * Fetch aggregated vote counts for a batch of contentIds, plus the
     * requesting user's own vote for each item.
     */
    fun getCounts(
        userId    : String,
        contentIds: List<String>,
        guideType : GuideType
    ): GuideContentCountsResponse
}

@Service
@Transactional
class GuideFeedbackServiceImpl(
    private val feedbackRepository: GuideFeedbackRepository,
    private val userRepository    : UserRepository
) : GuideFeedbackService {

    override fun castVote(userId: String, request: GuideFeedbackRequest): GuideFeedbackResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found: $userId") }

        val existing = feedbackRepository
            .findByUser_UserIdAndContentIdAndGuideType(userId, request.contentId, request.guideType)

        val saved = if (existing.isPresent) {
            val fb = existing.get()
            fb.vote    = request.vote          // flip or re-affirm
            fb.votedAt = LocalDateTime.now()
            feedbackRepository.save(fb)
        } else {
            feedbackRepository.save(
                GuideFeedback(
                    feedbackId = UUID.randomUUID().toString(),
                    user       = user,
                    contentId  = request.contentId,
                    guideType  = request.guideType,
                    vote       = request.vote
                )
            )
        }

        val usefulCount  = feedbackRepository.countByContentIdAndGuideTypeAndVote(
            request.contentId, request.guideType, VoteType.USEFUL)
        val uselessCount = feedbackRepository.countByContentIdAndGuideTypeAndVote(
            request.contentId, request.guideType, VoteType.USELESS)

        return GuideFeedbackResponse(
            feedbackId   = saved.feedbackId,
            contentId    = saved.contentId,
            guideType    = saved.guideType,
            vote         = saved.vote,
            usefulCount  = usefulCount,
            uselessCount = uselessCount,
            votedAt      = saved.votedAt
        )
    }

    @Transactional(readOnly = true)
    override fun getCounts(
        userId    : String,
        contentIds: List<String>,
        guideType : GuideType
    ): GuideContentCountsResponse {
        if (contentIds.isEmpty()) return GuideContentCountsResponse(emptyList())

        // Aggregate counts: [contentId, voteType, count]
        val rawCounts = feedbackRepository
            .countsByContentIdsAndGuideType(contentIds, guideType)

        // Build maps: contentId → (usefulCount, uselessCount)
        val usefulMap  = mutableMapOf<String, Long>()
        val uselessMap = mutableMapOf<String, Long>()
        for (row in rawCounts) {
            val cid  = row[0] as String
            val vote = row[1] as VoteType
            val cnt  = row[2] as Long
            if (vote == VoteType.USEFUL) usefulMap[cid] = cnt
            else uselessMap[cid] = cnt
        }

        // Fetch this user's own votes
        val userVotes = feedbackRepository
            .findUserVotesForContents(userId, contentIds, guideType)
            .associateBy { it.contentId }

        val counts = contentIds.map { cid ->
            ContentVoteCount(
                contentId    = cid,
                usefulCount  = usefulMap[cid]  ?: 0L,
                uselessCount = uselessMap[cid] ?: 0L,
                userVote     = userVotes[cid]?.vote
            )
        }
        return GuideContentCountsResponse(counts)
    }
}