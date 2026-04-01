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
// Audio backend : android.media.MediaPlayer
// Asset lookup  : Context.assets (files must be in src/androidMain/assets/
//                 or the shared composeResources assets folder bridged via
//                 Gradle).
// Position poll : android.os.Handler @ 500 ms on the main thread.
// Download      : MediaStore (API 29+) or legacy external storage.
//
// IMPORTANT — asset path convention
// ──────────────────────────────────
// The ViewModel passes the raw asset_key from the JSON, e.g.:
//   "lullaby_lale_lale_kurdan"
// The Android implementation appends ".mp3" automatically, so the file
// in assets/ must be named "lullaby_lale_lale_kurdan.mp3".
// ═══════════════════════════════════════════════════════════════════════════

actual class LullabyPlayer(private val context: Context) {

    private var mediaPlayer      : MediaPlayer? = null
    private var onPositionChanged: ((Int) -> Unit)? = null
    private var onCompleted      : (() -> Unit)?    = null
    private val handler          = Handler(Looper.getMainLooper())
    private var currentAssetKey  : String? = null

    // ── Position polling runnable ─────────────────────────────────────────

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
            // Resume after pause (ViewModel passes blank to resume)
            assetKey.isBlank() -> {
                mediaPlayer?.start()
                startPolling()
                return
            }

            // Same track — resume if paused, do nothing if already playing
            assetKey == currentAssetKey && mediaPlayer != null -> {
                if (mediaPlayer?.isPlaying == false) {
                    mediaPlayer?.start()
                    startPolling()
                }
                return
            }

            // New track — release old player first
            else -> {
                releaseInternal()
                currentAssetKey = assetKey
            }
        }

        val fileName = resolveFileName(assetKey)
        try {
            val afd = context.assets.openFd(fileName)
            mediaPlayer = MediaPlayer().apply {
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
                prepare()   // synchronous; use prepareAsync() for large files
                setOnCompletionListener {
                    onCompleted?.invoke()
                    // reset position display
                    onPositionChanged?.invoke(0)
                    this@LullabyPlayer.stopPolling()
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
        mediaPlayer?.pause()
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
        mediaPlayer?.let { mp ->
            val ms = (seconds * 1_000).coerceIn(0, mp.duration)
            mp.seekTo(ms)
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

    actual fun isPlaying(): Boolean = mediaPlayer?.isPlaying ?: false

    actual fun currentPosition(): Int =
        (mediaPlayer?.currentPosition ?: 0) / 1_000

    actual fun duration(): Int =
        (mediaPlayer?.duration ?: 0) / 1_000

    // ── release ───────────────────────────────────────────────────────────

    actual fun release() = stop()

    // ═══════════════════════════════════════════════════════════════════════
    // Download helper
    //
    // Copies the audio asset to the device's Music folder.
    // Call this from the Compose screen when the DownloadEvent is consumed.
    //   e.g. (lullabyPlayer as LullabyPlayer).downloadLullaby(assetKey, fileName)
    // ═══════════════════════════════════════════════════════════════════════

    fun downloadLullaby(assetKey: String, displayName: String) {
        val fileName = resolveFileName(assetKey)
        try {
            val inputStream = context.assets.open(fileName)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // API 29+ — no WRITE_EXTERNAL_STORAGE permission needed
                val values = ContentValues().apply {
                    put(MediaStore.Audio.Media.DISPLAY_NAME, displayName)
                    put(MediaStore.Audio.Media.MIME_TYPE, "audio/mpeg")
                    put(
                        MediaStore.Audio.Media.RELATIVE_PATH,
                        "${Environment.DIRECTORY_MUSIC}${File.separator}BabyGrowth"
                    )
                    put(MediaStore.Audio.Media.IS_PENDING, 1)
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)
                uri?.let {
                    resolver.openOutputStream(it)?.use { out -> inputStream.copyTo(out) }
                    values.clear()
                    values.put(MediaStore.Audio.Media.IS_PENDING, 0)
                    resolver.update(it, values, null, null)
                }
            } else {
                // Legacy — requires WRITE_EXTERNAL_STORAGE in manifest for API < 29
                val dir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
                    "BabyGrowth"
                )
                dir.mkdirs()
                val dest = File(dir, displayName)
                FileOutputStream(dest).use { out -> inputStream.copyTo(out) }
            }
            inputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ── internal helpers ──────────────────────────────────────────────────

    private fun resolveFileName(assetKey: String): String =
        if (assetKey.endsWith(".mp3", ignoreCase = true)) assetKey
        else "$assetKey.mp3"

    private fun startPolling() {
        handler.removeCallbacks(positionRunnable)
        handler.postDelayed(positionRunnable, 500)
    }

    private fun stopPolling() {
        handler.removeCallbacks(positionRunnable)
    }

    private fun releaseInternal() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) { }
        mediaPlayer = null
    }
}