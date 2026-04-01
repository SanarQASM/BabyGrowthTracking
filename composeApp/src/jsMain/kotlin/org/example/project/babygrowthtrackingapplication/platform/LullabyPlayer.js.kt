package org.example.project.babygrowthtrackingapplication.platform
// jsMain  (Kotlin/JS — targets web browsers via Kotlin/JS)

import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.HTMLAudioElement
import org.w3c.dom.url.URL

// ═══════════════════════════════════════════════════════════════════════════
// LullabyPlayer.js.kt  —  jsMain  (Kotlin/JS)
//
// Audio backend : HTMLAudioElement  (Web Audio API)
// Asset lookup  : URL path "assets/<assetKey>.mp3"
//                 Files must be placed in composeResources/files/ and
//                 copied to the web distribution's assets/ folder.
// Position poll : window.setInterval at 500 ms.
// Download      : Creates a hidden <a download> element and clicks it.
//
// IMPORTANT — CORS / same-origin
// ────────────────────────────────
// Audio files served from the same origin work without CORS headers.
// If assets are on a CDN, the server must emit Access-Control-Allow-Origin.
// ═══════════════════════════════════════════════════════════════════════════

actual class LullabyPlayer {

    private var audio            : HTMLAudioElement? = null
    private var onPositionChanged: ((Int) -> Unit)? = null
    private var onCompleted      : (() -> Unit)?    = null
    private var intervalId       : Int              = -1
    private var currentAssetKey  : String?          = null

    // ── play ──────────────────────────────────────────────────────────────

    actual fun play(assetKey: String) {
        when {
            // Resume
            assetKey.isBlank() -> {
                audio?.play()
                startPolling()
                return
            }

            // Same track — resume if paused
            assetKey == currentAssetKey && audio != null -> {
                if (audio?.paused == true) {
                    audio?.play()
                    startPolling()
                }
                return
            }

            // New track
            else -> {
                releaseInternal()
                currentAssetKey = assetKey
            }
        }

        val src = resolveUrl(assetKey)
        audio   = (document.createElement("audio") as HTMLAudioElement).apply {
            this.src       = src
            this.preload   = "auto"

            // Fired when the track ends naturally
            this.onended = {
                this@LullabyPlayer.onCompleted?.invoke()
                this@LullabyPlayer.onPositionChanged?.invoke(0)
                this@LullabyPlayer.stopPolling()
                Unit
            }

            this.onerror = { _, _, _, _, _ ->
                console.error("[LullabyPlayer] Error loading: $src")
                Unit
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
            val clamped = seconds.toDouble()
                .coerceIn(0.0, if (a.duration.isNaN()) 0.0 else a.duration)
            a.currentTime = clamped
            onPositionChanged?.invoke(seconds)
        }
    }

    // ── callbacks ─────────────────────────────────────────────────────────

    actual fun setOnPositionChanged(callback: (Int) -> Unit) {
        onPositionChanged = callback
        // Also wire the native timeupdate event for sub-500 ms resolution
        audio?.ontimeupdate = {
            callback(audio?.currentTime?.toInt() ?: 0)
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
    // Download helper
    //
    // Triggers a browser "Save As" dialog by creating a temporary <a> with
    // the download attribute and clicking it programmatically.
    // ═══════════════════════════════════════════════════════════════════════

    fun downloadLullaby(assetKey: String, displayName: String) {
        val src  = resolveUrl(assetKey)
        val link = document.createElement("a") as HTMLAnchorElement
        link.href     = src
        link.download = displayName
        link.style.display = "none"
        document.body?.appendChild(link)
        link.click()
        // Clean up after a short delay so the click is processed first
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
            val a = audio ?: return@setInterval
            if (!a.paused) {
                onPositionChanged?.invoke(a.currentTime.toInt())
            }
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
        audio?.src = ""   // detach the media resource
        audio = null
    }
}