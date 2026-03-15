package org.example.project.babygrowthtrackingapplication.theme

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Android implementation of screen dimension helpers.
 *
 * Uses [LocalConfiguration] which Compose re-reads every time the device
 * rotates — no Activity restart needed because AndroidManifest.xml already
 * declares android:configChanges="orientation|screenSize|screenLayout|keyboardHidden".
 *
 * [getScreenWidth]  → screenWidthDp  (swaps with height on rotation ✓)
 * [getScreenHeight] → screenHeightDp (swaps with width on rotation  ✓)
 *
 * Both values are in density-independent pixels, matching Compose's Dp unit.
 */

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
actual fun getScreenWidth(): Dp {
    val configuration = LocalConfiguration.current
    return configuration.screenWidthDp.dp
}

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
actual fun getScreenHeight(): Dp {
    val configuration = LocalConfiguration.current
    return configuration.screenHeightDp.dp
}