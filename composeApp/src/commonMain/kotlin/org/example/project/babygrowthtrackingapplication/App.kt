package org.example.project.babygrowthtrackingapplication

import androidx.compose.runtime.*
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.data.LanguageProvider
import org.example.project.babygrowthtrackingapplication.navigation.AppNavigation
import org.example.project.babygrowthtrackingapplication.navigation.rememberPreferencesManager
import org.example.project.babygrowthtrackingapplication.platform.ConfigureFullScreen
import org.example.project.babygrowthtrackingapplication.theme.BabyGrowthTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun App() {
    ConfigureFullScreen()

    val preferencesManager = rememberPreferencesManager()

    // ── Single source of truth for all 3 theme/language states ───────────────
    var currentLanguage by remember {
        mutableStateOf(preferencesManager.getCurrentLanguage())
    }

    var currentGenderTheme by remember {
        mutableStateOf(preferencesManager.getGenderTheme())
    }

    // FIX: read dark-mode from prefs on startup instead of hardcoding false
    var isDarkMode by remember {
        mutableStateOf(preferencesManager.getBoolean("dark_mode", false))
    }

    BabyGrowthTheme(
        genderTheme = currentGenderTheme,
        darkTheme   = isDarkMode
    ) {
        LanguageProvider(language = currentLanguage) {
            AppNavigation(
                // FIX: pass currentLanguage so Navigation.kt doesn't hold a duplicate copy
                currentLanguage      = currentLanguage,

                onLanguageChange     = { newLanguage ->
                    currentLanguage = newLanguage
                    preferencesManager.setLanguage(newLanguage)
                },

                // FIX: wire dark-mode so BabyGrowthTheme actually re-composes
                onDarkModeChange     = { dark ->
                    isDarkMode = dark
                },

                // FIX: wire gender theme so BabyGrowthTheme actually re-composes
                onGenderThemeChange  = { theme ->
                    currentGenderTheme = theme
                }
            )
        }
    }
}

@Preview
@Composable
fun AppPreview() {
    App()
}