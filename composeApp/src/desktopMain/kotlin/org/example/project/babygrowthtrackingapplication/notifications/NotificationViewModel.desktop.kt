package org.example.project.babygrowthtrackingapplication.notifications

// ─────────────────────────────────────────────────────────────────────────────
// Desktop (JVM) actual implementation of FcmTokenService
//
// FCM does not support a native desktop push channel, so we return null.
// The NotificationViewModel handles null gracefully — it simply skips
// registration. In-app polling (every 60s) still works for the notification
// bell badge on desktop.
//
// If you later add Web Push or another desktop notification channel,
// replace getToken() with the appropriate SDK call.
// ─────────────────────────────────────────────────────────────────────────────

actual class FcmTokenService actual constructor() {
    actual val platform: String = "desktop"

    actual suspend fun getToken(): String? = null
}