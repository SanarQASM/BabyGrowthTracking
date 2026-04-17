package org.example.project.babygrowthtrackingapplication.notifications

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

// ─────────────────────────────────────────────────────────────────────────────
// FcmTokenService.wasmJs.kt — FIXED (Kotlin/Wasm target)
//
// BUG: getWebFcmToken() returned null unconditionally.
//
// FIX: Identical strategy to the JS target — read window.__fcmToken written
//      by the Firebase JS initializer in index.html (see FcmTokenService.js.kt
//      for the full index.html snippet — it is the same for both targets).
//
// NOTE: Kotlin/Wasm uses a different interop mechanism than Kotlin/JS.
//       JS interop in Wasm goes through @JsExport + external fun declarations.
//       We declare getWebFcmTokenWasm() as an external JS function and provide
//       the JS implementation inline via @JsFun.
// ─────────────────────────────────────────────────────────────────────────────

actual class FcmTokenService actual constructor() {
    actual val platform: String = "web"

    actual suspend fun getToken(): String? = suspendCancellableCoroutine { cont ->
        val token = try {
            getWebFcmTokenWasm()
        } catch (e: Throwable) {
            null
        }
        cont.resume(token)
    }
}

// Kotlin/Wasm external JS function — reads window.__fcmToken
// @JsFun inlines the JS body. The null check converts JS undefined/null to Kotlin null.
@JsFun("() => { const t = window.__fcmToken; return (t && t.length > 0) ? t : null; }")
private external fun getWebFcmTokenWasm(): String?