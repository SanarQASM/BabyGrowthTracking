package org.example.project.babygrowthtrackingapplication.platform
// wasmJsMain
import kotlinx.browser.document
import org.w3c.dom.HTMLAudioElement

actual class LullabyPlayer {
    private var audio: HTMLAudioElement? = null

    actual fun play(assetKey: String) {
        if (audio == null) {
            audio = document.createElement("audio") as HTMLAudioElement
            audio?.src = "assets/$assetKey"
        }
        audio?.play()
    }

    actual fun pause() {
        audio?.pause()
    }

    actual fun stop() {
        audio?.pause()
        audio?.currentTime = 0.0
    }

    actual fun seekTo(seconds: Int) {
        audio?.currentTime = seconds.toDouble()
    }

    actual fun setOnPositionChanged(callback: (Int) -> Unit) {
        audio?.ontimeupdate = {
            callback(audio?.currentTime?.toInt() ?: 0)
        }
    }
}