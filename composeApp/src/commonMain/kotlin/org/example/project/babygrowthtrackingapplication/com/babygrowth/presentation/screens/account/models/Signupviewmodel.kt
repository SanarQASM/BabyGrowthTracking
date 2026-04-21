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

// ─────────────────────────────────────────────────────────────────────────────
// SignupViewModel — FIXED
//
// BUGS FIXED:
//
// 1. resetForNewSignup() added:
//    When the user navigates Welcome → Signup after a FAILED previous attempt,
//    the old SignupViewModel (now hoisted) retains the previous attempt's state
//    including otpSent=true, which causes the screen to skip straight to the
//    OTP step. resetForNewSignup() lets Navigation reset the form state when
//    the user navigates back to Welcome and then returns to Signup.
//    NOTE: Navigation.kt does NOT call this automatically — it is intentionally
//    NOT called so that in-progress signups survive background/foreground cycles.
//    Call it only when the user explicitly presses "Back" from Signup to Welcome.
//
// 2. verifyOtpAndComplete() — step sequencing hardened:
//    Added explicit loading state guards between step 2 (verifySignupCode) and
//    step 3 (completeRegistration) so the UI cannot trigger a double-submit
//    if the user taps the button while step 2 is in flight.
//
// 3. startResendTimer() is now idempotent:
//    Calling startResendTimer() while a timer is already running (e.g., on
//    screen re-composition after config change) no longer launches a second
//    concurrent countdown, which previously caused the countdown to jump
//    erratically.
// ─────────────────────────────────────────────────────────────────────────────

class SignupViewModel(
    private val repository       : AccountRepository,
    private val socialAuthManager: SocialAuthManager,
    private val socialLoginHelper: SocialLoginHelper
) {
    var uiState by mutableStateOf(SignupUiState())
        private set

    // FIX 1: Track whether a resend timer is already running to prevent
    // duplicate countdowns when startResendTimer() is called more than once.
    private var resendTimerRunning = false

    // ── FIX 1: Called by Navigation when user presses Back from Signup → Welcome.
    // Resets form so a fresh signup attempt starts cleanly.
    fun resetForNewSignup() {
        resendTimerRunning = false
        uiState = SignupUiState()
    }

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
    // ─────────────────────────────────────────────────────────────────────────

    fun preRegister(onOtpRequired: (email: String) -> Unit) {
        val error = validateFields()
        if (error != null) {
            uiState = uiState.copy(errorMessage = error)
            return
        }

        // FIX 2: Guard against double-submit
        if (uiState.isLoading) return

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

        // FIX 2: Guard against double-submit while verification is in flight
        if (uiState.isVerifying || uiState.isLoading) return

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

    // FIX 3: Idempotent — guards against launching a second concurrent timer.
    fun startResendTimer() {
        if (resendTimerRunning) return
        resendTimerRunning = true

        CoroutineScope(Dispatchers.Main).launch {
            uiState = uiState.copy(canResend = false, resendCountdown = 60)
            repeat(60) {
                delay(1000)
                val remaining = uiState.resendCountdown - 1
                uiState = uiState.copy(resendCountdown = remaining)
                if (remaining <= 0) {
                    uiState = uiState.copy(canResend = true)
                }
            }
            resendTimerRunning = false
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GOOGLE SIGNUP
    // ─────────────────────────────────────────────────────────────────────────

    fun signupWithGoogle(onSuccess: () -> Unit) {
        if (uiState.isLoading) return
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