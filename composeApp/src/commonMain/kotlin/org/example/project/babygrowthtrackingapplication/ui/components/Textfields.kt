package org.example.project.babygrowthtrackingapplication.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import org.example.project.babygrowthtrackingapplication.theme.LocalDimensions
import org.example.project.babygrowthtrackingapplication.theme.customColors

/**
 * iOS-Style Glassmorphic Text Fields
 *
 * REFACTORED:
 *  - `RoundedCornerShape(16.dp)` → RoundedCornerShape(dimensions.buttonCornerRadius)
 *  - `padding(top = 4.dp, start = 4.dp)` → padding(top = dimensions.spacingXSmall, start = dimensions.spacingXSmall)
 *  - Explicit `Color.Transparent` kept — intentional, not a magic value
 */
@Composable
fun GlassmorphicTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation =
        androidx.compose.ui.text.input.VisualTransformation.None,
    singleLine: Boolean = true
) {
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
                        )
                    ),
                    // WAS: RoundedCornerShape(16.dp)  →  dimensions.buttonCornerRadius
                    shape = RoundedCornerShape(dimensions.buttonCornerRadius)
                )
                .border(
                    width = dimensions.spacingXSmall / 4,
                    color = if (isError)
                        MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                    else
                        customColors.accentGradientStart.copy(alpha = 0.3f),
                    // WAS: RoundedCornerShape(16.dp)  →  dimensions.buttonCornerRadius
                    shape = RoundedCornerShape(dimensions.buttonCornerRadius)
                )
        ) {
            // Glass overlay
            // WAS: Color.White.copy(alpha = 0.08f) — replicated via customColors.glassOverlay
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        customColors.glassOverlay,
                        // WAS: RoundedCornerShape(16.dp)  →  dimensions.buttonCornerRadius
                        shape = RoundedCornerShape(dimensions.buttonCornerRadius)
                    )
            )

            TextField(
                value                  = value,
                onValueChange          = onValueChange,
                placeholder            = {
                    Text(
                        text  = placeholder,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                },
                leadingIcon            = leadingIcon,
                trailingIcon           = trailingIcon,
                isError                = isError,
                keyboardOptions        = keyboardOptions,
                keyboardActions        = keyboardActions,
                visualTransformation   = visualTransformation,
                singleLine             = singleLine,
                textStyle              = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor    = Color.Transparent,
                    unfocusedContainerColor  = Color.Transparent,
                    disabledContainerColor   = Color.Transparent,
                    errorContainerColor      = Color.Transparent,
                    focusedIndicatorColor    = Color.Transparent,
                    unfocusedIndicatorColor  = Color.Transparent,
                    disabledIndicatorColor   = Color.Transparent,
                    errorIndicatorColor      = Color.Transparent,
                    cursorColor              = customColors.accentGradientStart,
                    errorCursorColor         = MaterialTheme.colorScheme.error
                ),
                // WAS: RoundedCornerShape(16.dp)  →  dimensions.buttonCornerRadius
                shape    = RoundedCornerShape(dimensions.buttonCornerRadius),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Error message
        if (isError && errorMessage != null) {
            Text(
                text     = errorMessage,
                style    = MaterialTheme.typography.labelSmall,
                color    = MaterialTheme.colorScheme.error,
                // WAS: padding(top = 4.dp, start = 4.dp)  →  dimensions.spacingXSmall
                modifier = Modifier.padding(
                    top   = dimensions.spacingXSmall,
                    start = dimensions.spacingXSmall
                )
            )
        }
    }
}