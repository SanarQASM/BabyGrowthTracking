package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.account.screens

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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
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
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.account.models.ForgotPasswordUiState
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.account.models.ForgotPasswordViewModel
import org.example.project.babygrowthtrackingapplication.ui.components.PrimaryButton
import org.example.project.babygrowthtrackingapplication.ui.components.GlassmorphicTextField
import org.jetbrains.compose.resources.DrawableResource

// ─────────────────────────────────────────────────────────────────────────────
// Auth screens share a large pill-shaped card corner.
// Backed by spacingXXLarge so it scales with the responsive breakpoint system.
// ─────────────────────────────────────────────────────────────────────────────
private val AuthCardCornerRadius @Composable get() = LocalDimensions.current.spacingXXLarge

/**
 * REFACTORED:
 *  - 320.dp min-width  →  dimensions.authCardMinWidth
 *  - 420.dp max-width  →  dimensions.authCardMaxWidth
 */
@Composable
fun ForgotPasswordScreen(
    viewModel     : ForgotPasswordViewModel,
    onBackClick   : () -> Unit,
    onResetSuccess: (email: String) -> Unit
) {
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors
    val focusManager = LocalFocusManager.current
    val scrollState  = rememberScrollState()
    val uiState      = viewModel.uiState

    var animationStarted by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(100); animationStarted = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(
                    customColors.accentGradientStart.copy(alpha = 0.1f),
                    MaterialTheme.colorScheme.background
                ))
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = dimensions.screenPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Back button row ──────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    // WAS: .widthIn(min = 320.dp, max = 420.dp)
                    .widthIn(
                        min = dimensions.authCardMinWidth,
                        max = dimensions.authCardMaxWidth
                    )
                    .padding(top = dimensions.spacingMedium),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(Res.string.common_back),
                        tint               = customColors.accentGradientStart
                    )
                }
            }

            Spacer(modifier = Modifier.height(dimensions.spacingMedium))

            // ── Logo ─────────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    // WAS: .widthIn(min = 320.dp, max = 420.dp)
                    .widthIn(
                        min = dimensions.authCardMinWidth,
                        max = dimensions.authCardMaxWidth
                    ),
                contentAlignment = Alignment.Center
            ) {
                AnimatedForgotPasswordLogoSection(
                    animationStarted = animationStarted,
                    modifier         = Modifier.wrapContentHeight()
                )
            }

            Spacer(modifier = Modifier.height(dimensions.spacingLarge))

            // ── Card ─────────────────────────────────────────────────────────
            AnimatedForgotPasswordCard(
                animationStarted = animationStarted,
                uiState          = uiState,
                onEmailChange    = viewModel::onEmailChanged,
                onSendClick      = { viewModel.sendResetCode(onResetSuccess) },
                focusManager     = focusManager,
                modifier         = Modifier
                    // WAS: .widthIn(min = 320.dp, max = 420.dp)
                    .widthIn(
                        min = dimensions.authCardMinWidth,
                        max = dimensions.authCardMaxWidth
                    )
                    .fillMaxWidth()
                    .padding(bottom = dimensions.spacingXXLarge + dimensions.spacingLarge)
            )
        }

        // ── Decorative corners ───────────────────────────────────────────────
        ForgotPasswordDecorativeCorner(
            imageRes         = Res.drawable.bottom_left_background,
            alignment        = Alignment.BottomStart,
            fromX            = -100f, fromY = 100f,
            size             = dimensions.cornerImageSize,
            animationStarted = animationStarted,
            delayMillis      = 200
        )
        ForgotPasswordDecorativeCorner(
            imageRes         = Res.drawable.bottom_right_background,
            alignment        = Alignment.BottomEnd,
            fromX            = 100f, fromY = 100f,
            size             = dimensions.cornerImageSize,
            animationStarted = animationStarted,
            delayMillis      = 300
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Logo section
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun AnimatedForgotPasswordLogoSection(
    animationStarted: Boolean,
    modifier        : Modifier = Modifier
) {
    val dimensions = LocalDimensions.current
    var jsonString by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        jsonString = withContext(Dispatchers.Default) {
            try { Res.readBytes("files/login.json").decodeToString() }
            catch (e: Exception) { null }
        }
    }

    val offsetY by animateFloatAsState(
        targetValue   = if (animationStarted) 0f else 200f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow),
        label         = "logoY"
    )
    val alpha by animateFloatAsState(
        targetValue   = if (animationStarted) 1f else 0f,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label         = "logoAlpha"
    )

    Column(
        modifier            = modifier
            .graphicsLayer { translationY = offsetY }
            .alpha(alpha),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        jsonString?.let { json ->
            val composition by rememberLottieComposition { LottieCompositionSpec.JsonString(json) }
            val progress    by animateLottieCompositionAsState(
                composition, iterations = Compottie.IterateForever
            )
            Image(
                painter            = rememberLottiePainter(composition, { progress }),
                contentDescription = stringResource(Res.string.app_logo_description),
                modifier           = Modifier.size(dimensions.logoSize * 0.8f),
                contentScale       = ContentScale.Fit
            )
        }
        Spacer(Modifier.height(dimensions.spacingSmall))
        Text(
            text       = stringResource(Res.string.app_name),
            style      = MaterialTheme.typography.headlineSmall,
            color      = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            textAlign  = TextAlign.Center
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Animated card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AnimatedForgotPasswordCard(
    animationStarted: Boolean,
    uiState         : ForgotPasswordUiState,
    onEmailChange   : (String) -> Unit,
    onSendClick     : () -> Unit,
    focusManager    : FocusManager,
    modifier        : Modifier = Modifier
) {
    val dimensions       = LocalDimensions.current
    val cardCornerRadius = AuthCardCornerRadius
    val cardShape        = RoundedCornerShape(cardCornerRadius)

    val offsetY by animateFloatAsState(
        targetValue   = if (animationStarted) 0f else 400f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow),
        label         = "cardY"
    )
    val alpha by animateFloatAsState(
        targetValue   = if (animationStarted) 1f else 0f,
        animationSpec = tween(800, 400, FastOutSlowInEasing),
        label         = "cardAlpha"
    )

    Box(
        modifier = modifier
            .graphicsLayer { translationY = offsetY }
            .alpha(alpha)
            .wrapContentHeight()
    ) {
        Image(
            painter            = painterResource(Res.drawable.baby_background),
            contentDescription = null,
            modifier           = Modifier
                .fillMaxWidth()
                .matchParentSize()
                .clip(cardShape)
                .alpha(0.3f),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .clip(cardShape)
                .background(Color.White.copy(alpha = 0.05f))
                .padding(dimensions.spacingLarge),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text     = stringResource(Res.string.forgot_password_title),
                style    = MaterialTheme.typography.titleLarge,
                color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                modifier = Modifier.padding(bottom = dimensions.spacingSmall)
            )

            Text(
                text      = stringResource(Res.string.forgot_password_description),
                style     = MaterialTheme.typography.bodySmall,
                color     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier  = Modifier.padding(bottom = dimensions.spacingLarge)
            )

            GlassmorphicTextField(
                value         = uiState.email,
                onValueChange = onEmailChange,
                placeholder   = stringResource(Res.string.forgot_password_email_placeholder),
                leadingIcon   = {
                    Icon(
                        imageVector        = Icons.Default.Email,
                        contentDescription = stringResource(Res.string.forgot_password_email_icon_description),
                        tint               = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction    = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus(); onSendClick() }
                ),
                modifier = Modifier.fillMaxWidth()
            )

            if (uiState.successMessage != null) {
                Spacer(Modifier.height(dimensions.spacingSmall))
                Text(
                    text     = uiState.successMessage,
                    style    = MaterialTheme.typography.bodySmall,
                    color    = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (uiState.errorMessage != null) {
                Spacer(Modifier.height(dimensions.spacingSmall))
                Text(
                    text     = uiState.errorMessage,
                    style    = MaterialTheme.typography.bodySmall,
                    color    = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(dimensions.spacingLarge))

            PrimaryButton(
                text     = stringResource(Res.string.forgot_password_send_code),
                onClick  = onSendClick,
                loading  = uiState.isLoading,
                enabled  = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(dimensions.spacingMedium))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Decorative corner
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ForgotPasswordDecorativeCorner(
    imageRes        : DrawableResource,
    alignment       : Alignment,
    fromX           : Float,
    fromY           : Float,
    size            : Dp,
    animationStarted: Boolean,
    delayMillis     : Int,
    modifier        : Modifier = Modifier
) {
    val spec    = spring<Float>(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow)
    val offsetX by animateFloatAsState(if (animationStarted) 0f else fromX, spec, label = "x")
    val offsetY by animateFloatAsState(if (animationStarted) 0f else fromY, spec, label = "y")
    val scale   by animateFloatAsState(if (animationStarted) 1f else 0f,    spec, label = "scale")
    val alpha   by animateFloatAsState(
        if (animationStarted) 1f else 0f,
        tween(800, delayMillis, FastOutSlowInEasing),
        label = "alpha"
    )
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(modifier.fillMaxSize(), contentAlignment = alignment) {
            Image(
                painter            = painterResource(imageRes),
                contentDescription = stringResource(Res.string.decorative_corner_description),
                modifier           = Modifier
                    .size(size)
                    .graphicsLayer {
                        translationX = offsetX; translationY = offsetY
                        scaleX = scale;         scaleY = scale
                    }
                    .alpha(alpha),
                contentScale = ContentScale.Crop
            )
        }
    }
}