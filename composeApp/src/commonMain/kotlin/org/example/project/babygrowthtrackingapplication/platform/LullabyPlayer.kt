package org.example.project.babygrowthtrackingapplication.platform

// ═══════════════════════════════════════════════════════════════════════════
// LullabyPlayer.kt  —  commonMain  (expect)
//
// WHY THE CONSTRUCTOR IS PARAMETER-LESS HERE
// ───────────────────────────────────────────
// Kotlin Multiplatform expect/actual classes must share the same constructor
// signature in commonMain.  Android's MediaPlayer needs a Context, but we
// cannot put platform types in commonMain.
//
// SOLUTION — createLullabyPlayer() factory
// ─────────────────────────────────────────
// An expect top-level function createLullabyPlayer() is declared here and
// implemented in every source set.  On Android the actual reads the
// application Context from AppContextHolder (a tiny global set in
// Application.onCreate).  All other platforms use a no-arg constructor.
//
// The ViewModel calls createLullabyPlayer() once in its init block, so it
// always gets a fully initialised, non-null player.
// ═══════════════════════════════════════════════════════════════════════════

expect class LullabyPlayer {
    fun play(assetKey: String)
    fun pause()
    fun stop()
    fun seekTo(seconds: Int)
    fun setOnPositionChanged(callback: (Int) -> Unit)
    fun setOnCompleted(callback: () -> Unit)
    fun isPlaying(): Boolean
    fun currentPosition(): Int
    fun duration(): Int
    fun release()
}

/**
 * Platform factory — call this instead of a constructor.
 * Each source set provides its own actual implementation.
 */
expect fun createLullabyPlayer(): LullabyPlayer