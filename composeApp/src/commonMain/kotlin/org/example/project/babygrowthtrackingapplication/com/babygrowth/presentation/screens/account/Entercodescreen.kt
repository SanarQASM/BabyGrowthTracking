package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.account

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
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
import org.example.project.babygrowthtrackingapplication.ui.components.OtpTextField

@Composable
fun EnterCodeScreen(
    viewModel      : EnterCodeViewModel,
    emailOrPhone   : String,
    onBackClick    : () -> Unit,
    onCodeVerified : (email: String, code: String) -> Unit
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
                Modifier.fillMaxWidth()
                    .widthIn(min = dimensions.maxContentWidth.takeIf { it != androidx.compose.ui.unit.Dp.Unspecified } ?: dimensions.avatarLarge * 5, max = dimensions.maxContentWidth.takeIf { it != androidx.compose.ui.unit.Dp.Unspecified } ?: dimensions.avatarLarge * 6)
                    .padding(top = dimensions.spacingMedium)
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back",
                        tint = customColors.accentGradientStart)
                }
            }

            Spacer(Modifier.height(dimensions.spacingMedium))

            Box(
                Modifier.fillMaxWidth()
                    .widthIn(min = dimensions.avatarLarge * 5, max = dimensions.avatarLarge * 6),
                contentAlignment = Alignment.Center
            ) {
                AnimatedEnterCodeLogoSection(animationStarted, Modifier.wrapContentHeight())
            }

            Spacer(Modifier.height(dimensions.spacingLarge))

            AnimatedEnterCodeCard(
                animationStarted = animationStarted,
                uiState          = uiState,
                emailOrPhone     = emailOrPhone,
                onCodeChange     = viewModel::onCodeChanged,
                onVerifyClick    = { viewModel.verifyCode(onCodeVerified) },
                focusManager     = focusManager,
                modifier         = Modifier
                    .widthIn(min = dimensions.avatarLarge * 5, max = dimensions.avatarLarge * 6)
                    .fillMaxWidth()
                    .padding(bottom = dimensions.spacingXXLarge * 2)
            )
        }

        EnterCodeDecorativeCorner(Res.drawable.bottom_left_background, Alignment.BottomStart,
            -100f, 100f, dimensions.cornerImageSize, animationStarted, 200)
        EnterCodeDecorativeCorner(Res.drawable.bottom_right_background, Alignment.BottomEnd,
            100f, 100f, dimensions.cornerImageSize, animationStarted, 300)
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun AnimatedEnterCodeLogoSection(animationStarted: Boolean, modifier: Modifier = Modifier) {
    val dimensions = LocalDimensions.current
    var jsonString by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        jsonString = withContext(Dispatchers.Default) {
            try { Res.readBytes("files/login.json").decodeToString() } catch (e: Exception) { null }
        }
    }
    val offsetY by animateFloatAsState(if (animationStarted) 0f else 200f,
        spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow), label = "y")
    val alpha by animateFloatAsState(if (animationStarted) 1f else 0f,
        tween(800, easing = FastOutSlowInEasing), label = "alpha")

    Column(modifier.graphicsLayer { translationY = offsetY }.alpha(alpha),
        horizontalAlignment = Alignment.CenterHorizontally) {
        jsonString?.let { json ->
            val composition by rememberLottieComposition { LottieCompositionSpec.JsonString(json) }
            val progress by animateLottieCompositionAsState(composition,
                iterations = Compottie.IterateForever)
            Image(rememberLottiePainter(composition, { progress }), "Logo",
                Modifier.size(dimensions.logoSize * 0.8f), contentScale = ContentScale.Fit)
        }
        Spacer(Modifier.height(dimensions.spacingSmall))
        Text(stringResource(Res.string.app_name),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center)
    }
}

@Composable
private fun AnimatedEnterCodeCard(
    animationStarted : Boolean,
    uiState          : EnterCodeUiState,
    emailOrPhone     : String,
    onCodeChange     : (String) -> Unit,
    onVerifyClick    : () -> Unit,
    focusManager     : androidx.compose.ui.focus.FocusManager,
    modifier         : Modifier = Modifier
) {
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors

    val offsetY by animateFloatAsState(if (animationStarted) 0f else 400f,
        spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow), label = "cardY")
    val alpha by animateFloatAsState(if (animationStarted) 1f else 0f,
        tween(800, 400, FastOutSlowInEasing), label = "cardAlpha")

    Box(modifier.graphicsLayer { translationY = offsetY }.alpha(alpha).wrapContentHeight()) {
        Image(painterResource(Res.drawable.baby_background), null,
            modifier = Modifier.fillMaxWidth().matchParentSize()
                .clip(RoundedCornerShape(dimensions.cardCornerRadius * 2))
                .alpha(0.3f),
            contentScale = ContentScale.Crop)

        Column(
            Modifier.fillMaxWidth().wrapContentHeight()
                .clip(RoundedCornerShape(dimensions.cardCornerRadius * 2))
                .background(customColors.glassBackground)
                .padding(dimensions.spacingLarge),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(stringResource(Res.string.enter_code_title),
                style    = MaterialTheme.typography.titleLarge,
                color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                modifier = Modifier.padding(bottom = dimensions.spacingSmall))

            Text(stringResource(Res.string.enter_code_sent_to, emailOrPhone),
                style     = MaterialTheme.typography.bodySmall,
                color     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier  = Modifier.padding(bottom = dimensions.spacingLarge))

            OtpTextField(
                value         = uiState.code,
                onValueChange = onCodeChange,
                length        = 6,
                onComplete    = { focusManager.clearFocus(); if (uiState.code.length == 6) onVerifyClick() },
                modifier      = Modifier.fillMaxWidth().padding(horizontal = dimensions.spacingMedium)
            )

            if (uiState.errorMessage != null) {
                Spacer(Modifier.height(dimensions.spacingMedium))
                Text(uiState.errorMessage,
                    style     = MaterialTheme.typography.bodySmall,
                    color     = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier  = Modifier.fillMaxWidth())
            }

            Spacer(Modifier.height(dimensions.spacingLarge))

            PrimaryButton(
                text     = stringResource(Res.string.enter_code_button),
                onClick  = onVerifyClick,
                loading  = uiState.isLoading,
                enabled  = !uiState.isLoading && uiState.code.length == 6,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(dimensions.spacingMedium))
        }
    }
}

@Composable
private fun EnterCodeDecorativeCorner(
    imageRes         : org.jetbrains.compose.resources.DrawableResource,
    alignment        : Alignment,
    fromX            : Float, fromY: Float,
    size             : androidx.compose.ui.unit.Dp,
    animationStarted : Boolean,
    delayMillis      : Int,
    modifier         : Modifier = Modifier
) {
    val spec    = spring<Float>(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow)
    val offsetX by animateFloatAsState(if (animationStarted) 0f else fromX, spec, label = "x")
    val offsetY by animateFloatAsState(if (animationStarted) 0f else fromY, spec, label = "y")
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