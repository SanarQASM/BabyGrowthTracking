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

class SignupViewModel(
    private val repository: AccountRepository,
    private val socialAuthManager: SocialAuthManager,
    private val socialLoginHelper: SocialLoginHelper
) {
    var uiState by mutableStateOf(SignupUiState())
        private set

    fun onFullNameChanged(value: String) {
        uiState = uiState.copy(fullName = value, errorMessage = null)
    }

    fun onEmailChanged(value: String) {
        uiState = uiState.copy(email = value, errorMessage = null)
    }

    fun onPasswordChanged(value: String) {
        uiState = uiState.copy(password = value, errorMessage = null)
    }

    fun onConfirmPasswordChanged(value: String) {
        uiState = uiState.copy(confirmPassword = value, errorMessage = null)
    }

    fun onPhoneChanged(value: String) {
        uiState = uiState.copy(phone = value, errorMessage = null)
    }

    fun onCityChanged(value: String) {
        uiState = uiState.copy(city = value, errorMessage = null)
    }

    fun onAddressChanged(value: String) {
        uiState = uiState.copy(address = value, errorMessage = null)
    }

    fun onPasswordVisibilityToggled() {
        uiState = uiState.copy(passwordVisible = !uiState.passwordVisible)
    }

    fun onConfirmPasswordVisibilityToggled() {
        uiState = uiState.copy(confirmPasswordVisible = !uiState.confirmPasswordVisible)
    }

    fun setError(message: String) {
        uiState = uiState.copy(errorMessage = message, isLoading = false)
    }

    fun clearError() {
        uiState = uiState.copy(errorMessage = null)
    }

    // ─── Traditional Signup (email / phone) ─────────────────────────────────
    // On success → caller navigates to VerifyAccount

    private fun validateSignup(): String? {
        return when {
            uiState.fullName.isBlank() -> "Please enter your full name"
            uiState.email.isBlank() -> "Please enter your email"
            !uiState.email.contains("@") -> "Please enter a valid email"
            uiState.password.isBlank() -> "Please enter a password"
            uiState.password.length < 6 -> "Password must be at least 6 characters"
            uiState.confirmPassword.isBlank() -> "Please confirm your password"
            uiState.password != uiState.confirmPassword -> "Passwords do not match"
            else -> null
        }
    }

    fun signup(onSuccess: () -> Unit) {
        val validationError = validateSignup()
        if (validationError != null) {
            uiState = uiState.copy(errorMessage = validationError)
            return
        }

        uiState = uiState.copy(isLoading = true, errorMessage = null)

        CoroutineScope(Dispatchers.Main).launch {
            when (val result = repository.register(
                fullName = uiState.fullName,
                email    = uiState.email,
                password = uiState.password,
                phone    = uiState.phone.ifBlank { null },
                city     = uiState.city.ifBlank { null },
                address  = uiState.address.ifBlank { null }
            )) {
                is ApiResult.Success -> {
                    uiState = uiState.copy(isLoading = false)
                    onSuccess()        // → navigate to VerifyAccount
                }
                is ApiResult.Error -> {
                    uiState = uiState.copy(isLoading = false, errorMessage = result.message)
                }
                is ApiResult.Loading -> {}
            }
        }
    }

    // ─── Google Signup ───────────────────────────────────────────────────────
    // On success → caller navigates directly to Home (no verification needed)

    fun signupWithGoogle(onSuccess: () -> Unit) {
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
                                    onSuccess()   // → navigate to Home
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

data class SignupUiState(
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val phone: String = "",
    val city: String = "",
    val address: String = "",
    val passwordVisible: Boolean = false,
    val confirmPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)