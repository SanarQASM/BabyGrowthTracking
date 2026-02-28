package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.data

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings

actual fun createSettings(): Settings {
    // This will be called from a Composable context
    throw IllegalStateException("Use createSettings(context) for Android")
}

fun createSettings(context: Context): Settings {
    val sharedPreferences = context.getSharedPreferences(
        "app_preferences",
        Context.MODE_PRIVATE
    )
    return SharedPreferencesSettings(sharedPreferences)
}

@Composable
fun rememberSettings(): Settings {
    val context = LocalContext.current
    return androidx.compose.runtime.remember { createSettings(context) }
}