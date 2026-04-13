package org.example.project.babygrowthtrackingapplication.notifications

// ─────────────────────────────────────────────────────────────────────────────
// Kotlin/JS (jsMain) actual implementation of FcmTokenService
// See wasmJsMain version for full setup instructions.
// ─────────────────────────────────────────────────────────────────────────────

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

actual class FcmTokenService actual constructor() {
    actual val platform: String = "web"

    actual suspend fun getToken(): String? = suspendCancellableCoroutine { cont ->
        try {
            cont.resume(getWebFcmToken())
        } catch (e: Throwable) {
            cont.resume(null)
        }
    }
}

private fun getWebFcmToken(): String? {
    // Wire to Firebase JS SDK via kotlinx.browser.window.asDynamic().__fbMessaging
    // See wasmJsMain counterpart for full instructions.
    return null
}