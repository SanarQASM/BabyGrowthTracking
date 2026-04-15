// File: composeApp/src/commonMain/kotlin/org/example/project/babygrowthtrackingapplication/admin/AdminLoginViewModel.kt

package org.example.project.babygrowthtrackingapplication.admin

import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.data.PreferencesManager
import org.example.project.babygrowthtrackingapplication.data.network.ApiResult
import org.example.project.babygrowthtrackingapplication.data.network.ApiService
import org.example.project.babygrowthtrackingapplication.data.network.LoginRequest

// ─────────────────────────────────────────────────────────────────────────────
// Admin Login UI State
// ─────────────────────────────────────────────────────────────────────────────

data class AdminLoginUiState(
    val email           : String  = "",
    val password        : String  = "",
    val passwordVisible : Boolean = false,
    val isLoading       : Boolean = false,
    val errorMessage    : String? = null,
)

// ─────────────────────────────────────────────────────────────────────────────
// Admin Login ViewModel
//
// Admin login only accepts ROLE_ADMIN users.
// If a non-admin tries to log in via this screen, they are rejected with
// an error message — even if their credentials are correct.
// ─────────────────────────────────────────────────────────────────────────────

class AdminLoginViewModel(
    private val apiService        : ApiService,
    private val preferencesManager: PreferencesManager
) {
    var uiState by mutableStateOf(AdminLoginUiState())
        private set

    fun onEmailChanged(value: String) {
        uiState = uiState.copy(email = value, errorMessage = null)
    }

    fun onPasswordChanged(value: String) {
        uiState = uiState.copy(password = value, errorMessage = null)
    }

    fun onPasswordVisibilityToggled() {
        uiState = uiState.copy(passwordVisible = !uiState.passwordVisible)
    }

    fun login(onSuccess: () -> Unit) {
        val email    = uiState.email.trim()
        val password = uiState.password

        // Basic validation
        if (email.isBlank()) {
            uiState = uiState.copy(errorMessage = "Please enter your email address")
            return
        }
        if (!email.contains("@")) {
            uiState = uiState.copy(errorMessage = "Please enter a valid email address")
            return
        }
        if (password.isBlank() || password.length < 6) {
            uiState = uiState.copy(errorMessage = "Please enter your password (min 6 characters)")
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)

            when (val result = apiService.login(LoginRequest(emailOrPhone = email, password = password))) {
                is ApiResult.Success -> {
                    val authResponse = result.data

                    // ── Admin role gate ────────────────────────────────────────
                    // Reject any non-admin user even if credentials are correct.
                    val role = authResponse.user.role
                    if (!role.equals("admin", ignoreCase = true) &&
                        !role.equals("ADMIN", ignoreCase = true)) {
                        uiState = uiState.copy(
                            isLoading    = false,
                            errorMessage = "Access denied. This portal is for administrators only."
                        )
                        return@launch
                    }

                    // Save admin session
                    preferencesManager.saveAuthToken(authResponse.token)
                    preferencesManager.putString("auth_token", authResponse.token)
                    preferencesManager.putString("auth_provider", "email")
                    preferencesManager.setUserLoggedIn(true)
                    preferencesManager.saveUserId(authResponse.user.userId)
                    preferencesManager.saveUserEmail(authResponse.user.email)
                    preferencesManager.saveUserName(authResponse.user.fullName)
                    // Mark this session as an admin session so Navigation knows where to go
                    preferencesManager.putString("user_role", "ADMIN")

                    uiState = uiState.copy(isLoading = false)
                    onSuccess()
                }

                is ApiResult.Error -> {
                    uiState = uiState.copy(
                        isLoading    = false,
                        errorMessage = result.message.ifBlank { "Login failed. Please check your credentials." }
                    )
                }

                is ApiResult.Loading -> { /* handled by isLoading flag */ }
            }
        }
    }
}