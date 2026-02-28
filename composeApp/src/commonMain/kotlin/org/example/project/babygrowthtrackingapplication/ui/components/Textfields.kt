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
 * Primary Glassmorphic TextField - Main input with iOS-style blur
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
    val dimensions = LocalDimensions.current
    val customColors = MaterialTheme.customColors

    Column(modifier = modifier) {
        // Label
        label?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 6.dp)
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
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            // Glassmorphic overlay
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Color.White.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(16.dp)
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
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    errorContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent,
                    cursorColor = customColors.accentGradientStart,
                    errorCursorColor = MaterialTheme.colorScheme.error
                ),
                shape = RoundedCornerShape(16.dp)
            )
        }

        // Error message
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }
    }
}

/**
 * Outlined Glassmorphic TextField - Alternative style with subtle border
 *
 * Usage:
 * ```
 * OutlinedGlassmorphicTextField(
 *     value = password,
 *     onValueChange = { password = it },
 *     label = "Password",
 *     placeholder = "Enter password"
 * )
 * ```
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
    val dimensions = LocalDimensions.current
    val customColors = MaterialTheme.customColors

    Column(modifier = modifier) {
        // Label
        label?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 6.dp)
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
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f),
                disabledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.1f),
                errorContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                focusedBorderColor = customColors.accentGradientStart.copy(alpha = 0.5f),
                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                disabledBorderColor = Color.White.copy(alpha = 0.1f),
                errorBorderColor = MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
                cursorColor = customColors.accentGradientStart,
                errorCursorColor = MaterialTheme.colorScheme.error
            ),
            shape = RoundedCornerShape(16.dp)
        )

        // Error message
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }
    }
}

/**
 * Search TextField with iOS-style translucent background
 *
 * Usage:
 * ```
 * SearchTextField(
 *     value = searchQuery,
 *     onValueChange = { searchQuery = it },
 *     placeholder = "Search..."
 * )
 * ```
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
    val customColors = MaterialTheme.customColors

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
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        // Glassmorphic overlay
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Color.White.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(20.dp)
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
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                cursorColor = customColors.accentGradientStart
            ),
            shape = RoundedCornerShape(20.dp)
        )
    }
}

/**
 * Multi-line Glassmorphic TextField for longer text input
 *
 * Usage:
 * ```
 * MultiLineGlassmorphicTextField(
 *     value = notes,
 *     onValueChange = { notes = it },
 *     label = "Notes",
 *     placeholder = "Enter your notes here...",
 *     minLines = 4
 * )
 * ```
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
    val dimensions = LocalDimensions.current
    val customColors = MaterialTheme.customColors

    Column(modifier = modifier) {
        // Label
        label?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 6.dp)
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
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            // Glassmorphic overlay
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Color.White.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(16.dp)
                    )
            )

            TextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
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
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    errorContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent,
                    cursorColor = customColors.accentGradientStart,
                    errorCursorColor = MaterialTheme.colorScheme.error
                ),
                shape = RoundedCornerShape(16.dp)
            )
        }

        // Error message
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }
    }
}