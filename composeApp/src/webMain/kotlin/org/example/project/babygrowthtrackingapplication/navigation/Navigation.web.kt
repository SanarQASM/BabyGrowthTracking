package org.example.project.babygrowthtrackingapplication.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.data.PreferencesManager
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.data.createSettings
import org.example.project.babygrowthtrackingapplication.data.auth.SocialAuthManager

@Composable
actual fun rememberPreferencesManager(): PreferencesManager {
    return remember { PreferencesManager(createSettings()) }
}

@Composable
actual fun InitializeSocialAuth(socialAuthManager: SocialAuthManager) {
}

actual fun cleanupSocialAuth(socialAuthManager: SocialAuthManager) {
}