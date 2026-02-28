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

    var currentLanguage by remember {
        mutableStateOf(preferencesManager.getCurrentLanguage())
    }

    var currentGenderTheme by remember {
        mutableStateOf(preferencesManager.getGenderTheme())
    }

    var isDarkMode by remember {
        mutableStateOf(false)
    }

    BabyGrowthTheme(
        genderTheme = currentGenderTheme,
        darkTheme = isDarkMode
    ) {
        LanguageProvider(language = currentLanguage) {
            AppNavigation(
                onLanguageChange = { newLanguage ->
                    currentLanguage = newLanguage
                    preferencesManager.setLanguage(newLanguage)
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