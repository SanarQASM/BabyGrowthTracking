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
import babygrowthtrackingapplication.composeapp.generated.resources.Res
import babygrowthtrackingapplication.composeapp.generated.resources.*
import org.example.project.babygrowthtrackingapplication.theme.*
import org.jetbrains.compose.resources.stringResource

// ─────────────────────────────────────────────────────────────────────────────
// AdminLoginScreen
//
// All strings come from Res.string.*
// All sizes come from LocalDimensions.current
// All colours come from MaterialTheme.colorScheme / MaterialTheme.customColors
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

    // ── Resolve sentinel error keys to localised strings ──────────────────
    val errorMessage: String? = when (state.errorKey) {
        null                 -> null
        "ERR_EMAIL_EMPTY"    -> stringResource(Res.string.admin_login_email_error)
        "ERR_EMAIL_INVALID"  -> stringResource(Res.string.admin_login_email_invalid)
        "ERR_PASSWORD_SHORT" -> stringResource(Res.string.admin_login_password_error)
        "ERR_ACCESS_DENIED"  -> stringResource(Res.string.admin_access_denied)
        else                 -> state.errorKey // server message passed verbatim
    }

    // ── Entrance animation ────────────────────────────────────────────────
    var animStarted by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(100)
        animStarted = true
    }
    val cardOffsetY by animateFloatAsState(
        targetValue   = if (animStarted) 0f else 300f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessLow
        ),
        label = "cardY"
    )
    val contentAlpha by animateFloatAsState(
        targetValue   = if (animStarted) 1f else 0f,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label         = "alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = dimensions.screenPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(dimensions.spacingXLarge + dimensions.spacingLarge))

            // ── Shield icon ───────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(dimensions.avatarLarge + dimensions.spacingXLarge)
                    .clip(RoundedCornerShape(dimensions.cardCornerRadius))
                    .background(customColors.accentGradientStart.copy(alpha = 0.15f))
                    .alpha(contentAlpha),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text  = stringResource(Res.string.admin_shield_emoji),
                    style = MaterialTheme.typography.displaySmall
                )
            }

            Spacer(Modifier.height(dimensions.spacingMedium))

            Text(
                text       = stringResource(Res.string.admin_panel_title),
                style      = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onBackground,
                modifier   = Modifier.alpha(contentAlpha)
            )
            Text(
                text      = stringResource(Res.string.admin_panel_subtitle),
                style     = MaterialTheme.typography.bodyMedium,
                color     = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier  = Modifier.alpha(contentAlpha)
            )

            Spacer(Modifier.height(dimensions.spacingXLarge))

            // ── Login card ────────────────────────────────────────────────
            Card(
                modifier = Modifier
                    .widthIn(max = dimensions.authCardMaxWidth)
                    .fillMaxWidth()
                    .graphicsLayer { translationY = cardOffsetY }
                    .alpha(contentAlpha),
                shape     = RoundedCornerShape(dimensions.cardCornerRadius * 2),
                colors    = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
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
                        text       = stringResource(Res.string.admin_sign_in),
                        style      = MaterialTheme.typography.titleMedium,
                        color      = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(Modifier.height(dimensions.spacingXSmall))

                    // ── Email field ───────────────────────────────────────
                    OutlinedTextField(
                        value         = state.email,
                        onValueChange = viewModel::onEmailChanged,
                        label         = {
                            Text(
                                stringResource(Res.string.admin_email_label),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Email,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        },
                        singleLine      = true,
                        isError         = state.errorKey == "ERR_EMAIL_EMPTY" || state.errorKey == "ERR_EMAIL_INVALID",
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction    = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(dimensions.buttonCornerRadius),
                        colors   = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor    = customColors.accentGradientStart,
                            unfocusedBorderColor  = MaterialTheme.colorScheme.outline,
                            focusedTextColor      = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor    = MaterialTheme.colorScheme.onSurface,
                            cursorColor           = customColors.accentGradientStart
                        )
                    )

                    // ── Password field ────────────────────────────────────
                    OutlinedTextField(
                        value         = state.password,
                        onValueChange = viewModel::onPasswordChanged,
                        label         = {
                            Text(
                                stringResource(Res.string.admin_password_label),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = viewModel::onPasswordVisibilityToggled) {
                                Icon(
                                    imageVector = if (state.passwordVisible)
                                        Icons.Default.VisibilityOff
                                    else
                                        Icons.Default.Visibility,
                                    contentDescription = if (state.passwordVisible)
                                        stringResource(Res.string.admin_password_hide)
                                    else
                                        stringResource(Res.string.admin_password_show),
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        },
                        visualTransformation = if (state.passwordVisible)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        singleLine      = true,
                        isError         = state.errorKey == "ERR_PASSWORD_SHORT",
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction    = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                viewModel.login(onLoginSuccess)
                            }
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(dimensions.buttonCornerRadius),
                        colors   = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = customColors.accentGradientStart,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedTextColor     = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor   = MaterialTheme.colorScheme.onSurface,
                            cursorColor          = customColors.accentGradientStart
                        )
                    )

                    // ── Error message ─────────────────────────────────────
                    errorMessage?.let { error ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(dimensions.spacingSmall))
                                .background(MaterialTheme.colorScheme.errorContainer)
                                .padding(dimensions.spacingSmall),
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall)
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint     = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(dimensions.iconSmall)
                            )
                            Text(
                                text  = error,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }

                    Spacer(Modifier.height(dimensions.spacingXSmall))

                    // ── Login button ──────────────────────────────────────
                    Button(
                        onClick  = {
                            focusManager.clearFocus()
                            viewModel.login(onLoginSuccess)
                        },
                        enabled  = !state.isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(dimensions.buttonHeight),
                        shape  = RoundedCornerShape(dimensions.buttonCornerRadius),
                        colors = ButtonDefaults.buttonColors(
                            containerColor         = customColors.accentGradientStart,
                            disabledContainerColor = customColors.accentGradientStart.copy(alpha = 0.5f)
                        )
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier    = Modifier.size(dimensions.iconMedium),
                                color       = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = dimensions.borderWidthMedium
                            )
                        } else {
                            Row(
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
                            ) {
                                Icon(
                                    Icons.Default.AdminPanelSettings,
                                    contentDescription = null,
                                    tint     = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(dimensions.iconMedium)
                                )
                                Text(
                                    text       = stringResource(Res.string.admin_sign_in_button),
                                    fontWeight = FontWeight.Bold,
                                    color      = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(dimensions.spacingLarge))

            // ── Back to regular login ─────────────────────────────────────
            TextButton(onClick = onBackToLogin) {
                Text(
                    text  = stringResource(Res.string.admin_back_to_login),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(Modifier.height(dimensions.spacingXXLarge))
        }
    }
}