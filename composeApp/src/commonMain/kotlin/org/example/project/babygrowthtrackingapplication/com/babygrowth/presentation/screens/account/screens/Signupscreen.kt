package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.account.screens

import androidx.compose.animation.*
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
import org.example.project.babygrowthtrackingapplication.ui.components.PrimaryButton
import org.example.project.babygrowthtrackingapplication.ui.components.GlassmorphicTextField
import org.example.project.babygrowthtrackingapplication.ui.components.SocialLoginSection

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SignupScreen(
    viewModel            : SignupViewModel,
    onBackClick          : () -> Unit,
    onSignupSuccess      : () -> Unit,
    onSocialSignupSuccess: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope : AnimatedContentScope
) {
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors
    val focusManager = LocalFocusManager.current
    val isLandscape  = LocalIsLandscape.current

    val uiState          = viewModel.uiState
    var animationStarted by remember { mutableStateOf(false) }

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
        if (isLandscape) {
            // ── LANDSCAPE: Logo left, scrollable form right ───────────────────
            Row(modifier = Modifier.fillMaxSize()) {
                // Left: Logo + app name
                Column(
                    modifier = Modifier
                        .weight(0.35f)
                        .fillMaxHeight()
                        .padding(horizontal = dimensions.screenPadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier         = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.TopStart
                    ) {
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
                    AnimatedSignupLogoSection(
                        animationStarted      = animationStarted,
                        sharedTransitionScope = sharedTransitionScope,
                        animatedContentScope  = animatedContentScope,
                        modifier              = Modifier.wrapContentHeight()
                    )
                }

                // Right: Scrollable form in two-column grid layout
                Column(
                    modifier = Modifier
                        .weight(0.65f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                        .padding(
                            horizontal = dimensions.screenPadding,
                            vertical   = dimensions.spacingMedium
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AnimatedSignupCard(
                        animationStarted              = animationStarted,
                        uiState                       = uiState,
                        viewModel                     = viewModel,
                        onSignupSuccess               = onSignupSuccess,
                        onSocialSignupSuccess         = onSocialSignupSuccess,
                        onFullNameChange              = viewModel::onFullNameChanged,
                        onEmailChange                 = viewModel::onEmailChanged,
                        onPasswordChange              = viewModel::onPasswordChanged,
                        onConfirmPasswordChange       = viewModel::onConfirmPasswordChanged,
                        onPhoneChange                 = viewModel::onPhoneChanged,
                        onCityChange                  = viewModel::onCityChanged,
                        onAddressChange               = viewModel::onAddressChanged,
                        onPasswordVisibilityToggle    = viewModel::onPasswordVisibilityToggled,
                        onConfirmPasswordVisibilityToggle = viewModel::onConfirmPasswordVisibilityToggled,
                        onSignupClick                 = { viewModel.signup(onSignupSuccess) },
                        focusManager                  = focusManager,
                        sharedTransitionScope         = sharedTransitionScope,
                        animatedContentScope          = animatedContentScope,
                        modifier                      = Modifier.fillMaxWidth()
                    )
                }
            }
        } else {
            // ── PORTRAIT: Original vertical layout ───────────────────────────
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
                        .widthIn(min = dimensions.avatarLarge * 5, max = dimensions.avatarLarge * 6)
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(min = dimensions.avatarLarge * 5, max = dimensions.avatarLarge * 6),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedSignupLogoSection(
                        animationStarted      = animationStarted,
                        sharedTransitionScope = sharedTransitionScope,
                        animatedContentScope  = animatedContentScope,
                        modifier              = Modifier.wrapContentHeight()
                    )
                }

                Spacer(Modifier.height(dimensions.spacingLarge))

                AnimatedSignupCard(
                    animationStarted              = animationStarted,
                    uiState                       = uiState,
                    viewModel                     = viewModel,
                    onSignupSuccess               = onSignupSuccess,
                    onSocialSignupSuccess         = onSocialSignupSuccess,
                    onFullNameChange              = viewModel::onFullNameChanged,
                    onEmailChange                 = viewModel::onEmailChanged,
                    onPasswordChange              = viewModel::onPasswordChanged,
                    onConfirmPasswordChange       = viewModel::onConfirmPasswordChanged,
                    onPhoneChange                 = viewModel::onPhoneChanged,
                    onCityChange                  = viewModel::onCityChanged,
                    onAddressChange               = viewModel::onAddressChanged,
                    onPasswordVisibilityToggle    = viewModel::onPasswordVisibilityToggled,
                    onConfirmPasswordVisibilityToggle = viewModel::onConfirmPasswordVisibilityToggled,
                    onSignupClick                 = { viewModel.signup(onSignupSuccess) },
                    focusManager                  = focusManager,
                    sharedTransitionScope         = sharedTransitionScope,
                    animatedContentScope          = animatedContentScope,
                    modifier                      = Modifier
                        .widthIn(min = dimensions.avatarLarge * 5, max = dimensions.avatarLarge * 6)
                        .fillMaxWidth()
                        .padding(bottom = dimensions.spacingXXLarge * 2)
                )
            }

            SignupDecorativeCorner(Res.drawable.bottom_left_background, Alignment.BottomStart,
                -100f, 100f, dimensions.cornerImageSize, animationStarted, 200)
            SignupDecorativeCorner(Res.drawable.bottom_right_background, Alignment.BottomEnd,
                100f, 100f, dimensions.cornerImageSize, animationStarted, 300)
        }
    }
}

@OptIn(ExperimentalResourceApi::class, ExperimentalSharedTransitionApi::class)
@Composable
private fun AnimatedSignupLogoSection(
    animationStarted     : Boolean,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope : AnimatedContentScope,
    modifier             : Modifier = Modifier
) {
    val dimensions  = LocalDimensions.current
    val isLandscape = LocalIsLandscape.current
    var jsonString by remember { mutableStateOf<String?>(null) }

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
            modifier = modifier.graphicsLayer { translationY = offsetY }.alpha(alpha),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            jsonString?.let { json ->
                val composition by rememberLottieComposition { LottieCompositionSpec.JsonString(json) }
                val progress by animateLottieCompositionAsState(composition = composition, iterations = Compottie.IterateForever)
                val painter = rememberLottiePainter(composition = composition, progress = { progress })
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

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun AnimatedSignupCard(
    animationStarted                 : Boolean,
    uiState                          : SignupUiState,
    viewModel                        : SignupViewModel,
    onSignupSuccess                  : () -> Unit,
    onSocialSignupSuccess            : () -> Unit,
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
    sharedTransitionScope            : SharedTransitionScope,
    animatedContentScope             : AnimatedContentScope,
    modifier                         : Modifier = Modifier
) {
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors
    val isLandscape  = LocalIsLandscape.current

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
                        .alpha(0.3f),
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
                    // ── 2-column grid for form fields in landscape ────────────
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)
                    ) {
                        Column(
                            modifier            = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)
                        ) {
                            GlassmorphicTextField(
                                value           = uiState.fullName,
                                onValueChange   = onFullNameChange,
                                placeholder     = stringResource(Res.string.signup_full_name_placeholder),
                                leadingIcon     = { Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.onSurface.copy(0.6f)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                                modifier        = Modifier.fillMaxWidth()
                            )
                            GlassmorphicTextField(
                                value           = uiState.email,
                                onValueChange   = onEmailChange,
                                placeholder     = stringResource(Res.string.signup_email_placeholder),
                                leadingIcon     = { Icon(Icons.Default.Email, null, tint = MaterialTheme.colorScheme.onSurface.copy(0.6f)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                                modifier        = Modifier.fillMaxWidth()
                            )
                            GlassmorphicTextField(
                                value                = uiState.password,
                                onValueChange        = onPasswordChange,
                                placeholder          = stringResource(Res.string.signup_password_placeholder),
                                leadingIcon          = { Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.onSurface.copy(0.6f)) },
                                trailingIcon         = {
                                    IconButton(onClick = onPasswordVisibilityToggle) {
                                        Icon(if (uiState.passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null, tint = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                                    }
                                },
                                visualTransformation = if (uiState.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions      = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                                keyboardActions      = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                                modifier             = Modifier.fillMaxWidth()
                            )
                        }
                        Column(
                            modifier            = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)
                        ) {
                            GlassmorphicTextField(
                                value                = uiState.confirmPassword,
                                onValueChange        = onConfirmPasswordChange,
                                placeholder          = stringResource(Res.string.signup_confirm_password_placeholder),
                                leadingIcon          = { Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.onSurface.copy(0.6f)) },
                                trailingIcon         = {
                                    IconButton(onClick = onConfirmPasswordVisibilityToggle) {
                                        Icon(if (uiState.confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null, tint = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                                    }
                                },
                                visualTransformation = if (uiState.confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions      = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                                keyboardActions      = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                                modifier             = Modifier.fillMaxWidth()
                            )
                            GlassmorphicTextField(
                                value           = uiState.phone,
                                onValueChange   = onPhoneChange,
                                placeholder     = stringResource(Res.string.signup_phone_placeholder),
                                leadingIcon     = { Icon(Icons.Default.Phone, null, tint = MaterialTheme.colorScheme.onSurface.copy(0.6f)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                                modifier        = Modifier.fillMaxWidth()
                            )
                            GlassmorphicTextField(
                                value           = uiState.city,
                                onValueChange   = onCityChange,
                                placeholder     = stringResource(Res.string.signup_city_placeholder),
                                leadingIcon     = { Icon(Icons.Default.LocationCity, null, tint = MaterialTheme.colorScheme.onSurface.copy(0.6f)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                                modifier        = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(Modifier.height(dimensions.spacingMedium))

                    GlassmorphicTextField(
                        value           = uiState.address,
                        onValueChange   = onAddressChange,
                        placeholder     = stringResource(Res.string.signup_address_placeholder),
                        leadingIcon     = { Icon(Icons.Default.Home, null, tint = MaterialTheme.colorScheme.onSurface.copy(0.6f)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus(); onSignupClick() }),
                        modifier        = Modifier.fillMaxWidth()
                    )
                } else {
                    // ── Single column for portrait ────────────────────────────
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
                        trailingIcon = { IconButton(onClick = onPasswordVisibilityToggle) { Icon(if (uiState.passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null, tint = MaterialTheme.colorScheme.onSurface.copy(0.6f)) } },
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
                        trailingIcon = { IconButton(onClick = onConfirmPasswordVisibilityToggle) { Icon(if (uiState.confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null, tint = MaterialTheme.colorScheme.onSurface.copy(0.6f)) } },
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

@Composable
private fun SignupDecorativeCorner(
    imageRes        : DrawableResource,
    alignment       : Alignment,
    fromX           : Float, fromY: Float,
    size            : Dp,
    animationStarted: Boolean,
    delayMillis     : Int,
    modifier        : Modifier = Modifier
) {
    val spec    = spring<Float>(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow)
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