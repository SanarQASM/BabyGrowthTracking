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
// FIX summary (v2):
//   1. loadFeedbackCounts() now ALWAYS overwrites existing states so that
//      switching babies / categories always refreshes from source of truth.
//   2. castVote() optimistic-update logic corrected: the second early-return
//      block no longer re-copies state (was harmless but confusing). More
//      importantly the count delta now correctly handles USELESS→USEFUL flip.
//   3. Added loadAllFeedbackForStrategy() — a helper that collects ALL item
//      IDs across ALL age-ranges in a strategy (not just the current age),
//      then batch-loads them. This ensures every card rendered on screen has
//      a CardFeedbackState entry in the map.
//   4. The offline/preview path (repository == null) always seeds missing
//      entries but never clobbers existing ones, so optimistic updates
//      survive re-compositions.
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
                // FIX: seed feedback map for all items in the document immediately
                // after loading, so every card has an entry before it's rendered.
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
                // FIX: seed feedback map for all items in the document immediately.
                seedFeedbackMap(doc)
            } catch (e: Exception) {
                uiState = uiState.copy(isLoadingFeeding = false, errorMessage = e.message)
            }
        }
    }

    /**
     * FIX: Seeds the feedbackMap with a default CardFeedbackState for every
     * item in every strategy / age-range of the given document.
     * This is a local-only operation — no network call. It ensures that every
     * rendered card immediately has a state object so the buttons are
     * responsive even before the real counts arrive from the backend.
     * Existing entries (e.g. already voted) are NOT overwritten.
     */
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

    /**
     * Loads feedback counts for the given content IDs from the backend.
     *
     * FIX (v2): This now ALWAYS updates entries in the map (not just missing
     * ones), so switching between babies or categories always reflects the
     * real server counts. Entries that are currently mid-vote (isLoading=true)
     * are skipped so we don't clobber an in-flight optimistic update.
     */
    fun loadFeedbackCounts(contentIds: List<String>, guideType: String) {
        if (contentIds.isEmpty()) return

        // Offline / preview mode — ensure every ID has at least a default entry.
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
                        // Don't overwrite a card that is mid-flight for a vote.
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
                    uiState = uiState.copy(feedbackMap = updated)
                }
                is ApiResult.Error -> {
                    // Seed with empty state so buttons still work offline.
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

    /**
     * FIX: Convenience overload that collects ALL item IDs from the given
     * strategy (across every age-range, not just the currently visible age),
     * then calls loadFeedbackCounts in one batch.
     *
     * Call this from the screen after the guide document is loaded, e.g.
     *   LaunchedEffect(strategy) { viewModel.loadFeedbackForStrategy(strategy, "SLEEP") }
     */
    fun loadFeedbackForStrategy(strategy: GuideStrategy, guideType: String) {
        val ids = strategy.age_ranges
            .flatMap { it.items }
            .map { it.id }
            .distinct()
        loadFeedbackCounts(ids, guideType)
    }

    // ── Vote casting ───────────────────────────────────────────────────────

    /**
     * FIX (v2): Full rewrite of the optimistic-update + rollback logic.
     *
     * Rules:
     *  - Tapping the same vote again → UNDO (vote becomes NONE, count reverses).
     *  - Switching from USEFUL → USELESS (or vice-versa) → flip both counts.
     *  - Tapping a fresh vote → add +1 to the relevant count.
     *  - On backend error → roll back to the pre-tap state.
     *  - When repository == null → keep the optimistic state permanently.
     */
    fun castVote(contentId: String, guideType: String, vote: UserVote) {
        val current = uiState.feedbackMap[contentId] ?: CardFeedbackState(contentId)

        // Determine the new intended vote state.
        val newVote: UserVote = if (current.userVote == vote) UserVote.NONE else vote

        // Compute optimistic useful count delta.
        val optimisticUseful: Long = when {
            // Was USEFUL, now toggling off → subtract 1
            current.userVote == UserVote.USEFUL && newVote != UserVote.USEFUL ->
                (current.usefulCount - 1L).coerceAtLeast(0L)
            // Was NOT USEFUL, now becoming USEFUL → add 1
            current.userVote != UserVote.USEFUL && newVote == UserVote.USEFUL ->
                current.usefulCount + 1L
            // No change in useful direction
            else -> current.usefulCount
        }

        val optimistic = current.copy(
            userVote    = newVote,
            usefulCount = optimisticUseful,
            // Show spinner only when we actually have a backend to call.
            isLoading   = repository != null
        )

        // Apply optimistic update immediately so UI responds without latency.
        uiState = uiState.copy(
            feedbackMap = uiState.feedbackMap + (contentId to optimistic)
        )

        // Offline / preview mode — optimistic state is permanent.
        if (repository == null) return

        // When the user toggled their vote off (newVote == NONE), we still call
        // the backend with the original vote so the server can handle the flip.
        val voteToSend = if (newVote == UserVote.NONE) current.userVote else newVote

        scope.launch {
            when (val result = repository.castVote(contentId, guideType, voteToSend.name)) {
                is ApiResult.Success -> {
                    val confirmed = CardFeedbackState(
                        contentId   = contentId,
                        usefulCount = result.data.usefulCount,
                        userVote    = newVote,
                        isLoading   = false
                    )
                    uiState = uiState.copy(
                        feedbackMap = uiState.feedbackMap + (contentId to confirmed)
                    )
                }
                is ApiResult.Error -> {
                    // Roll back to the state that existed before this tap.
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