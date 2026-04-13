package org.example.project.babygrowthtrackingapplication.notifications

import androidx.compose.runtime.*
import kotlinx.coroutines.*
import org.example.project.babygrowthtrackingapplication.data.network.ApiResult

// ─────────────────────────────────────────────────────────────────────────────
// UI State
// ─────────────────────────────────────────────────────────────────────────────

data class NotificationUiState(
    val notifications         : List<AppNotification>     = emptyList(),
    val filteredNotifications : List<AppNotification>     = emptyList(),
    val unreadCount           : Long                      = 0L,
    val isLoading             : Boolean                   = false,
    val isRefreshing          : Boolean                   = false,
    val errorMessage          : String?                   = null,
    val successMessage        : String?                   = null,
    val selectedFilter        : NotificationFilter        = NotificationFilter.ALL,
    val showOnlyUnread        : Boolean                   = false,
    val pendingNavigateTo     : String?                   = null,
    val hasMore               : Boolean                   = true,
    val currentPage           : Int                       = 0,
    // Preferences — kept in sync with backend
    val preferences           : NotificationPreferencesDto? = null,
    val preferencesLoading    : Boolean                   = false
)

enum class NotificationFilter(val label: String) {
    ALL("All"), VACCINATION("Vaccination"), GROWTH("Growth"),
    APPOINTMENT("Appointment"), HEALTH("Health"), DEVELOPMENT("Development"), ACCOUNT("Account")
}

// ─────────────────────────────────────────────────────────────────────────────
// ViewModel
// ─────────────────────────────────────────────────────────────────────────────

class NotificationViewModel(
    private val repository     : NotificationRepository,
    private val getUserId      : () -> String?,
    private val fcmTokenService: FcmTokenService
) {
    var uiState by mutableStateOf(NotificationUiState())
        private set

    private val scope      = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var pollingJob : Job? = null

    init {
        startUnreadPolling()
        registerFcmToken()
        loadPreferences()
    }

    // ─── Load / Refresh notifications ────────────────────────────────────────

    fun loadNotifications(refresh: Boolean = false) {
        val userId = getUserId() ?: return
        scope.launch {
            uiState = if (refresh)
                uiState.copy(isRefreshing = true, currentPage = 0, errorMessage = null)
            else
                uiState.copy(isLoading = true, errorMessage = null)

            when (val result = repository.getNotifications(
                userId = userId,
                page   = if (refresh) 0 else uiState.currentPage
            )) {
                is ApiResult.Success -> {
                    val existing = if (refresh) emptyList() else uiState.notifications
                    val merged   = (existing + result.data.notifications).distinctBy { it.notificationId }
                    uiState = uiState.copy(
                        notifications         = merged,
                        filteredNotifications = applyFilter(merged),
                        unreadCount           = result.data.unreadCount,
                        isLoading             = false,
                        isRefreshing          = false,
                        hasMore               = result.data.notifications.size >= 50,
                        currentPage           = if (refresh) 1 else uiState.currentPage + 1
                    )
                }
                is ApiResult.Error -> uiState = uiState.copy(
                    isLoading = false, isRefreshing = false, errorMessage = result.message)
                is ApiResult.Loading -> {}
            }
        }
    }

    fun loadMore() {
        if (!uiState.hasMore || uiState.isLoading) return
        loadNotifications(refresh = false)
    }

    // ─── Mark read / delete ───────────────────────────────────────────────────

    fun markAsRead(notificationId: String) {
        scope.launch {
            repository.markAsRead(notificationId)
            val updated = uiState.notifications.map {
                if (it.notificationId == notificationId) it.copy(isRead = true) else it
            }
            val wasUnread = uiState.notifications.any { it.notificationId == notificationId && !it.isRead }
            uiState = uiState.copy(
                notifications         = updated,
                filteredNotifications = applyFilter(updated),
                unreadCount           = if (wasUnread) maxOf(0L, uiState.unreadCount - 1) else uiState.unreadCount
            )
        }
    }

    fun markAllAsRead() {
        val userId = getUserId() ?: return
        scope.launch {
            when (repository.markAllAsRead(userId)) {
                is ApiResult.Success -> {
                    val updated = uiState.notifications.map { it.copy(isRead = true) }
                    uiState = uiState.copy(
                        notifications         = updated,
                        filteredNotifications = applyFilter(updated),
                        unreadCount           = 0L,
                        successMessage        = "All notifications marked as read"
                    )
                }
                else -> {}
            }
        }
    }

    fun deleteNotification(notificationId: String) {
        scope.launch {
            repository.deleteNotification(notificationId)
            val updated = uiState.notifications.filter { it.notificationId != notificationId }
            uiState = uiState.copy(
                notifications         = updated,
                filteredNotifications = applyFilter(updated)
            )
        }
    }

    // ─── Navigation ───────────────────────────────────────────────────────────

    fun onNotificationTapped(notification: AppNotification) {
        markAsRead(notification.notificationId)
        notification.deepLinkRoute?.let { uiState = uiState.copy(pendingNavigateTo = it) }
    }

    fun onDeepLinkReceived(route: String) {
        uiState = uiState.copy(pendingNavigateTo = route)
    }

    fun onNavigationHandled() {
        uiState = uiState.copy(pendingNavigateTo = null)
    }

    // ─── Filtering ────────────────────────────────────────────────────────────

    fun setFilter(filter: NotificationFilter) {
        uiState = uiState.copy(
            selectedFilter        = filter,
            filteredNotifications = applyFilter(uiState.notifications, filter, uiState.showOnlyUnread)
        )
    }

    fun toggleUnreadOnly() {
        val newVal = !uiState.showOnlyUnread
        uiState = uiState.copy(
            showOnlyUnread        = newVal,
            filteredNotifications = applyFilter(uiState.notifications, uiState.selectedFilter, newVal)
        )
    }

    private fun applyFilter(
        list      : List<AppNotification>,
        filter    : NotificationFilter = uiState.selectedFilter,
        unreadOnly: Boolean            = uiState.showOnlyUnread
    ): List<AppNotification> {
        val categoryFiltered = when (filter) {
            NotificationFilter.ALL         -> list
            NotificationFilter.VACCINATION -> list.filter { it.categoryEnum == NotificationCategory.VACCINATION }
            NotificationFilter.GROWTH      -> list.filter { it.categoryEnum == NotificationCategory.GROWTH }
            NotificationFilter.APPOINTMENT -> list.filter { it.categoryEnum == NotificationCategory.APPOINTMENT }
            NotificationFilter.HEALTH      -> list.filter {
                it.categoryEnum in listOf(NotificationCategory.HEALTH, NotificationCategory.DEVELOPMENT)
            }
            NotificationFilter.DEVELOPMENT -> list.filter { it.categoryEnum == NotificationCategory.DEVELOPMENT }
            NotificationFilter.ACCOUNT     -> list.filter { it.categoryEnum == NotificationCategory.ACCOUNT }
        }
        return if (unreadOnly) categoryFiltered.filter { !it.isRead } else categoryFiltered
    }

    // ─── Polling ──────────────────────────────────────────────────────────────

    fun startUnreadPolling() {
        pollingJob?.cancel()
        pollingJob = scope.launch {
            while (isActive) {
                val userId = getUserId()
                if (userId != null) {
                    when (val result = repository.getUnreadCount(userId)) {
                        is ApiResult.Success -> {
                            val previousCount = uiState.unreadCount
                            val newCount      = result.data
                            uiState = uiState.copy(unreadCount = newCount)
                            if (newCount > previousCount) {
                                loadNotifications(refresh = true)
                            }
                        }
                        else -> {}
                    }
                }
                delay(60_000L)
            }
        }
    }

    fun stopPolling() { pollingJob?.cancel() }

    // ─── Preferences — load from backend, sync on every Settings toggle ───────

    fun loadPreferences() {
        val userId = getUserId() ?: return
        scope.launch {
            uiState = uiState.copy(preferencesLoading = true)
            when (val result = repository.getPreferences(userId)) {
                is ApiResult.Success -> uiState = uiState.copy(
                    preferences = result.data, preferencesLoading = false)
                else -> uiState = uiState.copy(preferencesLoading = false)
            }
        }
    }

    /**
     * Called by SettingsViewModel whenever the user flips a notification toggle.
     * Sends the change to the backend so PushNotificationScheduler respects it.
     */
    fun updatePreferences(request: UpdateNotificationPreferencesRequest) {
        val userId = getUserId() ?: return
        scope.launch {
            when (val result = repository.updatePreferences(userId, request)) {
                is ApiResult.Success -> uiState = uiState.copy(preferences = result.data)
                else -> {}  // silently fail — local toggle already applied in SettingsViewModel
            }
        }
    }

    // ─── FCM token registration ───────────────────────────────────────────────

    private fun registerFcmToken() {
        val userId = getUserId() ?: return
        scope.launch {
            try {
                val token = fcmTokenService.getToken() ?: return@launch
                repository.registerFcmToken(RegisterFcmTokenRequest(
                    userId = userId, fcmToken = token, platform = fcmTokenService.platform))
            } catch (_: Exception) {}
        }
    }

    // ─── Cleanup ──────────────────────────────────────────────────────────────

    fun clearMessages() { uiState = uiState.copy(successMessage = null, errorMessage = null) }
    fun onDestroy() { scope.cancel() }
}

// ─────────────────────────────────────────────────────────────────────────────
// Platform expect
// ─────────────────────────────────────────────────────────────────────────────

expect class FcmTokenService() {
    val platform: String
    suspend fun getToken(): String?
}