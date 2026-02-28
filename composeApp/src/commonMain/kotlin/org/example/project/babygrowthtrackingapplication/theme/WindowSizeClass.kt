package org.example.project.babygrowthtrackingapplication.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Window size classes based on Material Design 3 guidelines
 * https://m3.material.io/foundations/layout/applying-layout/window-size-classes
 */
enum class WindowSizeClass {
    COMPACT,    // Phone in portrait (< 600dp width)
    MEDIUM,     // Tablet in portrait, phone in landscape (600dp - 840dp)
    EXPANDED    // Tablet in landscape, desktop (> 840dp)
}

/**
 * Determines the window size class based on screen width
 */
fun getWindowSizeClass(windowWidth: Dp): WindowSizeClass {
    return when {
        windowWidth < 600.dp -> WindowSizeClass.COMPACT
        windowWidth < 840.dp -> WindowSizeClass.MEDIUM
        else -> WindowSizeClass.EXPANDED
    }
}

/**
 * Platform-specific screen information
 */
data class ScreenInfo(
    val widthDp: Dp,
    val heightDp: Dp,
    val windowSizeClass: WindowSizeClass
)