package org.example.project.babygrowthtrackingapplication.theme

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Composition local that tells every composable whether the app is currently
 * in landscape orientation / wide-window layout.
 *
 * Provided by BabyGrowthTheme (Theme.kt).
 * Derived from ScreenInfo.isLandscape.
 *
 * Usage:
 *   val isLandscape = LocalIsLandscape.current
 */
val LocalIsLandscape = staticCompositionLocalOf { false }