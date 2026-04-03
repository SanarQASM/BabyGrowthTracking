package org.example.project.babygrowthtrackingapplication.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import org.example.project.babygrowthtrackingapplication.theme.LocalDimensions
import org.example.project.babygrowthtrackingapplication.theme.customColors

/**
 * iOS-Style Glassmorphic Text Input Fields
 * Matches the translucent, blurred aesthetic of iPhone Control Center
 */

/**
 * Primary Glassmorphic TextField — Main input with iOS-style blur
 *
 * Usage:
 * ```
 * GlassmorphicTextField(
 *     value = email,
 *     onValueChange = { email = it },
 *     label = "Email",
 *     placeholder = "Enter your email"
 * )
 * ```
 */
@Composable
fun GlassmorphicTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String = "",
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors

    // WAS: RoundedCornerShape(16.dp)  →  dimensions.textFieldCornerRadius
    val fieldShape = RoundedCornerShape(dimensions.textFieldCornerRadius)

    Column(modifier = modifier) {
        // Label
        label?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium,
                // WAS: Modifier.padding(bottom = 6.dp)  →  dimensions.textFieldLabelBottomPadding
                modifier = Modifier.padding(bottom = dimensions.textFieldLabelBottomPadding)
            )
        }

        // Text Field with glassmorphic background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
                        )
                    ),
                    // WAS: RoundedCornerShape(16.dp)  →  fieldShape
                    shape = fieldShape
                )
        ) {
            // Glassmorphic overlay
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        // WAS: Color.White.copy(alpha = 0.1f)  →  customColors.glassOverlay
                        customColors.glassOverlay,
                        // WAS: RoundedCornerShape(16.dp)  →  fieldShape
                        shape = fieldShape
                    )
            )

            TextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = placeholder,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                },
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon,
                isError = isError,
                enabled = enabled,
                readOnly = readOnly,
                singleLine = singleLine,
                maxLines = maxLines,
                visualTransformation = visualTransformation,
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                colors = TextFieldDefaults.colors(
                    focusedTextColor        = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor      = MaterialTheme.colorScheme.onSurface,
                    disabledTextColor       = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    focusedContainerColor   = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor  = Color.Transparent,
                    errorContainerColor     = Color.Transparent,
                    focusedIndicatorColor   = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor  = Color.Transparent,
                    errorIndicatorColor     = Color.Transparent,
                    cursorColor             = customColors.accentGradientStart,
                    errorCursorColor        = MaterialTheme.colorScheme.error
                ),
                // WAS: RoundedCornerShape(16.dp)  →  fieldShape
                shape = fieldShape
            )
        }

        // Error message
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
                // WAS: Modifier.padding(top = 4.dp, start = 4.dp)  →  spacingXSmall
                modifier = Modifier.padding(
                    top   = dimensions.spacingXSmall,
                    start = dimensions.spacingXSmall
                )
            )
        }
    }
}

/**
 * Outlined Glassmorphic TextField — Alternative style with subtle border
 */
@Composable
fun OutlinedGlassmorphicTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String = "",
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors

    // WAS: RoundedCornerShape(16.dp)  →  dimensions.textFieldCornerRadius
    val fieldShape = RoundedCornerShape(dimensions.textFieldCornerRadius)

    Column(modifier = modifier) {
        // Label
        label?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium,
                // WAS: Modifier.padding(bottom = 6.dp)  →  dimensions.textFieldLabelBottomPadding
                modifier = Modifier.padding(bottom = dimensions.textFieldLabelBottomPadding)
            )
        }

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = placeholder,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            },
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            isError = isError,
            enabled = enabled,
            readOnly = readOnly,
            singleLine = singleLine,
            maxLines = maxLines,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor        = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor      = MaterialTheme.colorScheme.onSurface,
                disabledTextColor       = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                focusedContainerColor   = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f),
                disabledContainerColor  = MaterialTheme.colorScheme.surface.copy(alpha = 0.1f),
                errorContainerColor     = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                focusedBorderColor      = customColors.accentGradientStart.copy(alpha = 0.5f),
                // WAS: Color.White.copy(alpha = 0.2f)  →  customColors.glassOverlay
                unfocusedBorderColor    = customColors.glassOverlay,
                // WAS: Color.White.copy(alpha = 0.1f)  →  customColors.glassOverlay (at lower alpha)
                disabledBorderColor     = customColors.glassOverlay.copy(alpha = 0.5f),
                errorBorderColor        = MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
                cursorColor             = customColors.accentGradientStart,
                errorCursorColor        = MaterialTheme.colorScheme.error
            ),
            // WAS: RoundedCornerShape(16.dp)  →  fieldShape
            shape = fieldShape
        )

        // Error message
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
                // WAS: Modifier.padding(top = 4.dp, start = 4.dp)  →  spacingXSmall
                modifier = Modifier.padding(
                    top   = dimensions.spacingXSmall,
                    start = dimensions.spacingXSmall
                )
            )
        }
    }
}

/**
 * Search TextField with iOS-style translucent background
 */
@Composable
fun SearchTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search...",
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    enabled: Boolean = true
) {
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors

    // WAS: RoundedCornerShape(20.dp)  →  dimensions.searchFieldCornerRadius
    val fieldShape = RoundedCornerShape(dimensions.searchFieldCornerRadius)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
                    )
                ),
                // WAS: RoundedCornerShape(20.dp)  →  fieldShape
                shape = fieldShape
            )
    ) {
        // Glassmorphic overlay
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    // WAS: Color.White.copy(alpha = 0.1f)  →  customColors.glassOverlay
                    customColors.glassOverlay,
                    // WAS: RoundedCornerShape(20.dp)  →  fieldShape
                    shape = fieldShape
                )
        )

        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = placeholder,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            },
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            enabled = enabled,
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedTextColor        = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor      = MaterialTheme.colorScheme.onSurface,
                disabledTextColor       = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                focusedContainerColor   = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor  = Color.Transparent,
                focusedIndicatorColor   = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor  = Color.Transparent,
                cursorColor             = customColors.accentGradientStart
            ),
            // WAS: RoundedCornerShape(20.dp)  →  fieldShape
            shape = fieldShape
        )
    }
}

/**
 * Multi-line Glassmorphic TextField for longer text input
 */
@Composable
fun MultiLineGlassmorphicTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String = "",
    minLines: Int = 3,
    maxLines: Int = 6,
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true
) {
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors

    // WAS: RoundedCornerShape(16.dp)  →  dimensions.textFieldCornerRadius
    val fieldShape = RoundedCornerShape(dimensions.textFieldCornerRadius)

    Column(modifier = modifier) {
        // Label
        label?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium,
                // WAS: Modifier.padding(bottom = 6.dp)  →  dimensions.textFieldLabelBottomPadding
                modifier = Modifier.padding(bottom = dimensions.textFieldLabelBottomPadding)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
                        )
                    ),
                    // WAS: RoundedCornerShape(16.dp)  →  fieldShape
                    shape = fieldShape
                )
        ) {
            // Glassmorphic overlay
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        // WAS: Color.White.copy(alpha = 0.1f)  →  customColors.glassOverlay
                        customColors.glassOverlay,
                        // WAS: RoundedCornerShape(16.dp)  →  fieldShape
                        shape = fieldShape
                    )
            )

            TextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    // WAS: .heightIn(min = (minLines * 24).dp) — kept as dynamic expression
                    .heightIn(min = (minLines * 24).dp),
                placeholder = {
                    Text(
                        text = placeholder,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                },
                isError = isError,
                enabled = enabled,
                singleLine = false,
                minLines = minLines,
                maxLines = maxLines,
                colors = TextFieldDefaults.colors(
                    focusedTextColor        = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor      = MaterialTheme.colorScheme.onSurface,
                    disabledTextColor       = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    focusedContainerColor   = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor  = Color.Transparent,
                    errorContainerColor     = Color.Transparent,
                    focusedIndicatorColor   = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor  = Color.Transparent,
                    errorIndicatorColor     = Color.Transparent,
                    cursorColor             = customColors.accentGradientStart,
                    errorCursorColor        = MaterialTheme.colorScheme.error
                ),
                // WAS: RoundedCornerShape(16.dp)  →  fieldShape
                shape = fieldShape
            )
        }

        // Error message
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
                // WAS: Modifier.padding(top = 4.dp, start = 4.dp)  →  spacingXSmall
                modifier = Modifier.padding(
                    top   = dimensions.spacingXSmall,
                    start = dimensions.spacingXSmall
                )
            )
        }
    }
}