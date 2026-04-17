// File: composeApp/src/commonMain/kotlin/org/example/project/babygrowthtrackingapplication/data/repository/AccountRepository.kt
//
// CHANGELOG:
//  v2 — Added 3-step signup flow to support SignupViewModel:
//       preRegister(), verifySignupCode(), resendSignupCode(), completeRegistration()
//       The original register() is kept for backward compatibility but is no longer
//       the primary signup path.
//  v1 — saveUserSession() saves user_role for admin routing in Navigation.kt

package org.example.project.babygrowthtrackingapplication.data.repository

import org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.data.PreferencesManager
import org.example.project.babygrowthtrackingapplication.data.network.*

class AccountRepository(
    private val apiService         : ApiService,
    private val preferencesManager : PreferencesManager
) {

    // ─── 3-Step Signup Flow ───────────────────────────────────────────────────

    /**
     * STEP 1 — Pre-register.
     * Sends an OTP to [email]. No database row is created yet.
     * On success, the backend stores registration data in RAM until step 3.
     */
    suspend fun preRegister(
        fullName: String,
        email   : String,
        password: String,
        phone   : String? = null,
        city    : String? = null,
        address : String? = null
    ): ApiResult<Unit> {
        if (!validateRegistration(fullName, email, password))
            return ApiResult.Error("Please fill all required fields correctly")

        return try {
            val result = apiService.preRegister(
                PreRegisterRequest(fullName, email, password, phone, city, address)
            )
            when (result) {
                is ApiResult.Success -> {
                    preferencesManager.putString("pending_signup_email", email)
                    ApiResult.Success(Unit)   // ← caller only needs Unit
                }
                is ApiResult.Error   -> ApiResult.Error(result.message)
                is ApiResult.Loading -> ApiResult.Loading
            }
        } catch (e: Exception) {
            ApiResult.Error("Failed to initiate registration: ${e.message}")
        }
    }

    /**
     * STEP 2 — Verify the OTP the user entered.
     * Still no database row. Returns success if the code is valid.
     */
    suspend fun verifySignupCode(
        email: String,
        code : String
    ): ApiResult<Unit> {
        if (email.isBlank()) return ApiResult.Error("Email is required")
        if (code.length != 6) return ApiResult.Error("Please enter the complete 6-digit code")

        return try {
            val result = apiService.verifySignupCode(VerifySignupCodeRequest(email, code))
            when (result) {
                is ApiResult.Success -> ApiResult.Success(Unit)
                is ApiResult.Error   -> ApiResult.Error(result.message)
                is ApiResult.Loading -> ApiResult.Loading
            }
        } catch (e: Exception) {
            ApiResult.Error("Failed to verify code: ${e.message}")
        }
    }

    /**
     * STEP 3 — Complete registration.
     * Only called after OTP verification succeeds.
     * This is the call that creates the user row in the database (isActive = true).
     * Also persists the session so the user is immediately logged in.
     */
    suspend fun completeRegistration(email: String): ApiResult<UserResponse> {
        if (email.isBlank()) return ApiResult.Error("Email is required")

        return try {
            val result = apiService.completeRegistration(CompleteRegistrationRequest(email))
            if (result is ApiResult.Success) {
                saveToken(result.data.token)
                preferencesManager.putString("auth_provider", "email")
                preferencesManager.putBoolean("account_verified", true)
                preferencesManager.remove("pending_signup_email")
                saveUserSession(result.data.user, rememberMe = false, needsVerification = false)
            }
            when (result) {
                is ApiResult.Success -> ApiResult.Success(result.data.user)
                is ApiResult.Error   -> ApiResult.Error(result.message)
                is ApiResult.Loading -> ApiResult.Loading
            }
        } catch (e: Exception) {
            ApiResult.Error("Failed to complete registration: ${e.message}")
        }
    }

    /**
     * Resend signup OTP — can be called from the OTP screen if the timer expires.
     */
    suspend fun resendSignupCode(email: String): ApiResult<Unit> {
        if (email.isBlank()) return ApiResult.Error("Email is required")

        return try {
            val result = apiService.resendSignupCode(ResendSignupCodeRequest(email))
            when (result) {
                is ApiResult.Success -> ApiResult.Success(Unit)
                is ApiResult.Error   -> ApiResult.Error(result.message)
                is ApiResult.Loading -> ApiResult.Loading
            }
        } catch (e: Exception) {
            ApiResult.Error("Failed to resend code: ${e.message}")
        }
    }

    // ─── Legacy Register (kept for backward compatibility) ────────────────────

    /**
     * Original single-step register. No longer the primary signup path.
     * Use preRegister() → verifySignupCode() → completeRegistration() instead.
     */
    suspend fun register(
        fullName       : String,
        email          : String,
        password       : String,
        phone          : String? = null,
        city           : String? = null,
        address        : String? = null,
        profileImageUrl: String? = null
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
                preferencesManager.saveLoginCredentials(email, password)
            } else {
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
            saveUserSession(result.data.user, rememberMe = true, needsVerification = false)
            preferencesManager.putBoolean("save_password_enabled", false)
        }

        return when (result) {
            is ApiResult.Success -> ApiResult.Success(result.data.user)
            is ApiResult.Error   -> ApiResult.Error(result.message)
            is ApiResult.Loading -> ApiResult.Loading
        }
    }

    // ─── Account Verification (post-legacy-register) ──────────────────────────

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

    /**
     * Persists user data to preferences after any successful login or registration.
     * Also saves user.role so Navigation.kt can route admins to AdminHomeScreen.
     */
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

        // Save role for admin routing in Navigation.kt
        preferencesManager.putString("user_role", user.role.uppercase())
    }

    // ─── Getters ──────────────────────────────────────────────────────────────

    fun isLoggedIn(): Boolean {
        val flagSet    = preferencesManager.getBoolean("is_logged_in", false)
        val tokenValid = preferencesManager.isTokenValid()
        if (!flagSet || !tokenValid) return false
        val isGoogleUser = preferencesManager.getString("auth_provider") == "google"
        if (isGoogleUser) return true
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
    fun getPendingSignupEmail()      = preferencesManager.getString("pending_signup_email")

    fun getSavedEmailOrPhone()  = preferencesManager.getSavedEmailOrPhone()
    fun getSavedPassword()      = preferencesManager.getSavedPassword()
    fun isSavePasswordEnabled() = preferencesManager.isSavePasswordEnabled()

    fun isEmailLogin() = preferencesManager.getString("auth_provider") == "email"

    fun isAdminUser(): Boolean {
        val role = preferencesManager.getString("user_role", "")
        return role.equals("ADMIN", ignoreCase = true)
    }

    fun logout() {
        preferencesManager.setUserLoggedIn(false)
        preferencesManager.putBoolean("needs_verification", false)
        preferencesManager.putBoolean("account_verified", false)
        preferencesManager.clearAuthToken()
        preferencesManager.remove("auth_provider")
        preferencesManager.remove("user_role")
        preferencesManager.remove("pending_signup_email")
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