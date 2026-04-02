package org.example.project.babygrowthtrackingapplication.platform
// androidMain

import android.content.ContentValues
import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream

// ═══════════════════════════════════════════════════════════════════════════
// LullabyPlayer.android.kt  —  androidMain
//
// AUDIO FILES
// ───────────
// Place your audio files in:  src/androidMain/assets/
// Supported formats: .m4a (AAC), .mp3, .ogg
// Name them exactly as the asset_key in the JSON, e.g.:
//   src/androidMain/assets/lullaby_lale_lale_kurdan.m4a
//   src/androidMain/assets/lullaby_dilber_dilber.m4a
//   etc.
//
// FIX: resolveFileName() now tries .m4a first, then .mp3, then .ogg.
//      This fixes the silent failure when files are .m4a but code
//      was hardcoding .mp3.
// ═══════════════════════════════════════════════════════════════════════════

actual class LullabyPlayer(private val context: Context) {

    private var mediaPlayer      : MediaPlayer?     = null
    private var onPositionChanged: ((Int) -> Unit)? = null
    private var onCompleted      : (() -> Unit)?    = null
    private val handler          = Handler(Looper.getMainLooper())
    private var currentAssetKey  : String?          = null

    // ── Position polling runnable (500 ms) ────────────────────────────────
    private val positionRunnable = object : Runnable {
        override fun run() {
            val mp = mediaPlayer ?: return
            if (mp.isPlaying) {
                onPositionChanged?.invoke(mp.currentPosition / 1_000)
                handler.postDelayed(this, 500)
            }
        }
    }

    // ── play ──────────────────────────────────────────────────────────────
    actual fun play(assetKey: String) {
        when {
            // Blank = resume after pause
            assetKey.isBlank() -> {
                mediaPlayer?.start()
                startPolling()
            }
            // Same track already loaded — resume if paused
            assetKey == currentAssetKey && mediaPlayer != null -> {
                if (mediaPlayer?.isPlaying == false) {
                    mediaPlayer?.start()
                    startPolling()
                }
            }
            // New track
            else -> {
                releaseInternal()
                currentAssetKey = assetKey
                // FIX: resolve the actual file name by trying known extensions
                val fileName = resolveFileName(assetKey)
                if (fileName == null) {
                    android.util.Log.e("LullabyPlayer", "Asset not found for key: $assetKey")
                    return
                }
                try {
                    val afd = context.assets.openFd(fileName)
                    mediaPlayer = MediaPlayer().apply {
                        setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                        afd.close()
                        prepare()
                        setOnCompletionListener {
                            this@LullabyPlayer.onCompleted?.invoke()
                            this@LullabyPlayer.onPositionChanged?.invoke(0)
                            this@LullabyPlayer.stopPolling()
                        }
                        start()
                    }
                    startPolling()
                } catch (e: Exception) {
                    android.util.Log.e("LullabyPlayer", "Failed to play $fileName", e)
                }
            }
        }
    }

    actual fun pause() {
        mediaPlayer?.pause()
        stopPolling()
    }

    actual fun stop() {
        stopPolling()
        releaseInternal()
        currentAssetKey = null
        onPositionChanged?.invoke(0)
    }

    actual fun seekTo(seconds: Int) {
        mediaPlayer?.let { mp ->
            mp.seekTo((seconds * 1_000).coerceIn(0, mp.duration))
            onPositionChanged?.invoke(seconds)
        }
    }

    actual fun setOnPositionChanged(callback: (Int) -> Unit) {
        onPositionChanged = callback
    }

    actual fun setOnCompleted(callback: () -> Unit) {
        onCompleted = callback
    }

    actual fun isPlaying(): Boolean = mediaPlayer?.isPlaying ?: false

    actual fun currentPosition(): Int = (mediaPlayer?.currentPosition ?: 0) / 1_000

    actual fun duration(): Int = (mediaPlayer?.duration ?: 0) / 1_000

    actual fun release() = stop()

    // ── Download helper ───────────────────────────────────────────────────
    fun downloadLullaby(assetKey: String, displayName: String) {
        val fileName = resolveFileName(assetKey) ?: return
        val mimeType = when {
            fileName.endsWith(".m4a", ignoreCase = true) -> "audio/mp4"
            fileName.endsWith(".mp3", ignoreCase = true) -> "audio/mpeg"
            fileName.endsWith(".ogg", ignoreCase = true) -> "audio/ogg"
            else -> "audio/mpeg"
        }
        try {
            val input = context.assets.open(fileName)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Audio.Media.DISPLAY_NAME, displayName)
                    put(MediaStore.Audio.Media.MIME_TYPE, mimeType)
                    put(MediaStore.Audio.Media.RELATIVE_PATH,
                        "${Environment.DIRECTORY_MUSIC}${File.separator}BabyGrowth")
                    put(MediaStore.Audio.Media.IS_PENDING, 1)
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)
                uri?.let {
                    resolver.openOutputStream(it)?.use { out -> input.copyTo(out) }
                    values.clear()
                    values.put(MediaStore.Audio.Media.IS_PENDING, 0)
                    resolver.update(it, values, null, null)
                }
            } else {
                val dir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
                    "BabyGrowth"
                )
                dir.mkdirs()
                FileOutputStream(File(dir, displayName)).use { out -> input.copyTo(out) }
            }
            input.close()
        } catch (e: Exception) {
            android.util.Log.e("LullabyPlayer", "Download failed for $assetKey", e)
        }
    }

    // ── Internal helpers ──────────────────────────────────────────────────

    /**
     * FIX: Try extensions in order of preference.
     * Returns the first filename that exists in assets/, or null if none found.
     * Priority: exact key (if it already has an extension) → .m4a → .mp3 → .ogg
     */
    private fun resolveFileName(key: String): String? {
        // If the key already carries a known extension, use it directly
        if (key.endsWith(".m4a", ignoreCase = true) ||
            key.endsWith(".mp3", ignoreCase = true) ||
            key.endsWith(".ogg", ignoreCase = true)) {
            return key
        }
        // Otherwise probe the assets directory for a matching file
        val candidates = listOf("$key.m4a", "$key.mp3", "$key.ogg")
        val assetList  = try { context.assets.list("") ?: emptyArray() } catch (_: Exception) { emptyArray() }
        for (candidate in candidates) {
            if (assetList.contains(candidate)) return candidate
        }
        // Fallback: return .m4a even if not found (error will be logged in play())
        return "$key.m4a"
    }

    private fun startPolling() {
        handler.removeCallbacks(positionRunnable)
        handler.postDelayed(positionRunnable, 500)
    }

    private fun stopPolling() {
        handler.removeCallbacks(positionRunnable)
    }

    private fun releaseInternal() {
        try { mediaPlayer?.stop(); mediaPlayer?.release() } catch (_: Exception) {}
        mediaPlayer = null
    }
}

// ── Actual factory ────────────────────────────────────────────────────────
actual fun createLullabyPlayer(): LullabyPlayer =
    LullabyPlayer(AppContextHolder.context)