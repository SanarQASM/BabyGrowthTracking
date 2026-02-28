package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.data
import com.russhwolf.settings.PreferencesSettings
import com.russhwolf.settings.Settings
import java.util.prefs.Preferences

actual fun createSettings(): Settings {
    val preferences = Preferences.userRoot().node("app_preferences")
    return PreferencesSettings(preferences)
}