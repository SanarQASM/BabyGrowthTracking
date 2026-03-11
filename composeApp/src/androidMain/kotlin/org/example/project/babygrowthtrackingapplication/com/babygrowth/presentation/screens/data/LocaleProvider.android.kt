package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.data

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.os.LocaleListCompat
import java.util.Locale

/**
 * Android implementation of rememberCurrentLocale.
 *
 * FIX: Two-phase locale update so language changes take effect IMMEDIATELY
 *      inside the running Compose tree (no Activity restart required).
 *
 * PHASE 1 — Update the running Resources/Configuration:
 *   `context.resources.updateConfiguration()` rewrites the Resources object
 *   that is currently live in this Activity. After this call, any new call
 *   to `stringResource()` (including those triggered by the `key(language)`
 *   rebuild in LanguageProvider) resolves strings from the new locale.
 *   Without this step, strings keep showing the old language until the next
 *   Activity recreation even if the composable tree is rebuilt.
 *
 * PHASE 2 — Tell AppCompat for future Activity restarts:
 *   `AppCompatDelegate.setApplicationLocales()` persists the choice across
 *   process restarts and Activity recreations. This is the official Android
 *   per-app language API (Android 13+ natively, AppCompat backport for older).
 *   It does NOT affect the currently running Resources, which is why Phase 1
 *   is also needed.
 */
@SuppressLint("LocalContextConfigurationRead", "LocalContextResourcesRead")
@Composable
actual fun rememberCurrentLocale(language: Language): String {
    val context = LocalContext.current

    SideEffect {
        val locale = when (language) {
            Language.ENGLISH        -> Locale.ENGLISH
            Language.ARABIC         -> Locale("ar")
            Language.KURDISH_SORANI -> Locale("ku")
            Language.KURDISH_BADINI -> Locale("ckb")
        }

        // Phase 1: update the live Resources object so stringResource() re-resolves NOW
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        @Suppress("DEPRECATION")
        context.resources.updateConfiguration(config, context.resources.displayMetrics)

        // Phase 2: persist for future Activity restarts via AppCompat
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(language.code)
        )
    }

    return language.code
}