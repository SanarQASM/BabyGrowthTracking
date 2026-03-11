package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.data

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import platform.Foundation.NSUserDefaults

/**
 * iOS implementation of rememberCurrentLocale.
 *
 * Sets AppleLanguages in NSUserDefaults so that the system picks the correct
 * .lproj resource folder when Compose re-reads string resources.
 *
 * The `key(language)` block in LanguageProvider (commonMain) forces a full
 * subtree rebuild whenever the language changes, which causes every
 * `stringResource()` in the tree to re-execute — at which point the new
 * AppleLanguages setting is already in effect.
 *
 * NOTE: On iOS, full string-resource switching without an app restart is
 * best-effort because UIKit caches the main bundle's language at launch.
 * For a fully seamless experience, consider restarting the Compose root
 * or using a custom resource-loading mechanism that respects AppleLanguages
 * at runtime.
 */
@Composable
actual fun rememberCurrentLocale(language: Language): String {
    SideEffect {
        NSUserDefaults.standardUserDefaults.apply {
            setObject(listOf(language.code), forKey = "AppleLanguages")
            synchronize()
        }
    }
    return language.code
}