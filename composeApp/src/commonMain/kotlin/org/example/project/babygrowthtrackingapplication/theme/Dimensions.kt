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
    val cardElevationSmall : Dp,
    val cardCornerRadius : Dp,

    // ── Avatars ───────────────────────────────────────────────────────────────
    val avatarSmall  : Dp,
    val avatarMedium : Dp,
    val avatarLarge  : Dp,

    // ── Layout ────────────────────────────────────────────────────────────────
    val maxContentWidth : Dp,

    // ── Auth screen constraints ───────────────────────────────────────────────
    val authCardMinWidth : Dp,
    val authCardMaxWidth : Dp,

    // ── Borders ───────────────────────────────────────────────────────────────
    val borderWidthThin   : Dp,
    val borderWidthMedium : Dp,

    // ── Text fields ───────────────────────────────────────────────────────────
    val textFieldCornerRadius       : Dp,
    val searchFieldCornerRadius     : Dp,
    val textFieldLabelBottomPadding : Dp,

    // ── Social login / OTP ────────────────────────────────────────────────────
    val socialButtonSize     : Dp,
    val socialIconSize       : Dp,
    val orDividerTextPaddingH: Dp,
    val dividerHeight        : Dp,

    // ── OTP ───────────────────────────────────────────────────────────────────
    val otpBorderWidth : Dp,

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
    val navBarHeight     : Dp,
    val navBarPillSize   : Dp,
    val navBarPillCorner : Dp,
    val navBarLabelSize  : TextUnit,
    val navBarPaddingH   : Dp,
    val navBarPaddingV   : Dp,
    val navButtonPadding : Dp,
    val navIconLabelGap  : Dp,

    // ── Side navigation rail (landscape) ─────────────────────────────────────
    val railWidth        : Dp,
    val railItemSize     : Dp,
    val railIconSize     : Dp,

    // ── Rail item internals ───────────────────────────────────────────────────
    val railItemSpacing       : Dp,
    val railItemMinHeight     : Dp,
    val railItemCornerRadius  : Dp,
    val railItemPaddingV      : Dp,
    val railItemPaddingH      : Dp,
    val railIconLabelGap      : Dp,

    // ── BenchDetailScreen ─────────────────────────────────────────────────────
    val detailRowVertPadding  : Dp,
    val detailIconSize        : Dp,
    val detailIconTopPadding  : Dp,
    val benchDistanceIconSize : Dp,

    // ── NEW: Landscape pane widths ────────────────────────────────────────────
    /** Width of the left sidebar pane in landscape two-pane layouts */
    val landscapePaneWidth      : Dp,
    /** Width of the settings/chart/dev landscape left sidebar */
    val landscapeNarrowPaneWidth: Dp,
    /** Width of the feeding-guide landscape left pane */
    val landscapeWidePaneWidth  : Dp,

    // ── NEW: Child dev / milestone screen tokens ──────────────────────────────
    val devMonthBadgeSize     : Dp,   // month number circle (was 40.dp)
    val devEditButtonSize     : Dp,   // edit IconButton size (was 36.dp)
    val devCheckCircleSize    : Dp,   // three-state check circle (was 28.dp)
    val devIndicatorDotSize   : Dp,   // small preview progress dot (was 8.dp)
    val devHeaderIconBoxSize  : Dp,   // header icon box (was 52.dp)
    val devSettingsIconBoxSize: Dp,   // settings row icon box (was 36.dp)
    val devSettingsChevronSize: Dp,   // chevron icon in settings row (was 18.dp)
    val devSettingsRowPaddingH: Dp,   // horizontal row padding (was 16.dp)
    val devSettingsIconCorner : Dp,   // corner radius for icon box (was 8.dp)

    // ── NEW: Vaccination / health screen tokens ───────────────────────────────
    val vaccinationProgressBarHeight: Dp,   // LinearProgress height (was 8.dp)
    val vaccinationCardIconSize     : Dp,   // status circle (was 42.dp)
    val healthCircleSize            : Dp,   // assigned center icon circle (was 40.dp)
    val healthDropdownItemPaddingV  : Dp,   // dropdown item vertical (was 10.dp)
    val healthSubTabElevation       : Dp,   // sub-tab surface elevation (was 2.dp)

    // ── NEW: Chart screen tokens ──────────────────────────────────────────────
    val chartLandscapePaneWidth    : Dp,       // chart left pane in landscape (was 260.dp)
    val chartLetterSpacing         : TextUnit, // LATEST MEASUREMENT label (was 0.8.sp)
    val chartDividerThickness      : Dp,       // thin divider in chart card (was 0.5.dp)

    // ── NEW: Settings screen tokens ───────────────────────────────────────────
    val settingsLandscapeRailWidth  : Dp,   // settings navigation rail width (was 240.dp)
    val settingsAvatarSize          : Dp,   // profile avatar circle (was 44.dp / 40.dp)
    val settingsRowIconSize         : Dp,   // icon in ArrowRow / ToggleRow (was 20.dp)
    val settingsChevronSize         : Dp,   // chevron (was 18.dp)
    val settingsDividerThickness    : Dp,   // row separator (was 0.5.dp)
    val settingsRowPaddingH         : Dp,   // horizontal row padding (was 16.dp)
    val settingsVersionString       : String, // version number (was "1.0.0")

    // ── NEW: Guide / lullaby screen tokens ───────────────────────────────────
    val guideCategoryIconBoxSize : Dp,      // category tile icon circle (was 52.dp)
    val guideCategoryIconFontSize: TextUnit,// category emoji size (was 26.sp)
    val guideTabPillPaddingH     : Dp,      // pill tab horizontal padding (was 14.dp)
    val guideTabPillPaddingV     : Dp,      // pill tab vertical padding (was 7.dp)
    val guideFeedbackPillPaddingH: Dp,      // feedback pill horizontal (was 12.dp)
    val guideFeedbackPillPaddingV: Dp,      // feedback pill vertical (was 5.dp)
    val lullabyNoteEmojiSize     : TextUnit,// music note emoji (was 20.sp)
    val lullabySeekBarHeight     : Dp,      // seek bar track height (was 4.dp)
    val lullabyThumbSize         : Dp,      // draggable thumb circle (was 14.dp)
    val lullabyPlayCircleSize    : Dp,      // large play/pause button (was 46.dp)
    val lullabySkipLabelSize     : TextUnit,// ±10s label size (was 11.sp)

    // ── NEW: BenchMapScreen / BenchCard tokens ────────────────────────────────
    val benchPanelMinHeight : Dp,   // slide-up panel min (was 200.dp)
    val benchPanelMaxHeight : Dp,   // slide-up panel max (was 300.dp)
    val benchCardWidth      : Dp,   // individual bench card (was 220.dp)
    val benchCardFabBottomPad: Dp,  // FAB padding when panel visible (was 260.dp)

    // ── NEW: BabyProfileTabContent bottom padding ─────────────────────────────
    val babyTabBottomContentPadding: Dp,  // was 100.dp

    // ── NEW: Thin divider / hairline ──────────────────────────────────────────
    val hairlineDividerThickness: Dp,  // Dp.Hairline equivalent token (was Dp.Hairline)

    // ── NEW: Profile divider thickness ───────────────────────────────────────
    val profileDividerThickness: Dp,   // was 0.8.dp

    // ── NEW: ChildIllnesses tokens ────────────────────────────────────────────
    val illnessStatusDotSize     : Dp,   // status dot (was 10.dp)
    val illnessDividerThickness  : Dp,   // card inner divider (was 0.5.dp)
    val illnessActionIconSize    : Dp,   // edit/delete icon (was 16.dp)
    val illnessActionButtonSize  : Dp,   // icon button size (was 32.dp)
    val illnessSummaryIconSize   : Dp,   // summary card icon (was 42.dp)
    val illnessLandscapeStatusChipPaddingH: Dp, // chip horizontal padding (was 12.dp)
    val illnessLandscapeStatusChipPaddingV: Dp, // chip vertical padding (was 6.dp)

    // ── NEW: Home screen banner tokens ────────────────────────────────────────
    val bannerStarOffset1X : Dp,  // star offset values in BannerStars
    val bannerStarOffset1Y : Dp,
    val bannerStarOffset2X : Dp,
    val bannerStarOffset2Y : Dp,
    val bannerStarOffset3X : Dp,
    val bannerStarOffset3Y : Dp,
    val bannerSmallStar1Size: TextUnit,
    val bannerSmallStar2Size: TextUnit,
    val bannerSmallStar3Size: TextUnit,
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
            cardElevationSmall = 2.dp,
            cardCornerRadius = 16.dp,

            avatarSmall  = 32.dp,
            avatarMedium = 48.dp,
            avatarLarge  = 64.dp,

            maxContentWidth = Dp.Unspecified,

            authCardMinWidth = 320.dp,
            authCardMaxWidth = 420.dp,

            borderWidthThin   = 1.dp,
            borderWidthMedium = 2.dp,

            textFieldCornerRadius       = 16.dp,
            searchFieldCornerRadius     = 20.dp,
            textFieldLabelBottomPadding = 6.dp,

            socialButtonSize      = 56.dp,
            socialIconSize        = 24.dp,
            orDividerTextPaddingH = 16.dp,
            dividerHeight         = 1.dp,

            otpBorderWidth = 2.dp,

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

            navBarHeight     = if (landscape) 0.dp else 64.dp,
            navBarPillSize   = 40.dp,
            navBarPillCorner = 20.dp,
            navBarLabelSize  = 10.sp,
            navBarPaddingH   = 4.dp,
            navBarPaddingV   = 8.dp,
            navButtonPadding = 8.dp,
            navIconLabelGap  = 2.dp,

            railWidth    = 72.dp,
            railItemSize = 56.dp,
            railIconSize = 24.dp,

            railItemSpacing      = 4.dp,
            railItemMinHeight    = 56.dp,
            railItemCornerRadius = 12.dp,
            railItemPaddingV     = 8.dp,
            railItemPaddingH     = 4.dp,
            railIconLabelGap     = 2.dp,

            detailRowVertPadding  = 4.dp,
            detailIconSize        = 18.dp,
            detailIconTopPadding  = 2.dp,
            benchDistanceIconSize = 16.dp,

            // Landscape pane widths
            landscapePaneWidth       = if (landscape) 240.dp else 240.dp,
            landscapeNarrowPaneWidth = if (landscape) 220.dp else 220.dp,
            landscapeWidePaneWidth   = if (landscape) 280.dp else 280.dp,

            // Child dev tokens
            devMonthBadgeSize      = 40.dp,
            devEditButtonSize      = 36.dp,
            devCheckCircleSize     = 28.dp,
            devIndicatorDotSize    = 8.dp,
            devHeaderIconBoxSize   = 52.dp,
            devSettingsIconBoxSize = 36.dp,
            devSettingsChevronSize = 18.dp,
            devSettingsRowPaddingH = 16.dp,
            devSettingsIconCorner  = 8.dp,

            // Vaccination / health tokens
            vaccinationProgressBarHeight = 8.dp,
            vaccinationCardIconSize      = 42.dp,
            healthCircleSize             = 40.dp,
            healthDropdownItemPaddingV   = 10.dp,
            healthSubTabElevation        = 2.dp,

            // Chart tokens
            chartLandscapePaneWidth = 260.dp,
            chartLetterSpacing      = 0.8.sp,
            chartDividerThickness   = 0.5.dp,

            // Settings tokens
            settingsLandscapeRailWidth = 240.dp,
            settingsAvatarSize         = 44.dp,
            settingsRowIconSize        = 20.dp,
            settingsChevronSize        = 18.dp,
            settingsDividerThickness   = 0.5.dp,
            settingsRowPaddingH        = 16.dp,
            settingsVersionString      = "1.0.0",

            // Guide / lullaby tokens
            guideCategoryIconBoxSize  = 52.dp,
            guideCategoryIconFontSize = 26.sp,
            guideTabPillPaddingH      = 14.dp,
            guideTabPillPaddingV      = 7.dp,
            guideFeedbackPillPaddingH = 12.dp,
            guideFeedbackPillPaddingV = 5.dp,
            lullabyNoteEmojiSize      = 20.sp,
            lullabySeekBarHeight      = 4.dp,
            lullabyThumbSize          = 14.dp,
            lullabyPlayCircleSize     = 46.dp,
            lullabySkipLabelSize      = 11.sp,

            // Bench tokens
            benchPanelMinHeight  = 200.dp,
            benchPanelMaxHeight  = 300.dp,
            benchCardWidth       = 220.dp,
            benchCardFabBottomPad = 260.dp,

            // Baby tab
            babyTabBottomContentPadding = 100.dp,

            // Hairline
            hairlineDividerThickness = 0.5.dp,

            // Profile divider
            profileDividerThickness = 0.8.dp,

            // ChildIllnesses tokens
            illnessStatusDotSize               = 10.dp,
            illnessDividerThickness            = 0.5.dp,
            illnessActionIconSize              = 16.dp,
            illnessActionButtonSize            = 32.dp,
            illnessSummaryIconSize             = 42.dp,
            illnessLandscapeStatusChipPaddingH = 12.dp,
            illnessLandscapeStatusChipPaddingV = 6.dp,

            // Banner star offsets
            bannerStarOffset1X  = 28.dp,
            bannerStarOffset1Y  = 40.dp,
            bannerStarOffset2X  = 16.dp,
            bannerStarOffset2Y  = 56.dp,
            bannerStarOffset3X  = 48.dp,
            bannerStarOffset3Y  = 4.dp,
            bannerSmallStar1Size = 10.sp,
            bannerSmallStar2Size = 14.sp,
            bannerSmallStar3Size = 8.sp,
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
            cardElevationSmall = 3.dp,
            cardCornerRadius = 18.dp,

            avatarSmall  = 40.dp,
            avatarMedium = 60.dp,
            avatarLarge  = 80.dp,

            maxContentWidth = Dp.Unspecified,

            authCardMinWidth = 360.dp,
            authCardMaxWidth = 520.dp,

            borderWidthThin   = 1.dp,
            borderWidthMedium = 2.dp,

            textFieldCornerRadius       = 18.dp,
            searchFieldCornerRadius     = 22.dp,
            textFieldLabelBottomPadding = 7.dp,

            socialButtonSize      = 60.dp,
            socialIconSize        = 26.dp,
            orDividerTextPaddingH = 18.dp,
            dividerHeight         = 1.dp,

            otpBorderWidth = 2.dp,

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

            railItemSpacing      = 4.dp,
            railItemMinHeight    = 60.dp,
            railItemCornerRadius = 14.dp,
            railItemPaddingV     = 10.dp,
            railItemPaddingH     = 4.dp,
            railIconLabelGap     = 2.dp,

            detailRowVertPadding  = 5.dp,
            detailIconSize        = 20.dp,
            detailIconTopPadding  = 2.dp,
            benchDistanceIconSize = 18.dp,

            landscapePaneWidth       = 260.dp,
            landscapeNarrowPaneWidth = 240.dp,
            landscapeWidePaneWidth   = 300.dp,

            devMonthBadgeSize      = 44.dp,
            devEditButtonSize      = 38.dp,
            devCheckCircleSize     = 30.dp,
            devIndicatorDotSize    = 9.dp,
            devHeaderIconBoxSize   = 56.dp,
            devSettingsIconBoxSize = 38.dp,
            devSettingsChevronSize = 20.dp,
            devSettingsRowPaddingH = 18.dp,
            devSettingsIconCorner  = 9.dp,

            vaccinationProgressBarHeight = 8.dp,
            vaccinationCardIconSize      = 44.dp,
            healthCircleSize             = 42.dp,
            healthDropdownItemPaddingV   = 11.dp,
            healthSubTabElevation        = 2.dp,

            chartLandscapePaneWidth = 280.dp,
            chartLetterSpacing      = 0.8.sp,
            chartDividerThickness   = 0.5.dp,

            settingsLandscapeRailWidth = 260.dp,
            settingsAvatarSize         = 46.dp,
            settingsRowIconSize        = 22.dp,
            settingsChevronSize        = 20.dp,
            settingsDividerThickness   = 0.5.dp,
            settingsRowPaddingH        = 18.dp,
            settingsVersionString      = "1.0.0",

            guideCategoryIconBoxSize  = 56.dp,
            guideCategoryIconFontSize = 28.sp,
            guideTabPillPaddingH      = 16.dp,
            guideTabPillPaddingV      = 8.dp,
            guideFeedbackPillPaddingH = 14.dp,
            guideFeedbackPillPaddingV = 6.dp,
            lullabyNoteEmojiSize      = 22.sp,
            lullabySeekBarHeight      = 5.dp,
            lullabyThumbSize          = 16.dp,
            lullabyPlayCircleSize     = 50.dp,
            lullabySkipLabelSize      = 12.sp,

            benchPanelMinHeight   = 210.dp,
            benchPanelMaxHeight   = 320.dp,
            benchCardWidth        = 240.dp,
            benchCardFabBottomPad = 280.dp,

            babyTabBottomContentPadding = 110.dp,

            hairlineDividerThickness = 0.5.dp,

            profileDividerThickness = 0.8.dp,

            illnessStatusDotSize               = 10.dp,
            illnessDividerThickness            = 0.5.dp,
            illnessActionIconSize              = 17.dp,
            illnessActionButtonSize            = 34.dp,
            illnessSummaryIconSize             = 44.dp,
            illnessLandscapeStatusChipPaddingH = 13.dp,
            illnessLandscapeStatusChipPaddingV = 7.dp,

            bannerStarOffset1X  = 32.dp,
            bannerStarOffset1Y  = 44.dp,
            bannerStarOffset2X  = 20.dp,
            bannerStarOffset2Y  = 60.dp,
            bannerStarOffset3X  = 52.dp,
            bannerStarOffset3Y  = 6.dp,
            bannerSmallStar1Size = 11.sp,
            bannerSmallStar2Size = 15.sp,
            bannerSmallStar3Size = 9.sp,
        )

        // ─────────────────────────────────────────────────────────────────────
        // EXPANDED — tablet landscape / desktop (> 840dp)
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
            cardElevationSmall = 4.dp,
            cardCornerRadius = 20.dp,

            avatarSmall  = 48.dp,
            avatarMedium = 72.dp,
            avatarLarge  = 96.dp,

            maxContentWidth = 600.dp,

            authCardMinWidth = 400.dp,
            authCardMaxWidth = 600.dp,

            borderWidthThin   = 1.dp,
            borderWidthMedium = 2.dp,

            textFieldCornerRadius       = 20.dp,
            searchFieldCornerRadius     = 24.dp,
            textFieldLabelBottomPadding = 8.dp,

            socialButtonSize      = 64.dp,
            socialIconSize        = 28.dp,
            orDividerTextPaddingH = 20.dp,
            dividerHeight         = 1.dp,

            otpBorderWidth = 2.dp,

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

            railItemSpacing      = 6.dp,
            railItemMinHeight    = 64.dp,
            railItemCornerRadius = 16.dp,
            railItemPaddingV     = 12.dp,
            railItemPaddingH     = 6.dp,
            railIconLabelGap     = 4.dp,

            detailRowVertPadding  = 6.dp,
            detailIconSize        = 22.dp,
            detailIconTopPadding  = 3.dp,
            benchDistanceIconSize = 20.dp,

            landscapePaneWidth       = 300.dp,
            landscapeNarrowPaneWidth = 260.dp,
            landscapeWidePaneWidth   = 340.dp,

            devMonthBadgeSize      = 48.dp,
            devEditButtonSize      = 40.dp,
            devCheckCircleSize     = 32.dp,
            devIndicatorDotSize    = 10.dp,
            devHeaderIconBoxSize   = 60.dp,
            devSettingsIconBoxSize = 40.dp,
            devSettingsChevronSize = 22.dp,
            devSettingsRowPaddingH = 20.dp,
            devSettingsIconCorner  = 10.dp,

            vaccinationProgressBarHeight = 10.dp,
            vaccinationCardIconSize      = 46.dp,
            healthCircleSize             = 44.dp,
            healthDropdownItemPaddingV   = 12.dp,
            healthSubTabElevation        = 2.dp,

            chartLandscapePaneWidth = 300.dp,
            chartLetterSpacing      = 0.8.sp,
            chartDividerThickness   = 0.5.dp,

            settingsLandscapeRailWidth = 280.dp,
            settingsAvatarSize         = 48.dp,
            settingsRowIconSize        = 24.dp,
            settingsChevronSize        = 22.dp,
            settingsDividerThickness   = 0.5.dp,
            settingsRowPaddingH        = 20.dp,
            settingsVersionString      = "1.0.0",

            guideCategoryIconBoxSize  = 60.dp,
            guideCategoryIconFontSize = 30.sp,
            guideTabPillPaddingH      = 18.dp,
            guideTabPillPaddingV      = 9.dp,
            guideFeedbackPillPaddingH = 16.dp,
            guideFeedbackPillPaddingV = 7.dp,
            lullabyNoteEmojiSize      = 24.sp,
            lullabySeekBarHeight      = 5.dp,
            lullabyThumbSize          = 16.dp,
            lullabyPlayCircleSize     = 52.dp,
            lullabySkipLabelSize      = 13.sp,

            benchPanelMinHeight   = 220.dp,
            benchPanelMaxHeight   = 340.dp,
            benchCardWidth        = 260.dp,
            benchCardFabBottomPad = 300.dp,

            babyTabBottomContentPadding = 120.dp,

            hairlineDividerThickness = 0.5.dp,

            profileDividerThickness = 0.8.dp,

            illnessStatusDotSize               = 11.dp,
            illnessDividerThickness            = 0.5.dp,
            illnessActionIconSize              = 18.dp,
            illnessActionButtonSize            = 36.dp,
            illnessSummaryIconSize             = 46.dp,
            illnessLandscapeStatusChipPaddingH = 14.dp,
            illnessLandscapeStatusChipPaddingV = 8.dp,

            bannerStarOffset1X  = 36.dp,
            bannerStarOffset1Y  = 48.dp,
            bannerStarOffset2X  = 24.dp,
            bannerStarOffset2Y  = 64.dp,
            bannerStarOffset3X  = 56.dp,
            bannerStarOffset3Y  = 8.dp,
            bannerSmallStar1Size = 12.sp,
            bannerSmallStar2Size = 16.sp,
            bannerSmallStar3Size = 10.sp,
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