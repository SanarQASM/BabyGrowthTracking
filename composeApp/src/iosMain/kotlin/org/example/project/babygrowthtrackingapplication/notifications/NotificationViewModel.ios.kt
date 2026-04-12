package org.example.project.babygrowthtrackingapplication.notifications

import cocoapods.FirebaseMessaging.FIRMessaging
import platform.Foundation.NSError
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

actual class FcmTokenService actual constructor() {
    actual val platform: String = "ios"

    actual suspend fun getToken(): String? {
        return suspendCoroutine { continuation ->
            FIRMessaging.messaging().tokenWithCompletion { token: String?, error: NSError? ->
                continuation.resume(if (error != null) null else token)
            }
        }
    }
}