package org.example.project.babygrowthtrackingapplication.notifications

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

// ─────────────────────────────────────────────────────────────────────────────
// FcmTokenService.js.kt — FIXED (Kotlin/JS target)
//
// BUG: getWebFcmToken() returned null unconditionally. Web users never received
//      push notifications because no FCM token was ever registered.
//
// FIX:
//  We read window.__fcmToken which is written by the Firebase JS SDK initializer
//  in index.html (see the required index.html snippet below). This is the
//  standard way to bridge JS-side async Firebase calls into synchronous Kotlin.
//
//  The flow:
//    1. index.html loads Firebase JS SDK and calls getToken(messaging, {vapidKey})
//    2. The resolved token is stored as window.__fcmToken (string)
//    3. Kotlin reads window.__fcmToken here
//
// REQUIRED index.html changes — add before </body>:
//
//  <script type="module">
//    import { initializeApp } from 'https://www.gstatic.com/firebasejs/10.12.0/firebase-app.js';
//    import { getMessaging, getToken, onMessage } from
//        'https://www.gstatic.com/firebasejs/10.12.0/firebase-messaging.js';
//
//    const firebaseConfig = {
//      apiKey: "YOUR_API_KEY",
//      authDomain: "YOUR_AUTH_DOMAIN",
//      projectId: "YOUR_PROJECT_ID",
//      storageBucket: "YOUR_STORAGE_BUCKET",
//      messagingSenderId: "YOUR_SENDER_ID",
//      appId: "YOUR_APP_ID"
//    };
//
//    const app = initializeApp(firebaseConfig);
//    const messaging = getMessaging(app);
//    window.__fbMessaging = messaging;
//
//    // Request notification permission and get token
//    Notification.requestPermission().then(permission => {
//      if (permission === 'granted') {
//        getToken(messaging, { vapidKey: 'YOUR_VAPID_KEY' }).then(token => {
//          window.__fcmToken = token;
//          console.log('FCM token ready:', token.substring(0, 20) + '...');
//        }).catch(err => console.warn('FCM token error:', err));
//      }
//    });
//
//    // Handle foreground messages — dispatch as CustomEvent for Kotlin
//    onMessage(messaging, payload => {
//      const event = new CustomEvent('fcm-message', { detail: payload });
//      window.dispatchEvent(event);
//    });
//  </script>
//
// REQUIRED: firebase-messaging-sw.js at root of your web output:
//
//  importScripts('https://www.gstatic.com/firebasejs/10.12.0/firebase-app-compat.js');
//  importScripts('https://www.gstatic.com/firebasejs/10.12.0/firebase-messaging-compat.js');
//  firebase.initializeApp({ /* same config as above */ });
//  const messaging = firebase.messaging();
//  messaging.onBackgroundMessage(payload => {
//    const { title, body } = payload.notification;
//    self.registration.showNotification(title, { body, icon: '/icon-192.png' });
//  });
// ─────────────────────────────────────────────────────────────────────────────

actual class FcmTokenService actual constructor() {
    actual val platform: String = "web"

    actual suspend fun getToken(): String? = suspendCancellableCoroutine { cont ->
        val token = getWebFcmToken()
        cont.resume(token)
    }
}

// Reads window.__fcmToken written by Firebase JS SDK in index.html
@JsName("getWebFcmTokenImpl")
private fun getWebFcmToken(): String? {
    return try {
        val token = js("window.__fcmToken") as? String
        if (token.isNullOrBlank()) null else token
    } catch (e: Throwable) {
        null
    }
}