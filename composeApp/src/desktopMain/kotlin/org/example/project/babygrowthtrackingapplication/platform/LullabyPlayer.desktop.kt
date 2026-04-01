package org.example.project.babygrowthtrackingapplication.platform
// desktopMain

import kotlinx.coroutines.*
import java.io.BufferedInputStream
import javax.sound.sampled.*

// ═══════════════════════════════════════════════════════════════════════════
// LullabyPlayer.desktop.kt  —  desktopMain  (JVM / Compose Desktop)
//
// Audio backend : javax.sound.sampled.Clip (built-in JVM; supports WAV,
//                 AIFF, AU out of the box).
//                 For MP3 support add the mp3spi library to your Gradle:
//                   implementation("com.googlecode.soundlibs:mp3spi:1.9.5.4")
//                 With mp3spi on the classpath AudioSystem.getClip() will
//                 accept MP3 streams transparently.
//
// Asset lookup  : Classpath resource "/<assetKey>.mp3"
//                 Place audio files in src/desktopMain/resources/ (or the
//                 shared composeResources that Gradle copies there).
//
// Position poll : Coroutine on Dispatchers.Default every 500 ms.
// Download      : Copies the classpath resource to user's home music dir.
//
// IMPORTANT — asset path convention
// ──────────────────────────────────
// assetKey e.g. "lullaby_lale_lale_kurdan" → classpath resource
// "/lullaby_lale_lale_kurdan.mp3"
// ═══════════════════════════════════════════════════════════════════════════

actual class LullabyPlayer {

    private var clip             : Clip?            = null
    private var onPositionChanged: ((Int) -> Unit)? = null
    private var onCompleted      : (() -> Unit)?    = null
    private var pollingJob       : Job?             = null
    private var currentAssetKey  : String?          = null

    // Coroutine scope tied to the lifetime of this player
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // ── play ──────────────────────────────────────────────────────────────

    actual fun play(assetKey: String) {
        when {
            // Resume after pause
            assetKey.isBlank() -> {
                clip?.start()
                startPolling()
                return
            }

            // Same track — resume if stopped/paused
            assetKey == currentAssetKey && clip != null -> {
                if (clip?.isRunning == false) {
                    clip?.start()
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

        val fileName = resolveFileName(assetKey)
        val stream   = javaClass.getResourceAsStream("/$fileName")
        if (stream == null) {
            System.err.println("[LullabyPlayer] Resource not found: /$fileName")
            return
        }

        try {
            val audioInput = AudioSystem.getAudioInputStream(BufferedInputStream(stream))
            // Some formats (e.g. MP3) need conversion to a PCM format the Clip understands
            val baseFormat   = audioInput.format
            val decodedFormat = AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                baseFormat.sampleRate,
                16,
                baseFormat.channels,
                baseFormat.channels * 2,
                baseFormat.sampleRate,
                false
            )
            val decodedStream = AudioSystem.getAudioInputStream(decodedFormat, audioInput)

            clip = AudioSystem.getClip().apply {
                open(decodedStream)
                addLineListener { event ->
                    if (event.type == LineEvent.Type.STOP && microsecondPosition >= microsecondLength - 10_000) {
                        // Natural completion (not a pause/stop call)
                        onCompleted?.invoke()
                        onPositionChanged?.invoke(0)
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

    // ── pause ─────────────────────────────────────────────────────────────

    actual fun pause() {
        clip?.stop()   // Clip.stop() = pause (position is preserved)
        stopPolling()
    }

    // ── stop ──────────────────────────────────────────────────────────────

    actual fun stop() {
        stopPolling()
        releaseInternal()
        currentAssetKey = null
        onPositionChanged?.invoke(0)
    }

    // ── seekTo ────────────────────────────────────────────────────────────

    actual fun seekTo(seconds: Int) {
        clip?.let { c ->
            val micros = (seconds * 1_000_000L).coerceIn(0L, c.microsecondLength)
            c.microsecondPosition = micros
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

    actual fun isPlaying(): Boolean = clip?.isRunning ?: false

    actual fun currentPosition(): Int =
        ((clip?.microsecondPosition ?: 0L) / 1_000_000L).toInt()

    actual fun duration(): Int =
        ((clip?.microsecondLength ?: 0L) / 1_000_000L).toInt()

    // ── release ───────────────────────────────────────────────────────────

    actual fun release() {
        stop()
        scope.cancel()
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Download helper
    //
    // Copies the classpath resource to ~/Music/BabyGrowth/<displayName>.
    // Call from the Compose screen when DownloadEvent is consumed.
    // ═══════════════════════════════════════════════════════════════════════

    fun downloadLullaby(assetKey: String, displayName: String) {
        val fileName = resolveFileName(assetKey)
        val stream   = javaClass.getResourceAsStream("/$fileName") ?: run {
            System.err.println("[LullabyPlayer] Cannot download: /$fileName not found")
            return
        }
        try {
            val musicDir = java.io.File(
                System.getProperty("user.home"),
                "Music${java.io.File.separator}BabyGrowth"
            )
            musicDir.mkdirs()
            val dest = java.io.File(musicDir, displayName)
            dest.outputStream().use { out -> stream.copyTo(out) }
            stream.close()
            println("[LullabyPlayer] Downloaded to: ${dest.absolutePath}")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ── internal helpers ──────────────────────────────────────────────────

    private fun resolveFileName(assetKey: String): String =
        if (assetKey.endsWith(".mp3", ignoreCase = true)) assetKey else "$assetKey.mp3"

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

    private fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    private fun releaseInternal() {
        try {
            clip?.stop()
            clip?.close()
        } catch (_: Exception) { }
        clip = null
    }
}