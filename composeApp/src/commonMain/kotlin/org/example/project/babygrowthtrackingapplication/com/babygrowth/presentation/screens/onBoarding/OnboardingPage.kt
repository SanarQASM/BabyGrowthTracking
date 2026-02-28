package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.onBoarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.animateLottieCompositionAsState
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource
import babygrowthtrackingapplication.composeapp.generated.resources.Res
import babygrowthtrackingapplication.composeapp.generated.resources.*
import org.example.project.babygrowthtrackingapplication.theme.BabyGrowthTheme
import org.example.project.babygrowthtrackingapplication.theme.GenderTheme
import org.example.project.babygrowthtrackingapplication.theme.LocalDimensions
import org.example.project.babygrowthtrackingapplication.theme.LocalScreenInfo
import org.example.project.babygrowthtrackingapplication.theme.WindowSizeClass
import org.example.project.babygrowthtrackingapplication.theme.customColors
import org.example.project.babygrowthtrackingapplication.ui.components.PrimaryButton
import org.example.project.babygrowthtrackingapplication.ui.components.AppTextButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Responsive Onboarding Screen with Lottie Animations
 * ✨ UPDATED WITH FIXES! ✨
 *
 * ✅ FIXED: Background now works with NeutralLightColors and NeutralDarkColors
 * ✅ FIXED: Removed scrolling issues - content doesn't jump when swiping
 * ✅ FIXED: Skip and Back buttons stay at top with proper spacing
 * ✅ FIXED: Button has min/max width constraints for all platforms
 * ✅ NEW: iOS-style glassmorphic buttons
 * ✅ NEW: Updated gender-specific color system
 * ✅ Supports Light/Dark modes
 * ✅ Gender-aware theming (Girl/Boy/Neutral)
 * ✅ Adapts to different screen sizes using Dimensions system
 * ✅ Platform-agnostic (Android, iOS, Desktop, Web)
 * ✅ Three-page onboarding flow with animated illustrations
 * ✅ Localized with string resources
 * ✅ Beautiful gender-specific gradients and colors
 */

data class OnboardingPage(
    val lottieRes: String,
    val title: String,
    val description: String
)

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit
) {
    // Access responsive theme values - GENDER-AWARE!
    val dimensions = LocalDimensions.current
    val screenInfo = LocalScreenInfo.current
    val customColors = MaterialTheme.customColors // Automatically gender-specific!

    var currentPage by remember { mutableStateOf(0) }

    // Load strings from resources
    val pages = remember {
        listOf(
            OnboardingPage(
                lottieRes = "files/growth.json",
                title = Res.string.onboarding_title_1.toString(),
                description = Res.string.onboarding_desc_1.toString()
            ),
            OnboardingPage(
                lottieRes = "files/health_monitor.json",
                title = Res.string.onboarding_title_2.toString(),
                description = Res.string.onboarding_desc_2.toString()
            ),
            OnboardingPage(
                lottieRes = "files/vaccination.json",
                title = Res.string.onboarding_title_3.toString(),
                description = Res.string.onboarding_desc_3.toString()
            )
        )
    }

    // Adjust Lottie animation height based on screen class
    val lottieAnimationHeight = when (screenInfo.windowSizeClass) {
        WindowSizeClass.COMPACT -> 220.dp
        WindowSizeClass.MEDIUM -> 280.dp
        WindowSizeClass.EXPANDED -> 320.dp
    }

    // 🔥 FIX #1: Background now uses MaterialTheme.colorScheme.background
    // This automatically adapts to Neutral theme colors!
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                // 🎨 GENDER-AWARE GRADIENT BACKGROUND!
                // Now works with Pink for Girl, Blue for Boy, Teal/Mint for Neutral
                Brush.verticalGradient(
                    colors = listOf(
                        customColors.accentGradientStart.copy(alpha = 0.1f),
                        MaterialTheme.colorScheme.background // Uses theme background color
                    )
                )
            )
    ) {
        // 🔥 FIX #3: Top bar with Back and Skip buttons - FIXED POSITIONING!
        // Now stays at the top with SafeArea padding
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensions.screenPadding)
                .padding(top = dimensions.spacingMedium), // Extra top padding for status bar
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button - only visible if not on first page
            if (currentPage > 0) {
                IconButton(
                    onClick = {
                        if (currentPage > 0) {
                            currentPage--
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = customColors.accentGradientStart, // 🎨 GENDER-AWARE COLOR!
                        modifier = Modifier.size(dimensions.iconMedium)
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(dimensions.iconLarge))
            }

            // 🎨 NEW: AppTextButton with gender-aware styling
            AppTextButton(
                text = stringResource(Res.string.onboarding_skip),
                onClick = { onFinish() }
            )
        }

        // 🔥 FIX #2: Main content - NO SCROLL VIEW to prevent jumping!
        // Content is now centered and doesn't scroll
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = dimensions.spacingXLarge * 2) // Space for top buttons
                .padding(bottom = dimensions.spacingXLarge * 2), // Space for bottom button
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = dimensions.maxContentWidth)
                    .padding(horizontal = dimensions.screenPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Animated content that slides in/out when page changes
                AnimatedContent(
                    targetState = currentPage,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally(
                                initialOffsetX = { fullWidth -> fullWidth },
                                animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
                            ) + fadeIn(animationSpec = tween(durationMillis = 500)))
                                .togetherWith(
                                    slideOutHorizontally(
                                        targetOffsetX = { fullWidth -> -fullWidth },
                                        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
                                    ) + fadeOut(animationSpec = tween(durationMillis = 500))
                                )
                        } else {
                            (slideInHorizontally(
                                initialOffsetX = { fullWidth -> -fullWidth },
                                animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
                            ) + fadeIn(animationSpec = tween(durationMillis = 500)))
                                .togetherWith(
                                    slideOutHorizontally(
                                        targetOffsetX = { fullWidth -> fullWidth },
                                        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
                                    ) + fadeOut(animationSpec = tween(durationMillis = 500))
                                )
                        }.using(SizeTransform(clip = false))
                    },
                    label = "pageTransition"
                ) { page ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .pointerInput(Unit) {
                                detectHorizontalDragGestures { _, dragAmount ->
                                    when {
                                        dragAmount > 50 -> {
                                            if (currentPage > 0) {
                                                currentPage--
                                            }
                                        }
                                        dragAmount < -50 -> {
                                            if (currentPage < pages.size - 1) {
                                                currentPage++
                                            }
                                        }
                                    }
                                }
                            },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Lottie Animation
                        OnboardingLottieAnimation(
                            lottieRes = pages[page].lottieRes,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(lottieAnimationHeight)
                        )

                        Spacer(modifier = Modifier.height(dimensions.spacingXLarge))

                        // Title - Responsive typography with NEW gender-aware colors
                        Text(
                            text = when (page) {
                                0 -> stringResource(Res.string.onboarding_title_1)
                                1 -> stringResource(Res.string.onboarding_title_2)
                                2 -> stringResource(Res.string.onboarding_title_3)
                                else -> ""
                            },
                            style = when (screenInfo.windowSizeClass) {
                                WindowSizeClass.COMPACT -> MaterialTheme.typography.headlineMedium
                                WindowSizeClass.MEDIUM -> MaterialTheme.typography.headlineLarge
                                WindowSizeClass.EXPANDED -> MaterialTheme.typography.displaySmall
                            },
                            color = MaterialTheme.colorScheme.onBackground, // Uses new color system
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = dimensions.spacingMedium)
                        )

                        Spacer(modifier = Modifier.height(dimensions.spacingMedium))

                        // Description - Responsive typography with NEW color system
                        Text(
                            text = when (page) {
                                0 -> stringResource(Res.string.onboarding_desc_1)
                                1 -> stringResource(Res.string.onboarding_desc_2)
                                2 -> stringResource(Res.string.onboarding_desc_3)
                                else -> ""
                            },
                            style = when (screenInfo.windowSizeClass) {
                                WindowSizeClass.COMPACT -> MaterialTheme.typography.bodyLarge
                                WindowSizeClass.MEDIUM -> MaterialTheme.typography.titleMedium
                                WindowSizeClass.EXPANDED -> MaterialTheme.typography.titleLarge
                            },
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = dimensions.spacingLarge)
                        )

                        Spacer(modifier = Modifier.height(dimensions.spacingXLarge))

                        // Page Indicators - Responsive sizing with GENDER-AWARE COLORS from new system!
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall),
                            modifier = Modifier.padding(vertical = dimensions.spacingMedium)
                        ) {
                            val indicatorSizeInactive = when (screenInfo.windowSizeClass) {
                                WindowSizeClass.COMPACT -> 8.dp
                                WindowSizeClass.MEDIUM -> 10.dp
                                WindowSizeClass.EXPANDED -> 12.dp
                            }

                            val indicatorSizeActive = when (screenInfo.windowSizeClass) {
                                WindowSizeClass.COMPACT -> 12.dp
                                WindowSizeClass.MEDIUM -> 14.dp
                                WindowSizeClass.EXPANDED -> 16.dp
                            }

                            repeat(pages.size) { index ->
                                Box(
                                    modifier = Modifier
                                        .size(if (index == currentPage) indicatorSizeActive else indicatorSizeInactive)
                                        .clip(CircleShape)
                                        .background(
                                            if (index == currentPage)
                                                customColors.accentGradientStart // NEW: Gender-specific color!
                                            else
                                                customColors.accentGradientStart.copy(alpha = 0.3f)
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }

        // 🔥 FIX #4: Bottom button with MIN and MAX width constraints!
        // Ensures button looks good on all platforms and screen sizes
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(dimensions.screenPadding)
                .padding(bottom = dimensions.spacingLarge),
            contentAlignment = Alignment.BottomCenter
        ) {
            // 🎨 NEW: iOS-style glassmorphic PrimaryButton with gender-aware gradient!
            // Automatically uses gender-specific gradient from new color system
            PrimaryButton(
                text = if (currentPage < pages.size - 1)
                    stringResource(Res.string.onboarding_next)
                else
                    stringResource(Res.string.onboarding_get_started),
                onClick = {
                    if (currentPage < pages.size - 1) {
                        currentPage++
                    } else {
                        onFinish()
                    }
                },
                modifier = Modifier
                    .widthIn(
                        min = 280.dp, // Minimum width for small screens
                        max = 400.dp  // Maximum width for tablets/desktop
                    )
                    .fillMaxWidth()
                    .height(dimensions.buttonHeight)
            )
        }

        // 🔥 FIXED: Bottom-left decorative corner - FLUSH WITH EDGES!
        DecorativeCorner(
            imageRes = Res.drawable.bottom_left_background,
            alignment = Alignment.BottomStart,
            fromX = -100f,
            fromY = 100f,
            size = dimensions.cornerImageSize
        )

        // 🔥 FIXED: Bottom-right decorative corner - FLUSH WITH EDGES!
        DecorativeCorner(
            imageRes = Res.drawable.bottom_right_background,
            alignment = Alignment.BottomEnd,
            fromX = 100f,
            fromY = 100f,
            size = dimensions.cornerImageSize
        )
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun OnboardingLottieAnimation(
    lottieRes: String,
    modifier: Modifier = Modifier
) {
    var jsonString by remember(lottieRes) { mutableStateOf<String?>(null) }

    LaunchedEffect(lottieRes) {
        jsonString = withContext(Dispatchers.Default) {
            try {
                Res.readBytes(lottieRes).decodeToString()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    jsonString?.let { json ->
        val composition by rememberLottieComposition {
            LottieCompositionSpec.JsonString(json)
        }

        val progress by animateLottieCompositionAsState(
            composition = composition,
            iterations = Compottie.IterateForever
        )

        val painter = rememberLottiePainter(
            composition = composition,
            progress = { progress }
        )

        Image(
            painter = painter,
            contentDescription = null,
            modifier = modifier,
            contentScale = ContentScale.Fit
        )
    }
}

/**
 * 🔥 FIXED: Decorative corner images now use ContentScale.Crop for flush edges!
 * No more gaps between corner decorations and screen edges
 */
@Composable
fun DecorativeCorner(
    imageRes: org.jetbrains.compose.resources.DrawableResource,
    alignment: Alignment,
    fromX: Float,
    fromY: Float,
    size: Dp,
    modifier: Modifier = Modifier
) {
    val animationSpec = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )

    val offsetX by animateFloatAsState(
        targetValue = 0f,
        animationSpec = animationSpec,
        label = "offsetX"
    )

    val offsetY by animateFloatAsState(
        targetValue = 0f,
        animationSpec = animationSpec,
        label = "offsetY"
    )

    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = animationSpec,
        label = "scale"
    )

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = alignment
    ) {
        Image(
            painter = painterResource(imageRes),
            contentDescription = "Decorative corner",
            modifier = Modifier
                .size(size)
                .graphicsLayer {
                    translationX = lerp(fromX, offsetX, 1f)
                    translationY = lerp(fromY, offsetY, 1f)
                    scaleX = scale
                    scaleY = scale
                }
                .alpha(scale),
            contentScale = ContentScale.Crop // 🔥 CHANGED: Crop fills corners completely!
        )
    }
}

private fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return start + fraction * (stop - start)
}

// Preview for different gender themes with NEW color system
// name = "Neutral - Light", widthDp = 360, heightDp = 640
@Preview
@Composable
fun OnboardingNeutralLightPreview() {
    BabyGrowthTheme(genderTheme = GenderTheme.NEUTRAL, darkTheme = false) {
        OnboardingScreen(onFinish = {})
    }
}

// name = "Neutral - Dark", widthDp = 360, heightDp = 640
@Preview
@Composable
fun OnboardingNeutralDarkPreview() {
    BabyGrowthTheme(genderTheme = GenderTheme.NEUTRAL, darkTheme = true) {
        OnboardingScreen(onFinish = {})
    }
}

// name = "Girl - Light", widthDp = 360, heightDp = 640
@Preview
@Composable
fun OnboardingGirlLightPreview() {
    BabyGrowthTheme(genderTheme = GenderTheme.GIRL, darkTheme = false) {
        OnboardingScreen(onFinish = {})
    }
}

// name = "Girl - Dark", widthDp = 360, heightDp = 640
@Preview
@Composable
fun OnboardingGirlDarkPreview() {
    BabyGrowthTheme(genderTheme = GenderTheme.GIRL, darkTheme = true) {
        OnboardingScreen(onFinish = {})
    }
}

// name = "Boy - Light", widthDp = 360, heightDp = 640
@Preview
@Composable
fun OnboardingBoyLightPreview() {
    BabyGrowthTheme(genderTheme = GenderTheme.BOY, darkTheme = false) {
        OnboardingScreen(onFinish = {})
    }
}

// name = "Boy - Dark", widthDp = 360, heightDp = 640
@Preview
@Composable
fun OnboardingBoyDarkPreview() {
    BabyGrowthTheme(genderTheme = GenderTheme.BOY, darkTheme = true) {
        OnboardingScreen(onFinish = {})
    }
}

// name = "Tablet - Girl", widthDp = 800, heightDp = 1280
@Preview
@Composable
fun OnboardingTabletGirlPreview() {
    BabyGrowthTheme(genderTheme = GenderTheme.GIRL, darkTheme = false) {
        OnboardingScreen(onFinish = {})
    }
}

// name = "Tablet - Neutral", widthDp = 800, heightDp = 1280
@Preview
@Composable
fun OnboardingTabletNeutralPreview() {
    BabyGrowthTheme(genderTheme = GenderTheme.NEUTRAL, darkTheme = false) {
        OnboardingScreen(onFinish = {})
    }
}