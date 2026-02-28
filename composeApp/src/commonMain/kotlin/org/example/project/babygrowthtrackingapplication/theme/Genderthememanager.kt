package org.example.project.babygrowthtrackingapplication.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

/**
 * Theme manager to handle gender theme switching
 * In a real app, this should persist the theme preference using DataStore or SharedPreferences
 *
 * Usage:
 * ```
 * val themeManager = rememberGenderThemeManager()
 *
 * // Wrap your app
 * BabyGrowthTheme(genderTheme = themeManager.currentTheme.value) {
 *     // Your app content
 * }
 *
 * // Switch theme when gender is selected
 * themeManager.setGenderTheme(GenderTheme.GIRL)
 * ```
 */
class GenderThemeManager {
    private val _currentTheme: MutableState<GenderTheme> = mutableStateOf(GenderTheme.NEUTRAL)
    val currentTheme: MutableState<GenderTheme> = _currentTheme

    /**
     * Set the gender theme
     */
    fun setGenderTheme(theme: GenderTheme) {
        _currentTheme.value = theme
        // TODO: Save to persistent storage (DataStore/SharedPreferences)
        // saveThemePreference(theme)
    }

    /**
     * Get the current gender theme
     */
    fun getCurrentTheme(): GenderTheme {
        return _currentTheme.value
    }

    /**
     * Set theme based on baby's gender
     */
    fun setThemeFromGender(isFemale: Boolean) {
        setGenderTheme(if (isFemale) GenderTheme.GIRL else GenderTheme.BOY)
    }

    /**
     * Reset to neutral theme
     */
    fun resetToNeutral() {
        setGenderTheme(GenderTheme.NEUTRAL)
    }
}

/**
 * Remember a GenderThemeManager instance
 */
@Composable
fun rememberGenderThemeManager(): GenderThemeManager {
    return remember { GenderThemeManager() }
}