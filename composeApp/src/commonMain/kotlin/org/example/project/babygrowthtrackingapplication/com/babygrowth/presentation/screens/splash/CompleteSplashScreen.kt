package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.splash

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import babygrowthtrackingapplication.composeapp.generated.resources.Res
import babygrowthtrackingapplication.composeapp.generated.resources.app_name
import babygrowthtrackingapplication.composeapp.generated.resources.bottom_left_background
import babygrowthtrackingapplication.composeapp.generated.resources.bottom_right_background
import babygrowthtrackingapplication.composeapp.generated.resources.top_left_background
import babygrowthtrackingapplication.composeapp.generated.resources.top_right_background
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.example.project.babygrowthtrackingapplication.theme.BabyGrowthTheme
import org.example.project.babygrowthtrackingapplication.theme.GenderTheme
import org.example.project.babygrowthtrackingapplication.theme.LocalDimensions
import org.example.project.babygrowthtrackingapplication.theme.LocalIsLandscape
import org.example.project.babygrowthtrackingapplication.theme.LocalScreenInfo
import org.example.project.babygrowthtrackingapplication.theme.WindowSizeClass
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.animateLottieCompositionAsState
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Responsive Splash Screen with Lottie Animation
 *
 * LANDSCAPE FIX: In landscape the Lottie logo is constrained to at most 50 % of
 * the screen height so it never overflows on height-constrained phone landscape.
 * The logo padding is also increased slightly to give visual breathing room.
 * Corner images and animation timings are unchanged.
 */
@OptIn(ExperimentalResourceApi::class)
@Composable
fun CompleteSplashScreen(
    onSplashComplete: () -> Unit
) {
    var startAnimation by remember { mutableStateOf(false) }
    var lottieAnimationComplete by remember { mutableStateOf(false) }

    val dimensions   = LocalDimensions.current
    val screenInfo   = LocalScreenInfo.current
    val isLandscape  = LocalIsLandscape.current

    var jsonString by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        jsonString = withContext(Dispatchers.Default) {
            try { Res.readBytes("files/application_logo_lottie.json").decodeToString() }
            catch (e: Exception) { e.printStackTrace(); null }
        }
    }

    jsonString?.let { json ->
        val composition by rememberLottieComposition { LottieCompositionSpec.JsonString(json) }

        val lottieProgress by animateLottieCompositionAsState(
            composition = composition, iterations = 1, speed = 1f)

        LaunchedEffect(lottieProgress, composition) {
            if (lottieProgress >= 0.99f && composition != null) {
                lottieAnimationComplete = true
            }
        }

        val lottiePainter = rememberLottiePainter(composition = composition, progress = { lottieProgress })

        val animationDuration = when (screenInfo.windowSizeClass) {
            WindowSizeClass.COMPACT  -> 800
            WindowSizeClass.MEDIUM   -> 1000
            WindowSizeClass.EXPANDED -> 1200
        }

        val topLeftTranslationX  by animateFloatAsState(if (startAnimation) 0f else -400f,
            tween(animationDuration, easing = FastOutSlowInEasing), label = "topLeftX")
        val topLeftTranslationY  by animateFloatAsState(if (startAnimation) 0f else -400f,
            tween(animationDuration, easing = FastOutSlowInEasing), label = "topLeftY")
        val topRightTranslationX by animateFloatAsState(if (startAnimation) 0f else 400f,
            tween(animationDuration, easing = FastOutSlowInEasing), label = "topRightX")
        val topRightTranslationY by animateFloatAsState(if (startAnimation) 0f else -400f,
            tween(animationDuration, easing = FastOutSlowInEasing), label = "topRightY")
        val bottomLeftTranslationX  by animateFloatAsState(if (startAnimation) 0f else -400f,
            tween(animationDuration, easing = FastOutSlowInEasing), label = "bottomLeftX")
        val bottomLeftTranslationY  by animateFloatAsState(if (startAnimation) 0f else 400f,
            tween(animationDuration, easing = FastOutSlowInEasing), label = "bottomLeftY")
        val bottomRightTranslationX by animateFloatAsState(if (startAnimation) 0f else 400f,
            tween(animationDuration, easing = FastOutSlowInEasing), label = "bottomRightX")
        val bottomRightTranslationY by animateFloatAsState(if (startAnimation) 0f else 400f,
            tween(animationDuration, easing = FastOutSlowInEasing), label = "bottomRightY")

        val logoAlpha by animateFloatAsState(if (startAnimation) 1f else 0f,
            tween(animationDuration + 200, easing = FastOutSlowInEasing), label = "logoAlpha")
        val logoScale by animateFloatAsState(if (startAnimation) 1f else 0.3f,
            tween(animationDuration + 200, easing = FastOutSlowInEasing), label = "logoScale")

        LaunchedEffect(Unit) { delay(100); startAnimation = true }

        LaunchedEffect(lottieAnimationComplete) {
            if (lottieAnimationComplete) { delay(500); onSplashComplete() }
        }

        // 🔒 Force LTR — corner images must never flip in RTL languages
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {

                // Corner images
                Image(painterResource(Res.drawable.top_left_background), null,
                    modifier = Modifier.size(dimensions.cornerImageSize).align(Alignment.TopStart)
                        .graphicsLayer { translationX = topLeftTranslationX; translationY = topLeftTranslationY },
                    contentScale = ContentScale.Crop)

                Image(painterResource(Res.drawable.top_right_background), null,
                    modifier = Modifier.size(dimensions.cornerImageSize).align(Alignment.TopEnd)
                        .graphicsLayer { translationX = topRightTranslationX; translationY = topRightTranslationY },
                    contentScale = ContentScale.Crop)

                Image(painterResource(Res.drawable.bottom_left_background), null,
                    modifier = Modifier.size(dimensions.cornerImageSize).align(Alignment.BottomStart)
                        .graphicsLayer { translationX = bottomLeftTranslationX; translationY = bottomLeftTranslationY },
                    contentScale = ContentScale.Crop)

                Image(painterResource(Res.drawable.bottom_right_background), null,
                    modifier = Modifier.size(dimensions.cornerImageSize).align(Alignment.BottomEnd)
                        .graphicsLayer { translationX = bottomRightTranslationX; translationY = bottomRightTranslationY },
                    contentScale = ContentScale.Crop)

                // CENTER LOGO
                // LANDSCAPE FIX: In landscape the available height is much smaller.
                // We wrap in BoxWithConstraints so the logo never exceeds 70% of
                // the current screen height, keeping it comfortably within the
                // visible area even on phone landscape (≈ 360 dp tall).
                BoxWithConstraints(
                    modifier = Modifier.fillMaxSize()
                        .padding(dimensions.screenPadding)
                        .alpha(logoAlpha)
                        .scale(logoScale),
                    contentAlignment = Alignment.Center
                ) {
                    val maxLogoSize = if (isLandscape) {
                        // Cap the logo at 70 % of available height in landscape
                        (maxHeight * 0.70f)
                    } else {
                        // Portrait: unconstrained (fills available space naturally)
                        dimensions.logoSize   // reference — actual size is set by ContentScale.Fit
                    }

                    if (composition != null) {
                        Image(
                            painter = lottiePainter,
                            contentDescription = stringResource(Res.string.app_name),
                            modifier = Modifier
                                .then(
                                    if (isLandscape)
                                        Modifier.heightIn(max = maxLogoSize)
                                    else
                                        Modifier
                                )
                                .padding(dimensions.logoPadding),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Previews
// ─────────────────────────────────────────────────────────────────────────────

@Preview
@Composable
fun SplashScreenNeutralLightPreview() {
    BabyGrowthTheme(genderTheme = GenderTheme.NEUTRAL, darkTheme = false) {
        CompleteSplashScreen(onSplashComplete = {})
    }
}

@Preview
@Composable
fun SplashScreenNeutralDarkPreview() {
    BabyGrowthTheme(genderTheme = GenderTheme.NEUTRAL, darkTheme = true) {
        CompleteSplashScreen(onSplashComplete = {})
    }
}

@Preview
@Composable
fun SplashScreenGirlLightPreview() {
    BabyGrowthTheme(genderTheme = GenderTheme.GIRL, darkTheme = false) {
        CompleteSplashScreen(onSplashComplete = {})
    }
}

@Preview
@Composable
fun SplashScreenGirlDarkPreview() {
    BabyGrowthTheme(genderTheme = GenderTheme.GIRL, darkTheme = true) {
        CompleteSplashScreen(onSplashComplete = {})
    }
}

@Preview
@Composable
fun SplashScreenBoyLightPreview() {
    BabyGrowthTheme(genderTheme = GenderTheme.BOY, darkTheme = false) {
        CompleteSplashScreen(onSplashComplete = {})
    }
}

@Preview
@Composable
fun SplashScreenBoyDarkPreview() {
    BabyGrowthTheme(genderTheme = GenderTheme.BOY, darkTheme = true) {
        CompleteSplashScreen(onSplashComplete = {})
    }
}

@Preview
@Composable
fun SplashScreenTabletGirlPreview() {
    BabyGrowthTheme(genderTheme = GenderTheme.GIRL, darkTheme = false) {
        CompleteSplashScreen(onSplashComplete = {})
    }
}

@Preview
@Composable
fun SplashScreenDesktopBoyPreview() {
    BabyGrowthTheme(genderTheme = GenderTheme.BOY, darkTheme = false) {
        CompleteSplashScreen(onSplashComplete = {})
    }
}