package org.example.project.babygrowthtrackingapplication.platform

import kotlinx.browser.document
import org.w3c.dom.events.Event

actual class AppLifecycleObserver actual constructor() {
    private var listener: AppLifecycleListener? = null

    actual fun register(listener: AppLifecycleListener) {
        this.listener = listener
        document.addEventListener("visibilitychange", { _: Event ->
            // Use asDynamic() to access visibilityState which might be missing in some Kotlin/JS versions
            val state = document.asDynamic().visibilityState as? String
            if (state == "visible") {
                listener.onAppForeground()
            } else {
                listener.onAppBackground()
            }
        })
    }

    actual fun unregister() {
        listener = null
    }
}
