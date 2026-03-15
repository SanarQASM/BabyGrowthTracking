package org.example.project.babygrowthtrackingapplication.theme

import androidx.compose.runtime.*
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.browser.window
import org.w3c.dom.events.Event

/**
 * Web (WasmJS + JS) implementation of screen dimension helpers.
 *
 * Uses window.innerWidth / window.innerHeight so the values reflect the
 * visible browser viewport (respects mobile browser chrome, Dev Tools panel, etc.).
 *
 * A ResizeObserver on the document root triggers recomposition whenever the
 * viewport dimensions change — covering:
 *   • Browser window resize on desktop
 *   • Device rotation on mobile browsers
 *   • Split-screen / picture-in-picture mode changes
 *
 * The observer is cleaned up automatically when the composable leaves the
 * composition via DisposableEffect.
 */

@Composable
actual fun getScreenWidth(): Dp {
    var width by remember { mutableStateOf(window.innerWidth.dp) }

    DisposableEffect(Unit) {
        val handler: (Event) -> Unit = { width = window.innerWidth.dp }
        window.addEventListener("resize", handler)
        onDispose { window.removeEventListener("resize", handler) }
    }

    return width
}

@Composable
actual fun getScreenHeight(): Dp {
    var height by remember { mutableStateOf(window.innerHeight.dp) }

    DisposableEffect(Unit) {
        val handler: (Event) -> Unit = { height = window.innerHeight.dp }
        window.addEventListener("resize", handler)
        onDispose { window.removeEventListener("resize", handler) }
    }

    return height
}