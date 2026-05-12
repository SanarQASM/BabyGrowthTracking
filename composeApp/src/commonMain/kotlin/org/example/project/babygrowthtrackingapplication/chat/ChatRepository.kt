// File: composeApp/src/commonMain/kotlin/org/example/project/babygrowthtrackingapplication/chat/ChatRepository.kt
package org.example.project.babygrowthtrackingapplication.chat

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.example.project.babygrowthtrackingapplication.data.network.ApiResult
import org.example.project.babygrowthtrackingapplication.data.network.ApiSingleResponse

// ─────────────────────────────────────────────────────────────────────────────
// ChatRepository
//
// All network calls for the group chat feature.
// Uses the same HttpClient (with auth token) as ApiService.
// ─────────────────────────────────────────────────────────────────────────────

class ChatRepository(
    private val client   : HttpClient,
    private val baseUrl  : String,
    private val getToken : () -> String?
) {

    companion object {
        private const val CHAT_BASE = "/v1/chat/messages"
    }

    // ── Fetch paginated history ───────────────────────────────────────────────

    suspend fun getMessages(page: Int = 0, size: Int = 40): ApiResult<ChatPageNet> = safeCall {
        val resp = client.get("$baseUrl$CHAT_BASE") {
            getToken()?.let { header(HttpHeaders.Authorization, "Bearer $it") }
            parameter("page", page)
            parameter("size", size)
        }
        resp.body<ApiSingleResponse<ChatPageNet>>().data!!
    }

    // ── Send a new message ────────────────────────────────────────────────────

    suspend fun sendMessage(content: String): ApiResult<ChatMessageNet> = safeCall {
        val resp = client.post("$baseUrl$CHAT_BASE") {
            getToken()?.let { header(HttpHeaders.Authorization, "Bearer $it") }
            contentType(ContentType.Application.Json)
            setBody(SendMessageRequest(content = content))
        }
        when (resp.status) {
            HttpStatusCode.Created -> resp.body<ApiSingleResponse<ChatMessageNet>>().data!!
            HttpStatusCode.UnprocessableEntity -> {
                val err = resp.body<ApiSingleResponse<ChatMessageNet>>()
                throw ModerationException(err.message ?: "Message blocked by moderation.")
            }
            HttpStatusCode.Forbidden -> throw ForbiddenException(
                "Vaccination team members cannot access the parent chat."
            )
            else -> throw Exception("Send failed: ${resp.status.description}")
        }
    }

    // ── Delete a message (author or admin) ────────────────────────────────────

    suspend fun deleteMessage(messageId: String): ApiResult<Unit> = safeCall {
        client.delete("$baseUrl$CHAT_BASE/$messageId") {
            getToken()?.let { header(HttpHeaders.Authorization, "Bearer $it") }
        }
        Unit
    }

    // ── Poll for new messages since timestamp ─────────────────────────────────

    suspend fun pollSince(since: String): ApiResult<List<ChatMessageNet>> = safeCall {
        val resp = client.get("$baseUrl$CHAT_BASE/poll") {
            getToken()?.let { header(HttpHeaders.Authorization, "Bearer $it") }
            parameter("since", since)
        }
        resp.body<ApiSingleResponse<List<ChatMessageNet>>>().data ?: emptyList()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helper
    // ─────────────────────────────────────────────────────────────────────────

    private suspend fun <T> safeCall(block: suspend () -> T): ApiResult<T> = try {
        ApiResult.Success(block())
    } catch (e: ModerationException) {
        ApiResult.Error(e.message ?: "Moderation error", 422)
    } catch (e: ForbiddenException) {
        ApiResult.Error(e.message ?: "Forbidden", 403)
    } catch (e: Exception) {
        e.printStackTrace()
        ApiResult.Error("Network error: ${e.message ?: "Unknown"}")
    }
}

/** Thrown when the server rejects a message for content moderation reasons. */
class ModerationException(message: String) : Exception(message)

/** Thrown when the caller's role is not permitted. */
class ForbiddenException(message: String) : Exception(message)