package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model

import androidx.compose.runtime.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import org.example.project.babygrowthtrackingapplication.data.network.ApiResult
import org.example.project.babygrowthtrackingapplication.data.repository.GuideRepository
import org.example.project.babygrowthtrackingapplication.platform.LullabyPlayer
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
    val sleepGuide      : GuideDocument?              = null,
    val feedingGuide    : GuideDocument?              = null,
    val isLoadingSleep  : Boolean                     = false,
    val isLoadingFeeding: Boolean                     = false,
    val feedbackMap     : Map<String, CardFeedbackState> = emptyMap(),
    val playerState     : LullabyPlayerState          = LullabyPlayerState(),
    val errorMessage    : String?                     = null,
    val downloadEvent   : DownloadEvent?              = null   // one-shot download trigger
)

/** One-shot event emitted when the user taps Download. */
data class DownloadEvent(
    val assetKey : String,
    val fileName : String,
    val consumed : Boolean = false
)

// ── ViewModel ─────────────────────────────────────────────────────────────

class GuideViewModel(
    private val repository   : GuideRepository? = null,   // null in preview/test
    private val lullabyPlayer: LullabyPlayer?   = null    // injected by DI / platform
) {
    var uiState by mutableStateOf(GuideUiState())
        private set

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // Position polling job — runs while audio is playing
    private var positionJob: Job? = null

    init {
        // Wire the player position callback into the ViewModel state
        lullabyPlayer?.setOnPositionChanged { seconds ->
            updatePosition(seconds)
        }
    }

    // ── JSON loading ───────────────────────────────────────────────────────

    @OptIn(ExperimentalResourceApi::class)
    fun loadSleepGuide() {
        if (uiState.sleepGuide != null) return
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

    fun loadFeedbackCounts(contentIds: List<String>, guideType: String) {
        if (repository == null || contentIds.isEmpty()) return
        scope.launch {
            when (val result = repository.getCounts(contentIds, guideType)) {
                is ApiResult.Success -> {
                    val updated = uiState.feedbackMap.toMutableMap()
                    for (item in result.data.counts) {
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
                else -> Unit
            }
        }
    }

    // ── Vote casting ───────────────────────────────────────────────────────

    fun castVote(contentId: String, guideType: String, vote: UserVote) {
        val current    = uiState.feedbackMap[contentId] ?: CardFeedbackState(contentId)
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
                    uiState = uiState.copy(
                        feedbackMap  = uiState.feedbackMap + (contentId to current.copy(isLoading = false)),
                        errorMessage = result.message
                    )
                }
                else -> Unit
            }
        }
    }

    // ── Lullaby player ─────────────────────────────────────────────────────

    /**
     * Start playing a lullaby.
     *
     * If the same item is tapped again while playing, this acts as a replay
     * from the start. For toggle behaviour, call [togglePlayPause] instead.
     */
    fun playLullaby(item: GuideItem) {
        val assetKey  = item.media?.asset_key ?: return
        val duration  = item.media.duration_seconds

        // Stop any currently playing track
        lullabyPlayer?.stop()
        stopPositionPolling()

        uiState = uiState.copy(
            playerState = LullabyPlayerState(
                currentItemId   = item.id,
                isPlaying       = true,
                positionSeconds = 0,
                durationSeconds = duration
            )
        )

        lullabyPlayer?.play(assetKey)
        startPositionPolling()
    }

    fun togglePlayPause() {
        val playing = uiState.playerState.isPlaying
        if (playing) {
            lullabyPlayer?.pause()
            stopPositionPolling()
        } else {
            lullabyPlayer?.play(/* resume — platform impls handle this */ "")
            startPositionPolling()
        }
        uiState = uiState.copy(
            playerState = uiState.playerState.copy(isPlaying = !playing)
        )
    }

    fun stopLullaby() {
        lullabyPlayer?.stop()
        stopPositionPolling()
        uiState = uiState.copy(playerState = LullabyPlayerState())
    }

    fun seekTo(seconds: Int) {
        lullabyPlayer?.seekTo(seconds)
        uiState = uiState.copy(
            playerState = uiState.playerState.copy(positionSeconds = seconds)
        )
    }

    fun updatePosition(seconds: Int) {
        // Avoid recomposition flood if position has not changed
        if (uiState.playerState.positionSeconds == seconds) return
        uiState = uiState.copy(
            playerState = uiState.playerState.copy(positionSeconds = seconds)
        )
    }

    // ── Download ───────────────────────────────────────────────────────────

    /**
     * Emit a one-shot [DownloadEvent] that the screen layer consumes to
     * trigger a platform-specific file download.
     *
     * The screen should call [consumeDownloadEvent] once the download has been
     * handed off to the OS / platform API.
     */
    fun requestDownload(item: GuideItem) {
        val assetKey = item.media?.asset_key ?: return
        // Build a human-readable file name, e.g. "lale_lale_kurdan.mp3"
        val fileName = "${assetKey.removePrefix("lullaby_")}.mp3"
        uiState = uiState.copy(
            downloadEvent = DownloadEvent(assetKey = assetKey, fileName = fileName)
        )
    }

    fun consumeDownloadEvent() {
        uiState = uiState.copy(downloadEvent = null)
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    fun feedbackFor(contentId: String): CardFeedbackState =
        uiState.feedbackMap[contentId] ?: CardFeedbackState(contentId)

    fun clearError() {
        uiState = uiState.copy(errorMessage = null)
    }

    fun onDestroy() {
        lullabyPlayer?.stop()
        scope.cancel()
    }

    // ── Position polling (fallback for platforms that use callback) ─────────

    private fun startPositionPolling() {
        // If the platform already uses setOnPositionChanged (Android, Desktop),
        // this coroutine loop is redundant but harmless — it simply reads the
        // same position one more time per second.
        positionJob = scope.launch {
            while (isActive) {
                delay(500)
                // Platforms that don't push callbacks can override updatePosition
                // by injecting their own polling here.
            }
        }
    }

    private fun stopPositionPolling() {
        positionJob?.cancel()
        positionJob = null
    }
}