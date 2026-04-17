// File: composeApp/src/commonMain/kotlin/org/example/project/babygrowthtrackingapplication/admin/AdminLoginViewModel.kt

package org.example.project.babygrowthtrackingapplication.admin

import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.data.PreferencesManager
import org.example.project.babygrowthtrackingapplication.data.network.ApiResult
import org.example.project.babygrowthtrackingapplication.data.network.ApiService
import org.example.project.babygrowthtrackingapplication.data.network.LoginRequest

// ─────────────────────────────────────────────────────────────────────────────
// UI State
// ─────────────────────────────────────────────────────────────────────────────

data class AdminLoginUiState(
    val email: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val isLoading: Boolean = false,
    // Sentinel keys resolved to localised strings in the composable:
    //   "ERR_EMAIL_EMPTY"    → admin_login_email_error
    //   "ERR_EMAIL_INVALID"  → admin_login_email_invalid
    //   "ERR_PASSWORD_SHORT" → admin_login_password_error
    //   "ERR_ACCESS_DENIED"  → admin_access_denied
    //   Any other value      → passed through verbatim (server message)
    val errorKey: String? = null,
)

// ─────────────────────────────────────────────────────────────────────────────
// ViewModel
// ─────────────────────────────────────────────────────────────────────────────

class AdminLoginViewModel(
    private val apiService: ApiService,
    private val preferencesManager: PreferencesManager
) {
    var uiState by mutableStateOf(AdminLoginUiState())
        private set

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    fun onEmailChanged(value: String) {
        uiState = uiState.copy(email = value, errorKey = null)
    }

    fun onPasswordChanged(value: String) {
        uiState = uiState.copy(password = value, errorKey = null)
    }

    fun onPasswordVisibilityToggled() {
        uiState = uiState.copy(passwordVisible = !uiState.passwordVisible)
    }

    fun login(onSuccess: () -> Unit) {
        val email = uiState.email.trim()
        val password = uiState.password

        // ── Local validation ───────────────────────────────────────────────
        if (email.isBlank()) {
            uiState = uiState.copy(errorKey = "ERR_EMAIL_EMPTY"); return
        }
        if (!email.contains("@")) {
            uiState = uiState.copy(errorKey = "ERR_EMAIL_INVALID"); return
        }
        if (password.isBlank() || password.length < 6) {
            uiState = uiState.copy(errorKey = "ERR_PASSWORD_SHORT"); return
        }

        scope.launch {
            uiState = uiState.copy(isLoading = true, errorKey = null)

            when (val result =
                apiService.login(LoginRequest(emailOrPhone = email, password = password))) {
                is ApiResult.Success -> {
                    val authResponse = result.data
                    val role = authResponse.user.role

                    // ── Role gate ──────────────────────────────────────────
                    if (!role.equals("admin", ignoreCase = true)) {
                        uiState = uiState.copy(isLoading = false, errorKey = "ERR_ACCESS_DENIED")
                        return@launch
                    }

                    // ── Persist admin session ──────────────────────────────
                    preferencesManager.saveAuthToken(authResponse.token)
                    preferencesManager.putString("auth_token", authResponse.token)
                    preferencesManager.putString("auth_provider", "email")
                    preferencesManager.setUserLoggedIn(true)
                    preferencesManager.saveUserId(authResponse.user.userId)
                    preferencesManager.saveUserEmail(authResponse.user.email)
                    preferencesManager.saveUserName(authResponse.user.fullName)
                    preferencesManager.putString("user_role", "ADMIN")

                    uiState = uiState.copy(isLoading = false)
                    onSuccess()
                }

                is ApiResult.Error -> {
                    uiState = uiState.copy(
                        isLoading = false,
                        errorKey = result.message.ifBlank { "ERR_GENERIC" }
                    )
                }

                is ApiResult.Loading -> { /* handled by isLoading flag */
                }
            }
        }
    }

    fun onDestroy() {
        scope.cancel()
    }
}