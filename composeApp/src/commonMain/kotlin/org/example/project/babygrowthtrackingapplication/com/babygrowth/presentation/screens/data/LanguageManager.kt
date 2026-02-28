package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.data

import com.russhwolf.settings.Settings

enum class Language(val code: String, val displayName: String, val isRTL: Boolean) {
    ENGLISH("en", "English", isRTL = false),
    ARABIC("ar", "العربية", isRTL = true),
    KURDISH_SORANI("ku", "کوردی (سۆرانی)", isRTL = true),
    KURDISH_BADINI("ckb", "کوردی (بادینی)", isRTL = true);

    companion object {
        fun fromCode(code: String): Language {
            return values().find { it.code == code } ?: ENGLISH
        }
    }
}

class LanguageManager(private val settings: Settings) {
    companion object {
        private const val KEY_LANGUAGE = "app_language"
    }

    fun getCurrentLanguage(): Language {
        val code = settings.getString(KEY_LANGUAGE, Language.ENGLISH.code)
        return Language.fromCode(code)
    }

    fun setLanguage(language: Language) {
        settings.putString(KEY_LANGUAGE, language.code)
    }
}