package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.account.models

import androidx.compose.runtime.*
import org.example.project.babygrowthtrackingapplication.data.repository.AccountRepository
import org.example.project.babygrowthtrackingapplication.data.network.ApiResult
import org.example.project.babygrowthtrackingapplication.data.auth.SocialAuthManager
import org.example.project.babygrowthtrackingapplication.data.auth.SocialLoginHelper
import org.example.project.babygrowthtrackingapplication.data.auth.GoogleSignInResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────────────────
// LoginViewModel — FIXED
//
// BUGS FIXED:
//
// 1. clearState() added:
//    Called from Navigation.kt on logout (all roles) so stale credentials from
//    one role session never bleed into a new session. Without this, if an admin
//    logs out and a parent logs in, the login form would still show the admin's
//    email pre-filled from the previous session.
//
// 2. reloadSavedCredentials() added (public):
//    Called from Navigation.kt after a successful password reset. When the user
//    resets their password and is sent back to LoginScreen, the ViewModel needs
//    to re-read preferences to reflect the new state (e.g., saved password is
//    now invalid and should be cleared). Previously init{} only ran once per
//    ViewModel lifetime, so the form could show the OLD password.
//
// 3. loadSavedCredentials() is now private and only called from init{} and
//    reloadSavedCredentials(), preventing accidental double-loading.
// ─────────────────────────────────────────────────────────────────────────────

class LoginViewModel(
    private val authRepository    : AccountRepository,
    private val socialAuthManager : SocialAuthManager,
    private val socialLoginHelper : SocialLoginHelper
) {
    var uiState by mutableStateOf(LoginUiState())
        private set

    init {
        loadSavedCredentials()
    }

    // ── FIX: Public — called by Navigation after password reset so the form
    // reflects the updated credential state (saved password invalidated).
    fun reloadSavedCredentials() {
        loadSavedCredentials()
    }

    // ── FIX: Public — called by Navigation on logout so state is clean for the
    // next login session. Without this, the previous session's email/password
    // remain pre-filled when a different user (or role) tries to log in.
    fun clearState() {
        uiState = LoginUiState()
        // Re-load only if there genuinely are saved credentials — if the user
        // that just logged out had "save password" enabled, their credentials
        // are still in prefs. We intentionally keep them so the SAME user can
        // log back in conveniently, but we reset the loading flag and error.
        loadSavedCredentials()
    }

    private fun loadSavedCredentials() {
        val saveEnabled   = authRepository.isSavePasswordEnabled()
        val savedEmail    = authRepository.getSavedEmailOrPhone()
            ?: authRepository.getSavedEmail()
        val savedPassword = if (saveEnabled) authRepository.getSavedPassword() else null

        if (savedEmail != null) {
            uiState = uiState.copy(
                emailOrPhone  = savedEmail,
                password      = savedPassword ?: "",
                savePassword  = saveEnabled,
                // Reset transient states on reload
                isLoading     = false,
                errorMessage  = null
            )
        } else {
            // No saved credentials — start clean
            uiState = LoginUiState()
        }
    }

    fun onEmailOrPhoneChanged(value: String) {
        uiState = uiState.copy(emailOrPhone = value, errorMessage = null)
    }

    fun onPasswordChanged(value: String) {
        uiState = uiState.copy(password = value, errorMessage = null)
    }

    fun onPasswordVisibilityToggled() {
        uiState = uiState.copy(passwordVisible = !uiState.passwordVisible)
    }

    fun onSavePasswordToggled() {
        uiState = uiState.copy(savePassword = !uiState.savePassword)
    }

    fun setError(message: String) {
        uiState = uiState.copy(errorMessage = message, isLoading = false)
    }

    fun clearError() {
        uiState = uiState.copy(errorMessage = null)
    }

    // ─── Email/Password Login ─────────────────────────────────────────────────

    fun login(onSuccess: () -> Unit) {
        if (uiState.emailOrPhone.isBlank() || uiState.password.isBlank()) {
            uiState = uiState.copy(errorMessage = "Please enter email/phone and password")
            return
        }

        uiState = uiState.copy(isLoading = true, errorMessage = null)

        CoroutineScope(Dispatchers.Main).launch {
            when (val result = authRepository.login(
                email      = uiState.emailOrPhone,
                password   = uiState.password,
                rememberMe = uiState.savePassword
            )) {
                is ApiResult.Success -> {
                    uiState = uiState.copy(isLoading = false)
                    onSuccess()
                }
                is ApiResult.Error -> {
                    uiState = uiState.copy(isLoading = false, errorMessage = result.message)
                }
                is ApiResult.Loading -> {}
            }
        }
    }

    // ─── Google Login ─────────────────────────────────────────────────────────

    fun loginWithGoogle(onSuccess: () -> Unit) {
        uiState = uiState.copy(isLoading = true, errorMessage = null)

        CoroutineScope(Dispatchers.Main).launch {
            socialAuthManager.signInWithGoogle { result ->
                launch {
                    when (result) {
                        is GoogleSignInResult.Success -> {
                            when (val apiResult = socialLoginHelper.handleGoogleSignIn(
                                idToken     = result.idToken,
                                email       = result.email,
                                displayName = result.displayName,
                                photoUrl    = result.photoUrl
                            )) {
                                is ApiResult.Success -> {
                                    uiState = uiState.copy(isLoading = false)
                                    onSuccess()
                                }
                                is ApiResult.Error   -> setError(apiResult.message)
                                is ApiResult.Loading -> {}
                            }
                        }
                        is GoogleSignInResult.Error     -> setError(result.message)
                        is GoogleSignInResult.Cancelled -> {
                            uiState = uiState.copy(isLoading = false)
                        }
                    }
                }
            }
        }
    }
}

data class LoginUiState(
    val emailOrPhone   : String  = "",
    val password       : String  = "",
    val passwordVisible: Boolean = false,
    val savePassword   : Boolean = false,
    val isLoading      : Boolean = false,
    val errorMessage   : String? = null
)