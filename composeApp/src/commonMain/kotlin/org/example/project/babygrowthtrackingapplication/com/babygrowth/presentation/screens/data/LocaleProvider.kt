package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.data

import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection

@Composable
expect fun rememberCurrentLocale(language: Language): String

val LocalAppLanguage = compositionLocalOf { Language.ENGLISH }

/**
 * Simple boolean so any composable can know "am I in an RTL language?"
 * without needing to check LocalLayoutDirection.
 */
val LocalIsRTL = compositionLocalOf { false }

/**
 * LanguageProvider — sets the active language AND layout direction for the
 * entire app subtree.
 *
 * HOW RTL/LTR WORKS:
 * `LocalLayoutDirection` is provided globally here so that ALL Compose
 * layout primitives (Row, padding, Arrangement.Start/End, etc.) automatically
 * respect the reading direction:
 *   • Kurdish / Arabic  → LayoutDirection.Rtl  → UI flows right-to-left
 *   • English           → LayoutDirection.Ltr  → UI flows left-to-right
 *
 * This is the standard Compose Multiplatform approach for full RTL support.
 * Rows reverse their children order, Start/End padding swaps sides, and
 * text aligns correctly — exactly what RTL users expect.
 *
 * WHAT ABOUT ICONS FLIPPING?
 * Only icons using `Icons.AutoMirrored.*` will mirror in RTL — that is
 * intentional (e.g. ArrowBack becomes ArrowForward in RTL navigation).
 * Regular icons (Home, Settings, Close, etc.) do NOT mirror because they
 * are not in the AutoMirrored set.
 *
 * WHY `key(language)` IS STILL NEEDED:
 * `rememberCurrentLocale()` calls platform APIs to change the locale, but
 * `stringResource()` resolves strings at composition time. `key(language)`
 * forces a full subtree rebuild so every `stringResource()` re-executes
 * with the new locale — giving instant string updates without a restart.
 */
@Composable
fun LanguageProvider(
    language: Language,
    content : @Composable () -> Unit
) {
    // Push the locale to the platform (Android / iOS / Desktop / Web)
    rememberCurrentLocale(language)

    // key(language) forces a full subtree rebuild on language change,
    // making stringResource() pick up the new locale immediately.
    key(language) {
        val layoutDirection = if (language.isRTL) LayoutDirection.Rtl else LayoutDirection.Ltr

        CompositionLocalProvider(
            LocalAppLanguage     provides language,
            LocalIsRTL           provides language.isRTL,
            LocalLayoutDirection provides layoutDirection,  // ← applies RTL/LTR to whole UI
        ) {
            content()
        }
    }
}

// ─── RTL helpers (kept for backwards compatibility) ───────────────────────────
// These are no longer required for layout direction — the global
// LocalLayoutDirection handles that. They remain useful if you ever need to
// selectively OVERRIDE direction for a specific subtree (e.g. a code block
// that must always be LTR inside an RTL screen).

/**
 * Wraps a subtree with an explicit layout direction based on the current
 * language. Useful if you need to override a specific section back to a
 * fixed direction.
 *
 * Example — force a code snippet to always be LTR inside an RTL screen:
 *   CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
 *       CodeBlock(...)
 *   }
 */
@Composable
fun RTLAwareContent(content: @Composable () -> Unit) {
    val isRTL     = LocalIsRTL.current
    val direction = if (isRTL) LayoutDirection.Rtl else LayoutDirection.Ltr
    CompositionLocalProvider(LocalLayoutDirection provides direction) {
        content()
    }
}

/**
 * Returns the correct text alignment for the current language's reading direction.
 * Useful for Text composables that need explicit TextAlign (e.g. multiline body text).
 *
 * Example:
 *   Text(
 *       text      = label,
 *       textAlign = readingTextAlign()
 *   )
 */
@Composable
fun readingTextAlign(): TextAlign {
    val isRTL = LocalIsRTL.current
    return if (isRTL) TextAlign.Right else TextAlign.Left
}

/**
 * Returns the correct "back" arrow icon for the current language.
 *   LTR → ← ArrowBack
 *   RTL → → ArrowForward  (right arrow = "go back" in RTL navigation)
 *
 * Example:
 *   Icon(imageVector = backArrowIcon(), contentDescription = "Back")
 */
@Composable
fun backArrowIcon(): androidx.compose.ui.graphics.vector.ImageVector {
    val isRTL = LocalIsRTL.current
    return if (isRTL) {
        androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowForward
    } else {
        androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack
    }
}