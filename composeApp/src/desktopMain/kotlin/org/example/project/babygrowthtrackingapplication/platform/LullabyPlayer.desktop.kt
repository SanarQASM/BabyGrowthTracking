package org.example.project.babygrowthtrackingapplication.platform
// desktopMain

import kotlinx.coroutines.*
import java.io.BufferedInputStream
import javax.sound.sampled.*

// ═══════════════════════════════════════════════════════════════════════════
// LullabyPlayer.desktop.kt  —  desktopMain
//
// FIX: resolveFileName() tries .m4a → .mp3.
//
// M4A/AAC support on desktop requires an SPI provider (JavaFX Media or
// mp3spi does NOT handle AAC). If your desktop target needs M4A, the
// simplest approach is to also keep an .mp3 copy in resources and rely
// on the fallback, OR use JavaFX MediaPlayer instead of javax.sound.
//
// MP3 support: add to desktop Gradle:
//   implementation("com.googlecode.soundlibs:mp3spi:1.9.5.4")
// ═══════════════════════════════════════════════════════════════════════════

actual class LullabyPlayer {

    private var clip             : Clip?            = null
    private var onPositionChanged: ((Int) -> Unit)? = null
    private var onCompleted      : (() -> Unit)?    = null
    private var pollingJob       : Job?             = null
    private var currentAssetKey  : String?          = null
    private val scope            = CoroutineScope(Dispatchers.Default + SupervisorJob())

    actual fun play(assetKey: String) {
        when {
            assetKey.isBlank() -> { clip?.start(); startPolling() }
            assetKey == currentAssetKey && clip != null -> {
                if (clip?.isRunning == false) { clip?.start(); startPolling() }
            }
            else -> {
                releaseInternal()
                currentAssetKey = assetKey
                // FIX: probe resources for .m4a or .mp3
                val (stream, fileName) = resolveStream(assetKey) ?: run {
                    System.err.println("[LullabyPlayer/Desktop] Not found: $assetKey (.m4a / .mp3)"); return
                }
                try {
                    val raw     = AudioSystem.getAudioInputStream(BufferedInputStream(stream))
                    val fmt     = raw.format
                    val pcm     = AudioFormat(
                        AudioFormat.Encoding.PCM_SIGNED,
                        fmt.sampleRate, 16, fmt.channels,
                        fmt.channels * 2, fmt.sampleRate, false
                    )
                    val decoded = AudioSystem.getAudioInputStream(pcm, raw)
                    clip = AudioSystem.getClip().apply {
                        open(decoded)
                        addLineListener { event ->
                            if (event.type == LineEvent.Type.STOP &&
                                microsecondPosition >= microsecondLength - 50_000) {
                                this@LullabyPlayer.onCompleted?.invoke()
                                this@LullabyPlayer.onPositionChanged?.invoke(0)
                                this@LullabyPlayer.stopPolling()
                            }
                        }
                        start()
                    }
                    startPolling()
                } catch (e: Exception) {
                    System.err.println("[LullabyPlayer/Desktop] Playback error for $fileName: ${e.message}")
                }
            }
        }
    }

    actual fun pause() { clip?.stop(); stopPolling() }

    actual fun stop() {
        stopPolling(); releaseInternal()
        currentAssetKey = null; onPositionChanged?.invoke(0)
    }

    actual fun seekTo(seconds: Int) {
        clip?.let { c ->
            c.microsecondPosition = (seconds * 1_000_000L).coerceIn(0L, c.microsecondLength)
            onPositionChanged?.invoke(seconds)
        }
    }

    actual fun setOnPositionChanged(callback: (Int) -> Unit) { onPositionChanged = callback }
    actual fun setOnCompleted(callback: () -> Unit)          { onCompleted = callback }

    actual fun isPlaying(): Boolean   = clip?.isRunning ?: false
    actual fun currentPosition(): Int = ((clip?.microsecondPosition ?: 0L) / 1_000_000L).toInt()
    actual fun duration(): Int        = ((clip?.microsecondLength  ?: 0L) / 1_000_000L).toInt()

    actual fun release() { stop(); scope.cancel() }

    fun downloadLullaby(assetKey: String, displayName: String) {
        val (stream, _) = resolveStream(assetKey) ?: return
        try {
            val dir = java.io.File(System.getProperty("user.home"), "Music/BabyGrowth")
            dir.mkdirs()
            java.io.File(dir, displayName).outputStream().use { stream.copyTo(it) }
            stream.close()
        } catch (e: Exception) { e.printStackTrace() }
    }

    // FIX: probe classpath for .m4a first, then .mp3
    private fun resolveStream(key: String): Pair<java.io.InputStream, String>? {
        val base = key.removeSuffix(".mp3").removeSuffix(".m4a")
        for (ext in listOf("m4a", "mp3")) {
            val name   = "$base.$ext"
            val stream = javaClass.getResourceAsStream("/$name")
            if (stream != null) return stream to name
        }
        return null
    }

    private fun startPolling() {
        pollingJob?.cancel()
        pollingJob = scope.launch {
            while (isActive) {
                delay(500)
                val c = clip ?: break
                if (c.isRunning) {
                    val pos = (c.microsecondPosition / 1_000_000L).toInt()
                    withContext(Dispatchers.Main) { onPositionChanged?.invoke(pos) }
                }
            }
        }
    }

    private fun stopPolling()  { pollingJob?.cancel(); pollingJob = null }

    private fun releaseInternal() {
        try { clip?.stop(); clip?.close() } catch (_: Exception) {}
        clip = null
    }
}

actual fun createLullabyPlayer(): LullabyPlayer = LullabyPlayer()