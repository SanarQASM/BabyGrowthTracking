package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.account.models

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.example.project.babygrowthtrackingapplication.data.network.ApiResult
import org.example.project.babygrowthtrackingapplication.data.repository.AccountRepository

class EnterCodeViewModel(
    private val accountRepository : AccountRepository,
    private val email             : String   // forwarded from ForgotPasswordScreen
) {
    var uiState by mutableStateOf(EnterCodeUiState())
        private set

    fun onCodeChanged(value: String) {
        val filtered = value.filter { it.isDigit() }.take(6)
        uiState = uiState.copy(code = filtered, errorMessage = null)
    }

    // Step 2: POST /verify-reset-code → navigate to EnterNewPassword on success
    // Passes (email, code) forward so Step 3 can send them to /reset-password
    fun verifyCode(onSuccess: (email: String, code: String) -> Unit) {
        if (uiState.code.length != 6) {
            uiState = uiState.copy(errorMessage = "Please enter the complete 6-digit code")
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)

            when (val result = accountRepository.verifyResetCode(email = email, code = uiState.code)) {
                is ApiResult.Success -> {
                    uiState = uiState.copy(isLoading = false)
                    onSuccess(email, uiState.code)
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

data class EnterCodeUiState(
    val code         : String  = "",
    val isLoading    : Boolean = false,
    val errorMessage : String? = null
)