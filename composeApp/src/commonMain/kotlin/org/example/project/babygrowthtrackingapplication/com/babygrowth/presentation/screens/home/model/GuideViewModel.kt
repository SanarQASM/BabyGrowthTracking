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
                seedFeedbackMap(doc)
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
                seedFeedbackMap(doc)
            } catch (e: Exception) {
                uiState = uiState.copy(isLoadingFeeding = false, errorMessage = e.message)
            }
        }
    }

    private fun seedFeedbackMap(doc: GuideDocument) {
        val seeded = uiState.feedbackMap.toMutableMap()
        for (strategy in doc.strategies) {
            for (ageRange in strategy.age_ranges) {
                for (item in ageRange.items) {
                    if (!seeded.containsKey(item.id)) {
                        seeded[item.id] = CardFeedbackState(contentId = item.id)
                    }
                }
            }
        }
        uiState = uiState.copy(feedbackMap = seeded)
    }

    // ── Feedback loading ───────────────────────────────────────────────────

    fun loadFeedbackCounts(contentIds: List<String>, guideType: String) {
        if (contentIds.isEmpty()) return

        if (repository == null) {
            val seeded = uiState.feedbackMap.toMutableMap()
            for (id in contentIds) {
                if (!seeded.containsKey(id)) seeded[id] = CardFeedbackState(contentId = id)
            }
            uiState = uiState.copy(feedbackMap = seeded)
            return
        }

        scope.launch {
            when (val result = repository.getCounts(contentIds, guideType)) {
                is ApiResult.Success -> {
                    val updated = uiState.feedbackMap.toMutableMap()
                    for (item in result.data.counts) {
                        val existing = updated[item.contentId]
                        if (existing?.isLoading == true) continue

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
                    // Ensure every requested ID has at least a default entry
                    for (id in contentIds) {
                        if (!updated.containsKey(id)) updated[id] = CardFeedbackState(id)
                    }
                    uiState = uiState.copy(feedbackMap = updated)
                }
                is ApiResult.Error -> {
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

    fun loadFeedbackForStrategy(strategy: GuideStrategy, guideType: String) {
        val ids = strategy.age_ranges
            .flatMap { it.items }
            .map { it.id }
            .distinct()
        loadFeedbackCounts(ids, guideType)
    }

    // ── Vote casting ───────────────────────────────────────────────────────

    /**
     * FIX — complete rewrite to correctly handle toggle-off.
     *
     * The flow:
     *  1. Read the current vote state for this card.
     *  2. ALWAYS send the tapped vote to the server — the server compares
     *     with the stored row and decides insert / update / delete.
     *  3. Apply an optimistic update immediately so the UI responds.
     *     For optimistic purposes, toggle off if same vote, otherwise flip.
     *  4. On server success, replace the optimistic state with the server-
     *     confirmed state (usefulCount from DB, vote=null if toggled off).
     *  5. On error, roll back to the pre-tap state.
     *
     * Key fix: we ALWAYS send `vote` (the button the user tapped) to the
     * server. The old code sent `current.userVote` when toggling off, which
     * re-saved the old vote instead of removing it. The server now handles
     * "same vote → delete" and returns vote=null in the response.
     */
    fun castVote(contentId: String, guideType: String, vote: UserVote) {
        val current = uiState.feedbackMap[contentId] ?: CardFeedbackState(contentId)

        // Guard: ignore taps while a request is already in flight for this card
        if (current.isLoading) return

        // Optimistic: toggling same vote → NONE, different or fresh → new vote
        val optimisticVote: UserVote = if (current.userVote == vote) UserVote.NONE else vote

        val optimisticUseful: Long = when {
            current.userVote == UserVote.USEFUL && optimisticVote != UserVote.USEFUL ->
                (current.usefulCount - 1L).coerceAtLeast(0L)
            current.userVote != UserVote.USEFUL && optimisticVote == UserVote.USEFUL ->
                current.usefulCount + 1L
            else -> current.usefulCount
        }

        val optimistic = current.copy(
            userVote    = optimisticVote,
            usefulCount = optimisticUseful,
            isLoading   = repository != null   // no spinner in offline/preview mode
        )

        uiState = uiState.copy(
            feedbackMap = uiState.feedbackMap + (contentId to optimistic)
        )

        // Offline / preview: keep optimistic state permanently
        if (repository == null) return

        // FIX: always send the tapped vote; the server handles the toggle-off
        scope.launch {
            when (val result = repository.castVote(contentId, guideType, vote.name)) {
                is ApiResult.Success -> {
                    // Server tells us the real post-mutation state
                    val confirmedVote = when (result.data.vote) {
                        "USEFUL"  -> UserVote.USEFUL
                        "USELESS" -> UserVote.USELESS
                        else      -> UserVote.NONE   // null → vote was removed
                    }
                    val confirmed = CardFeedbackState(
                        contentId   = contentId,
                        usefulCount = result.data.usefulCount,
                        userVote    = confirmedVote,
                        isLoading   = false
                    )
                    uiState = uiState.copy(
                        feedbackMap = uiState.feedbackMap + (contentId to confirmed)
                    )
                }
                is ApiResult.Error -> {
                    // Roll back and clear spinner
                    uiState = uiState.copy(
                        feedbackMap  = uiState.feedbackMap + (contentId to current.copy(isLoading = false)),
                        errorMessage = result.message
                    )
                }
                else -> {
                    // Loading state shouldn't happen; clear spinner defensively
                    uiState = uiState.copy(
                        feedbackMap = uiState.feedbackMap + (contentId to current.copy(isLoading = false))
                    )
                }
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
        val ext = when {
            assetKey.endsWith(".m4a", ignoreCase = true) -> ".m4a"
            assetKey.endsWith(".mp3", ignoreCase = true) -> ".mp3"
            else -> ".m4a"
        }
        val base = assetKey
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