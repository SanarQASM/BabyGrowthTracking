package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import babygrowthtrackingapplication.composeapp.generated.resources.Res
import babygrowthtrackingapplication.composeapp.generated.resources.*
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.data.Language
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model.SettingsViewModel
import org.example.project.babygrowthtrackingapplication.theme.GenderTheme
import org.example.project.babygrowthtrackingapplication.theme.LocalDimensions
import org.example.project.babygrowthtrackingapplication.theme.customColors
import org.jetbrains.compose.resources.stringResource

// ─────────────────────────────────────────────────────────────────────────────
// Single enum tracks which dialog is open — avoids 10+ boolean state variables
// ─────────────────────────────────────────────────────────────────────────────
private enum class SettingsDialog {
    NONE,
    EDIT_PROFILE,
    CHANGE_PW_STEP1,   // "send code to email"
    CHANGE_PW_STEP2,   // "enter 6-digit code"
    CHANGE_PW_STEP3,   // "enter new password"
    PICK_LANGUAGE,
    PICK_THEME,
    PICK_GENDER_THEME,
    REMINDER_DAYS,
    CONFIRM_LOGOUT,
    CONFIRM_DELETE,
    ABOUT,
}

// =============================================================================
// Main Composable
// =============================================================================

@Composable
fun SettingsTabContent(
    viewModel           : SettingsViewModel,
    onLanguageChange    : (Language) -> Unit    = {},   // bubble up so App.kt re-composes
    onDarkModeChange    : (Boolean) -> Unit     = {},   // bubble up so BabyGrowthTheme re-composes
    onGenderThemeChange : (GenderTheme) -> Unit = {},
    onNavigateToWelcome : () -> Unit            = {},   // called after logout / delete
) {
    val state        = viewModel.uiState
    val dim          = LocalDimensions.current
    val cc           = MaterialTheme.customColors
    val snackbar     = remember { SnackbarHostState() }
    var dialog       by remember { mutableStateOf(SettingsDialog.NONE) }
    // Holds the verified reset code between step 2 and step 3
    var resetCode    by remember { mutableStateOf("") }

    // ── react to navigation signal ────────────────────────────────────────────
    LaunchedEffect(state.navigateToWelcome) {
        if (state.navigateToWelcome) onNavigateToWelcome()
    }

    // ── snackbars for success / error ─────────────────────────────────────────
    LaunchedEffect(state.successMessage) {
        state.successMessage?.let { snackbar.showSnackbar(it); viewModel.clearMessages() }
    }
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { snackbar.showSnackbar(it); viewModel.clearMessages() }
    }

    // ── Dialogs ───────────────────────────────────────────────────────────────
    when (dialog) {

        SettingsDialog.EDIT_PROFILE -> EditProfileDialog(
            initialName  = state.userName,
            initialPhone = state.userPhone,
            isLoading    = state.isLoading,
            onSave       = { name, phone ->
                viewModel.updateProfile(name, phone)
                dialog = SettingsDialog.NONE
            },
            onDismiss = { dialog = SettingsDialog.NONE }
        )

        SettingsDialog.CHANGE_PW_STEP1 -> ChangePwStep1Dialog(
            email     = state.userEmail,
            isLoading = state.isLoading,
            onSend    = {
                viewModel.sendPasswordResetCode { dialog = SettingsDialog.CHANGE_PW_STEP2 }
            },
            onDismiss = { dialog = SettingsDialog.NONE }
        )

        SettingsDialog.CHANGE_PW_STEP2 -> ChangePwStep2Dialog(
            isLoading = state.isLoading,
            onVerify  = { code ->
                viewModel.verifyPasswordCode(code) { verified ->
                    resetCode = verified
                    dialog    = SettingsDialog.CHANGE_PW_STEP3
                }
            },
            onDismiss = { dialog = SettingsDialog.NONE }
        )

        SettingsDialog.CHANGE_PW_STEP3 -> ChangePwStep3Dialog(
            isLoading = state.isLoading,
            onSave    = { newPwd ->
                viewModel.confirmNewPassword(resetCode, newPwd)
                dialog = SettingsDialog.NONE
            },
            onDismiss = { dialog = SettingsDialog.NONE }
        )

        SettingsDialog.PICK_LANGUAGE -> PickerDialog(
            title     = stringResource(Res.string.select_language),
            options   = Language.entries.map { it to it.displayName },
            current   = state.currentLanguage,
            onSelect  = { lang ->
                viewModel.setLanguage(lang)
                onLanguageChange(lang)
                dialog = SettingsDialog.NONE
            },
            onDismiss = { dialog = SettingsDialog.NONE }
        )

        SettingsDialog.PICK_THEME -> PickerDialog(
            title     = stringResource(Res.string.settings_app_theme),
            options   = listOf(false to "☀️  ${stringResource(Res.string.settings_theme_light)}",
                true  to "🌙  ${stringResource(Res.string.settings_theme_dark)}"),
            current   = state.isDarkMode,
            onSelect  = { dark ->
                viewModel.setDarkMode(dark)
                onDarkModeChange(dark)
                dialog = SettingsDialog.NONE
            },
            onDismiss = { dialog = SettingsDialog.NONE }
        )

        SettingsDialog.PICK_GENDER_THEME -> PickerDialog(
            title     = stringResource(Res.string.settings_gender_theme),
            options   = listOf(
                GenderTheme.GIRL    to "🎀  ${stringResource(Res.string.settings_gender_girl)}",
                GenderTheme.BOY     to "🚀  ${stringResource(Res.string.settings_gender_boy)}",
                GenderTheme.NEUTRAL to "🌟  ${stringResource(Res.string.settings_gender_neutral)}",
            ),
            current   = state.genderTheme,
            onSelect  = { theme ->
                viewModel.setGenderTheme(theme)
                onGenderThemeChange(theme)
                dialog = SettingsDialog.NONE
            },
            onDismiss = { dialog = SettingsDialog.NONE }
        )

        SettingsDialog.REMINDER_DAYS -> PickerDialog(
            title     = stringResource(Res.string.settings_notif_reminder_days),
            options   = listOf(1, 2, 3, 5, 7, 14).map { d ->
                d to stringResource(Res.string.settings_reminder_days_value, d)
            },
            current   = state.reminderDaysBefore,
            onSelect  = { days ->
                viewModel.setReminderDays(days)
                dialog = SettingsDialog.NONE
            },
            onDismiss = { dialog = SettingsDialog.NONE }
        )

        SettingsDialog.CONFIRM_LOGOUT -> ConfirmDialog(
            title       = stringResource(Res.string.settings_logout_title),
            message     = stringResource(Res.string.settings_logout_message),
            confirmText = stringResource(Res.string.settings_logout_confirm),
            isDanger    = false,
            isLoading   = false,
            onConfirm   = { viewModel.logout(); dialog = SettingsDialog.NONE },
            onDismiss   = { dialog = SettingsDialog.NONE }
        )

        SettingsDialog.CONFIRM_DELETE -> ConfirmDialog(
            title       = stringResource(Res.string.settings_delete_title),
            message     = stringResource(Res.string.settings_delete_message),
            confirmText = stringResource(Res.string.settings_delete_confirm),
            isDanger    = true,
            isLoading   = state.isLoading,
            onConfirm   = { viewModel.deleteAccount() },
            onDismiss   = { if (!state.isLoading) dialog = SettingsDialog.NONE }
        )

        SettingsDialog.ABOUT -> AboutDialog(
            onDismiss = { dialog = SettingsDialog.NONE }
        )

        SettingsDialog.NONE -> { /* nothing */ }
    }

    // ── Scaffold with snackbar ────────────────────────────────────────────────
    Scaffold(
        snackbarHost   = { SnackbarHost(hostState = snackbar) },
        containerColor = Color.Transparent,
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = inner.calculateBottomPadding())
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
        ) {

            // ── Gradient header ───────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                cc.accentGradientStart.copy(alpha = 0.14f),
                                cc.accentGradientEnd.copy(alpha   = 0.03f),
                                MaterialTheme.colorScheme.background,
                            )
                        )
                    )
                    .padding(
                        start  = dim.screenPadding,
                        end    = dim.screenPadding,
                        top    = dim.spacingLarge,
                        bottom = dim.spacingMedium,
                    )
            ) {
                Column {
                    Text(
                        text       = stringResource(Res.string.settings_title),
                        style      = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(dim.spacingXSmall))
                    Text(
                        text  = stringResource(Res.string.settings_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    )
                }
            }

            val hPad = Modifier.padding(horizontal = dim.screenPadding)

            // ══════════════════════════════════════════════════════════════════
            // SECTION 1 — Account
            // ══════════════════════════════════════════════════════════════════
            SectionLabel("👤", stringResource(Res.string.settings_section_account), hPad)
            SettingsCard(hPad) {
                ProfileRow(
                    name    = state.userName,
                    email   = state.userEmail,
                    onClick = { dialog = SettingsDialog.EDIT_PROFILE }
                )
                RowDivider()
                ArrowRow(
                    icon    = Icons.Default.Lock,
                    label   = stringResource(Res.string.settings_change_password),
                    onClick = { dialog = SettingsDialog.CHANGE_PW_STEP1 }
                )
            }
            Spacer(Modifier.height(dim.spacingMedium))

            // ══════════════════════════════════════════════════════════════════
            // SECTION 2 — Preferences
            // ══════════════════════════════════════════════════════════════════
            SectionLabel("🎨", stringResource(Res.string.settings_section_preferences), hPad)
            SettingsCard(hPad) {
                ArrowRow(
                    icon    = Icons.Default.Language,
                    label   = stringResource(Res.string.settings_language),
                    value   = state.currentLanguage.displayName,
                    onClick = { dialog = SettingsDialog.PICK_LANGUAGE }
                )
                RowDivider()
                ArrowRow(
                    icon    = Icons.Default.Palette,
                    label   = stringResource(Res.string.settings_app_theme),
                    value   = if (state.isDarkMode) stringResource(Res.string.settings_theme_dark)
                    else stringResource(Res.string.settings_theme_light),
                    onClick = { dialog = SettingsDialog.PICK_THEME }
                )
                RowDivider()
                ArrowRow(
                    icon    = Icons.Default.ChildCare,
                    label   = stringResource(Res.string.settings_gender_theme),
                    value   = when (state.genderTheme) {
                        GenderTheme.GIRL    -> stringResource(Res.string.settings_gender_girl)
                        GenderTheme.BOY     -> stringResource(Res.string.settings_gender_boy)
                        GenderTheme.NEUTRAL -> stringResource(Res.string.settings_gender_neutral)
                    },
                    onClick = { dialog = SettingsDialog.PICK_GENDER_THEME }
                )
            }
            Spacer(Modifier.height(dim.spacingMedium))

            // ══════════════════════════════════════════════════════════════════
            // SECTION 3 — Notifications
            // ══════════════════════════════════════════════════════════════════
            SectionLabel("🔔", stringResource(Res.string.settings_section_notifications), hPad)
            SettingsCard(hPad) {
                ToggleRow(
                    icon     = Icons.Default.Notifications,
                    label    = stringResource(Res.string.settings_notif_master),
                    subtitle = stringResource(Res.string.settings_notif_master_sub),
                    checked  = state.notificationsEnabled,
                    onToggle = viewModel::setNotificationsEnabled
                )
                AnimatedVisibility(
                    visible = state.notificationsEnabled,
                    enter   = expandVertically(),
                    exit    = shrinkVertically(),
                ) {
                    Column {
                        RowDivider()
                        ToggleRow(
                            icon     = Icons.Default.HealthAndSafety,
                            label    = stringResource(Res.string.settings_notif_vaccination),
                            subtitle = stringResource(Res.string.settings_notif_vaccination_sub),
                            checked  = state.vaccinationReminders,
                            onToggle = viewModel::setVaccinationReminders
                        )
                        RowDivider()
                        ToggleRow(
                            icon     = Icons.Default.TrendingUp,
                            label    = stringResource(Res.string.settings_notif_growth),
                            subtitle = stringResource(Res.string.settings_notif_growth_sub),
                            checked  = state.growthAlerts,
                            onToggle = viewModel::setGrowthAlerts
                        )
                        RowDivider()
                        ToggleRow(
                            icon     = Icons.Default.CalendarToday,
                            label    = stringResource(Res.string.settings_notif_appointment),
                            subtitle = stringResource(Res.string.settings_notif_appointment_sub),
                            checked  = state.appointmentReminders,
                            onToggle = viewModel::setAppointmentReminders
                        )
                        RowDivider()
                        ArrowRow(
                            icon    = Icons.Default.AccessTime,
                            label   = stringResource(Res.string.settings_notif_reminder_days),
                            value   = stringResource(Res.string.settings_reminder_days_value, state.reminderDaysBefore),
                            onClick = { dialog = SettingsDialog.REMINDER_DAYS }
                        )
                    }
                }
            }
            Spacer(Modifier.height(dim.spacingMedium))

            // ══════════════════════════════════════════════════════════════════
            // SECTION 4 — Security
            // ══════════════════════════════════════════════════════════════════
            SectionLabel("🔒", stringResource(Res.string.settings_section_security), hPad)
            SettingsCard(hPad) {
                ToggleRow(
                    icon     = Icons.Default.Password,
                    label    = stringResource(Res.string.settings_save_password),
                    subtitle = stringResource(Res.string.settings_save_password_sub),
                    checked  = state.savePasswordEnabled,
                    onToggle = viewModel::setSavePassword
                )
            }
            Spacer(Modifier.height(dim.spacingMedium))

            // ══════════════════════════════════════════════════════════════════
            // SECTION 5 — About
            // ══════════════════════════════════════════════════════════════════
            SectionLabel("ℹ️", stringResource(Res.string.settings_section_about), hPad)
            SettingsCard(hPad) {
                ArrowRow(icon = Icons.Default.Info,           label = stringResource(Res.string.settings_about_app),       onClick = { dialog = SettingsDialog.ABOUT })
                RowDivider()
                ArrowRow(icon = Icons.Default.Policy,         label = stringResource(Res.string.settings_privacy_policy),  onClick = { /* open URL */ })
                RowDivider()
                ArrowRow(icon = Icons.Default.Gavel,          label = stringResource(Res.string.settings_terms),           onClick = { /* open URL */ })
                RowDivider()
                ArrowRow(icon = Icons.Default.ContactSupport, label = stringResource(Res.string.settings_contact_support), onClick = { /* open email */ })
            }
            Spacer(Modifier.height(dim.spacingMedium))

            // ══════════════════════════════════════════════════════════════════
            // SECTION 6 — Danger zone
            // ══════════════════════════════════════════════════════════════════
            SectionLabel("⚠️", stringResource(Res.string.settings_section_actions), hPad)
            SettingsCard(hPad) {
                DangerRow(
                    icon    = Icons.AutoMirrored.Filled.Logout,
                    label   = stringResource(Res.string.settings_logout),
                    color   = cc.warning,
                    onClick = { dialog = SettingsDialog.CONFIRM_LOGOUT }
                )
                RowDivider()
                DangerRow(
                    icon     = Icons.Default.DeleteForever,
                    label    = stringResource(Res.string.settings_delete_account),
                    subtitle = stringResource(Res.string.settings_delete_account_sub),
                    color    = MaterialTheme.colorScheme.error,
                    onClick  = { dialog = SettingsDialog.CONFIRM_DELETE }
                )
            }

            // Version footer
            Spacer(Modifier.height(dim.spacingLarge))
            Text(
                text     = stringResource(Res.string.settings_version, "1.0.0"),
                style    = MaterialTheme.typography.bodySmall,
                color    = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.28f),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = dim.spacingXXLarge)
            )
        }
    }
}

// =============================================================================
// Small reusable atoms
// =============================================================================

@Composable
private fun SectionLabel(emoji: String, text: String, modifier: Modifier = Modifier) {
    val dim = LocalDimensions.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = dim.spacingSmall, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dim.spacingXSmall)
    ) {
        Text(emoji)
        Text(
            text       = text.uppercase(),
            style      = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f),
        )
    }
}

@Composable
private fun SettingsCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val dim = LocalDimensions.current
    Card(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(dim.cardCornerRadius),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = dim.cardElevation),
    ) { Column(content = content) }
}

@Composable
private fun RowDivider() {
    val dim = LocalDimensions.current
    HorizontalDivider(
        modifier  = Modifier.padding(horizontal = dim.spacingMedium),
        thickness = dim.borderWidthThin,
        color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
    )
}

@Composable
private fun IconPill(icon: ImageVector, tint: Color) {
    val dim = LocalDimensions.current
    Box(
        modifier = Modifier
            .size(dim.iconXLarge)
            .clip(RoundedCornerShape(dim.chipCornerRadius))
            .background(tint.copy(alpha = 0.10f)),
        contentAlignment = Alignment.Center,
    ) { Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(dim.iconMedium)) }
}

@Composable
private fun ArrowRow(
    icon: ImageVector, label: String, value: String? = null, onClick: () -> Unit
) {
    val dim = LocalDimensions.current
    val cc  = MaterialTheme.customColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = dim.spacingMedium, vertical = dim.spacingMedium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dim.spacingMedium),
    ) {
        IconPill(icon = icon, tint = cc.accentGradientStart)
        Text(
            text     = label,
            style    = MaterialTheme.typography.bodyLarge,
            color    = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        if (value != null) {
            Text(
                text  = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
            )
        }
        Icon(
            Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.22f),
            modifier = Modifier.size(dim.iconSmall),
        )
    }
}

@Composable
private fun ToggleRow(
    icon: ImageVector, label: String, subtitle: String? = null, checked: Boolean, onToggle: (Boolean) -> Unit
) {
    val dim = LocalDimensions.current
    val cc  = MaterialTheme.customColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dim.spacingMedium, vertical = dim.spacingSmall),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dim.spacingMedium),
    ) {
        IconPill(icon = icon, tint = cc.accentGradientStart)
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f))
            }
        }
        Switch(
            checked  = checked,
            onCheckedChange = onToggle,
            colors   = SwitchDefaults.colors(
                checkedTrackColor   = cc.accentGradientStart,
                uncheckedTrackColor = MaterialTheme.colorScheme.outlineVariant,
            ),
        )
    }
}

@Composable
private fun ProfileRow(name: String, email: String, onClick: () -> Unit) {
    val dim     = LocalDimensions.current
    val cc      = MaterialTheme.customColors
    val initial = name.firstOrNull()?.uppercaseChar()?.toString() ?: "P"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(dim.spacingMedium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dim.spacingMedium),
    ) {
        // Avatar circle with gradient + initial
        Box(
            modifier = Modifier
                .size(dim.avatarLarge)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(cc.accentGradientStart, cc.accentGradientEnd))),
            contentAlignment = Alignment.Center,
        ) {
            Text(initial, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color.White)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(name.ifBlank { "—" }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(2.dp))
            Text(email.ifBlank { "—" }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
        }
        Box(
            modifier = Modifier
                .size(dim.iconXLarge)
                .clip(RoundedCornerShape(dim.chipCornerRadius))
                .background(cc.accentGradientStart.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center,
        ) { Icon(Icons.Default.Edit, null, tint = cc.accentGradientStart, modifier = Modifier.size(dim.iconSmall)) }
    }
}

@Composable
private fun DangerRow(
    icon: ImageVector, label: String, subtitle: String? = null, color: Color, onClick: () -> Unit
) {
    val dim = LocalDimensions.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = dim.spacingMedium, vertical = dim.spacingMedium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dim.spacingMedium),
    ) {
        Box(
            modifier = Modifier
                .size(dim.iconXLarge)
                .clip(RoundedCornerShape(dim.chipCornerRadius))
                .background(color.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center,
        ) { Icon(icon, null, tint = color, modifier = Modifier.size(dim.iconMedium)) }
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, color = color)
            if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = color.copy(alpha = 0.60f))
        }
    }
}

// =============================================================================
// Generic picker dialog — works for any T (Language, Boolean, GenderTheme, Int)
// =============================================================================

@Composable
private fun <T> PickerDialog(
    title     : String,
    options   : List<Pair<T, String>>,
    current   : T,
    onSelect  : (T) -> Unit,
    onDismiss : () -> Unit,
) {
    val dim = LocalDimensions.current
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(dim.cardCornerRadius),
        title = { Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
        text  = {
            Column(verticalArrangement = Arrangement.spacedBy(dim.spacingXSmall)) {
                options.forEach { (value, label) ->
                    val selected = value == current
                    Card(
                        modifier  = Modifier.fillMaxWidth(),
                        shape     = RoundedCornerShape(dim.chipCornerRadius),
                        colors    = CardDefaults.cardColors(
                            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        onClick   = { onSelect(value) },
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(dim.spacingMedium),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically,
                        ) {
                            Text(
                                label,
                                style  = MaterialTheme.typography.bodyLarge,
                                color  = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurface,
                            )
                            if (selected) {
                                val dim2 = LocalDimensions.current
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint     = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(dim2.iconMedium),
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton  = {},
        dismissButton  = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.btn_cancel)) } },
    )
}

// =============================================================================
// Edit Profile Dialog
// =============================================================================

@Composable
private fun EditProfileDialog(
    initialName  : String,
    initialPhone : String,
    isLoading    : Boolean,
    onSave       : (name: String, phone: String) -> Unit,
    onDismiss    : () -> Unit,
) {
    val dim   = LocalDimensions.current
    var name  by remember { mutableStateOf(initialName) }
    var phone by remember { mutableStateOf(initialPhone) }
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(dim.cardCornerRadius),
        title = { Text(stringResource(Res.string.settings_edit_profile), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
        text  = {
            Column(verticalArrangement = Arrangement.spacedBy(dim.spacingSmall)) {
                OutlinedTextField(
                    value         = name,
                    onValueChange = { name = it },
                    label         = { Text(stringResource(Res.string.signup_full_name_placeholder)) },
                    leadingIcon   = { Icon(Icons.Default.Person, null) },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = RoundedCornerShape(dim.chipCornerRadius),
                )
                OutlinedTextField(
                    value         = phone,
                    onValueChange = { phone = it },
                    label         = { Text(stringResource(Res.string.signup_phone_placeholder)) },
                    leadingIcon   = { Icon(Icons.Default.Phone, null) },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = RoundedCornerShape(dim.chipCornerRadius),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(name.trim(), phone.trim()) }, enabled = name.isNotBlank() && !isLoading) {
                if (isLoading) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                else Text(stringResource(Res.string.settings_save))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.btn_cancel)) } },
    )
}

// =============================================================================
// Change Password — 3 steps
// =============================================================================

@Composable
private fun ChangePwStep1Dialog(email: String, isLoading: Boolean, onSend: () -> Unit, onDismiss: () -> Unit) {
    val dim = LocalDimensions.current
    AlertDialog(
        onDismissRequest = onDismiss,
        shape  = RoundedCornerShape(dim.cardCornerRadius),
        title  = { Text(stringResource(Res.string.settings_change_password), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
        text   = {
            Text(
                stringResource(Res.string.settings_change_pw_send_desc, email),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )
        },
        confirmButton = {
            TextButton(onClick = onSend, enabled = !isLoading) {
                if (isLoading) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                else Text(stringResource(Res.string.settings_send_code))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.btn_cancel)) } },
    )
}

@Composable
private fun ChangePwStep2Dialog(isLoading: Boolean, onVerify: (String) -> Unit, onDismiss: () -> Unit) {
    val dim  = LocalDimensions.current
    var code by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        shape  = RoundedCornerShape(dim.cardCornerRadius),
        title  = { Text(stringResource(Res.string.settings_enter_code_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
        text   = {
            OutlinedTextField(
                value         = code,
                onValueChange = { code = it.filter { c -> c.isDigit() }.take(6) },
                label         = { Text(stringResource(Res.string.settings_enter_code_hint)) },
                leadingIcon   = { Icon(Icons.Default.Tag, null) },
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth(),
                shape         = RoundedCornerShape(dim.chipCornerRadius),
            )
        },
        confirmButton = {
            TextButton(onClick = { onVerify(code) }, enabled = code.length == 6 && !isLoading) {
                if (isLoading) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                else Text(stringResource(Res.string.settings_verify_code))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.btn_cancel)) } },
    )
}

@Composable
private fun ChangePwStep3Dialog(isLoading: Boolean, onSave: (String) -> Unit, onDismiss: () -> Unit) {
    val dim       = LocalDimensions.current
    var pwd       by remember { mutableStateOf("") }
    var confirm   by remember { mutableStateOf("") }
    var visible   by remember { mutableStateOf(false) }
    val mismatch  = confirm.isNotBlank() && pwd != confirm
    val canSubmit = pwd.length >= 6 && pwd == confirm && !isLoading
    AlertDialog(
        onDismissRequest = onDismiss,
        shape  = RoundedCornerShape(dim.cardCornerRadius),
        title  = { Text(stringResource(Res.string.settings_new_password_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
        text   = {
            Column(verticalArrangement = Arrangement.spacedBy(dim.spacingSmall)) {
                OutlinedTextField(
                    value                  = pwd,
                    onValueChange          = { pwd = it },
                    label                  = { Text(stringResource(Res.string.settings_new_password)) },
                    leadingIcon            = { Icon(Icons.Default.Lock, null) },
                    trailingIcon           = { IconButton({ visible = !visible }) { Icon(if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null) } },
                    visualTransformation   = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine             = true,
                    modifier               = Modifier.fillMaxWidth(),
                    shape                  = RoundedCornerShape(dim.chipCornerRadius),
                )
                OutlinedTextField(
                    value                  = confirm,
                    onValueChange          = { confirm = it },
                    label                  = { Text(stringResource(Res.string.settings_confirm_password)) },
                    leadingIcon            = { Icon(Icons.Default.Lock, null) },
                    trailingIcon           = { IconButton({ visible = !visible }) { Icon(if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null) } },
                    visualTransformation   = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
                    isError                = mismatch,
                    singleLine             = true,
                    modifier               = Modifier.fillMaxWidth(),
                    shape                  = RoundedCornerShape(dim.chipCornerRadius),
                )
                if (mismatch) {
                    Text(
                        stringResource(Res.string.settings_passwords_no_match),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(pwd) }, enabled = canSubmit) {
                if (isLoading) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                else Text(stringResource(Res.string.settings_save))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.btn_cancel)) } },
    )
}

// =============================================================================
// Confirm (logout / delete)
// =============================================================================

@Composable
private fun ConfirmDialog(
    title: String, message: String, confirmText: String,
    isDanger: Boolean, isLoading: Boolean,
    onConfirm: () -> Unit, onDismiss: () -> Unit,
) {
    val dim = LocalDimensions.current
    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        shape  = RoundedCornerShape(dim.cardCornerRadius),
        title  = { Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
        text   = { Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)) },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !isLoading) {
                if (isLoading) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                else Text(confirmText, color = if (isDanger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
            }
        },
        dismissButton = {
            if (!isLoading) TextButton(onClick = onDismiss) { Text(stringResource(Res.string.btn_cancel)) }
        },
    )
}

// =============================================================================
// About
// =============================================================================

@Composable
private fun AboutDialog(onDismiss: () -> Unit) {
    val dim = LocalDimensions.current
    val cc  = MaterialTheme.customColors
    AlertDialog(
        onDismissRequest = onDismiss,
        shape  = RoundedCornerShape(dim.cardCornerRadius),
        title  = { Text(stringResource(Res.string.settings_about_app), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
        text   = {
            Column(
                modifier              = Modifier.fillMaxWidth(),
                horizontalAlignment   = Alignment.CenterHorizontally,
                verticalArrangement   = Arrangement.spacedBy(dim.spacingSmall),
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(dim.cardCornerRadius))
                        .background(Brush.linearGradient(listOf(cc.accentGradientStart, cc.accentGradientEnd))),
                    contentAlignment = Alignment.Center,
                ) { Text("🍼", style = MaterialTheme.typography.headlineLarge) }
                Text(stringResource(Res.string.app_name), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(stringResource(Res.string.settings_version, "1.0.0"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.40f))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Text(stringResource(Res.string.settings_about_desc), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f))
            }
        },
        confirmButton  = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.btn_cancel)) } },
    )
}