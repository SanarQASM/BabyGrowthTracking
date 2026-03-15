package org.example.project.babygrowthtrackingapplication.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp

/**
 * Desktop (JVM) implementation of screen dimension helpers.
 *
 * IMPORTANT FIX: The previous implementation used
 *   Toolkit.getDefaultToolkit().screenSize
 * which returns the *physical monitor* resolution — not the application window
 * size.  On desktop the window can be any size and the user can resize it, so
 * the monitor resolution is completely wrong for layout decisions.
 *
 * LocalWindowInfo.containerSize gives the current Compose window bounds in
 * pixels.  It updates reactively whenever the user resizes the window, so all
 * breakpoint decisions (COMPACT / MEDIUM / EXPANDED, isLandscape) stay correct
 * at any window size without any additional listener code.
 *
 * The Main.kt window already uses rememberWindowState — no extra wiring needed.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun getScreenWidth(): Dp {
    val windowInfo = LocalWindowInfo.current
    return with(LocalDensity.current) {
        windowInfo.containerSize.width.toDp()
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun getScreenHeight(): Dp {
    val windowInfo = LocalWindowInfo.current
    return with(LocalDensity.current) {
        windowInfo.containerSize.height.toDp()
    }
}