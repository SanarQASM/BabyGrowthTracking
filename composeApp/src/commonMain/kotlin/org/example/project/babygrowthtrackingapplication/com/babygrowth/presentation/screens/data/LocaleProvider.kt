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
 * without triggering a layout flip.
 */
val LocalIsRTL = compositionLocalOf { false }

/**
 * LanguageProvider — sets the active language for the entire app subtree.
 *
 * FIX: `key(language)` added around the CompositionLocalProvider block.
 *
 * WHY THIS FIX IS NEEDED:
 * `rememberCurrentLocale()` calls platform APIs (AppCompatDelegate on
 * Android, NSUserDefaults on iOS, Locale.setDefault on Desktop) to change
 * the locale. However, Compose's `stringResource()` is backed by the
 * `Resources` object that was resolved at composition time — it does NOT
 * automatically re-read after a SideEffect fires. Without `key(language)`,
 * every Text/Button/label that uses `stringResource()` keeps displaying
 * the OLD language until the whole Activity is recreated.
 *
 * `key(language)` tells Compose: "whenever `language` changes, throw away
 * the entire subtree and build it fresh." This forces every `stringResource()`
 * call inside the tree to re-execute against the newly-set locale, so
 * all UI text updates instantly without an app restart.
 */
@Composable
fun LanguageProvider(
    language: Language,
    content : @Composable () -> Unit
) {
    // Push the locale to the platform (Android / iOS / Desktop / Web)
    rememberCurrentLocale(language)

    // FIX: key(language) forces a full subtree rebuild when the language changes,
    //      which makes stringResource() pick up the new locale immediately.
    key(language) {
        // ✅ Do NOT provide LocalLayoutDirection here globally.
        //
        //    THE ROOT CAUSE OF "EVERYTHING FLIPS":
        //    Setting `LocalLayoutDirection provides LayoutDirection.Rtl` globally
        //    means EVERY composable in the entire app tree inherits RTL, because
        //    LocalLayoutDirection is consumed by:
        //      - Row        → reverses children order
        //      - Padding    → swaps left/right
        //      - Icon       → mirrors horizontally
        //      - Image      → mirrors horizontally
        //      - Button     → mirrors content
        //      - Card       → mirrors content
        //      - Arrangement.Start/End → flips sides
        //    ...basically every single layout primitive in Compose.
        //
        //    The fix: expose only LocalIsRTL (a plain Boolean).
        //    Individual composables that NEED RTL behaviour opt-in explicitly.
        CompositionLocalProvider(
            LocalAppLanguage provides language,
            LocalIsRTL       provides language.isRTL
        ) {
            content()
        }
    }
}

// ─── Opt-in RTL helpers ────────────────────────────────────────────────────────
// Use these ONLY on the specific composables that need RTL-aware behaviour.
// Never wrap an entire screen or the app root with RTLAwareContent.

/**
 * Applies RTL layout direction to a specific subtree ONLY.
 * Use this on individual text blocks or list containers where Arabic/Kurdish
 * text should flow right-to-left.
 *
 * Example — a paragraph of Arabic body text:
 *   RTLAwareContent {
 *       Text(text = arabicParagraph)
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