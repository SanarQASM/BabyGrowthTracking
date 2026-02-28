// androidMain/kotlin/org/example/project/babygrowthtrackingapplication/platform/FullScreen.android.kt

package org.example.project.babygrowthtrackingapplication.platform

import android.app.Activity
import android.os.Build
import android.view.View
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

/**
 * Android implementation for full screen mode
 * - Hides status bar (time, battery, notifications)
 * - Hides navigation bar (optional, currently enabled)
 * - Enables edge-to-edge content
 * - Uses immersive sticky mode (bars auto-hide after swipe)
 * - Supports Android API 21+ (Lollipop and above)
 */
@Composable
actual fun ConfigureFullScreen() {
    val context = LocalContext.current

    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window
        if (window != null) {
            // Keep screen on during the app session (optional)
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

            // Enable edge-to-edge content
            WindowCompat.setDecorFitsSystemWindows(window, false)

            val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
            windowInsetsController?.apply {
                // Hide both status bar and navigation bar
                hide(WindowInsetsCompat.Type.statusBars())
                hide(WindowInsetsCompat.Type.navigationBars())

                // If you only want to hide status bar, comment out the line above and uncomment this:
                // hide(WindowInsetsCompat.Type.statusBars())

                // Immersive sticky mode - bars stay hidden until user swipes from edge
                systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }

            // Support for older Android versions (API < 30)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                @Suppress("DEPRECATION")
                window.decorView.systemUiVisibility = (
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                or View.SYSTEM_UI_FLAG_FULLSCREEN
                                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        )
            }
        }

        onDispose {
            // Cleanup: Remove keep screen on flag
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

            // Optionally restore system bars when leaving
            // Uncomment if you want to show bars again when app is disposed
            /*
            windowInsetsController?.apply {
                show(WindowInsetsCompat.Type.statusBars())
                show(WindowInsetsCompat.Type.navigationBars())
            }
            */
        }
    }
}