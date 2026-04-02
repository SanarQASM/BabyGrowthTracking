package org.example.project.babygrowthtrackingapplication.platform
// jsMain

import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.HTMLAudioElement

// ═══════════════════════════════════════════════════════════════════════════
// LullabyPlayer.js.kt  —  jsMain
//
// FIX: resolveUrl() tries .m4a first, then .mp3.
//      Place files in composeResources/files/ — they are served at
//      assets/lullaby_lale_lale_kurdan.m4a etc.
// ═══════════════════════════════════════════════════════════════════════════

actual class LullabyPlayer {

    private var audio            : HTMLAudioElement? = null
    private var onPositionChanged: ((Int) -> Unit)?  = null
    private var onCompleted      : (() -> Unit)?     = null
    private var intervalId       : Int               = -1
    private var currentAssetKey  : String?           = null

    actual fun play(assetKey: String) {
        when {
            assetKey.isBlank() -> { audio?.play(); startPolling() }
            assetKey == currentAssetKey && audio != null -> {
                if (audio?.paused == true) { audio?.play(); startPolling() }
            }
            else -> {
                releaseInternal()
                currentAssetKey = assetKey
                audio = (document.createElement("audio") as HTMLAudioElement).apply {
                    // FIX: try .m4a first via <source> elements so the browser picks
                    // the format it supports natively
                    val srcM4a = resolveUrl(assetKey, "m4a")
                    val srcMp3 = resolveUrl(assetKey, "mp3")
                    innerHTML = """
                        <source src="$srcM4a" type="audio/mp4">
                        <source src="$srcMp3" type="audio/mpeg">
                    """.trimIndent()
                    preload = "auto"
                    onended = {
                        this@LullabyPlayer.onCompleted?.invoke()
                        this@LullabyPlayer.onPositionChanged?.invoke(0)
                        this@LullabyPlayer.stopPolling()
                        Unit
                    }
                    ontimeupdate = {
                        this@LullabyPlayer.onPositionChanged?.invoke(currentTime.toInt())
                        Unit
                    }
                    play()
                }
                startPolling()
            }
        }
    }

    actual fun pause() { audio?.pause(); stopPolling() }

    actual fun stop() {
        stopPolling()
        audio?.pause(); audio?.let { it.currentTime = 0.0 }
        releaseInternal()
        currentAssetKey = null; onPositionChanged?.invoke(0)
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
        audio?.ontimeupdate = { callback(audio?.currentTime?.toInt() ?: 0); Unit }
    }

    actual fun setOnCompleted(callback: () -> Unit) { onCompleted = callback }

    actual fun isPlaying(): Boolean   = audio?.paused == false
    actual fun currentPosition(): Int = audio?.currentTime?.toInt() ?: 0
    actual fun duration(): Int {
        val d = audio?.duration ?: return 0
        return if (d.isNaN() || d.isInfinite()) 0 else d.toInt()
    }
    actual fun release() = stop()

    fun downloadLullaby(assetKey: String, displayName: String) {
        val link = document.createElement("a") as HTMLAnchorElement
        link.href = resolveUrl(assetKey, "m4a")
        link.download = displayName
        link.style.display = "none"
        document.body?.appendChild(link); link.click()
        window.setTimeout({ document.body?.removeChild(link) }, 1_000)
    }

    // FIX: explicit extension parameter
    private fun resolveUrl(key: String, ext: String): String {
        val base = key.removeSuffix(".mp3").removeSuffix(".m4a")
        return "assets/$base.$ext"
    }

    private fun startPolling() {
        stopPolling()
        intervalId = window.setInterval({
            val a = audio ?: return@setInterval
            if (!a.paused) onPositionChanged?.invoke(a.currentTime.toInt())
        }, 500)
    }

    private fun stopPolling() {
        if (intervalId >= 0) { window.clearInterval(intervalId); intervalId = -1 }
    }

    private fun releaseInternal() { audio?.pause(); audio?.src = ""; audio = null }
}

actual fun createLullabyPlayer(): LullabyPlayer = LullabyPlayer()