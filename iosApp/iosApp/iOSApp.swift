import SwiftUI
import FirebaseCore
import FirebaseMessaging
/**
 * iOSApp.swift — UPDATED FOR LANDSCAPE SUPPORT
 *
 * Changes:
 *  1. Added .supportedOrientations(.all) on the WindowGroup scene modifier
 *     so iOS allows portrait, landscape-left, landscape-right, and
 *     portrait-upside-down on iPhone/iPad.
 *
 *  2. If your iOS deployment target is < 16 (where the scene modifier was
 *     introduced) use the Info.plist approach instead — see comment below.
 *
 * Info.plist approach (for iOS < 16 or if the modifier is unavailable):
 *  Add / ensure the following keys are present in iosApp/iosApp/Info.plist:
 *
 *  <key>UISupportedInterfaceOrientations</key>
 *  <array>
 *      <string>UIInterfaceOrientationPortrait</string>
 *      <string>UIInterfaceOrientationLandscapeLeft</string>
 *      <string>UIInterfaceOrientationLandscapeRight</string>
 *  </array>
 *  <key>UISupportedInterfaceOrientations~ipad</key>
 *  <array>
 *      <string>UIInterfaceOrientationPortrait</string>
 *      <string>UIInterfaceOrientationPortraitUpsideDown</string>
 *      <string>UIInterfaceOrientationLandscapeLeft</string>
 *      <string>UIInterfaceOrientationLandscapeRight</string>
 *  </array>
 *
 * ContentView.swift does NOT need changes — ComposeUIViewController already
 * fills the entire safe area and Compose reads LocalWindowInfo.containerSize
 * which UIKit updates automatically on rotation.
 */

@main
struct iOSApp: App {
    init() {
        FirebaseApp.configure()

        Messaging.messaging().token { token, error in
            if let error = error {
                print("FCM token error: \(error)")
            } else if let token = token {
                print("FCM token: \(token)")
                // Store it in UserDefaults so Kotlin can read it
                UserDefaults.standard.set(token, forKey: "fcm_token")
            }
        }
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}