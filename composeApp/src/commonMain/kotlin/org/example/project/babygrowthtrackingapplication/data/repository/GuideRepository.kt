package org.example.project.babygrowthtrackingapplication.data.repository

import org.example.project.babygrowthtrackingapplication.data.network.ApiResult
import org.example.project.babygrowthtrackingapplication.data.network.ApiService
import org.example.project.babygrowthtrackingapplication.data.network.AuthApiResponse
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

// ═══════════════════════════════════════════════════════════════════════════
// GuideRepository.kt
//
// Handles all network calls for the guide feedback feature.
// Mirrors the pattern of AccountRepository — thin layer over ApiService.
// ═══════════════════════════════════════════════════════════════════════════

// ── Network DTOs ────────────────────────────────────────────────────────

@Serializable
data class GuideFeedbackRequest(
    val contentId: String,
    val guideType: String,   // "SLEEP" | "FEEDING"
    val vote     : String    // "USEFUL" | "USELESS"
)

@Serializable
data class GuideFeedbackResponse(
    val feedbackId  : String,
    val contentId   : String,
    val guideType   : String,
    val vote        : String,
    val usefulCount : Long,
    val uselessCount: Long,
    val votedAt     : String
)

@Serializable
data class ContentVoteCount(
    val contentId   : String,
    val usefulCount : Long,
    val uselessCount: Long,
    val userVote    : String?   // "USEFUL" | "USELESS" | null
)

@Serializable
data class GuideCountsResponse(
    val counts: List<ContentVoteCount>
)

// ── Repository ───────────────────────────────────────────────────────────

class GuideRepository(private val apiService: ApiService) {

    companion object {
        private const val BASE = "/v1/guide"
    }

    /**
     * Cast or update a vote (USEFUL / USELESS) for a guide content card.
     * Idempotent — voting twice with the same value is a no-op on the server.
     */
    suspend fun castVote(
        contentId: String,
        guideType: String,
        vote     : String
    ): ApiResult<GuideFeedbackResponse> = try {
        val resp = apiService.client.post("/v1/guide/feedback") {
            contentType(ContentType.Application.Json)
            setBody(GuideFeedbackRequest(contentId, guideType, vote))
        }
        when (resp.status) {
            HttpStatusCode.OK -> {
                val env = resp.body<AuthApiResponse<GuideFeedbackResponse>>()
                if (env.success && env.data != null) ApiResult.Success(env.data)
                else ApiResult.Error(env.message ?: "Vote failed")
            }
            else -> ApiResult.Error("Vote failed: ${resp.status.description}")
        }
    } catch (e: Exception) {
        ApiResult.Error("Network error: ${e.message}")
    }

    /**
     * Batch-fetch vote counts for a list of content IDs.
     * Called once when a guide strategy tab is displayed.
     */
    suspend fun getCounts(
        contentIds: List<String>,
        guideType : String
    ): ApiResult<GuideCountsResponse> = try {
        val idsParam = contentIds.joinToString(",")
        val resp = apiService.client.get("$BASE/feedback/counts") {
            parameter("guideType",  guideType)
            parameter("contentIds", idsParam)
        }
        when (resp.status) {
            HttpStatusCode.OK -> {
                val env = resp.body<AuthApiResponse<GuideCountsResponse>>()
                if (env.success && env.data != null) ApiResult.Success(env.data)
                else ApiResult.Error(env.message ?: "Failed to fetch counts")
            }
            else -> ApiResult.Error("Counts failed: ${resp.status.description}")
        }
    } catch (e: Exception) {
        ApiResult.Error("Network error: ${e.message}")
    }
}