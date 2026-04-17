package org.example.project.babygrowthtrackingapplication.notifications

import androidx.compose.runtime.*
import kotlinx.coroutines.*
import org.example.project.babygrowthtrackingapplication.data.network.ApiResult

// ─────────────────────────────────────────────────────────────────────────────
// NotificationViewModel — FIXED
//
// Changes from original:
//
//  1. FCM token registration now retries up to 3 times with exponential backoff.
//     On iOS the token may not be available immediately on first launch (APNs
//     registration is async). Without retry the registration silently failed.
//
//  2. onDeepLinkReceived() is now called from outside the class (Android
//     MainActivity.onNewIntent, iOS DeepLinkObserver) so navigation works when
//     the app is already running.
//
//  3. startUnreadPolling() is idempotent — calling it multiple times (e.g.
//     after app resume) cancels the previous job before starting a new one.
//     The original code could accumulate multiple simultaneous polling loops.
//
//  4. loadNotifications() now passes page correctly so the paginated backend
//     endpoint works.
// ─────────────────────────────────────────────────────────────────────────────

data class NotificationUiState(
    val notifications         : List<AppNotification>       = emptyList(),
    val filteredNotifications : List<AppNotification>       = emptyList(),
    val unreadCount           : Long                        = 0L,
    val isLoading             : Boolean                     = false,
    val isRefreshing          : Boolean                     = false,
    val errorMessage          : String?                     = null,
    val successMessage        : String?                     = null,
    val selectedFilter        : NotificationFilter          = NotificationFilter.ALL,
    val showOnlyUnread        : Boolean                     = false,
    val pendingNavigateTo     : String?                     = null,
    val hasMore               : Boolean                     = true,
    val currentPage           : Int                         = 0,
    val preferences           : NotificationPreferencesDto? = null,
    val preferencesLoading    : Boolean                     = false
)

enum class NotificationFilter(val label: String) {
    ALL("All"), VACCINATION("Vaccination"), GROWTH("Growth"),
    APPOINTMENT("Appointment"), HEALTH("Health"), DEVELOPMENT("Development"), ACCOUNT("Account")
}

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
        // FIX 3: startUnreadPolling() is now idempotent — safe to call in init
        startUnreadPolling()
        registerFcmToken()
        loadPreferences()
    }

    // ── Load / Refresh ────────────────────────────────────────────────────────

    fun loadNotifications(refresh: Boolean = false) {
        val userId = getUserId() ?: return
        scope.launch {
            uiState = if (refresh)
                uiState.copy(isRefreshing = true, currentPage = 0, errorMessage = null)
            else
                uiState.copy(isLoading = true, errorMessage = null)

            // FIX 4: pass the correct page number so backend pagination works
            val targetPage = if (refresh) 0 else uiState.currentPage

            when (val result = repository.getNotifications(userId = userId, page = targetPage)) {
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
                        currentPage           = targetPage + 1
                    )
                }
                is ApiResult.Error -> uiState = uiState.copy(
                    isLoading    = false,
                    isRefreshing = false,
                    errorMessage = result.message
                )
                is ApiResult.Loading -> {}
            }
        }
    }

    fun loadMore() {
        if (!uiState.hasMore || uiState.isLoading) return
        loadNotifications(refresh = false)
    }

    // ── Mark read / delete ────────────────────────────────────────────────────

    fun markAsRead(notificationId: String) {
        scope.launch {
            repository.markAsRead(notificationId)
            val updated   = uiState.notifications.map {
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

    // ── Navigation ────────────────────────────────────────────────────────────

    fun onNotificationTapped(notification: AppNotification) {
        markAsRead(notification.notificationId)
        notification.deepLinkRoute?.let { uiState = uiState.copy(pendingNavigateTo = it) }
    }

    // FIX 2: Called by Android MainActivity.onNewIntent() and iOS DeepLinkObserver
    //        so navigation works when the app is already running in the background.
    fun onDeepLinkReceived(route: String) {
        uiState = uiState.copy(pendingNavigateTo = route)
    }

    fun onNavigationHandled() {
        uiState = uiState.copy(pendingNavigateTo = null)
    }

    // ── Filtering ─────────────────────────────────────────────────────────────

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

    // ── Polling ───────────────────────────────────────────────────────────────

    // FIX 3: Idempotent — cancels previous polling job before starting a new one.
    //        The original code could accumulate multiple simultaneous polling loops
    //        when the app was resumed or the ViewModel was re-initialized.
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

    // ── Preferences ───────────────────────────────────────────────────────────

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

    fun updatePreferences(request: UpdateNotificationPreferencesRequest) {
        val userId = getUserId() ?: return
        scope.launch {
            when (val result = repository.updatePreferences(userId, request)) {
                is ApiResult.Success -> uiState = uiState.copy(preferences = result.data)
                else -> {}
            }
        }
    }

    // ── FCM token registration ────────────────────────────────────────────────

    // FIX 1: Retry up to 3 times with exponential backoff.
    //        On iOS, the FCM token from APNs is async. On first launch the token
    //        may not be available immediately, so we wait and retry.
    private fun registerFcmToken() {
        val userId = getUserId() ?: return
        scope.launch {
            var attempt = 0
            var token: String? = null

            while (attempt < 3 && token == null) {
                if (attempt > 0) {
                    // Exponential backoff: 2s, 4s
                    delay(2_000L * attempt)
                }
                try {
                    token = fcmTokenService.getToken()
                } catch (_: Exception) { }
                attempt++
            }

            if (token == null) return@launch  // platform doesn't support push (desktop/web not wired)

            repository.registerFcmToken(RegisterFcmTokenRequest(
                userId   = userId,
                fcmToken = token,
                platform = fcmTokenService.platform
            ))
        }
    }

    // ── Cleanup ───────────────────────────────────────────────────────────────

    fun clearMessages() { uiState = uiState.copy(successMessage = null, errorMessage = null) }
    fun onDestroy() { scope.cancel() }
}

expect class FcmTokenService() {
    val platform: String
    suspend fun getToken(): String?
}