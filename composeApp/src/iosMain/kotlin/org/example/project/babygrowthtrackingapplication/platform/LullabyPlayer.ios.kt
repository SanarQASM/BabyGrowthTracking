package org.example.project.babygrowthtrackingapplication.platform
// iosMain

import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFAudio.AVAudioPlayer
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.setActive
import platform.Foundation.NSBundle
import platform.Foundation.NSTimer
import platform.Foundation.NSURL

// ═══════════════════════════════════════════════════════════════════════════
// LullabyPlayer.ios.kt  —  iosMain
//
// FIX: resolveUrl() tries .m4a first, then .mp3.
//      Add each audio file to Xcode → Build Phases → Copy Bundle Resources.
//
// Background audio: add to Info.plist:
//   <key>UIBackgroundModes</key><array><string>audio</string></array>
// ═══════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalForeignApi::class)
actual class LullabyPlayer {

    private var player           : AVAudioPlayer? = null
    private var onPositionChanged: ((Int) -> Unit)? = null
    private var onCompleted      : (() -> Unit)?    = null
    private var timer            : NSTimer?         = null
    private var currentAssetKey  : String?          = null

    init {
        try {
            val session = AVAudioSession.sharedInstance()
            session.setCategory(AVAudioSessionCategoryPlayback, error = null)
            session.setActive(true, error = null)
        } catch (_: Exception) {}
    }

    actual fun play(assetKey: String) {
        when {
            assetKey.isBlank() -> { player?.play(); startTimer() }
            assetKey == currentAssetKey && player != null -> {
                if (player?.isPlaying() == false) { player?.play(); startTimer() }
            }
            else -> {
                releaseInternal()
                currentAssetKey = assetKey
                // FIX: probe .m4a first, then .mp3
                val url = resolveUrl(assetKey)
                if (url == null) {
                    println("[LullabyPlayer/iOS] Not found in bundle: $assetKey (.m4a / .mp3)")
                    return
                }
                val p = AVAudioPlayer(contentsOfURL = url, error = null) ?: run {
                    println("[LullabyPlayer/iOS] Could not create player for $assetKey"); return
                }
                player = p.apply { prepareToPlay(); play() }
                startTimer()
            }
        }
    }

    actual fun pause()  { player?.pause(); stopTimer() }

    actual fun stop() {
        stopTimer(); releaseInternal()
        currentAssetKey = null; onPositionChanged?.invoke(0)
    }

    actual fun seekTo(seconds: Int) {
        player?.let { p ->
            p.setCurrentTime(seconds.toDouble().coerceIn(0.0, p.duration))
            onPositionChanged?.invoke(seconds)
        }
    }

    actual fun setOnPositionChanged(callback: (Int) -> Unit) { onPositionChanged = callback }
    actual fun setOnCompleted(callback: () -> Unit)          { onCompleted = callback }

    actual fun isPlaying(): Boolean  = player?.isPlaying() ?: false
    actual fun currentPosition(): Int = player?.currentTime?.toInt() ?: 0
    actual fun duration(): Int        = player?.duration?.toInt()    ?: 0
    actual fun release()              = stop()

    // FIX: try .m4a first, then .mp3
    private fun resolveUrl(key: String): NSURL? {
        val baseName = key.removeSuffix(".mp3").removeSuffix(".m4a")
        for (ext in listOf("m4a", "mp3")) {
            val path = NSBundle.mainBundle.pathForResource(baseName, ofType = ext)
            if (path != null) return NSURL.fileURLWithPath(path)
        }
        return null
    }

    private fun startTimer() {
        stopTimer()
        timer = NSTimer.scheduledTimerWithTimeInterval(0.5, repeats = true) { _ ->
            val p = player ?: return@scheduledTimerWithTimeInterval
            onPositionChanged?.invoke(p.currentTime.toInt())
            if (p.duration > 0.0 && p.currentTime >= p.duration - 0.2) {
                onCompleted?.invoke()
                stop()
            }
        }
    }

    private fun stopTimer()       { timer?.invalidate(); timer = null }
    private fun releaseInternal() { player?.stop(); player = null }
}

actual fun createLullabyPlayer(): LullabyPlayer = LullabyPlayer()