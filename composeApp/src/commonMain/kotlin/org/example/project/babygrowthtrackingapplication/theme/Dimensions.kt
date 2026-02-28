package org.example.project.babygrowthtrackingapplication.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Responsive dimensions that adapt based on screen size
 * Updated with iOS-style reduced corner radius
 */
data class Dimensions(
    // Splash Screen
    val cornerImageSize: Dp,
    val logoSize: Dp,
    val screenPadding: Dp,
    val logoPadding: Dp,

    // Common spacing
    val spacingXSmall: Dp,
    val spacingSmall: Dp,
    val spacingMedium: Dp,
    val spacingLarge: Dp,
    val spacingXLarge: Dp,
    val spacingXXLarge: Dp,

    // Icon sizes
    val iconSmall: Dp,
    val iconMedium: Dp,
    val iconLarge: Dp,
    val iconXLarge: Dp,

    // Button sizes (iOS-style with reduced corner radius)
    val buttonHeight: Dp,
    val buttonCornerRadius: Dp,

    // Card sizes
    val cardElevation: Dp,
    val cardCornerRadius: Dp,

    // Image sizes
    val avatarSmall: Dp,
    val avatarMedium: Dp,
    val avatarLarge: Dp,

    // Content max width
    val maxContentWidth: Dp
) {
    companion object {
        /**
         * Creates dimensions based on window size class
         */
        @Composable
        @ReadOnlyComposable
        fun create(windowSizeClass: WindowSizeClass): Dimensions {
            return when (windowSizeClass) {
                WindowSizeClass.COMPACT -> compactDimensions()
                WindowSizeClass.MEDIUM -> mediumDimensions()
                WindowSizeClass.EXPANDED -> expandedDimensions()
            }
        }

        /**
         * COMPACT - Phone in portrait (< 600dp)
         * iOS-style with reduced corner radius (16dp instead of 10dp)
         */
        private fun compactDimensions() = Dimensions(
            // Splash Screen
            cornerImageSize = 120.dp,
            logoSize = 320.dp,
            screenPadding = 16.dp,
            logoPadding = 8.dp,

            // Common spacing
            spacingXSmall = 4.dp,
            spacingSmall = 8.dp,
            spacingMedium = 16.dp,
            spacingLarge = 24.dp,
            spacingXLarge = 32.dp,
            spacingXXLarge = 40.dp,

            // Icon sizes
            iconSmall = 16.dp,
            iconMedium = 24.dp,
            iconLarge = 32.dp,
            iconXLarge = 48.dp,

            // Button sizes - iOS-style reduced corner radius
            buttonHeight = 50.dp,
            buttonCornerRadius = 16.dp, // Updated from 10dp to 16dp for iOS style

            // Card sizes
            cardElevation = 4.dp,
            cardCornerRadius = 16.dp, // Matching button style

            // Image sizes
            avatarSmall = 32.dp,
            avatarMedium = 48.dp,
            avatarLarge = 64.dp,

            // Content max width
            maxContentWidth = Dp.Unspecified
        )

        /**
         * MEDIUM - Tablet in portrait, phone in landscape (600dp - 840dp)
         */
        private fun mediumDimensions() = Dimensions(
            // Splash Screen
            cornerImageSize = 160.dp,
            logoSize = 400.dp,
            screenPadding = 24.dp,
            logoPadding = 12.dp,

            // Common spacing
            spacingXSmall = 6.dp,
            spacingSmall = 12.dp,
            spacingMedium = 20.dp,
            spacingLarge = 28.dp,
            spacingXLarge = 40.dp,
            spacingXXLarge = 56.dp,

            // Icon sizes
            iconSmall = 20.dp,
            iconMedium = 28.dp,
            iconLarge = 36.dp,
            iconXLarge = 56.dp,

            // Button sizes - iOS-style reduced corner radius
            buttonHeight = 54.dp,
            buttonCornerRadius = 18.dp, // Updated from 12dp to 18dp

            // Card sizes
            cardElevation = 6.dp,
            cardCornerRadius = 18.dp, // Matching button style

            // Image sizes
            avatarSmall = 40.dp,
            avatarMedium = 60.dp,
            avatarLarge = 80.dp,

            // Content max width
            maxContentWidth = 700.dp
        )

        /**
         * EXPANDED - Tablet in landscape, desktop (> 840dp)
         */
        private fun expandedDimensions() = Dimensions(
            // Splash Screen
            cornerImageSize = 200.dp,
            logoSize = 480.dp,
            screenPadding = 32.dp,
            logoPadding = 16.dp,

            // Common spacing
            spacingXSmall = 8.dp,
            spacingSmall = 16.dp,
            spacingMedium = 24.dp,
            spacingLarge = 32.dp,
            spacingXLarge = 48.dp,
            spacingXXLarge = 64.dp,

            // Icon sizes
            iconSmall = 24.dp,
            iconMedium = 32.dp,
            iconLarge = 40.dp,
            iconXLarge = 64.dp,

            // Button sizes - iOS-style reduced corner radius
            buttonHeight = 56.dp,
            buttonCornerRadius = 20.dp, // Updated from 14dp to 20dp

            // Card sizes
            cardElevation = 8.dp,
            cardCornerRadius = 20.dp, // Matching button style

            // Image sizes
            avatarSmall = 48.dp,
            avatarMedium = 72.dp,
            avatarLarge = 96.dp,

            // Content max width
            maxContentWidth = 600.dp
        )
    }
}

/**
 * CompositionLocal for providing dimensions throughout the app
 */
val LocalDimensions = staticCompositionLocalOf<Dimensions> {
    error("No Dimensions provided")
}

/**
 * CompositionLocal for providing screen info throughout the app
 */
val LocalScreenInfo = staticCompositionLocalOf<ScreenInfo> {
    ScreenInfo(
        widthDp = 0.dp,
        heightDp = 0.dp,
        windowSizeClass = WindowSizeClass.COMPACT
    )
}