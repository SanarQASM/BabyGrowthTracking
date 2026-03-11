package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.data

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect

/**
 * Web (shared jsMain + wasmJsMain) implementation of createWebCurrentLocale.
 *
 * Both `LocaleProvider.js.kt` and `LocaleProvider.wasmJs.kt` delegate here.
 *
 * FIX: Previously this function just returned `language.code` and did nothing
 *      else — so changing language in Settings had zero effect on the browser.
 *
 * WHAT THIS NOW DOES:
 *   1. Sets `document.documentElement.lang` to the BCP-47 language tag.
 *      This is the standard HTML attribute browsers, screen-readers, and
 *      CSS `:lang()` selectors use to know what language the page is in.
 *
 *   2. Sets `document.documentElement.dir` to "rtl" or "ltr".
 *      This makes the browser apply correct BiDi rendering for Arabic and
 *      Kurdish without needing to set it on every individual element.
 *
 * WHY THIS IS ENOUGH FOR COMPOSE:
 *   Compose Multiplatform on Web resolves string resources at composition
 *   time from the compiled resource bundle — it does NOT re-read from the
 *   browser's Accept-Language header at runtime. The actual re-composition
 *   that picks up the new strings is driven by `key(language)` inside
 *   `LanguageProvider` (commonMain/LocaleProvider.kt). These DOM attribute
 *   changes are the correct web-platform signal that the language has changed.
 */
@Composable
fun createWebCurrentLocale(language: Language): String {
    SideEffect {
        // Map our Language enum to BCP-47 tags the browser understands
        val bcp47Tag = when (language) {
            Language.ENGLISH        -> "en"
            Language.ARABIC         -> "ar"
            Language.KURDISH_SORANI -> "ku"       // ISO 639-1 / BCP-47 for Sorani
            Language.KURDISH_BADINI -> "ckb"      // ISO 639-3 for Central Kurdish (Badini)
        }

        val dir = if (language.isRTL) "rtl" else "ltr"

        // Update the root <html> element
        setDocumentLanguage(bcp47Tag, dir)
    }
    return language.code
}

/**
 * Platform bridge — implemented separately in jsMain and wasmJsMain
 * because the DOM interop API differs between the two targets.
 */
expect fun setDocumentLanguage(langTag: String, dir: String)