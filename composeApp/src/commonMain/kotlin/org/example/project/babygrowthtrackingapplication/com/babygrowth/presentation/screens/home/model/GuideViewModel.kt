package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model

import androidx.compose.runtime.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import org.example.project.babygrowthtrackingapplication.data.network.ApiResult
import org.example.project.babygrowthtrackingapplication.data.repository.GuideRepository
import org.example.project.babygrowthtrackingapplication.platform.LullabyPlayer
import org.example.project.babygrowthtrackingapplication.platform.createLullabyPlayer
import org.jetbrains.compose.resources.ExperimentalResourceApi
import babygrowthtrackingapplication.composeapp.generated.resources.Res

// ═══════════════════════════════════════════════════════════════════════════
// GuideViewModel.kt
//
// FIX summary:
//   1. lullabyPlayer now defaults to createLullabyPlayer() instead of null,
//      so audio always works even when the DI-injected player is absent.
//   2. loadFeedbackCounts() and castVote() now work in offline/preview mode:
//      when repository == null the feedback state is still updated locally
//      so the UI toggles correctly (no backend call, but buttons respond).
//   3. CardFeedbackState.usefulCount is now Long to match backend.
// ═══════════════════════════════════════════════════════════════════════════

private val guideJson = Json {
    ignoreUnknownKeys = true
    isLenient         = true
}

// ── UI State ──────────────────────────────────────────────────────────────

data class GuideUiState(
    val sleepGuide      : GuideDocument?                 = null,
    val feedingGuide    : GuideDocument?                 = null,
    val isLoadingSleep  : Boolean                        = false,
    val isLoadingFeeding: Boolean                        = false,
    val feedbackMap     : Map<String, CardFeedbackState> = emptyMap(),
    val playerState     : LullabyPlayerState             = LullabyPlayerState(),
    val errorMessage    : String?                        = null,
    val downloadEvent   : DownloadEvent?                 = null
)

data class DownloadEvent(
    val assetKey : String,
    val fileName : String,
    val consumed : Boolean = false
)

// ── ViewModel ─────────────────────────────────────────────────────────────

class GuideViewModel(
    private val repository   : GuideRepository? = null,
    // FIX: default to createLullabyPlayer() so audio works out-of-the-box
    // without a DI framework. Pass an explicit instance from your DI graph
    // when you need lifecycle control (e.g. Activity-scoped player).
    private val lullabyPlayer: LullabyPlayer = createLullabyPlayer()
) {
    var uiState by mutableStateOf(GuideUiState())
        private set

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var positionJob: Job? = null

    init {
        lullabyPlayer.setOnPositionChanged { seconds -> updatePosition(seconds) }
        lullabyPlayer.setOnCompleted {
            uiState = uiState.copy(playerState = uiState.playerState.copy(isPlaying = false))
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
        if (contentIds.isEmpty()) return

        // FIX: if no repository, seed each card with 0 count + NONE vote so the
        // UI is at least functional (buttons respond, count shows 0)
        if (repository == null) {
            val seeded = uiState.feedbackMap.toMutableMap()
            for (id in contentIds) {
                if (!seeded.containsKey(id)) {
                    seeded[id] = CardFeedbackState(contentId = id)
                }
            }
            uiState = uiState.copy(feedbackMap = seeded)
            return
        }

        scope.launch {
            when (val result = repository.getCounts(contentIds, guideType)) {
                is ApiResult.Success -> {
                    val updated = uiState.feedbackMap.toMutableMap()
                    for (item in result.data.counts) {
                        updated[item.contentId] = CardFeedbackState(
                            contentId   = item.contentId,
                            usefulCount = item.usefulCount,        // Long
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
                is ApiResult.Error -> {
                    // Seed with empty state so buttons still work offline
                    val seeded = uiState.feedbackMap.toMutableMap()
                    for (id in contentIds) {
                        if (!seeded.containsKey(id)) seeded[id] = CardFeedbackState(id)
                    }
                    uiState = uiState.copy(feedbackMap = seeded, errorMessage = result.message)
                }
                else -> Unit
            }
        }
    }

    // ── Vote casting ───────────────────────────────────────────────────────

    fun castVote(contentId: String, guideType: String, vote: UserVote) {
        val current = uiState.feedbackMap[contentId] ?: CardFeedbackState(contentId)

        // FIX: toggling the same vote removes it (acts as undo)
        val newVote: UserVote = if (current.userVote == vote) UserVote.NONE else vote

        val optimistic = current.copy(
            userVote    = newVote,
            usefulCount = when {
                newVote == UserVote.USEFUL && current.userVote != UserVote.USEFUL ->
                    current.usefulCount + 1L
                newVote != UserVote.USEFUL && current.userVote == UserVote.USEFUL ->
                    (current.usefulCount - 1L).coerceAtLeast(0L)
                else -> current.usefulCount
            },
            isLoading = repository != null   // only show spinner when we have a real backend
        )
        uiState = uiState.copy(feedbackMap = uiState.feedbackMap + (contentId to optimistic))

        // FIX: no repository → keep the optimistic state permanently (offline/preview mode)
        if (repository == null) {
            uiState = uiState.copy(
                feedbackMap = uiState.feedbackMap + (contentId to optimistic.copy(isLoading = false))
            )
            return
        }

        // When the user toggled their vote off (newVote == NONE), we still call
        // the backend with the original vote so the server can handle the toggle.
        val voteToSend = if (newVote == UserVote.NONE) current.userVote else newVote

        scope.launch {
            when (val result = repository.castVote(contentId, guideType, voteToSend.name)) {
                is ApiResult.Success -> {
                    val confirmed = CardFeedbackState(
                        contentId   = contentId,
                        usefulCount = result.data.usefulCount,   // Long from backend
                        userVote    = newVote,
                        isLoading   = false
                    )
                    uiState = uiState.copy(
                        feedbackMap = uiState.feedbackMap + (contentId to confirmed)
                    )
                }
                is ApiResult.Error -> {
                    // Roll back to previous state on error
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

    fun playLullaby(item: GuideItem) {
        val assetKey = item.media?.asset_key ?: return
        val duration = item.media.duration_seconds

        lullabyPlayer.stop()
        stopPositionPolling()

        uiState = uiState.copy(
            playerState = LullabyPlayerState(
                currentItemId   = item.id,
                isPlaying       = true,
                positionSeconds = 0,
                durationSeconds = duration
            )
        )

        lullabyPlayer.play(assetKey)
        startPositionPolling()
    }

    fun togglePlayPause() {
        val playing = uiState.playerState.isPlaying
        if (playing) {
            lullabyPlayer.pause()
            stopPositionPolling()
        } else {
            lullabyPlayer.play("")
            startPositionPolling()
        }
        uiState = uiState.copy(playerState = uiState.playerState.copy(isPlaying = !playing))
    }

    fun stopLullaby() {
        lullabyPlayer.stop()
        stopPositionPolling()
        uiState = uiState.copy(playerState = LullabyPlayerState())
    }

    fun seekTo(seconds: Int) {
        lullabyPlayer.seekTo(seconds)
        uiState = uiState.copy(playerState = uiState.playerState.copy(positionSeconds = seconds))
    }

    fun updatePosition(seconds: Int) {
        if (uiState.playerState.positionSeconds == seconds) return
        uiState = uiState.copy(playerState = uiState.playerState.copy(positionSeconds = seconds))
    }

    // ── Download ───────────────────────────────────────────────────────────

    fun requestDownload(item: GuideItem) {
        val assetKey = item.media?.asset_key ?: return
        // FIX: preserve the original extension from the asset_key if present,
        // otherwise default to .m4a (matching our audio files)
        val ext      = when {
            assetKey.endsWith(".m4a", ignoreCase = true) -> ".m4a"
            assetKey.endsWith(".mp3", ignoreCase = true) -> ".mp3"
            else -> ".m4a"
        }
        val base     = assetKey
            .removePrefix("lullaby_")
            .removeSuffix(".m4a")
            .removeSuffix(".mp3")
        uiState = uiState.copy(
            downloadEvent = DownloadEvent(assetKey = assetKey, fileName = "$base$ext")
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
        lullabyPlayer.stop()
        lullabyPlayer.release()
        scope.cancel()
    }

    private fun startPositionPolling() {
        positionJob = scope.launch {
            while (isActive) {
                delay(500)
                // Platforms that push callbacks (Android, iOS, Desktop) already
                // call updatePosition via setOnPositionChanged.  This loop is a
                // no-op for those platforms but acts as the primary mechanism for
                // any platform that doesn't implement the callback.
                val pos = lullabyPlayer.currentPosition()
                if (pos > 0) updatePosition(pos)
            }
        }
    }

    private fun stopPositionPolling() {
        positionJob?.cancel()
        positionJob = null
    }
}