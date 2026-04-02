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
// AUDIO FILES
// ───────────
// Add each MP3 to your Xcode project and make sure it is listed in:
//   Build Phases → Copy Bundle Resources
// File names must match the asset_key + ".mp3", e.g.:
//   lullaby_lale_lale_kurdan.mp3
//   lullaby_dilber_dilber.mp3
//   lullaby_ya_mulay.mp3
//   lullaby_nami_nami.mp3
//   lullaby_twinkle.mp3
//   lullaby_hush_little_baby.mp3
//
// BACKGROUND AUDIO
// ────────────────
// To keep playback going when the screen is locked, add to Info.plist:
//   <key>UIBackgroundModes</key>
//   <array><string>audio</string></array>
// ═══════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalForeignApi::class)
actual class LullabyPlayer {

    private var player           : AVAudioPlayer? = null
    private var onPositionChanged: ((Int) -> Unit)? = null
    private var onCompleted      : (() -> Unit)?    = null
    private var timer            : NSTimer?         = null
    private var currentAssetKey  : String?          = null

    init {
        // Activate audio session: plays even in silent mode
        try {
            val session = AVAudioSession.sharedInstance()
            session.setCategory(AVAudioSessionCategoryPlayback, error = null)
            session.setActive(true, error = null)
        } catch (_: Exception) {}
    }

    // ── play ──────────────────────────────────────────────────────────────
    actual fun play(assetKey: String) {
        when {
            assetKey.isBlank() -> {
                player?.play(); startTimer()
            }
            assetKey == currentAssetKey && player != null -> {
                if (player?.isPlaying() == false) { player?.play(); startTimer() }
            }
            else -> {
                releaseInternal()
                currentAssetKey = assetKey
                val name = assetKey.removeSuffix(".mp3")
                val path = NSBundle.mainBundle.pathForResource(name, ofType = "mp3")
                if (path == null) {
                    println("[LullabyPlayer/iOS] Not found in bundle: $name.mp3")
                    return
                }
                val url = NSURL.fileURLWithPath(path)
                val p   = AVAudioPlayer(contentsOfURL = url, error = null) ?: run {
                    println("[LullabyPlayer/iOS] Could not create player for $name"); return
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

    // ── Timer helpers ─────────────────────────────────────────────────────
    private fun startTimer() {
        stopTimer()
        timer = NSTimer.scheduledTimerWithTimeInterval(0.5, repeats = true) { _ ->
            val p = player ?: return@scheduledTimerWithTimeInterval
            onPositionChanged?.invoke(p.currentTime.toInt())
            // Detect natural end (no delegate in K/N without bridging)
            if (p.duration > 0.0 && p.currentTime >= p.duration - 0.2) {
                onCompleted?.invoke()
                stop()
            }
        }
    }

    private fun stopTimer()      { timer?.invalidate(); timer = null }
    private fun releaseInternal() { player?.stop(); player = null }
}

actual fun createLullabyPlayer(): LullabyPlayer = LullabyPlayer()