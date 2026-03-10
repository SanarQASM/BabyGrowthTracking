package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.data.Language
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.data.PreferencesManager
import org.example.project.babygrowthtrackingapplication.data.network.ApiResult
import org.example.project.babygrowthtrackingapplication.data.network.ApiService
import org.example.project.babygrowthtrackingapplication.data.network.UpdateUserRequest
import org.example.project.babygrowthtrackingapplication.data.repository.AccountRepository
import org.example.project.babygrowthtrackingapplication.theme.GenderTheme

// =============================================================================
// UI State
// =============================================================================

data class SettingsUiState(
    // Account info
    val userName  : String = "",
    val userEmail : String = "",
    val userPhone : String = "",
    val userId    : String = "",

    // Preferences
    val currentLanguage : Language    = Language.ENGLISH,
    val isDarkMode      : Boolean     = false,
    val genderTheme     : GenderTheme = GenderTheme.NEUTRAL,

    // Notifications (local prefs — FCM integration future work)
    val notificationsEnabled : Boolean = true,
    val vaccinationReminders : Boolean = true,
    val growthAlerts         : Boolean = true,
    val appointmentReminders : Boolean = true,
    val reminderDaysBefore   : Int     = 3,

    // Security
    val savePasswordEnabled : Boolean = false,

    // Async feedback
    val isLoading      : Boolean = false,
    val successMessage : String? = null,
    val errorMessage   : String? = null,

    // Navigation signal (true → navigate to Welcome screen)
    val navigateToWelcome : Boolean = false,
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

    init { loadFromPreferences() }

    // ─────────────────────────────────────────────────────────────────────────
    // INIT — load everything from local prefs
    // ─────────────────────────────────────────────────────────────────────────

    private fun loadFromPreferences() {
        uiState = uiState.copy(
            userName             = preferencesManager.getUserName()  ?: "",
            userEmail            = preferencesManager.getUserEmail() ?: "",
            userPhone            = preferencesManager.getUserPhone() ?: "",
            userId               = preferencesManager.getUserId()    ?: "",
            currentLanguage      = preferencesManager.getCurrentLanguage(),
            isDarkMode           = preferencesManager.getBoolean("dark_mode", false),
            genderTheme          = preferencesManager.getGenderTheme(),
            savePasswordEnabled  = preferencesManager.isSavePasswordEnabled(),
            notificationsEnabled = preferencesManager.getBoolean("notif_enabled",     true),
            vaccinationReminders = preferencesManager.getBoolean("notif_vaccination", true),
            growthAlerts         = preferencesManager.getBoolean("notif_growth",      true),
            appointmentReminders = preferencesManager.getBoolean("notif_appointment", true),
            reminderDaysBefore   = preferencesManager.getInt("notif_reminder_days",   3),
        )
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
            when (val result = apiService.updateUser(
                userId  = userId,
                request = UpdateUserRequest(
                    fullName = name.trim().ifBlank { null },
                    phone    = phone.trim().ifBlank { null }
                )
            )) {
                is ApiResult.Success -> {
                    preferencesManager.saveUserName(result.data.fullName)
                    result.data.phone?.let { preferencesManager.saveUserPhone(it) }
                    uiState = uiState.copy(
                        isLoading      = false,
                        userName       = result.data.fullName,
                        userPhone      = result.data.phone ?: "",
                        successMessage = "Profile updated successfully ✓"
                    )
                }
                is ApiResult.Error   -> uiState = uiState.copy(isLoading = false, errorMessage = result.message)
                is ApiResult.Loading -> uiState = uiState.copy(isLoading = false)
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ACCOUNT — Change Password  (3-step forgot-password flow reused in-app)
    //   Step 1: POST /auth/forgot-password  → sends 6-digit code to email
    //   Step 2: POST /auth/verify-reset-code
    //   Step 3: POST /auth/reset-password
    // ─────────────────────────────────────────────────────────────────────────

    /** Step 1 — request a reset code sent to the user's registered email */
    fun sendPasswordResetCode(onCodeSent: () -> Unit) {
        val email = uiState.userEmail.ifBlank {
            uiState = uiState.copy(errorMessage = "No email on file. Please log in again.")
            return
        }
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
                is ApiResult.Success -> uiState = uiState.copy(
                    isLoading      = false,
                    successMessage = "Password changed successfully ✓"
                )
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
    // SECURITY
    // ─────────────────────────────────────────────────────────────────────────

    fun setSavePassword(on: Boolean) {
        if (on) {
            val email = preferencesManager.getUserEmail() ?: ""
            val pwd   = preferencesManager.getSavedPassword() ?: ""
            if (email.isNotBlank() && pwd.isNotBlank()) {
                preferencesManager.saveLoginCredentials(email, pwd)
            }
        } else {
            preferencesManager.clearLoginCredentials()
        }
        uiState = uiState.copy(savePasswordEnabled = on)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ACCOUNT ACTIONS
    // ─────────────────────────────────────────────────────────────────────────

    /** Standard logout — preserves saved credentials for pre-fill */
    fun logout() {
        accountRepository.logout()
        uiState = uiState.copy(navigateToWelcome = true)
    }

    /**
     * Delete account:
     *  1. DELETE /v1/users/{userId}  →  backend removes user + all children (cascade)
     *  2. clearAllData()             →  wipe all local prefs / token
     *  3. navigateToWelcome = true   →  Navigation.kt sends user to WelcomeScreen
     */
    fun deleteAccount() {
        val userId = uiState.userId.ifBlank {
            uiState = uiState.copy(errorMessage = "Cannot identify account. Please log in again.")
            return
        }
        scope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            when (val result = apiService.deleteUser(userId)) {
                is ApiResult.Success -> {
                    preferencesManager.clearAllData()
                    uiState = uiState.copy(isLoading = false, navigateToWelcome = true)
                }
                is ApiResult.Error   -> uiState = uiState.copy(isLoading = false, errorMessage = result.message)
                is ApiResult.Loading -> uiState = uiState.copy(isLoading = false)
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    fun clearMessages() {
        uiState = uiState.copy(successMessage = null, errorMessage = null)
    }

    fun onDestroy() { scope.cancel() }
}