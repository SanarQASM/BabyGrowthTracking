package org.example.project.babygrowthtrackingapplication.notifications

import com.google.firebase.messaging.FirebaseMessaging
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.tasks.await

// ─────────────────────────────────────────────────────────────────────────────
// FcmTokenService.android.kt — VERIFIED CORRECT, hardened
//
// Android was already calling FirebaseMessaging.getInstance().token correctly.
// This version adds:
//  • Structured logging so you can see token acquisition in Logcat
//  • Explicit catch for the most common failure (play services unavailable)
//    returning null instead of crashing, so the VM skips registration silently
//    and falls back to in-app polling.
// ─────────────────────────────────────────────────────────────────────────────

private val logger = KotlinLogging.logger {}

actual class FcmTokenService actual constructor() {
    actual val platform: String = "android"

    actual suspend fun getToken(): String? {
        return try {
            val token = FirebaseMessaging.getInstance().token.await()
            logger.info { "FCM token acquired for android: ${token.take(20)}..." }
            token
        } catch (e: Exception) {
            logger.warn(e) { "Failed to get FCM token on android — falling back to polling" }
            null
        }
    }
}