package org.example.project.babygrowthtrackingapplication.admin

import androidx.compose.animation.core.*
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import babygrowthtrackingapplication.composeapp.generated.resources.Res
import babygrowthtrackingapplication.composeapp.generated.resources.*
import kotlinx.coroutines.launch
import org.example.project.babygrowthtrackingapplication.data.network.ApiResult
import org.example.project.babygrowthtrackingapplication.data.network.ApiService
import org.example.project.babygrowthtrackingapplication.data.network.UserResponse
import org.example.project.babygrowthtrackingapplication.theme.*
import org.example.project.babygrowthtrackingapplication.ui.components.GlassmorphicTextField
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

private data class CreateTeamMemberFormState(
    val fullName             : String  = "",
    val email                : String  = "",
    val password             : String  = "",
    val confirmPassword      : String  = "",
    val phone                : String  = "",
    val city                 : String  = "",
    val address              : String  = "",
    val passwordVisible      : Boolean = false,
    val confirmPasswordVisible: Boolean = false,
    val isLoading            : Boolean = false,
    val isSuccess            : Boolean = false,
    val errorMessage         : String? = null,
    // FIX: store the created user so it can be passed to onCreated callback
    val createdUser          : UserResponse? = null,
)

// ─────────────────────────────────────────────────────────────────────────────
// FIX: onCreated signature changed from () -> Unit to (UserResponse) -> Unit.
// Previously the created user was discarded, making viewModel.onTeamMemberCreated
// unreachable (dead code). Now the UserResponse is passed through so:
//  1. AdminViewModel.onTeamMemberCreated() can do an optimistic list update
//  2. AdminHomeScreen can navigate to AssignBench(newMember) immediately
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun AdminCreateTeamMemberScreen(
    apiService  : ApiService,
    onBackClick : () -> Unit,
    // FIX: was () -> Unit — now (UserResponse) -> Unit to propagate created user
    onCreated   : (UserResponse) -> Unit,
    modifier    : Modifier = Modifier,
) {
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors
    val focusManager = LocalFocusManager.current
    val scrollState  = rememberScrollState()

    var form by remember { mutableStateOf(CreateTeamMemberFormState()) }
    var animationStarted by remember { mutableStateOf(false) }

    // FIX: use rememberCoroutineScope() instead of creating a new
    // CoroutineScope(Dispatchers.Main) on every submit tap.
    // The remembered scope is tied to the composable lifecycle and is
    // cancelled automatically when the composable leaves composition,
    // preventing coroutine leaks when the user navigates away mid-request.
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(80)
        animationStarted = true
    }

    // FIX: navigate with the created user when success flag is set
    LaunchedEffect(form.isSuccess) {
        if (form.isSuccess) {
            kotlinx.coroutines.delay(700)
            form.createdUser?.let { onCreated(it) }
        }
    }

    val cardOffsetY by animateFloatAsState(
        targetValue   = if (animationStarted) 0f else 260f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow),
        label         = "cardY"
    )
    val contentAlpha by animateFloatAsState(
        targetValue   = if (animationStarted) 1f else 0f,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label         = "alpha"
    )

    // FIX: submit now uses the remembered scope (bound to composable lifecycle)
    // and stores the created UserResponse in form.createdUser for propagation.
    fun submit() {
        scope.launch {
            val error = when {
                form.fullName.isBlank()               -> getString(Res.string.signup_error_full_name_required)
                form.email.isBlank()                  -> getString(Res.string.signup_error_email_required)
                !form.email.contains("@")             -> getString(Res.string.signup_error_email_invalid)
                form.password.isBlank()               -> getString(Res.string.signup_error_password_required)
                form.password.length < 8              -> getString(Res.string.signup_error_password_too_short)
                form.confirmPassword.isBlank()        -> getString(Res.string.signup_error_confirm_required)
                form.password != form.confirmPassword -> getString(Res.string.signup_error_passwords_no_match)
                else                                  -> null
            }

            if (error != null) {
                form = form.copy(errorMessage = error)
                return@launch
            }

            form = form.copy(isLoading = true, errorMessage = null)

            val result = apiService.createTeamMember(
                fullName = form.fullName.trim(),
                email    = form.email.trim(),
                password = form.password,
                phone    = form.phone.trim().ifBlank { null },
                city     = form.city.trim().ifBlank { null },
                address  = form.address.trim().ifBlank { null },
            )

            form = when (result) {
                is ApiResult.Success -> form.copy(
                    isLoading   = false,
                    isSuccess   = true,
                    // FIX: store the created UserResponse so LaunchedEffect can pass it
                    createdUser = result.data
                )
                is ApiResult.Error  -> form.copy(
                    isLoading    = false,
                    errorMessage = result.message
                )
                else -> form.copy(isLoading = false)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = dimensions.screenPadding)
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top bar
        Row(
            modifier          = Modifier
                .fillMaxWidth()
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
            Text(
                text       = stringResource(Res.string.admin_team_create_title),
                style      = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onBackground,
                modifier   = Modifier.weight(1f).alpha(contentAlpha)
            )
        }

        Spacer(Modifier.height(dimensions.spacingMedium))

        // Header icon
        Box(
            modifier         = Modifier
                .size(dimensions.avatarLarge + dimensions.spacingXLarge)
                .clip(RoundedCornerShape(dimensions.cardCornerRadius))
                .background(customColors.accentGradientStart.copy(alpha = 0.13f))
                .alpha(contentAlpha),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text  = stringResource(Res.string.admin_team_emoji),
                style = MaterialTheme.typography.displaySmall
            )
        }

        Spacer(Modifier.height(dimensions.spacingSmall))

        Text(
            text      = stringResource(Res.string.admin_team_create_subtitle),
            style     = MaterialTheme.typography.bodyMedium,
            color     = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier  = Modifier.alpha(contentAlpha)
        )

        Spacer(Modifier.height(dimensions.spacingLarge))

        // Form card
        Card(
            modifier  = Modifier
                .widthIn(max = dimensions.authCardMaxWidth)
                .fillMaxWidth()
                .graphicsLayer { translationY = cardOffsetY }
                .alpha(contentAlpha),
            shape     = RoundedCornerShape(dimensions.cardCornerRadius * 2),
            colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = dimensions.cardElevation)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimensions.spacingLarge),
                verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)
            ) {
                AdminSectionHeader(title = stringResource(Res.string.admin_team_section_personal))

                GlassmorphicTextField(
                    value           = form.fullName,
                    onValueChange   = { form = form.copy(fullName = it, errorMessage = null) },
                    label           = stringResource(Res.string.admin_team_field_full_name),
                    placeholder     = stringResource(Res.string.admin_team_field_full_name_placeholder),
                    leadingIcon     = {
                        Icon(Icons.Default.Person, null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    modifier        = Modifier.fillMaxWidth()
                )

                GlassmorphicTextField(
                    value           = form.email,
                    onValueChange   = { form = form.copy(email = it, errorMessage = null) },
                    label           = stringResource(Res.string.admin_email_label),
                    placeholder     = stringResource(Res.string.admin_team_field_email_placeholder),
                    leadingIcon     = {
                        Icon(Icons.Default.Email, null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    modifier        = Modifier.fillMaxWidth()
                )

                GlassmorphicTextField(
                    value           = form.phone,
                    onValueChange   = { form = form.copy(phone = it) },
                    label           = stringResource(Res.string.admin_team_field_phone),
                    placeholder     = stringResource(Res.string.admin_team_field_phone_placeholder),
                    leadingIcon     = {
                        Icon(Icons.Default.Phone, null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    modifier        = Modifier.fillMaxWidth()
                )

                GlassmorphicTextField(
                    value           = form.city,
                    onValueChange   = { form = form.copy(city = it) },
                    label           = stringResource(Res.string.admin_team_field_city),
                    placeholder     = stringResource(Res.string.admin_team_field_city_placeholder),
                    leadingIcon     = {
                        Icon(Icons.Default.LocationCity, null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    modifier        = Modifier.fillMaxWidth()
                )

                GlassmorphicTextField(
                    value           = form.address,
                    onValueChange   = { form = form.copy(address = it) },
                    label           = stringResource(Res.string.admin_team_field_address),
                    placeholder     = stringResource(Res.string.admin_team_field_address_placeholder),
                    leadingIcon     = {
                        Icon(Icons.Default.Home, null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    modifier        = Modifier.fillMaxWidth()
                )

                AdminSectionHeader(title = stringResource(Res.string.admin_team_section_credentials))

                GlassmorphicTextField(
                    value                = form.password,
                    onValueChange        = { form = form.copy(password = it, errorMessage = null) },
                    label                = stringResource(Res.string.admin_password_label),
                    placeholder          = stringResource(Res.string.admin_team_field_password_placeholder),
                    leadingIcon          = {
                        Icon(Icons.Default.Lock, null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    },
                    trailingIcon         = {
                        IconButton(onClick = { form = form.copy(passwordVisible = !form.passwordVisible) }) {
                            Icon(
                                imageVector = if (form.passwordVisible) Icons.Default.Visibility
                                else Icons.Default.VisibilityOff,
                                contentDescription = if (form.passwordVisible)
                                    stringResource(Res.string.admin_password_hide)
                                else
                                    stringResource(Res.string.admin_password_show),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    },
                    visualTransformation = if (form.passwordVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    keyboardOptions      = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                    keyboardActions      = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    modifier             = Modifier.fillMaxWidth()
                )

                GlassmorphicTextField(
                    value                = form.confirmPassword,
                    onValueChange        = { form = form.copy(confirmPassword = it, errorMessage = null) },
                    label                = stringResource(Res.string.admin_team_field_confirm_password),
                    placeholder          = stringResource(Res.string.admin_team_field_confirm_password_placeholder),
                    leadingIcon          = {
                        Icon(Icons.Default.Lock, null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    },
                    trailingIcon         = {
                        IconButton(onClick = {
                            form = form.copy(confirmPasswordVisible = !form.confirmPasswordVisible)
                        }) {
                            Icon(
                                imageVector = if (form.confirmPasswordVisible) Icons.Default.Visibility
                                else Icons.Default.VisibilityOff,
                                contentDescription = if (form.confirmPasswordVisible)
                                    stringResource(Res.string.admin_password_hide)
                                else
                                    stringResource(Res.string.admin_password_show),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    },
                    visualTransformation = if (form.confirmPasswordVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    keyboardOptions      = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions      = KeyboardActions(onDone = { focusManager.clearFocus(); submit() }),
                    modifier             = Modifier.fillMaxWidth()
                )

                // Role info banner
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(dimensions.cardCornerRadius),
                    color    = customColors.accentGradientStart.copy(alpha = 0.08f)
                ) {
                    Row(
                        modifier          = Modifier.padding(dimensions.spacingMedium),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
                    ) {
                        Icon(
                            imageVector        = Icons.Default.Info,
                            contentDescription = null,
                            tint               = customColors.accentGradientStart,
                            modifier           = Modifier.size(dimensions.iconSmall)
                        )
                        Text(
                            text  = stringResource(Res.string.admin_team_role_info),
                            style = MaterialTheme.typography.bodySmall,
                            color = customColors.accentGradientStart
                        )
                    }
                }

                // Error message
                form.errorMessage?.let { error ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(dimensions.cardCornerRadius),
                        color    = MaterialTheme.colorScheme.errorContainer
                    ) {
                        Row(
                            modifier          = Modifier.padding(dimensions.spacingSmall),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall)
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint               = MaterialTheme.colorScheme.error,
                                modifier           = Modifier.size(dimensions.iconSmall)
                            )
                            Text(
                                text  = error,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                // Success banner
                if (form.isSuccess) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(dimensions.cardCornerRadius),
                        color    = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
                    ) {
                        Row(
                            modifier          = Modifier.padding(dimensions.spacingSmall),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(dimensions.spacingXSmall)
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint               = MaterialTheme.colorScheme.secondary,
                                modifier           = Modifier.size(dimensions.iconSmall)
                            )
                            Text(
                                text       = stringResource(Res.string.admin_team_created_success),
                                style      = MaterialTheme.typography.bodySmall,
                                color      = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(Modifier.height(dimensions.spacingXSmall))

                // Submit button
                Button(
                    onClick  = { focusManager.clearFocus(); submit() },
                    enabled  = !form.isLoading && !form.isSuccess,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dimensions.buttonHeight),
                    shape  = RoundedCornerShape(dimensions.buttonCornerRadius),
                    colors = ButtonDefaults.buttonColors(
                        containerColor         = customColors.accentGradientStart,
                        disabledContainerColor = customColors.accentGradientStart.copy(alpha = 0.5f)
                    )
                ) {
                    if (form.isLoading) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(dimensions.iconMedium),
                            color       = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = dimensions.borderWidthMedium
                        )
                    } else {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
                        ) {
                            Icon(
                                Icons.Default.PersonAdd,
                                contentDescription = null,
                                tint     = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(dimensions.iconMedium)
                            )
                            Text(
                                text       = stringResource(Res.string.admin_team_create_button),
                                fontWeight = FontWeight.Bold,
                                color      = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }

                // Cancel
                TextButton(
                    onClick  = onBackClick,
                    enabled  = !form.isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text  = stringResource(Res.string.btn_cancel),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }

        Spacer(Modifier.height(dimensions.spacingXXLarge))
    }
}