// File: composeApp/src/commonMain/kotlin/org/example/project/babygrowthtrackingapplication/admin/AdminLoginScreen.kt

package org.example.project.babygrowthtrackingapplication.admin

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import org.example.project.babygrowthtrackingapplication.theme.*

// ─────────────────────────────────────────────────────────────────────────────
// Admin Login Screen
//
// Purpose: A dedicated secure login screen for administrators.
// - Email + password only (no phone, city, address, etc.)
// - Shows a shield/admin badge to distinguish from parent login
// - On success, navigates to AdminHomeScreen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun AdminLoginScreen(
    viewModel       : AdminLoginViewModel,
    onLoginSuccess  : () -> Unit,
    onBackToLogin   : () -> Unit,
) {
    val state        = viewModel.uiState
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors
    val focusManager = LocalFocusManager.current
    val scrollState  = rememberScrollState()

    var animStarted by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(100)
        animStarted = true
    }

    val cardOffsetY by animateFloatAsState(
        targetValue   = if (animStarted) 0f else 300f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label         = "cardY"
    )
    val alpha by animateFloatAsState(
        targetValue   = if (animStarted) 1f else 0f,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label         = "alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF1A1A2E),
                        Color(0xFF16213E),
                        Color(0xFF0F3460)
                    )
                )
            )
    ) {
        // Decorative shield background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensions.logoSize * 0.8f)
                .alpha(0.08f)
                .background(
                    Brush.radialGradient(listOf(Color.White, Color.Transparent))
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = dimensions.screenPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(dimensions.spacingXLarge + dimensions.spacingLarge))

            // Shield icon / logo
            Box(
                modifier = Modifier
                    .size(dimensions.avatarLarge + dimensions.spacingXLarge)
                    .clip(RoundedCornerShape(dimensions.cardCornerRadius))
                    .background(Color.White.copy(0.12f))
                    .alpha(alpha),
                contentAlignment = Alignment.Center
            ) {
                Text("🛡️", style = MaterialTheme.typography.displaySmall)
            }

            Spacer(Modifier.height(dimensions.spacingMedium))

            Text(
                "Admin Panel",
                style      = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color      = Color.White,
                modifier   = Modifier.alpha(alpha)
            )
            Text(
                "Secure administrator access",
                style    = MaterialTheme.typography.bodyMedium,
                color    = Color.White.copy(0.6f),
                textAlign = TextAlign.Center,
                modifier  = Modifier.alpha(alpha)
            )

            Spacer(Modifier.height(dimensions.spacingXLarge))

            // ── Login Card ───────────────────────────────────────────────────
            Card(
                modifier = Modifier
                    .widthIn(max = dimensions.authCardMaxWidth)
                    .fillMaxWidth()
                    .graphicsLayer { translationY = cardOffsetY }
                    .alpha(alpha),
                shape  = RoundedCornerShape(dimensions.cardCornerRadius * 2),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2A3A)),
                elevation = CardDefaults.cardElevation(defaultElevation = dimensions.cardElevation)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(dimensions.spacingLarge),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)
                ) {
                    Text(
                        "Sign in to continue",
                        style    = MaterialTheme.typography.titleMedium,
                        color    = Color.White.copy(0.9f),
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(Modifier.height(dimensions.spacingXSmall))

                    // Email field
                    OutlinedTextField(
                        value         = state.email,
                        onValueChange = viewModel::onEmailChanged,
                        label         = { Text("Email", color = Color.White.copy(0.6f)) },
                        leadingIcon   = {
                            Icon(Icons.Default.Email, null, tint = Color.White.copy(0.6f))
                        },
                        singleLine      = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(dimensions.buttonCornerRadius),
                        colors   = OutlinedTextFieldDefaults.colors(
                            focusedTextColor      = Color.White,
                            unfocusedTextColor    = Color.White.copy(0.8f),
                            focusedBorderColor    = Color(0xFF3E90F0),
                            unfocusedBorderColor  = Color.White.copy(0.2f),
                            focusedContainerColor = Color.White.copy(0.05f),
                            unfocusedContainerColor = Color.White.copy(0.03f),
                            cursorColor           = Color(0xFF3E90F0)
                        )
                    )

                    // Password field
                    OutlinedTextField(
                        value         = state.password,
                        onValueChange = viewModel::onPasswordChanged,
                        label         = { Text("Password", color = Color.White.copy(0.6f)) },
                        leadingIcon   = { Icon(Icons.Default.Lock, null, tint = Color.White.copy(0.6f)) },
                        trailingIcon  = {
                            IconButton(onClick = viewModel::onPasswordVisibilityToggled) {
                                Icon(
                                    imageVector        = if (state.passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint               = Color.White.copy(0.6f)
                                )
                            }
                        },
                        visualTransformation = if (state.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine      = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus(); viewModel.login(onLoginSuccess) }),
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(dimensions.buttonCornerRadius),
                        colors   = OutlinedTextFieldDefaults.colors(
                            focusedTextColor      = Color.White,
                            unfocusedTextColor    = Color.White.copy(0.8f),
                            focusedBorderColor    = Color(0xFF3E90F0),
                            unfocusedBorderColor  = Color.White.copy(0.2f),
                            focusedContainerColor = Color.White.copy(0.05f),
                            unfocusedContainerColor = Color.White.copy(0.03f),
                            cursorColor           = Color(0xFF3E90F0)
                        )
                    )

                    // Error message
                    state.errorMessage?.let { error ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(dimensions.spacingSmall))
                                .background(MaterialTheme.colorScheme.error.copy(0.15f))
                                .padding(dimensions.spacingSmall),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall)
                        ) {
                            Icon(Icons.Default.Error, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(dimensions.iconSmall))
                            Text(error, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                        }
                    }

                    Spacer(Modifier.height(dimensions.spacingXSmall))

                    // Login button
                    Button(
                        onClick  = { focusManager.clearFocus(); viewModel.login(onLoginSuccess) },
                        enabled  = !state.isLoading,
                        modifier = Modifier.fillMaxWidth().height(dimensions.buttonHeight),
                        shape    = RoundedCornerShape(dimensions.buttonCornerRadius),
                        colors   = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF3E90F0),
                            disabledContainerColor = Color(0xFF3E90F0).copy(0.5f)
                        )
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(Modifier.size(dimensions.iconMedium), color = Color.White, strokeWidth = dimensions.borderWidthMedium)
                        } else {
                            Row(
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
                            ) {
                                Icon(Icons.Default.AdminPanelSettings, null, tint = Color.White, modifier = Modifier.size(dimensions.iconMedium))
                                Text("Sign In as Admin", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(dimensions.spacingLarge))

            // Back to regular login
            TextButton(onClick = onBackToLogin) {
                Text(
                    "← Back to regular login",
                    color  = Color.White.copy(0.6f),
                    style  = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(Modifier.height(dimensions.spacingXXLarge))
        }
    }
}