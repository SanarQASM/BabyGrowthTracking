package org.example.project.babygrowthtrackingapplication.data.repository

import org.example.project.babygrowthtrackingapplication.data.network.ApiResult
import org.example.project.babygrowthtrackingapplication.data.network.ApiService
import org.example.project.babygrowthtrackingapplication.data.network.AuthApiResponse
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

// ─────────────────────────────────────────────────────────────────────────────
// DTOs
// ─────────────────────────────────────────────────────────────────────────────

@Serializable
data class GuideFeedbackRequest(
    val contentId: String,
    val guideType: String,
    val vote     : String
)

/**
 * FIX: vote is nullable String — null means the vote was toggled off (removed).
 * The client must treat null as UserVote.NONE and clear the button highlight.
 */
@Serializable
data class GuideFeedbackResponse(
    val feedbackId  : String?  = null,
    val contentId   : String,
    val guideType   : String,
    val vote        : String?  = null,   // null = vote was removed
    val usefulCount : Long,
    val uselessCount: Long,
    val votedAt     : String?  = null
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

// ─────────────────────────────────────────────────────────────────────────────
// Repository
// ─────────────────────────────────────────────────────────────────────────────

class GuideRepository(private val apiService: ApiService) {

    companion object {
        // FIX: must match the BASE_URL used by ApiService exactly.
        // All URLs are constructed as BASE_URL + path so the Ktor client
        // sends to the correct host and port.
        private const val BASE_URL = "http://172.20.10.3:8080/api"
        private const val BASE     = "$BASE_URL/v1/guide"
    }

    /**
     * Cast or update a vote for a guide content card.
     *
     * FIX 1: URL now includes the full BASE_URL prefix.
     *        Previously "/v1/guide/feedback" resolved to a relative path and
     *        the Ktor client had no base-URL configured, so every request
     *        was silently dropped / went to localhost.
     *
     * FIX 2: The response vote field is nullable — the caller (GuideViewModel)
     *        must handle null (= vote toggled off) by setting UserVote.NONE.
     */
    suspend fun castVote(
        contentId: String,
        guideType: String,
        vote     : String
    ): ApiResult<GuideFeedbackResponse> = try {
        val resp = apiService.client.post("$BASE/feedback") {
            contentType(ContentType.Application.Json)
            setBody(GuideFeedbackRequest(contentId, guideType, vote))
        }
        when (resp.status) {
            HttpStatusCode.OK -> {
                val env = resp.body<AuthApiResponse<GuideFeedbackResponse>>()
                if (env.success && env.data != null) ApiResult.Success(env.data)
                else ApiResult.Error(env.message ?: "Vote failed")
            }
            HttpStatusCode.Unauthorized -> ApiResult.Error("Not authenticated", 401)
            else -> ApiResult.Error("Vote failed: ${resp.status.description}", resp.status.value)
        }
    } catch (e: Exception) {
        ApiResult.Error("Network error: ${e.message}")
    }

    /**
     * Batch-fetch vote counts for a list of content IDs.
     *
     * FIX: URL now includes the full BASE_URL prefix.
     * The contentIds param is sent as a single comma-separated string:
     * ?contentIds=id1,id2,id3
     * The server's @RequestParam String splits on "," so this matches.
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
            HttpStatusCode.Unauthorized -> ApiResult.Error("Not authenticated", 401)
            else -> ApiResult.Error("Counts failed: ${resp.status.description}", resp.status.value)
        }
    } catch (e: Exception) {
        ApiResult.Error("Network error: ${e.message}")
    }
}