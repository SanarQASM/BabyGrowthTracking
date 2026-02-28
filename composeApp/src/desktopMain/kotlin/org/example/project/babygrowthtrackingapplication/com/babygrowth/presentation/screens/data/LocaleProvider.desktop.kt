package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.data

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import java.util.Locale


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