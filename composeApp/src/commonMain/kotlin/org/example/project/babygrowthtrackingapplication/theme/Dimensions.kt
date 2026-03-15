package org.example.project.babygrowthtrackingapplication.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Responsive dimension tokens for the Baby Growth Tracking app.
 *
 * Tokens are resolved in [Dimensions.create] based on [WindowSizeClass] and
 * the new [isLandscape] flag so that every layout detail adapts to both
 * portrait and landscape across all platforms.
 *
 * LANDSCAPE RULES applied here:
 *  • navBarHeight = 0.dp   (hidden — replaced by SideNavigationRail)
 *  • logoSize     reduced  (less vertical space available)
 *  • spacingXXLarge reduced (phone landscape is height-constrained)
 *  • maxContentWidth = Dp.Unspecified on phone landscape (full width)
 */
data class Dimensions(
    // ── Branding / logo ──────────────────────────────────────────────────────
    val cornerImageSize : Dp,
    val logoSize        : Dp,
    val screenPadding   : Dp,
    val logoPadding     : Dp,

    // ── Spacing scale ─────────────────────────────────────────────────────────
    val spacingXSmall  : Dp,
    val spacingSmall   : Dp,
    val spacingMedium  : Dp,
    val spacingLarge   : Dp,
    val spacingXLarge  : Dp,
    val spacingXXLarge : Dp,

    // ── Icon sizes ────────────────────────────────────────────────────────────
    val iconSmall  : Dp,
    val iconMedium : Dp,
    val iconLarge  : Dp,
    val iconXLarge : Dp,

    // ── Buttons ───────────────────────────────────────────────────────────────
    val buttonHeight       : Dp,
    val buttonCornerRadius : Dp,

    // ── Cards ─────────────────────────────────────────────────────────────────
    val cardElevation    : Dp,
    val cardCornerRadius : Dp,

    // ── Avatars ───────────────────────────────────────────────────────────────
    val avatarSmall  : Dp,
    val avatarMedium : Dp,
    val avatarLarge  : Dp,

    // ── Layout ────────────────────────────────────────────────────────────────
    val maxContentWidth : Dp,

    // ── Borders ───────────────────────────────────────────────────────────────
    val borderWidthThin   : Dp,
    val borderWidthMedium : Dp,

    // ── Profile ───────────────────────────────────────────────────────────────
    val profileInfoRowVerticalPadding  : Dp,
    val profileSectionLabelStartPadding: Dp,
    val profileInfoIconWidth           : Dp,
    val profileInfoIconFontSize        : TextUnit,
    val profileSectionCardVertPad      : Dp,
    val profileArchivedCorner          : Dp,
    val profileArchivedPaddingH        : Dp,
    val profileArchivedPaddingV        : Dp,
    val profileQuickActionEmojiSize    : TextUnit,
    val profileQuickActionLabelSize    : TextUnit,
    val profileQuickActionLineHeight   : TextUnit,
    val profileQuickActionItemGap      : Dp,

    // ── Tonal elevation ───────────────────────────────────────────────────────
    val cardTonalElevation6 : Dp,

    // ── Baby card ─────────────────────────────────────────────────────────────
    val babyCardAvatarSize         : Dp,
    val babyCardEmojiSize          : TextUnit,
    val babyCardInnerPadding       : Dp,
    val babyCardAvatarNameGap      : Dp,
    val babyCardNameBadgeGap       : Dp,
    val babyCardInlineBadgeCorner  : Dp,
    val babyCardInlineBadgePaddingH: Dp,
    val babyCardInlineBadgePaddingV: Dp,
    val babyCardSpacerAfterAvatar  : Dp,
    val babyCardGenderSpacerW      : Dp,
    val babyCardStatSpacer         : Dp,
    val babyCardBottomSpacer       : Dp,

    // ── BabyProfileTabContent misc ────────────────────────────────────────────
    val addButtonSize        : Dp,
    val chartCardCornerRadius: Dp,
    val chipCornerRadius     : Dp,
    val filterTabCorner      : Dp,
    val noBabiesEmojiSize    : TextUnit,

    // ── Small text ────────────────────────────────────────────────────────────
    val homeSmallTextSize  : TextUnit,
    val homeSmallLineHeight: TextUnit,

    // ── Gender banner ─────────────────────────────────────────────────────────
    val starSizeLarge           : TextUnit,
    val starSizeSmall           : TextUnit,
    val bannerMoonEmojiSize     : TextUnit,
    val bannerLabelLetterSpacing: TextUnit,

    // ── Bottom navigation bar ─────────────────────────────────────────────────
    // navBarHeight = 0.dp when in landscape (bar is hidden; rail is used).
    val navBarHeight     : Dp,
    val navBarPillSize   : Dp,
    val navBarPillCorner : Dp,
    val navBarLabelSize  : TextUnit,
    val navBarPaddingH   : Dp,
    val navBarPaddingV   : Dp,
    val navButtonPadding : Dp,
    val navIconLabelGap  : Dp,

    // ── Side navigation rail (landscape) ─────────────────────────────────────
    val railWidth        : Dp,   // width of the NavigationRail in landscape
    val railItemSize     : Dp,   // clickable item height in the rail
    val railIconSize     : Dp,   // icon size inside the rail item

    // ── BenchDetailScreen ─────────────────────────────────────────────────────
    val detailRowVertPadding  : Dp,
    val detailIconSize        : Dp,
    val detailIconTopPadding  : Dp,
    val benchDistanceIconSize : Dp,
) {
    companion object {

        @Composable
        @ReadOnlyComposable
        fun create(
            windowSizeClass: WindowSizeClass,
            isLandscape    : Boolean = false,
        ): Dimensions = when (windowSizeClass) {
            WindowSizeClass.COMPACT  -> compactDimensions(isLandscape)
            WindowSizeClass.MEDIUM   -> mediumDimensions(isLandscape)
            WindowSizeClass.EXPANDED -> expandedDimensions(isLandscape)
        }

        // ─────────────────────────────────────────────────────────────────────
        // COMPACT — phone portrait (< 600dp)
        // In landscape the same compact base is used but several height-bound
        // tokens are reduced and the bottom nav height is zeroed out.
        // ─────────────────────────────────────────────────────────────────────
        private fun compactDimensions(landscape: Boolean) = Dimensions(
            cornerImageSize = 120.dp,
            logoSize        = if (landscape) 160.dp else 320.dp,
            screenPadding   = 16.dp,
            logoPadding     = if (landscape) 4.dp  else 8.dp,

            spacingXSmall  = 4.dp,
            spacingSmall   = 8.dp,
            spacingMedium  = 16.dp,
            spacingLarge   = 24.dp,
            spacingXLarge  = 32.dp,
            spacingXXLarge = if (landscape) 24.dp else 40.dp,

            iconSmall  = 16.dp,
            iconMedium = 24.dp,
            iconLarge  = 32.dp,
            iconXLarge = 48.dp,

            buttonHeight       = 50.dp,
            buttonCornerRadius = 16.dp,

            cardElevation    = 4.dp,
            cardCornerRadius = 16.dp,

            avatarSmall  = 32.dp,
            avatarMedium = 48.dp,
            avatarLarge  = 64.dp,

            maxContentWidth = Dp.Unspecified,

            borderWidthThin   = 1.dp,
            borderWidthMedium = 2.dp,

            profileInfoRowVerticalPadding   = 5.dp,
            profileSectionLabelStartPadding = 4.dp,
            profileInfoIconWidth            = 28.dp,
            profileInfoIconFontSize         = 15.sp,
            profileSectionCardVertPad       = 14.dp,
            profileArchivedCorner           = 8.dp,
            profileArchivedPaddingH         = 12.dp,
            profileArchivedPaddingV         = 4.dp,
            profileQuickActionEmojiSize     = 20.sp,
            profileQuickActionLabelSize     = 9.sp,
            profileQuickActionLineHeight    = 11.sp,
            profileQuickActionItemGap       = 4.dp,

            cardTonalElevation6 = 6.dp,

            babyCardAvatarSize          = 52.dp,
            babyCardEmojiSize           = 26.sp,
            babyCardInnerPadding        = 16.dp,
            babyCardAvatarNameGap       = 12.dp,
            babyCardNameBadgeGap        = 8.dp,
            babyCardInlineBadgeCorner   = 6.dp,
            babyCardInlineBadgePaddingH = 6.dp,
            babyCardInlineBadgePaddingV = 2.dp,
            babyCardSpacerAfterAvatar   = 10.dp,
            babyCardGenderSpacerW       = 6.dp,
            babyCardStatSpacer          = 3.dp,
            babyCardBottomSpacer        = 14.dp,

            addButtonSize         = 36.dp,
            chartCardCornerRadius = 18.dp,
            chipCornerRadius      = 10.dp,
            filterTabCorner       = 20.dp,
            noBabiesEmojiSize     = 64.sp,

            homeSmallTextSize   = 9.sp,
            homeSmallLineHeight = 11.sp,

            starSizeLarge            = 16.sp,
            starSizeSmall            = 12.sp,
            bannerMoonEmojiSize      = 42.sp,
            bannerLabelLetterSpacing = 1.5.sp,

            // Bottom nav: 0.dp in landscape — bar is hidden, rail takes over
            navBarHeight     = if (landscape) 0.dp else 64.dp,
            navBarPillSize   = 40.dp,
            navBarPillCorner = 20.dp,
            navBarLabelSize  = 10.sp,
            navBarPaddingH   = 4.dp,
            navBarPaddingV   = 8.dp,
            navButtonPadding = 8.dp,
            navIconLabelGap  = 2.dp,

            // Rail dimensions (only relevant in landscape)
            railWidth    = 72.dp,
            railItemSize = 56.dp,
            railIconSize = 24.dp,

            detailRowVertPadding  = 4.dp,
            detailIconSize        = 18.dp,
            detailIconTopPadding  = 2.dp,
            benchDistanceIconSize = 16.dp,
        )

        // ─────────────────────────────────────────────────────────────────────
        // MEDIUM — tablet portrait / phone landscape (600dp–840dp)
        // ─────────────────────────────────────────────────────────────────────
        private fun mediumDimensions(landscape: Boolean) = Dimensions(
            cornerImageSize = 160.dp,
            logoSize        = if (landscape) 200.dp else 400.dp,
            screenPadding   = 24.dp,
            logoPadding     = if (landscape) 6.dp  else 12.dp,

            spacingXSmall  = 6.dp,
            spacingSmall   = 12.dp,
            spacingMedium  = 20.dp,
            spacingLarge   = 28.dp,
            spacingXLarge  = 40.dp,
            spacingXXLarge = if (landscape) 32.dp else 48.dp,

            iconSmall  = 20.dp,
            iconMedium = 28.dp,
            iconLarge  = 36.dp,
            iconXLarge = 56.dp,

            buttonHeight       = 52.dp,
            buttonCornerRadius = 18.dp,

            cardElevation    = 6.dp,
            cardCornerRadius = 18.dp,

            avatarSmall  = 40.dp,
            avatarMedium = 60.dp,
            avatarLarge  = 80.dp,

            maxContentWidth = Dp.Unspecified,

            borderWidthThin   = 1.dp,
            borderWidthMedium = 2.dp,

            profileInfoRowVerticalPadding   = 6.dp,
            profileSectionLabelStartPadding = 5.dp,
            profileInfoIconWidth            = 32.dp,
            profileInfoIconFontSize         = 16.sp,
            profileSectionCardVertPad       = 18.dp,
            profileArchivedCorner           = 10.dp,
            profileArchivedPaddingH         = 14.dp,
            profileArchivedPaddingV         = 5.dp,
            profileQuickActionEmojiSize     = 22.sp,
            profileQuickActionLabelSize     = 10.sp,
            profileQuickActionLineHeight    = 12.sp,
            profileQuickActionItemGap       = 5.dp,

            cardTonalElevation6 = 6.dp,

            babyCardAvatarSize          = 62.dp,
            babyCardEmojiSize           = 30.sp,
            babyCardInnerPadding        = 20.dp,
            babyCardAvatarNameGap       = 14.dp,
            babyCardNameBadgeGap        = 10.dp,
            babyCardInlineBadgeCorner   = 8.dp,
            babyCardInlineBadgePaddingH = 8.dp,
            babyCardInlineBadgePaddingV = 3.dp,
            babyCardSpacerAfterAvatar   = 12.dp,
            babyCardGenderSpacerW       = 8.dp,
            babyCardStatSpacer          = 4.dp,
            babyCardBottomSpacer        = 16.dp,

            addButtonSize         = 40.dp,
            chartCardCornerRadius = 20.dp,
            chipCornerRadius      = 12.dp,
            filterTabCorner       = 22.dp,
            noBabiesEmojiSize     = 72.sp,

            homeSmallTextSize   = 10.sp,
            homeSmallLineHeight = 12.sp,

            starSizeLarge            = 18.sp,
            starSizeSmall            = 14.sp,
            bannerMoonEmojiSize      = 50.sp,
            bannerLabelLetterSpacing = 1.5.sp,

            navBarHeight     = if (landscape) 0.dp else 72.dp,
            navBarPillSize   = 48.dp,
            navBarPillCorner = 24.dp,
            navBarLabelSize  = 11.sp,
            navBarPaddingH   = 8.dp,
            navBarPaddingV   = 10.dp,
            navButtonPadding = 10.dp,
            navIconLabelGap  = 3.dp,

            railWidth    = 80.dp,
            railItemSize = 60.dp,
            railIconSize = 28.dp,

            detailRowVertPadding  = 5.dp,
            detailIconSize        = 20.dp,
            detailIconTopPadding  = 2.dp,
            benchDistanceIconSize = 18.dp,
        )

        // ─────────────────────────────────────────────────────────────────────
        // EXPANDED — tablet landscape / desktop (> 840dp)
        // Always has the rail; landscape flag only affects a few tokens.
        // ─────────────────────────────────────────────────────────────────────
        private fun expandedDimensions(landscape: Boolean) = Dimensions(
            cornerImageSize = 200.dp,
            logoSize        = if (landscape) 280.dp else 480.dp,
            screenPadding   = 32.dp,
            logoPadding     = 16.dp,

            spacingXSmall  = 8.dp,
            spacingSmall   = 16.dp,
            spacingMedium  = 24.dp,
            spacingLarge   = 32.dp,
            spacingXLarge  = 48.dp,
            spacingXXLarge = 64.dp,

            iconSmall  = 24.dp,
            iconMedium = 32.dp,
            iconLarge  = 40.dp,
            iconXLarge = 64.dp,

            buttonHeight       = 56.dp,
            buttonCornerRadius = 20.dp,

            cardElevation    = 8.dp,
            cardCornerRadius = 20.dp,

            avatarSmall  = 48.dp,
            avatarMedium = 72.dp,
            avatarLarge  = 96.dp,

            maxContentWidth = 600.dp,

            borderWidthThin   = 1.dp,
            borderWidthMedium = 2.dp,

            profileInfoRowVerticalPadding   = 8.dp,
            profileSectionLabelStartPadding = 6.dp,
            profileInfoIconWidth            = 36.dp,
            profileInfoIconFontSize         = 18.sp,
            profileSectionCardVertPad       = 22.dp,
            profileArchivedCorner           = 12.dp,
            profileArchivedPaddingH         = 16.dp,
            profileArchivedPaddingV         = 6.dp,
            profileQuickActionEmojiSize     = 24.sp,
            profileQuickActionLabelSize     = 11.sp,
            profileQuickActionLineHeight    = 14.sp,
            profileQuickActionItemGap       = 6.dp,

            cardTonalElevation6 = 6.dp,

            babyCardAvatarSize          = 72.dp,
            babyCardEmojiSize           = 36.sp,
            babyCardInnerPadding        = 24.dp,
            babyCardAvatarNameGap       = 16.dp,
            babyCardNameBadgeGap        = 12.dp,
            babyCardInlineBadgeCorner   = 10.dp,
            babyCardInlineBadgePaddingH = 10.dp,
            babyCardInlineBadgePaddingV = 4.dp,
            babyCardSpacerAfterAvatar   = 14.dp,
            babyCardGenderSpacerW       = 10.dp,
            babyCardStatSpacer          = 5.dp,
            babyCardBottomSpacer        = 18.dp,

            addButtonSize         = 44.dp,
            chartCardCornerRadius = 22.dp,
            chipCornerRadius      = 14.dp,
            filterTabCorner       = 24.dp,
            noBabiesEmojiSize     = 80.sp,

            homeSmallTextSize   = 11.sp,
            homeSmallLineHeight = 13.sp,

            starSizeLarge            = 20.sp,
            starSizeSmall            = 16.sp,
            bannerMoonEmojiSize      = 58.sp,
            bannerLabelLetterSpacing = 1.5.sp,

            // Expanded is always "landscape enough" to use the rail.
            navBarHeight     = 0.dp,
            navBarPillSize   = 56.dp,
            navBarPillCorner = 28.dp,
            navBarLabelSize  = 12.sp,
            navBarPaddingH   = 12.dp,
            navBarPaddingV   = 12.dp,
            navButtonPadding = 12.dp,
            navIconLabelGap  = 4.dp,

            railWidth    = 88.dp,
            railItemSize = 64.dp,
            railIconSize = 28.dp,

            detailRowVertPadding  = 6.dp,
            detailIconSize        = 22.dp,
            detailIconTopPadding  = 3.dp,
            benchDistanceIconSize = 20.dp,
        )
    }
}

val LocalDimensions = staticCompositionLocalOf<Dimensions> {
    error("No Dimensions provided — wrap your composable tree in BabyGrowthTheme")
}

val LocalScreenInfo = staticCompositionLocalOf {
    ScreenInfo(
        widthDp         = 0.dp,
        heightDp        = 0.dp,
        windowSizeClass = WindowSizeClass.COMPACT,
        isLandscape     = false,
    )
}