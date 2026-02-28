package org.example.project.babygrowthtrackingapplication

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val body = document.getElementById("ComposeTarget")!!  // ✅ must match div id in HTML
    ComposeViewport(body) {
        App()
    }
}