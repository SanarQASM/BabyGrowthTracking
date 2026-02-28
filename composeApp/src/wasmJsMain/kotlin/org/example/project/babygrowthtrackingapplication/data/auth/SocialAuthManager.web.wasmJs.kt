package org.example.project.babygrowthtrackingapplication.data.auth

internal actual fun checkFirebaseInitialized(): Boolean = try {
    js("typeof firebase !== 'undefined' && typeof firebase.auth === 'function'") as Boolean
} catch (_: Exception) { false }

internal actual fun cleanupJsCallbacks() {
    js("""
        delete window.kotlinGoogleSignInSuccess;
        delete window.kotlinGoogleSignInError;
        delete window.kotlinGoogleSignInCancelled;
    """)
}

internal actual fun signInWithGoogleBridge(
    onSuccess: (String, String, String, String?) -> Unit,
    onError: (String) -> Unit,
    onCancel: () -> Unit
) {
    js("""
        (function() {
            try {
                var provider = new firebase.auth.GoogleAuthProvider();
                provider.addScope('email'); provider.addScope('profile');
                firebase.auth().signInWithPopup(provider)
                    .then(function(result) {
                        result.user.getIdToken().then(function(idToken) {
                            onSuccess(idToken, result.user.email||'', result.user.displayName||'', result.user.photoURL||null);
                        }).catch(function() { onError('Failed to get ID token'); });
                    })
                    .catch(function(error) {
                        if (error.code==='auth/popup-closed-by-user'||error.code==='auth/cancelled-popup-request') onCancel();
                        else if (error.code==='auth/popup-blocked') onError('Popup blocked. Please allow popups.');
                        else onError('Google sign-in failed: ' + error.message);
                    });
            } catch(e) { onError('Error: ' + e.message); }
        })();
    """)
}