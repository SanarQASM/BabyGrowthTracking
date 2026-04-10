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

// ─────────────────────────────────────────────────────────────────────────────
// DTOs
// ─────────────────────────────────────────────────────────────────────────────

data class GuideFeedbackRequest @JsonCreator constructor(
    @JsonProperty("contentId")
    @field:NotBlank(message = "contentId is required")
    val contentId: String,

    @JsonProperty("guideType")
    @field:NotNull(message = "guideType is required — SLEEP or FEEDING")
    val guideType: GuideType,

    @JsonProperty("vote")
    @field:NotNull(message = "vote is required — USEFUL or USELESS")
    val vote: VoteType
)

/**
 * FIX: Added `userVote` field (nullable String) so the client knows the
 * resulting vote state after a toggle. If the user toggled their vote OFF,
 * userVote is null, usefulCount is decremented. The client uses this field
 * to decide the button highlight state — it must not rely on the sent vote.
 */
data class GuideFeedbackResponse(
    val feedbackId  : String?,          // null when the vote was removed
    val contentId   : String,
    val guideType   : GuideType,
    val vote        : VoteType?,        // null = vote was removed (toggled off)
    val usefulCount : Long,
    val uselessCount: Long,
    val votedAt     : LocalDateTime?    // null when vote was removed
)

data class GuideContentCountsResponse(
    val counts: List<ContentVoteCount>
)

/**
 * FIX: userVote is now a nullable String (not VoteType?) so Jackson
 * serializes it as "USEFUL" / "USELESS" / null — matching what the
 * Kotlin client's @Serializable data class expects.
 */
data class ContentVoteCount(
    val contentId   : String,
    val usefulCount : Long,
    val uselessCount: Long,
    val userVote    : String?   // "USEFUL" | "USELESS" | null
)

// ─────────────────────────────────────────────────────────────────────────────
// Service interface
// ─────────────────────────────────────────────────────────────────────────────

interface GuideFeedbackService {
    fun castVote(userId: String, request: GuideFeedbackRequest): GuideFeedbackResponse
    fun getCounts(
        userId    : String,
        contentIds: List<String>,
        guideType : GuideType
    ): GuideContentCountsResponse
}

// ─────────────────────────────────────────────────────────────────────────────
// Implementation
// ─────────────────────────────────────────────────────────────────────────────

@Service
@Transactional
class GuideFeedbackServiceImpl(
    private val feedbackRepository: GuideFeedbackRepository,
    private val userRepository    : UserRepository
) : GuideFeedbackService {

    /**
     * FIX: Three cases now handled correctly:
     *
     * 1. No existing vote  → INSERT the new vote.
     * 2. Same vote re-tapped → DELETE (toggle off). Response has vote=null.
     * 3. Different vote    → UPDATE (flip). Response has the new vote.
     *
     * The client's castVote() sends the *intended* vote (USEFUL/USELESS).
     * The server compares with any existing row and handles the toggle.
     * This means the client does NOT need to track toggle-off state
     * separately — the server is the source of truth.
     */
    override fun castVote(userId: String, request: GuideFeedbackRequest): GuideFeedbackResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found: $userId") }

        val existing = feedbackRepository
            .findByUser_UserIdAndContentIdAndGuideType(userId, request.contentId, request.guideType)

        val resultVote: VoteType?
        val resultFeedbackId: String?
        val resultVotedAt: LocalDateTime?

        if (existing.isPresent) {
            val fb = existing.get()
            if (fb.vote == request.vote) {
                // FIX Case 2: same vote tapped again → toggle OFF (delete row)
                feedbackRepository.delete(fb)
                resultVote       = null
                resultFeedbackId = null
                resultVotedAt    = null
            } else {
                // FIX Case 3: different vote → flip
                fb.vote    = request.vote
                fb.votedAt = LocalDateTime.now()
                val saved  = feedbackRepository.save(fb)
                resultVote       = saved.vote
                resultFeedbackId = saved.feedbackId
                resultVotedAt    = saved.votedAt
            }
        } else {
            // Case 1: no existing vote → insert
            val saved = feedbackRepository.save(
                GuideFeedback(
                    feedbackId = UUID.randomUUID().toString(),
                    user       = user,
                    contentId  = request.contentId,
                    guideType  = request.guideType,
                    vote       = request.vote
                )
            )
            resultVote       = saved.vote
            resultFeedbackId = saved.feedbackId
            resultVotedAt    = saved.votedAt
        }

        // Re-count after mutation
        val usefulCount  = feedbackRepository.countByContentIdAndGuideTypeAndVote(
            request.contentId, request.guideType, VoteType.USEFUL
        )
        val uselessCount = feedbackRepository.countByContentIdAndGuideTypeAndVote(
            request.contentId, request.guideType, VoteType.USELESS
        )

        return GuideFeedbackResponse(
            feedbackId   = resultFeedbackId,
            contentId    = request.contentId,
            guideType    = request.guideType,
            vote         = resultVote,
            usefulCount  = usefulCount,
            uselessCount = uselessCount,
            votedAt      = resultVotedAt
        )
    }

    @Transactional(readOnly = true)
    override fun getCounts(
        userId    : String,
        contentIds: List<String>,
        guideType : GuideType
    ): GuideContentCountsResponse {
        if (contentIds.isEmpty()) return GuideContentCountsResponse(emptyList())

        val rawCounts = feedbackRepository
            .countsByContentIdsAndGuideType(contentIds, guideType)

        val usefulMap  = mutableMapOf<String, Long>()
        val uselessMap = mutableMapOf<String, Long>()
        for (row in rawCounts) {
            val cid  = row[0] as String
            val vote = row[1] as VoteType
            val cnt  = row[2] as Long
            if (vote == VoteType.USEFUL) usefulMap[cid]  = cnt
            else                          uselessMap[cid] = cnt
        }

        val userVotes = feedbackRepository
            .findUserVotesForContents(userId, contentIds, guideType)
            .associateBy { it.contentId }

        val counts = contentIds.map { cid ->
            ContentVoteCount(
                contentId    = cid,
                usefulCount  = usefulMap[cid]  ?: 0L,
                uselessCount = uselessMap[cid] ?: 0L,
                // FIX: serialize as String so Kotlin client's String? field matches
                userVote     = userVotes[cid]?.vote?.name   // "USEFUL" / "USELESS" / null
            )
        }
        return GuideContentCountsResponse(counts)
    }
}