package org.example.project.babygrowthtrackingapplication.platform
// desktopMain

import kotlinx.coroutines.*
import java.io.BufferedInputStream
import javax.sound.sampled.*

// ═══════════════════════════════════════════════════════════════════════════
// LullabyPlayer.desktop.kt  —  desktopMain  (Compose for Desktop / JVM)
//
// AUDIO FILES
// ───────────
// Place MP3s in:  src/desktopMain/resources/
// (Gradle copies them to the classpath root so they are found at "/<name>")
// File names must match asset_key + ".mp3", e.g.:
//   src/desktopMain/resources/lullaby_lale_lale_kurdan.mp3
//
// MP3 SUPPORT
// ───────────
// javax.sound.sampled supports WAV/AIFF natively.
// For MP3 add to your desktop Gradle module:
//   implementation("com.googlecode.soundlibs:mp3spi:1.9.5.4")
// With mp3spi on the classpath, AudioSystem handles MP3 transparently.
//
// DOWNLOAD
// ────────
// downloadLullaby() copies the classpath resource to ~/Music/BabyGrowth/.
// ═══════════════════════════════════════════════════════════════════════════

actual class LullabyPlayer {

    private var clip             : Clip?            = null
    private var onPositionChanged: ((Int) -> Unit)? = null
    private var onCompleted      : (() -> Unit)?    = null
    private var pollingJob       : Job?             = null
    private var currentAssetKey  : String?          = null
    private val scope            = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // ── play ──────────────────────────────────────────────────────────────
    actual fun play(assetKey: String) {
        when {
            assetKey.isBlank() -> {
                clip?.start(); startPolling()
            }
            assetKey == currentAssetKey && clip != null -> {
                if (clip?.isRunning == false) { clip?.start(); startPolling() }
            }
            else -> {
                releaseInternal()
                currentAssetKey = assetKey
                val fileName = resolveFileName(assetKey)
                val stream   = javaClass.getResourceAsStream("/$fileName") ?: run {
                    System.err.println("[LullabyPlayer/Desktop] Not found: /$fileName"); return
                }
                try {
                    val raw = AudioSystem.getAudioInputStream(BufferedInputStream(stream))
                    // Decode to PCM so the Clip can handle MP3 (requires mp3spi)
                    val fmt = raw.format
                    val pcm = AudioFormat(
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
                                // Natural end (not pause/stop)
                                this@LullabyPlayer.onCompleted?.invoke()
                                this@LullabyPlayer.onPositionChanged?.invoke(0)
                                this@LullabyPlayer.stopPolling()
                            }
                        }
                        start()
                    }
                    startPolling()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    actual fun pause() { clip?.stop(); stopPolling() }   // Clip.stop() = pause

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

    // ── Download helper ───────────────────────────────────────────────────
    fun downloadLullaby(assetKey: String, displayName: String) {
        val fileName = resolveFileName(assetKey)
        val stream   = javaClass.getResourceAsStream("/$fileName") ?: return
        try {
            val dir = java.io.File(System.getProperty("user.home"), "Music/BabyGrowth")
            dir.mkdirs()
            java.io.File(dir, displayName).outputStream().use { stream.copyTo(it) }
            stream.close()
        } catch (e: Exception) { e.printStackTrace() }
    }

    // ── Internal helpers ──────────────────────────────────────────────────
    private fun resolveFileName(key: String) =
        if (key.endsWith(".mp3", ignoreCase = true)) key else "$key.mp3"

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