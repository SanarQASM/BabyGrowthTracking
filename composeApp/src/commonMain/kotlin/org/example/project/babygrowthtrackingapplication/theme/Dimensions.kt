package org.example.project.babygrowthtrackingapplication.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Responsive dimensions that adapt based on screen size (COMPACT / MEDIUM / EXPANDED).
 *
 * ┌──────────────────────────────────────────────────────────────────────────────┐
 * │  ALL NEW TOKENS (added during theme audit)                                   │
 * ├──────────────────────────────────┬───────────────────────────────────────────┤
 * │  Token                           │  Replaces                                 │
 * ├──────────────────────────────────┼───────────────────────────────────────────┤
 * │  borderWidthThin                 │  1.dp in border() calls                   │
 * │  borderWidthMedium               │  2.dp in border() calls                   │
 * │  profileInfoRowVerticalPadding   │  vertical = 5.dp in ProfileInfoRow         │
 * │  profileSectionLabelStartPadding │  start = 4.dp in ProfileSectionCard label  │
 * │  profileInfoIconWidth            │  28.dp icon column in ProfileInfoRow        │
 * │  profileInfoIconFontSize         │  15.sp emoji in ProfileInfoRow              │
 * │  profileSectionCardVertPad       │  spacingMedium - 2.dp card padding          │
 * │  profileArchivedCorner           │  RoundedCornerShape(8.dp) archived badge    │
 * │  profileArchivedPaddingH         │  horizontal = 12.dp archived badge          │
 * │  profileArchivedPaddingV         │  vertical = 4.dp archived badge             │
 * │  profileQuickActionEmojiSize     │  fontSize = 20.sp quick-action emoji        │
 * │  profileQuickActionLabelSize     │  fontSize = 9.sp quick-action label         │
 * │  profileQuickActionLineHeight    │  lineHeight = 11.sp quick-action label      │
 * │  profileQuickActionItemGap       │  Arrangement.spacedBy(4.dp) quick-action    │
 * │  cardTonalElevation6             │  tonalElevation = 6.dp dialogs/cards        │
 * │  babyCardAvatarSize              │  52.dp avatar in BabyCard                   │
 * │  babyCardEmojiSize               │  26.sp emoji in BabyCard                    │
 * │  babyCardInnerPadding            │  padding(16.dp) BabyCard box                │
 * │  babyCardAvatarNameGap           │  Spacer width = 12.dp                        │
 * │  babyCardNameBadgeGap            │  Spacer width = 8.dp                         │
 * │  babyCardInlineBadgeCorner       │  RoundedCornerShape(6.dp) inline badge       │
 * │  babyCardInlineBadgePaddingH     │  horizontal = 6.dp inline badge              │
 * │  babyCardInlineBadgePaddingV     │  vertical = 2.dp inline badge                │
 * │  babyCardSpacerAfterAvatar       │  Spacer height = 10.dp                       │
 * │  babyCardGenderSpacerW           │  Spacer width = 6.dp gender row              │
 * │  babyCardStatSpacer              │  Spacer height = 3.dp between stats          │
 * │  babyCardBottomSpacer            │  Spacer height = 14.dp before action row     │
 * │  addButtonSize                   │  36.dp "+" box in BabyProfileTabContent      │
 * │  chartCardCornerRadius           │  18.dp card corner in BabyProfileTabContent  │
 * │  chipCornerRadius                │  spacingSmall + 2.dp chip/badge corner       │
 * │  filterTabCorner                 │  RoundedCornerShape(20.dp) filter tabs        │
 * │  noBabiesEmojiSize               │  fontSize = 64.sp no-babies emoji             │
 * │  homeSmallTextSize               │  fontSize = 9.sp small labels                 │
 * │  homeSmallLineHeight             │  lineHeight = 11.sp small labels              │
 * │  starSizeLarge                   │  fontSize = 16.sp HangingStars large          │
 * │  starSizeSmall                   │  fontSize = 12.sp HangingStars small          │
 * │  bannerMoonEmojiSize             │  (iconXLarge.value - 6).sp moon emoji          │
 * │  bannerLabelLetterSpacing        │  letterSpacing = 1.5.sp banner label           │
 * │  navBarHeight                    │  NavigationBar height                          │
 * │  navBarPillSize                  │  selected indicator pill                       │
 * │  navBarPillCorner                │  pill corner radius                            │
 * │  navBarLabelSize                 │  fontSize nav label                            │
 * │  navBarPaddingH                  │  horizontal padding nav bar                    │
 * │  navBarPaddingV                  │  vertical padding nav bar                      │
 * │  navButtonPadding                │  inner padding each nav button                 │
 * │  navIconLabelGap                 │  gap icon→label in nav button                  │
 * │  detailRowVertPadding            │  vertical = 4.dp DetailRow in BenchDetail      │
 * │  detailIconSize                  │  size = 18.dp detail icon                      │
 * │  detailIconTopPadding            │  top = 2.dp detail icon                        │
 * │  benchDistanceIconSize           │  size = 16.dp distance chip icon               │
 * └──────────────────────────────────┴───────────────────────────────────────────┘
 */
data class Dimensions(
    // ── Splash Screen ─────────────────────────────────────────────────────────
    val cornerImageSize : Dp,
    val logoSize        : Dp,
    val screenPadding   : Dp,
    val logoPadding     : Dp,

    // ── Common spacing ────────────────────────────────────────────────────────
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

    // ── Button sizes ──────────────────────────────────────────────────────────
    val buttonHeight       : Dp,
    val buttonCornerRadius : Dp,

    // ── Card sizes ────────────────────────────────────────────────────────────
    val cardElevation    : Dp,
    val cardCornerRadius : Dp,

    // ── Avatar / image sizes ──────────────────────────────────────────────────
    val avatarSmall  : Dp,
    val avatarMedium : Dp,
    val avatarLarge  : Dp,

    // ── Content max width ─────────────────────────────────────────────────────
    val maxContentWidth : Dp,

    // ── ★ Border widths ───────────────────────────────────────────────────────
    val borderWidthThin   : Dp,   // replaces: 1.dp
    val borderWidthMedium : Dp,   // replaces: 2.dp

    // ── ★ Profile screen — InfoRow / SectionCard ──────────────────────────────
    val profileInfoRowVerticalPadding   : Dp,         // replaces: vertical = 5.dp
    val profileSectionLabelStartPadding : Dp,         // replaces: start = 4.dp
    val profileInfoIconWidth            : Dp,         // replaces: width = 28.dp
    val profileInfoIconFontSize         : TextUnit,   // replaces: fontSize = 15.sp
    val profileSectionCardVertPad       : Dp,         // replaces: spacingMedium - 2.dp

    // ── ★ Profile screen — Archived badge ────────────────────────────────────
    val profileArchivedCorner   : Dp,   // replaces: RoundedCornerShape(8.dp)
    val profileArchivedPaddingH : Dp,   // replaces: horizontal = 12.dp
    val profileArchivedPaddingV : Dp,   // replaces: vertical = 4.dp

    // ── ★ Profile screen — Quick-action buttons ───────────────────────────────
    val profileQuickActionEmojiSize  : TextUnit,   // replaces: fontSize = 20.sp
    val profileQuickActionLabelSize  : TextUnit,   // replaces: fontSize = 9.sp
    val profileQuickActionLineHeight : TextUnit,   // replaces: lineHeight = 11.sp
    val profileQuickActionItemGap    : Dp,         // replaces: Arrangement.spacedBy(4.dp)

    // ── ★ Dialogs / tonal elevation ──────────────────────────────────────────
    val cardTonalElevation6 : Dp,   // replaces: tonalElevation = 6.dp

    // ── ★ Baby card specifics ─────────────────────────────────────────────────
    val babyCardAvatarSize          : Dp,         // replaces: 52.dp
    val babyCardEmojiSize           : TextUnit,   // replaces: 26.sp
    val babyCardInnerPadding        : Dp,         // replaces: padding(16.dp)
    val babyCardAvatarNameGap       : Dp,         // replaces: Spacer width 12.dp
    val babyCardNameBadgeGap        : Dp,         // replaces: Spacer width 8.dp
    val babyCardInlineBadgeCorner   : Dp,         // replaces: RoundedCornerShape(6.dp)
    val babyCardInlineBadgePaddingH : Dp,         // replaces: horizontal = 6.dp
    val babyCardInlineBadgePaddingV : Dp,         // replaces: vertical = 2.dp
    val babyCardSpacerAfterAvatar   : Dp,         // replaces: Spacer height 10.dp
    val babyCardGenderSpacerW       : Dp,         // replaces: Spacer width 6.dp
    val babyCardStatSpacer          : Dp,         // replaces: Spacer height 3.dp
    val babyCardBottomSpacer        : Dp,         // replaces: Spacer height 14.dp

    // ── ★ BabyProfileTabContent misc ─────────────────────────────────────────
    val addButtonSize         : Dp,         // replaces: 36.dp add-measurement button
    val chartCardCornerRadius : Dp,         // replaces: 18.dp card corner
    val chipCornerRadius      : Dp,         // replaces: spacingSmall + 2.dp
    val filterTabCorner       : Dp,         // replaces: RoundedCornerShape(20.dp)
    val noBabiesEmojiSize     : TextUnit,   // replaces: fontSize = 64.sp

    // ── ★ Small text tokens (HomeTab / WelcomeSection / BabyCard) ────────────
    val homeSmallTextSize   : TextUnit,   // replaces: fontSize = 9.sp  labels
    val homeSmallLineHeight : TextUnit,   // replaces: lineHeight = 11.sp labels

    // ── ★ Gender banner ───────────────────────────────────────────────────────
    val starSizeLarge             : TextUnit,   // replaces: 16.sp HangingStars
    val starSizeSmall             : TextUnit,   // replaces: 12.sp HangingStars
    val bannerMoonEmojiSize       : TextUnit,   // replaces: (iconXLarge.value - 6).sp
    val bannerLabelLetterSpacing  : TextUnit,   // replaces: letterSpacing = 1.5.sp

    // ── ★ Bottom Navigation Bar ───────────────────────────────────────────────
    val navBarHeight      : Dp,         // overall bar height
    val navBarPillSize    : Dp,         // selected indicator circle
    val navBarPillCorner  : Dp,         // pill corner radius
    val navBarLabelSize   : TextUnit,   // nav label font size
    val navBarPaddingH    : Dp,         // horizontal padding
    val navBarPaddingV    : Dp,         // vertical padding
    val navButtonPadding  : Dp,         // inner padding per button
    val navIconLabelGap   : Dp,         // gap between icon and label

    // ── ★ BenchDetailScreen ───────────────────────────────────────────────────
    val detailRowVertPadding  : Dp,   // replaces: vertical = 4.dp
    val detailIconSize        : Dp,   // replaces: size = 18.dp
    val detailIconTopPadding  : Dp,   // replaces: top = 2.dp
    val benchDistanceIconSize : Dp,   // replaces: size = 16.dp
) {
    companion object {

        @Composable
        @ReadOnlyComposable
        fun create(windowSizeClass: WindowSizeClass): Dimensions = when (windowSizeClass) {
            WindowSizeClass.COMPACT  -> compactDimensions()
            WindowSizeClass.MEDIUM   -> mediumDimensions()
            WindowSizeClass.EXPANDED -> expandedDimensions()
        }

        // ── COMPACT — Phone portrait < 600 dp ─────────────────────────────────
        private fun compactDimensions() = Dimensions(
            cornerImageSize = 120.dp,
            logoSize        = 320.dp,
            screenPadding   = 16.dp,
            logoPadding     = 8.dp,

            spacingXSmall  = 4.dp,
            spacingSmall   = 8.dp,
            spacingMedium  = 16.dp,
            spacingLarge   = 24.dp,
            spacingXLarge  = 32.dp,
            spacingXXLarge = 40.dp,

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

            // Border
            borderWidthThin   = 1.dp,
            borderWidthMedium = 2.dp,

            // Profile — InfoRow / SectionCard
            profileInfoRowVerticalPadding   = 5.dp,
            profileSectionLabelStartPadding = 4.dp,
            profileInfoIconWidth            = 28.dp,
            profileInfoIconFontSize         = 15.sp,
            profileSectionCardVertPad       = 14.dp,   // ≈ spacingMedium - 2.dp

            // Profile — Archived badge
            profileArchivedCorner   = 8.dp,
            profileArchivedPaddingH = 12.dp,
            profileArchivedPaddingV = 4.dp,

            // Profile — Quick-action buttons
            profileQuickActionEmojiSize  = 20.sp,
            profileQuickActionLabelSize  = 9.sp,
            profileQuickActionLineHeight = 11.sp,
            profileQuickActionItemGap    = 4.dp,

            // Tonal elevation
            cardTonalElevation6 = 6.dp,

            // Baby card
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

            // BabyProfileTabContent misc
            addButtonSize         = 36.dp,
            chartCardCornerRadius = 18.dp,
            chipCornerRadius      = 10.dp,
            filterTabCorner       = 20.dp,
            noBabiesEmojiSize     = 64.sp,

            // Small text
            homeSmallTextSize   = 9.sp,
            homeSmallLineHeight = 11.sp,

            // Gender banner
            starSizeLarge            = 16.sp,
            starSizeSmall            = 12.sp,
            bannerMoonEmojiSize      = 42.sp,   // ≈ iconXLarge(48dp) - 6
            bannerLabelLetterSpacing = 1.5.sp,

            // Bottom nav
            navBarHeight     = 64.dp,
            navBarPillSize   = 40.dp,
            navBarPillCorner = 20.dp,
            navBarLabelSize  = 10.sp,
            navBarPaddingH   = 4.dp,
            navBarPaddingV   = 8.dp,
            navButtonPadding = 8.dp,
            navIconLabelGap  = 2.dp,

            // BenchDetailScreen
            detailRowVertPadding  = 4.dp,
            detailIconSize        = 18.dp,
            detailIconTopPadding  = 2.dp,
            benchDistanceIconSize = 16.dp,
        )

        // ── MEDIUM — Tablet portrait / phone landscape 600–840 dp ─────────────
        private fun mediumDimensions() = Dimensions(
            cornerImageSize = 160.dp,
            logoSize        = 400.dp,
            screenPadding   = 24.dp,
            logoPadding     = 12.dp,

            spacingXSmall  = 6.dp,
            spacingSmall   = 12.dp,
            spacingMedium  = 20.dp,
            spacingLarge   = 28.dp,
            spacingXLarge  = 40.dp,
            spacingXXLarge = 56.dp,

            iconSmall  = 20.dp,
            iconMedium = 28.dp,
            iconLarge  = 36.dp,
            iconXLarge = 56.dp,

            buttonHeight       = 54.dp,
            buttonCornerRadius = 18.dp,

            cardElevation    = 6.dp,
            cardCornerRadius = 18.dp,

            avatarSmall  = 40.dp,
            avatarMedium = 60.dp,
            avatarLarge  = 80.dp,

            maxContentWidth = 700.dp,

            // Border
            borderWidthThin   = 1.dp,
            borderWidthMedium = 2.dp,

            // Profile — InfoRow / SectionCard
            profileInfoRowVerticalPadding   = 6.dp,
            profileSectionLabelStartPadding = 4.dp,
            profileInfoIconWidth            = 32.dp,
            profileInfoIconFontSize         = 17.sp,
            profileSectionCardVertPad       = 18.dp,

            // Profile — Archived badge
            profileArchivedCorner   = 10.dp,
            profileArchivedPaddingH = 14.dp,
            profileArchivedPaddingV = 5.dp,

            // Profile — Quick-action buttons
            profileQuickActionEmojiSize  = 22.sp,
            profileQuickActionLabelSize  = 10.sp,
            profileQuickActionLineHeight = 12.sp,
            profileQuickActionItemGap    = 5.dp,

            // Tonal elevation
            cardTonalElevation6 = 6.dp,

            // Baby card
            babyCardAvatarSize          = 60.dp,
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

            // BabyProfileTabContent misc
            addButtonSize         = 40.dp,
            chartCardCornerRadius = 20.dp,
            chipCornerRadius      = 12.dp,
            filterTabCorner       = 22.dp,
            noBabiesEmojiSize     = 72.sp,

            // Small text
            homeSmallTextSize   = 10.sp,
            homeSmallLineHeight = 12.sp,

            // Gender banner
            starSizeLarge            = 18.sp,
            starSizeSmall            = 14.sp,
            bannerMoonEmojiSize      = 50.sp,
            bannerLabelLetterSpacing = 1.5.sp,

            // Bottom nav
            navBarHeight     = 72.dp,
            navBarPillSize   = 48.dp,
            navBarPillCorner = 24.dp,
            navBarLabelSize  = 11.sp,
            navBarPaddingH   = 8.dp,
            navBarPaddingV   = 10.dp,
            navButtonPadding = 10.dp,
            navIconLabelGap  = 3.dp,

            // BenchDetailScreen
            detailRowVertPadding  = 5.dp,
            detailIconSize        = 20.dp,
            detailIconTopPadding  = 2.dp,
            benchDistanceIconSize = 18.dp,
        )

        // ── EXPANDED — Tablet landscape / desktop > 840 dp ────────────────────
        private fun expandedDimensions() = Dimensions(
            cornerImageSize = 200.dp,
            logoSize        = 480.dp,
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

            // Border
            borderWidthThin   = 1.dp,
            borderWidthMedium = 2.dp,

            // Profile — InfoRow / SectionCard
            profileInfoRowVerticalPadding   = 8.dp,
            profileSectionLabelStartPadding = 6.dp,
            profileInfoIconWidth            = 36.dp,
            profileInfoIconFontSize         = 18.sp,
            profileSectionCardVertPad       = 22.dp,

            // Profile — Archived badge
            profileArchivedCorner   = 12.dp,
            profileArchivedPaddingH = 16.dp,
            profileArchivedPaddingV = 6.dp,

            // Profile — Quick-action buttons
            profileQuickActionEmojiSize  = 24.sp,
            profileQuickActionLabelSize  = 11.sp,
            profileQuickActionLineHeight = 14.sp,
            profileQuickActionItemGap    = 6.dp,

            // Tonal elevation
            cardTonalElevation6 = 6.dp,

            // Baby card
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

            // BabyProfileTabContent misc
            addButtonSize         = 44.dp,
            chartCardCornerRadius = 22.dp,
            chipCornerRadius      = 14.dp,
            filterTabCorner       = 24.dp,
            noBabiesEmojiSize     = 80.sp,

            // Small text
            homeSmallTextSize   = 11.sp,
            homeSmallLineHeight = 13.sp,

            // Gender banner
            starSizeLarge            = 20.sp,
            starSizeSmall            = 16.sp,
            bannerMoonEmojiSize      = 58.sp,
            bannerLabelLetterSpacing = 1.5.sp,

            // Bottom nav
            navBarHeight     = 80.dp,
            navBarPillSize   = 56.dp,
            navBarPillCorner = 28.dp,
            navBarLabelSize  = 12.sp,
            navBarPaddingH   = 12.dp,
            navBarPaddingV   = 12.dp,
            navButtonPadding = 12.dp,
            navIconLabelGap  = 4.dp,

            // BenchDetailScreen
            detailRowVertPadding  = 6.dp,
            detailIconSize        = 22.dp,
            detailIconTopPadding  = 3.dp,
            benchDistanceIconSize = 20.dp,
        )
    }
}

val LocalDimensions = staticCompositionLocalOf<Dimensions> {
    error("No Dimensions provided")
}

val LocalScreenInfo = staticCompositionLocalOf<ScreenInfo> {
    ScreenInfo(
        widthDp         = 0.dp,
        heightDp        = 0.dp,
        windowSizeClass = WindowSizeClass.COMPACT
    )
}