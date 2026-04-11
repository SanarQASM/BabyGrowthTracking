package org.example.project.babygrowthtrackingapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import org.example.project.babygrowthtrackingapplication.data.auth.SocialAuthManager
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val socialAuthManager: SocialAuthManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        socialAuthManager.initialize(this)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            App()
        }
    }

    // Called when the user minimizes or leaves the app (process still alive).
    // The PreferencesManager already persisted the screen via LaunchedEffect
    // in Navigation.kt, so nothing extra is needed here.
    override fun onPause() {
        super.onPause()
        // State is already saved continuously via LaunchedEffect in Navigation.kt
    }

    override fun onDestroy() {
        super.onDestroy()
        socialAuthManager.cleanup()
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}