package org.example.project.babygrowthtrackingapplication.notifications

import androidx.compose.runtime.*
import kotlinx.coroutines.*
import org.example.project.babygrowthtrackingapplication.data.network.ApiResult

// ─────────────────────────────────────────────────────────────────────────────
// UI State
// ─────────────────────────────────────────────────────────────────────────────

data class NotificationUiState(
    val notifications         : List<AppNotification> = emptyList(),
    val filteredNotifications : List<AppNotification> = emptyList(),
    val unreadCount           : Long                  = 0L,
    val isLoading             : Boolean               = false,
    val isRefreshing          : Boolean               = false,
    val errorMessage          : String?               = null,
    val successMessage        : String?               = null,
    val selectedFilter        : NotificationFilter    = NotificationFilter.ALL,
    val showOnlyUnread        : Boolean               = false,
    val pendingNavigateTo     : String?               = null,
    val hasMore               : Boolean               = true,
    val currentPage           : Int                   = 0
)

enum class NotificationFilter(val label: String) {
    ALL("All"),
    VACCINATION("Vaccination"),
    GROWTH("Growth"),
    APPOINTMENT("Appointment"),
    HEALTH("Health"),
    DEVELOPMENT("Development"),
    ACCOUNT("Account")
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
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LOAD & REFRESH
    // ─────────────────────────────────────────────────────────────────────────

    fun loadNotifications(refresh: Boolean = false) {
        val userId = getUserId() ?: return
        scope.launch {
            uiState = if (refresh) {
                uiState.copy(isRefreshing = true, currentPage = 0, errorMessage = null)
            } else {
                uiState.copy(isLoading = true, errorMessage = null)
            }

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
                is ApiResult.Error -> {
                    uiState = uiState.copy(
                        isLoading    = false,
                        isRefreshing = false,
                        errorMessage = result.message
                    )
                }
                is ApiResult.Loading -> {}
            }
        }
    }

    fun loadMore() {
        if (!uiState.hasMore || uiState.isLoading) return
        loadNotifications(refresh = false)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // MARK READ / DELETE
    // ─────────────────────────────────────────────────────────────────────────

    fun markAsRead(notificationId: String) {
        scope.launch {
            repository.markAsRead(notificationId)
            val updated = uiState.notifications.map {
                if (it.notificationId == notificationId) it.copy(isRead = true) else it
            }
            val newUnread = uiState.notifications
                .count { it.notificationId == notificationId && !it.isRead }
                .toLong()
            uiState = uiState.copy(
                notifications         = updated,
                filteredNotifications = applyFilter(updated),
                unreadCount           = maxOf(0L, uiState.unreadCount - newUnread)
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
                is ApiResult.Error   -> {}
                is ApiResult.Loading -> {}
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

    // ─────────────────────────────────────────────────────────────────────────
    // NAVIGATION — notification tap + cold-start deep link
    // ─────────────────────────────────────────────────────────────────────────

    fun onNotificationTapped(notification: AppNotification) {
        markAsRead(notification.notificationId)
        notification.deepLinkRoute?.let { route ->
            uiState = uiState.copy(pendingNavigateTo = route)
        }
    }

    /** Called from MainActivity / AppNavigation when a cold-start deep link arrives. */
    fun onDeepLinkReceived(route: String) {
        uiState = uiState.copy(pendingNavigateTo = route)
    }

    fun onNavigationHandled() {
        uiState = uiState.copy(pendingNavigateTo = null)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FILTERING
    // ─────────────────────────────────────────────────────────────────────────

    fun setFilter(filter: NotificationFilter) {
        uiState = uiState.copy(
            selectedFilter        = filter,
            filteredNotifications = applyFilter(
                uiState.notifications,
                filter,
                uiState.showOnlyUnread
            )
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
            NotificationFilter.VACCINATION -> list.filter { it.category == NotificationCategory.VACCINATION }
            NotificationFilter.GROWTH      -> list.filter { it.category == NotificationCategory.GROWTH }
            NotificationFilter.APPOINTMENT -> list.filter { it.category == NotificationCategory.APPOINTMENT }
            NotificationFilter.HEALTH      -> list.filter { it.category in listOf(
                NotificationCategory.HEALTH, NotificationCategory.DEVELOPMENT
            )}
            NotificationFilter.DEVELOPMENT -> list.filter { it.category == NotificationCategory.DEVELOPMENT }
            NotificationFilter.ACCOUNT     -> list.filter { it.category == NotificationCategory.ACCOUNT }
        }
        return if (unreadOnly) categoryFiltered.filter { !it.isRead } else categoryFiltered
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POLLING — unread count refresh every 60 seconds
    // ─────────────────────────────────────────────────────────────────────────

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

                            // Update badge first, then check if new ones arrived
                            uiState = uiState.copy(unreadCount = newCount)

                            // BUG FIX: compare against previousCount, not uiState.unreadCount
                            // (which was already updated on the line above)
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

    fun stopPolling() {
        pollingJob?.cancel()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FCM TOKEN REGISTRATION
    // ─────────────────────────────────────────────────────────────────────────

    private fun registerFcmToken() {
        val userId = getUserId() ?: return
        scope.launch {
            try {
                val token = fcmTokenService.getToken() ?: return@launch
                repository.registerFcmToken(
                    RegisterFcmTokenRequest(
                        userId   = userId,
                        fcmToken = token,
                        platform = fcmTokenService.platform
                    )
                )
            } catch (e: Exception) {
                // Non-fatal — FCM registration failure doesn't block the app
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CLEANUP
    // ─────────────────────────────────────────────────────────────────────────

    fun clearMessages() {
        uiState = uiState.copy(successMessage = null, errorMessage = null)
    }

    fun onDestroy() {
        scope.cancel()
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Platform expect — implemented per platform (Android / iOS / Desktop / Web)
// ─────────────────────────────────────────────────────────────────────────────

expect class FcmTokenService() {
    val platform: String
    suspend fun getToken(): String?
}