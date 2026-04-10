package org.example.project.babygrowthtrackingapplication.platform

import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.refTo
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.*

// ─────────────────────────────────────────────────────────────────────────────
// iOS actual implementation of MemoryLocalStorage
// Stores images in the app's Application Support directory under
// "memory_images/" — excluded from iCloud backup by default.
// ─────────────────────────────────────────────────────────────────────────────

actual class MemoryLocalStorage actual constructor() {

    @OptIn(ExperimentalForeignApi::class)
    private fun getDir(): String {
        val paths = NSSearchPathForDirectoriesInDomains(
            NSApplicationSupportDirectory,
            NSUserDomainMask,
            true
        )
        val base = paths.first() as String
        val dir  = "$base/memory_images"
        val fileManager = NSFileManager.defaultManager
        if (!fileManager.fileExistsAtPath(dir)) {
            fileManager.createDirectoryAtPath(
                path          = dir,
                withIntermediateDirectories = true,
                attributes    = null,
                error         = null
            )
        }
        return dir
    }

    private fun keyToPath(key: String): String {
        val safeName = key.replace("/", "_").replace("\\", "_")
        return "${getDir()}/$safeName.img"
    }

    actual suspend fun saveImage(key: String, bytes: ByteArray): Boolean =
        withContext(Dispatchers.Default) {
            try {
                val path = keyToPath(key)
                val data = bytes.toNSData()
                data.writeToFile(path, atomically = true)
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }

    actual suspend fun loadImage(key: String): ByteArray? =
        withContext(Dispatchers.Default) {
            try {
                val path = keyToPath(key)
                if (!NSFileManager.defaultManager.fileExistsAtPath(path)) return@withContext null
                NSData.dataWithContentsOfFile(path)?.toByteArray()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun deleteImage(key: String): Boolean =
        withContext(Dispatchers.Default) {
            try {
                NSFileManager.defaultManager.removeItemAtPath(keyToPath(key), error = null)
            } catch (e: Exception) { false }
        }

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun deleteImagesByPrefix(prefix: String): Boolean =
        withContext(Dispatchers.Default) {
            try {
                val safePrefix = prefix.replace("/", "_").replace("\\", "_")
                val dir = getDir()
                val fileManager = NSFileManager.defaultManager
                val contents = fileManager.contentsOfDirectoryAtPath(dir, error = null)
                    ?.filterIsInstance<String>() ?: emptyList()
                contents
                    .filter { it.startsWith(safePrefix) }
                    .forEach { fileManager.removeItemAtPath("$dir/$it", error = null) }
                true
            } catch (e: Exception) {
                false
            }
        }

    actual suspend fun imageExists(key: String): Boolean =
        withContext(Dispatchers.Default) {
            try {
                NSFileManager.defaultManager.fileExistsAtPath(keyToPath(key))
            } catch (e: Exception) { false }
        }

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun listKeys(prefix: String): List<String> =
        withContext(Dispatchers.Default) {
            try {
                val safePrefix = prefix.replace("/", "_").replace("\\", "_")
                val dir = getDir()
                val contents = NSFileManager.defaultManager
                    .contentsOfDirectoryAtPath(dir, error = null)
                    ?.filterIsInstance<String>() ?: emptyList()
                contents
                    .filter { it.startsWith(safePrefix) }
                    .map { it.removeSuffix(".img") }
            } catch (e: Exception) {
                emptyList()
            }
        }
}

// ─────────────────────────────────────────────────────────────────────────────
// Kotlin ↔ NSData conversion helpers
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalForeignApi::class)
private fun ByteArray.toNSData(): NSData =
    NSData.create(bytes = this.refTo(0) as COpaquePointer?, length = this.size.toULong())

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val bytes = ByteArray(this.length.toInt())
    bytes.usePinned { pinned ->
        platform.posix.memcpy(pinned.addressOf(0), this.bytes, this.length)
    }
    return bytes
}