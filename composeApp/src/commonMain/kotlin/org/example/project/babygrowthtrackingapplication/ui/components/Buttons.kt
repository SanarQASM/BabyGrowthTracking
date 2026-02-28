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
import androidx.compose.ui.unit.dp
import org.example.project.babygrowthtrackingapplication.theme.LocalDimensions
import org.example.project.babygrowthtrackingapplication.theme.customColors

/**
 * iOS-Style Glassmorphic Buttons
 * Inspired by iPhone Control Center design with blur effects and translucent backgrounds
 */

/**
 * Primary Glassmorphic Button - Main action with iOS-style blur and gradient
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
    val dimensions = LocalDimensions.current
    val customColors = MaterialTheme.customColors

    Button(
        onClick = onClick,
        modifier = modifier
            .height(dimensions.buttonHeight),
        enabled = enabled && !loading,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent
        ),
        contentPadding = PaddingValues(),
        shape = RoundedCornerShape(16.dp), // Less corner radius as requested
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            disabledElevation = 0.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (enabled && !loading) {
                        // iOS-style translucent gradient with blur effect
                        Brush.verticalGradient(
                            colors = listOf(
                                customColors.accentGradientStart.copy(alpha = 0.85f),
                                customColors.accentGradientEnd.copy(alpha = 0.75f)
                            )
                        )
                    } else {
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Gray.copy(alpha = 0.3f),
                                Color.Gray.copy(alpha = 0.2f)
                            )
                        )
                    },
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            // Background blur effect layer
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(16.dp)
                    )
            )

            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * Secondary Glassmorphic Button - Alternative action with translucent outline
 *
 * Usage:
 * ```
 * SecondaryButton(
 *     text = "Cancel",
 *     onClick = { /* action */ }
 * )
 * ```
 */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val dimensions = LocalDimensions.current
    val customColors = MaterialTheme.customColors

    Button(
        onClick = onClick,
        modifier = modifier
            .height(dimensions.buttonHeight),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent
        ),
        contentPadding = PaddingValues(),
        shape = RoundedCornerShape(16.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    // Translucent background like iOS
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.2f)
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            // Subtle border effect
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Color.White.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(16.dp)
                    )
            )

            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                color = customColors.accentGradientStart,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Text Button - Minimal glassmorphic button
 *
 * Usage:
 * ```
 * AppTextButton(
 *     text = "Skip",
 *     onClick = { /* action */ }
 * )
 * ```
 */
@Composable
fun AppTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.textButtonColors(
            contentColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Icon Button with Glassmorphic Background
 *
 * Usage:
 * ```
 * GradientIconButton(
 *     icon = Icons.Default.Add,
 *     contentDescription = "Add milestone",
 *     onClick = { /* action */ }
 * )
 * ```
 */
@Composable
fun GradientIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val dimensions = LocalDimensions.current
    val customColors = MaterialTheme.customColors

    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(dimensions.iconLarge + 16.dp),
        enabled = enabled
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            customColors.accentGradientStart.copy(alpha = 0.85f),
                            customColors.accentGradientEnd.copy(alpha = 0.75f)
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            // Overlay for glassmorphic effect
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(16.dp)
                    )
            )

            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = Color.White,
                modifier = Modifier.size(dimensions.iconMedium)
            )
        }
    }
}

/**
 * Chip Button - iOS-style pill button
 *
 * Usage:
 * ```
 * ChipButton(
 *     text = "0-3 months",
 *     selected = true,
 *     onClick = { /* action */ }
 * )
 * ```
 */
@Composable
fun ChipButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val customColors = MaterialTheme.customColors

    Button(
        onClick = onClick,
        modifier = modifier.height(36.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(18.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp
        )
    ) {
        Box(
            modifier = Modifier
                .background(
                    if (selected) {
                        Brush.verticalGradient(
                            colors = listOf(
                                customColors.accentGradientStart.copy(alpha = 0.85f),
                                customColors.accentGradientEnd.copy(alpha = 0.75f)
                            )
                        )
                    } else {
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.2f)
                            )
                        )
                    },
                    shape = RoundedCornerShape(18.dp)
                )
        ) {
            // Glassmorphic overlay
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Color.White.copy(alpha = if (selected) 0.15f else 0.1f),
                        shape = RoundedCornerShape(18.dp)
                    )
            )
        }

        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

/**
 * Floating Action Button with Glassmorphic gradient
 *
 * Usage:
 * ```
 * GradientFAB(
 *     icon = Icons.Default.Add,
 *     contentDescription = "Add new",
 *     onClick = { /* action */ }
 * )
 * ```
 */

@Composable
fun GradientFAB(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    expanded: Boolean = false,
    text: String? = null
) {
    val customColors = MaterialTheme.customColors

    if (expanded && text != null) {
        ExtendedFloatingActionButton(
            onClick = onClick,
            modifier = modifier,
            containerColor = Color.Transparent,
            contentColor = Color.White
        ) {
            Box(
                modifier = Modifier
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                customColors.accentGradientStart.copy(alpha = 0.9f),
                                customColors.accentGradientEnd.copy(alpha = 0.8f)
                            )
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Glassmorphic layer
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(16.dp)
                        )
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = contentDescription
                    )
                    Text(text = text)
                }
            }
        }
    } else {
        FloatingActionButton(
            onClick = onClick,
            modifier = modifier,
            containerColor = Color.Transparent,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                customColors.accentGradientStart.copy(alpha = 0.9f),
                                customColors.accentGradientEnd.copy(alpha = 0.8f)
                            )
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Glassmorphic overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(16.dp)
                        )
                )

                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription
                )
            }
        }
    }
}

/**
 * Large Full-Width Button
 *
 * Usage:
 * ```
 * LargeButton(
 *     text = "Get Started",
 *     onClick = { /* action */ }
 * )
 * ```
 */
@Composable
fun LargeButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    PrimaryButton(
        text = text,
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled
    )
}

/**
 * Danger/Delete Button with iOS-style translucent red
 *
 * Usage:
 * ```
 * DangerButton(
 *     text = "Delete Account",
 *     onClick = { /* action */ }
 * )
 * ```
 */
@Composable
fun DangerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val dimensions = LocalDimensions.current

    Button(
        onClick = onClick,
        modifier = modifier.height(dimensions.buttonHeight),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFEF5350).copy(alpha = 0.85f),
                            Color(0xFFE53935).copy(alpha = 0.75f)
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            // Glassmorphic overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(16.dp)
                    )
            )

            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/*
Button Type            Use For                    Example
PrimaryButton          Main actions              Save, Submit, Continue
SecondaryButton        Alternative actions       Cancel, Back, Skip
AppTextButton          Minor actions             Learn More, Skip
DangerButton           Destructive actions       Delete, Remove
GradientIconButton     Icon actions              Add, Edit, Share
ChipButton             Filters, selections       Categories, Tags
GradientFAB            Floating actions          Add, Create
 */