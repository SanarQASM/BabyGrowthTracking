package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.data

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.core.os.LocaleListCompat

@Composable
actual fun rememberCurrentLocale(language: Language): String {
    SideEffect {
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(language.code)
        )
    }
    return language.code
}