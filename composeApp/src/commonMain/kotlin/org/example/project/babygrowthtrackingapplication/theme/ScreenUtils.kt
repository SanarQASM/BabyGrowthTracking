package org.example.project.babygrowthtrackingapplication.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp

/**
 * Platform-specific screen dimension providers.
 *
 * These are `expect` declarations — each platform target (androidMain,
 * iosMain, desktopMain, wasmJsMain / jsMain) provides an `actual` implementation.
 *
 * HOW THEY DRIVE LANDSCAPE SUPPORT:
 *   BabyGrowthTheme calls both functions on every recomposition.
 *   When the device rotates (or the desktop window is resized):
 *     • getScreenWidth()  returns the new wider value
 *     • getScreenHeight() returns the new shorter value
 *     • isLandscape = width > height  becomes true
 *     • LocalIsLandscape.current flips to true
 *     • HomeScreen switches from BottomNavigationBar → SideNavigationRail
 *     • All screens switch from portrait to landscape layout branches
 *
 * Platform implementations:
 *   Android  → LocalConfiguration.current (auto-updates on rotation)
 *   iOS      → LocalWindowInfo.containerSize (auto-updates on rotation)
 *   Desktop  → LocalWindowInfo.containerSize (auto-updates on window resize)
 *   WasmJS   → window.innerWidth/innerHeight + resize event listener
 */

@Composable
expect fun getScreenWidth(): Dp

@Composable
expect fun getScreenHeight(): Dp