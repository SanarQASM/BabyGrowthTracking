package org.example.project.babygrowthtrackingapplication.notifications

import kotlinx.coroutines.await
import kotlin.js.JsName
import kotlin.js.Promise

@JsName("getFcmToken")
external fun getFcmToken(): Promise<String?>

actual class FcmTokenService actual constructor() {
    actual val platform: String = "web"

    actual suspend fun getToken(): String? {
        return try {
            getFcmToken().await()
        } catch (_: Throwable) {
            null
        }
    }
}