package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.account

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.example.project.babygrowthtrackingapplication.data.network.ApiResult
import org.example.project.babygrowthtrackingapplication.data.repository.AccountRepository

class EnterNewPasswordViewModel(
    private val authRepository: AccountRepository
) {
    var uiState by mutableStateOf(EnterNewPasswordUiState())
        private set

    fun onNewPasswordChanged(value: String) {
        uiState = uiState.copy(newPassword = value, errorMessage = null)
    }

    fun onConfirmPasswordChanged(value: String) {
        uiState = uiState.copy(confirmPassword = value, errorMessage = null)
    }

    fun onNewPasswordVisibilityToggled() {
        uiState = uiState.copy(newPasswordVisible = !uiState.newPasswordVisible)
    }

    fun onConfirmPasswordVisibilityToggled() {
        uiState = uiState.copy(confirmPasswordVisible = !uiState.confirmPasswordVisible)
    }

    // Step 3: POST /reset-password -> navigate to LoginScreen on success
    fun resetPassword(email: String, code: String, onSuccess: () -> Unit) {
        if (uiState.newPassword.length < 6) {
            uiState = uiState.copy(errorMessage = "Password must be at least 6 characters")
            return
        }
        if (uiState.newPassword != uiState.confirmPassword) {
            uiState = uiState.copy(errorMessage = "Passwords do not match")
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)

            when (val result = authRepository.resetPasswordWithCode(
                email       = email,
                code        = code,
                newPassword = uiState.newPassword
            )) {
                is ApiResult.Success -> {
                    uiState = uiState.copy(isLoading = false, isSuccess = true)
                    kotlinx.coroutines.delay(800)
                    onSuccess()
                }
                is ApiResult.Error -> {
                    uiState = uiState.copy(isLoading = false, errorMessage = result.message)
                }
                is ApiResult.Loading -> {}
            }
        }
    }

    fun clearError() { uiState = uiState.copy(errorMessage = null) }
}

data class EnterNewPasswordUiState(
    val newPassword            : String  = "",
    val confirmPassword        : String  = "",
    val newPasswordVisible     : Boolean = false,
    val confirmPasswordVisible : Boolean = false,
    val isLoading              : Boolean = false,
    val isSuccess              : Boolean = false,
    val errorMessage           : String? = null
)