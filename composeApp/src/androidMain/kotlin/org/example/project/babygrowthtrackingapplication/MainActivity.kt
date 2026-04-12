package org.example.project.babygrowthtrackingapplication

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import org.example.project.babygrowthtrackingapplication.data.auth.SocialAuthManager
import org.example.project.babygrowthtrackingapplication.notifications.BabyGrowthFirebaseService
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val socialAuthManager: SocialAuthManager by inject()
    private val pendingRoute = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        socialAuthManager.initialize(this)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        BabyGrowthFirebaseService.createChannels(this)

        // Capture deep link route from notification tap
        pendingRoute.value = intent.getStringExtra("notification_route")

        setContent {
            App(startRoute = pendingRoute.value)
        }
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        socialAuthManager.cleanup()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        pendingRoute.value = intent.getStringExtra("notification_route")
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}