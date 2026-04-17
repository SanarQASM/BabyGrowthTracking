package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.screen

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import babygrowthtrackingapplication.composeapp.generated.resources.Res
import babygrowthtrackingapplication.composeapp.generated.resources.*
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.data.Language
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model.SettingsViewModel
import org.example.project.babygrowthtrackingapplication.theme.GenderTheme
import org.example.project.babygrowthtrackingapplication.theme.LocalDimensions
import org.example.project.babygrowthtrackingapplication.theme.LocalIsLandscape
import org.example.project.babygrowthtrackingapplication.theme.customColors
import org.jetbrains.compose.resources.stringResource

private enum class SettingsDialog {
    NONE,
    EDIT_PROFILE, CHANGE_PW_STEP1, CHANGE_PW_STEP2, CHANGE_PW_STEP3,
    PICK_LANGUAGE, PICK_THEME, PICK_GENDER_THEME, REMINDER_DAYS,
    CONFIRM_LOGOUT, CONFIRM_DELETE,
    ABOUT, PRIVACY_POLICY, TERMS_OF_USE, CONTACT_SUPPORT,
}

private enum class SettingsSection {
    ACCOUNT, PREFERENCES, NOTIFICATIONS, SECURITY, INFORMATION, ABOUT, ACTIONS
}

@Composable
fun SettingsTabContent(
    viewModel                         : SettingsViewModel,
    onLanguageChange                  : (Language) -> Unit = {},
    onDarkModeChange                  : (Boolean) -> Unit = {},
    onGenderThemeChange               : (GenderTheme) -> Unit = {},
    onNavigateToWelcome               : () -> Unit = {},
    onNavigateToFamilyHistory         : (babyId: String, babyName: String) -> Unit = { _, _ -> },
    onNavigateToChildIllnesses        : (babyId: String, babyName: String) -> Unit = { _, _ -> },
    onNavigateToVisionMotor           : (babyId: String, babyName: String) -> Unit = { _, _ -> },
    onNavigateToHearingSpeech         : (babyId: String, babyName: String) -> Unit = { _, _ -> },
    onNavigateToPreCheckInvestigation : (babyId: String, babyName: String) -> Unit = { _, _ -> },
    selectedBabyId                    : String? = null,
    selectedBabyName                  : String  = "",
    familyHistoryIsSet                : Boolean = false,
    childIllnessCount                 : Int     = 0,
    childIllnessActiveCount           : Int     = 0,
    visionMotorCount                  : Int     = 0,
    hearingSpeechCount                : Int     = 0,
    preCheckInvestigationIsSet        : Boolean = false,
) {
    val state       = viewModel.uiState
    val dim         = LocalDimensions.current
    val cc          = MaterialTheme.customColors
    val snackbar    = remember { SnackbarHostState() }
    val isLandscape = LocalIsLandscape.current

    var dialog          by remember { mutableStateOf(SettingsDialog.NONE) }
    var resetCode       by remember { mutableStateOf("") }
    var selectedSection by remember { mutableStateOf(SettingsSection.ACCOUNT) }

    LaunchedEffect(state.navigateToWelcome) { if (state.navigateToWelcome) onNavigateToWelcome() }
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
                viewModel.updateProfile(name, phone); dialog = SettingsDialog.NONE
            },
            onDismiss = { dialog = SettingsDialog.NONE })

        SettingsDialog.CHANGE_PW_STEP1 -> ChangePwStep1Dialog(
            email     = state.userEmail, isLoading = state.isLoading,
            onSend    = { viewModel.sendPasswordResetCode { dialog = SettingsDialog.CHANGE_PW_STEP2 } },
            onDismiss = { dialog = SettingsDialog.NONE })

        SettingsDialog.CHANGE_PW_STEP2 -> ChangePwStep2Dialog(
            isLoading = state.isLoading,
            onVerify  = { code ->
                viewModel.verifyPasswordCode(code) { verified ->
                    resetCode = verified; dialog = SettingsDialog.CHANGE_PW_STEP3
                }
            },
            onDismiss = { dialog = SettingsDialog.NONE })

        SettingsDialog.CHANGE_PW_STEP3 -> ChangePwStep3Dialog(
            isLoading = state.isLoading,
            onSave    = { newPwd ->
                viewModel.confirmNewPassword(resetCode, newPwd); dialog = SettingsDialog.NONE
            },
            onDismiss = { dialog = SettingsDialog.NONE })

        SettingsDialog.PICK_LANGUAGE -> PickerDialog(
            title    = stringResource(Res.string.select_language),
            options  = Language.entries.map { it to it.displayName },
            current  = state.currentLanguage,
            onSelect = { lang ->
                viewModel.setLanguage(lang); onLanguageChange(lang); dialog = SettingsDialog.NONE
            },
            onDismiss = { dialog = SettingsDialog.NONE })

        SettingsDialog.PICK_THEME -> PickerDialog(
            title    = stringResource(Res.string.settings_app_theme),
            options  = listOf(
                false to "☀️  ${stringResource(Res.string.settings_theme_light)}",
                true  to "🌙  ${stringResource(Res.string.settings_theme_dark)}"
            ),
            current  = state.isDarkMode,
            onSelect = { dark ->
                viewModel.setDarkMode(dark); onDarkModeChange(dark); dialog = SettingsDialog.NONE
            },
            onDismiss = { dialog = SettingsDialog.NONE })

        SettingsDialog.PICK_GENDER_THEME -> PickerDialog(
            title    = stringResource(Res.string.settings_gender_theme),
            options  = listOf(
                GenderTheme.GIRL    to "🎀  ${stringResource(Res.string.settings_gender_girl)}",
                GenderTheme.BOY     to "🚀  ${stringResource(Res.string.settings_gender_boy)}",
                GenderTheme.NEUTRAL to "🌟  ${stringResource(Res.string.settings_gender_neutral)}"
            ),
            current  = state.genderTheme,
            onSelect = { theme ->
                viewModel.setGenderTheme(theme); onGenderThemeChange(theme)
                dialog = SettingsDialog.NONE
            },
            onDismiss = { dialog = SettingsDialog.NONE })

        SettingsDialog.REMINDER_DAYS -> PickerDialog(
            title    = stringResource(Res.string.settings_notif_reminder_days),
            options  = listOf(1, 2, 3, 5, 7, 14).map { d ->
                d to stringResource(Res.string.settings_reminder_days_value, d)
            },
            current  = state.reminderDaysBefore,
            onSelect = { days -> viewModel.setReminderDays(days); dialog = SettingsDialog.NONE },
            onDismiss = { dialog = SettingsDialog.NONE })

        SettingsDialog.CONFIRM_LOGOUT -> ConfirmDialog(
            title       = stringResource(Res.string.settings_logout_title),
            message     = stringResource(Res.string.settings_logout_message),
            confirmText = stringResource(Res.string.settings_logout_confirm),
            isDanger    = false, isLoading = false,
            onConfirm   = { viewModel.logout(); dialog = SettingsDialog.NONE },
            onDismiss   = { dialog = SettingsDialog.NONE })

        SettingsDialog.CONFIRM_DELETE -> ConfirmDialog(
            title       = stringResource(Res.string.settings_delete_title),
            message     = stringResource(Res.string.settings_delete_message),
            confirmText = stringResource(Res.string.settings_delete_confirm),
            isDanger    = true, isLoading = state.isLoading,
            onConfirm   = { viewModel.deleteAccount(); dialog = SettingsDialog.NONE },
            onDismiss   = { dialog = SettingsDialog.NONE })

        SettingsDialog.ABOUT -> InfoDialog(
            stringResource(Res.string.settings_about_app),
            stringResource(Res.string.settings_about_desc)
        ) { dialog = SettingsDialog.NONE }

        SettingsDialog.PRIVACY_POLICY -> InfoDialog(
            stringResource(Res.string.settings_privacy_policy),
            stringResource(Res.string.settings_privacy_policy_body)
        ) { dialog = SettingsDialog.NONE }

        SettingsDialog.TERMS_OF_USE -> InfoDialog(
            stringResource(Res.string.settings_terms),
            stringResource(Res.string.settings_terms_body)
        ) { dialog = SettingsDialog.NONE }

        SettingsDialog.CONTACT_SUPPORT -> ContactSupportDialog { dialog = SettingsDialog.NONE }
        SettingsDialog.NONE -> { /* nothing */ }
    }

    Scaffold(
        snackbarHost   = { SnackbarHost(snackbar) },
        containerColor = Color.Transparent
    ) { inner ->
        if (isLandscape) {
            Row(modifier = Modifier.fillMaxSize().padding(inner)) {
                // ── Left rail ─────────────────────────────────────────────────
                Column(
                    modifier = Modifier
                        .width(dim.settingsLandscapeRailWidth)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surface)
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = dim.spacingSmall)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = dim.spacingMedium, vertical = dim.spacingMedium),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(dim.spacingSmall)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(dim.settingsAvatarSize)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                state.userName.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                state.userName.ifBlank { "—" },
                                style      = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines   = 1
                            )
                            Text(
                                state.userEmail.ifBlank { "—" },
                                style    = MaterialTheme.typography.bodySmall,
                                color    = MaterialTheme.colorScheme.onSurface.copy(0.5f),
                                maxLines = 1
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(Modifier.height(dim.spacingSmall))

                    data class NavItem(val section: SettingsSection, val emoji: String, val label: String)

                    val navItems = buildList {
                        add(NavItem(SettingsSection.ACCOUNT,       "👤", stringResource(Res.string.settings_section_account)))
                        add(NavItem(SettingsSection.PREFERENCES,   "🎨", stringResource(Res.string.settings_section_preferences)))
                        add(NavItem(SettingsSection.NOTIFICATIONS, "🔔", stringResource(Res.string.settings_section_notifications)))
                        if (state.isEmailLogin)
                            add(NavItem(SettingsSection.SECURITY,  "🔒", stringResource(Res.string.settings_section_security)))
                        add(NavItem(SettingsSection.INFORMATION,   "👨‍👩‍👧‍👦", stringResource(Res.string.settings_section_information)))
                        add(NavItem(SettingsSection.ABOUT,         "ℹ️", stringResource(Res.string.settings_section_about)))
                        add(NavItem(SettingsSection.ACTIONS,       "⚠️", stringResource(Res.string.settings_section_actions)))
                    }

                    navItems.forEach { item ->
                        val isSelected = selectedSection == item.section
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isSelected) cc.accentGradientStart.copy(0.12f) else Color.Transparent,
                                    RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
                                )
                                .clickable { selectedSection = item.section }
                                .padding(horizontal = dim.spacingMedium, vertical = dim.spacingSmall + 2.dp),
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(dim.spacingSmall)
                        ) {
                            Text(item.emoji)
                            Text(
                                item.label,
                                style      = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color      = if (isSelected) cc.accentGradientStart
                                else MaterialTheme.colorScheme.onSurface.copy(0.7f)
                            )
                        }
                    }

                    Spacer(Modifier.weight(1f))
                    Text(
                        stringResource(Res.string.settings_version, dim.settingsVersionString),
                        style    = MaterialTheme.typography.bodySmall,
                        color    = MaterialTheme.colorScheme.onBackground.copy(0.28f),
                        modifier = Modifier.padding(dim.spacingMedium)
                    )
                }

                VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // ── Right content pane ────────────────────────────────────────
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                        .padding(dim.spacingLarge),
                    verticalArrangement = Arrangement.spacedBy(dim.spacingMedium)
                ) {
                    val hPad = Modifier
                    when (selectedSection) {
                        SettingsSection.ACCOUNT -> {
                            SectionLabel("👤", stringResource(Res.string.settings_section_account), hPad)
                            SettingsCard(hPad) {
                                ProfileRow(
                                    name    = state.userName,
                                    email   = state.userEmail,
                                    onClick = { dialog = SettingsDialog.EDIT_PROFILE })
                                RowDivider()
                                ArrowRow(
                                    icon    = Icons.Default.Lock,
                                    label   = stringResource(Res.string.settings_change_password),
                                    onClick = { dialog = SettingsDialog.CHANGE_PW_STEP1 })
                            }
                        }

                        SettingsSection.PREFERENCES -> {
                            SectionLabel("🎨", stringResource(Res.string.settings_section_preferences), hPad)
                            SettingsCard(hPad) {
                                ArrowRow(
                                    icon    = Icons.Default.Language,
                                    label   = stringResource(Res.string.settings_language),
                                    value   = state.currentLanguage.displayName,
                                    onClick = { dialog = SettingsDialog.PICK_LANGUAGE })
                                RowDivider()
                                ArrowRow(
                                    icon    = Icons.Default.Palette,
                                    label   = stringResource(Res.string.settings_app_theme),
                                    value   = if (state.isDarkMode) stringResource(Res.string.settings_theme_dark)
                                    else stringResource(Res.string.settings_theme_light),
                                    onClick = { dialog = SettingsDialog.PICK_THEME })
                                RowDivider()
                                ArrowRow(
                                    icon    = Icons.Default.ChildCare,
                                    label   = stringResource(Res.string.settings_gender_theme),
                                    value   = when (state.genderTheme) {
                                        GenderTheme.GIRL    -> stringResource(Res.string.settings_gender_girl)
                                        GenderTheme.BOY     -> stringResource(Res.string.settings_gender_boy)
                                        GenderTheme.NEUTRAL -> stringResource(Res.string.settings_gender_neutral)
                                    },
                                    onClick = { dialog = SettingsDialog.PICK_GENDER_THEME })
                            }
                        }

                        SettingsSection.NOTIFICATIONS -> {
                            SectionLabel("🔔", stringResource(Res.string.settings_section_notifications), hPad)
                            SettingsCard(hPad) {
                                NotificationToggles(
                                    state               = state,
                                    viewModel           = viewModel,
                                    onReminderDaysClick = { dialog = SettingsDialog.REMINDER_DAYS }
                                )
                            }
                        }

                        SettingsSection.SECURITY -> {
                            if (state.isEmailLogin) {
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
                            }
                        }

                        SettingsSection.INFORMATION -> {
                            SectionLabel("👨‍👩‍👧‍👦", stringResource(Res.string.settings_section_information), hPad)
                            SettingsCard(hPad) {
                                FamilyHistorySettingsRow(
                                    isSet    = familyHistoryIsSet,
                                    babyName = selectedBabyName,
                                    onClick  = {
                                        if (selectedBabyId != null)
                                            onNavigateToFamilyHistory(selectedBabyId, selectedBabyName)
                                    })
                                RowDivider()
                                ChildIllnessesSettingsRow(
                                    illnessCount = childIllnessCount,
                                    activeCount  = childIllnessActiveCount,
                                    babyName     = selectedBabyName,
                                    onClick      = {
                                        if (selectedBabyId != null)
                                            onNavigateToChildIllnesses(selectedBabyId, selectedBabyName)
                                    })
                                RowDivider()
                                ChildDevSettingsRowItem(
                                    emoji         = stringResource(Res.string.child_dev_vision_emoji),
                                    title         = stringResource(Res.string.settings_child_dev_vision_motor),
                                    assessedCount = visionMotorCount,
                                    onClick       = {
                                        if (selectedBabyId != null)
                                            onNavigateToVisionMotor(selectedBabyId, selectedBabyName)
                                    })
                                RowDivider()
                                ChildDevSettingsRowItem(
                                    emoji         = stringResource(Res.string.child_dev_hearing_emoji),
                                    title         = stringResource(Res.string.settings_child_dev_hearing_speech),
                                    assessedCount = hearingSpeechCount,
                                    onClick       = {
                                        if (selectedBabyId != null)
                                            onNavigateToHearingSpeech(selectedBabyId, selectedBabyName)
                                    })
                                RowDivider()
                                PreCheckInvestigationSettingsRow(
                                    isSet    = preCheckInvestigationIsSet,
                                    babyName = selectedBabyName,
                                    onClick  = {
                                        if (selectedBabyId != null)
                                            onNavigateToPreCheckInvestigation(selectedBabyId, selectedBabyName)
                                    })
                            }
                        }

                        SettingsSection.ABOUT -> {
                            SectionLabel("ℹ️", stringResource(Res.string.settings_section_about), hPad)
                            SettingsCard(hPad) {
                                ArrowRow(Icons.Default.Info,           stringResource(Res.string.settings_about_app))       { dialog = SettingsDialog.ABOUT }
                                RowDivider()
                                ArrowRow(Icons.Default.Policy,         stringResource(Res.string.settings_privacy_policy))  { dialog = SettingsDialog.PRIVACY_POLICY }
                                RowDivider()
                                ArrowRow(Icons.Default.Gavel,          stringResource(Res.string.settings_terms))           { dialog = SettingsDialog.TERMS_OF_USE }
                                RowDivider()
                                ArrowRow(Icons.Default.ContactSupport, stringResource(Res.string.settings_contact_support)) { dialog = SettingsDialog.CONTACT_SUPPORT }
                            }
                        }

                        SettingsSection.ACTIONS -> {
                            SectionLabel("⚠️", stringResource(Res.string.settings_section_actions), hPad)
                            SettingsCard(hPad) {
                                DangerRow(
                                    icon    = Icons.AutoMirrored.Filled.Logout,
                                    label   = stringResource(Res.string.settings_logout),
                                    color   = cc.warning,
                                    onClick = { dialog = SettingsDialog.CONFIRM_LOGOUT })
                                RowDivider()
                                DangerRow(
                                    icon     = Icons.Default.DeleteForever,
                                    label    = stringResource(Res.string.settings_delete_account),
                                    subtitle = stringResource(Res.string.settings_delete_account_sub),
                                    color    = MaterialTheme.colorScheme.error,
                                    onClick  = { dialog = SettingsDialog.CONFIRM_DELETE })
                            }
                        }
                    }
                }
            }

        } else {
            // ── PORTRAIT ──────────────────────────────────────────────────────
            val hPad = Modifier.padding(horizontal = dim.screenPadding)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(inner)
                    .verticalScroll(rememberScrollState())
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                    Color.Transparent
                                )
                            )
                        )
                        .padding(horizontal = dim.screenPadding, vertical = dim.spacingLarge)
                ) {
                    Column {
                        Text(
                            stringResource(Res.string.settings_title),
                            style      = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color      = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            stringResource(Res.string.settings_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                }
                Spacer(Modifier.height(dim.spacingSmall))

                SectionLabel("👤", stringResource(Res.string.settings_section_account), hPad)
                SettingsCard(hPad) {
                    ProfileRow(
                        name    = state.userName,
                        email   = state.userEmail,
                        onClick = { dialog = SettingsDialog.EDIT_PROFILE })
                    RowDivider()
                    ArrowRow(
                        icon    = Icons.Default.Lock,
                        label   = stringResource(Res.string.settings_change_password),
                        onClick = { dialog = SettingsDialog.CHANGE_PW_STEP1 })
                }
                Spacer(Modifier.height(dim.spacingMedium))

                SectionLabel("🎨", stringResource(Res.string.settings_section_preferences), hPad)
                SettingsCard(hPad) {
                    ArrowRow(
                        icon    = Icons.Default.Language,
                        label   = stringResource(Res.string.settings_language),
                        value   = state.currentLanguage.displayName,
                        onClick = { dialog = SettingsDialog.PICK_LANGUAGE })
                    RowDivider()
                    ArrowRow(
                        icon    = Icons.Default.Palette,
                        label   = stringResource(Res.string.settings_app_theme),
                        value   = if (state.isDarkMode) stringResource(Res.string.settings_theme_dark)
                        else stringResource(Res.string.settings_theme_light),
                        onClick = { dialog = SettingsDialog.PICK_THEME })
                    RowDivider()
                    ArrowRow(
                        icon    = Icons.Default.ChildCare,
                        label   = stringResource(Res.string.settings_gender_theme),
                        value   = when (state.genderTheme) {
                            GenderTheme.GIRL    -> stringResource(Res.string.settings_gender_girl)
                            GenderTheme.BOY     -> stringResource(Res.string.settings_gender_boy)
                            GenderTheme.NEUTRAL -> stringResource(Res.string.settings_gender_neutral)
                        },
                        onClick = { dialog = SettingsDialog.PICK_GENDER_THEME })
                }
                Spacer(Modifier.height(dim.spacingMedium))

                SectionLabel("🔔", stringResource(Res.string.settings_section_notifications), hPad)
                SettingsCard(hPad) {
                    NotificationToggles(
                        state               = state,
                        viewModel           = viewModel,
                        onReminderDaysClick = { dialog = SettingsDialog.REMINDER_DAYS }
                    )
                }
                Spacer(Modifier.height(dim.spacingMedium))

                if (state.isEmailLogin) {
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
                }

                SectionLabel("👨‍👩‍👧‍👦", stringResource(Res.string.settings_section_information), hPad)
                SettingsCard(hPad) {
                    FamilyHistorySettingsRow(
                        isSet    = familyHistoryIsSet,
                        babyName = selectedBabyName,
                        onClick  = {
                            if (selectedBabyId != null)
                                onNavigateToFamilyHistory(selectedBabyId, selectedBabyName)
                        })
                    RowDivider()
                    ChildIllnessesSettingsRow(
                        illnessCount = childIllnessCount,
                        activeCount  = childIllnessActiveCount,
                        babyName     = selectedBabyName,
                        onClick      = {
                            if (selectedBabyId != null)
                                onNavigateToChildIllnesses(selectedBabyId, selectedBabyName)
                        })
                    RowDivider()
                    ChildDevSettingsRowItem(
                        emoji         = stringResource(Res.string.child_dev_vision_emoji),
                        title         = stringResource(Res.string.settings_child_dev_vision_motor),
                        assessedCount = visionMotorCount,
                        onClick       = {
                            if (selectedBabyId != null)
                                onNavigateToVisionMotor(selectedBabyId, selectedBabyName)
                        })
                    RowDivider()
                    ChildDevSettingsRowItem(
                        emoji         = stringResource(Res.string.child_dev_hearing_emoji),
                        title         = stringResource(Res.string.settings_child_dev_hearing_speech),
                        assessedCount = hearingSpeechCount,
                        onClick       = {
                            if (selectedBabyId != null)
                                onNavigateToHearingSpeech(selectedBabyId, selectedBabyName)
                        })
                    RowDivider()
                    // ── Pre-Check Investigation row ───────────────────────────
                    PreCheckInvestigationSettingsRow(
                        isSet    = preCheckInvestigationIsSet,
                        babyName = selectedBabyName,
                        onClick  = {
                            if (selectedBabyId != null)
                                onNavigateToPreCheckInvestigation(selectedBabyId, selectedBabyName)
                        })
                }
                Spacer(Modifier.height(dim.spacingMedium))

                SectionLabel("ℹ️", stringResource(Res.string.settings_section_about), hPad)
                SettingsCard(hPad) {
                    ArrowRow(Icons.Default.Info,           stringResource(Res.string.settings_about_app))       { dialog = SettingsDialog.ABOUT }
                    RowDivider()
                    ArrowRow(Icons.Default.Policy,         stringResource(Res.string.settings_privacy_policy))  { dialog = SettingsDialog.PRIVACY_POLICY }
                    RowDivider()
                    ArrowRow(Icons.Default.Gavel,          stringResource(Res.string.settings_terms))           { dialog = SettingsDialog.TERMS_OF_USE }
                    RowDivider()
                    ArrowRow(Icons.Default.ContactSupport, stringResource(Res.string.settings_contact_support)) { dialog = SettingsDialog.CONTACT_SUPPORT }
                }
                Spacer(Modifier.height(dim.spacingMedium))

                SectionLabel("⚠️", stringResource(Res.string.settings_section_actions), hPad)
                SettingsCard(hPad) {
                    DangerRow(
                        icon    = Icons.AutoMirrored.Filled.Logout,
                        label   = stringResource(Res.string.settings_logout),
                        color   = cc.warning,
                        onClick = { dialog = SettingsDialog.CONFIRM_LOGOUT })
                    RowDivider()
                    DangerRow(
                        icon     = Icons.Default.DeleteForever,
                        label    = stringResource(Res.string.settings_delete_account),
                        subtitle = stringResource(Res.string.settings_delete_account_sub),
                        color    = MaterialTheme.colorScheme.error,
                        onClick  = { dialog = SettingsDialog.CONFIRM_DELETE })
                }

                Spacer(Modifier.height(dim.spacingLarge))
                Text(
                    stringResource(Res.string.settings_version, dim.settingsVersionString),
                    style    = MaterialTheme.typography.bodySmall,
                    color    = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.28f),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = dim.spacingXXLarge)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// NotificationToggles
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun NotificationToggles(
    state               : org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model.SettingsUiState,
    viewModel           : SettingsViewModel,
    onReminderDaysClick : () -> Unit
) {
    val dim = LocalDimensions.current

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
        exit    = shrinkVertically()
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
            ToggleRow(
                icon     = Icons.Default.Favorite,
                label    = stringResource(Res.string.settings_notif_health),
                subtitle = stringResource(Res.string.settings_notif_health_sub),
                checked  = state.healthAlerts,
                onToggle = viewModel::setHealthAlerts
            )
            RowDivider()
            ToggleRow(
                icon     = Icons.Default.Psychology,
                label    = stringResource(Res.string.settings_notif_development),
                subtitle = stringResource(Res.string.settings_notif_development_sub),
                checked  = state.developmentAlerts,
                onToggle = viewModel::setDevelopmentAlerts
            )
            RowDivider()
            ArrowRow(
                icon    = Icons.Default.AccessTime,
                label   = stringResource(Res.string.settings_notif_reminder_days),
                value   = stringResource(Res.string.settings_reminder_days_value, state.reminderDaysBefore),
                onClick = onReminderDaysClick
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Row helpers
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(emoji: String, text: String, modifier: Modifier = Modifier) {
    val dim = LocalDimensions.current
    Row(
        modifier              = modifier.fillMaxWidth().padding(top = dim.spacingSmall, bottom = 4.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dim.spacingXSmall)
    ) {
        Text(emoji)
        Text(
            text.uppercase(),
            style      = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f)
        )
    }
}

@Composable
private fun SettingsCard(
    modifier: Modifier = Modifier,
    content : @Composable ColumnScope.() -> Unit
) {
    val dim = LocalDimensions.current
    Card(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(dim.cardCornerRadius),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth(), content = content)
    }
}

@Composable
private fun RowDivider() {
    val dim = LocalDimensions.current
    HorizontalDivider(
        modifier  = Modifier.padding(horizontal = dim.settingsRowPaddingH),
        thickness = dim.settingsDividerThickness,
        color     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
    )
}

@Composable
private fun ProfileRow(name: String, email: String, onClick: () -> Unit) {
    val dim = LocalDimensions.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = dim.settingsRowPaddingH, vertical = dim.spacingMedium),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dim.spacingSmall)
    ) {
        Box(
            modifier = Modifier
                .size(dim.settingsAvatarSize)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                name.ifBlank { "—" },
                style      = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onSurface
            )
            Text(
                email.ifBlank { "—" },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        Icon(
            Icons.Default.Edit, null,
            tint     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            modifier = Modifier.size(dim.settingsChevronSize)
        )
    }
}

@Composable
private fun ArrowRow(icon: ImageVector, label: String, value: String? = null, onClick: () -> Unit) {
    val dim = LocalDimensions.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = dim.settingsRowPaddingH, vertical = dim.spacingMedium),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dim.spacingSmall)
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(dim.settingsRowIconSize))
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
        if (value != null) {
            Text(value, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            Spacer(Modifier.width(4.dp))
        }
        Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f), modifier = Modifier.size(dim.settingsChevronSize))
    }
}

@Composable
private fun ToggleRow(
    icon    : ImageVector,
    label   : String,
    subtitle: String? = null,
    checked : Boolean,
    onToggle: (Boolean) -> Unit
) {
    val dim = LocalDimensions.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle(!checked) }
            .padding(horizontal = dim.settingsRowPaddingH, vertical = dim.spacingMedium),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dim.spacingSmall)
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(dim.settingsRowIconSize))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            if (subtitle != null)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        }
        Switch(checked = checked, onCheckedChange = onToggle)
    }
}

@Composable
private fun DangerRow(
    icon    : ImageVector,
    label   : String,
    subtitle: String? = null,
    color   : Color,
    onClick : () -> Unit
) {
    val dim = LocalDimensions.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = dim.settingsRowPaddingH, vertical = dim.spacingMedium),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dim.spacingSmall)
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(dim.settingsRowIconSize))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = color, fontWeight = FontWeight.Medium)
            if (subtitle != null)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = color.copy(alpha = 0.6f))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Dialog composables
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun <T> PickerDialog(
    title    : String,
    options  : List<Pair<T, String>>,
    current  : T,
    onSelect : (T) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                options.forEach { (value, label) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { onSelect(value) }.padding(vertical = 12.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        RadioButton(selected = value == current, onClick = { onSelect(value) })
                        Text(label, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        },
        confirmButton  = {},
        dismissButton  = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.btn_cancel)) } })
}

@Composable
private fun ConfirmDialog(
    title      : String,
    message    : String,
    confirmText: String,
    isDanger   : Boolean,
    isLoading  : Boolean,
    onConfirm  : () -> Unit,
    onDismiss  : () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text  = { Text(message) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isLoading,
                colors  = if (isDanger)
                    ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                else ButtonDefaults.buttonColors()
            ) {
                if (isLoading) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                else Text(confirmText)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.btn_cancel)) } })
}

@Composable
private fun InfoDialog(title: String, body: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(body, style = MaterialTheme.typography.bodyMedium)
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.add_baby_date_ok)) } })
}

@Composable
private fun ContactSupportDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.settings_contact_support), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(Res.string.settings_contact_support_intro), style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Email, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Text(
                        stringResource(Res.string.settings_support_email),
                        style      = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color      = MaterialTheme.colorScheme.primary
                    )
                }
                Text(stringResource(Res.string.settings_support_response_time), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.add_baby_date_ok)) } })
}

@Composable
private fun EditProfileDialog(
    initialName : String,
    initialPhone: String,
    isLoading   : Boolean,
    onSave      : (String, String) -> Unit,
    onDismiss   : () -> Unit
) {
    var name  by remember { mutableStateOf(initialName) }
    var phone by remember { mutableStateOf(initialPhone) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.settings_edit_profile)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name,  onValueChange = { name  = it }, label = { Text(stringResource(Res.string.settings_field_name))  }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text(stringResource(Res.string.settings_field_phone)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = { onSave(name, phone) }, enabled = !isLoading) {
                if (isLoading) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                else Text(stringResource(Res.string.settings_save))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.btn_cancel)) } })
}

@Composable
private fun ChangePwStep1Dialog(email: String, isLoading: Boolean, onSend: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.settings_change_password)) },
        text  = { Text(stringResource(Res.string.settings_change_pw_send_desc, email)) },
        confirmButton = {
            Button(onClick = onSend, enabled = !isLoading) {
                if (isLoading) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                else Text(stringResource(Res.string.settings_send_code))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.btn_cancel)) } })
}

@Composable
private fun ChangePwStep2Dialog(isLoading: Boolean, onVerify: (String) -> Unit, onDismiss: () -> Unit) {
    var code by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.settings_enter_code_title)) },
        text = {
            OutlinedTextField(
                value         = code,
                onValueChange = { if (it.length <= 6) code = it },
                label         = { Text(stringResource(Res.string.settings_enter_code_hint)) },
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        },
        confirmButton = {
            Button(onClick = { onVerify(code) }, enabled = !isLoading && code.length == 6) {
                if (isLoading) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                else Text(stringResource(Res.string.settings_verify_code))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.btn_cancel)) } })
}

@Composable
private fun ChangePwStep3Dialog(isLoading: Boolean, onSave: (String) -> Unit, onDismiss: () -> Unit) {
    var pwd     by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var visible by remember { mutableStateOf(false) }
    val mismatch = pwd.isNotBlank() && confirm.isNotBlank() && pwd != confirm
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.settings_new_password_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value                = pwd,
                    onValueChange        = { pwd = it },
                    label                = { Text(stringResource(Res.string.settings_new_password)) },
                    singleLine           = true,
                    modifier             = Modifier.fillMaxWidth(),
                    visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { visible = !visible }) {
                            Icon(if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                        }
                    })
                OutlinedTextField(
                    value                = confirm,
                    onValueChange        = { confirm = it },
                    label                = { Text(stringResource(Res.string.settings_confirm_password)) },
                    singleLine           = true,
                    modifier             = Modifier.fillMaxWidth(),
                    visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
                    isError              = mismatch
                )
                if (mismatch)
                    Text(stringResource(Res.string.settings_passwords_no_match), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = {
            Button(onClick = { onSave(pwd) }, enabled = !isLoading && pwd.isNotBlank() && pwd == confirm) {
                if (isLoading) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                else Text(stringResource(Res.string.settings_save))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.btn_cancel)) } })
}