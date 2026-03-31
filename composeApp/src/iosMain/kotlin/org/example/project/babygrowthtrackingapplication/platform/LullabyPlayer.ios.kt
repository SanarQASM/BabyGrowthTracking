package org.example.project.babygrowthtrackingapplication.platform
// iosMain
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFAudio.AVAudioPlayer
import platform.Foundation.NSBundle
import platform.Foundation.NSURL

actual class LullabyPlayer {
    private var player: AVAudioPlayer? = null
    private var onPositionChanged: ((Int) -> Unit)? = null

    @OptIn(ExperimentalForeignApi::class)
    actual fun play(assetKey: String) {
        if (player == null) {
            val path = NSBundle.mainBundle.pathForResource(assetKey, null)
            val url = NSURL.fileURLWithPath(path!!)
            player = AVAudioPlayer(contentsOfURL = url, error = null)
            player?.prepareToPlay()
        }
        player?.play()
    }

    actual fun pause() {
        player?.pause()
    }

    actual fun stop() {
        player?.stop()
        player = null
    }

    actual fun seekTo(seconds: Int) {
        player?.setCurrentTime(seconds.toDouble())
    }

    actual fun setOnPositionChanged(callback: (Int) -> Unit) {
        onPositionChanged = callback
    }
}