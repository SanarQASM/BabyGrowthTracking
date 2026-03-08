package org.example.project.babygrowthtrackingapplication.data.repository

import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.data.PreferencesManager
import org.example.project.babygrowthtrackingapplication.data.network.*

class AccountRepository(
    private val apiService         : ApiService,
    private val preferencesManager : PreferencesManager
) {

    // ─── Register ──────────────────────────────────────────────────────────────

    suspend fun register(
        fullName: String, email: String, password: String,
        phone: String? = null, city: String? = null,
        address: String? = null, profileImageUrl: String? = null
    ): ApiResult<UserResponse> {
        if (!validateRegistration(fullName, email, password))
            return ApiResult.Error("Please fill all required fields correctly")

        val result = apiService.register(
            RegisterRequest(fullName, email, password, phone, city, address, profileImageUrl)
        )

        if (result is ApiResult.Success) {
            saveToken(result.data.token)
            preferencesManager.putBoolean("needs_verification", true)
            preferencesManager.setUserLoggedIn(false)   // never auto-login after register
            preferencesManager.saveUserId(result.data.user.userId)
            preferencesManager.saveUserEmail(result.data.user.email)
            preferencesManager.saveUserName(result.data.user.fullName)
            result.data.user.phone?.let { preferencesManager.saveUserPhone(it) }
        }

        return when (result) {
            is ApiResult.Success -> ApiResult.Success(result.data.user)
            is ApiResult.Error   -> ApiResult.Error(result.message)
            is ApiResult.Loading -> ApiResult.Loading
        }
    }

    // ─── Login ──────────────────────────────────────────────────────────────────

    suspend fun login(
        email     : String,
        password  : String,
        rememberMe: Boolean = false
    ): ApiResult<UserResponse> {
        if (!validateLogin(email, password))
            return ApiResult.Error("Please enter valid email/phone and password")

        val result = apiService.login(LoginRequest(emailOrPhone = email, password = password))

        if (result is ApiResult.Success) {
            saveToken(result.data.token)
            preferencesManager.putString("auth_provider", "email")

            // FIX: saveUserSession now receives rememberMe so it can decide
            //      whether to set is_logged_in = true or false.
            saveUserSession(result.data.user, rememberMe, needsVerification = false)

            if (rememberMe) {
                // Save both email/phone + password → enables full auto-login next launch
                preferencesManager.saveLoginCredentials(email, password)
            } else {
                // Clear any previously saved password and disable auto-login flag.
                // Keep email in "saved_email" only for field pre-fill convenience.
                preferencesManager.clearLoginCredentials()
                preferencesManager.putString("saved_email", email)
            }
        }

        return when (result) {
            is ApiResult.Success -> ApiResult.Success(result.data.user)
            is ApiResult.Error   -> ApiResult.Error(result.message)
            is ApiResult.Loading -> ApiResult.Loading
        }
    }

    // ─── Google Login ───────────────────────────────────────────────────────────

    suspend fun loginWithGoogle(
        idToken    : String,
        email      : String,
        displayName: String,
        photoUrl   : String?
    ): ApiResult<UserResponse> {
        val result = apiService.loginWithGoogle(
            GoogleAuthRequest(idToken, email, displayName, photoUrl)
        )

        if (result is ApiResult.Success) {
            saveToken(result.data.token)
            preferencesManager.putString("auth_provider", "google")
            // Google login is always treated as "remember me" — user authenticated
            // via Google so there is no password to remember; we set the flag so
            // subsequent launches go straight to Home.
            saveUserSession(result.data.user, rememberMe = true, needsVerification = false)
        }

        return when (result) {
            is ApiResult.Success -> ApiResult.Success(result.data.user)
            is ApiResult.Error   -> ApiResult.Error(result.message)
            is ApiResult.Loading -> ApiResult.Loading
        }
    }

    // ─── Account Verification ───────────────────────────────────────────────────

    suspend fun sendVerificationCode(
        recipient: String,
        method   : String
    ): ApiResult<VerificationResponse> =
        try {
            apiService.sendVerificationCode(SendVerificationCodeRequest(recipient, method))
        } catch (e: Exception) {
            ApiResult.Error("Failed to send verification code: ${e.message}")
        }

    suspend fun verifyAccount(
        code  : String,
        method: String
    ): ApiResult<VerificationResponse> =
        try {
            val result = apiService.verifyAccount(VerifyAccountRequest(code, method))
            if (result is ApiResult.Success)
                preferencesManager.putBoolean("account_verified", true)
            result
        } catch (e: Exception) {
            ApiResult.Error("Failed to verify account: ${e.message}")
        }

    // ─── Forgot Password ─────────────────────────────────────────────────────────

    suspend fun requestPasswordReset(email: String): ApiResult<VerificationResponse> {
        if (email.isBlank())      return ApiResult.Error("Please enter your email address")
        if (!email.contains("@")) return ApiResult.Error("Please enter a valid email address")
        return try {
            apiService.forgotPassword(ForgotPasswordRequest(email = email))
        } catch (e: Exception) {
            ApiResult.Error("Failed to send reset email: ${e.message}")
        }
    }

    suspend fun verifyResetCode(email: String, code: String): ApiResult<VerificationResponse> {
        if (email.isBlank())   return ApiResult.Error("Email is required")
        if (code.length != 6) return ApiResult.Error("Please enter the complete 6-digit code")
        return try {
            apiService.verifyResetCode(VerifyResetCodeRequest(email = email, code = code))
        } catch (e: Exception) {
            ApiResult.Error("Failed to verify code: ${e.message}")
        }
    }

    suspend fun resetPasswordWithCode(
        email      : String,
        code       : String,
        newPassword: String
    ): ApiResult<VerificationResponse> {
        if (email.isBlank())        return ApiResult.Error("Email is required")
        if (code.length != 6)       return ApiResult.Error("Invalid verification code")
        if (newPassword.length < 6) return ApiResult.Error("Password must be at least 6 characters")
        return try {
            apiService.resetPassword(
                ResetPasswordRequest(email = email, code = code, newPassword = newPassword)
            )
        } catch (e: Exception) {
            ApiResult.Error("Failed to reset password: ${e.message}")
        }
    }

    // ─── Session ─────────────────────────────────────────────────────────────────

    private fun saveToken(token: String) {
        preferencesManager.saveAuthToken(token)
        preferencesManager.putString("auth_token", token) // raw key for ApiService.getToken lambda
    }

    /**
     * Persists user profile data after a successful login/register/social-auth.
     *
     * FIX: The logged-in flag is now controlled by [rememberMe].
     *   • rememberMe = true  → is_logged_in = true  → next launch goes straight to Home
     *   • rememberMe = false → is_logged_in = false → next launch forces Login screen
     *   • needsVerification overrides both: always sets is_logged_in = false
     */
    private fun saveUserSession(
        user             : UserResponse,
        rememberMe       : Boolean = false,
        needsVerification: Boolean = false
    ) {
        // Only allow auto-login if rememberMe is true AND account is already verified.
        val allowAutoLogin = rememberMe && !needsVerification
        preferencesManager.setUserLoggedIn(allowAutoLogin)

        preferencesManager.putBoolean("needs_verification", needsVerification)
        preferencesManager.saveUserId(user.userId)
        preferencesManager.saveUserEmail(user.email)
        preferencesManager.saveUserName(user.fullName)
        user.phone?.let           { preferencesManager.saveUserPhone(it) }
        user.profileImageUrl?.let { preferencesManager.putString("user_profile_image", it) }
    }

    // ─── Getters ──────────────────────────────────────────────────────────────────

    /**
     * Returns true only when ALL THREE conditions hold:
     *  1. The is_logged_in flag was set to true (only happens when rememberMe = true)
     *  2. A valid auth token exists (not blank, not expired)
     *  3. The "Save Password" feature flag is enabled
     *
     * FIX: Added isSavePasswordEnabled() check so that even if the flag was
     * somehow left as true from an old install, the user is still sent to Login
     * if they never checked "Save Password".
     */
    fun isLoggedIn(): Boolean {
        val flagSet    = preferencesManager.getBoolean("is_logged_in", false)
        val tokenValid = preferencesManager.isTokenValid()
        val rememberMe = preferencesManager.isSavePasswordEnabled()
        return flagSet && tokenValid && rememberMe
    }

    fun needsVerification()          = preferencesManager.getBoolean("needs_verification", false)
    fun isAccountVerified()          = preferencesManager.getBoolean("account_verified", false)
    fun getCurrentUserId()           = preferencesManager.getString("user_id")
    fun getCurrentUserEmail()        = preferencesManager.getString("user_email")
    fun getCurrentUserName()         = preferencesManager.getString("user_name")
    fun getCurrentUserPhone()        = preferencesManager.getString("user_phone")
    fun getCurrentUserProfileImage() = preferencesManager.getString("user_profile_image")
    fun getSavedEmail()              = preferencesManager.getString("saved_email")
    fun getAuthProvider()            = preferencesManager.getString("auth_provider")
    fun getAuthToken()               = preferencesManager.getAuthToken()

    // ── Credential helpers forwarded from PreferencesManager ──────────────────
    fun getSavedEmailOrPhone()  = preferencesManager.getSavedEmailOrPhone()
    fun getSavedPassword()      = preferencesManager.getSavedPassword()
    fun isSavePasswordEnabled() = preferencesManager.isSavePasswordEnabled()

    /**
     * Standard logout.
     *
     * FIX: Explicitly sets is_logged_in = false via setUserLoggedIn() so the
     * next launch never bypasses the Login screen.
     * Saved credentials are preserved intentionally — if the user had "Save
     * Password" enabled, they can still log back in without re-typing.
     * Call logoutAndClearCredentials() to wipe everything.
     */
    fun logout() {
        preferencesManager.setUserLoggedIn(false)   // clears both "user_logged_in" and "is_logged_in"
        preferencesManager.putBoolean("needs_verification", false)
        preferencesManager.putBoolean("account_verified", false)
        preferencesManager.clearAuthToken()
        preferencesManager.remove("auth_provider")
        // Intentionally keep saved_email / saved credentials so the login
        // screen can pre-fill them on next visit.
    }

    /** Logout + wipe all saved credentials (e.g. user explicitly signs out and clears data). */
    fun logoutAndClearCredentials() {
        logout()
        preferencesManager.clearLoginCredentials()
    }

    // ─── Validation ───────────────────────────────────────────────────────────────

    private fun validateRegistration(fullName: String, email: String, password: String) =
        fullName.isNotBlank() && email.isNotBlank() && email.contains("@") &&
                password.isNotBlank() && password.length >= 6

    private fun validateLogin(email: String, password: String) =
        email.isNotBlank() && password.isNotBlank() && password.length >= 6
}

data class PasswordResetResponse(val message: String, val success: Boolean)
data class CodeVerificationResponse(val message: String, val success: Boolean, val token: String? = null)