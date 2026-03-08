package org.example.project.babygrowthtrackingapplication.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import org.example.project.babygrowthtrackingapplication.theme.LocalDimensions
import org.example.project.babygrowthtrackingapplication.theme.customColors

/**
 * iOS-Style Glassmorphic Buttons
 * Inspired by iPhone Control Center design with blur effects and translucent backgrounds
 *
 * REFACTORED:
 *  - All hardcoded `16.dp` shape corners  →  dimensions.buttonCornerRadius
 *  - All hardcoded `0.dp` elevations kept as-is (semantic zero — intentional)
 *  - `Color.White.copy(alpha = 0.2f)` overlay  →  customColors.glassOverlay
 *  - `Color.White.copy(alpha = 0.1f)` subtle overlay  →  customColors.glassOverlay
 */

/**
 * Primary Glassmorphic Button — Main action with iOS-style blur and gradient
 *
 * Usage:
 * ```
 * PrimaryButton(
 *     text = "Login",
 *     onClick = { /* action */ }
 * )
 * ```
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false
) {
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors

    Button(
        onClick = onClick,
        modifier = modifier.height(dimensions.buttonHeight),
        enabled = enabled && !loading,
        colors = ButtonDefaults.buttonColors(
            containerColor         = Color.Transparent,
            disabledContainerColor = Color.Transparent
        ),
        contentPadding = PaddingValues(),
        // WAS: RoundedCornerShape(16.dp)  →  dimensions.buttonCornerRadius
        shape = RoundedCornerShape(dimensions.buttonCornerRadius),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation  = dimensions.cardElevation * 0f, // semantic zero
            pressedElevation  = dimensions.cardElevation * 0f,
            disabledElevation = dimensions.cardElevation * 0f
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (enabled && !loading) {
                        Brush.verticalGradient(
                            colors = listOf(
                                customColors.accentGradientStart.copy(alpha = 0.85f),
                                customColors.accentGradientEnd.copy(alpha = 0.70f)
                            )
                        )
                    } else {
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                            )
                        )
                    },
                    // WAS: RoundedCornerShape(16.dp)  →  dimensions.buttonCornerRadius
                    shape = RoundedCornerShape(dimensions.buttonCornerRadius)
                ),
            contentAlignment = Alignment.Center
        ) {
            // Glassmorphic overlay
            // WAS: Color.White.copy(alpha = 0.2f)  →  customColors.glassOverlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        customColors.glassOverlay,
                        // WAS: RoundedCornerShape(16.dp)  →  dimensions.buttonCornerRadius
                        shape = RoundedCornerShape(dimensions.buttonCornerRadius)
                    )
            )

            if (loading) {
                CircularProgressIndicator(
                    color    = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(dimensions.iconMedium),
                    // WAS: strokeWidth = 2.dp  →  dimensions.spacingXSmall / 2
                    strokeWidth = dimensions.spacingXSmall / 2
                )
            } else {
                Text(
                    text       = text,
                    style      = MaterialTheme.typography.titleMedium,
                    color      = if (enabled) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * Secondary Glassmorphic Button — Translucent ghost style
 */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors

    Button(
        onClick        = onClick,
        modifier       = modifier.height(dimensions.buttonHeight),
        enabled        = enabled,
        colors         = ButtonDefaults.buttonColors(
            containerColor         = Color.Transparent,
            disabledContainerColor = Color.Transparent
        ),
        contentPadding = PaddingValues(),
        // WAS: RoundedCornerShape(16.dp)  →  dimensions.buttonCornerRadius
        shape          = RoundedCornerShape(dimensions.buttonCornerRadius),
        elevation      = ButtonDefaults.buttonElevation(
            defaultElevation = dimensions.cardElevation * 0f,
            pressedElevation = dimensions.cardElevation * 0f
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.2f)
                        )
                    ),
                    // WAS: RoundedCornerShape(16.dp)  →  dimensions.buttonCornerRadius
                    shape = RoundedCornerShape(dimensions.buttonCornerRadius)
                ),
            contentAlignment = Alignment.Center
        ) {
            // Subtle border effect
            // WAS: Color.White.copy(alpha = 0.1f)  →  customColors.glassOverlay (dark-mode safe)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        customColors.glassOverlay,
                        // WAS: RoundedCornerShape(16.dp)  →  dimensions.buttonCornerRadius
                        shape = RoundedCornerShape(dimensions.buttonCornerRadius)
                    )
            )

            Text(
                text       = text,
                style      = MaterialTheme.typography.titleMedium,
                color      = customColors.accentGradientStart,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Danger / Destructive Button — Warning gradient style
 */
@Composable
fun DangerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors

    Button(
        onClick        = onClick,
        modifier       = modifier.height(dimensions.buttonHeight),
        enabled        = enabled,
        colors         = ButtonDefaults.buttonColors(
            containerColor         = Color.Transparent,
            disabledContainerColor = Color.Transparent
        ),
        contentPadding = PaddingValues(),
        // WAS: RoundedCornerShape(16.dp)  →  dimensions.buttonCornerRadius
        shape          = RoundedCornerShape(dimensions.buttonCornerRadius),
        elevation      = ButtonDefaults.buttonElevation(
            defaultElevation = dimensions.cardElevation * 0f,
            pressedElevation = dimensions.cardElevation * 0f
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            customColors.warning.copy(alpha = 0.85f),
                            customColors.warning.copy(alpha = 0.70f)
                        )
                    ),
                    // WAS: RoundedCornerShape(16.dp)  →  dimensions.buttonCornerRadius
                    shape = RoundedCornerShape(dimensions.buttonCornerRadius)
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        customColors.glassOverlay,
                        // WAS: RoundedCornerShape(16.dp)  →  dimensions.buttonCornerRadius
                        shape = RoundedCornerShape(dimensions.buttonCornerRadius)
                    )
            )
            Text(
                text       = text,
                style      = MaterialTheme.typography.titleMedium,
                color      = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Text Button — Minimal glassmorphic button
 */
@Composable
fun AppTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    TextButton(
        onClick  = onClick,
        modifier = modifier,
        enabled  = enabled,
        colors   = ButtonDefaults.textButtonColors(
            contentColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Text(
            text       = text,
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Icon Button with Glassmorphic Background
 */
@Composable
fun GradientIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors

    IconButton(
        onClick  = onClick,
        modifier = modifier.size(dimensions.iconXLarge)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            customColors.accentGradientStart.copy(alpha = 0.85f),
                            customColors.accentGradientEnd.copy(alpha = 0.80f)
                        )
                    ),
                    // WAS: RoundedCornerShape(16.dp)  →  dimensions.buttonCornerRadius
                    shape = RoundedCornerShape(dimensions.buttonCornerRadius)
                ),
            contentAlignment = Alignment.Center
        ) {
            // WAS: Color.White.copy(alpha = 0.2f)  →  customColors.glassOverlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        customColors.glassOverlay,
                        shape = RoundedCornerShape(dimensions.buttonCornerRadius)
                    )
            )
            Icon(
                imageVector        = icon,
                contentDescription = contentDescription,
                tint               = MaterialTheme.colorScheme.onPrimary,
                modifier           = Modifier.size(dimensions.iconMedium)
            )
        }
    }
}

/**
 * Chip / Filter Button
 */
@Composable
fun ChipButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors

    FilterChip(
        selected = selected,
        onClick  = onClick,
        label    = {
            Text(
                text  = text,
                style = MaterialTheme.typography.labelMedium
            )
        },
        modifier = modifier,
        colors   = FilterChipDefaults.filterChipColors(
            selectedContainerColor      = customColors.accentGradientStart.copy(alpha = 0.85f),
            selectedLabelColor          = MaterialTheme.colorScheme.onPrimary,
            containerColor              = MaterialTheme.colorScheme.surface,
            labelColor                  = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(dimensions.buttonCornerRadius / 2)
    )
}

/**
 * Large Full-Width Button
 */
@Composable
fun LargeButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors

    Button(
        onClick        = onClick,
        modifier       = modifier.height(dimensions.buttonHeight),
        enabled        = enabled,
        colors         = ButtonDefaults.buttonColors(
            containerColor         = Color.Transparent,
            disabledContainerColor = Color.Transparent
        ),
        contentPadding = PaddingValues(),
        // WAS: RoundedCornerShape(16.dp)  →  dimensions.buttonCornerRadius
        shape          = RoundedCornerShape(dimensions.buttonCornerRadius)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            customColors.accentGradientStart.copy(alpha = 0.85f),
                            customColors.accentGradientEnd.copy(alpha = 0.70f)
                        )
                    ),
                    shape = RoundedCornerShape(dimensions.buttonCornerRadius)
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        customColors.glassOverlay,
                        shape = RoundedCornerShape(dimensions.buttonCornerRadius)
                    )
            )
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
            ) {
                icon?.let {
                    Icon(
                        imageVector        = it,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.onPrimary,
                        modifier           = Modifier.size(dimensions.iconMedium)
                    )
                }
                Text(
                    text       = text,
                    style      = MaterialTheme.typography.titleMedium,
                    color      = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}