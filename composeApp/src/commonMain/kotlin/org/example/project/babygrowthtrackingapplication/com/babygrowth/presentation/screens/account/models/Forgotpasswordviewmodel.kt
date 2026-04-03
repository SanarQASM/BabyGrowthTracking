package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.account.models

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.example.project.babygrowthtrackingapplication.data.network.ApiResult
import org.example.project.babygrowthtrackingapplication.data.repository.AccountRepository

class ForgotPasswordViewModel(
    private val authRepository: AccountRepository
) {
    var uiState by mutableStateOf(ForgotPasswordUiState())
        private set

    fun onEmailChanged(value: String) {
        uiState = uiState.copy(email = value, errorMessage = null, successMessage = null)
    }

    // Step 1: validate format → call POST /forgot-password → navigate on success
    // onSuccess receives the email so it can be passed to EnterCode + EnterNewPassword
    fun sendResetCode(onSuccess: (email: String) -> Unit) {
        val email = uiState.email.trim()

        if (email.isBlank()) {
            uiState = uiState.copy(errorMessage = "Please enter your email address")
            return
        }
        if (!email.contains("@") || !email.contains(".")) {
            uiState = uiState.copy(errorMessage = "Please enter a valid email address")
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null, successMessage = null)

            when (val result = authRepository.requestPasswordReset(email)) {
                is ApiResult.Success -> {
                    uiState = uiState.copy(
                        isLoading      = false,
                        successMessage = "A reset code has been sent to $email"
                    )
                    onSuccess(email)
                }
                is ApiResult.Error -> {
                    uiState = uiState.copy(isLoading = false, errorMessage = result.message)
                }
                is ApiResult.Loading -> {}
            }
        }
    }

    fun clearError()   { uiState = uiState.copy(errorMessage = null) }
    fun clearSuccess() { uiState = uiState.copy(successMessage = null) }
}

data class ForgotPasswordUiState(
    val email          : String  = "",
    val isLoading      : Boolean = false,
    val errorMessage   : String? = null,
    val successMessage : String? = null
)