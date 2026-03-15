package org.example.project.babygrowthtrackingapplication.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * iOS implementation of screen dimension helpers.
 *
 * WHY LocalWindowInfo instead of UIScreen.mainScreen.bounds?
 * UIScreen.mainScreen gives the physical screen rectangle.  On modern iPhones
 * that value does NOT swap width/height on rotation in all configurations.
 * LocalWindowInfo.containerSize is the Compose window bounds — it always
 * reflects the current orientation and respects Split View / Slide Over on iPad.
 *
 * The values are in pixels; we divide by the display density to get Dp.
 *
 * NOTE: This API is @ExperimentalComposeUiApi — add the opt-in in your
 *       gradle.properties or at the call site if needed.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun getScreenWidth(): Dp {
    val windowInfo = LocalWindowInfo.current
    // containerSize is in pixels; convert to Dp via density
    // Compose Desktop and iOS both expose containerSize correctly.
    return with(androidx.compose.ui.platform.LocalDensity.current) {
        windowInfo.containerSize.width.toDp()
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun getScreenHeight(): Dp {
    val windowInfo = LocalWindowInfo.current
    return with(androidx.compose.ui.platform.LocalDensity.current) {
        windowInfo.containerSize.height.toDp()
    }
}