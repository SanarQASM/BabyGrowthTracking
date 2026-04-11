package org.example.project.babygrowthtrackingapplication.util

import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class DataCache<T>(private val ttl: Duration = 5.minutes) {
    private var cachedData: T? = null
    private var lastFetchTime: Long = 0L

    fun get(): T? = if (isFresh()) cachedData else null

    fun set(data: T) {
        cachedData = data
        lastFetchTime = Clock.System.now().toEpochMilliseconds()
    }

    fun invalidate() {
        cachedData = null
        lastFetchTime = 0L
    }

    fun isFresh(): Boolean {
        if (cachedData == null) return false
        val age = Clock.System.now().toEpochMilliseconds() - lastFetchTime
        return age < ttl.inWholeMilliseconds
    }
}