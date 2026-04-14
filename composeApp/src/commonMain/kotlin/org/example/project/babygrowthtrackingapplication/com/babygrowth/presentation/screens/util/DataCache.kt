package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.util

import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime

// ─────────────────────────────────────────────────────────────────────────────
// DataCache — UNCHANGED (the class itself was correct)
//
// The only problem was that it was never instantiated anywhere. The fix is
// in HomeViewModel.kt and NotificationViewModel.kt where DataCache is now
// used to avoid redundant network calls.
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalTime::class)
class DataCache<T>(private val ttl: Duration = 5.minutes) {

    private var cachedData   : T?   = null
    private var lastFetchTime: Long = 0L

    /** Returns cached data if still fresh, or null if stale / empty. */
    fun get(): T? = if (isFresh()) cachedData else null

    /** Store new data and record the fetch timestamp. */
    fun set(data: T) {
        cachedData    = data
        lastFetchTime = Clock.System.now().toEpochMilliseconds()
    }

    /** Force the next call to re-fetch from the network. */
    fun invalidate() {
        cachedData    = null
        lastFetchTime = 0L
    }

    /** True if cached data exists and was fetched within the TTL window. */
    fun isFresh(): Boolean {
        if (cachedData == null) return false
        val age = Clock.System.now().toEpochMilliseconds() - lastFetchTime
        return age < ttl.inWholeMilliseconds
    }
}