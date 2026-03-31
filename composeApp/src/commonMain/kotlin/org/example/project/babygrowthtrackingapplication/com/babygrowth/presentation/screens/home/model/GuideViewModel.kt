package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model

import androidx.compose.runtime.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import org.example.project.babygrowthtrackingapplication.data.network.ApiResult
import org.example.project.babygrowthtrackingapplication.data.repository.GuideRepository
import org.jetbrains.compose.resources.ExperimentalResourceApi
import babygrowthtrackingapplication.composeapp.generated.resources.Res

// ═══════════════════════════════════════════════════════════════════════════
// GuideViewModel.kt
// ═══════════════════════════════════════════════════════════════════════════

private val guideJson = Json {
    ignoreUnknownKeys = true
    isLenient         = true
}

// ── UI State ─────────────────────────────────────────────────────────────

data class GuideUiState(
    val sleepGuide      : GuideDocument?            = null,
    val feedingGuide    : GuideDocument?            = null,
    val isLoadingSleep  : Boolean                   = false,
    val isLoadingFeeding: Boolean                   = false,
    val feedbackMap     : Map<String, CardFeedbackState> = emptyMap(),
    val playerState     : LullabyPlayerState        = LullabyPlayerState(),
    val errorMessage    : String?                   = null
)

// ── ViewModel ─────────────────────────────────────────────────────────────

class GuideViewModel(
    private val repository: GuideRepository? = null   // null in preview/test
) {
    var uiState by mutableStateOf(GuideUiState())
        private set

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // ── JSON loading ───────────────────────────────────────────────────────

    @OptIn(ExperimentalResourceApi::class)
    fun loadSleepGuide() {
        if (uiState.sleepGuide != null) return   // already loaded
        scope.launch {
            uiState = uiState.copy(isLoadingSleep = true)
            try {
                val bytes = Res.readBytes("files/sleep_guide_content.json")
                val doc   = guideJson.decodeFromString<GuideDocument>(bytes.decodeToString())
                uiState   = uiState.copy(sleepGuide = doc, isLoadingSleep = false)
            } catch (e: Exception) {
                uiState = uiState.copy(isLoadingSleep = false, errorMessage = e.message)
            }
        }
    }

    @OptIn(ExperimentalResourceApi::class)
    fun loadFeedingGuide() {
        if (uiState.feedingGuide != null) return
        scope.launch {
            uiState = uiState.copy(isLoadingFeeding = true)
            try {
                val bytes = Res.readBytes("files/feeding_guide_content.json")
                val doc   = guideJson.decodeFromString<GuideDocument>(bytes.decodeToString())
                uiState   = uiState.copy(feedingGuide = doc, isLoadingFeeding = false)
            } catch (e: Exception) {
                uiState = uiState.copy(isLoadingFeeding = false, errorMessage = e.message)
            }
        }
    }

    // ── Feedback loading ───────────────────────────────────────────────────

    /**
     * Fetch aggregated vote counts for all items visible in the current view.
     * Called every time the user switches strategy or tab.
     */
    fun loadFeedbackCounts(contentIds: List<String>, guideType: String) {
        if (repository == null || contentIds.isEmpty()) return
        scope.launch {
            when (val result = repository.getCounts(contentIds, guideType)) {
                is ApiResult.Success -> {
                    val updated = uiState.feedbackMap.toMutableMap()
                    for (item in result.data.counts) {
                        val existing = updated[item.contentId]
                        updated[item.contentId] = CardFeedbackState(
                            contentId   = item.contentId,
                            usefulCount = item.usefulCount,
                            userVote    = when (item.userVote) {
                                "USEFUL"  -> UserVote.USEFUL
                                "USELESS" -> UserVote.USELESS
                                else      -> UserVote.NONE
                            },
                            isLoading   = false
                        )
                    }
                    uiState = uiState.copy(feedbackMap = updated)
                }
                else -> Unit   // silently ignore — non-critical
            }
        }
    }

    // ── Vote casting ───────────────────────────────────────────────────────

    fun castVote(contentId: String, guideType: String, vote: UserVote) {
        // Optimistic UI update
        val current   = uiState.feedbackMap[contentId] ?: CardFeedbackState(contentId)
        val optimistic = current.copy(
            userVote    = vote,
            usefulCount = when {
                vote == UserVote.USEFUL && current.userVote != UserVote.USEFUL ->
                    current.usefulCount + 1
                vote != UserVote.USEFUL && current.userVote == UserVote.USEFUL ->
                    (current.usefulCount - 1).coerceAtLeast(0)
                else -> current.usefulCount
            },
            isLoading = true
        )
        uiState = uiState.copy(feedbackMap = uiState.feedbackMap + (contentId to optimistic))

        if (repository == null) {
            // Preview mode: just confirm the optimistic state
            uiState = uiState.copy(
                feedbackMap = uiState.feedbackMap + (contentId to optimistic.copy(isLoading = false))
            )
            return
        }

        scope.launch {
            when (val result = repository.castVote(contentId, guideType, vote.name)) {
                is ApiResult.Success -> {
                    val confirmed = CardFeedbackState(
                        contentId   = contentId,
                        usefulCount = result.data.usefulCount,
                        userVote    = vote,
                        isLoading   = false
                    )
                    uiState = uiState.copy(
                        feedbackMap = uiState.feedbackMap + (contentId to confirmed)
                    )
                }
                is ApiResult.Error -> {
                    // Roll back optimistic update
                    uiState = uiState.copy(
                        feedbackMap = uiState.feedbackMap + (contentId to current.copy(isLoading = false)),
                        errorMessage = result.message
                    )
                }
                else -> Unit
            }
        }
    }

    // ── Lullaby player ─────────────────────────────────────────────────────

    fun playLullaby(itemId: String, durationSeconds: Int) {
        uiState = uiState.copy(
            playerState = LullabyPlayerState(
                currentItemId   = itemId,
                isPlaying       = true,
                positionSeconds = 0,
                durationSeconds = durationSeconds
            )
        )
    }

    fun togglePlayPause() {
        uiState = uiState.copy(
            playerState = uiState.playerState.copy(isPlaying = !uiState.playerState.isPlaying)
        )
    }

    fun stopLullaby() {
        uiState = uiState.copy(playerState = LullabyPlayerState())
    }

    fun seekTo(seconds: Int) {
        uiState = uiState.copy(
            playerState = uiState.playerState.copy(positionSeconds = seconds)
        )
    }

    fun updatePosition(seconds: Int) {
        uiState = uiState.copy(
            playerState = uiState.playerState.copy(positionSeconds = seconds)
        )
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    fun feedbackFor(contentId: String): CardFeedbackState =
        uiState.feedbackMap[contentId] ?: CardFeedbackState(contentId)

    fun clearError() {
        uiState = uiState.copy(errorMessage = null)
    }

    fun onDestroy() {
        scope.cancel()
    }
}