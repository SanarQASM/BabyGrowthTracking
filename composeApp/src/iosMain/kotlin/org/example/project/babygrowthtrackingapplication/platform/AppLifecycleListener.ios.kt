package org.example.project.babygrowthtrackingapplication.platform

import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.UIKit.UIApplicationDidBecomeActiveNotification
import platform.UIKit.UIApplicationDidEnterBackgroundNotification
import platform.darwin.NSObjectProtocol

actual class AppLifecycleObserver actual constructor() {
    private var listener: AppLifecycleListener? = null
    private val tokens = mutableListOf<NSObjectProtocol>()

    actual fun register(listener: AppLifecycleListener) {
        this.listener = listener
        val center = NSNotificationCenter.defaultCenter
        val mainQueue = NSOperationQueue.mainQueue

        // Observer for foreground
        center.addObserverForName(
            name = UIApplicationDidBecomeActiveNotification,
            `object` = null,
            queue = mainQueue
        ) { _ ->
            this.listener?.onAppForeground()
        }.also { tokens.add(it) }

        // Observer for background
        center.addObserverForName(
            name = UIApplicationDidEnterBackgroundNotification,
            `object` = null,
            queue = mainQueue
        ) { _ ->
            this.listener?.onAppBackground()
        }.also { tokens.add(it) }
    }

    actual fun unregister() {
        val center = NSNotificationCenter.defaultCenter
        tokens.forEach { center.removeObserver(it) }
        tokens.clear()
        listener = null
    }
}
