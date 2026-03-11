package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.data

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import java.util.Locale

/**
 * Desktop (JVM) implementation of rememberCurrentLocale.
 *
 * `Locale.setDefault()` updates the JVM's default locale, which Compose
 * Multiplatform's resource system uses when resolving string resources.
 *
 * Combined with the `key(language)` block in LanguageProvider (commonMain),
 * this makes all `stringResource()` calls in the entire UI tree re-execute
 * with the new locale as soon as the user picks a language — no restart needed.
 */
@Composable
actual fun rememberCurrentLocale(language: Language): String {
    SideEffect {
        val locale = when (language) {
            Language.ENGLISH        -> Locale.ENGLISH
            Language.ARABIC         -> Locale("ar")
            Language.KURDISH_SORANI -> Locale("ku")
            Language.KURDISH_BADINI -> Locale("ckb")
        }
        Locale.setDefault(locale)
    }
    return language.code
}