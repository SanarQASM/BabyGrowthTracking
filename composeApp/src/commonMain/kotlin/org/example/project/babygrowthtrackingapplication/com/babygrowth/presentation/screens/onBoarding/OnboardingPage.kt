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
import org.example.project.babygrowthtrackingapplication.theme.LocalIsLandscape
import org.example.project.babygrowthtrackingapplication.theme.LocalScreenInfo
import org.example.project.babygrowthtrackingapplication.theme.WindowSizeClass
import org.example.project.babygrowthtrackingapplication.theme.customColors
import org.example.project.babygrowthtrackingapplication.ui.components.PrimaryButton
import org.example.project.babygrowthtrackingapplication.ui.components.AppTextButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.ui.tooling.preview.Preview

data class OnboardingPage(
    val lottieRes: String,
    val title: String,
    val description: String
)

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit
) {
    val dimensions   = LocalDimensions.current
    val screenInfo   = LocalScreenInfo.current
    val customColors = MaterialTheme.customColors
    val isLandscape  = LocalIsLandscape.current

    var currentPage by remember { mutableStateOf(0) }

    val pages = remember {
        listOf(
            OnboardingPage(
                lottieRes   = "files/growth.json",
                title       = Res.string.onboarding_title_1.toString(),
                description = Res.string.onboarding_desc_1.toString()
            ),
            OnboardingPage(
                lottieRes   = "files/health_monitor.json",
                title       = Res.string.onboarding_title_2.toString(),
                description = Res.string.onboarding_desc_2.toString()
            ),
            OnboardingPage(
                lottieRes   = "files/vaccination.json",
                title       = Res.string.onboarding_title_3.toString(),
                description = Res.string.onboarding_desc_3.toString()
            )
        )
    }

    // Reduce animation height in landscape
    val lottieAnimationHeight = when {
        isLandscape -> when (screenInfo.windowSizeClass) {
            WindowSizeClass.COMPACT  -> 140.dp
            WindowSizeClass.MEDIUM   -> 180.dp
            WindowSizeClass.EXPANDED -> 220.dp
        }
        else -> when (screenInfo.windowSizeClass) {
            WindowSizeClass.COMPACT  -> 220.dp
            WindowSizeClass.MEDIUM   -> 280.dp
            WindowSizeClass.EXPANDED -> 320.dp
        }
    }

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
        if (isLandscape) {
            // ── LANDSCAPE: Side-by-side layout ────────────────────────────────
            Column(modifier = Modifier.fillMaxSize()) {
                // Top bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = dimensions.screenPadding,
                            vertical   = dimensions.spacingSmall
                        ),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    if (currentPage > 0) {
                        IconButton(onClick = { if (currentPage > 0) currentPage-- }) {
                            Icon(
                                imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint               = customColors.accentGradientStart,
                                modifier           = Modifier.size(dimensions.iconMedium)
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(dimensions.iconLarge))
                    }
                    AppTextButton(text = stringResource(Res.string.onboarding_skip), onClick = { onFinish() })
                }

                // Main content: animation left, text+button right
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = dimensions.screenPadding),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(dimensions.spacingLarge)
                ) {
                    // Left: Lottie animation
                    Box(
                        modifier         = Modifier
                            .weight(0.45f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedContent(
                            targetState = currentPage,
                            transitionSpec = {
                                (fadeIn(tween(400)) togetherWith fadeOut(tween(300)))
                                    .using(SizeTransform(clip = false))
                            },
                            label = "lottieTransition"
                        ) { page ->
                            OnboardingLottieAnimation(
                                lottieRes = pages[page].lottieRes,
                                modifier  = Modifier
                                    .fillMaxWidth()
                                    .height(lottieAnimationHeight)
                            )
                        }
                    }

                    // Right: Text content + indicators + button
                    Column(
                        modifier            = Modifier
                            .weight(0.55f)
                            .fillMaxHeight()
                            .padding(end = dimensions.spacingSmall),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        AnimatedContent(
                            targetState = currentPage,
                            transitionSpec = {
                                if (targetState > initialState) {
                                    (slideInHorizontally(
                                        initialOffsetX = { it / 2 },
                                        animationSpec  = tween(400)
                                    ) + fadeIn(tween(400))) togetherWith
                                            (slideOutHorizontally(
                                                targetOffsetX = { -it / 2 },
                                                animationSpec = tween(300)
                                            ) + fadeOut(tween(300)))
                                } else {
                                    (slideInHorizontally(
                                        initialOffsetX = { -it / 2 },
                                        animationSpec  = tween(400)
                                    ) + fadeIn(tween(400))) togetherWith
                                            (slideOutHorizontally(
                                                targetOffsetX = { it / 2 },
                                                animationSpec = tween(300)
                                            ) + fadeOut(tween(300)))
                                }.using(SizeTransform(clip = false))
                            },
                            label = "textTransition"
                        ) { page ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = when (page) {
                                        0 -> stringResource(Res.string.onboarding_title_1)
                                        1 -> stringResource(Res.string.onboarding_title_2)
                                        2 -> stringResource(Res.string.onboarding_title_3)
                                        else -> ""
                                    },
                                    style     = MaterialTheme.typography.headlineSmall,
                                    color     = MaterialTheme.colorScheme.onBackground,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(dimensions.spacingSmall))
                                Text(
                                    text = when (page) {
                                        0 -> stringResource(Res.string.onboarding_desc_1)
                                        1 -> stringResource(Res.string.onboarding_desc_2)
                                        2 -> stringResource(Res.string.onboarding_desc_3)
                                        else -> ""
                                    },
                                    style     = MaterialTheme.typography.bodyMedium,
                                    color     = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(dimensions.spacingMedium))

                        // Page indicators
                        Row(horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)) {
                            repeat(pages.size) { index ->
                                Box(
                                    modifier = Modifier
                                        .size(if (index == currentPage) 12.dp else 8.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (index == currentPage)
                                                customColors.accentGradientStart
                                            else
                                                customColors.accentGradientStart.copy(alpha = 0.3f)
                                        )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(dimensions.spacingMedium))

                        PrimaryButton(
                            text = if (currentPage < pages.size - 1)
                                stringResource(Res.string.onboarding_next)
                            else
                                stringResource(Res.string.onboarding_get_started),
                            onClick = {
                                if (currentPage < pages.size - 1) currentPage++
                                else onFinish()
                            },
                            modifier = Modifier
                                .widthIn(min = 200.dp, max = 320.dp)
                                .fillMaxWidth()
                                .height(dimensions.buttonHeight)
                        )
                    }
                }
            }
        } else {
            // ── PORTRAIT: Original vertical layout ───────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimensions.screenPadding)
                    .padding(top = dimensions.spacingMedium),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                if (currentPage > 0) {
                    IconButton(onClick = { if (currentPage > 0) currentPage-- }) {
                        Icon(
                            imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint               = customColors.accentGradientStart,
                            modifier           = Modifier.size(dimensions.iconMedium)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(dimensions.iconLarge))
                }
                AppTextButton(text = stringResource(Res.string.onboarding_skip), onClick = { onFinish() })
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = dimensions.spacingXLarge * 2)
                    .padding(bottom = dimensions.spacingXLarge * 2),
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
                    AnimatedContent(
                        targetState = currentPage,
                        transitionSpec = {
                            if (targetState > initialState) {
                                (slideInHorizontally(
                                    initialOffsetX = { fullWidth -> fullWidth },
                                    animationSpec  = tween(durationMillis = 500, easing = FastOutSlowInEasing)
                                ) + fadeIn(tween(500)))
                                    .togetherWith(
                                        slideOutHorizontally(
                                            targetOffsetX = { fullWidth -> -fullWidth },
                                            animationSpec = tween(500, easing = FastOutSlowInEasing)
                                        ) + fadeOut(tween(500))
                                    )
                            } else {
                                (slideInHorizontally(
                                    initialOffsetX = { fullWidth -> -fullWidth },
                                    animationSpec  = tween(500, easing = FastOutSlowInEasing)
                                ) + fadeIn(tween(500)))
                                    .togetherWith(
                                        slideOutHorizontally(
                                            targetOffsetX = { fullWidth -> fullWidth },
                                            animationSpec = tween(500, easing = FastOutSlowInEasing)
                                        ) + fadeOut(tween(500))
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
                                            dragAmount > 50 -> { if (currentPage > 0) currentPage-- }
                                            dragAmount < -50 -> { if (currentPage < pages.size - 1) currentPage++ }
                                        }
                                    }
                                },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            OnboardingLottieAnimation(
                                lottieRes = pages[page].lottieRes,
                                modifier  = Modifier
                                    .fillMaxWidth()
                                    .height(lottieAnimationHeight)
                            )

                            Spacer(modifier = Modifier.height(dimensions.spacingXLarge))

                            Text(
                                text = when (page) {
                                    0 -> stringResource(Res.string.onboarding_title_1)
                                    1 -> stringResource(Res.string.onboarding_title_2)
                                    2 -> stringResource(Res.string.onboarding_title_3)
                                    else -> ""
                                },
                                style = when (screenInfo.windowSizeClass) {
                                    WindowSizeClass.COMPACT  -> MaterialTheme.typography.headlineMedium
                                    WindowSizeClass.MEDIUM   -> MaterialTheme.typography.headlineLarge
                                    WindowSizeClass.EXPANDED -> MaterialTheme.typography.displaySmall
                                },
                                color     = MaterialTheme.colorScheme.onBackground,
                                textAlign = TextAlign.Center,
                                modifier  = Modifier.fillMaxWidth().padding(horizontal = dimensions.spacingMedium)
                            )

                            Spacer(modifier = Modifier.height(dimensions.spacingMedium))

                            Text(
                                text = when (page) {
                                    0 -> stringResource(Res.string.onboarding_desc_1)
                                    1 -> stringResource(Res.string.onboarding_desc_2)
                                    2 -> stringResource(Res.string.onboarding_desc_3)
                                    else -> ""
                                },
                                style = when (screenInfo.windowSizeClass) {
                                    WindowSizeClass.COMPACT  -> MaterialTheme.typography.bodyLarge
                                    WindowSizeClass.MEDIUM   -> MaterialTheme.typography.titleMedium
                                    WindowSizeClass.EXPANDED -> MaterialTheme.typography.titleLarge
                                },
                                color     = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center,
                                modifier  = Modifier.fillMaxWidth().padding(horizontal = dimensions.spacingLarge)
                            )

                            Spacer(modifier = Modifier.height(dimensions.spacingXLarge))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall),
                                modifier              = Modifier.padding(vertical = dimensions.spacingMedium)
                            ) {
                                val indicatorSizeInactive = when (screenInfo.windowSizeClass) {
                                    WindowSizeClass.COMPACT  -> 8.dp
                                    WindowSizeClass.MEDIUM   -> 10.dp
                                    WindowSizeClass.EXPANDED -> 12.dp
                                }
                                val indicatorSizeActive = when (screenInfo.windowSizeClass) {
                                    WindowSizeClass.COMPACT  -> 12.dp
                                    WindowSizeClass.MEDIUM   -> 14.dp
                                    WindowSizeClass.EXPANDED -> 16.dp
                                }
                                repeat(pages.size) { index ->
                                    Box(
                                        modifier = Modifier
                                            .size(if (index == currentPage) indicatorSizeActive else indicatorSizeInactive)
                                            .clip(CircleShape)
                                            .background(
                                                if (index == currentPage)
                                                    customColors.accentGradientStart
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

            Box(
                modifier         = Modifier
                    .fillMaxSize()
                    .padding(dimensions.screenPadding)
                    .padding(bottom = dimensions.spacingLarge),
                contentAlignment = Alignment.BottomCenter
            ) {
                PrimaryButton(
                    text = if (currentPage < pages.size - 1)
                        stringResource(Res.string.onboarding_next)
                    else
                        stringResource(Res.string.onboarding_get_started),
                    onClick = {
                        if (currentPage < pages.size - 1) currentPage++
                        else onFinish()
                    },
                    modifier = Modifier
                        .widthIn(min = 280.dp, max = 400.dp)
                        .fillMaxWidth()
                        .height(dimensions.buttonHeight)
                )
            }

            DecorativeCorner(
                imageRes = Res.drawable.bottom_left_background,
                alignment = Alignment.BottomStart,
                fromX = -100f, fromY = 100f,
                size = dimensions.cornerImageSize
            )
            DecorativeCorner(
                imageRes = Res.drawable.bottom_right_background,
                alignment = Alignment.BottomEnd,
                fromX = 100f, fromY = 100f,
                size = dimensions.cornerImageSize
            )
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun OnboardingLottieAnimation(
    lottieRes: String,
    modifier : Modifier = Modifier
) {
    var jsonString by remember(lottieRes) { mutableStateOf<String?>(null) }

    LaunchedEffect(lottieRes) {
        jsonString = withContext(Dispatchers.Default) {
            try { Res.readBytes(lottieRes).decodeToString() }
            catch (e: Exception) { e.printStackTrace(); null }
        }
    }

    jsonString?.let { json ->
        val composition by rememberLottieComposition { LottieCompositionSpec.JsonString(json) }
        val progress    by animateLottieCompositionAsState(
            composition = composition,
            iterations  = Compottie.IterateForever
        )
        val painter = rememberLottiePainter(composition = composition, progress = { progress })
        Image(
            painter            = painter,
            contentDescription = null,
            modifier           = modifier,
            contentScale       = ContentScale.Fit
        )
    }
}

@Composable
fun DecorativeCorner(
    imageRes : org.jetbrains.compose.resources.DrawableResource,
    alignment: Alignment,
    fromX    : Float,
    fromY    : Float,
    size     : Dp,
    modifier : Modifier = Modifier
) {
    val animationSpec = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness    = Spring.StiffnessLow
    )
    val offsetX by animateFloatAsState(targetValue = 0f, animationSpec = animationSpec, label = "offsetX")
    val offsetY by animateFloatAsState(targetValue = 0f, animationSpec = animationSpec, label = "offsetY")
    val scale   by animateFloatAsState(targetValue = 1f, animationSpec = animationSpec, label = "scale")

    Box(modifier = modifier.fillMaxSize(), contentAlignment = alignment) {
        Image(
            painter            = painterResource(imageRes),
            contentDescription = "Decorative corner",
            modifier           = Modifier
                .size(size)
                .graphicsLayer {
                    translationX = lerp(fromX, offsetX, 1f)
                    translationY = lerp(fromY, offsetY, 1f)
                    scaleX = scale
                    scaleY = scale
                }
                .alpha(scale),
            contentScale = ContentScale.Crop
        )
    }
}

private fun lerp(start: Float, stop: Float, fraction: Float): Float =
    start + fraction * (stop - start)

@Preview
@Composable
fun OnboardingNeutralLightPreview() {
    BabyGrowthTheme(genderTheme = GenderTheme.NEUTRAL, darkTheme = false) {
        OnboardingScreen(onFinish = {})
    }
}