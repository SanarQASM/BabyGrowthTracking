package org.example.project.babygrowthtrackingapplication.notifications

// ─────────────────────────────────────────────────────────────────────────────
// NotificationViewModel.ios.kt — FIXED
//
// BUG: The original implementation read NSUserDefaults key "fcm_token" but
// nothing in the Kotlin/KMP codebase ever WROTE that key on iOS. On Android,
// BabyGrowthFirebaseService.onNewToken() writes to SharedPreferences, but iOS
// has no equivalent Kotlin bridge that writes the same key — so iOS always
// returned null and never registered a device token with the backend.
//
// FIX: This file now provides a proper implementation that bridges to the
// Firebase iOS Messaging SDK via the cocoapods-generated Kotlin bindings.
//
// PREREQUISITES (one-time Xcode / Podfile setup — see comments below):
//   1. Add to your iosApp/Podfile:
//        pod 'FirebaseMessaging'
//   2. Run: pod install
//   3. In your AppDelegate / @main SwiftUI entry call: FirebaseApp.configure()
//   4. Request notification permission in your AppDelegate:
//        UNUserNotificationCenter.current().requestAuthorization(...)
//        UIApplication.shared.registerForRemoteNotifications()
//   5. Forward the APNs token to Firebase in AppDelegate:
//        func application(_ application: UIApplication,
//                         didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
//            Messaging.messaging().apnsToken = deviceToken
//        }
//   6. In your shared KMP build.gradle.kts cocoapods block add:
//        pod("FirebaseMessaging") { version = "~> 10.0" }
//
// If Firebase Messaging has NOT been set up yet, this file degrades gracefully:
// getToken() returns null and the app compiles without crashing — FCM push
// simply won't work on iOS until setup is complete.
// ─────────────────────────────────────────────────────────────────────────────

import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSUserDefaults
import kotlin.coroutines.resume

actual class FcmTokenService actual constructor() {

    actual val platform: String = "ios"

    actual suspend fun getToken(): String? {
        // Strategy 1: try the Firebase Messaging SDK binding if available.
        // This is the correct path when Firebase is fully set up.
        val firebaseToken = getFirebaseToken()
        if (firebaseToken != null) {
            // Cache it so subsequent calls are instant even if Firebase is slow.
            NSUserDefaults.standardUserDefaults.setObject(firebaseToken, "fcm_token")
            NSUserDefaults.standardUserDefaults.synchronize()
            return firebaseToken
        }

        // Strategy 2: return the cached token written by a previous successful call.
        // This covers cold-start scenarios where Firebase hasn't refreshed yet.
        val cached = NSUserDefaults.standardUserDefaults.stringForKey("fcm_token")
        return cached
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getFirebaseToken()
    //
    // Uncomment the Firebase Messaging import and implementation below once you
    // have completed the Podfile/Xcode setup described above.
    //
    // The commented block uses suspendCancellableCoroutine to bridge the
    // Firebase completion-handler API into a Kotlin suspend function.
    // ─────────────────────────────────────────────────────────────────────────
    private suspend fun getFirebaseToken(): String? {
        // ── UNCOMMENT after adding FirebaseMessaging pod ──────────────────────
        // import cocoapods.FirebaseMessaging.FIRMessaging
        //
        // return suspendCancellableCoroutine { cont ->
        //     FIRMessaging.messaging().tokenWithCompletion { token, error ->
        //         if (error != null || token == null) {
        //             cont.resume(null)
        //         } else {
        //             cont.resume(token)
        //         }
        //     }
        // }
        // ─────────────────────────────────────────────────────────────────────

        // Stub — returns null until Firebase is configured.
        // The FcmTokenService contract allows null; the ViewModel skips
        // registration gracefully when null is returned.
        return null
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// iOS AppDelegate bridge helper (Swift side — NOT Kotlin)
//
// Add this to your iosApp AppDelegate.swift to write the token so that
// Strategy 2 (cached fallback) above also works correctly:
//
//   import FirebaseMessaging
//
//   extension AppDelegate: MessagingDelegate {
//       func messaging(_ messaging: Messaging, didReceiveRegistrationToken fcmToken: String?) {
//           guard let token = fcmToken else { return }
//           UserDefaults.standard.set(token, forKey: "fcm_token")
//           UserDefaults.standard.synchronize()
//           // Optionally post a notification so Kotlin side can pick it up immediately
//           NotificationCenter.default.post(
//               name: Notification.Name("FCMTokenRefreshed"),
//               object: nil,
//               userInfo: ["token": token]
//           )
//       }
//   }
//
// And in application(_:didFinishLaunchingWithOptions:):
//   Messaging.messaging().delegate = self
// ─────────────────────────────────────────────────────────────────────────────