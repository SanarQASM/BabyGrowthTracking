package org.example.project.babygrowthtrackingapplication.platform

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

// ─────────────────────────────────────────────────────────────────────────────
// Web Wasm JS actual implementation of MemoryLocalStorage
//
// Wasm JS does not yet have direct kotlinx.browser bindings, so we use
// @JsName / external declarations to call localStorage directly via JS interop.
//
// Storage strategy: Base64-encoded bytes stored as strings in localStorage
// (same approach as JS target for consistent cross-tab data sharing).
// Prefix: "bgt_img_" for namespace isolation.
//
// Note: localStorage quota is ~5–10 MB. For large image collections consider
// IndexedDB via a JS interop wrapper.
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalEncodingApi::class)
actual class MemoryLocalStorage actual constructor() {

    private fun storageKey(key: String) = "bgt_img_$key"

    actual suspend fun saveImage(key: String, bytes: ByteArray): Boolean {
        return try {
            val encoded = Base64.encode(bytes)
            jsLocalStorageSet(storageKey(key), encoded)
            true
        } catch (e: Exception) {
            false
        }
    }

    actual suspend fun loadImage(key: String): ByteArray? {
        return try {
            val encoded = jsLocalStorageGet(storageKey(key)) ?: return null
            Base64.decode(encoded)
        } catch (e: Exception) {
            null
        }
    }

    actual suspend fun deleteImage(key: String): Boolean {
        return try {
            jsLocalStorageRemove(storageKey(key))
            true
        } catch (e: Exception) {
            false
        }
    }

    actual suspend fun deleteImagesByPrefix(prefix: String): Boolean {
        return try {
            val storagePrefix = storageKey(prefix)
            val allKeys       = jsLocalStorageAllKeys()
            allKeys
                .filter { it.startsWith(storagePrefix) }
                .forEach { jsLocalStorageRemove(it) }
            true
        } catch (e: Exception) {
            false
        }
    }

    actual suspend fun imageExists(key: String): Boolean {
        return try {
            jsLocalStorageGet(storageKey(key)) != null
        } catch (e: Exception) {
            false
        }
    }

    actual suspend fun listKeys(prefix: String): List<String> {
        return try {
            val storagePrefix = storageKey(prefix)
            jsLocalStorageAllKeys()
                .filter  { it.startsWith(storagePrefix) }
                .map     { it.removePrefix("bgt_img_") }
        } catch (e: Exception) {
            emptyList()
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// JS interop helpers — call browser localStorage from Wasm
// ─────────────────────────────────────────────────────────────────────────────

@JsFun("(key, value) => localStorage.setItem(key, value)")
private external fun jsLocalStorageSet(key: String, value: String)

@JsFun("(key) => localStorage.getItem(key)")
private external fun jsLocalStorageGet(key: String): String?

@JsFun("(key) => localStorage.removeItem(key)")
private external fun jsLocalStorageRemove(key: String)

@JsFun("""() => {
    const keys = [];
    for (let i = 0; i < localStorage.length; i++) {
        keys.push(localStorage.key(i));
    }
    return keys;
}""")
private external fun jsLocalStorageAllKeys(): Array<String>