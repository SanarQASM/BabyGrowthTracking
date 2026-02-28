package org.example.project.babygrowthtrackingapplication.data.auth

import androidx.activity.ComponentActivity
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import org.example.project.babygrowthtrackingapplication.R

actual class SocialAuthManager {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private var currentActivity: ComponentActivity? = null
    private var credentialManager: CredentialManager? = null

    fun initialize(activity: ComponentActivity) {
        currentActivity = activity
        credentialManager = CredentialManager.create(activity)
    }

    actual suspend fun signInWithGoogle(onResult: (GoogleSignInResult) -> Unit) {
        val activity = currentActivity
        val manager = credentialManager

        if (activity == null || manager == null) {
            onResult(GoogleSignInResult.Error("Google Sign-In not initialized. Call initialize() first."))
            return
        }

        try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(activity.getString(R.string.google_web_client_id))
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = manager.getCredential(request = request, context = activity)
            val credential = result.credential

            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken

                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = firebaseAuth.signInWithCredential(firebaseCredential).await()
                val user = authResult.user

                if (user != null) {
                    onResult(
                        GoogleSignInResult.Success(
                            idToken = idToken,
                            email = user.email ?: "",
                            displayName = user.displayName ?: "",
                            photoUrl = user.photoUrl?.toString()
                        )
                    )
                } else {
                    onResult(GoogleSignInResult.Error("Failed to get user info"))
                }

            } else {
                onResult(GoogleSignInResult.Error("Unexpected credential type returned"))
            }

        } catch (e: GetCredentialCancellationException) {
            onResult(GoogleSignInResult.Cancelled)

        } catch (e: GetCredentialException) {
            onResult(GoogleSignInResult.Error(e.message ?: "Google sign-in failed"))

        } catch (e: Exception) {
            onResult(GoogleSignInResult.Error("Sign in failed: ${e.message}"))
        }
    }

    fun signOut() {
        firebaseAuth.signOut()
    }

    fun cleanup() {
        currentActivity = null
        credentialManager = null
    }
}