package org.example.project.babygrowthtrackingapplication.notifications

// ─────────────────────────────────────────────────────────────────────────────
// Web (Kotlin/JS + Kotlin/Wasm) actual implementation of FcmTokenService
//
// Uses the Firebase JS SDK loaded via the index.html <script> tags.
// Both jsMain and wasmJsMain can share this file if your build is configured
// with a shared sourceSet (e.g. webMain → jsMain + wasmJsMain).
// If they are separate sourceSets, copy this file to each.
//
// Prerequisites in your index.html / main entry point:
//   <script src="https://www.gstatic.com/firebasejs/10.x.x/firebase-app.js" type="module"></script>
//   <script src="https://www.gstatic.com/firebasejs/10.x.x/firebase-messaging.js" type="module"></script>
//   <script>
//     import { initializeApp } from 'firebase/app';
//     import { getMessaging, getToken } from 'firebase/messaging';
//     const app = initializeApp({ apiKey: "...", ... });
//     window.__fbMessaging = getMessaging(app);
//   </script>
//
// Also register a Service Worker at /firebase-messaging-sw.js:
//   importScripts('https://www.gstatic.com/firebasejs/10.x.x/firebase-app-compat.js');
//   importScripts('https://www.gstatic.com/firebasejs/10.x.x/firebase-messaging-compat.js');
//   firebase.initializeApp({ ... });
//   const messaging = firebase.messaging();
// ─────────────────────────────────────────────────────────────────────────────

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

actual class FcmTokenService actual constructor() {
    actual val platform: String = "web"

    actual suspend fun getToken(): String? = suspendCancellableCoroutine { cont ->
        try {
            // Call into the Firebase JS SDK via JS interop.
            // This assumes window.__fbMessaging was set up in index.html.
            val token = getWebFcmToken()
            cont.resume(token)
        } catch (e: Throwable) {
            // FCM is optional on web — return null instead of crashing
            cont.resume(null)
        }
    }
}

// JS interop helper — calls into the Firebase JS messaging SDK.
// Returns the FCM registration token string or null on failure.
private fun getWebFcmToken(): String? {
    // This is a stub — wire to your actual JS interop bridge.
    // In a real project you would use @JsExport + external fun declarations
    // or kotlinx.browser.window.asDynamic().__fbMessaging to call getToken().
    //
    // Example with Kotlin/JS:
    //   val messaging = kotlinx.browser.window.asDynamic().__fbMessaging
    //   if (messaging == null) return null
    //   // getToken() returns a JS Promise — bridged via await in a suspend fun
    //
    // For now return null so the web build compiles without Firebase JS config.
    return null
}