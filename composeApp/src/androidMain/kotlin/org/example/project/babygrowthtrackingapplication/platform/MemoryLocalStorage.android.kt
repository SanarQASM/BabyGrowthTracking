// composeApp/src/androidMain/.../platform/MemoryLocalStorage.android.kt
package org.example.project.babygrowthtrackingapplication.platform

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

private var appContext: Context? = null

fun initMemoryLocalStorage(context: Context) {
    appContext = context.applicationContext
}

actual class MemoryLocalStorage actual constructor() {

    private fun getDir(): File? {
        val ctx = appContext ?: return null
        val dir = File(ctx.filesDir, "memory_images")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    private fun keyToFile(key: String): File? {
        val dir = getDir() ?: return null
        val safeName = key.replace("/", "_").replace("\\", "_")
        return File(dir, "$safeName.img")
    }

    actual suspend fun saveImage(key: String, bytes: ByteArray): Boolean =
        withContext(Dispatchers.IO) {
            try {
                keyToFile(key)?.writeBytes(bytes) != null
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }

    actual suspend fun loadImage(key: String): ByteArray? =
        withContext(Dispatchers.IO) {
            try {
                val file = keyToFile(key) ?: return@withContext null
                if (file.exists()) file.readBytes() else null
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

    actual suspend fun deleteImage(key: String): Boolean =
        withContext(Dispatchers.IO) {
            try { keyToFile(key)?.delete() ?: false } catch (e: Exception) { false }
        }

    actual suspend fun deleteImagesByPrefix(prefix: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val safePrefix = prefix.replace("/", "_").replace("\\", "_")
                getDir()?.listFiles()
                    ?.filter { it.name.startsWith(safePrefix) }
                    ?.forEach { it.delete() }
                true
            } catch (e: Exception) {
                false
            }
        }

    actual suspend fun imageExists(key: String): Boolean =
        withContext(Dispatchers.IO) {
            try { keyToFile(key)?.exists() ?: false } catch (e: Exception) { false }
        }

    actual suspend fun listKeys(prefix: String): List<String> =
        withContext(Dispatchers.IO) {
            try {
                val safePrefix = prefix.replace("/", "_").replace("\\", "_")
                getDir()?.listFiles()
                    ?.filter { it.name.startsWith(safePrefix) }
                    ?.map { it.name.removeSuffix(".img") }
                    ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }
}