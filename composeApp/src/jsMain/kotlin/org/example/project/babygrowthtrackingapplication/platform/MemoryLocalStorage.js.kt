package org.example.project.babygrowthtrackingapplication.platform

import kotlinx.browser.localStorage
import kotlinx.coroutines.await
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

// ─────────────────────────────────────────────────────────────────────────────
// Web JS actual implementation of MemoryLocalStorage
//
// Uses the browser's localStorage with Base64-encoded image bytes.
// Note: localStorage has a ~5–10 MB quota. For production consider
// switching to IndexedDB via a JS interop library (e.g. js("...")).
//
// Keys are prefixed with "bgt_img_" to avoid collisions.
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalEncodingApi::class)
actual class MemoryLocalStorage actual constructor() {

    private fun storageKey(key: String) = "bgt_img_$key"

    actual suspend fun saveImage(key: String, bytes: ByteArray): Boolean {
        return try {
            val encoded = Base64.encode(bytes)
            localStorage.setItem(storageKey(key), encoded)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    actual suspend fun loadImage(key: String): ByteArray? {
        return try {
            val encoded = localStorage.getItem(storageKey(key)) ?: return null
            Base64.decode(encoded)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    actual suspend fun deleteImage(key: String): Boolean {
        return try {
            localStorage.removeItem(storageKey(key))
            true
        } catch (e: Exception) {
            false
        }
    }

    actual suspend fun deleteImagesByPrefix(prefix: String): Boolean {
        return try {
            val storagePrefix = storageKey(prefix)
            val keysToDelete  = mutableListOf<String>()
            for (i in 0 until localStorage.length) {
                val k = localStorage.key(i) ?: continue
                if (k.startsWith(storagePrefix)) keysToDelete.add(k)
            }
            keysToDelete.forEach { localStorage.removeItem(it) }
            true
        } catch (e: Exception) {
            false
        }
    }

    actual suspend fun imageExists(key: String): Boolean {
        return try {
            localStorage.getItem(storageKey(key)) != null
        } catch (e: Exception) {
            false
        }
    }

    actual suspend fun listKeys(prefix: String): List<String> {
        return try {
            val storagePrefix = storageKey(prefix)
            val result        = mutableListOf<String>()
            for (i in 0 until localStorage.length) {
                val k = localStorage.key(i) ?: continue
                if (k.startsWith(storagePrefix)) {
                    // Strip the "bgt_img_" prefix before returning
                    result.add(k.removePrefix("bgt_img_"))
                }
            }
            result
        } catch (e: Exception) {
            emptyList()
        }
    }
}