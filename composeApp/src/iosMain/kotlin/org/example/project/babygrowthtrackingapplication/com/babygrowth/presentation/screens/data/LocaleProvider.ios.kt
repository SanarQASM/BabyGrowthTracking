package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.data

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import platform.Foundation.NSUserDefaults

@Composable
actual fun rememberCurrentLocale(language: Language): String {
    SideEffect {
        NSUserDefaults.standardUserDefaults.apply {
            setObject(listOf(language.code), forKey = "AppleLanguages")
            synchronize()
        }
    }
    return language.code
}