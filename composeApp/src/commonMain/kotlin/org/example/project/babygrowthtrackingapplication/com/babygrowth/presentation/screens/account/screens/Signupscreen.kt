// composeApp/src/commonMain/kotlin/org/example/project/babygrowthtrackingapplication/com/babygrowth/presentation/screens/account/screens/SignupScreen.kt
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import io.github.alexzhirkevich.compottie.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.example.project.babygrowthtrackingapplication.theme.*
import org.jetbrains.compose.resources.*
import babygrowthtrackingapplication.composeapp.generated.resources.Res
import babygrowthtrackingapplication.composeapp.generated.resources.*
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.account.models.SignupUiState
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.account.models.SignupViewModel
import org.example.project.babygrowthtrackingapplication.ui.components.GlassmorphicTextField
import org.example.project.babygrowthtrackingapplication.ui.components.OtpTextField
import org.example.project.babygrowthtrackingapplication.ui.components.PrimaryButton
import org.example.project.babygrowthtrackingapplication.ui.components.SocialLoginSection

// ─────────────────────────────────────────────────────────────────────────────
// Internal step enum — drives AnimatedContent inside SignupScreen
// ─────────────────────────────────────────────────────────────────────────────

private enum class SignupStep { FORM, OTP }

/**
 * Single entry-point for the entire signup flow:
 *
 *  FORM step  → user fills name / email / password / phone / city / address
 *               → taps "Sign Up"
 *               → ViewModel calls /pre-register (sends OTP, NO DB row yet)
 *               → transitions internally to OTP step
 *
 *  OTP step   → user enters 6-digit code
 *               → ViewModel calls /verify-signup-code then /complete-registration
 *               → DB row is created (isActive = true) ONLY after OTP passes
 *               → [onRegistrationComplete] fires → caller navigates to Home
 *
 * Google signup bypasses OTP entirely → [onSocialSignupSuccess] fires immediately.
 *
 * @param onBackClick              Navigate back to Welcome (from FORM step).
 * @param onRegistrationComplete   Navigate to Home after OTP-verified registration.
 * @param onSocialSignupSuccess    Navigate to Home after Google signup.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SignupScreen(
    viewModel              : SignupViewModel,
    onBackClick            : () -> Unit,
    onRegistrationComplete : () -> Unit,
    onSocialSignupSuccess  : () -> Unit,
    sharedTransitionScope  : SharedTransitionScope,
    animatedContentScope   : AnimatedContentScope
) {
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors
    val focusManager = LocalFocusManager.current
    val isLandscape  = LocalIsLandscape.current
    val scrollState  = rememberScrollState()
    val uiState      = viewModel.uiState

    // Internal navigation state — never leaks to the caller
    var step             by remember { mutableStateOf(SignupStep.FORM) }
    var animationStarted by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { delay(100); animationStarted = true }

    // When the ViewModel signals that the OTP was sent, advance to OTP step
    LaunchedEffect(uiState.otpSent) {
        if (uiState.otpSent && step == SignupStep.FORM) {
            step = SignupStep.OTP
            viewModel.startResendTimer()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        customColors.accentGradientStart.copy(alpha = 0.1f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .imePadding()
    ) {
        AnimatedContent(
            targetState = step,
            transitionSpec = {
                (slideInHorizontally { it } + fadeIn()) togetherWith
                        (slideOutHorizontally { -it } + fadeOut())
            },
            label = "signupStepTransition"
        ) { currentStep ->
            when (currentStep) {
                SignupStep.FORM -> FormStepContent(
                    viewModel             = viewModel,
                    uiState               = uiState,
                    animationStarted      = animationStarted,
                    isLandscape           = isLandscape,
                    dimensions            = dimensions,
                    customColors          = customColors,
                    focusManager          = focusManager,
                    scrollState           = scrollState,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedContentScope  = animatedContentScope,
                    onBackClick           = onBackClick,
                    onSocialSignupSuccess = onSocialSignupSuccess,
                    onSignupClick = {
                        viewModel.preRegister(
                            onOtpRequired = { step = SignupStep.OTP }
                        )
                    }
                )

                SignupStep.OTP -> OtpStepContent(
                    viewModel            = viewModel,
                    uiState              = uiState,
                    animationStarted     = animationStarted,
                    dimensions           = dimensions,
                    customColors         = customColors,
                    focusManager         = focusManager,
                    scrollState          = scrollState,
                    // Back from OTP → return to FORM so user can fix email
                    onBackClick          = { step = SignupStep.FORM },
                    // OTP verified + DB row created → navigate out
                    onSuccess            = onRegistrationComplete
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// FORM step
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun FormStepContent(
    viewModel            : SignupViewModel,
    uiState              : SignupUiState,
    animationStarted     : Boolean,
    isLandscape          : Boolean,
    dimensions           : Dimensions,
    customColors         : CustomColors,
    focusManager         : FocusManager,
    scrollState          : androidx.compose.foundation.ScrollState,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope : AnimatedContentScope,
    onBackClick          : () -> Unit,
    onSocialSignupSuccess: () -> Unit,
    onSignupClick        : () -> Unit
) {
    if (isLandscape) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Left pane: logo
            Column(
                modifier = Modifier
                    .weight(0.35f)
                    .fillMaxHeight()
                    .padding(horizontal = dimensions.screenPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopStart) {
                    IconButton(
                        onClick  = onBackClick,
                        modifier = Modifier.padding(top = dimensions.spacingSmall)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.common_back),
                            tint               = customColors.accentGradientStart
                        )
                    }
                }
                AnimatedLogoSection(
                    animationStarted     = animationStarted,
                    sharedTransitionScope= sharedTransitionScope,
                    animatedContentScope = animatedContentScope,
                    modifier             = Modifier.wrapContentHeight()
                )
            }
            // Right pane: scrollable form
            Column(
                modifier = Modifier
                    .weight(0.65f)
                    .fillMaxHeight()
                    .verticalScroll(scrollState)
                    .padding(horizontal = dimensions.screenPadding, vertical = dimensions.spacingMedium)
                    .imePadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedFormCard(
                    animationStarted                  = animationStarted,
                    uiState                           = uiState,
                    viewModel                         = viewModel,
                    isLandscape                       = true,
                    dimensions                        = dimensions,
                    customColors                      = customColors,
                    focusManager                      = focusManager,
                    sharedTransitionScope             = sharedTransitionScope,
                    animatedContentScope              = animatedContentScope,
                    onSocialSignupSuccess             = onSocialSignupSuccess,
                    onSignupClick                     = onSignupClick,
                    modifier                          = Modifier.fillMaxWidth()
                )
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = dimensions.screenPadding)
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(min = dimensions.authCardMinWidth, max = dimensions.authCardMaxWidth)
                    .padding(top = dimensions.spacingMedium),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(Res.string.common_back),
                        tint               = customColors.accentGradientStart
                    )
                }
            }
            Spacer(Modifier.height(dimensions.spacingMedium))
            Box(
                modifier         = Modifier
                    .fillMaxWidth()
                    .widthIn(min = dimensions.authCardMinWidth, max = dimensions.authCardMaxWidth),
                contentAlignment = Alignment.Center
            ) {
                AnimatedLogoSection(
                    animationStarted     = animationStarted,
                    sharedTransitionScope= sharedTransitionScope,
                    animatedContentScope = animatedContentScope,
                    modifier             = Modifier.wrapContentHeight()
                )
            }
            Spacer(Modifier.height(dimensions.spacingLarge))
            AnimatedFormCard(
                animationStarted     = animationStarted,
                uiState              = uiState,
                viewModel            = viewModel,
                isLandscape          = false,
                dimensions           = dimensions,
                customColors         = customColors,
                focusManager         = focusManager,
                sharedTransitionScope= sharedTransitionScope,
                animatedContentScope = animatedContentScope,
                onSocialSignupSuccess= onSocialSignupSuccess,
                onSignupClick        = onSignupClick,
                modifier             = Modifier
                    .widthIn(min = dimensions.authCardMinWidth, max = dimensions.authCardMaxWidth)
                    .fillMaxWidth()
                    .padding(bottom = dimensions.spacingXXLarge * 2)
            )
        }
        SignupDecorativeCorner(Res.drawable.bottom_left_background,  Alignment.BottomStart, -100f, 100f, dimensions.cornerImageSize, animationStarted, 200)
        SignupDecorativeCorner(Res.drawable.bottom_right_background, Alignment.BottomEnd,   100f, 100f, dimensions.cornerImageSize, animationStarted, 300)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// OTP step
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun OtpStepContent(
    viewModel        : SignupViewModel,
    uiState          : SignupUiState,
    animationStarted : Boolean,
    dimensions       : Dimensions,
    customColors     : CustomColors,
    focusManager     : FocusManager,
    scrollState      : androidx.compose.foundation.ScrollState,
    onBackClick      : () -> Unit,
    onSuccess        : () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = dimensions.screenPadding)
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(min = dimensions.authCardMinWidth, max = dimensions.authCardMaxWidth)
                .padding(top = dimensions.spacingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(Res.string.common_back),
                    tint               = customColors.accentGradientStart
                )
            }
        }

        Spacer(Modifier.height(dimensions.spacingMedium))

        Box(
            modifier         = Modifier
                .fillMaxWidth()
                .widthIn(min = dimensions.authCardMinWidth, max = dimensions.authCardMaxWidth),
            contentAlignment = Alignment.Center
        ) {
            OtpLogoSection(animationStarted, Modifier.wrapContentHeight(), dimensions)
        }

        Spacer(Modifier.height(dimensions.spacingLarge))

        OtpCard(
            animationStarted = animationStarted,
            email            = uiState.email,
            uiState          = uiState,
            dimensions       = dimensions,
            customColors     = customColors,
            focusManager     = focusManager,
            onOtpChanged     = viewModel::onOtpChanged,
            // verifyOtpAndComplete calls /verify-signup-code then /complete-registration
            // which creates the DB row only after OTP passes
            onVerifyClick    = { viewModel.verifyOtpAndComplete(onSuccess) },
            onResendClick    = viewModel::resendOtp,
            modifier         = Modifier
                .widthIn(min = dimensions.authCardMinWidth, max = dimensions.authCardMaxWidth)
                .fillMaxWidth()
                .padding(bottom = dimensions.spacingXXLarge * 2)
        )
    }

    SignupDecorativeCorner(Res.drawable.bottom_left_background,  Alignment.BottomStart, -100f, 100f, dimensions.cornerImageSize, animationStarted, 200)
    SignupDecorativeCorner(Res.drawable.bottom_right_background, Alignment.BottomEnd,   100f, 100f, dimensions.cornerImageSize, animationStarted, 300)
}

// ─────────────────────────────────────────────────────────────────────────────
// Shared logo (form step) — with shared element transitions
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalResourceApi::class, ExperimentalSharedTransitionApi::class)
@Composable
private fun AnimatedLogoSection(
    animationStarted     : Boolean,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope : AnimatedContentScope,
    modifier             : Modifier = Modifier
) {
    val dimensions  = LocalDimensions.current
    val isLandscape = LocalIsLandscape.current
    var jsonString  by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        jsonString = withContext(Dispatchers.Default) {
            try { Res.readBytes("files/login.json").decodeToString() } catch (e: Exception) { null }
        }
    }

    val offsetY by animateFloatAsState(
        if (animationStarted) 0f else 200f,
        spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow), label = "logoY"
    )
    val alpha by animateFloatAsState(
        if (animationStarted) 1f else 0f,
        tween(800, easing = FastOutSlowInEasing), label = "logoAlpha"
    )

    val logoSize = if (isLandscape) dimensions.logoSize * 0.4f else dimensions.logoSize * 0.5f

    with(sharedTransitionScope) {
        Column(
            modifier = modifier
                .graphicsLayer { translationY = offsetY }
                .alpha(alpha),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            jsonString?.let { json ->
                val composition by rememberLottieComposition { LottieCompositionSpec.JsonString(json) }
                val progress    by animateLottieCompositionAsState(composition, iterations = Compottie.IterateForever)
                val painter      = rememberLottiePainter(composition = composition, progress = { progress })
                Image(
                    painter            = painter,
                    contentDescription = stringResource(Res.string.app_logo_description),
                    modifier           = Modifier
                        .size(logoSize)
                        .sharedBounds(
                            rememberSharedContentState("lottie_animation"),
                            animatedContentScope,
                            boundsTransform = { _, _ -> tween(600, easing = FastOutSlowInEasing) }
                        ),
                    contentScale = ContentScale.Fit
                )
            }
            Spacer(Modifier.height(dimensions.spacingSmall))
            Text(
                text       = stringResource(Res.string.app_name),
                style      = MaterialTheme.typography.headlineSmall,
                color      = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                textAlign  = TextAlign.Center,
                modifier   = Modifier.sharedBounds(
                    rememberSharedContentState("app_name"),
                    animatedContentScope,
                    boundsTransform = { _, _ -> tween(600, easing = FastOutSlowInEasing) }
                )
            )
            if (!isLandscape) {
                Spacer(Modifier.height(dimensions.spacingSmall))
                Text(
                    text      = stringResource(Res.string.signup_page_title),
                    style     = MaterialTheme.typography.titleLarge,
                    color     = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// OTP step logo (no shared-element — different nav scope)
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun OtpLogoSection(
    animationStarted: Boolean,
    modifier        : Modifier = Modifier,
    dimensions      : Dimensions
) {
    var jsonString by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        jsonString = withContext(Dispatchers.Default) {
            try { Res.readBytes("files/login.json").decodeToString() } catch (e: Exception) { null }
        }
    }

    val offsetY by animateFloatAsState(
        if (animationStarted) 0f else 200f,
        spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow), label = "otpLogoY"
    )
    val alpha by animateFloatAsState(
        if (animationStarted) 1f else 0f,
        tween(800, easing = FastOutSlowInEasing), label = "otpLogoAlpha"
    )

    Column(
        modifier            = modifier.graphicsLayer { translationY = offsetY }.alpha(alpha),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        jsonString?.let { json ->
            val composition by rememberLottieComposition { LottieCompositionSpec.JsonString(json) }
            val progress    by animateLottieCompositionAsState(composition, iterations = Compottie.IterateForever)
            Image(
                rememberLottiePainter(composition, { progress }),
                contentDescription = stringResource(Res.string.app_logo_description),
                modifier           = Modifier.size(dimensions.logoSize * 0.4f),
                contentScale       = ContentScale.Fit
            )
        }
        Spacer(Modifier.height(dimensions.spacingSmall))
        Text(
            stringResource(Res.string.app_name),
            style      = MaterialTheme.typography.headlineSmall,
            color      = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            textAlign  = TextAlign.Center
        )
        Spacer(Modifier.height(dimensions.spacingXSmall))
        Text(
            stringResource(Res.string.signup_otp_title),
            style     = MaterialTheme.typography.titleLarge,
            color     = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Animated form card
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun AnimatedFormCard(
    animationStarted     : Boolean,
    uiState              : SignupUiState,
    viewModel            : SignupViewModel,
    isLandscape          : Boolean,
    dimensions           : Dimensions,
    customColors         : CustomColors,
    focusManager         : FocusManager,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope : AnimatedContentScope,
    onSocialSignupSuccess: () -> Unit,
    onSignupClick        : () -> Unit,
    modifier             : Modifier = Modifier
) {
    val offsetY by animateFloatAsState(
        if (animationStarted) 0f else 400f,
        spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow), label = "cardY"
    )
    val alpha by animateFloatAsState(
        if (animationStarted) 1f else 0f,
        tween(800, delayMillis = 400, easing = FastOutSlowInEasing), label = "cardAlpha"
    )

    val cardShape = if (isLandscape)
        RoundedCornerShape(dimensions.cardCornerRadius)
    else
        RoundedCornerShape(dimensions.cardCornerRadius * 2)

    with(sharedTransitionScope) {
        Box(
            modifier = modifier
                .graphicsLayer { translationY = offsetY }
                .alpha(alpha)
                .wrapContentHeight()
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
                        .clip(cardShape)
                        .alpha(0.3f),               // intentional design constant
                    contentScale = ContentScale.Crop
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clip(cardShape)
                    .background(customColors.glassBackground)
                    .padding(dimensions.spacingLarge),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text     = stringResource(Res.string.welcome_select_one),
                    style    = MaterialTheme.typography.titleMedium,
                    color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                    modifier = Modifier.padding(bottom = dimensions.spacingMedium)
                )

                if (isLandscape) {
                    SignupFieldsLandscape(
                        uiState                           = uiState,
                        onFullNameChange                  = viewModel::onFullNameChanged,
                        onEmailChange                     = viewModel::onEmailChanged,
                        onPasswordChange                  = viewModel::onPasswordChanged,
                        onConfirmPasswordChange           = viewModel::onConfirmPasswordChanged,
                        onPhoneChange                     = viewModel::onPhoneChanged,
                        onCityChange                      = viewModel::onCityChanged,
                        onAddressChange                   = viewModel::onAddressChanged,
                        onPasswordVisibilityToggle        = viewModel::onPasswordVisibilityToggled,
                        onConfirmPasswordVisibilityToggle = viewModel::onConfirmPasswordVisibilityToggled,
                        onSignupClick                     = onSignupClick,
                        focusManager                      = focusManager,
                        dimensions                        = dimensions
                    )
                } else {
                    SignupFieldsPortrait(
                        uiState                           = uiState,
                        onFullNameChange                  = viewModel::onFullNameChanged,
                        onEmailChange                     = viewModel::onEmailChanged,
                        onPasswordChange                  = viewModel::onPasswordChanged,
                        onConfirmPasswordChange           = viewModel::onConfirmPasswordChanged,
                        onPhoneChange                     = viewModel::onPhoneChanged,
                        onCityChange                      = viewModel::onCityChanged,
                        onAddressChange                   = viewModel::onAddressChanged,
                        onPasswordVisibilityToggle        = viewModel::onPasswordVisibilityToggled,
                        onConfirmPasswordVisibilityToggle = viewModel::onConfirmPasswordVisibilityToggled,
                        onSignupClick                     = onSignupClick,
                        focusManager                      = focusManager,
                        dimensions                        = dimensions
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
                    text     = stringResource(Res.string.signup_button),
                    onClick  = onSignupClick,
                    // isLoading covers the /pre-register API call
                    loading  = uiState.isLoading,
                    enabled  = !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(dimensions.spacingMedium))

                SocialLoginSection(
                    onGoogleClick = { viewModel.signupWithGoogle(onSocialSignupSuccess) },
                    modifier      = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(dimensions.spacingMedium))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// OTP card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun OtpCard(
    animationStarted: Boolean,
    email           : String,
    uiState         : SignupUiState,
    dimensions      : Dimensions,
    customColors    : CustomColors,
    focusManager    : FocusManager,
    onOtpChanged    : (String) -> Unit,
    onVerifyClick   : () -> Unit,
    onResendClick   : () -> Unit,
    modifier        : Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(dimensions.cardCornerRadius * 2)

    val offsetY by animateFloatAsState(
        if (animationStarted) 0f else 400f,
        spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow), label = "otpCardY"
    )
    val alpha by animateFloatAsState(
        if (animationStarted) 1f else 0f,
        tween(800, 400, FastOutSlowInEasing), label = "otpCardAlpha"
    )

    Box(modifier.graphicsLayer { translationY = offsetY }.alpha(alpha).wrapContentHeight()) {
        Image(
            painterResource(Res.drawable.baby_background), null,
            modifier     = Modifier.fillMaxWidth().matchParentSize().clip(cardShape).alpha(0.3f),
            contentScale = ContentScale.Crop
        )
        Column(
            Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .clip(cardShape)
                .background(customColors.glassBackground)
                .padding(dimensions.spacingLarge),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                stringResource(Res.string.signup_otp_enter_code),
                style    = MaterialTheme.typography.titleLarge,
                color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                modifier = Modifier.padding(bottom = dimensions.spacingSmall)
            )
            Text(
                stringResource(Res.string.signup_otp_sent_to, maskEmail(email)),
                style     = MaterialTheme.typography.bodySmall,
                color     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier  = Modifier.padding(bottom = dimensions.spacingLarge)
            )

            OtpTextField(
                value         = uiState.otpCode,
                onValueChange = onOtpChanged,
                length        = 6,
                onComplete    = {
                    focusManager.clearFocus()
                    if (uiState.otpCode.length == 6) onVerifyClick()
                },
                modifier      = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimensions.spacingMedium)
            )

            Spacer(Modifier.height(dimensions.spacingSmall))

            // Resend row
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(Res.string.verify_didnt_receive),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(Modifier.width(dimensions.spacingXSmall))
                Text(
                    text = if (uiState.canResend)
                        stringResource(Res.string.verify_resend)
                    else
                        stringResource(Res.string.verify_resend_in, uiState.resendCountdown),
                    style    = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color    = if (uiState.canResend)
                        customColors.accentGradientStart
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    modifier = Modifier.clickable(enabled = uiState.canResend) { onResendClick() }
                )
            }

            uiState.errorMessage?.let { error ->
                Spacer(Modifier.height(dimensions.spacingSmall))
                Text(
                    text      = error,
                    style     = MaterialTheme.typography.bodySmall,
                    color     = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier  = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(dimensions.spacingLarge))

            val isWorking = uiState.isVerifying || uiState.isLoading
            PrimaryButton(
                text     = stringResource(Res.string.signup_otp_verify_button),
                onClick  = onVerifyClick,
                loading  = isWorking,
                enabled  = !isWorking && uiState.otpCode.length == 6,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(dimensions.spacingMedium))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Portrait form fields (single column)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SignupFieldsPortrait(
    uiState                          : SignupUiState,
    onFullNameChange                 : (String) -> Unit,
    onEmailChange                    : (String) -> Unit,
    onPasswordChange                 : (String) -> Unit,
    onConfirmPasswordChange          : (String) -> Unit,
    onPhoneChange                    : (String) -> Unit,
    onCityChange                     : (String) -> Unit,
    onAddressChange                  : (String) -> Unit,
    onPasswordVisibilityToggle       : () -> Unit,
    onConfirmPasswordVisibilityToggle: () -> Unit,
    onSignupClick                    : () -> Unit,
    focusManager                     : FocusManager,
    dimensions                       : Dimensions
) {
    GlassmorphicTextField(
        value = uiState.fullName, onValueChange = onFullNameChange,
        placeholder = stringResource(Res.string.signup_full_name_placeholder),
        leadingIcon = { Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.onSurface.copy(0.6f)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(dimensions.spacingMedium))
    GlassmorphicTextField(
        value = uiState.email, onValueChange = onEmailChange,
        placeholder = stringResource(Res.string.signup_email_placeholder),
        leadingIcon = { Icon(Icons.Default.Email, null, tint = MaterialTheme.colorScheme.onSurface.copy(0.6f)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(dimensions.spacingMedium))
    GlassmorphicTextField(
        value = uiState.password, onValueChange = onPasswordChange,
        placeholder = stringResource(Res.string.signup_password_placeholder),
        leadingIcon = { Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.onSurface.copy(0.6f)) },
        trailingIcon = {
            IconButton(onClick = onPasswordVisibilityToggle) {
                Icon(if (uiState.passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    null, tint = MaterialTheme.colorScheme.onSurface.copy(0.6f))
            }
        },
        visualTransformation = if (uiState.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(dimensions.spacingMedium))
    GlassmorphicTextField(
        value = uiState.confirmPassword, onValueChange = onConfirmPasswordChange,
        placeholder = stringResource(Res.string.signup_confirm_password_placeholder),
        leadingIcon = { Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.onSurface.copy(0.6f)) },
        trailingIcon = {
            IconButton(onClick = onConfirmPasswordVisibilityToggle) {
                Icon(if (uiState.confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    null, tint = MaterialTheme.colorScheme.onSurface.copy(0.6f))
            }
        },
        visualTransformation = if (uiState.confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(dimensions.spacingMedium))
    GlassmorphicTextField(
        value = uiState.phone, onValueChange = onPhoneChange,
        placeholder = stringResource(Res.string.signup_phone_placeholder),
        leadingIcon = { Icon(Icons.Default.Phone, null, tint = MaterialTheme.colorScheme.onSurface.copy(0.6f)) },
        isError      = uiState.phoneError != null,
        errorMessage = uiState.phoneError?.let { stringResource(Res.string.phone_error_invalid_format) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(dimensions.spacingMedium))
    GlassmorphicTextField(
        value = uiState.city, onValueChange = onCityChange,
        placeholder = stringResource(Res.string.signup_city_placeholder),
        leadingIcon = { Icon(Icons.Default.LocationCity, null, tint = MaterialTheme.colorScheme.onSurface.copy(0.6f)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(dimensions.spacingMedium))
    GlassmorphicTextField(
        value = uiState.address, onValueChange = onAddressChange,
        placeholder = stringResource(Res.string.signup_address_placeholder),
        leadingIcon = { Icon(Icons.Default.Home, null, tint = MaterialTheme.colorScheme.onSurface.copy(0.6f)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus(); onSignupClick() }),
        modifier = Modifier.fillMaxWidth()
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Landscape form fields (two-column grid)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SignupFieldsLandscape(
    uiState                          : SignupUiState,
    onFullNameChange                 : (String) -> Unit,
    onEmailChange                    : (String) -> Unit,
    onPasswordChange                 : (String) -> Unit,
    onConfirmPasswordChange          : (String) -> Unit,
    onPhoneChange                    : (String) -> Unit,
    onCityChange                     : (String) -> Unit,
    onAddressChange                  : (String) -> Unit,
    onPasswordVisibilityToggle       : () -> Unit,
    onConfirmPasswordVisibilityToggle: () -> Unit,
    onSignupClick                    : () -> Unit,
    focusManager                     : FocusManager,
    dimensions                       : Dimensions
) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)) {
            GlassmorphicTextField(
                value = uiState.fullName, onValueChange = onFullNameChange,
                placeholder = stringResource(Res.string.signup_full_name_placeholder),
                leadingIcon = { Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.onSurface.copy(0.6f)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                modifier = Modifier.fillMaxWidth()
            )
            GlassmorphicTextField(
                value = uiState.email, onValueChange = onEmailChange,
                placeholder = stringResource(Res.string.signup_email_placeholder),
                leadingIcon = { Icon(Icons.Default.Email, null, tint = MaterialTheme.colorScheme.onSurface.copy(0.6f)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                modifier = Modifier.fillMaxWidth()
            )
            GlassmorphicTextField(
                value = uiState.password, onValueChange = onPasswordChange,
                placeholder = stringResource(Res.string.signup_password_placeholder),
                leadingIcon = { Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.onSurface.copy(0.6f)) },
                trailingIcon = {
                    IconButton(onClick = onPasswordVisibilityToggle) {
                        Icon(if (uiState.passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            null, tint = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                    }
                },
                visualTransformation = if (uiState.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                modifier = Modifier.fillMaxWidth()
            )
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)) {
            GlassmorphicTextField(
                value = uiState.confirmPassword, onValueChange = onConfirmPasswordChange,
                placeholder = stringResource(Res.string.signup_confirm_password_placeholder),
                leadingIcon = { Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.onSurface.copy(0.6f)) },
                trailingIcon = {
                    IconButton(onClick = onConfirmPasswordVisibilityToggle) {
                        Icon(if (uiState.confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            null, tint = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                    }
                },
                visualTransformation = if (uiState.confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                modifier = Modifier.fillMaxWidth()
            )
            GlassmorphicTextField(
                value = uiState.phone, onValueChange = onPhoneChange,
                placeholder = stringResource(Res.string.signup_phone_placeholder),
                leadingIcon = { Icon(Icons.Default.Phone, null, tint = MaterialTheme.colorScheme.onSurface.copy(0.6f)) },
                isError      = uiState.phoneError != null,
                errorMessage = uiState.phoneError?.let { stringResource(Res.string.phone_error_invalid_format) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                modifier = Modifier.fillMaxWidth()
            )
            GlassmorphicTextField(
                value = uiState.city, onValueChange = onCityChange,
                placeholder = stringResource(Res.string.signup_city_placeholder),
                leadingIcon = { Icon(Icons.Default.LocationCity, null, tint = MaterialTheme.colorScheme.onSurface.copy(0.6f)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
    Spacer(Modifier.height(dimensions.spacingMedium))
    GlassmorphicTextField(
        value = uiState.address, onValueChange = onAddressChange,
        placeholder = stringResource(Res.string.signup_address_placeholder),
        leadingIcon = { Icon(Icons.Default.Home, null, tint = MaterialTheme.colorScheme.onSurface.copy(0.6f)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus(); onSignupClick() }),
        modifier = Modifier.fillMaxWidth()
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Decorative corner (shared by both steps)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SignupDecorativeCorner(
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
        tween(800, delayMillis, FastOutSlowInEasing), label = "alpha"
    )
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(modifier.fillMaxSize(), contentAlignment = alignment) {
            Image(
                painter            = painterResource(imageRes),
                contentDescription = null,
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

// ─────────────────────────────────────────────────────────────────────────────
// Helper
// ─────────────────────────────────────────────────────────────────────────────

private fun maskEmail(email: String): String {
    if (email.isBlank()) return ""
    val parts = email.split("@")
    if (parts.size != 2) return email
    val user = parts[0]; val domain = parts[1]
    return if (user.length <= 2) "$user@$domain" else "${user.first()}***@$domain"
}