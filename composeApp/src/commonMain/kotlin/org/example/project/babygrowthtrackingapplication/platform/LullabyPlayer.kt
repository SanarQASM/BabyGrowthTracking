package org.example.project.babygrowthtrackingapplication.platform

expect class LullabyPlayer {
    fun play(assetKey: String)
    fun pause()
    fun stop()
    fun seekTo(seconds: Int)
    fun setOnPositionChanged(callback: (Int) -> Unit)
}