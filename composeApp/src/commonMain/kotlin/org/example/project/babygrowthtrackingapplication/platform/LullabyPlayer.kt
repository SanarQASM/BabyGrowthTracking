package org.example.project.babygrowthtrackingapplication.platform

expect class LullabyPlayer {

    /** Start playback of [assetKey]. Blank string = resume after pause. */
    fun play(assetKey: String)

    /** Pause playback. Resources are kept; resume with play(""). */
    fun pause()

    /** Stop playback and release all native resources. */
    fun stop()

    /** Seek to [seconds] from the beginning of the track. */
    fun seekTo(seconds: Int)

    /**
     * Register a callback that is called every ~500 ms during playback
     * with the current position in seconds.
     */
    fun setOnPositionChanged(callback: (Int) -> Unit)

    /**
     * Register a callback fired when the track finishes playing naturally.
     * Not called when [stop] is invoked programmatically.
     */
    fun setOnCompleted(callback: () -> Unit)

    /** Returns true while audio is actively playing. */
    fun isPlaying(): Boolean

    /** Current playback position in seconds (0 if nothing loaded). */
    fun currentPosition(): Int

    /** Total duration of the current track in seconds (0 if nothing loaded). */
    fun duration(): Int

    /** Convenience alias for [stop] — call before discarding the player. */
    fun release()
}