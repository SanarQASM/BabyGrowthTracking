package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model

import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.data.Language
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.data.PreferencesManager
import org.example.project.babygrowthtrackingapplication.data.network.ApiResult
import org.example.project.babygrowthtrackingapplication.data.network.ApiService
import org.example.project.babygrowthtrackingapplication.data.network.UpdateUserRequest
import org.example.project.babygrowthtrackingapplication.data.repository.AccountRepository
import org.example.project.babygrowthtrackingapplication.notifications.NotificationViewModel
import org.example.project.babygrowthtrackingapplication.notifications.UpdateNotificationPreferencesRequest
import org.example.project.babygrowthtrackingapplication.theme.GenderTheme

// =============================================================================
// UiState
// =============================================================================

data class SettingsUiState(
    val userName             : String      = "",
    val userEmail            : String      = "",
    val userPhone            : String      = "",
    val userId               : String      = "",
    val currentLanguage      : Language    = Language.ENGLISH,
    val isDarkMode           : Boolean     = false,
    val genderTheme          : GenderTheme = GenderTheme.NEUTRAL,

    val savePasswordEnabled  : Boolean     = false,
    val isEmailLogin         : Boolean     = false,

    val notificationsEnabled : Boolean     = true,
    val vaccinationReminders : Boolean     = true,
    val growthAlerts         : Boolean     = true,
    val appointmentReminders : Boolean     = true,
    val healthAlerts         : Boolean     = true,       // FIX: was missing, never persisted
    val developmentAlerts    : Boolean     = true,       // FIX: was missing, never persisted
    val reminderDaysBefore   : Int         = 3,

    val isLoading            : Boolean     = false,
    val successMessage       : String?     = null,
    val errorMessage         : String?     = null,
    val navigateToWelcome    : Boolean     = false,
)

// =============================================================================
// ViewModel
// =============================================================================

class SettingsViewModel(
    private val apiService            : ApiService,
    private val preferencesManager    : PreferencesManager,
    private val accountRepository     : AccountRepository,
    private val notificationViewModel : NotificationViewModel
) {
    var uiState by mutableStateOf(SettingsUiState())
        private set

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    init {
        loadFromPreferences()
        loadUserProfile()
    }

    // ─── Init ────────────────────────────────────────────────────────────────

    private fun loadFromPreferences() {
        val isEmail = accountRepository.isEmailLogin()
        uiState = uiState.copy(
            userName             = preferencesManager.getUserName()  ?: "",
            userEmail            = preferencesManager.getUserEmail() ?: "",
            userPhone            = preferencesManager.getUserPhone() ?: "",
            userId               = preferencesManager.getUserId()    ?: "",
            currentLanguage      = preferencesManager.getCurrentLanguage(),
            isDarkMode           = preferencesManager.getBoolean("dark_mode",           false),
            genderTheme          = preferencesManager.getGenderTheme(),
            isEmailLogin         = isEmail,
            savePasswordEnabled  = if (isEmail) preferencesManager.isSavePasswordEnabled() else false,
            notificationsEnabled = preferencesManager.getBoolean("notif_enabled",        true),
            vaccinationReminders = preferencesManager.getBoolean("notif_vaccination",    true),
            growthAlerts         = preferencesManager.getBoolean("notif_growth",         true),
            appointmentReminders = preferencesManager.getBoolean("notif_appointment",    true),
            // FIX: load health + development from prefs (were never loaded before)
            healthAlerts         = preferencesManager.getBoolean("notif_health",         true),
            developmentAlerts    = preferencesManager.getBoolean("notif_development",    true),
            reminderDaysBefore   = preferencesManager.getInt("notif_reminder_days",      3),
        )
    }

    fun refreshProfile() {
        loadFromPreferences()
        loadUserProfile()
    }

    private fun loadUserProfile() {
        val userId = uiState.userId.ifBlank { return }
        scope.launch {
            when (val result = apiService.getUser(userId)) {
                is ApiResult.Success -> {
                    val user = result.data
                    preferencesManager.saveUserName(user.fullName)
                    preferencesManager.saveUserEmail(user.email)
                    user.phone?.let { preferencesManager.saveUserPhone(it) }
                    uiState = uiState.copy(
                        userName  = user.fullName,
                        userEmail = user.email,
                        userPhone = user.phone ?: ""
                    )
                }
                else -> {}
            }
        }
    }

    // ─── Account actions ─────────────────────────────────────────────────────

    fun updateProfile(name: String, phone: String) {
        val userId = uiState.userId.ifBlank {
            uiState = uiState.copy(errorMessage = "Session expired. Please log in again."); return
        }
        scope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            when (val result = apiService.updateUser(
                userId,
                UpdateUserRequest(fullName = name, phone = phone)
            )) {
                is ApiResult.Success -> {
                    preferencesManager.saveUserName(name)
                    preferencesManager.saveUserPhone(phone)
                    uiState = uiState.copy(
                        isLoading      = false,
                        userName       = name,
                        userPhone      = phone,
                        successMessage = "Profile updated ✓"
                    )
                }
                is ApiResult.Error   -> uiState = uiState.copy(isLoading = false, errorMessage = result.message)
                is ApiResult.Loading -> uiState = uiState.copy(isLoading = false)
            }
        }
    }

    fun sendPasswordResetCode(onCodeSent: () -> Unit) {
        val email = uiState.userEmail.ifBlank { return }
        scope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            when (val result = accountRepository.requestPasswordReset(email)) {
                is ApiResult.Success -> {
                    uiState = uiState.copy(isLoading = false, successMessage = "Code sent to $email")
                    onCodeSent()
                }
                is ApiResult.Error   -> uiState = uiState.copy(isLoading = false, errorMessage = result.message)
                is ApiResult.Loading -> uiState = uiState.copy(isLoading = false)
            }
        }
    }

    fun verifyPasswordCode(code: String, onVerified: (verifiedCode: String) -> Unit) {
        val email = uiState.userEmail.ifBlank { return }
        scope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            when (val result = accountRepository.verifyResetCode(email, code)) {
                is ApiResult.Success -> { uiState = uiState.copy(isLoading = false); onVerified(code) }
                is ApiResult.Error   -> uiState = uiState.copy(isLoading = false, errorMessage = result.message)
                is ApiResult.Loading -> uiState = uiState.copy(isLoading = false)
            }
        }
    }

    fun confirmNewPassword(verifiedCode: String, newPassword: String) {
        val email = uiState.userEmail.ifBlank { return }
        if (newPassword.length < 6) {
            uiState = uiState.copy(errorMessage = "Password must be at least 6 characters"); return
        }
        scope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            when (val result = accountRepository.resetPasswordWithCode(email, verifiedCode, newPassword)) {
                is ApiResult.Success -> {
                    if (uiState.savePasswordEnabled) {
                        val emailOrPhone = preferencesManager.getSavedEmailOrPhone() ?: email
                        preferencesManager.saveLoginCredentials(emailOrPhone, newPassword)
                    }
                    uiState = uiState.copy(isLoading = false, successMessage = "Password changed successfully ✓")
                }
                is ApiResult.Error   -> uiState = uiState.copy(isLoading = false, errorMessage = result.message)
                is ApiResult.Loading -> uiState = uiState.copy(isLoading = false)
            }
        }
    }

    // ─── Preferences (local + backend sync) ──────────────────────────────────

    fun setLanguage(language: Language) {
        preferencesManager.setLanguage(language)
        uiState = uiState.copy(currentLanguage = language)
    }

    fun setDarkMode(dark: Boolean) {
        preferencesManager.putBoolean("dark_mode", dark)
        uiState = uiState.copy(isDarkMode = dark)
    }

    fun setGenderTheme(theme: GenderTheme) {
        preferencesManager.saveGenderTheme(theme)
        uiState = uiState.copy(genderTheme = theme)
    }

    // ─── Notification toggles — write locally AND sync to backend ────────────
    //
    // FIX 1: notificationViewModel is now non-nullable so syncPrefs() always fires.
    //
    // FIX 2: setNotificationsEnabled() now restores ALL six categories (including
    //         health + development which were previously omitted) when turning ON.
    //
    // FIX 3: Added setHealthAlerts() and setDevelopmentAlerts() which were
    //         completely absent before — toggles existed in UI but had no handler.

    fun setNotificationsEnabled(on: Boolean) {
        preferencesManager.putBoolean("notif_enabled", on)
        uiState = uiState.copy(notificationsEnabled = on)
        if (!on) {
            // Master OFF → disable every category on the backend
            syncPrefs(
                vaccination  = false,
                growth       = false,
                appointment  = false,
                health       = false,
                development  = false,
                milestones   = false,
                general      = false
            )
        } else {
            // FIX: restore ALL six per-category values, not just three
            syncPrefs(
                vaccination  = uiState.vaccinationReminders,
                growth       = uiState.growthAlerts,
                appointment  = uiState.appointmentReminders,
                health       = uiState.healthAlerts,         // was missing before
                development  = uiState.developmentAlerts,    // was missing before
                milestones   = true,
                general      = true
            )
        }
    }

    fun setVaccinationReminders(on: Boolean) {
        preferencesManager.putBoolean("notif_vaccination", on)
        uiState = uiState.copy(vaccinationReminders = on)
        syncPrefs(vaccination = on)
    }

    fun setGrowthAlerts(on: Boolean) {
        preferencesManager.putBoolean("notif_growth", on)
        uiState = uiState.copy(growthAlerts = on)
        syncPrefs(growth = on)
    }

    fun setAppointmentReminders(on: Boolean) {
        preferencesManager.putBoolean("notif_appointment", on)
        uiState = uiState.copy(appointmentReminders = on)
        syncPrefs(appointment = on)
    }

    // FIX: these two functions were completely missing — UI had no handler
    fun setHealthAlerts(on: Boolean) {
        preferencesManager.putBoolean("notif_health", on)
        uiState = uiState.copy(healthAlerts = on)
        syncPrefs(health = on)
    }

    fun setDevelopmentAlerts(on: Boolean) {
        preferencesManager.putBoolean("notif_development", on)
        uiState = uiState.copy(developmentAlerts = on)
        syncPrefs(development = on)
    }

    fun setReminderDays(days: Int) {
        preferencesManager.putInt("notif_reminder_days", days)
        uiState = uiState.copy(reminderDaysBefore = days)
        syncPrefs(reminderDaysBefore = days)
    }

    /**
     * FIX: notificationViewModel is now non-nullable — this always executes.
     * Previously the entire sync was silently skipped whenever the DI graph
     * didn't inject the ViewModel (the default `null` was never replaced).
     */
    private fun syncPrefs(
        vaccination       : Boolean? = null,
        growth            : Boolean? = null,
        appointment       : Boolean? = null,
        health            : Boolean? = null,
        development       : Boolean? = null,
        milestones        : Boolean? = null,
        general           : Boolean? = null,
        reminderDaysBefore: Int?     = null
    ) {
        notificationViewModel.updatePreferences(
            UpdateNotificationPreferencesRequest(
                vaccination        = vaccination,
                growth             = growth,
                appointment        = appointment,
                health             = health,
                development        = development,
                milestones         = milestones,
                general            = general,
                reminderDaysBefore = reminderDaysBefore
            )
        )
    }

    fun setSavePassword(on: Boolean) {
        if (on) {
            val email    = preferencesManager.getUserEmail() ?: ""
            val password = preferencesManager.getSavedPassword() ?: ""
            if (email.isBlank() || password.isBlank()) {
                uiState = uiState.copy(
                    errorMessage = "Please log in again with 'Save Password' checked."
                ); return
            }
            preferencesManager.saveLoginCredentials(email, password)
        } else {
            preferencesManager.clearLoginCredentials()
        }
        uiState = uiState.copy(savePasswordEnabled = on)
    }

    fun logout() {
        accountRepository.logout()
        uiState = uiState.copy(navigateToWelcome = true)
    }

    fun deleteAccount() {
        val userId = uiState.userId.ifBlank {
            uiState = uiState.copy(
                errorMessage = "Cannot identify account. Please log in again."
            ); return
        }
        scope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            when (val result = apiService.deleteUser(userId)) {
                is ApiResult.Success -> {
                    accountRepository.logoutAndClearCredentials()
                    uiState = uiState.copy(isLoading = false, navigateToWelcome = true)
                }
                is ApiResult.Error   -> uiState = uiState.copy(isLoading = false, errorMessage = result.message)
                is ApiResult.Loading -> uiState = uiState.copy(isLoading = false)
            }
        }
    }

    fun clearMessages() {
        uiState = uiState.copy(successMessage = null, errorMessage = null)
    }

    fun clearState() {
        // Reset to defaults — prevents previous user's name/email/phone
        // from appearing briefly when a new user reaches the settings tab
        uiState = SettingsUiState()
    }

    fun onDestroy() {
        scope.coroutineContext[kotlinx.coroutines.Job]?.cancel()
    }
}