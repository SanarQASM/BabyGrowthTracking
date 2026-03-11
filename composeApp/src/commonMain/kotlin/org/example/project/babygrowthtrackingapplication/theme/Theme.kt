package org.example.project.babygrowthtrackingapplication.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * CompositionLocal for accessing the current gender theme throughout the app
 * This is the ONLY definition of LocalGenderTheme - do not create duplicates!
 */
val LocalGenderTheme = staticCompositionLocalOf { GenderTheme.NEUTRAL }

/**
 * FIX: CompositionLocal for the app's in-app dark mode toggle.
 *
 * WHY THIS EXISTS:
 * `isSystemInDarkTheme()` reads the OS-level dark mode setting, NOT the
 * user's in-app preference. When the user toggles dark mode in Settings,
 * `BabyGrowthTheme` correctly switches the MaterialTheme colorScheme
 * (because `darkTheme` param is passed from App.kt state), BUT every call
 * to `MaterialTheme.customColors` was independently calling
 * `isSystemInDarkTheme()` — which never changes — so all accent colors,
 * gradients, and glass overlays stayed stuck in the wrong mode.
 *
 * THE FIX: `BabyGrowthTheme` provides `LocalIsDarkTheme` with the same
 * `darkTheme` value it already uses for colorScheme selection. Every
 * composable that needs to know "is dark mode on?" reads THIS local instead
 * of calling `isSystemInDarkTheme()` directly.
 */
val LocalIsDarkTheme = staticCompositionLocalOf { false }

/**
 * Gender-specific color schemes for Girl theme
 */
private val GirlLightColorScheme = lightColorScheme(
    primary            = GirlLightColors.Primary,
    onPrimary          = GirlLightColors.OnPrimary,
    primaryContainer   = GirlLightColors.PrimaryVariant,
    secondary          = GirlLightColors.Secondary,
    onSecondary        = GirlLightColors.OnSecondary,
    secondaryContainer = GirlLightColors.SecondaryVariant,
    background         = GirlLightColors.Background,
    onBackground       = GirlLightColors.OnBackground,
    surface            = GirlLightColors.Surface,
    onSurface          = GirlLightColors.OnSurface,
    surfaceVariant     = GirlLightColors.SurfaceVariant,
    error              = GirlLightColors.Error,
    onError            = GirlLightColors.OnError,
)

private val GirlDarkColorScheme = darkColorScheme(
    primary            = GirlDarkColors.Primary,
    onPrimary          = GirlDarkColors.OnPrimary,
    primaryContainer   = GirlDarkColors.PrimaryVariant,
    secondary          = GirlDarkColors.Secondary,
    onSecondary        = GirlDarkColors.OnSecondary,
    secondaryContainer = GirlDarkColors.SecondaryVariant,
    background         = GirlDarkColors.Background,
    onBackground       = GirlDarkColors.OnBackground,
    surface            = GirlDarkColors.Surface,
    onSurface          = GirlDarkColors.OnSurface,
    surfaceVariant     = GirlDarkColors.SurfaceVariant,
    error              = GirlDarkColors.Error,
    onError            = GirlDarkColors.OnError,
)

/**
 * Gender-specific color schemes for Boy theme
 */
private val BoyLightColorScheme = lightColorScheme(
    primary            = BoyLightColors.Primary,
    onPrimary          = BoyLightColors.OnPrimary,
    primaryContainer   = BoyLightColors.PrimaryVariant,
    secondary          = BoyLightColors.Secondary,
    onSecondary        = BoyLightColors.OnSecondary,
    secondaryContainer = BoyLightColors.SecondaryVariant,
    background         = BoyLightColors.Background,
    onBackground       = BoyLightColors.OnBackground,
    surface            = BoyLightColors.Surface,
    onSurface          = BoyLightColors.OnSurface,
    surfaceVariant     = BoyLightColors.SurfaceVariant,
    error              = BoyLightColors.Error,
    onError            = BoyLightColors.OnError,
)

private val BoyDarkColorScheme = darkColorScheme(
    primary            = BoyDarkColors.Primary,
    onPrimary          = BoyDarkColors.OnPrimary,
    primaryContainer   = BoyDarkColors.PrimaryVariant,
    secondary          = BoyDarkColors.Secondary,
    onSecondary        = BoyDarkColors.OnSecondary,
    secondaryContainer = BoyDarkColors.SecondaryVariant,
    background         = BoyDarkColors.Background,
    onBackground       = BoyDarkColors.OnBackground,
    surface            = BoyDarkColors.Surface,
    onSurface          = BoyDarkColors.OnSurface,
    surfaceVariant     = BoyDarkColors.SurfaceVariant,
    error              = BoyDarkColors.Error,
    onError            = BoyDarkColors.OnError,
)

/**
 * Neutral color schemes (for when gender is not set)
 */
private val NeutralLightColorScheme = lightColorScheme(
    primary            = NeutralLightColors.Primary,
    onPrimary          = NeutralLightColors.OnPrimary,
    primaryContainer   = NeutralLightColors.PrimaryVariant,
    secondary          = NeutralLightColors.Secondary,
    onSecondary        = NeutralLightColors.OnSecondary,
    secondaryContainer = NeutralLightColors.SecondaryVariant,
    background         = NeutralLightColors.Background,
    onBackground       = NeutralLightColors.OnBackground,
    surface            = NeutralLightColors.Surface,
    onSurface          = NeutralLightColors.OnSurface,
    error              = NeutralLightColors.Error,
    onError            = NeutralLightColors.OnError,
)

private val NeutralDarkColorScheme = darkColorScheme(
    primary            = NeutralDarkColors.Primary,
    onPrimary          = NeutralDarkColors.OnPrimary,
    primaryContainer   = NeutralDarkColors.PrimaryVariant,
    secondary          = NeutralDarkColors.Secondary,
    onSecondary        = NeutralDarkColors.OnSecondary,
    secondaryContainer = NeutralDarkColors.SecondaryVariant,
    background         = NeutralDarkColors.Background,
    onBackground       = NeutralDarkColors.OnBackground,
    surface            = NeutralDarkColors.Surface,
    onSurface          = NeutralDarkColors.OnSurface,
    error              = NeutralDarkColors.Error,
    onError            = NeutralDarkColors.OnError,
)

/**
 * Main theme composable with gender-specific theming support.
 *
 * @param genderTheme The gender theme to apply (GIRL, BOY, or NEUTRAL)
 * @param darkTheme   Whether to use dark theme — driven by the user's in-app
 *                    toggle (stored in PreferencesManager), NOT isSystemInDarkTheme()
 * @param content     The content to apply the theme to
 */
@Composable
fun BabyGrowthTheme(
    genderTheme: GenderTheme = GenderTheme.NEUTRAL,
    darkTheme  : Boolean     = isSystemInDarkTheme(),
    content    : @Composable () -> Unit
) {
    // Select color scheme based on gender and dark-mode flag
    val colorScheme = when (genderTheme) {
        GenderTheme.GIRL    -> if (darkTheme) GirlDarkColorScheme    else GirlLightColorScheme
        GenderTheme.BOY     -> if (darkTheme) BoyDarkColorScheme     else BoyLightColorScheme
        GenderTheme.NEUTRAL -> if (darkTheme) NeutralDarkColorScheme else NeutralLightColorScheme
    }

    // Get screen dimensions from platform-specific implementation
    val screenWidth  = getScreenWidth()
    val screenHeight = getScreenHeight()

    // Determine window size class based on width
    val windowSizeClass = getWindowSizeClass(screenWidth)

    // Create screen info
    val screenInfo = ScreenInfo(
        widthDp         = screenWidth,
        heightDp        = screenHeight,
        windowSizeClass = windowSizeClass
    )

    // Create responsive dimensions based on window size class
    val dimensions = Dimensions.create(windowSizeClass)

    // Create typography with custom fonts
    val typography = appTypography()

    // Provide all composition locals.
    // FIX: LocalIsDarkTheme is now provided here so that MaterialTheme.customColors
    //      reads the same darkTheme value used to pick the colorScheme above,
    //      instead of calling isSystemInDarkTheme() independently.
    CompositionLocalProvider(
        LocalDimensions  provides dimensions,
        LocalScreenInfo  provides screenInfo,
        LocalGenderTheme provides genderTheme,
        LocalIsDarkTheme provides darkTheme,        // ← FIX: added
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = typography,
            content     = content
        )
    }
}

/**
 * Extension property to access gender-specific custom colors.
 * Provides access to accent colors, glassmorphic overlays, etc.
 *
 * FIX: switched from `isSystemInDarkTheme()` to `LocalIsDarkTheme.current`
 *      so the correct dark/light palette is picked when the user toggles
 *      dark mode inside the app (without requiring an OS-level change).
 */
val MaterialTheme.customColors: CustomColors
    @Composable
    get() {
        val genderTheme = LocalGenderTheme.current
        // FIX: was isSystemInDarkTheme() — now reads the app-level toggle
        val isDark      = LocalIsDarkTheme.current

        return when (genderTheme) {
            GenderTheme.GIRL -> if (isDark) {
                CustomColors(
                    flowerPink          = GirlDarkColors.FlowerPink,
                    heartPink           = GirlDarkColors.HeartPink,
                    babyBlue            = GirlDarkColors.BabyPink,
                    accentGradientStart = GirlDarkColors.AccentGradientStart,
                    accentGradientEnd   = GirlDarkColors.AccentGradientEnd,
                    glassOverlay        = GirlDarkColors.GlassOverlay,
                    glassBackground     = GirlDarkColors.GlassBackground,
                    success             = GirlDarkColors.Success,
                    warning             = GirlDarkColors.Warning,
                    info                = GirlDarkColors.Info
                )
            } else {
                CustomColors(
                    flowerPink          = GirlLightColors.FlowerPink,
                    heartPink           = GirlLightColors.HeartPink,
                    babyBlue            = GirlLightColors.BabyPink,
                    accentGradientStart = GirlLightColors.AccentGradientStart,
                    accentGradientEnd   = GirlLightColors.AccentGradientEnd,
                    glassOverlay        = GirlLightColors.GlassOverlay,
                    glassBackground     = GirlLightColors.GlassBackground,
                    success             = GirlLightColors.Success,
                    warning             = GirlLightColors.Warning,
                    info                = GirlLightColors.Info
                )
            }

            GenderTheme.BOY -> if (isDark) {
                CustomColors(
                    flowerPink          = BoyDarkColors.SkyBlue,
                    heartPink           = BoyDarkColors.OceanBlue,
                    babyBlue            = BoyDarkColors.BabyBlue,
                    accentGradientStart = BoyDarkColors.AccentGradientStart,
                    accentGradientEnd   = BoyDarkColors.AccentGradientEnd,
                    glassOverlay        = BoyDarkColors.GlassOverlay,
                    glassBackground     = BoyDarkColors.GlassBackground,
                    success             = BoyDarkColors.Success,
                    warning             = BoyDarkColors.Warning,
                    info                = BoyDarkColors.Info
                )
            } else {
                CustomColors(
                    flowerPink          = BoyLightColors.SkyBlue,
                    heartPink           = BoyLightColors.OceanBlue,
                    babyBlue            = BoyLightColors.BabyBlue,
                    accentGradientStart = BoyLightColors.AccentGradientStart,
                    accentGradientEnd   = BoyLightColors.AccentGradientEnd,
                    glassOverlay        = BoyLightColors.GlassOverlay,
                    glassBackground     = BoyLightColors.GlassBackground,
                    success             = BoyLightColors.Success,
                    warning             = BoyLightColors.Warning,
                    info                = BoyLightColors.Info
                )
            }

            GenderTheme.NEUTRAL -> if (isDark) {
                CustomColors(
                    flowerPink          = NeutralDarkColors.Primary,
                    heartPink           = NeutralDarkColors.Secondary,
                    babyBlue            = NeutralDarkColors.PrimaryVariant,
                    accentGradientStart = NeutralDarkColors.AccentGradientStart,
                    accentGradientEnd   = NeutralDarkColors.AccentGradientEnd,
                    glassOverlay        = Color.White.copy(alpha = 0.1f),
                    glassBackground     = Color(0xFF2B2930).copy(alpha = 0.3f),
                    success             = Color(0xFF66BB6A),
                    warning             = Color(0xFFFFA726),
                    info                = Color(0xFF42A5F5)
                )
            } else {
                CustomColors(
                    flowerPink          = NeutralLightColors.Primary,
                    heartPink           = NeutralLightColors.Secondary,
                    babyBlue            = NeutralLightColors.PrimaryVariant,
                    accentGradientStart = NeutralLightColors.AccentGradientStart,
                    accentGradientEnd   = NeutralLightColors.AccentGradientEnd,
                    glassOverlay        = Color.White.copy(alpha = 0.15f),
                    glassBackground     = Color(0xFFF5F5F5).copy(alpha = 0.4f),
                    success             = Color(0xFF81C784),
                    warning             = Color(0xFFFFB74D),
                    info                = Color(0xFF64B5F6)
                )
            }
        }
    }

/**
 * Data class holding custom colors for the app.
 * These are gender-specific and theme-aware.
 */
data class CustomColors(
    val flowerPink         : Color,
    val heartPink          : Color,
    val babyBlue           : Color,
    val accentGradientStart: Color,
    val accentGradientEnd  : Color,
    val glassOverlay       : Color,
    val glassBackground    : Color,
    val success            : Color,
    val warning            : Color,
    val info               : Color
)