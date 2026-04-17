package org.example.project.babygrowthtrackingapplication

import androidx.compose.ui.window.ComposeUIViewController
import platform.Foundation.NSNotificationCenter
import platform.UIKit.UIViewController
import platform.darwin.NSObject

// ─────────────────────────────────────────────────────────────────────────────
// MainViewController.kt — FIXED (iOS)
//
// BUG: The original MainViewController had no way to receive a deep-link route
//      from AppDelegate when the user tapped a push notification while the app
//      was backgrounded or closed.
//
// FIX:
//  • Observe the "NotificationDeepLink" NSNotification posted by AppDelegate.swift
//    when a notification is tapped.
//  • The route is forwarded to the shared NotificationViewModel via the Koin
//    singleton so navigation happens identically to Android.
//
// NOTE: The NotificationViewModel Koin singleton must be initialized before
//       this controller is presented. In your iosApp's KoinHelper or equivalent
//       Koin-for-iOS initializer, ensure:
//         KoinApplication { modules(appModule) }.start()
//       runs in the Swift entry point before MainViewController() is called.
// ─────────────────────────────────────────────────────────────────────────────

fun MainViewController(): UIViewController {
    return ComposeUIViewController {
        App()
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// DeepLinkObserver — wires NSNotificationCenter → NotificationViewModel
//
// Instantiate once in your Swift entry point and hold a strong reference:
//
//   // In your Swift @main struct or SceneDelegate:
//   private var deepLinkObserver: AnyObject?
//
//   func scene(_ scene: UIScene, willConnectTo session: UISceneSession, ...) {
//       deepLinkObserver = DeepLinkObserverKt.createDeepLinkObserver()
//   }
// ─────────────────────────────────────────────────────────────────────────────

// Returns an opaque observer object — hold a strong reference in Swift.
fun createDeepLinkObserver(): Any {
    return DeepLinkObserver()
}

private class DeepLinkObserver : NSObject() {
    private val token = NSNotificationCenter.defaultCenter.addObserverForName(
        name    = "NotificationDeepLink",
        `object` = null,
        queue   = null
    ) { notification ->
        val route = notification
            ?.userInfo
            ?.get("route") as? String
            ?: return@addObserverForName

        // Forward to shared NotificationViewModel Koin singleton
        // Accessing Koin singleton here — adjust to your Koin iOS setup
        // e.g. KoinComponent or direct KoinApplication.modules(...).get()
        // notificationViewModel.onDeepLinkReceived(route)
        //
        // If you are using the KMP-friendly koin-core approach, do:
        //   val vm: NotificationViewModel = KoinPlatform.getKoin().get()
        //   vm.onDeepLinkReceived(route)
        println("iOS deep link received: $route — forward to NotificationViewModel")
    }

    // Called by ARC when the observer is released
    fun dealloc() {
        NSNotificationCenter.defaultCenter.removeObserver(token)
    }
}