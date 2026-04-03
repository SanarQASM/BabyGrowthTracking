package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.account.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
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
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.account.models.VerificationMethod
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.account.models.VerifyAccountUiState
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.account.models.VerifyAccountViewModel
import org.example.project.babygrowthtrackingapplication.ui.components.PrimaryButton
import org.example.project.babygrowthtrackingapplication.ui.components.GlassmorphicTextField
import org.jetbrains.compose.resources.DrawableResource

// ─────────────────────────────────────────────────────────────────────────────
// Large pill corner shared by all auth screens — scales via spacingXXLarge
// ─────────────────────────────────────────────────────────────────────────────
private val AuthCardCornerRadius @Composable get() = LocalDimensions.current.spacingXXLarge

/**
 * REFACTORED:
 *  - 320.dp min-width  →  dimensions.authCardMinWidth
 *  - 420.dp max-width  →  dimensions.authCardMaxWidth
 *  - 32.dp icon size   →  dimensions.iconLarge
 *  - 2.dp border width →  dimensions.borderWidthMedium
 *  - 4.dp spacer       →  dimensions.spacingXSmall
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun VerifyAccountScreen(
    viewModel            : VerifyAccountViewModel,
    onBackClick          : () -> Unit,
    onVerificationSuccess: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope : AnimatedContentScope
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
            // ── Back button ──────────────────────────────────────────────────
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
                AnimatedVerifyLogoSection(
                    animationStarted      = animationStarted,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedContentScope  = animatedContentScope,
                    modifier              = Modifier.wrapContentHeight()
                )
            }

            Spacer(modifier = Modifier.height(dimensions.spacingLarge))

            // ── Card ─────────────────────────────────────────────────────────
            AnimatedVerifyCard(
                animationStarted      = animationStarted,
                uiState               = uiState,
                onMethodSelected      = viewModel::onVerificationMethodSelected,
                onCodeChanged         = viewModel::onCodeChanged,
                onSendCode            = viewModel::sendVerificationCode,
                onVerifyClick         = { viewModel.verifyCode(onVerificationSuccess) },
                onResendClick         = viewModel::resendCode,
                focusManager          = focusManager,
                sharedTransitionScope = sharedTransitionScope,
                animatedContentScope  = animatedContentScope,
                modifier              = Modifier
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
        VerifyDecorativeCorner(
            imageRes         = Res.drawable.bottom_left_background,
            alignment        = Alignment.BottomStart,
            fromX            = -100f, fromY = 100f,
            size             = dimensions.cornerImageSize,
            animationStarted = animationStarted,
            delayMillis      = 200
        )
        VerifyDecorativeCorner(
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

@OptIn(ExperimentalResourceApi::class, ExperimentalSharedTransitionApi::class)
@Composable
private fun AnimatedVerifyLogoSection(
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
        targetValue   = if (animationStarted) 0f else 200f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow),
        label         = "logoOffsetY"
    )
    val alpha by animateFloatAsState(
        targetValue   = if (animationStarted) 1f else 0f,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
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
                val painter = rememberLottiePainter(composition = composition, progress = { progress })
                Image(
                    painter            = painter,
                    contentDescription = stringResource(Res.string.verify_logo_description),
                    modifier           = Modifier
                        .size(dimensions.logoSize * 0.4f)
                        .sharedBounds(
                            rememberSharedContentState("lottie_animation"),
                            animatedContentScope,
                            boundsTransform = { _, _ -> tween(600, easing = FastOutSlowInEasing) }
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
                    rememberSharedContentState("app_name"),
                    animatedContentScope,
                    boundsTransform = { _, _ -> tween(600, easing = FastOutSlowInEasing) }
                )
            )

            Spacer(modifier = Modifier.height(dimensions.spacingSmall))

            Text(
                text      = stringResource(Res.string.verify_account_title),
                style     = MaterialTheme.typography.titleLarge,
                color     = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Animated card
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun AnimatedVerifyCard(
    animationStarted     : Boolean,
    uiState              : VerifyAccountUiState,
    onMethodSelected     : (VerificationMethod) -> Unit,
    onCodeChanged        : (String) -> Unit,
    onSendCode           : () -> Unit,
    onVerifyClick        : () -> Unit,
    onResendClick        : () -> Unit,
    focusManager         : FocusManager,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope : AnimatedContentScope,
    modifier             : Modifier = Modifier
) {
    val dimensions       = LocalDimensions.current
    val customColors     = MaterialTheme.customColors
    val cardCornerRadius = AuthCardCornerRadius
    val cardShape        = RoundedCornerShape(topStart = cardCornerRadius, topEnd = cardCornerRadius)

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
                    text = if (uiState.codeSent)
                        stringResource(Res.string.verify_enter_code)
                    else
                        stringResource(Res.string.verify_select_method),
                    style    = MaterialTheme.typography.titleMedium,
                    color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                    modifier = Modifier.padding(bottom = dimensions.spacingMedium)
                )

                // ── Step 1: Method selection ──────────────────────────────────
                AnimatedVisibility(
                    visible = !uiState.codeSent,
                    enter   = fadeIn() + expandVertically(),
                    exit    = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        modifier            = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text      = stringResource(Res.string.verify_method_description),
                            style     = MaterialTheme.typography.bodyMedium,
                            color     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            modifier  = Modifier.padding(bottom = dimensions.spacingMedium)
                        )

                        Spacer(modifier = Modifier.height(dimensions.spacingMedium))

                        VerificationMethodCard(
                            icon        = Icons.Default.Email,
                            title       = stringResource(Res.string.verify_email_title),
                            description = maskEmail(uiState.email),
                            isSelected  = uiState.selectedMethod == VerificationMethod.EMAIL,
                            onClick     = { onMethodSelected(VerificationMethod.EMAIL) }
                        )

                        if (uiState.isSmsAvailable) {
                            Spacer(modifier = Modifier.height(dimensions.spacingMedium))
                            VerificationMethodCard(
                                icon        = Icons.Default.Sms,
                                title       = stringResource(Res.string.verify_sms_title),
                                description = maskPhone(uiState.phone),
                                isSelected  = uiState.selectedMethod == VerificationMethod.SMS,
                                onClick     = { onMethodSelected(VerificationMethod.SMS) }
                            )
                        }

                        uiState.errorMessage?.let { error ->
                            Spacer(modifier = Modifier.height(dimensions.spacingSmall))
                            Text(
                                text      = error,
                                style     = MaterialTheme.typography.bodySmall,
                                color     = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center,
                                modifier  = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(dimensions.spacingLarge))

                        PrimaryButton(
                            text     = stringResource(Res.string.verify_send_code),
                            onClick  = onSendCode,
                            loading  = uiState.isSendingCode,
                            enabled  = !uiState.isSendingCode && uiState.selectedMethod != null,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // ── Step 2: Code entry ────────────────────────────────────────
                AnimatedVisibility(
                    visible = uiState.codeSent,
                    enter   = fadeIn() + expandVertically(),
                    exit    = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        modifier            = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${stringResource(Res.string.verify_code_sent_to)} ${
                                if (uiState.selectedMethod == VerificationMethod.EMAIL)
                                    maskEmail(uiState.email)
                                else
                                    maskPhone(uiState.phone)
                            }",
                            style     = MaterialTheme.typography.bodyMedium,
                            color     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            modifier  = Modifier.padding(bottom = dimensions.spacingMedium)
                        )

                        Spacer(modifier = Modifier.height(dimensions.spacingMedium))

                        GlassmorphicTextField(
                            value         = uiState.verificationCode,
                            onValueChange = onCodeChanged,
                            placeholder   = stringResource(Res.string.verify_code_placeholder),
                            leadingIcon   = {
                                Icon(
                                    imageVector        = Icons.Default.Pin,
                                    contentDescription = stringResource(Res.string.verify_code_icon_description),
                                    tint               = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction    = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    if (uiState.verificationCode.length == 6) onVerifyClick()
                                }
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(dimensions.spacingSmall))

                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Text(
                                text  = stringResource(Res.string.verify_didnt_receive),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            // WAS: Spacer(modifier = Modifier.width(4.dp))  →  spacingXSmall
                            Spacer(modifier = Modifier.width(dimensions.spacingXSmall))
                            Text(
                                text = if (uiState.canResend)
                                    stringResource(Res.string.verify_resend)
                                else
                                    stringResource(Res.string.verify_resend_in, uiState.resendCountdown),
                                style    = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color    = if (uiState.canResend)
                                    customColors.accentGradientStart
                                else
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                modifier = Modifier.clickable(enabled = uiState.canResend) { onResendClick() }
                            )
                        }

                        uiState.errorMessage?.let { error ->
                            Spacer(modifier = Modifier.height(dimensions.spacingSmall))
                            Text(
                                text      = error,
                                style     = MaterialTheme.typography.bodySmall,
                                color     = MaterialTheme.colorScheme.error,
                                modifier  = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(dimensions.spacingLarge))

                        PrimaryButton(
                            text     = stringResource(Res.string.verify_account_button),
                            onClick  = onVerifyClick,
                            loading  = uiState.isVerifying,
                            enabled  = !uiState.isVerifying && uiState.verificationCode.length == 6,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(dimensions.spacingMedium))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Method card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun VerificationMethodCard(
    icon       : ImageVector,
    title      : String,
    description: String,
    isSelected : Boolean,
    onClick    : () -> Unit,
    modifier   : Modifier = Modifier
) {
    val customColors = MaterialTheme.customColors
    val dimensions   = LocalDimensions.current
    val cardShape    = RoundedCornerShape(dimensions.cardCornerRadius)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .then(
                if (isSelected) Modifier.border(
                    // WAS: width = 2.dp  →  dimensions.borderWidthMedium
                    width = dimensions.borderWidthMedium,
                    color = customColors.accentGradientStart,
                    shape = cardShape
                ) else Modifier
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                customColors.accentGradientStart.copy(alpha = 0.1f)
            else
            // WAS: Color.White.copy(alpha = 0.1f)  →  customColors.glassOverlay
                customColors.glassOverlay
        ),
        shape = cardShape
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(dimensions.spacingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = title,
                tint               = if (isSelected) customColors.accentGradientStart
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                // WAS: Modifier.size(32.dp)  →  dimensions.iconLarge
                modifier           = Modifier.size(dimensions.iconLarge)
            )
            Spacer(modifier = Modifier.width(dimensions.spacingMedium))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = title,
                    style      = MaterialTheme.typography.titleMedium,
                    color      = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text  = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            RadioButton(
                selected = isSelected,
                onClick  = onClick,
                colors   = RadioButtonDefaults.colors(
                    selectedColor = customColors.accentGradientStart
                )
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Decorative corner
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun VerifyDecorativeCorner(
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
    val offsetX by animateFloatAsState(if (animationStarted) 0f else fromX, spec, label = "offsetX")
    val offsetY by animateFloatAsState(if (animationStarted) 0f else fromY, spec, label = "offsetY")
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

// ─────────────────────────────────────────────────────────────────────────────
// Masking helpers
// ─────────────────────────────────────────────────────────────────────────────

private fun maskEmail(email: String): String {
    if (email.isBlank()) return ""
    val parts = email.split("@")
    if (parts.size != 2) return email
    val username = parts[0]
    val domain   = parts[1]
    return if (username.length <= 2) "$username@$domain"
    else "${username.first()}***@$domain"
}

private fun maskPhone(phone: String): String {
    if (phone.isBlank()) return ""
    return if (phone.length > 4) {
        val visible = phone.takeLast(4)
        val prefix  = phone.take(2)
        "$prefix *** *** $visible"
    } else phone
}