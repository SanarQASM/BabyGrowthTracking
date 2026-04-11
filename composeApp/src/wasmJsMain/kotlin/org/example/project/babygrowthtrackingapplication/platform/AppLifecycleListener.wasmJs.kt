package org.example.project.babygrowthtrackingapplication.platform

import kotlinx.browser.document

// ── JS Interop Helper ────────────────────────────────────────────────────────
// In current Kotlin/Wasm bindings, visibilityState might be missing or 
// hard to access on the Document object. This helper uses @JsFun to
// call the browser API directly.
// ─────────────────────────────────────────────────────────────────────────────
@JsFun("() => document.visibilityState")
private external fun getVisibilityState(): String

actual class AppLifecycleObserver actual constructor() {
    private var listener: AppLifecycleListener? = null

    actual fun register(listener: AppLifecycleListener) {
        this.listener = listener
        
        // In Wasm, the event listener lambda must return JsAny?
        document.addEventListener("visibilitychange") {
            if (getVisibilityState() == "visible") {
                listener.onAppForeground()
            } else {
                listener.onAppBackground()
            }
            null // Return null to satisfy JsAny?
        }
    }

    actual fun unregister() {
        listener = null
    }
}
