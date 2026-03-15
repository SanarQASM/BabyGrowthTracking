package org.example.project.babygrowthtrackingapplication.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.example.project.babygrowthtrackingapplication.theme.LocalIsLandscape
import org.example.project.babygrowthtrackingapplication.theme.LocalScreenInfo
import org.example.project.babygrowthtrackingapplication.theme.WindowSizeClass

// ─────────────────────────────────────────────────────────────────────────────
// AdaptiveLayout
//
// Switches between a portrait composable and a landscape composable based on
// the current orientation derived from BabyGrowthTheme / LocalIsLandscape.
//
// Usage:
//   AdaptiveLayout(
//       portrait  = { MyPortraitContent() },
//       landscape = { MyLandscapeContent() },
//   )
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun AdaptiveLayout(
    modifier : Modifier = Modifier,
    portrait : @Composable () -> Unit,
    landscape: @Composable () -> Unit,
) {
    val isLandscape = LocalIsLandscape.current
    Box(modifier = modifier) {
        if (isLandscape) landscape() else portrait()
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TwoPaneLayout
//
// In landscape / EXPANDED: places [start] and [end] side-by-side in a Row.
// In portrait / COMPACT:   stacks them vertically in a Column.
//
// [startWeight] controls how much of the horizontal space the start pane gets
// when side-by-side (default 0.4f = 40 % left / 60 % right).
//
// Usage (auth screens, onboarding, profile detail):
//   TwoPaneLayout(
//       start = { IllustrationOrLogo() },
//       end   = { FormOrContent() },
//   )
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun TwoPaneLayout(
    modifier    : Modifier = Modifier,
    startWeight : Float    = 0.4f,
    start       : @Composable RowScope.() -> Unit,
    end         : @Composable RowScope.() -> Unit,
    portraitFallback: (@Composable ColumnScope.() -> Unit)? = null,
) {
    val isLandscape     = LocalIsLandscape.current
    val windowSizeClass = LocalScreenInfo.current.windowSizeClass

    val useSideBySide = isLandscape ||
            windowSizeClass == WindowSizeClass.EXPANDED ||
            windowSizeClass == WindowSizeClass.MEDIUM

    if (useSideBySide) {
        Row(modifier = modifier.fillMaxSize()) {
            start()
            end()
        }
    } else {
        if (portraitFallback != null) {
            Column(modifier = modifier.fillMaxSize()) {
                portraitFallback()
            }
        } else {
            // Default portrait: just stack start then end vertically
            Column(modifier = modifier.fillMaxSize()) {
                Row { start() }
                Row { end() }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// isLandscape() helper (non-Composable) — DO NOT USE in composables.
// Use LocalIsLandscape.current instead.
// This exists only for logic outside the composition (e.g. ViewModel).
// ─────────────────────────────────────────────────────────────────────────────

/** Convenience Composable accessor — identical to [LocalIsLandscape].current */
@Composable
fun rememberIsLandscape(): Boolean = LocalIsLandscape.current

/** True when layout should show the side navigation rail instead of the bottom bar. */
@Composable
fun rememberUseSideRail(): Boolean {
    val isLandscape     = LocalIsLandscape.current
    val windowSizeClass = LocalScreenInfo.current.windowSizeClass
    return isLandscape || windowSizeClass == WindowSizeClass.EXPANDED
}