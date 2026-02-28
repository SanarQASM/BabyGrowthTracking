package org.example.project.babygrowthtrackingapplication.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.example.project.babygrowthtrackingapplication.theme.LocalDimensions
import org.example.project.babygrowthtrackingapplication.theme.customColors

/**
 * OTP/PIN Input Component with Individual Boxes
 *
 * Creates 5 individual boxes for entering a verification code
 * Each box shows one digit with glassmorphic styling
 *
 * @param value Current OTP value (string of digits)
 * @param onValueChange Callback when OTP value changes
 * @param length Number of digits (default: 5)
 * @param onComplete Callback when all digits are entered
 * @param modifier Modifier for the component
 */
@Composable
fun OtpTextField(
    value: String,
    onValueChange: (String) -> Unit,
    length: Int = 5,
    onComplete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val dimensions = LocalDimensions.current
    val customColors = MaterialTheme.customColors
    val focusRequester = remember { FocusRequester() }

    // Use TextFieldValue to control cursor position
    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(value, TextRange(value.length)))
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    // Update when external value changes
    LaunchedEffect(value) {
        if (textFieldValue.text != value) {
            textFieldValue = TextFieldValue(value, TextRange(value.length))
        }
    }

    Box(modifier = modifier) {
        // Invisible TextField that captures input
        BasicTextField(
            value = textFieldValue,
            onValueChange = { newValue ->
                // Only accept digits and limit to specified length
                val digits = newValue.text.filter { it.isDigit() }.take(length)
                textFieldValue = TextFieldValue(
                    text = digits,
                    selection = TextRange(digits.length)
                )
                onValueChange(digits)

                // Trigger onComplete when all digits entered
                if (digits.length == length) {
                    onComplete()
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.NumberPassword,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { if (value.length == length) onComplete() }
            ),
            cursorBrush = SolidColor(Color.Transparent), // Hide cursor
            modifier = Modifier
                .size(0.dp) // Make it invisible
                .focusRequester(focusRequester),
            decorationBox = { }
        )

        // Visible OTP boxes
        Row(
            horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall),
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(length) { index ->
                OtpDigitBox(
                    digit = value.getOrNull(index)?.toString() ?: "",
                    isFocused = index == value.length,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Single digit box for OTP input
 */
@Composable
private fun OtpDigitBox(
    digit: String,
    isFocused: Boolean,
    modifier: Modifier = Modifier
) {
    val dimensions = LocalDimensions.current
    val customColors = MaterialTheme.customColors

    // Glassmorphic background color
    val backgroundColor = if (digit.isNotEmpty()) {
        customColors.accentGradientStart.copy(alpha = 0.2f)
    } else {
        Color.White.copy(alpha = 0.1f)
    }

    // Border color
    val borderColor = when {
        isFocused -> customColors.accentGradientStart.copy(alpha = 0.8f)
        digit.isNotEmpty() -> customColors.accentGradientStart.copy(alpha = 0.5f)
        else -> Color.White.copy(alpha = 0.3f)
    }

    Box(
        modifier = modifier
            .aspectRatio(1f) // Make it square
            .clip(RoundedCornerShape(dimensions.buttonCornerRadius))
            .background(backgroundColor)
            .border(
                width = 2.dp,
                color = borderColor,
                shape = RoundedCornerShape(dimensions.buttonCornerRadius)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = digit,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
    }
}