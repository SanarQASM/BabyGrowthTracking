package org.example.project.babygrowthtrackingapplication.notifications

import kotlinx.browser.window
import org.w3c.dom.MessageEvent
import org.w3c.dom.events.Event

// ─────────────────────────────────────────────────────────────────────────────
// WebNotificationBridge.kt — NEW (jsMain only)
// ─────────────────────────────────────────────────────────────────────────────

object WebNotificationBridge {

    private var viewModel: NotificationViewModel? = null

    fun initialize(vm: NotificationViewModel) {
        viewModel = vm

        // ✅ FIX: Explicitly typed lambda parameter (event: Event)
        window.addEventListener("message", { event: Event ->
            handleMessage(event)
        })

        // Also check the URL — service worker may have opened with ?notification_route=...
        checkUrlForDeepLink()
    }

    private fun handleMessage(event: Event) {
        val messageEvent = event as? MessageEvent ?: return
        val data: dynamic = messageEvent.data ?: return
        
        // ✅ FIX: Access dynamic properties directly instead of using js() on variables
        val type = data.type as? String ?: return
        if (type != "NOTIFICATION_DEEP_LINK") return
        
        val route = data.route as? String ?: return
        viewModel?.onDeepLinkReceived(route)
    }

    private fun checkUrlForDeepLink() {
        val search = window.location.search
        if (!search.contains("notification_route")) return
        val params = URLSearchParams(search)
        val route  = params.get("notification_route") ?: return
        if (route.isBlank()) return
        
        window.setTimeout({
            viewModel?.onDeepLinkReceived(route)
            window.history.replaceState(null, "", window.location.pathname)
        }, 500)
    }
}

private class URLSearchParams(search: String) {
    // ✅ FIX: Correctly pass the search string into the constructor
    private val params: dynamic = js("new URLSearchParams(search)")
    fun get(key: String): String? = params.get(key) as? String
}
