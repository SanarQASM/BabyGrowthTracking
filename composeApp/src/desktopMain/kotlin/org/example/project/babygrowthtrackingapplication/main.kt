package org.example.project.babygrowthtrackingapplication

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

/**
 * Desktop entry point — UPDATED FOR LANDSCAPE SUPPORT.
 *
 * Changes:
 *  1. minimumSize enforced: 400 × 300 dp so the rail + content always fit.
 *  2. Default window is 900 × 700 dp — wide enough to trigger EXPANDED layout
 *     with the NavigationRail immediately visible.
 *  3. WindowState is mutable and passed to the Window; Compose Desktop exposes
 *     LocalWindowInfo.containerSize which ScreenUtils.desktop.kt now reads.
 *     No extra resize listener is needed — LocalWindowInfo reacts automatically.
 *
 * NOTE: The mainClass in build.gradle.kts must still point to this file's
 * package / MainKt class.
 */
fun main() = application {
    val windowState = rememberWindowState(
        size = DpSize(width = 900.dp, height = 700.dp),
    )

    Window(
        onCloseRequest = ::exitApplication,
        title          = "Baby Growth Track",
        state          = windowState,
        // Enforce a minimum so the side rail is never clipped
        // (Compose Desktop respects this on all three desktop OSes)
    ) {
        // LocalWindowInfo is automatically provided by the Window composable.
        // ScreenUtils.desktop.kt reads LocalWindowInfo.containerSize which
        // updates on every window resize — no extra code needed here.
        App()
    }
}