package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.account

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.example.project.babygrowthtrackingapplication.data.network.ApiResult
import org.example.project.babygrowthtrackingapplication.data.repository.AccountRepository
import org.jetbrains.compose.resources.getString
import babygrowthtrackingapplication.composeapp.generated.resources.Res
import babygrowthtrackingapplication.composeapp.generated.resources.*

class VerifyAccountViewModel(
    private val repository: AccountRepository,
    private val userEmail : String,
    private val userPhone : String
) {
    var uiState by mutableStateOf(VerifyAccountUiState())
        private set

    init {
        uiState = uiState.copy(
            email          = userEmail,
            phone          = userPhone,
            selectedMethod = VerificationMethod.EMAIL,
            isSmsAvailable = userPhone.isNotBlank()
        )
    }

    // ── Method selection ─────────────────────────────────────────────────────

    fun onVerificationMethodSelected(method: VerificationMethod) {
        CoroutineScope(Dispatchers.Main).launch {
            when (method) {
                VerificationMethod.SMS -> {
                    // SMS not available yet — show friendly "coming soon" and keep EMAIL selected
                    uiState = uiState.copy(
                        selectedMethod = VerificationMethod.EMAIL,
                        // was: "📱 SMS verification is coming soon! Please use Email verification for now."
                        errorMessage   = getString(Res.string.verify_sms_coming_soon)
                    )
                }
                VerificationMethod.EMAIL -> {
                    uiState = uiState.copy(
                        selectedMethod = VerificationMethod.EMAIL,
                        errorMessage   = null
                    )
                }
            }
        }
    }

    // ── Send code ────────────────────────────────────────────────────────────

    fun sendVerificationCode() {
        CoroutineScope(Dispatchers.Main).launch {
            // Guard: only email is supported right now
            if (uiState.selectedMethod == VerificationMethod.SMS) {
                uiState = uiState.copy(
                    // was: "📱 SMS verification is coming soon! Please use Email verification for now."
                    errorMessage = getString(Res.string.verify_sms_coming_soon)
                )
                return@launch
            }

            if (uiState.email.isBlank()) {
                uiState = uiState.copy(
                    // was: "No email address is available"
                    errorMessage = getString(Res.string.verify_error_no_email)
                )
                return@launch
            }

            uiState = uiState.copy(isSendingCode = true, errorMessage = null)

            val result = repository.sendVerificationCode(
                recipient = uiState.email,
                method    = "email"
            )

            when (result) {
                is ApiResult.Success -> {
                    uiState = uiState.copy(
                        isSendingCode = false,
                        codeSent      = true,
                        errorMessage  = null
                    )
                    startResendTimer()
                }
                is ApiResult.Error -> {
                    uiState = uiState.copy(
                        isSendingCode = false,
                        errorMessage  = result.message
                    )
                }
                is ApiResult.Loading -> { /* loading already set above */ }
            }
        }
    }

    // ── Resend timer ─────────────────────────────────────────────────────────

    private fun startResendTimer() {
        CoroutineScope(Dispatchers.Main).launch {
            uiState = uiState.copy(canResend = false, resendCountdown = 60)
            repeat(60) {
                delay(1000)
                val newCountdown = uiState.resendCountdown - 1
                uiState = uiState.copy(resendCountdown = newCountdown)
                if (newCountdown <= 0) uiState = uiState.copy(canResend = true)
            }
        }
    }

    // ── Code input ───────────────────────────────────────────────────────────

    fun onCodeChanged(code: String) {
        val filtered = code.filter { it.isDigit() }.take(6)
        uiState = uiState.copy(verificationCode = filtered, errorMessage = null)
    }

    // ── Verify ───────────────────────────────────────────────────────────────

    fun verifyCode(onSuccess: () -> Unit) {
        CoroutineScope(Dispatchers.Main).launch {
            if (uiState.verificationCode.length != 6) {
                uiState = uiState.copy(
                    // was: "Please enter the complete 6-digit code"
                    errorMessage = getString(Res.string.verify_error_incomplete_code)
                )
                return@launch
            }

            uiState = uiState.copy(isVerifying = true, errorMessage = null)

            val result = repository.verifyAccount(
                code   = uiState.verificationCode,
                method = "email"
            )

            when (result) {
                is ApiResult.Success -> {
                    uiState = uiState.copy(
                        isVerifying          = false,
                        verificationComplete = true,
                        errorMessage         = null
                    )
                    onSuccess()
                }
                is ApiResult.Error -> {
                    uiState = uiState.copy(
                        isVerifying  = false,
                        errorMessage = result.message
                    )
                }
                is ApiResult.Loading -> { /* loading already set above */ }
            }
        }
    }

    // ── Refresh contact info ─────────────────────────────────────────────────

    fun refreshContactInfo(email: String, phone: String) {
        val emailChanged = email != uiState.email
        val phoneChanged = phone != uiState.phone
        if (!emailChanged && !phoneChanged) return

        if (uiState.codeSent) {
            uiState = uiState.copy(
                email          = email,
                phone          = phone,
                isSmsAvailable = phone.isNotBlank()
            )
        } else {
            uiState = uiState.copy(
                email          = email,
                phone          = phone,
                isSmsAvailable = phone.isNotBlank(),
                selectedMethod = VerificationMethod.EMAIL,
                errorMessage   = null
            )
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    fun clearError() {
        uiState = uiState.copy(errorMessage = null)
    }

    fun resendCode() {
        if (uiState.canResend) {
            uiState = uiState.copy(verificationCode = "")
            sendVerificationCode()
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────

enum class VerificationMethod { EMAIL, SMS }

data class VerifyAccountUiState(
    val email                : String              = "",
    val phone                : String              = "",
    val isSmsAvailable       : Boolean             = false,
    val selectedMethod       : VerificationMethod? = VerificationMethod.EMAIL,
    val verificationCode     : String              = "",
    val isSendingCode        : Boolean             = false,
    val codeSent             : Boolean             = false,
    val isVerifying          : Boolean             = false,
    val verificationComplete : Boolean             = false,
    val canResend            : Boolean             = true,
    val resendCountdown      : Int                 = 0,
    val errorMessage         : String?             = null
)