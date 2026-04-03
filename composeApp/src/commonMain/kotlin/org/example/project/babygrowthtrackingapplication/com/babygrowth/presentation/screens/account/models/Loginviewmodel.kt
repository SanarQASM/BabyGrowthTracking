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

    /**
     * Pre-fill the login form with saved credentials if the user previously
     * checked "Save Password".  If only the email was saved (rememberMe = false
     * on last login) we still pre-fill the email field as a convenience.
     */
    private fun loadSavedCredentials() {
        val saveEnabled   = authRepository.isSavePasswordEnabled()
        val savedEmail    = authRepository.getSavedEmailOrPhone()
            ?: authRepository.getSavedEmail()   // fallback: plain email saved without password
        val savedPassword = if (saveEnabled) authRepository.getSavedPassword() else null

        if (savedEmail != null) {
            uiState = uiState.copy(
                emailOrPhone = savedEmail,
                password     = savedPassword ?: "",
                savePassword = saveEnabled
            )
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