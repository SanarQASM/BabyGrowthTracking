package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repository for persisting guide votes in-memory during a session.
 *
 * Votes (useful / useless) are held in a [StateFlow]-backed map so that any
 * collector (e.g. a Composable or ViewModel) automatically receives updates
 * whenever a vote changes — no manual refresh needed.
 *
 * Lifecycle notes:
 *  - Votes survive recomposition and navigation within the same session.
 *  - Call [clearAll] on user-logout or an explicit "reset" action.
 *  - For cross-session persistence, wrap this class with Multiplatform Settings
 *    (already on the classpath via `libs.multiplatform.settings`) or DataStore.
 *
 * The Blink SDK (`@blinkdotnew/sdk`) is a JavaScript/TypeScript SDK and cannot
 * be accessed directly from Kotlin Multiplatform commonMain code.  If server-side
 * vote aggregation is needed in the future, delegate to an edge function via the
 * existing [org.example.project.babygrowthtrackingapplication.data.network.ApiService].
 */
class GuideRepository {

    // ── Internal state ────────────────────────────────────────────────────────

    private val _votes = MutableStateFlow<Map<String, GuideVote>>(emptyMap())

    /**
     * Reactive snapshot of all current votes, keyed by [GuideVote.itemId].
     * Collect this flow in a Composable with [kotlinx.coroutines.flow.collectAsState]
     * or inside a ViewModel with [kotlinx.coroutines.flow.stateIn].
     */
    val votes: StateFlow<Map<String, GuideVote>> = _votes.asStateFlow()

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Cast or toggle a vote for a guide item.
     *
     * Business rules:
     *  - **Toggle off**: tapping the currently-active vote type removes it
     *    (count decremented, [VoteType.NONE] restored).
     *  - **First vote**: no previous vote exists; count incremented, vote stored.
     *  - **Switch vote**: previous vote is undone first (count decremented), then
     *    the new vote is applied (count incremented).
     *
     * @param itemId Unique identifier of the guide item being voted on.
     * @param type   The [VoteType] the user selected ([VoteType.USEFUL] or [VoteType.USELESS]).
     */
    fun vote(itemId: String, type: VoteType) {
        val current = _votes.value[itemId] ?: GuideVote(itemId)

        val updated: GuideVote = when {

            // ── Toggle off: same vote tapped again ────────────────────────────
            current.userVote == type -> current.copy(
                userVote     = VoteType.NONE,
                usefulCount  = if (type == VoteType.USEFUL)
                    (current.usefulCount  - 1).coerceAtLeast(0)
                else
                    current.usefulCount,
                uselessCount = if (type == VoteType.USELESS)
                    (current.uselessCount - 1).coerceAtLeast(0)
                else
                    current.uselessCount
            )

            // ── First vote: no previous vote ──────────────────────────────────
            current.userVote == VoteType.NONE -> current.copy(
                userVote     = type,
                usefulCount  = if (type == VoteType.USEFUL)  current.usefulCount  + 1 else current.usefulCount,
                uselessCount = if (type == VoteType.USELESS) current.uselessCount + 1 else current.uselessCount
            )

            // ── Switch vote: undo previous then apply new ─────────────────────
            else -> {
                val undone = when (current.userVote) {
                    VoteType.USEFUL  -> current.copy(usefulCount  = (current.usefulCount  - 1).coerceAtLeast(0))
                    VoteType.USELESS -> current.copy(uselessCount = (current.uselessCount - 1).coerceAtLeast(0))
                    else             -> current
                }
                undone.copy(
                    userVote     = type,
                    usefulCount  = if (type == VoteType.USEFUL)  undone.usefulCount  + 1 else undone.usefulCount,
                    uselessCount = if (type == VoteType.USELESS) undone.uselessCount + 1 else undone.uselessCount
                )
            }
        }

        _votes.value = _votes.value + (itemId to updated)
    }

    /**
     * Convenience overloads — match the naming used by [GuideViewModel] so
     * callers can migrate to the repository without changing call-sites.
     */
    fun voteSleepItem(itemId: String, type: VoteType)   = vote(itemId, type)
    fun voteFeedingItem(itemId: String, type: VoteType) = vote(itemId, type)

    /**
     * Returns the current [GuideVote] for [itemId], or a default (zero counts,
     * [VoteType.NONE]) if the item has never been voted on.
     */
    fun getVote(itemId: String): GuideVote =
        _votes.value[itemId] ?: GuideVote(itemId)

    /**
     * Returns a point-in-time snapshot of every stored vote.
     * For reactive updates, collect [votes] instead.
     */
    fun getAllVotes(): Map<String, GuideVote> = _votes.value

    /**
     * Clears all in-memory votes.
     * Call this on user logout or when a full reset is required.
     */
    fun clearAll() {
        _votes.value = emptyMap()
    }
}
