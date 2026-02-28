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

    // ✅ Koin is now started in MyApplication before this runs
    private val socialAuthManager: SocialAuthManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Initialize the shared SocialAuthManager with this Activity
        socialAuthManager.initialize(this)

        // ✅ Enable edge-to-edge display
        enableEdgeToEdge()

        // ✅ Make status bar transparent
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            App()
        }
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