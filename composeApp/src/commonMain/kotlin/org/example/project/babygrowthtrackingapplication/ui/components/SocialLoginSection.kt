package org.example.project.babygrowthtrackingapplication.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.painterResource
import babygrowthtrackingapplication.composeapp.generated.resources.Res
import babygrowthtrackingapplication.composeapp.generated.resources.*
import org.example.project.babygrowthtrackingapplication.theme.LocalDimensions

/**
 * Social Login Section with Divider and Social Buttons
 * Shows "-----or-----" divider followed by Google button
 *
 * REFACTORED:
 *  - 56.dp button size            →  dimensions.socialButtonSize
 *  - 24.dp icon size              →  dimensions.socialIconSize
 *  - 16.dp horizontal "or" padding →  dimensions.orDividerTextPaddingH
 *  - 1.dp divider line height     →  dimensions.dividerHeight
 */
@Composable
fun SocialLoginSection(
    onGoogleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dimensions = LocalDimensions.current

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Divider with "or" text
        OrDivider()

        Spacer(modifier = Modifier.height(dimensions.spacingLarge))

        // Social login buttons row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Google button
            SocialLoginButton(
                icon               = Res.drawable.ic_google,
                contentDescription = "Sign in with Google",
                backgroundColor    = Color.White,
                onClick            = onGoogleClick
            )
        }
    }
}

/**
 * Divider with "or" text in the middle — shows like: -----or-----
 */
@Composable
private fun OrDivider(
    modifier: Modifier = Modifier
) {
    val dimensions = LocalDimensions.current

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment   = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        // Left line
        // WAS: Modifier.height(1.dp)  →  dimensions.dividerHeight
        Box(
            modifier = Modifier
                .weight(1f)
                .height(dimensions.dividerHeight)
                .background(
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
        )

        // "or" text
        Text(
            text = "or",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            fontWeight = FontWeight.Medium,
            // WAS: Modifier.padding(horizontal = 16.dp)  →  dimensions.orDividerTextPaddingH
            modifier = Modifier.padding(horizontal = dimensions.orDividerTextPaddingH)
        )

        // Right line
        // WAS: Modifier.height(1.dp)  →  dimensions.dividerHeight
        Box(
            modifier = Modifier
                .weight(1f)
                .height(dimensions.dividerHeight)
                .background(
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
        )
    }
}

/**
 * Individual social login button with icon
 */
@Composable
private fun SocialLoginButton(
    icon: org.jetbrains.compose.resources.DrawableResource,
    contentDescription: String,
    backgroundColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dimensions = LocalDimensions.current

    Box(
        modifier = modifier
            // WAS: Modifier.size(56.dp)  →  dimensions.socialButtonSize
            .size(dimensions.socialButtonSize)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = contentDescription,
            // WAS: Modifier.size(24.dp)  →  dimensions.socialIconSize
            modifier = Modifier.size(dimensions.socialIconSize),
            tint = if (backgroundColor == Color.White) Color.Unspecified else Color.White
        )
    }
}