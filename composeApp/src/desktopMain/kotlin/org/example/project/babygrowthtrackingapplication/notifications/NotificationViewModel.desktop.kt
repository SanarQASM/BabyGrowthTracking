package org.example.project.babygrowthtrackingapplication.notifications

// ─────────────────────────────────────────────────────────────────────────────
// FcmTokenService.desktop.kt — NO CHANGE REQUIRED, documented
//
// Desktop (JVM/Compose Desktop) correctly returns null, causing the VM to skip
// FCM registration. Desktop users receive notifications exclusively via the
// 60-second in-app polling loop (NotificationViewModel.startUnreadPolling()).
//
// This is the correct and intended behaviour for a desktop app:
//  • macOS does not use FCM — it uses APNS via the Mac App Store.
//  • Windows 10+ uses WNS (Windows Notification Service).
//  • Linux has no standard push channel.
//
// If you later add OS-level toast notifications on desktop, implement them
// here using java.awt.SystemTray or a platform notification library.
// The polling loop will still trigger in-app badge updates regardless.
// ─────────────────────────────────────────────────────────────────────────────

actual class FcmTokenService actual constructor() {
    actual val platform: String = "desktop"

    actual suspend fun getToken(): String? = null
}