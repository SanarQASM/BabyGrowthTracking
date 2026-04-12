
package org.example.project.babygrowthtrackingapplication.notifications

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.example.project.babygrowthtrackingapplication.data.network.ApiResult

class NotificationRepository(
    private val client   : HttpClient,
    private val baseUrl  : String,
    private val getToken : () -> String?
) {

    // ── Helper ────────────────────────────────────────────────────────────────

    private fun HttpRequestBuilder.auth() {
        getToken()?.let { header(HttpHeaders.Authorization, "Bearer $it") }
        contentType(ContentType.Application.Json)
    }

    // ── Fetch notifications for the current user ──────────────────────────────

    suspend fun getNotifications(
        userId : String,
        page   : Int = 0,
        size   : Int = 50
    ): ApiResult<NotificationListResponse> = try {
        val resp = client.get("$baseUrl/v1/notifications/user/$userId") {
            auth()
            parameter("page", page)
            parameter("size", size)
        }
        when (resp.status) {
            HttpStatusCode.OK -> ApiResult.Success(resp.body<NotificationListResponse>())
            else              -> ApiResult.Error("Failed to load notifications: ${resp.status}")
        }
    } catch (e: Exception) {
        ApiResult.Error("Network error: ${e.message}")
    }

    // ── Unread count only (lightweight polling) ───────────────────────────────

    suspend fun getUnreadCount(userId: String): ApiResult<Long> = try {
        val resp = client.get("$baseUrl/v1/notifications/user/$userId/unread-count") { auth() }
        when (resp.status) {
            HttpStatusCode.OK -> ApiResult.Success(resp.body<Map<String, Long>>()["count"] ?: 0L)
            else              -> ApiResult.Error("Failed: ${resp.status}")
        }
    } catch (e: Exception) {
        ApiResult.Error("Network error: ${e.message}")
    }

    // ── Mark single notification as read ─────────────────────────────────────

    suspend fun markAsRead(notificationId: String): ApiResult<Unit> = try {
        val resp = client.patch("$baseUrl/v1/notifications/$notificationId/read") { auth() }
        if (resp.status.isSuccess()) ApiResult.Success(Unit)
        else ApiResult.Error("Failed: ${resp.status}")
    } catch (e: Exception) {
        ApiResult.Error("Network error: ${e.message}")
    }

    // ── Mark all as read for user ─────────────────────────────────────────────

    suspend fun markAllAsRead(userId: String): ApiResult<Unit> = try {
        val resp = client.patch("$baseUrl/v1/notifications/user/$userId/mark-all-read") { auth() }
        if (resp.status.isSuccess()) ApiResult.Success(Unit)
        else ApiResult.Error("Failed: ${resp.status}")
    } catch (e: Exception) {
        ApiResult.Error("Network error: ${e.message}")
    }

    // ── Delete a notification ─────────────────────────────────────────────────

    suspend fun deleteNotification(notificationId: String): ApiResult<Unit> = try {
        val resp = client.delete("$baseUrl/v1/notifications/$notificationId") { auth() }
        if (resp.status.isSuccess()) ApiResult.Success(Unit)
        else ApiResult.Error("Failed: ${resp.status}")
    } catch (e: Exception) {
        ApiResult.Error("Network error: ${e.message}")
    }

    // ── Register FCM device token ─────────────────────────────────────────────

    suspend fun registerFcmToken(request: RegisterFcmTokenRequest): ApiResult<Unit> = try {
        val resp = client.post("$baseUrl/v1/notifications/register-token") {
            auth()
            setBody(request)
        }
        if (resp.status.isSuccess()) ApiResult.Success(Unit)
        else ApiResult.Error("Failed: ${resp.status}")
    } catch (e: Exception) {
        ApiResult.Error("Network error: ${e.message}")
    }
}