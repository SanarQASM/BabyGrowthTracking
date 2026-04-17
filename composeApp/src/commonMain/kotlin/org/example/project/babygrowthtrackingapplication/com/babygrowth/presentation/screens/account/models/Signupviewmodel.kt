// composeApp/src/commonMain/kotlin/org/example/project/babygrowthtrackingapplication/com/babygrowth/presentation/screens/account/models/Signupviewmodel.kt
package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.account.models

import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.example.project.babygrowthtrackingapplication.data.network.ApiResult
import org.example.project.babygrowthtrackingapplication.data.repository.AccountRepository
import org.example.project.babygrowthtrackingapplication.data.auth.SocialAuthManager
import org.example.project.babygrowthtrackingapplication.data.auth.SocialLoginHelper
import org.example.project.babygrowthtrackingapplication.data.auth.GoogleSignInResult

/**
 * ViewModel for the complete signup flow.
 *
 * CORRECT 3-STEP FLOW — user is NEVER written to the DB until step 3:
 *
 *  SignupScreen  ──[tap Sign Up]──▶  Step 1: preRegister()
 *                                      backend stores data in RAM, sends OTP
 *                                      NO DB row created
 *                                    navigate to SignupOtpScreen(email)
 *
 *  SignupOtpScreen  ──[enter code]──▶  Step 2: verifyOtp()
 *                                        backend validates OTP
 *                                        OTP removed, still NO DB row
 *                                      Step 3: completeRegistration()
 *                                        DB row created (isActive=true)
 *                                      navigate to Home
 *
 * If the user leaves after step 1 or 2, NO database row is ever created.
 */
class SignupViewModel(
    private val repository       : AccountRepository,
    private val socialAuthManager: SocialAuthManager,
    private val socialLoginHelper: SocialLoginHelper
) {
    var uiState by mutableStateOf(SignupUiState())
        private set

    // ── Field handlers ────────────────────────────────────────────────────────

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

    fun onPhoneChanged(raw: String) {
        val normalised = IraqiPhoneValidator.normalise(raw)
        val error      = if (normalised.isNotBlank()) IraqiPhoneValidator.validate(normalised) else null
        uiState = uiState.copy(phone = normalised, phoneError = error, errorMessage = null)
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

    fun onOtpChanged(value: String) {
        val digits = value.filter { it.isDigit() }.take(6)
        uiState = uiState.copy(otpCode = digits, errorMessage = null)
    }

    // ── Validation ────────────────────────────────────────────────────────────

    private fun validateFields(): String? = when {
        uiState.fullName.isBlank()                  -> "Please enter your full name"
        uiState.email.isBlank()                     -> "Please enter your email"
        !uiState.email.contains("@")                -> "Please enter a valid email address"
        uiState.password.isBlank()                  -> "Please enter a password"
        uiState.password.length < 6                 -> "Password must be at least 6 characters"
        uiState.confirmPassword.isBlank()           -> "Please confirm your password"
        uiState.password != uiState.confirmPassword -> "Passwords do not match"
        uiState.phone.isNotBlank() &&
                IraqiPhoneValidator.validate(uiState.phone) != null ->
            "Please enter a valid Iraqi phone number"
        else -> null
    }

    // ─────────────────────────────────────────────────────────────────────────
    // STEP 1 — pre-register
    // Sends OTP. No DB row created.
    // Navigates to OTP screen on success.
    // ─────────────────────────────────────────────────────────────────────────

    fun preRegister(onOtpRequired: (email: String) -> Unit) {
        val error = validateFields()
        if (error != null) {
            uiState = uiState.copy(errorMessage = error)
            return
        }

        uiState = uiState.copy(isLoading = true, errorMessage = null)

        CoroutineScope(Dispatchers.Main).launch {
            when (val result = repository.preRegister(
                fullName = uiState.fullName,
                email    = uiState.email,
                password = uiState.password,
                phone    = uiState.phone.ifBlank { null },
                city     = uiState.city.ifBlank { null },
                address  = uiState.address.ifBlank { null }
            )) {
                is ApiResult.Success -> {
                    uiState = uiState.copy(
                        isLoading       = false,
                        otpSent         = true,
                        registeredEmail = uiState.email
                    )
                    startResendTimer()
                    onOtpRequired(uiState.email)
                }
                is ApiResult.Error   -> uiState = uiState.copy(isLoading = false, errorMessage = result.message)
                is ApiResult.Loading -> {}
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // STEP 2 → 3 — verify OTP then immediately complete registration
    // ─────────────────────────────────────────────────────────────────────────

    fun verifyOtpAndComplete(onSuccess: () -> Unit) {
        if (uiState.otpCode.length != 6) {
            uiState = uiState.copy(errorMessage = "Please enter the complete 6-digit code")
            return
        }

        uiState = uiState.copy(isVerifying = true, errorMessage = null)

        CoroutineScope(Dispatchers.Main).launch {
            // Step 2: verify OTP
            when (val verifyResult = repository.verifySignupCode(
                email = uiState.registeredEmail,
                code  = uiState.otpCode
            )) {
                is ApiResult.Success -> {
                    uiState = uiState.copy(otpVerified = true)

                    // Step 3: create user in DB — only reached if OTP was correct
                    when (val regResult = repository.completeRegistration(uiState.registeredEmail)) {
                        is ApiResult.Success -> {
                            uiState = uiState.copy(
                                isVerifying          = false,
                                isLoading            = false,
                                registrationComplete = true
                            )
                            onSuccess()
                        }
                        is ApiResult.Error   -> uiState = uiState.copy(
                            isVerifying  = false,
                            isLoading    = false,
                            errorMessage = regResult.message
                        )
                        is ApiResult.Loading -> {}
                    }
                }
                is ApiResult.Error   -> uiState = uiState.copy(isVerifying = false, errorMessage = verifyResult.message)
                is ApiResult.Loading -> {}
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // RESEND OTP
    // ─────────────────────────────────────────────────────────────────────────

    fun resendOtp() {
        if (!uiState.canResend) return
        uiState = uiState.copy(isLoading = true, errorMessage = null)

        CoroutineScope(Dispatchers.Main).launch {
            when (val result = repository.resendSignupCode(uiState.registeredEmail)) {
                is ApiResult.Success -> {
                    uiState = uiState.copy(isLoading = false)
                    startResendTimer()
                }
                is ApiResult.Error   -> uiState = uiState.copy(isLoading = false, errorMessage = result.message)
                is ApiResult.Loading -> {}
            }
        }
    }

    fun startResendTimer() {
        CoroutineScope(Dispatchers.Main).launch {
            uiState = uiState.copy(canResend = false, resendCountdown = 60)
            repeat(60) {
                delay(1000)
                val remaining = uiState.resendCountdown - 1
                uiState = uiState.copy(resendCountdown = remaining)
                if (remaining <= 0) uiState = uiState.copy(canResend = true)
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GOOGLE SIGNUP — Google already verifies identity, no OTP needed
    // ─────────────────────────────────────────────────────────────────────────

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
                                    onSuccess()
                                }
                                is ApiResult.Error   -> setError(apiResult.message)
                                is ApiResult.Loading -> {}
                            }
                        }
                        is GoogleSignInResult.Error     -> setError(result.message)
                        is GoogleSignInResult.Cancelled -> uiState = uiState.copy(isLoading = false)
                    }
                }
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    fun setError(message: String) {
        uiState = uiState.copy(errorMessage = message, isLoading = false, isVerifying = false)
    }

    fun clearError() {
        uiState = uiState.copy(errorMessage = null)
    }

    fun getPendingEmail(): String = uiState.registeredEmail
}

// ─────────────────────────────────────────────────────────────────────────────
// UI State
// ─────────────────────────────────────────────────────────────────────────────

data class SignupUiState(
    // Form fields
    val fullName              : String  = "",
    val email                 : String  = "",
    val password              : String  = "",
    val confirmPassword       : String  = "",
    val phone                 : String  = "",
    val phoneError            : String? = null,
    val city                  : String  = "",
    val address               : String  = "",
    val passwordVisible       : Boolean = false,
    val confirmPasswordVisible: Boolean = false,

    // OTP step
    val otpCode             : String  = "",
    val otpSent             : Boolean = false,
    val otpVerified         : Boolean = false,
    val registeredEmail     : String  = "",
    val isVerifying         : Boolean = false,
    val canResend           : Boolean = true,
    val resendCountdown     : Int     = 0,

    // Final
    val registrationComplete: Boolean = false,
    val isLoading           : Boolean = false,
    val errorMessage        : String? = null
)