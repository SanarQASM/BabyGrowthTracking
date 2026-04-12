package org.example.project.babygrowthtrackingapplication.notifications

// Desktop has no FCM support — push notifications are not available
actual class FcmTokenService actual constructor() {
    actual val platform: String = "desktop"

    actual suspend fun getToken(): String? = null
}