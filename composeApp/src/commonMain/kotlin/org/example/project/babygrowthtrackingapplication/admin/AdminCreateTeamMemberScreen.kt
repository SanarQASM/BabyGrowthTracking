// File: composeApp/src/commonMain/kotlin/org/example/project/babygrowthtrackingapplication/admin/AdminCreateTeamMemberScreen.kt

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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.example.project.babygrowthtrackingapplication.data.network.ApiResult
import org.example.project.babygrowthtrackingapplication.data.network.ApiService
import org.example.project.babygrowthtrackingapplication.theme.*
import org.example.project.babygrowthtrackingapplication.ui.components.GlassmorphicTextField
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

// ─────────────────────────────────────────────────────────────────────────────
// AdminCreateTeamMemberScreen
//
// Allows admin to create a new vaccination_team account.
// All attributes match what the signup flow captures:
//   fullName, email, password, confirmPassword, phone, city, address
//
// Uses the same glassmorphic field style as the rest of the auth/admin screens.
// No hardcoded values — all dims from LocalDimensions, colours from theme.
// ─────────────────────────────────────────────────────────────────────────────

// ── Lightweight local UI state for this form ──────────────────────────────────
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
)

@Composable
fun AdminCreateTeamMemberScreen(
    apiService  : ApiService,
    onBackClick : () -> Unit,
    onCreated   : () -> Unit,
    modifier    : Modifier = Modifier,
) {
    val dimensions   = LocalDimensions.current
    val customColors = MaterialTheme.customColors
    val focusManager = LocalFocusManager.current
    val scrollState  = rememberScrollState()

    var form by remember { mutableStateOf(CreateTeamMemberFormState()) }
    var animationStarted by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(80)
        animationStarted = true
    }

    // ── Navigate away after success ────────────────────────────────────────
    LaunchedEffect(form.isSuccess) {
        if (form.isSuccess) {
            delay(700)
            onCreated()
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

    // ── Submit ─────────────────────────────────────────────────────────────
    fun submit() {
        // Launch the coroutine immediately to handle both suspendable string fetching and the API call
        CoroutineScope(Dispatchers.Main).launch {

            // 1. Perform validation using suspendable getString() instead of @Composable stringResource()
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

            // 2. Halt if validation fails
            if (error != null) {
                form = form.copy(errorMessage = error)
                return@launch
            }

            // 3. Proceed with API call
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
                is ApiResult.Success -> form.copy(isLoading = false, isSuccess = true)
                is ApiResult.Error   -> form.copy(isLoading = false, errorMessage = result.message)
                else                 -> form.copy(isLoading = false)
            }
        }
    }
    // ── UI ─────────────────────────────────────────────────────────────────
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

        // ── Form card ──────────────────────────────────────────────────────
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
                // Section: Personal Info
                AdminSectionHeader(title = stringResource(Res.string.admin_team_section_personal))

                // Full Name
                GlassmorphicTextField(
                    value           = form.fullName,
                    onValueChange   = { form = form.copy(fullName = it, errorMessage = null) },
                    label           = stringResource(Res.string.admin_team_field_full_name),
                    placeholder     = stringResource(Res.string.admin_team_field_full_name_placeholder),
                    leadingIcon     = {
                        Icon(Icons.Default.Person, null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
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

                // Email
                GlassmorphicTextField(
                    value           = form.email,
                    onValueChange   = { form = form.copy(email = it, errorMessage = null) },
                    label           = stringResource(Res.string.admin_email_label),
                    placeholder     = stringResource(Res.string.admin_team_field_email_placeholder),
                    leadingIcon     = {
                        Icon(Icons.Default.Email, null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction    = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Phone (optional)
                GlassmorphicTextField(
                    value           = form.phone,
                    onValueChange   = { form = form.copy(phone = it) },
                    label           = stringResource(Res.string.admin_team_field_phone),
                    placeholder     = stringResource(Res.string.admin_team_field_phone_placeholder),
                    leadingIcon     = {
                        Icon(Icons.Default.Phone, null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction    = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // City (optional)
                GlassmorphicTextField(
                    value           = form.city,
                    onValueChange   = { form = form.copy(city = it) },
                    label           = stringResource(Res.string.admin_team_field_city),
                    placeholder     = stringResource(Res.string.admin_team_field_city_placeholder),
                    leadingIcon     = {
                        Icon(Icons.Default.LocationCity, null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
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

                // Address (optional)
                GlassmorphicTextField(
                    value           = form.address,
                    onValueChange   = { form = form.copy(address = it) },
                    label           = stringResource(Res.string.admin_team_field_address),
                    placeholder     = stringResource(Res.string.admin_team_field_address_placeholder),
                    leadingIcon     = {
                        Icon(Icons.Default.Home, null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
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

                // Section: Credentials
                AdminSectionHeader(title = stringResource(Res.string.admin_team_section_credentials))

                // Password
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
                    keyboardOptions      = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction    = ImeAction.Next
                    ),
                    keyboardActions      = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    modifier             = Modifier.fillMaxWidth()
                )

                // Confirm Password
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
                        IconButton(
                            onClick = { form = form.copy(confirmPasswordVisible = !form.confirmPasswordVisible) }
                        ) {
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
                    keyboardOptions      = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction    = ImeAction.Done
                    ),
                    keyboardActions      = KeyboardActions(
                        onDone = { focusManager.clearFocus(); submit() }
                    ),
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
                                text  = stringResource(Res.string.admin_team_created_success),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary,
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