package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.data

import com.russhwolf.settings.Settings
import com.russhwolf.settings.StorageSettings

// Helper function (NO 'actual' keyword)
fun createWebSettings(): Settings {
    return StorageSettings()
}