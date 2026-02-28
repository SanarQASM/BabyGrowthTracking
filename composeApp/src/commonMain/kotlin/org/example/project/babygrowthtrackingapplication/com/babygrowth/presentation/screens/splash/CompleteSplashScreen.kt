package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.splash

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
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
 * ✨ FIXED: LOTTIE SIZE & CORNER IMAGE POSITIONING! ✨
 *
 * ✅ Lottie has NO size constraint (like login screen) - renders naturally
 * ✅ Corner images use Crop scale to fill edges completely
 * ✅ Corner images positioned flush to screen edges (no gaps!)
 * ✅ Black background for all modes and genders
 * ✅ Waits for Lottie animation to complete before proceeding
 * ✅ Clean, minimalist design without title text
 * ✅ Adapts to different screen sizes (Phone, Tablet, Desktop)
 * ✅ Platform-agnostic (Android, iOS, Desktop, Web)
 * ✅ Smooth corner animations
 * ✅ Responsive dimensions
 */

@OptIn(ExperimentalResourceApi::class)
@Composable
fun CompleteSplashScreen(
    onSplashComplete: () -> Unit
) {
    var startAnimation by remember { mutableStateOf(false) }
    var lottieAnimationComplete by remember { mutableStateOf(false) }

    // Access responsive theme values - GENDER-AWARE!
    val dimensions = LocalDimensions.current
    val screenInfo = LocalScreenInfo.current

    // 🔥 OPTIMIZED: Load Lottie JSON using cleaner approach (like OnboardingScreen)
    var jsonString by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        jsonString = withContext(Dispatchers.Default) {
            try {
                Res.readBytes("files/application_logo_lottie.json").decodeToString()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    // Only create composition if JSON loaded successfully
    jsonString?.let { json ->
        val composition by rememberLottieComposition {
            LottieCompositionSpec.JsonString(json)
        }

        // Animate Lottie composition - Play ONCE (not loop)
        val lottieProgress by animateLottieCompositionAsState(
            composition = composition,
            iterations = 1, // Play only once
            speed = 1f
        )

        // Track when Lottie animation is complete
        LaunchedEffect(lottieProgress, composition) {
            if (lottieProgress >= 0.99f && composition != null) {
                lottieAnimationComplete = true
            }
        }

        // Get Lottie painter
        val lottiePainter = rememberLottiePainter(
            composition = composition,
            progress = { lottieProgress }
        )

        // Adjust animation duration based on screen size
        val animationDuration = when (screenInfo.windowSizeClass) {
            WindowSizeClass.COMPACT -> 800
            WindowSizeClass.MEDIUM -> 1000
            WindowSizeClass.EXPANDED -> 1200
        }

        // Corner animations with screen-size-aware duration
        // 🔥 FIXED: Initial positions further off-screen for smoother entry
        val topLeftTranslationX by animateFloatAsState(
            targetValue = if (startAnimation) 0f else -400f,
            animationSpec = tween(durationMillis = animationDuration, easing = FastOutSlowInEasing),
            label = "topLeftX"
        )
        val topLeftTranslationY by animateFloatAsState(
            targetValue = if (startAnimation) 0f else -400f,
            animationSpec = tween(durationMillis = animationDuration, easing = FastOutSlowInEasing),
            label = "topLeftY"
        )
        val topRightTranslationX by animateFloatAsState(
            targetValue = if (startAnimation) 0f else 400f,
            animationSpec = tween(durationMillis = animationDuration, easing = FastOutSlowInEasing),
            label = "topRightX"
        )
        val topRightTranslationY by animateFloatAsState(
            targetValue = if (startAnimation) 0f else -400f,
            animationSpec = tween(durationMillis = animationDuration, easing = FastOutSlowInEasing),
            label = "topRightY"
        )
        val bottomLeftTranslationX by animateFloatAsState(
            targetValue = if (startAnimation) 0f else -400f,
            animationSpec = tween(durationMillis = animationDuration, easing = FastOutSlowInEasing),
            label = "bottomLeftX"
        )
        val bottomLeftTranslationY by animateFloatAsState(
            targetValue = if (startAnimation) 0f else 400f,
            animationSpec = tween(durationMillis = animationDuration, easing = FastOutSlowInEasing),
            label = "bottomLeftY"
        )
        val bottomRightTranslationX by animateFloatAsState(
            targetValue = if (startAnimation) 0f else 400f,
            animationSpec = tween(durationMillis = animationDuration, easing = FastOutSlowInEasing),
            label = "bottomRightX"
        )
        val bottomRightTranslationY by animateFloatAsState(
            targetValue = if (startAnimation) 0f else 400f,
            animationSpec = tween(durationMillis = animationDuration, easing = FastOutSlowInEasing),
            label = "bottomRightY"
        )

        // Logo animations
        val logoAlpha by animateFloatAsState(
            targetValue = if (startAnimation) 1f else 0f,
            animationSpec = tween(
                durationMillis = animationDuration + 200,
                easing = FastOutSlowInEasing
            ),
            label = "logoAlpha"
        )
        val logoScale by animateFloatAsState(
            targetValue = if (startAnimation) 1f else 0.3f,
            animationSpec = tween(
                durationMillis = animationDuration + 200,
                easing = FastOutSlowInEasing
            ),
            label = "logoScale"
        )

        // Start animations and wait for Lottie to complete before navigating
        LaunchedEffect(Unit) {
            // Wait a bit for resources to load
            delay(100)
            startAnimation = true
        }

        // Navigate to next screen only when Lottie animation is complete
        LaunchedEffect(lottieAnimationComplete) {
            if (lottieAnimationComplete) {
                // Add a small delay after animation completes for better UX
                delay(500)
                onSplashComplete()
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black) // 🎨 BLACK BACKGROUND for all modes/genders!
        ) {
            // TOP LEFT corner image - FLUSH with edges using Crop
            Image(
                painter = painterResource(Res.drawable.top_left_background),
                contentDescription = null,
                modifier = Modifier
                    .size(dimensions.cornerImageSize)
                    .align(Alignment.TopStart)
                    .graphicsLayer {
                        translationX = topLeftTranslationX
                        translationY = topLeftTranslationY
                    },
                contentScale = ContentScale.Crop // 🔥 CHANGED: Crop fills corner completely
            )

            // TOP RIGHT corner image - FLUSH with edges using Crop
            Image(
                painter = painterResource(Res.drawable.top_right_background),
                contentDescription = null,
                modifier = Modifier
                    .size(dimensions.cornerImageSize)
                    .align(Alignment.TopEnd)
                    .graphicsLayer {
                        translationX = topRightTranslationX
                        translationY = topRightTranslationY
                    },
                contentScale = ContentScale.Crop // 🔥 CHANGED: Crop fills corner completely
            )

            // BOTTOM LEFT corner image - FLUSH with edges using Crop
            Image(
                painter = painterResource(Res.drawable.bottom_left_background),
                contentDescription = null,
                modifier = Modifier
                    .size(dimensions.cornerImageSize)
                    .align(Alignment.BottomStart)
                    .graphicsLayer {
                        translationX = bottomLeftTranslationX
                        translationY = bottomLeftTranslationY
                    },
                contentScale = ContentScale.Crop // 🔥 CHANGED: Crop fills corner completely
            )

            // BOTTOM RIGHT corner image - FLUSH with edges using Crop
            Image(
                painter = painterResource(Res.drawable.bottom_right_background),
                contentDescription = null,
                modifier = Modifier
                    .size(dimensions.cornerImageSize)
                    .align(Alignment.BottomEnd)
                    .graphicsLayer {
                        translationX = bottomRightTranslationX
                        translationY = bottomRightTranslationY
                    },
                contentScale = ContentScale.Crop // 🔥 CHANGED: Crop fills corner completely
            )

            // CENTER LOGO - Natural size (like login screen)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(dimensions.screenPadding)
                    .alpha(logoAlpha)
                    .scale(logoScale),
                contentAlignment = Alignment.Center
            ) {
                // 🎨 LOTTIE ANIMATION - NO SIZE CONSTRAINT! Renders naturally like login screen
                if (composition != null) {
                    Image(
                        painter = lottiePainter,
                        contentDescription = stringResource(Res.string.app_name),
                        modifier = Modifier
                            .padding(dimensions.logoPadding),
                        contentScale = ContentScale.Fit
                    )
                }

            }
        }
    }
}

// Preview for different themes - All with BLACK background
//(name = "Neutral - Light", widthDp = 360, heightDp = 640)
@Preview
@Composable
fun SplashScreenNeutralLightPreview() {
    BabyGrowthTheme(genderTheme = GenderTheme.NEUTRAL, darkTheme = false) {
        CompleteSplashScreen(onSplashComplete = {})
    }
}
//(name = "Neutral - Dark", widthDp = 360, heightDp = 640)
@Preview
@Composable
fun SplashScreenNeutralDarkPreview() {
    BabyGrowthTheme(genderTheme = GenderTheme.NEUTRAL, darkTheme = true) {
        CompleteSplashScreen(onSplashComplete = {})
    }
}

//(name = "Girl - Light", widthDp = 360, heightDp = 640)
@Preview
@Composable
fun SplashScreenGirlLightPreview() {
    BabyGrowthTheme(genderTheme = GenderTheme.GIRL, darkTheme = false) {
        CompleteSplashScreen(onSplashComplete = {})
    }
}
//(name = "Girl - Dark", widthDp = 360, heightDp = 640)
@Preview
@Composable
fun SplashScreenGirlDarkPreview() {
    BabyGrowthTheme(genderTheme = GenderTheme.GIRL, darkTheme = true) {
        CompleteSplashScreen(onSplashComplete = {})
    }
}
//(name = "Boy - Light", widthDp = 360, heightDp = 640)
@Preview
@Composable
fun SplashScreenBoyLightPreview() {
    BabyGrowthTheme(genderTheme = GenderTheme.BOY, darkTheme = false) {
        CompleteSplashScreen(onSplashComplete = {})
    }
}

//(name = "Boy - Dark", widthDp = 360, heightDp = 640)
@Preview
@Composable
fun SplashScreenBoyDarkPreview() {
    BabyGrowthTheme(genderTheme = GenderTheme.BOY, darkTheme = true) {
        CompleteSplashScreen(onSplashComplete = {})
    }
}

//(name = "Tablet - Girl Light", widthDp = 800, heightDp = 1280)
@Preview
@Composable
fun SplashScreenTabletGirlPreview() {
    BabyGrowthTheme(genderTheme = GenderTheme.GIRL, darkTheme = false) {
        CompleteSplashScreen(onSplashComplete = {})
    }
}

//(name = "Desktop - Boy Light", widthDp = 1920, heightDp = 1080)
@Preview
@Composable
fun SplashScreenDesktopBoyPreview() {
    BabyGrowthTheme(genderTheme = GenderTheme.BOY, darkTheme = false) {
        CompleteSplashScreen(onSplashComplete = {})
    }
}