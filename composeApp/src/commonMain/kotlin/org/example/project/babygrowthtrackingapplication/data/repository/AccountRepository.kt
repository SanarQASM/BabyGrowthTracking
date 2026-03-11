package org.example.project.babygrowthtrackingapplication.data.repository

import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.data.PreferencesManager
import org.example.project.babygrowthtrackingapplication.data.network.*

class AccountRepository(
    private val apiService         : ApiService,
    private val preferencesManager : PreferencesManager
) {

    // ─── Register ─────────────────────────────────────────────────────────────

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
            preferencesManager.setUserLoggedIn(false)
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

    // ─── Email/Password Login ─────────────────────────────────────────────────

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
            saveUserSession(result.data.user, rememberMe, needsVerification = false)

            if (rememberMe) {
                // User checked "Save Password" → store credentials and set the flag
                preferencesManager.saveLoginCredentials(email, password)
            } else {
                // User did NOT check "Save Password" → clear any previously stored
                // credentials and set the flag to false.
                // Keep only email for field pre-fill convenience on next login.
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

    // ─── Google Login ─────────────────────────────────────────────────────────

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
            // Google login has no password — mark as logged-in WITHOUT touching the
            // save_password_enabled flag so the Settings toggle stays irrelevant.
            saveUserSession(result.data.user, rememberMe = true, needsVerification = false)
            // Explicitly ensure the password flag is NOT set for Google users so
            // there is no ambiguity if they switch to email login later.
            preferencesManager.putBoolean("save_password_enabled", false)
        }

        return when (result) {
            is ApiResult.Success -> ApiResult.Success(result.data.user)
            is ApiResult.Error   -> ApiResult.Error(result.message)
            is ApiResult.Loading -> ApiResult.Loading
        }
    }

    // ─── Account Verification ─────────────────────────────────────────────────

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

    // ─── Forgot Password ──────────────────────────────────────────────────────

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

    // ─── Session ──────────────────────────────────────────────────────────────

    private fun saveToken(token: String) {
        preferencesManager.saveAuthToken(token)
        preferencesManager.putString("auth_token", token)
    }

    private fun saveUserSession(
        user             : UserResponse,
        rememberMe       : Boolean = false,
        needsVerification: Boolean = false
    ) {
        val allowAutoLogin = rememberMe && !needsVerification
        preferencesManager.setUserLoggedIn(allowAutoLogin)
        preferencesManager.putBoolean("needs_verification", needsVerification)
        preferencesManager.saveUserId(user.userId)
        preferencesManager.saveUserEmail(user.email)
        preferencesManager.saveUserName(user.fullName)
        user.phone?.let           { preferencesManager.saveUserPhone(it) }
        user.profileImageUrl?.let { preferencesManager.putString("user_profile_image", it) }
    }

    // ─── Getters ──────────────────────────────────────────────────────────────

    /**
     * Returns true when the user has an active session.
     *
     * For EMAIL login:  requires is_logged_in=true + valid token + save_password_enabled=true
     * For GOOGLE login: requires is_logged_in=true + valid token only
     *                   (no password flag — Google users are always "remembered" by token)
     */
    fun isLoggedIn(): Boolean {
        val flagSet    = preferencesManager.getBoolean("is_logged_in", false)
        val tokenValid = preferencesManager.isTokenValid()
        if (!flagSet || !tokenValid) return false

        // Google users bypass the save-password gate
        val isGoogleUser = preferencesManager.getString("auth_provider") == "google"
        if (isGoogleUser) return true

        // Email users only auto-login if they checked "Save Password" at login
        return preferencesManager.isSavePasswordEnabled()
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

    fun getSavedEmailOrPhone()  = preferencesManager.getSavedEmailOrPhone()
    fun getSavedPassword()      = preferencesManager.getSavedPassword()
    fun isSavePasswordEnabled() = preferencesManager.isSavePasswordEnabled()

    /** Returns true only for email/password login sessions (not Google). */
    fun isEmailLogin() = preferencesManager.getString("auth_provider") == "email"

    fun logout() {
        preferencesManager.setUserLoggedIn(false)
        preferencesManager.putBoolean("needs_verification", false)
        preferencesManager.putBoolean("account_verified", false)
        preferencesManager.clearAuthToken()
        preferencesManager.remove("auth_provider")
        // Intentionally keep saved credentials so login screen can pre-fill email
    }

    fun logoutAndClearCredentials() {
        logout()
        preferencesManager.clearLoginCredentials()
    }

    // ─── Validation ───────────────────────────────────────────────────────────

    private fun validateRegistration(fullName: String, email: String, password: String) =
        fullName.isNotBlank() && email.isNotBlank() && email.contains("@") &&
                password.isNotBlank() && password.length >= 6

    private fun validateLogin(email: String, password: String) =
        email.isNotBlank() && password.isNotBlank() && password.length >= 6
}

data class PasswordResetResponse(val message: String, val success: Boolean)
data class CodeVerificationResponse(val message: String, val success: Boolean, val token: String? = null)