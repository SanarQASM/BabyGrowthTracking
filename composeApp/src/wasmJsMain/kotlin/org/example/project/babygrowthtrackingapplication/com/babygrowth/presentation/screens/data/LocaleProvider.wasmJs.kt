package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.data

import androidx.compose.runtime.Composable

/**
 * WasmJS target implementation of rememberCurrentLocale.
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
 * WasmJS-target actual for the DOM bridge declared in webMain.
 *
 * Kotlin/Wasm does not have `kotlinx.browser` yet, so we call into JS
 * via a lightweight `@JsFun` extern to set the HTML attributes directly.
 *
 * The extern is a plain JS arrow function — no external library needed.
 */
@JsFun("(langTag, dir) => { document.documentElement.setAttribute('lang', langTag); document.documentElement.setAttribute('dir', dir); }")
private external fun setHtmlLangAndDir(langTag: String, dir: String)

actual fun setDocumentLanguage(langTag: String, dir: String) {
    setHtmlLangAndDir(langTag, dir)
}