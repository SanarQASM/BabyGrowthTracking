package org.example.project.babygrowthtrackingapplication.notifications

import platform.Foundation.NSUserDefaults

// ─────────────────────────────────────────────────────────────────────────────
// iOS actual implementation of FcmTokenService
//
// For Firebase on iOS you need:
//   1. GoogleService-Info.plist in your iosApp Xcode project
//   2. Firebase/Messaging pod in your Podfile:
//      pod 'FirebaseMessaging'
//   3. FirebaseApp.configure() in AppDelegate / @UIApplicationMain
//   4. Request notification permission from the user
//
// This implementation bridges to the Objective-C Firebase Messaging SDK
// via the cocoapods-generated Kotlin bindings.
//
// If you haven't added Firebase iOS yet, the stub below returns null safely
// so the app won't crash — it just won't register for FCM on iOS.
// ─────────────────────────────────────────────────────────────────────────────

actual class FcmTokenService actual constructor() {
    actual val platform: String = "ios"
    actual suspend fun getToken(): String? {
        return NSUserDefaults.standardUserDefaults
            .stringForKey("fcm_token")
    }
}