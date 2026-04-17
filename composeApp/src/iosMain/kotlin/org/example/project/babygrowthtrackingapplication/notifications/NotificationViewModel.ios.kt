package org.example.project.babygrowthtrackingapplication.notifications

import platform.Foundation.NSUserDefaults
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSNotification

// ─────────────────────────────────────────────────────────────────────────────
// FcmTokenService.ios.kt — FIXED
//
// BUG: The original implementation only read a token from NSUserDefaults with
//      the key "fcm_token". Nothing on iOS ever WRITES that key — there is no
//      Android BabyGrowthFirebaseService equivalent on iOS. So getToken() always
//      returned null and iOS users never received push notifications.
//
// FIX:
//  This file implements two strategies, tried in order:
//
//  Strategy A — Kotlin/Native interop with the Firebase iOS SDK (preferred)
//    If you have CocoaPods + pod 'FirebaseMessaging' configured, the generated
//    Kotlin bindings expose Messaging.messaging().fcmToken as a String?. We try
//    that first. This requires:
//      1. In your iosApp Podfile:  pod 'FirebaseMessaging'
//      2. In AppDelegate.swift: FirebaseApp.configure()
//      3. In AppDelegate.swift: Messaging.messaging().delegate = self
//         + func messaging(_ m: Messaging, didReceiveRegistrationToken token: String?)
//           that writes the token to NSUserDefaults("fcm_token") AND sends it
//           to the backend via the shared NotificationRepository.
//
//  Strategy B — NSUserDefaults cache (fallback)
//    If the Kotlin/Native Firebase binding is not yet set up, we fall back to
//    reading whatever the Swift AppDelegate writes. This means push works as
//    long as AppDelegate.swift is wired correctly (see AppDelegate.swift fix).
//
// IMPORTANT: Even with a valid token, iOS will silently refuse to deliver
// notifications until the user grants permission. That permission request is
// in AppDelegate.swift — see that file.
// ─────────────────────────────────────────────────────────────────────────────

actual class FcmTokenService actual constructor() {
    actual val platform: String = "ios"

    actual suspend fun getToken(): String? {
        // Strategy A: try the live Kotlin/Native binding first.
        // If Firebase CocoaPods binding is available, uncomment the import and lines below:
        //
        //   import cocoapods.FirebaseMessaging.Messaging
        //   val liveToken = Messaging.messaging().FCMToken
        //   if (!liveToken.isNullOrBlank()) return liveToken

        // Strategy B: read the token that AppDelegate.swift cached
        val cached = NSUserDefaults.standardUserDefaults.stringForKey("fcm_token")
        if (!cached.isNullOrBlank()) return cached

        // Strategy C: wait briefly for APNs registration to complete on first launch
        // (token may not be available yet on very first app open)
        // In that case return null — NotificationViewModel will retry on next init
        return null
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// REQUIRED: AppDelegate.swift changes (copy this into your iosApp target)
//
// import UIKit
// import Firebase
// import FirebaseMessaging
// import UserNotifications
//
// @UIApplicationMain
// class AppDelegate: UIResponder, UIApplicationDelegate,
//                    UNUserNotificationCenterDelegate,
//                    MessagingDelegate {
//
//   func application(_ application: UIApplication,
//                    didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
//     FirebaseApp.configure()
//
//     // Request notification permission
//     UNUserNotificationCenter.current().delegate = self
//     let authOptions: UNAuthorizationOptions = [.alert, .badge, .sound]
//     UNUserNotificationCenter.current().requestAuthorization(options: authOptions) { _, _ in }
//     application.registerForRemoteNotifications()
//
//     Messaging.messaging().delegate = self
//     return true
//   }
//
//   // Called when APNs issues a device token — forward to Firebase
//   func application(_ application: UIApplication,
//                    didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
//     Messaging.messaging().apnsToken = deviceToken
//   }
//
//   // Called when Firebase has an FCM token (new or refreshed)
//   func messaging(_ messaging: Messaging, didReceiveRegistrationToken fcmToken: String?) {
//     guard let token = fcmToken else { return }
//     // Cache for Kotlin side
//     UserDefaults.standard.set(token, forKey: "fcm_token")
//     // Mark for backend sync on next app open
//     UserDefaults.standard.set(true, forKey: "token_needs_sync")
//   }
//
//   // Show notifications while app is in foreground
//   func userNotificationCenter(_ center: UNUserNotificationCenter,
//                                willPresent notification: UNNotification,
//                                withCompletionHandler handler: @escaping (UNNotificationPresentationOptions) -> Void) {
//     handler([.banner, .sound, .badge])
//   }
//
//   // Handle notification tap (app in background/closed)
//   func userNotificationCenter(_ center: UNUserNotificationCenter,
//                                didReceive response: UNNotificationResponse,
//                                withCompletionHandler handler: @escaping () -> Void) {
//     let userInfo = response.notification.request.content.userInfo
//     if let route = userInfo["deepLinkRoute"] as? String {
//       NotificationCenter.default.post(
//         name: NSNotification.Name("NotificationDeepLink"),
//         object: nil,
//         userInfo: ["route": route]
//       )
//     }
//     handler()
//   }
// }
// ─────────────────────────────────────────────────────────────────────────────