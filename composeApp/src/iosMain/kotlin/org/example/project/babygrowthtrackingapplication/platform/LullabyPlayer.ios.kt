package org.example.project.babygrowthtrackingapplication.platform
// iosMain

import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFAudio.AVAudioPlayer
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.setActive
import platform.Foundation.NSBundle
import platform.Foundation.NSError
import platform.Foundation.NSTimer
import platform.Foundation.NSURL

// ═══════════════════════════════════════════════════════════════════════════
// LullabyPlayer.ios.kt  —  iosMain
//
// Audio backend : AVAudioPlayer (AVFoundation)
// Asset lookup  : NSBundle.mainBundle — the audio file (e.g.
//                 "lullaby_lale_lale_kurdan.mp3") must be included in the
//                 Xcode target's "Copy Bundle Resources" phase.
// Position poll : NSTimer at 0.5 s interval on the main run loop.
// Category      : AVAudioSessionCategoryPlayback so audio continues when
//                 the device is on silent / screen-off.
//
// IMPORTANT — asset path convention
// ──────────────────────────────────
// assetKey e.g. "lullaby_lale_lale_kurdan" → looks for
// "lullaby_lale_lale_kurdan.mp3" in the main bundle.
// ═══════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalForeignApi::class)
actual class LullabyPlayer {

    private var player           : AVAudioPlayer? = null
    private var onPositionChanged: ((Int) -> Unit)? = null
    private var onCompleted      : (() -> Unit)?    = null
    private var timer            : NSTimer?         = null
    private var currentAssetKey  : String?          = null

    init {
        // Activate the audio session so playback works in silent mode /
        // when the screen is locked (background audio requires the
        // UIBackgroundModes → audio key in Info.plist as well).
        try {
            val session = AVAudioSession.sharedInstance()
            session.setCategory(AVAudioSessionCategoryPlayback, error = null)
            session.setActive(true, error = null)
        } catch (_: Exception) { }
    }

    // ── play ──────────────────────────────────────────────────────────────

    actual fun play(assetKey: String) {
        when {
            // Resume
            assetKey.isBlank() -> {
                player?.play()
                startTimer()
                return
            }

            // Same track — resume if paused
            assetKey == currentAssetKey && player != null -> {
                if (player?.isPlaying() == false) {
                    player?.play()
                    startTimer()
                }
                return
            }

            // New track
            else -> {
                releaseInternal()
                currentAssetKey = assetKey
            }
        }

        val fileName = assetKey.removeSuffix(".mp3")
        val path     = NSBundle.mainBundle.pathForResource(fileName, ofType = "mp3")
        if (path == null) {
            println("[LullabyPlayer] Asset not found in bundle: $fileName.mp3")
            return
        }

        val url    = NSURL.fileURLWithPath(path)
        var error  : NSError? = null
        val newPlayer = AVAudioPlayer(contentsOfURL = url, error = null)
        if (newPlayer == null) {
            println("[LullabyPlayer] Failed to create AVAudioPlayer for $fileName")
            return
        }

        player = newPlayer.apply {
            prepareToPlay()
            // AVAudioPlayer has no native completion callback in K/N without
            // a delegate; we poll and detect completion via currentTime ≥ duration.
            play()
        }
        startTimer()
    }

    // ── pause ─────────────────────────────────────────────────────────────

    actual fun pause() {
        player?.pause()
        stopTimer()
    }

    // ── stop ──────────────────────────────────────────────────────────────

    actual fun stop() {
        stopTimer()
        releaseInternal()
        currentAssetKey = null
        onPositionChanged?.invoke(0)
    }

    // ── seekTo ────────────────────────────────────────────────────────────

    actual fun seekTo(seconds: Int) {
        player?.let { p ->
            val clamped = seconds.toDouble().coerceIn(0.0, p.duration)
            p.setCurrentTime(clamped)
            onPositionChanged?.invoke(seconds)
        }
    }

    // ── callbacks ─────────────────────────────────────────────────────────

    actual fun setOnPositionChanged(callback: (Int) -> Unit) {
        onPositionChanged = callback
    }

    actual fun setOnCompleted(callback: () -> Unit) {
        onCompleted = callback
    }

    // ── state queries ─────────────────────────────────────────────────────

    actual fun isPlaying(): Boolean = player?.isPlaying() ?: false

    actual fun currentPosition(): Int = player?.currentTime?.toInt() ?: 0

    actual fun duration(): Int = player?.duration?.toInt() ?: 0

    // ── release ───────────────────────────────────────────────────────────

    actual fun release() = stop()

    // ── internal helpers ──────────────────────────────────────────────────

    private fun startTimer() {
        stopTimer()
        // NSTimer repeating every 0.5 seconds
        timer = NSTimer.scheduledTimerWithTimeInterval(
            interval  = 0.5,
            repeats   = true,
            block     = { _ ->
                val p = player ?: return@scheduledTimerWithTimeInterval
                val pos = p.currentTime.toInt()
                onPositionChanged?.invoke(pos)

                // Detect natural completion (currentTime >= duration)
                if (p.duration > 0.0 && p.currentTime >= p.duration - 0.1) {
                    onCompleted?.invoke()
                    stop()
                }
            }
        )
    }

    private fun stopTimer() {
        timer?.invalidate()
        timer = null
    }

    private fun releaseInternal() {
        player?.stop()
        player = null
    }
}