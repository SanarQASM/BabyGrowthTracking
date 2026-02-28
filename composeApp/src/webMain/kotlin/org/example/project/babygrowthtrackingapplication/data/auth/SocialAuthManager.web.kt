package org.example.project.babygrowthtrackingapplication.data.auth

import kotlinx.coroutines.isActive
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

internal expect fun checkFirebaseInitialized(): Boolean
internal expect fun cleanupJsCallbacks()
internal expect fun signInWithGoogleBridge(
    onSuccess: (idToken: String, email: String, displayName: String, photoUrl: String?) -> Unit,
    onError: (String) -> Unit,
    onCancel: () -> Unit
)

actual class SocialAuthManager {

    private var firebaseInitialized = false
    private var googleClientId: String? = null

    fun initialize(googleClientId: String? = null) {
        this.googleClientId = googleClientId
        firebaseInitialized = checkFirebaseInitialized()
    }

    actual suspend fun signInWithGoogle(onResult: (GoogleSignInResult) -> Unit) {
        suspendCancellableCoroutine { continuation ->
            if (!firebaseInitialized) {
                onResult(GoogleSignInResult.Error("Firebase not initialized. Please add Firebase SDK to your index.html"))
                if (continuation.context.isActive) continuation.resume(Unit)
                return@suspendCancellableCoroutine
            }
            try {
                signInWithGoogleBridge(
                    onSuccess = { idToken, email, displayName, photoUrl ->
                        onResult(GoogleSignInResult.Success(idToken, email, displayName, photoUrl))
                        if (continuation.context.isActive) continuation.resume(Unit)
                    },
                    onError = { msg ->
                        onResult(GoogleSignInResult.Error(msg))
                        if (continuation.context.isActive) continuation.resume(Unit)
                    },
                    onCancel = {
                        onResult(GoogleSignInResult.Cancelled)
                        if (continuation.context.isActive) continuation.resume(Unit)
                    }
                )
            } catch (e: Exception) {
                onResult(GoogleSignInResult.Error("Error: ${e.message}"))
                if (continuation.context.isActive) continuation.resume(Unit)
            }
        }
    }
}