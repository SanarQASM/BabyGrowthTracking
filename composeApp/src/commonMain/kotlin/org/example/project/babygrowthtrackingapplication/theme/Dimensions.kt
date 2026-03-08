package org.example.project.babygrowthtrackingapplication.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Responsive dimensions that adapt based on screen size.
 * Updated with iOS-style reduced corner radius.
 *
 * REFACTORED — NEW TOKENS ADDED:
 *  ┌──────────────────────────────────────────────────────────────────────────────┐
 *  │  Token                      │ Replaces                                       │
 *  ├──────────────────────────────────────────────────────────────────────────────┤
 *  │  borderWidthThin            │  1.dp scattered in border() calls              │
 *  │  borderWidthMedium          │  2.dp scattered in border() calls              │
 *  │  profileInfoRowVertPad      │  vertical = 5.dp in ProfileInfoRow             │
 *  │  profileSectionLabelStart   │  start = 4.dp in ProfileSectionCard label      │
 *  │  babyCardAvatarSize         │  52.dp hardcoded in BabyProfileTabContent      │
 *  │  babyCardEmojiSize          │  26.sp hardcoded in BabyProfileTabContent      │
 *  │  profileInfoIconWidth       │  28.dp for icon column width                   │
 *  │  chartCardCornerRadius      │  18.dp in BabyProfileTabContent MeasureCard    │
 *  │  addButtonSize              │  36.dp for the "+" box in BabyProfileTabContent│
 *  └──────────────────────────────────────────────────────────────────────────────┘
 */
data class Dimensions(
    // ── Splash Screen ─────────────────────────────────────────────────────────
    val cornerImageSize: Dp,
    val logoSize: Dp,
    val screenPadding: Dp,
    val logoPadding: Dp,

    // ── Common spacing ────────────────────────────────────────────────────────
    val spacingXSmall: Dp,
    val spacingSmall: Dp,
    val spacingMedium: Dp,
    val spacingLarge: Dp,
    val spacingXLarge: Dp,
    val spacingXXLarge: Dp,

    // ── Icon sizes ────────────────────────────────────────────────────────────
    val iconSmall: Dp,
    val iconMedium: Dp,
    val iconLarge: Dp,
    val iconXLarge: Dp,

    // ── Button sizes (iOS-style with reduced corner radius) ───────────────────
    val buttonHeight: Dp,
    val buttonCornerRadius: Dp,

    // ── Card sizes ────────────────────────────────────────────────────────────
    val cardElevation: Dp,
    val cardCornerRadius: Dp,

    // ── Image sizes ───────────────────────────────────────────────────────────
    val avatarSmall: Dp,
    val avatarMedium: Dp,
    val avatarLarge: Dp,

    // ── Content max width ─────────────────────────────────────────────────────
    val maxContentWidth: Dp,

    // ── ★ NEW: Border widths ──────────────────────────────────────────────────
    // WAS: 1.dp / 2.dp used ad-hoc in border() calls throughout screens
    val borderWidthThin: Dp,
    val borderWidthMedium: Dp,

    // ── ★ NEW: Profile screen specifics ──────────────────────────────────────
    // WAS: vertical = 5.dp in ProfileInfoRow padding
    val profileInfoRowVerticalPadding: Dp,
    // WAS: start = 4.dp in ProfileSectionCard label padding
    val profileSectionLabelStartPadding: Dp,
    // WAS: width = 28.dp for icon column in ProfileInfoRow
    val profileInfoIconWidth: Dp,

    // ── ★ NEW: Baby card / chart specifics ───────────────────────────────────
    // WAS: 52.dp hardcoded for avatar circle in BabyCard
    val babyCardAvatarSize: Dp,
    // WAS: 36.dp hardcoded for the add-measurement button in BabyProfileTabContent
    val addButtonSize: Dp,
    // WAS: 18.dp hardcoded as card corner in BabyProfileTabContent MeasureCard
    val chartCardCornerRadius: Dp,
    // WAS: spacing.spacingSmall + 2.dp  →  now a named token
    val chipCornerRadius: Dp,
) {
    companion object {

        @Composable
        @ReadOnlyComposable
        fun create(windowSizeClass: WindowSizeClass): Dimensions {
            return when (windowSizeClass) {
                WindowSizeClass.COMPACT  -> compactDimensions()
                WindowSizeClass.MEDIUM   -> mediumDimensions()
                WindowSizeClass.EXPANDED -> expandedDimensions()
            }
        }

        // ── COMPACT — Phone portrait (< 600dp) ─────────────────────────────────
        private fun compactDimensions() = Dimensions(
            cornerImageSize  = 120.dp,
            logoSize         = 320.dp,
            screenPadding    = 16.dp,
            logoPadding      = 8.dp,

            spacingXSmall    = 4.dp,
            spacingSmall     = 8.dp,
            spacingMedium    = 16.dp,
            spacingLarge     = 24.dp,
            spacingXLarge    = 32.dp,
            spacingXXLarge   = 40.dp,

            iconSmall        = 16.dp,
            iconMedium       = 24.dp,
            iconLarge        = 32.dp,
            iconXLarge       = 48.dp,

            buttonHeight       = 50.dp,
            buttonCornerRadius = 16.dp,

            cardElevation      = 4.dp,
            cardCornerRadius   = 16.dp,

            avatarSmall  = 32.dp,
            avatarMedium = 48.dp,
            avatarLarge  = 64.dp,

            maxContentWidth = Dp.Unspecified,

            // ★ NEW tokens — compact
            borderWidthThin  = 1.dp,
            borderWidthMedium = 2.dp,

            profileInfoRowVerticalPadding   = 5.dp,
            profileSectionLabelStartPadding = 4.dp,
            profileInfoIconWidth            = 28.dp,

            babyCardAvatarSize  = 52.dp,
            addButtonSize       = 36.dp,
            chartCardCornerRadius = 18.dp,
            chipCornerRadius    = 10.dp,
        )

        // ── MEDIUM — Tablet portrait / phone landscape (600–840dp) ─────────────
        private fun mediumDimensions() = Dimensions(
            cornerImageSize  = 160.dp,
            logoSize         = 400.dp,
            screenPadding    = 24.dp,
            logoPadding      = 12.dp,

            spacingXSmall    = 6.dp,
            spacingSmall     = 12.dp,
            spacingMedium    = 20.dp,
            spacingLarge     = 28.dp,
            spacingXLarge    = 40.dp,
            spacingXXLarge   = 56.dp,

            iconSmall        = 20.dp,
            iconMedium       = 28.dp,
            iconLarge        = 36.dp,
            iconXLarge       = 56.dp,

            buttonHeight       = 54.dp,
            buttonCornerRadius = 18.dp,

            cardElevation      = 6.dp,
            cardCornerRadius   = 18.dp,

            avatarSmall  = 40.dp,
            avatarMedium = 60.dp,
            avatarLarge  = 80.dp,

            maxContentWidth = 700.dp,

            // ★ NEW tokens — medium
            borderWidthThin   = 1.dp,
            borderWidthMedium = 2.dp,

            profileInfoRowVerticalPadding   = 6.dp,
            profileSectionLabelStartPadding = 4.dp,
            profileInfoIconWidth            = 32.dp,

            babyCardAvatarSize  = 60.dp,
            addButtonSize       = 40.dp,
            chartCardCornerRadius = 20.dp,
            chipCornerRadius    = 12.dp,
        )

        // ── EXPANDED — Tablet landscape / desktop (> 840dp) ────────────────────
        private fun expandedDimensions() = Dimensions(
            cornerImageSize  = 200.dp,
            logoSize         = 480.dp,
            screenPadding    = 32.dp,
            logoPadding      = 16.dp,

            spacingXSmall    = 8.dp,
            spacingSmall     = 16.dp,
            spacingMedium    = 24.dp,
            spacingLarge     = 32.dp,
            spacingXLarge    = 48.dp,
            spacingXXLarge   = 64.dp,

            iconSmall        = 24.dp,
            iconMedium       = 32.dp,
            iconLarge        = 40.dp,
            iconXLarge       = 64.dp,

            buttonHeight       = 56.dp,
            buttonCornerRadius = 20.dp,

            cardElevation      = 8.dp,
            cardCornerRadius   = 20.dp,

            avatarSmall  = 48.dp,
            avatarMedium = 72.dp,
            avatarLarge  = 96.dp,

            maxContentWidth = 600.dp,

            // ★ NEW tokens — expanded
            borderWidthThin   = 1.dp,
            borderWidthMedium = 2.dp,

            profileInfoRowVerticalPadding   = 8.dp,
            profileSectionLabelStartPadding = 6.dp,
            profileInfoIconWidth            = 36.dp,

            babyCardAvatarSize  = 72.dp,
            addButtonSize       = 44.dp,
            chartCardCornerRadius = 22.dp,
            chipCornerRadius    = 14.dp,
        )
    }
}

val LocalDimensions = staticCompositionLocalOf<Dimensions> {
    error("No Dimensions provided")
}

val LocalScreenInfo = staticCompositionLocalOf<ScreenInfo> {
    ScreenInfo(
        widthDp          = 0.dp,
        heightDp         = 0.dp,
        windowSizeClass  = WindowSizeClass.COMPACT
    )
}