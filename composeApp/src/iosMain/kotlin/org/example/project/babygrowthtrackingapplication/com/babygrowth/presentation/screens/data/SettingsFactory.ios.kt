package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.data

import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import platform.Foundation.NSUserDefaults

actual fun createSettings(): Settings {
    return NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults)
}