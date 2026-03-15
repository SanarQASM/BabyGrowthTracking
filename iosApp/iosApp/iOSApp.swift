import SwiftUI

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
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
        // ✅ Allow all orientations — Compose handles the layout changes.
        // Remove .supportedOrientations if your iOS target < 16 and use
        // the Info.plist approach described above instead.
        .supportedOrientations(.all)  // requires iOS 16+
    }
}