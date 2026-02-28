package org.example.project.babygrowthtrackingapplication.data.auth

// Declare window as dynamic to set properties on it
private val window: dynamic = js("window")

internal actual fun checkFirebaseInitialized(): Boolean {
    // ✅ Check both the flag AND that firebase.auth is callable
    val firebaseExists = js("typeof firebase !== 'undefined'") as Boolean
    val authExists = js("typeof firebase !== 'undefined' && typeof firebase.auth === 'function'") as Boolean
    val flagReady = (js("window.__firebaseReady") as? Boolean) ?: false

    return firebaseExists && authExists && flagReady
}

internal actual fun cleanupJsCallbacks() {
    js("""
        delete window.__kotlinGoogleSignInSuccess;
        delete window.__kotlinGoogleSignInError;
        delete window.__kotlinGoogleSignInCancelled;
    """)
}

internal actual fun signInWithGoogleBridge(
    onSuccess: (String, String, String, String?) -> Unit,
    onError: (String) -> Unit,
    onCancel: () -> Unit
) {
    // ✅ Must store callbacks on window BEFORE entering js() block
    // Kotlin lambdas are NOT visible inside js() closures directly
    window.__kotlinGoogleSignInSuccess = { token: String, email: String, name: String, photo: String? ->
        onSuccess(token, email, name, photo)
    }
    window.__kotlinGoogleSignInError = { msg: String ->
        onError(msg)
    }
    window.__kotlinGoogleSignInCancelled = {
        onCancel()
    }

    js("""
        (function() {
            try {
                // ✅ Guard check inside JS too
                if (typeof firebase === 'undefined' || !window.__firebaseReady) {
                    window.__kotlinGoogleSignInError('Firebase not initialized. Please add Firebase SDK to your index.html');
                    return;
                }

                var provider = new firebase.auth.GoogleAuthProvider();
                provider.addScope('email');
                provider.addScope('profile');

                firebase.auth().signInWithPopup(provider)
                    .then(function(result) {
                        result.user.getIdToken()
                            .then(function(token) {
                                window.__kotlinGoogleSignInSuccess(
                                    token,
                                    result.user.email    || '',
                                    result.user.displayName || '',
                                    result.user.photoURL || null
                                );
                            })
                            .catch(function(err) {
                                window.__kotlinGoogleSignInError('Failed to get ID token: ' + err.message);
                            });
                    })
                    .catch(function(error) {
                        if (
                            error.code === 'auth/popup-closed-by-user' ||
                            error.code === 'auth/cancelled-popup-request'
                        ) {
                            window.__kotlinGoogleSignInCancelled();
                        } else if (error.code === 'auth/popup-blocked') {
                            window.__kotlinGoogleSignInError('Popup blocked. Please allow popups for this site.');
                        } else {
                            window.__kotlinGoogleSignInError('Google sign-in failed: ' + error.message);
                        }
                    });

            } catch(e) {
                window.__kotlinGoogleSignInError('Unexpected error: ' + e.message);
            }
        })()
    """)
}