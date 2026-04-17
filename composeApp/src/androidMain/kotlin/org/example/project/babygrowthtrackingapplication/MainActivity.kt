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
import org.example.project.babygrowthtrackingapplication.notifications.NotificationViewModel
import org.koin.android.ext.android.inject

// ─────────────────────────────────────────────────────────────────────────────
// MainActivity — FIXED
//
// BUG: onNewIntent() set pendingRoute.value but never forwarded the route to
//      NotificationViewModel. If the app was already running when the user tapped
//      a notification, the deep-link was silently dropped.
//
// FIX:
//  1. Inject NotificationViewModel from the Koin graph (same singleton the
//     Compose tree uses, registered in AppModule.kt).
//  2. In onNewIntent(), call notificationViewModel.onDeepLinkReceived(route)
//     so the VM can navigate even when the activity is resumed rather than created.
//  3. Also call it in onCreate() so cold-start taps are handled by the same code path.
// ─────────────────────────────────────────────────────────────────────────────

class MainActivity : ComponentActivity() {

    private val socialAuthManager: SocialAuthManager by inject()

    // Keep the mutableStateOf for the initial startRoute passed to App(),
    // but route ALL deep-link handling through NotificationViewModel.
    private val pendingRoute = mutableStateOf<String?>(null)

    // Injected so we can call onDeepLinkReceived() from onNewIntent()
    // NOTE: NotificationViewModel must be registered as a singleton in your
    //       Koin AppModule (see AppModule.kt fix file).
    private val notificationViewModel: NotificationViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        socialAuthManager.initialize(this)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        BabyGrowthFirebaseService.createChannels(this)

        // Handle cold-start notification tap
        val startRoute = intent.getStringExtra("notification_route")
        pendingRoute.value = startRoute
        // Also tell the VM so it navigates as soon as the screen is composed
        startRoute?.let { notificationViewModel.onDeepLinkReceived(it) }

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

    // ─────────────────────────────────────────────────────────────────────────
    // BUG FIX: onNewIntent was updating pendingRoute but App() had already
    // composed — the startRoute parameter is read once at composition time and
    // does not recompose when pendingRoute changes because it is not observable
    // inside App(). The only reliable way to handle this is through the VM.
    // ─────────────────────────────────────────────────────────────────────────
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val route = intent.getStringExtra("notification_route") ?: return
        pendingRoute.value = route
        // Forward to ViewModel — this triggers navigation via pendingNavigateTo state
        notificationViewModel.onDeepLinkReceived(route)
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}