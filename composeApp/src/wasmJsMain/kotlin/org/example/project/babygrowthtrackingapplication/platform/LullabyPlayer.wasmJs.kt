package org.example.project.babygrowthtrackingapplication.platform
// wasmJsMain  (Kotlin/Wasm → WASM-JS target)

import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.HTMLAudioElement

// ═══════════════════════════════════════════════════════════════════════════
// LullabyPlayer.wasmJs.kt  —  wasmJsMain
//
// Identical feature set to the jsMain implementation; Kotlin/Wasm accesses
// the same Web APIs through its own DOM bindings (org.w3c.dom.*).
//
// Audio backend : HTMLAudioElement
// Asset lookup  : "assets/<assetKey>.mp3"
// Position poll : window.setInterval @ 500 ms
// Download      : Hidden <a download> click trick
//
// NOTE: Kotlin/Wasm's DOM bindings are slightly different from Kotlin/JS in
// that some event handler lambdas have different types.  The implementation
// below uses the patterns that compile correctly under Kotlin/Wasm 2.x.
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
            // Resume
            assetKey.isBlank() -> {
                audio?.play()
                startPolling()
                return
            }

            // Same track already loaded — resume if paused
            assetKey == currentAssetKey && audio != null -> {
                if (audio?.paused == true) {
                    audio?.play()
                    startPolling()
                }
                return
            }

            // New track — tear down current one first
            else -> {
                releaseInternal()
                currentAssetKey = assetKey
            }
        }

        val src = resolveUrl(assetKey)
        audio   = (document.createElement("audio") as HTMLAudioElement).apply {
            this.src     = src
            this.preload = "auto"

            // Natural end-of-track callback
            this.onended = {
                this@LullabyPlayer.onCompleted?.invoke()
                this@LullabyPlayer.onPositionChanged?.invoke(0)
                this@LullabyPlayer.stopPolling()
                null
            }

            this.onerror = { _, _, _, _, _ ->
                println("[LullabyPlayer/wasm] Error loading: $src")
                null
            }

            // Wire position via native timeupdate (fires more often than polling)
            this.ontimeupdate = {
                this@LullabyPlayer.onPositionChanged?.invoke(
                    this.currentTime.toInt()
                )
                null
            }

            play()
        }
        startPolling()
    }

    // ── pause ─────────────────────────────────────────────────────────────

    actual fun pause() {
        audio?.pause()
        stopPolling()
    }

    // ── stop ──────────────────────────────────────────────────────────────

    actual fun stop() {
        stopPolling()
        audio?.pause()
        audio?.let { it.currentTime = 0.0 }
        releaseInternal()
        currentAssetKey = null
        onPositionChanged?.invoke(0)
    }

    // ── seekTo ────────────────────────────────────────────────────────────

    actual fun seekTo(seconds: Int) {
        audio?.let { a ->
            val dur     = if (a.duration.isNaN()) 0.0 else a.duration
            val clamped = seconds.toDouble().coerceIn(0.0, dur)
            a.currentTime = clamped
            onPositionChanged?.invoke(seconds)
        }
    }

    // ── callbacks ─────────────────────────────────────────────────────────

    actual fun setOnPositionChanged(callback: (Int) -> Unit) {
        onPositionChanged = callback
        // Re-wire the native event on the already-created element (if any)
        audio?.ontimeupdate = {
            callback(audio?.currentTime?.toInt() ?: 0)
            null
        }
    }

    actual fun setOnCompleted(callback: () -> Unit) {
        onCompleted = callback
    }

    // ── state queries ─────────────────────────────────────────────────────

    actual fun isPlaying(): Boolean = audio?.paused == false

    actual fun currentPosition(): Int = audio?.currentTime?.toInt() ?: 0

    actual fun duration(): Int {
        val d = audio?.duration ?: return 0
        return if (d.isNaN() || d.isInfinite()) 0 else d.toInt()
    }

    // ── release ───────────────────────────────────────────────────────────

    actual fun release() = stop()

    // ═══════════════════════════════════════════════════════════════════════
    // Download helper  (Wasm version)
    //
    // Same hidden-anchor trick as jsMain.  In Wasm, we cannot directly use
    // URL.createObjectURL on a Blob without additional JS interop; the
    // simplest cross-origin-safe approach is to point the anchor at the
    // asset URL with the download attribute.
    // ═══════════════════════════════════════════════════════════════════════

    fun downloadLullaby(assetKey: String, displayName: String) {
        val src  = resolveUrl(assetKey)
        val link = document.createElement("a") as HTMLAnchorElement
        link.href          = src
        link.download      = displayName
        link.style.display = "none"
        document.body?.appendChild(link)
        link.click()
        window.setTimeout({ document.body?.removeChild(link) }, 1_000)
    }

    // ── internal helpers ──────────────────────────────────────────────────

    private fun resolveUrl(assetKey: String): String {
        val fileName = if (assetKey.endsWith(".mp3", ignoreCase = true)) assetKey
        else "$assetKey.mp3"
        return "assets/$fileName"
    }

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
        audio?.src = ""   // release the media resource
        audio = null
    }
}
