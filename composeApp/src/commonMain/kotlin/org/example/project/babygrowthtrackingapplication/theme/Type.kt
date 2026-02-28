package org.example.project.babygrowthtrackingapplication.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import babygrowthtrackingapplication.composeapp.generated.resources.Res
import babygrowthtrackingapplication.composeapp.generated.resources.*

/**
 * Poppins font family - friendly, modern, and perfect for baby tracking app
 * Falls back to system sans-serif if fonts fail to load
 */
@Composable
fun poppinsFontFamily() = FontFamily(
    Font(Res.font.poppins_light, FontWeight.Light),
    Font(Res.font.poppins_regular, FontWeight.Normal),
    Font(Res.font.poppins_semibold, FontWeight.Medium),
    Font(Res.font.poppins_semibold, FontWeight.SemiBold),
    Font(Res.font.poppins_bold, FontWeight.Bold),
)

/**
 * Alternative: Nunito font family - rounded and friendly
 * Uncomment to use instead of Poppins
 */
// @Composable
// fun nunitoFontFamily() = FontFamily(
//     Font(Res.font.nunito_light, FontWeight.Light),
//     Font(Res.font.nunito_regular, FontWeight.Normal),
//     Font(Res.font.nunito_medium, FontWeight.Medium),
//     Font(Res.font.nunito_semibold, FontWeight.SemiBold),
//     Font(Res.font.nunito_bold, FontWeight.Bold),
// )

/**
 * App Typography with Poppins font
 * Provides a warm, friendly, and modern look perfect for a baby tracking app
 */
@Composable
fun appTypography(): Typography {
    val fontFamily = poppinsFontFamily()

    return Typography(
        // Display styles - Used for large, impactful text
        displayLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 57.sp,
            lineHeight = 64.sp,
            letterSpacing = (-0.25).sp
        ),
        displayMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 45.sp,
            lineHeight = 52.sp,
            letterSpacing = 0.sp
        ),
        displaySmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 36.sp,
            lineHeight = 44.sp,
            letterSpacing = 0.sp
        ),

        // Headline styles - Used for important headings
        headlineLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            lineHeight = 40.sp,
            letterSpacing = 0.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.SemiBold,  // Slightly lighter for better readability
            fontSize = 28.sp,
            lineHeight = 36.sp,
            letterSpacing = 0.sp
        ),
        headlineSmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 24.sp,
            lineHeight = 32.sp,
            letterSpacing = 0.sp
        ),

        // Title styles - Used for section headers and card titles
        titleLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 22.sp,
            lineHeight = 28.sp,
            letterSpacing = 0.sp
        ),
        titleMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.15.sp
        ),
        titleSmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.1.sp
        ),

        // Body styles - Used for main content text
        bodyLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.5.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.25.sp
        ),
        bodySmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.4.sp
        ),

        // Label styles - Used for buttons, tabs, and UI elements
        labelLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.1.sp
        ),
        labelMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp
        ),
        labelSmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp
        )
    )
}