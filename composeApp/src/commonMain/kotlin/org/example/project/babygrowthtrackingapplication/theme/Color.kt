package org.example.project.babygrowthtrackingapplication.theme

import androidx.compose.ui.graphics.Color

/**
 * Gender-specific color palettes for Baby Growth Tracking Application
 * Supports both Light and Dark modes with beautiful glassmorphic colors
 * 🎨 CUSTOM NEUTRAL COLORS: Pink-Purple (#E56399) & Blue (#3279B8)
 */

// ==================== GIRL THEME COLORS ====================

object GirlLightColors {
    // Primary colors - Soft pink palette
    val Primary = Color(0xFFFFB5D7) // Main pink (#ffb5d7)
    val PrimaryVariant = Color(0xFFFF8CB8) // Deeper pink
    val Secondary = Color(0xFFFFD4E5) // Light blush pink
    val SecondaryVariant = Color(0xFFFFC1D9) // Soft rose

    // Background and Surface
    val Background = Color(0xFFFFF5F9) // Very light pink tint
    val Surface = Color(0xFFFFFFFF) // Pure white
    val SurfaceVariant = Color(0xFFFFF0F5) // Lavender blush

    // Status colors
    val Error = Color(0xFFD32F2F)
    val Success = Color(0xFF81C784)
    val Warning = Color(0xFFFFB74D)
    val Info = Color(0xFF64B5F6)

    // On colors
    val OnPrimary = Color(0xFFFFFFFF)
    val OnSecondary = Color(0xFF4D3A47)
    val OnBackground = Color(0xFF2D1B24)
    val OnSurface = Color(0xFF2D1B24)
    val OnError = Color(0xFFFFFFFF)

    // Custom accent colors
    val FlowerPink = Color(0xFFFFC9DD) // Lighter flower pink
    val HeartPink = Color(0xFFFF85B3) // Vibrant heart pink
    val BabyPink = Color(0xFFFFE0ED) // Softest pink
    val AccentGradientStart = Color(0xFFFFB5D7) // Main pink
    val AccentGradientEnd = Color(0xFFFFD4E5) // Soft blush

    // Glassmorphic overlay colors
    val GlassOverlay = Color(0xFFFFFFFF).copy(alpha = 0.15f)
    val GlassBackground = Color(0xFFFFE8F0).copy(alpha = 0.4f)
}

object GirlDarkColors {
    // Primary colors - Rich pink palette for dark mode
    val Primary = Color(0xFFFFB5D7) // Keep main pink bright
    val PrimaryVariant = Color(0xFFFF8CB8) // Deeper pink
    val Secondary = Color(0xFFFF99C2) // Medium pink
    val SecondaryVariant = Color(0xFFFF70A6) // Vibrant pink

    // Background and Surface - Dark with pink undertones
    val Background = Color(0xFF1A1419) // Very dark with purple-pink hint
    val Surface = Color(0xFF251D23) // Dark surface with pink undertone
    val SurfaceVariant = Color(0xFF2F252C) // Slightly lighter surface

    // Status colors (adjusted for dark mode)
    val Error = Color(0xFFEF5350)
    val Success = Color(0xFF66BB6A)
    val Warning = Color(0xFFFFA726)
    val Info = Color(0xFF42A5F5)

    // On colors
    val OnPrimary = Color(0xFF4D0026)
    val OnSecondary = Color(0xFF2D0013)
    val OnBackground = Color(0xFFFFE8F0)
    val OnSurface = Color(0xFFFFE8F0)
    val OnError = Color(0xFF000000)

    // Custom accent colors
    val FlowerPink = Color(0xFFD147A1)
    val HeartPink = Color(0xFFFF5A9E)
    val BabyPink = Color(0xFFFF8CB8)
    val AccentGradientStart = Color(0xFFFFB5D7)
    val AccentGradientEnd = Color(0xFFD147A1)

    // Glassmorphic overlay colors
    val GlassOverlay = Color(0xFFFFFFFF).copy(alpha = 0.1f)
    val GlassBackground = Color(0xFF3D2F38).copy(alpha = 0.3f)
}

// ==================== BOY THEME COLORS ====================

object BoyLightColors {
    // Primary colors - Soft blue palette
    val Primary = Color(0xFF9FC5E8) // Main blue (#9fc5e8)
    val PrimaryVariant = Color(0xFF6FA8DC) // Deeper blue
    val Secondary = Color(0xFFC9DAF0) // Light sky blue
    val SecondaryVariant = Color(0xFFB4D7ED) // Soft azure

    // Background and Surface
    val Background = Color(0xFFF5F9FC) // Very light blue tint
    val Surface = Color(0xFFFFFFFF) // Pure white
    val SurfaceVariant = Color(0xFFF0F6FA) // Alice blue

    // Status colors
    val Error = Color(0xFFD32F2F)
    val Success = Color(0xFF81C784)
    val Warning = Color(0xFFFFB74D)
    val Info = Color(0xFF64B5F6)

    // On colors
    val OnPrimary = Color(0xFFFFFFFF)
    val OnSecondary = Color(0xFF2C3E50)
    val OnBackground = Color(0xFF1A2634)
    val OnSurface = Color(0xFF1A2634)
    val OnError = Color(0xFFFFFFFF)

    // Custom accent colors
    val SkyBlue = Color(0xFFD4E7F5) // Lighter sky blue
    val OceanBlue = Color(0xFF5DA3D5) // Vibrant ocean blue
    val BabyBlue = Color(0xFFE0EEF7) // Softest blue
    val AccentGradientStart = Color(0xFF9FC5E8) // Main blue
    val AccentGradientEnd = Color(0xFFC9DAF0) // Soft sky

    // Glassmorphic overlay colors
    val GlassOverlay = Color(0xFFFFFFFF).copy(alpha = 0.15f)
    val GlassBackground = Color(0xFFE8F2F7).copy(alpha = 0.4f)
}

object BoyDarkColors {
    // Primary colors - Rich blue palette for dark mode
    val Primary = Color(0xFF9FC5E8) // Keep main blue bright
    val PrimaryVariant = Color(0xFF6FA8DC) // Deeper blue
    val Secondary = Color(0xFF7BB3E0) // Medium blue
    val SecondaryVariant = Color(0xFF5A9DD1) // Vibrant blue

    // Background and Surface - Dark with blue undertones
    val Background = Color(0xFF141A1F) // Very dark with blue hint
    val Surface = Color(0xFF1D252C) // Dark surface with blue undertone
    val SurfaceVariant = Color(0xFF252F36) // Slightly lighter surface

    // Status colors (adjusted for dark mode)
    val Error = Color(0xFFEF5350)
    val Success = Color(0xFF66BB6A)
    val Warning = Color(0xFFFFA726)
    val Info = Color(0xFF42A5F5)

    // On colors
    val OnPrimary = Color(0xFF002952)
    val OnSecondary = Color(0xFF001A33)
    val OnBackground = Color(0xFFE8F1F7)
    val OnSurface = Color(0xFFE8F1F7)
    val OnError = Color(0xFF000000)

    // Custom accent colors
    val SkyBlue = Color(0xFF5191C7)
    val OceanBlue = Color(0xFF4A90D9)
    val BabyBlue = Color(0xFF7BB3E0)
    val AccentGradientStart = Color(0xFF9FC5E8)
    val AccentGradientEnd = Color(0xFF5191C7)

    // Glassmorphic overlay colors
    val GlassOverlay = Color(0xFFFFFFFF).copy(alpha = 0.1f)
    val GlassBackground = Color(0xFF2F3D47).copy(alpha = 0.3f)
}

// ==================== NEUTRAL/DEFAULT COLORS ====================
// For when gender is not set or app first launch
// 🎨 CUSTOM COLORS: Pink-Purple (#E56399) for Light, Blue (#3279B8) for Dark!

object NeutralLightColors {
    // Primary colors - Pink-Purple palette (#E56399) ⭐
    val Primary = Color(0xFFE56399) // Your custom pink-purple! ⭐
    val PrimaryVariant = Color(0xFFD94D85) // Deeper pink-purple
    val Secondary = Color(0xFFF087B3) // Light pink-purple
    val SecondaryVariant = Color(0xFFED70A6) // Medium pink-purple

    // Background and Surface
    val Background = Color(0xFFFFF5F9) // Very light pink tint
    val Surface = Color(0xFFFFFFFF) // Pure white
    val SurfaceVariant = Color(0xFFFFF0F6) // Pale pink

    // Status colors
    val Error = Color(0xFFD32F2F)
    val Success = Color(0xFF81C784)
    val Warning = Color(0xFFFFB74D)
    val Info = Color(0xFF64B5F6)

    // On colors
    val OnPrimary = Color(0xFFFFFFFF) // White text on pink-purple
    val OnSecondary = Color(0xFFFFFFFF)
    val OnBackground = Color(0xFF2D1B24)
    val OnSurface = Color(0xFF2D1B24)
    val OnError = Color(0xFFFFFFFF)

    // Custom accent colors - Pink-Purple theme
    val RosePink = Color(0xFFF5A1C2) // Soft rose
    val VibrantPink = Color(0xFFDB5088) // Vibrant pink-purple
    val BabyRose = Color(0xFFFAD1E3) // Softest pink
    val AccentGradientStart = Color(0xFFE56399) // Your pink-purple ⭐
    val AccentGradientEnd = Color(0xFFF087B3) // Light pink-purple

    // Glassmorphic overlay colors
    val GlassOverlay = Color(0xFFFFFFFF).copy(alpha = 0.15f)
    val GlassBackground = Color(0xFFFCE4F0).copy(alpha = 0.4f)
}

object NeutralDarkColors {
    // Primary colors - Blue palette (#3279B8) for dark mode ⭐
    val Primary = Color(0xFF3279B8) // Your custom blue! ⭐
    val PrimaryVariant = Color(0xFF2563A3) // Deeper blue
    val Secondary = Color(0xFF5294D1) // Light blue
    val SecondaryVariant = Color(0xFF4286C4) // Medium blue

    // Background and Surface - Dark with blue undertones
    val Background = Color(0xFF141A1F) // Very dark with blue hint
    val Surface = Color(0xFF1D252C) // Dark surface with blue undertone
    val SurfaceVariant = Color(0xFF252F36) // Slightly lighter surface

    // Status colors (adjusted for dark mode)
    val Error = Color(0xFFEF5350)
    val Success = Color(0xFF66BB6A)
    val Warning = Color(0xFFFFA726)
    val Info = Color(0xFF42A5F5)

    // On colors
    val OnPrimary = Color(0xFFFFFFFF) // White text on blue
    val OnSecondary = Color(0xFFFFFFFF)
    val OnBackground = Color(0xFFE8F1F7)
    val OnSurface = Color(0xFFE8F1F7)
    val OnError = Color(0xFF000000)

    // Custom accent colors - Blue theme
    val SkyBlue = Color(0xFF5A9FD4) // Bright sky blue
    val DeepBlue = Color(0xFF2968A8) // Deep blue
    val SoftBlue = Color(0xFF72A8D6) // Soft blue
    val AccentGradientStart = Color(0xFF3279B8) // Your blue ⭐
    val AccentGradientEnd = Color(0xFF5294D1) // Light blue

    // Glassmorphic overlay colors
    val GlassOverlay = Color(0xFFFFFFFF).copy(alpha = 0.1f)
    val GlassBackground = Color(0xFF2F3D47).copy(alpha = 0.3f)
}