package org.example.project.babygrowthtrackingapplication

import androidx.compose.runtime.*
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.data.LanguageProvider
import org.example.project.babygrowthtrackingapplication.navigation.AppNavigation
import org.example.project.babygrowthtrackingapplication.navigation.rememberPreferencesManager
import org.example.project.babygrowthtrackingapplication.platform.ConfigureFullScreen
import org.example.project.babygrowthtrackingapplication.theme.BabyGrowthTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun App(startRoute: String? = null) {
    ConfigureFullScreen()

    val preferencesManager = rememberPreferencesManager()

    var currentLanguage by remember {
        mutableStateOf(preferencesManager.getCurrentLanguage())
    }

    var currentGenderTheme by remember {
        mutableStateOf(preferencesManager.getGenderTheme())
    }

    var isDarkMode by remember {
        mutableStateOf(preferencesManager.getBoolean("dark_mode", false))
    }

    BabyGrowthTheme(
        genderTheme = currentGenderTheme,
        darkTheme   = isDarkMode
    ) {
        LanguageProvider(language = currentLanguage) {
            AppNavigation(
                currentLanguage     = currentLanguage,
                startRoute          = startRoute,
                onLanguageChange    = { newLanguage ->
                    currentLanguage = newLanguage
                    preferencesManager.setLanguage(newLanguage)
                },
                onDarkModeChange    = { dark ->
                    isDarkMode = dark
                },
                onGenderThemeChange = { theme ->
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