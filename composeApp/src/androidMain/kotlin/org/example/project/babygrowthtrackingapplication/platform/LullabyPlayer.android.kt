package org.example.project.babygrowthtrackingapplication.platform
// androidMain
import android.media.MediaPlayer
import android.content.res.AssetFileDescriptor
import android.content.Context

actual class LullabyPlayer(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private var onPositionChanged: ((Int) -> Unit)? = null

    actual fun play(assetKey: String) {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer().apply {
                val descriptor: AssetFileDescriptor = context.assets.openFd(assetKey)
                setDataSource(descriptor.fileDescriptor, descriptor.startOffset, descriptor.length)
                prepare()
                setOnCompletionListener { stop() }
            }
        }
        mediaPlayer?.start()
    }

    actual fun pause() {
        mediaPlayer?.pause()
    }

    actual fun stop() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    actual fun seekTo(seconds: Int) {
        mediaPlayer?.seekTo(seconds * 1000)
    }

    actual fun setOnPositionChanged(callback: (Int) -> Unit) {
        onPositionChanged = callback
        // In a real app, use a Coroutine loop or Handler to poll mediaPlayer.currentPosition
    }
}