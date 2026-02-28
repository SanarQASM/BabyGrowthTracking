package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.account

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.animateLottieCompositionAsState
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.example.project.babygrowthtrackingapplication.theme.*
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import babygrowthtrackingapplication.composeapp.generated.resources.Res
import babygrowthtrackingapplication.composeapp.generated.resources.*
import org.example.project.babygrowthtrackingapplication.ui.components.PrimaryButton
import org.example.project.babygrowthtrackingapplication.ui.components.GlassmorphicTextField

@Composable
fun EnterNewPasswordScreen(
    viewModel              : EnterNewPasswordViewModel,
    emailOrPhone           : String,
    verificationCode       : String,
    onBackClick            : () -> Unit,
    onPasswordResetSuccess : () -> Unit
) {
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors
    val focusManager = LocalFocusManager.current
    val scrollState  = rememberScrollState()
    val uiState      = viewModel.uiState
    var animationStarted by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { delay(100); animationStarted = true }

    Box(
        modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(
                customColors.accentGradientStart.copy(alpha = 0.1f),
                MaterialTheme.colorScheme.background
            ))
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(scrollState)
                .padding(horizontal = dimensions.screenPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .widthIn(min = dimensions.avatarLarge * 5, max = dimensions.avatarLarge * 6)
                    .padding(top = dimensions.spacingMedium),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back",
                        tint = customColors.accentGradientStart)
                }
            }

            Spacer(modifier = Modifier.height(dimensions.spacingMedium))

            Box(
                modifier = Modifier.fillMaxWidth()
                    .widthIn(min = dimensions.avatarLarge * 5, max = dimensions.avatarLarge * 6),
                contentAlignment = Alignment.Center
            ) {
                AnimatedNewPasswordLogoSection(animationStarted, Modifier.wrapContentHeight())
            }

            Spacer(modifier = Modifier.height(dimensions.spacingLarge))

            AnimatedNewPasswordCard(
                animationStarted                  = animationStarted,
                uiState                           = uiState,
                onNewPasswordChange               = viewModel::onNewPasswordChanged,
                onConfirmPasswordChange           = viewModel::onConfirmPasswordChanged,
                onNewPasswordVisibilityToggle     = viewModel::onNewPasswordVisibilityToggled,
                onConfirmPasswordVisibilityToggle = viewModel::onConfirmPasswordVisibilityToggled,
                onResetClick = {
                    viewModel.resetPassword(
                        email     = emailOrPhone,
                        code      = verificationCode,
                        onSuccess = onPasswordResetSuccess
                    )
                },
                focusManager = focusManager,
                modifier     = Modifier
                    .widthIn(min = dimensions.avatarLarge * 5, max = dimensions.avatarLarge * 6)
                    .fillMaxWidth()
                    .padding(bottom = dimensions.spacingXXLarge * 2)
            )
        }

        NewPasswordDecorativeCorner(
            imageRes = Res.drawable.bottom_left_background, alignment = Alignment.BottomStart,
            fromX = -100f, fromY = 100f, size = dimensions.cornerImageSize,
            animationStarted = animationStarted, delayMillis = 200)
        NewPasswordDecorativeCorner(
            imageRes = Res.drawable.bottom_right_background, alignment = Alignment.BottomEnd,
            fromX = 100f, fromY = 100f, size = dimensions.cornerImageSize,
            animationStarted = animationStarted, delayMillis = 300)
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun AnimatedNewPasswordLogoSection(
    animationStarted: Boolean,
    modifier: Modifier = Modifier
) {
    val dimensions = LocalDimensions.current
    var jsonString by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        jsonString = withContext(Dispatchers.Default) {
            try { Res.readBytes("files/login.json").decodeToString() }
            catch (e: Exception) { e.printStackTrace(); null }
        }
    }
    val offsetY by animateFloatAsState(
        targetValue   = if (animationStarted) 0f else 200f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label         = "logoOffsetY")
    val alpha by animateFloatAsState(
        targetValue   = if (animationStarted) 1f else 0f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label         = "logoAlpha")

    Column(
        modifier            = modifier.graphicsLayer { translationY = offsetY }.alpha(alpha),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        jsonString?.let { json ->
            val composition by rememberLottieComposition { LottieCompositionSpec.JsonString(json) }
            val progress by animateLottieCompositionAsState(composition,
                iterations = Compottie.IterateForever)
            Image(rememberLottiePainter(composition, { progress }), "App Logo",
                modifier = Modifier.size(dimensions.logoSize * 0.8f), contentScale = ContentScale.Fit)
        }
        Spacer(modifier = Modifier.height(dimensions.spacingSmall))
        Text(stringResource(Res.string.app_name),
            style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
    }
}

@Composable
private fun AnimatedNewPasswordCard(
    animationStarted                  : Boolean,
    uiState                           : EnterNewPasswordUiState,
    onNewPasswordChange               : (String) -> Unit,
    onConfirmPasswordChange           : (String) -> Unit,
    onNewPasswordVisibilityToggle     : () -> Unit,
    onConfirmPasswordVisibilityToggle : () -> Unit,
    onResetClick                      : () -> Unit,
    focusManager                      : androidx.compose.ui.focus.FocusManager,
    modifier                          : Modifier = Modifier
) {
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors

    val offsetY by animateFloatAsState(
        targetValue   = if (animationStarted) 0f else 400f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label         = "cardOffset")
    val alpha by animateFloatAsState(
        targetValue   = if (animationStarted) 1f else 0f,
        animationSpec = tween(durationMillis = 800, delayMillis = 400, easing = FastOutSlowInEasing),
        label         = "cardAlpha")

    Box(modifier.graphicsLayer { translationY = offsetY }.alpha(alpha).wrapContentHeight()) {
        Image(painterResource(Res.drawable.baby_background), null,
            modifier = Modifier.fillMaxWidth().matchParentSize()
                .clip(RoundedCornerShape(dimensions.cardCornerRadius * 2)).alpha(0.3f),
            contentScale = ContentScale.Crop)

        Column(
            modifier = Modifier.fillMaxWidth().wrapContentHeight()
                .clip(RoundedCornerShape(dimensions.cardCornerRadius * 2))
                .background(customColors.glassBackground)
                .padding(dimensions.spacingLarge),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(stringResource(Res.string.new_password_title),
                style    = MaterialTheme.typography.titleLarge,
                color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                modifier = Modifier.padding(bottom = dimensions.spacingLarge))

            GlassmorphicTextField(
                value                = uiState.newPassword,
                onValueChange        = onNewPasswordChange,
                placeholder          = stringResource(Res.string.new_password_placeholder),
                leadingIcon          = {
                    Icon(Icons.Default.Lock, "New Password",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                },
                trailingIcon         = {
                    IconButton(onClick = onNewPasswordVisibilityToggle) {
                        Icon(
                            imageVector        = if (uiState.newPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (uiState.newPasswordVisible) "Hide password" else "Show password",
                            tint               = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                },
                visualTransformation = if (uiState.newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions      = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                keyboardActions      = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                modifier             = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(dimensions.spacingMedium))

            GlassmorphicTextField(
                value                = uiState.confirmPassword,
                onValueChange        = onConfirmPasswordChange,
                placeholder          = stringResource(Res.string.new_password_confirm_placeholder),
                leadingIcon          = {
                    Icon(Icons.Default.Lock, "Confirm Password",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                },
                trailingIcon         = {
                    IconButton(onClick = onConfirmPasswordVisibilityToggle) {
                        Icon(
                            imageVector        = if (uiState.confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (uiState.confirmPasswordVisible) "Hide password" else "Show password",
                            tint               = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                },
                visualTransformation = if (uiState.confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions      = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions      = KeyboardActions(onDone = { focusManager.clearFocus(); onResetClick() }),
                modifier             = Modifier.fillMaxWidth()
            )

            if (uiState.errorMessage != null) {
                Spacer(modifier = Modifier.height(dimensions.spacingMedium))
                Text(uiState.errorMessage,
                    style     = MaterialTheme.typography.bodySmall,
                    color     = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier  = Modifier.fillMaxWidth())
            }

            Spacer(modifier = Modifier.height(dimensions.spacingLarge))

            PrimaryButton(
                text     = stringResource(Res.string.new_password_button),
                onClick  = onResetClick,
                loading  = uiState.isLoading,
                enabled  = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(dimensions.spacingMedium))
        }
    }
}

@Composable
private fun NewPasswordDecorativeCorner(
    imageRes         : org.jetbrains.compose.resources.DrawableResource,
    alignment        : Alignment,
    fromX            : Float, fromY: Float,
    size             : androidx.compose.ui.unit.Dp,
    animationStarted : Boolean,
    delayMillis      : Int,
    modifier         : Modifier = Modifier
) {
    val spec    = spring<Float>(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
    val offsetX by animateFloatAsState(if (animationStarted) 0f else fromX, spec, label = "offsetX")
    val offsetY by animateFloatAsState(if (animationStarted) 0f else fromY, spec, label = "offsetY")
    val scale   by animateFloatAsState(if (animationStarted) 1f else 0f, spec, label = "scale")
    val alpha   by animateFloatAsState(if (animationStarted) 1f else 0f,
        tween(800, delayMillis, FastOutSlowInEasing), label = "alpha")
    Box(modifier.fillMaxSize(), contentAlignment = alignment) {
        Image(painterResource(imageRes), "Decorative corner",
            modifier = Modifier.size(size)
                .graphicsLayer { translationX = offsetX; translationY = offsetY; scaleX = scale; scaleY = scale }
                .alpha(alpha),
            contentScale = ContentScale.Crop)
    }
}