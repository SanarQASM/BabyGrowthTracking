package org.example.project.babygrowthtrackingapplication.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Window size classes based on Material Design 3 guidelines.
 * https://m3.material.io/foundations/layout/applying-layout/window-size-classes
 */
enum class WindowSizeClass {
    COMPACT,    // Phone portrait (< 600dp width)
    MEDIUM,     // Tablet portrait / phone landscape (600dp–840dp)
    EXPANDED    // Tablet landscape / desktop (> 840dp)
}

/** Derive the correct class from a width value. */
fun getWindowSizeClass(windowWidth: Dp): WindowSizeClass = when {
    windowWidth < 600.dp -> WindowSizeClass.COMPACT
    windowWidth < 840.dp -> WindowSizeClass.MEDIUM
    else                 -> WindowSizeClass.EXPANDED
}

/**
 * Platform-specific screen information passed into the theme.
 *
 * [isLandscape] is true when width > height.
 * On desktop / web the window can be any shape, so we always derive
 * orientation from the actual size rather than a device sensor.
 */
data class ScreenInfo(
    val widthDp        : Dp,
    val heightDp       : Dp,
    val windowSizeClass: WindowSizeClass,
    // Derived — true whenever the container is wider than it is tall.
    val isLandscape    : Boolean = widthDp > heightDp,
)