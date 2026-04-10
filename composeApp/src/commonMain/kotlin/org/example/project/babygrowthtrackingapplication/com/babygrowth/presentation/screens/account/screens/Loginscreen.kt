package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.account.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.github.alexzhirkevich.compottie.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.example.project.babygrowthtrackingapplication.theme.*
import org.jetbrains.compose.resources.*
import babygrowthtrackingapplication.composeapp.generated.resources.Res
import babygrowthtrackingapplication.composeapp.generated.resources.*
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.account.models.LoginUiState
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.account.models.LoginViewModel
import org.example.project.babygrowthtrackingapplication.ui.components.PrimaryButton
import org.example.project.babygrowthtrackingapplication.ui.components.GlassmorphicTextField
import org.example.project.babygrowthtrackingapplication.ui.components.SocialLoginSection

/**
 * REFACTORED:
 *  - avatarLarge * 5 min widthIn  →  dimensions.authCardMinWidth
 *  - avatarLarge * 6 max widthIn  →  dimensions.authCardMaxWidth
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onBackClick: () -> Unit,
    onLoginSuccess: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope
) {
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors
    val focusManager = LocalFocusManager.current
    val isLandscape  = LocalIsLandscape.current

    val uiState          = viewModel.uiState
    var animationStarted by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        animationStarted = true
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
            // ── LANDSCAPE: Two-column layout ──────────────────────────────────
            Row(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .weight(0.4f)
                        .fillMaxHeight()
                        .padding(horizontal = dimensions.screenPadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.TopStart
                    ) {
                        IconButton(
                            onClick  = onBackClick,
                            modifier = Modifier.padding(top = dimensions.spacingSmall)
                        ) {
                            Icon(
                                imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(Res.string.login_back),
                                tint               = customColors.accentGradientStart
                            )
                        }
                    }

                    AnimatedLogoSection(
                        animationStarted      = animationStarted,
                        sharedTransitionScope = sharedTransitionScope,
                        animatedContentScope  = animatedContentScope,
                        modifier              = Modifier.wrapContentHeight()
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(0.6f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                        .padding(
                            horizontal = dimensions.screenPadding,
                            vertical   = dimensions.spacingMedium
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    AnimatedLoginCard(
                        animationStarted           = animationStarted,
                        uiState                    = uiState,
                        viewModel                  = viewModel,
                        onLoginSuccess             = onLoginSuccess,
                        onEmailOrPhoneChange       = viewModel::onEmailOrPhoneChanged,
                        onPasswordChange           = viewModel::onPasswordChanged,
                        onPasswordVisibilityToggle = viewModel::onPasswordVisibilityToggled,
                        onSavePasswordToggle       = viewModel::onSavePasswordToggled,
                        onForgotPasswordClick      = onForgotPasswordClick,
                        onLoginClick               = { viewModel.login(onLoginSuccess) },
                        focusManager               = focusManager,
                        sharedTransitionScope      = sharedTransitionScope,
                        animatedContentScope       = animatedContentScope,
                        modifier                   = Modifier.fillMaxWidth()
                    )
                }
            }
        } else {
            // ── PORTRAIT: Original vertical scrollable layout ─────────────────
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = dimensions.screenPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        // WAS: .widthIn(min = dimensions.avatarLarge * 5, max = dimensions.avatarLarge * 6)
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
                            contentDescription = stringResource(Res.string.login_back),
                            tint               = customColors.accentGradientStart
                        )
                    }
                }

                Spacer(modifier = Modifier.height(dimensions.spacingMedium))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        // WAS: .widthIn(min = dimensions.avatarLarge * 5, max = dimensions.avatarLarge * 6)
                        .widthIn(
                            min = dimensions.authCardMinWidth,
                            max = dimensions.authCardMaxWidth
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedLogoSection(
                        animationStarted      = animationStarted,
                        sharedTransitionScope = sharedTransitionScope,
                        animatedContentScope  = animatedContentScope,
                        modifier              = Modifier.wrapContentHeight()
                    )
                }

                Spacer(modifier = Modifier.height(dimensions.spacingLarge))

                AnimatedLoginCard(
                    animationStarted           = animationStarted,
                    uiState                    = uiState,
                    viewModel                  = viewModel,
                    onLoginSuccess             = onLoginSuccess,
                    onEmailOrPhoneChange       = viewModel::onEmailOrPhoneChanged,
                    onPasswordChange           = viewModel::onPasswordChanged,
                    onPasswordVisibilityToggle = viewModel::onPasswordVisibilityToggled,
                    onSavePasswordToggle       = viewModel::onSavePasswordToggled,
                    onForgotPasswordClick      = onForgotPasswordClick,
                    onLoginClick               = { viewModel.login(onLoginSuccess) },
                    focusManager               = focusManager,
                    sharedTransitionScope      = sharedTransitionScope,
                    animatedContentScope       = animatedContentScope,
                    modifier                   = Modifier
                        // WAS: .widthIn(min = dimensions.avatarLarge * 5, max = dimensions.avatarLarge * 6)
                        .widthIn(
                            min = dimensions.authCardMinWidth,
                            max = dimensions.authCardMaxWidth
                        )
                        .fillMaxWidth()
                        .padding(bottom = dimensions.spacingXXLarge * 2)
                )
            }

            LoginDecorativeCorner(
                imageRes         = Res.drawable.bottom_left_background,
                alignment        = Alignment.BottomStart,
                fromX            = -100f, fromY = 100f,
                size             = dimensions.cornerImageSize,
                animationStarted = animationStarted,
                delayMillis      = 200
            )
            LoginDecorativeCorner(
                imageRes         = Res.drawable.bottom_right_background,
                alignment        = Alignment.BottomEnd,
                fromX            = 100f, fromY = 100f,
                size             = dimensions.cornerImageSize,
                animationStarted = animationStarted,
                delayMillis      = 300
            )
        }
    }
}

@Composable
private fun LoginDecorativeCorner(
    imageRes: DrawableResource,
    alignment: Alignment,
    fromX: Float, fromY: Float,
    size: Dp,
    animationStarted: Boolean,
    delayMillis: Int,
    modifier: Modifier = Modifier
) {
    val spec    = spring<Float>(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
    val offsetX by animateFloatAsState(if (animationStarted) 0f else fromX, spec, label = "x")
    val offsetY by animateFloatAsState(if (animationStarted) 0f else fromY, spec, label = "y")
    val scale   by animateFloatAsState(if (animationStarted) 1f else 0f, spec, label = "scale")
    val alpha   by animateFloatAsState(
        if (animationStarted) 1f else 0f,
        tween(800, delayMillis, FastOutSlowInEasing), label = "alpha"
    )
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(modifier.fillMaxSize(), contentAlignment = alignment) {
            Image(
                painter            = painterResource(imageRes),
                contentDescription = null,
                modifier           = Modifier
                    .size(size)
                    .graphicsLayer { translationX = offsetX; translationY = offsetY; scaleX = scale; scaleY = scale }
                    .alpha(alpha),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@OptIn(ExperimentalResourceApi::class, ExperimentalSharedTransitionApi::class)
@Composable
private fun AnimatedLogoSection(
    animationStarted: Boolean,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    modifier: Modifier = Modifier
) {
    val dimensions  = LocalDimensions.current
    val isLandscape = LocalIsLandscape.current
    var jsonString by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        jsonString = withContext(Dispatchers.Default) {
            try { Res.readBytes("files/login.json").decodeToString() }
            catch (e: Exception) { e.printStackTrace(); null }
        }
    }

    val offsetY by animateFloatAsState(
        targetValue   = if (animationStarted) 0f else -100f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow),
        label         = "logoOffset"
    )
    val alpha by animateFloatAsState(
        targetValue   = if (animationStarted) 1f else 0f,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label         = "logoAlpha"
    )

    val logoSize = if (isLandscape) dimensions.logoSize * 0.55f else dimensions.logoSize

    with(sharedTransitionScope) {
        Box(
            modifier = modifier
                .graphicsLayer { translationY = offsetY }
                .alpha(alpha)
                .sharedBounds(
                    rememberSharedContentState("lottie_animation"),
                    animatedContentScope,
                    boundsTransform = { _, _ -> tween(600, easing = FastOutSlowInEasing) }
                ),
            contentAlignment = Alignment.Center
        ) {
            jsonString?.let { json ->
                val composition by rememberLottieComposition {
                    LottieCompositionSpec.JsonString(json)
                }
                val progress by animateLottieCompositionAsState(
                    composition = composition,
                    iterations  = Compottie.IterateForever
                )
                val painter = rememberLottiePainter(
                    composition = composition,
                    progress    = { progress }
                )
                Image(
                    painter            = painter,
                    contentDescription = stringResource(Res.string.login_logo_description),
                    modifier           = Modifier.size(logoSize),
                    contentScale       = ContentScale.Fit
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun AnimatedLoginCard(
    animationStarted: Boolean,
    uiState: LoginUiState,
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit,
    onEmailOrPhoneChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibilityToggle: () -> Unit,
    onSavePasswordToggle: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onLoginClick: () -> Unit,
    focusManager: FocusManager,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    modifier: Modifier = Modifier
) {
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors
    val isLandscape  = LocalIsLandscape.current

    val offsetY by animateFloatAsState(
        targetValue   = if (animationStarted) 0f else 400f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow),
        label         = "cardOffset"
    )
    val alpha by animateFloatAsState(
        targetValue   = if (animationStarted) 1f else 0f,
        animationSpec = tween(800, delayMillis = 400, easing = FastOutSlowInEasing),
        label         = "cardAlpha"
    )

    val cardCornerRadius = if (isLandscape)
        RoundedCornerShape(dimensions.cardCornerRadius)
    else
        RoundedCornerShape(topStart = dimensions.cardCornerRadius * 2, topEnd = dimensions.cardCornerRadius * 2)

    with(sharedTransitionScope) {
        Box(
            modifier = modifier
                .graphicsLayer { translationY = offsetY }
                .alpha(alpha)
                .sharedBounds(
                    rememberSharedContentState("card_background"),
                    animatedContentScope,
                    boundsTransform = { _, _ -> tween(600, easing = FastOutSlowInEasing) }
                )
        ) {
            if (!isLandscape) {
                Image(
                    painter            = painterResource(Res.drawable.baby_background),
                    contentDescription = null,
                    modifier           = Modifier
                        .fillMaxWidth()
                        .matchParentSize()
                        .clip(cardCornerRadius)
                        .alpha(0.3f),
                    contentScale = ContentScale.Crop
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(cardCornerRadius)
                    .background(customColors.glassBackground)
                    .padding(dimensions.spacingLarge),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text     = stringResource(Res.string.login_select_one),
                    style    = MaterialTheme.typography.titleMedium,
                    color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                    modifier = Modifier.padding(bottom = dimensions.spacingMedium)
                )

                GlassmorphicTextField(
                    value         = uiState.emailOrPhone,
                    onValueChange = onEmailOrPhoneChange,
                    placeholder   = stringResource(Res.string.login_email_or_phone_placeholder),
                    leadingIcon   = {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Email or Phone",
                            tint               = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction    = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(dimensions.spacingMedium))

                GlassmorphicTextField(
                    value         = uiState.password,
                    onValueChange = onPasswordChange,
                    placeholder   = stringResource(Res.string.login_password_placeholder),
                    leadingIcon   = {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = "Password",
                            tint               = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    },
                    trailingIcon  = {
                        IconButton(onClick = onPasswordVisibilityToggle) {
                            Icon(
                                imageVector        = if (uiState.passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (uiState.passwordVisible)
                                    stringResource(Res.string.login_hide_password)
                                else
                                    stringResource(Res.string.login_show_password),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    },
                    visualTransformation = if (uiState.passwordVisible)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction    = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus(); onLoginClick() }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(dimensions.spacingSmall))

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier          = Modifier.clickable { onSavePasswordToggle() }
                    ) {
                        Checkbox(
                            checked         = uiState.savePassword,
                            onCheckedChange = { onSavePasswordToggle() },
                            colors          = CheckboxDefaults.colors(
                                checkedColor   = customColors.accentGradientStart,
                                uncheckedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        )
                        Text(
                            text  = stringResource(Res.string.login_save_password),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }

                    Text(
                        text     = stringResource(Res.string.login_forget_password),
                        style    = MaterialTheme.typography.labelMedium.copy(
                            textDecoration = TextDecoration.Underline,
                            fontWeight     = FontWeight.Medium
                        ),
                        color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        modifier = Modifier.clickable { onForgotPasswordClick() }
                            .widthIn(max = 140.dp)
                    )
                }

                uiState.errorMessage?.let { error ->
                    Spacer(Modifier.height(dimensions.spacingSmall))
                    Text(
                        text     = error,
                        style    = MaterialTheme.typography.bodySmall,
                        color    = MaterialTheme.colorScheme.error,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(dimensions.spacingLarge))

                PrimaryButton(
                    text     = stringResource(Res.string.login_button),
                    onClick  = onLoginClick,
                    loading  = uiState.isLoading,
                    enabled  = !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(dimensions.spacingMedium))

                SocialLoginSection(
                    onGoogleClick = { viewModel.loginWithGoogle(onLoginSuccess) },
                    modifier      = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(dimensions.spacingMedium))
            }
        }
    }
}