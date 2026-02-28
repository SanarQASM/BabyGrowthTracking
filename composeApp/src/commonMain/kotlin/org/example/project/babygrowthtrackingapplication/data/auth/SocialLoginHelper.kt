package org.example.project.babygrowthtrackingapplication.data.auth

import org.example.project.babygrowthtrackingapplication.data.network.ApiResult
import org.example.project.babygrowthtrackingapplication.data.network.UserResponse
import org.example.project.babygrowthtrackingapplication.data.repository.AccountRepository

/**
 * Social Login Helper
 * Provides platform-specific social login implementations
 *
 * Usage:
 * - Call appropriate login method with OAuth tokens
 * - Handle the ApiResult response
 * - Navigate to appropriate screen based on result
 */
class SocialLoginHelper(
    private val repository: AccountRepository
) {

    /**
     * Handle Google Sign In
     *
     * Platform-specific implementation:
     * - Android: Use Google Sign-In SDK
     * - iOS: Use Google Sign-In for iOS
     * - Web: Use Firebase Auth or Google Identity Services
     */
    suspend fun handleGoogleSignIn(
        idToken: String,
        email: String,
        displayName: String,
        photoUrl: String?
    ): ApiResult<UserResponse> {
        return repository.loginWithGoogle(
            idToken = idToken,
            email = email,
            displayName = displayName,
            photoUrl = photoUrl
        )
    }
}

/**
 * Platform-specific expect/actual implementations
 *
 * Create these files in your platform-specific source sets:
 *
 * androidMain/kotlin/.../data/auth/SocialAuthManager.android.kt
 * iosMain/kotlin/.../data/auth/SocialAuthManager.ios.kt
 * webMain/kotlin/.../data/auth/SocialAuthManager.web.kt
 * desktopMain/kotlin/.../data/auth/SocialAuthManager.desktop.kt
 */
expect class SocialAuthManager() {
    suspend fun signInWithGoogle(onResult: (GoogleSignInResult) -> Unit)
}

// Result classes for social login
sealed class GoogleSignInResult {
    data class Success(
        val idToken: String,
        val email: String,
        val displayName: String,
        val photoUrl: String?
    ) : GoogleSignInResult()

    data class Error(val message: String) : GoogleSignInResult()
    object Cancelled : GoogleSignInResult()
}
