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

    // Save Password — only meaningful for email/password logins.
    // For Google logins this is always false and the toggle is hidden in the UI.
    val savePasswordEnabled  : Boolean     = false,
    val isEmailLogin         : Boolean     = false,   // drives whether the toggle is visible

    val notificationsEnabled : Boolean     = true,
    val vaccinationReminders : Boolean     = true,
    val growthAlerts         : Boolean     = true,
    val appointmentReminders : Boolean     = true,
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
    private val apiService         : ApiService,
    private val preferencesManager : PreferencesManager,
    private val accountRepository  : AccountRepository,
) {
    var uiState by mutableStateOf(SettingsUiState())
        private set

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    init {
        loadFromPreferences()
        loadUserProfile() // ✅ FIX: Fetch latest profile from server on startup
    }

    // ─────────────────────────────────────────────────────────────────────────
    // INIT
    // ─────────────────────────────────────────────────────────────────────────

    private fun loadFromPreferences() {
        val isEmail = accountRepository.isEmailLogin()
        uiState = uiState.copy(
            userName             = preferencesManager.getUserName()  ?: "",
            userEmail            = preferencesManager.getUserEmail() ?: "",
            userPhone            = preferencesManager.getUserPhone() ?: "",
            userId               = preferencesManager.getUserId()    ?: "",
            currentLanguage      = preferencesManager.getCurrentLanguage(),
            isDarkMode           = preferencesManager.getBoolean("dark_mode", false),
            genderTheme          = preferencesManager.getGenderTheme(),
            // Save Password is only shown and active for email logins
            isEmailLogin         = isEmail,
            savePasswordEnabled  = if (isEmail) preferencesManager.isSavePasswordEnabled() else false,
            notificationsEnabled = preferencesManager.getBoolean("notif_enabled",     true),
            vaccinationReminders = preferencesManager.getBoolean("notif_vaccination", true),
            growthAlerts         = preferencesManager.getBoolean("notif_growth",      true),
            appointmentReminders = preferencesManager.getBoolean("notif_appointment", true),
            reminderDaysBefore   = preferencesManager.getInt("notif_reminder_days",   3),
        )
    }

    /** ✅ NEW: Fetch user profile from API to ensure settings info is up-to-date */
    private fun loadUserProfile() {
        val userId = uiState.userId.ifBlank { return }
        scope.launch {
            when (val result = apiService.getUser(userId)) {
                is ApiResult.Success -> {
                    val user = result.data
                    // Sync to Preferences
                    preferencesManager.saveUserName(user.fullName)
                    preferencesManager.saveUserEmail(user.email)
                    user.phone?.let { preferencesManager.saveUserPhone(it) }

                    // Sync to UI
                    uiState = uiState.copy(
                        userName  = user.fullName,
                        userEmail = user.email,
                        userPhone = user.phone ?: ""
                    )
                }
                else -> { /* Fallback to what we have in preferences */ }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ACCOUNT — update profile  →  PUT /v1/users/{userId}
    // ─────────────────────────────────────────────────────────────────────────

    fun updateProfile(name: String, phone: String) {
        val userId = uiState.userId.ifBlank {
            uiState = uiState.copy(errorMessage = "Session expired. Please log in again.")
            return
        }
        scope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            when (val result = apiService.updateUser(userId, UpdateUserRequest(fullName = name, phone = phone))) {
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

    // ─────────────────────────────────────────────────────────────────────────
    // CHANGE PASSWORD (3-step)
    // ─────────────────────────────────────────────────────────────────────────

    /** Step 1 — send reset code to user's email */
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

    /** Step 2 — verify the 6-digit code */
    fun verifyPasswordCode(code: String, onVerified: (verifiedCode: String) -> Unit) {
        val email = uiState.userEmail.ifBlank { return }
        scope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            when (val result = accountRepository.verifyResetCode(email, code)) {
                is ApiResult.Success -> {
                    uiState = uiState.copy(isLoading = false)
                    onVerified(code)
                }
                is ApiResult.Error   -> uiState = uiState.copy(isLoading = false, errorMessage = result.message)
                is ApiResult.Loading -> uiState = uiState.copy(isLoading = false)
            }
        }
    }

    /** Step 3 — set the new password */
    fun confirmNewPassword(verifiedCode: String, newPassword: String) {
        val email = uiState.userEmail.ifBlank { return }
        if (newPassword.length < 6) {
            uiState = uiState.copy(errorMessage = "Password must be at least 6 characters")
            return
        }
        scope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            when (val result = accountRepository.resetPasswordWithCode(email, verifiedCode, newPassword)) {
                is ApiResult.Success -> {
                    // If "Save Password" is enabled, update the stored password to the new one
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

    // ─────────────────────────────────────────────────────────────────────────
    // PREFERENCES
    // ─────────────────────────────────────────────────────────────────────────

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

    // ─────────────────────────────────────────────────────────────────────────
    // NOTIFICATIONS
    // ─────────────────────────────────────────────────────────────────────────

    fun setNotificationsEnabled(on: Boolean) {
        preferencesManager.putBoolean("notif_enabled", on)
        uiState = uiState.copy(notificationsEnabled = on)
    }

    fun setVaccinationReminders(on: Boolean) {
        preferencesManager.putBoolean("notif_vaccination", on)
        uiState = uiState.copy(vaccinationReminders = on)
    }

    fun setGrowthAlerts(on: Boolean) {
        preferencesManager.putBoolean("notif_growth", on)
        uiState = uiState.copy(growthAlerts = on)
    }

    fun setAppointmentReminders(on: Boolean) {
        preferencesManager.putBoolean("notif_appointment", on)
        uiState = uiState.copy(appointmentReminders = on)
    }

    fun setReminderDays(days: Int) {
        preferencesManager.putInt("notif_reminder_days", days)
        uiState = uiState.copy(reminderDaysBefore = days)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SECURITY — Save Password toggle
    //
    // Only reachable for email login users (isEmailLogin = true).
    // The UI hides the toggle entirely for Google users so this method is
    // never called for them.
    //
    // ON  → store email + password in prefs (same key as LoginScreen checkbox)
    // OFF → clear stored credentials
    //
    // Both directions update the same KEY_SAVE_PASSWORD_ENABLED flag that
    // LoginScreen reads on startup, so the two are always in sync.
    // ─────────────────────────────────────────────────────────────────────────

    fun setSavePassword(on: Boolean) {
        if (on) {
            val email    = preferencesManager.getUserEmail() ?: ""
            // Use the already-stored password if available; if the user has never
            // saved before there is no password in prefs, so we cannot enable this.
            val password = preferencesManager.getSavedPassword() ?: ""

            if (email.isBlank() || password.isBlank()) {
                uiState = uiState.copy(
                    errorMessage = "To enable this, please log out and log in again with 'Save Password' checked."
                )
                // Do NOT update the toggle — leave it as false
                return
            }
            preferencesManager.saveLoginCredentials(email, password)
        } else {
            preferencesManager.clearLoginCredentials()
        }
        uiState = uiState.copy(savePasswordEnabled = on)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ACCOUNT ACTIONS
    // ─────────────────────────────────────────────────────────────────────────

    fun logout() {
        accountRepository.logout()
        uiState = uiState.copy(navigateToWelcome = true)
    }

    fun deleteAccount() {
        val userId = uiState.userId.ifBlank {
            uiState = uiState.copy(errorMessage = "Cannot identify account. Please log in again.")
            return
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

    // ─────────────────────────────────────────────────────────────────────────
    // LIFECYCLE
    // ─────────────────────────────────────────────────────────────────────────

    fun clearMessages() {
        uiState = uiState.copy(successMessage = null, errorMessage = null)
    }

    fun onDestroy() {
        scope.coroutineContext[kotlinx.coroutines.Job]?.cancel()
    }
}