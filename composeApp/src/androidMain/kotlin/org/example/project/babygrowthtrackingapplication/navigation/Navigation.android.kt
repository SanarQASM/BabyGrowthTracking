package org.example.project.babygrowthtrackingapplication.navigation

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.russhwolf.settings.SharedPreferencesSettings
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.data.PreferencesManager
import org.example.project.babygrowthtrackingapplication.data.auth.SocialAuthManager

@Composable
actual fun rememberPreferencesManager(): PreferencesManager {
    val context = LocalContext.current
    return remember {
        val sharedPreferences = context.getSharedPreferences(
            "app_preferences",
            Context.MODE_PRIVATE
        )
        val settings = SharedPreferencesSettings(sharedPreferences)
        PreferencesManager(settings)
    }
}

@Composable
actual fun InitializeSocialAuth(socialAuthManager: SocialAuthManager) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity

    LaunchedEffect(activity) {
        activity?.let {
            socialAuthManager.initialize(it)
        }
    }
}

actual fun cleanupSocialAuth(socialAuthManager: SocialAuthManager) {
    socialAuthManager.cleanup()
}