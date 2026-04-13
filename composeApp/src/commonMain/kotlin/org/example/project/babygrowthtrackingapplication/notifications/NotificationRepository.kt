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

    private fun HttpRequestBuilder.auth() {
        getToken()?.let { header(HttpHeaders.Authorization, "Bearer $it") }
        contentType(ContentType.Application.Json)
    }

    // ── Fetch notifications ───────────────────────────────────────────────────

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

    // ── Unread count polling ──────────────────────────────────────────────────

    suspend fun getUnreadCount(userId: String): ApiResult<Long> = try {
        val resp = client.get("$baseUrl/v1/notifications/user/$userId/unread-count") { auth() }
        when (resp.status) {
            HttpStatusCode.OK -> ApiResult.Success(resp.body<Map<String, Long>>()["count"] ?: 0L)
            else              -> ApiResult.Error("Failed: ${resp.status}")
        }
    } catch (e: Exception) {
        ApiResult.Error("Network error: ${e.message}")
    }

    // ── Mark read ─────────────────────────────────────────────────────────────

    suspend fun markAsRead(notificationId: String): ApiResult<Unit> = try {
        val resp = client.patch("$baseUrl/v1/notifications/$notificationId/read") { auth() }
        if (resp.status.isSuccess()) ApiResult.Success(Unit)
        else ApiResult.Error("Failed: ${resp.status}")
    } catch (e: Exception) {
        ApiResult.Error("Network error: ${e.message}")
    }

    suspend fun markAllAsRead(userId: String): ApiResult<Unit> = try {
        val resp = client.patch("$baseUrl/v1/notifications/user/$userId/mark-all-read") { auth() }
        if (resp.status.isSuccess()) ApiResult.Success(Unit)
        else ApiResult.Error("Failed: ${resp.status}")
    } catch (e: Exception) {
        ApiResult.Error("Network error: ${e.message}")
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    suspend fun deleteNotification(notificationId: String): ApiResult<Unit> = try {
        val resp = client.delete("$baseUrl/v1/notifications/$notificationId") { auth() }
        if (resp.status.isSuccess()) ApiResult.Success(Unit)
        else ApiResult.Error("Failed: ${resp.status}")
    } catch (e: Exception) {
        ApiResult.Error("Network error: ${e.message}")
    }

    // ── FCM token registration ────────────────────────────────────────────────
    // FIX: path was /v1/notifications/register-token — backend is at /v1/fcm/register-token

    suspend fun registerFcmToken(request: RegisterFcmTokenRequest): ApiResult<Unit> = try {
        val resp = client.post("$baseUrl/v1/fcm/register-token") {
            auth()
            setBody(request)
        }
        if (resp.status.isSuccess()) ApiResult.Success(Unit)
        else ApiResult.Error("Failed: ${resp.status}")
    } catch (e: Exception) {
        ApiResult.Error("Network error: ${e.message}")
    }

    // ── Notification preferences ──────────────────────────────────────────────

    suspend fun getPreferences(userId: String): ApiResult<NotificationPreferencesDto> = try {
        val resp = client.get("$baseUrl/v1/notification-preferences/$userId") { auth() }
        if (resp.status.isSuccess()) {
            // Response is wrapped in ApiResponse<NotificationPreferencesDto>
            val wrapper = resp.body<ApiResponseWrapper<NotificationPreferencesDto>>()
            if (wrapper.success && wrapper.data != null) ApiResult.Success(wrapper.data)
            else ApiResult.Error(wrapper.message ?: "Failed to load preferences")
        } else ApiResult.Error("HTTP ${resp.status}")
    } catch (e: Exception) {
        ApiResult.Error("Network error: ${e.message}")
    }

    suspend fun updatePreferences(
        userId: String,
        request: UpdateNotificationPreferencesRequest
    ): ApiResult<NotificationPreferencesDto> = try {
        val resp = client.put("$baseUrl/v1/notification-preferences/$userId") {
            auth()
            setBody(request)
        }
        if (resp.status.isSuccess()) {
            val wrapper = resp.body<ApiResponseWrapper<NotificationPreferencesDto>>()
            if (wrapper.success && wrapper.data != null) ApiResult.Success(wrapper.data)
            else ApiResult.Error(wrapper.message ?: "Failed to update preferences")
        } else ApiResult.Error("HTTP ${resp.status}")
    } catch (e: Exception) {
        ApiResult.Error("Network error: ${e.message}")
    }
}

// Minimal wrapper for ApiResponse<T> deserialization
@kotlinx.serialization.Serializable
private data class ApiResponseWrapper<T>(
    val success : Boolean,
    val message : String? = null,
    val data    : T?      = null
)