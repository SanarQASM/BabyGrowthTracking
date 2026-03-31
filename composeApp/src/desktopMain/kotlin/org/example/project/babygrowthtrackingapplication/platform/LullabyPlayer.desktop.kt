package org.example.project.babygrowthtrackingapplication.platform
import kotlinx.coroutines.*
import java.io.BufferedInputStream
import javax.sound.sampled.*

actual class LullabyPlayer {

    private var clip: Clip? = null
    private var callback: ((Int) -> Unit)? = null
    private var job: Job? = null

    actual fun play(assetKey: String) {
        stop()

        val stream = javaClass.getResourceAsStream("/$assetKey")
            ?: error("Audio file not found: $assetKey")

        val audioInput = AudioSystem.getAudioInputStream(BufferedInputStream(stream))
        clip = AudioSystem.getClip().apply {
            open(audioInput)
            start()
        }

        startTracking()
    }

    actual fun pause() {
        clip?.stop()
    }

    actual fun stop() {
        job?.cancel()
        clip?.stop()
        clip?.close()
        clip = null
    }

    actual fun seekTo(seconds: Int) {
        clip?.microsecondPosition = seconds * 1_000_000L
    }

    actual fun setOnPositionChanged(callback: (Int) -> Unit) {
        this.callback = callback
    }

    private fun startTracking() {
        job = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                clip?.let {
                    val seconds = (it.microsecondPosition / 1_000_000L).toInt()
                    callback?.invoke(seconds)
                }
                delay(1000)
            }
        }
    }
}