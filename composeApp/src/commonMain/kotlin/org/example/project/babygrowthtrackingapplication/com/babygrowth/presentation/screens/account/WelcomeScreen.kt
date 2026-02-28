package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.account

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
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
import org.jetbrains.compose.resources.DrawableResource
import org.example.project.babygrowthtrackingapplication.ui.components.PrimaryButton
import org.example.project.babygrowthtrackingapplication.ui.components.SecondaryButton

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun WelcomeScreen(
    onLoginClick         : () -> Unit,
    onSignUpClick        : () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope : AnimatedContentScope
) {
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors

    var animationStarted by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) { delay(100); animationStarted = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        customColors.accentGradientStart.copy(alpha = 0.1f),
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(dimensions.spacingLarge))

            Box(
                modifier         = Modifier
                    .fillMaxWidth()
                    .widthIn(max = dimensions.maxContentWidth),
                contentAlignment = Alignment.Center
            ) {
                AnimatedLottieLogoSection(
                    animationStarted      = animationStarted,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedContentScope  = animatedContentScope,
                    modifier              = Modifier.wrapContentHeight()
                )
            }

            Spacer(modifier = Modifier.height(dimensions.spacingLarge))

            Box(
                modifier         = Modifier
                    .fillMaxWidth()
                    .widthIn(max = dimensions.maxContentWidth),
                contentAlignment = Alignment.Center
            ) {
                AnimatedCardSection(
                    animationStarted      = animationStarted,
                    onLoginClick          = onLoginClick,
                    onSignUpClick         = onSignUpClick,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedContentScope  = animatedContentScope,
                    modifier              = Modifier
                        .widthIn(max = dimensions.maxContentWidth)
                        .fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(dimensions.spacingLarge))
        }

        // ── Decorative corners ───────────────────────────────────────────────
        DecorativeCorner(
            imageRes         = Res.drawable.bottom_left_background,
            alignment        = Alignment.BottomStart,
            fromX            = -(dimensions.cornerImageSize.value),
            fromY            = dimensions.cornerImageSize.value,
            size             = dimensions.cornerImageSize,
            animationStarted = animationStarted,
            delayMillis      = 200
        )
        DecorativeCorner(
            imageRes         = Res.drawable.bottom_right_background,
            alignment        = Alignment.BottomEnd,
            fromX            = dimensions.cornerImageSize.value,
            fromY            = dimensions.cornerImageSize.value,
            size             = dimensions.cornerImageSize,
            animationStarted = animationStarted,
            delayMillis      = 300
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Logo section
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalResourceApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun AnimatedLottieLogoSection(
    animationStarted     : Boolean,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope : AnimatedContentScope,
    modifier             : Modifier = Modifier
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
        targetValue   = if (animationStarted) 0f else dimensions.logoSize.value * 0.6f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label         = "logoOffsetY"
    )
    val alpha by animateFloatAsState(
        targetValue   = if (animationStarted) 1f else 0f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label         = "logoAlpha"
    )

    with(sharedTransitionScope) {
        Column(
            modifier            = modifier
                .graphicsLayer { translationY = offsetY }
                .alpha(alpha),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            jsonString?.let { json ->
                val composition by rememberLottieComposition { LottieCompositionSpec.JsonString(json) }
                val progress    by animateLottieCompositionAsState(
                    composition = composition,
                    iterations  = Compottie.IterateForever
                )
                val painter = rememberLottiePainter(
                    composition = composition,
                    progress    = { progress }
                )
                Image(
                    painter            = painter,
                    contentDescription = stringResource(Res.string.app_logo_description),
                    modifier           = Modifier
                        .size(dimensions.logoSize * 0.8f)
                        .sharedBounds(
                            sharedContentState      = rememberSharedContentState(key = "lottie_animation"),
                            animatedVisibilityScope = animatedContentScope,
                            boundsTransform         = { _, _ ->
                                tween(durationMillis = 600, easing = FastOutSlowInEasing)
                            }
                        ),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(dimensions.spacingSmall))

            Text(
                text       = stringResource(Res.string.app_name),
                style      = MaterialTheme.typography.headlineSmall,
                color      = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                textAlign  = TextAlign.Center,
                modifier   = Modifier.sharedBounds(
                    sharedContentState      = rememberSharedContentState(key = "app_name"),
                    animatedVisibilityScope = animatedContentScope,
                    boundsTransform         = { _, _ ->
                        tween(durationMillis = 600, easing = FastOutSlowInEasing)
                    }
                )
            )

            Spacer(modifier = Modifier.height(dimensions.spacingSmall))

            Text(
                text      = stringResource(Res.string.login_page_title),
                style     = MaterialTheme.typography.titleMedium,
                color     = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Card section
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AnimatedCardSection(
    animationStarted     : Boolean,
    onLoginClick         : () -> Unit,
    onSignUpClick        : () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope : AnimatedContentScope,
    modifier             : Modifier = Modifier
) {
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors
    val cardShape    = RoundedCornerShape(dimensions.cardCornerRadius)

    val offsetY by animateFloatAsState(
        targetValue   = if (animationStarted) 0f else dimensions.logoSize.value,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label         = "cardOffset"
    )
    val alpha by animateFloatAsState(
        targetValue   = if (animationStarted) 1f else 0f,
        animationSpec = tween(durationMillis = 800, delayMillis = 400, easing = FastOutSlowInEasing),
        label         = "cardAlpha"
    )

    with(sharedTransitionScope) {
        Box(
            modifier = modifier
                .graphicsLayer { translationY = offsetY }
                .alpha(alpha)
                .sharedBounds(
                    sharedContentState      = rememberSharedContentState(key = "card_background"),
                    animatedVisibilityScope = animatedContentScope,
                    boundsTransform         = { _, _ ->
                        tween(durationMillis = 600, easing = FastOutSlowInEasing)
                    }
                )
        ) {
            Image(
                painter            = painterResource(Res.drawable.baby_background),
                contentDescription = null,
                modifier           = Modifier
                    .fillMaxWidth()
                    .height(dimensions.logoSize * 0.7f)
                    .clip(cardShape),
                contentScale = ContentScale.Crop,
                alpha        = 0.3f
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(cardShape)
                    .background(customColors.glassBackground)
                    .padding(dimensions.spacingLarge),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.height(dimensions.spacingMedium))

                Text(
                    text     = stringResource(Res.string.welcome_select_one),
                    style    = MaterialTheme.typography.titleMedium,
                    color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                    modifier = Modifier.padding(bottom = dimensions.spacingMedium)
                )

                Spacer(modifier = Modifier.height(dimensions.spacingSmall))

                PrimaryButton(
                    text     = stringResource(Res.string.welcome_login),
                    onClick  = onLoginClick,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(dimensions.spacingMedium))

                SecondaryButton(
                    text     = stringResource(Res.string.welcome_signup),
                    onClick  = onSignUpClick,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(dimensions.spacingMedium))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Decorative corner
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun DecorativeCorner(
    imageRes        : DrawableResource,
    alignment       : Alignment,
    fromX           : Float,
    fromY           : Float,
    size            : Dp,
    animationStarted: Boolean,
    delayMillis     : Int,
    modifier        : Modifier = Modifier
) {
    val animationSpec = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness    = Spring.StiffnessLow
    )

    val offsetX by animateFloatAsState(
        targetValue   = if (animationStarted) 0f else fromX,
        animationSpec = animationSpec,
        label         = "offsetX"
    )
    val offsetY by animateFloatAsState(
        targetValue   = if (animationStarted) 0f else fromY,
        animationSpec = animationSpec,
        label         = "offsetY"
    )
    val scale by animateFloatAsState(
        targetValue   = if (animationStarted) 1f else 0f,
        animationSpec = animationSpec,
        label         = "scale"
    )
    val alpha by animateFloatAsState(
        targetValue   = if (animationStarted) 1f else 0f,
        animationSpec = tween(durationMillis = 800, delayMillis = delayMillis, easing = FastOutSlowInEasing),
        label         = "alpha"
    )

    Box(
        modifier         = modifier.fillMaxSize(),
        contentAlignment = alignment
    ) {
        Image(
            painter            = painterResource(imageRes),
            // was: "Decorative corner"
            contentDescription = stringResource(Res.string.decorative_corner_description),
            modifier           = Modifier
                .size(size)
                .graphicsLayer {
                    translationX = offsetX
                    translationY = offsetY
                    scaleX       = scale
                    scaleY       = scale
                }
                .alpha(alpha),
            contentScale = ContentScale.Crop
        )
    }
}