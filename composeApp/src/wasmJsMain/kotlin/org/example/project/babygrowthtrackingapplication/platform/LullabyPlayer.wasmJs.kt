package org.example.project.babygrowthtrackingapplication.platform
// wasmJsMain

import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.HTMLAudioElement

// ═══════════════════════════════════════════════════════════════════════════
// LullabyPlayer.wasmJs.kt  —  wasmJsMain  (Kotlin/Wasm → WASM-JS)
//
// Identical feature set to jsMain; uses Kotlin/Wasm's DOM bindings.
// Audio files and download work the same way as jsMain.
//
// FIX: In Kotlin/Wasm, event handler lambdas must return JsAny?.
//      We return 'null' to satisfy this requirement.
// ═══════════════════════════════════════════════════════════════════════════

actual class LullabyPlayer {

    private var audio            : HTMLAudioElement? = null
    private var onPositionChanged: ((Int) -> Unit)?  = null
    private var onCompleted      : (() -> Unit)?     = null
    private var intervalId       : Int               = -1
    private var currentAssetKey  : String?           = null

    // ── play ──────────────────────────────────────────────────────────────
    actual fun play(assetKey: String) {
        when {
            assetKey.isBlank() -> {
                audio?.play()
                startPolling()
                return
            }
            assetKey == currentAssetKey && audio != null -> {
                if (audio?.paused == true) {
                    audio?.play()
                    startPolling()
                }
                return
            }
            else -> {
                releaseInternal()
                currentAssetKey = assetKey
                audio = (document.createElement("audio") as HTMLAudioElement).apply {
                    src     = resolveUrl(assetKey)
                    preload = "auto"
                    onended = {
                        this@LullabyPlayer.onCompleted?.invoke()
                        this@LullabyPlayer.onPositionChanged?.invoke(0)
                        this@LullabyPlayer.stopPolling()
                        null
                    }
                    ontimeupdate = {
                        this@LullabyPlayer.onPositionChanged?.invoke(currentTime.toInt())
                        null
                    }
                    play()
                }
                startPolling()
            }
        }
    }

    actual fun pause() {
        audio?.pause()
        stopPolling()
    }

    actual fun stop() {
        stopPolling()
        audio?.pause()
        audio?.let { it.currentTime = 0.0 }
        releaseInternal()
        currentAssetKey = null
        onPositionChanged?.invoke(0)
    }

    actual fun seekTo(seconds: Int) {
        audio?.let { a ->
            val dur = if (a.duration.isNaN()) 0.0 else a.duration
            a.currentTime = seconds.toDouble().coerceIn(0.0, dur)
            onPositionChanged?.invoke(seconds)
        }
    }

    actual fun setOnPositionChanged(callback: (Int) -> Unit) {
        onPositionChanged = callback
        audio?.ontimeupdate = {
            callback(audio?.currentTime?.toInt() ?: 0)
            null
        }
    }

    actual fun setOnCompleted(callback: () -> Unit) {
        onCompleted = callback
    }

    actual fun isPlaying(): Boolean   = audio?.paused == false
    actual fun currentPosition(): Int = audio?.currentTime?.toInt() ?: 0
    actual fun duration(): Int {
        val d = audio?.duration ?: return 0
        return if (d.isNaN() || d.isInfinite()) 0 else d.toInt()
    }
    actual fun release() = stop()

    // ── Download ──────────────────────────────────────────────────────────
    fun downloadLullaby(assetKey: String, displayName: String) {
        val link = document.createElement("a") as HTMLAnchorElement
        link.href = resolveUrl(assetKey)
        link.download = displayName
        link.style.display = "none"
        document.body?.appendChild(link)
        link.click()
        window.setTimeout({ document.body?.removeChild(link) }, 1_000)
    }

    // ── Internal helpers ──────────────────────────────────────────────────
    private fun resolveUrl(key: String) =
        "assets/${if (key.endsWith(".mp3", true)) key else "$key.mp3"}"

    private fun startPolling() {
        stopPolling()
        intervalId = window.setInterval({
            val a = audio ?: return@setInterval null
            if (!a.paused) onPositionChanged?.invoke(a.currentTime.toInt())
            null
        }, 500)
    }

    private fun stopPolling() {
        if (intervalId >= 0) {
            window.clearInterval(intervalId)
            intervalId = -1
        }
    }

    private fun releaseInternal() {
        audio?.pause()
        audio?.src = ""
        audio = null
    }
}

actual fun createLullabyPlayer(): LullabyPlayer = LullabyPlayer()
