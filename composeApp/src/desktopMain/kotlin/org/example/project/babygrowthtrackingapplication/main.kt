package org.example.project.babygrowthtrackingapplication

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.compose.ui.unit.dp

fun main() = application {
    val windowState = rememberWindowState(
        width = 400.dp,
        height = 800.dp
    )

    Window(
        onCloseRequest = ::exitApplication,
        title = "Baby Growth Track",
        state = windowState
    ) {
        App() // Set to true for premium version
    }
}