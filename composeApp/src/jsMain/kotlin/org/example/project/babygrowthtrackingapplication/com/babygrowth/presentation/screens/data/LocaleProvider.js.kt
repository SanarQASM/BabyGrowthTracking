package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.data

import androidx.compose.runtime.Composable
import kotlinx.browser.document

/**
 * JS target implementation of rememberCurrentLocale.
 *
 * Delegates to `createWebCurrentLocale()` (defined in webMain) which sets
 * `document.documentElement.lang` and `dir` via `setDocumentLanguage()`.
 *
 * The `key(language)` block in LanguageProvider (commonMain) then forces
 * a full Compose subtree rebuild so all `stringResource()` calls re-execute.
 */
@Composable
actual fun rememberCurrentLocale(language: Language): String {
    return createWebCurrentLocale(language)
}

/**
 * JS-target actual for the DOM bridge declared in webMain.
 * Uses `kotlinx.browser.document` which is available in jsMain.
 */
actual fun setDocumentLanguage(langTag: String, dir: String) {
    document.documentElement?.apply {
        setAttribute("lang", langTag)
        setAttribute("dir", dir)
    }
}