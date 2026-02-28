package org.example.project.babygrowthtrackingapplication.data.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.awt.Desktop
import java.net.URI
import java.net.ServerSocket
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.coroutines.resume

/**
 * Desktop (JVM) Implementation of Social Authentication
 *
 * Uses OAuth 2.0 flow with local redirect server
 * Opens browser for authentication and captures callback
 *
 * NOTE: Desktop OAuth requires a backend server to exchange authorization codes
 * for access tokens securely. This implementation captures the auth code but
 * requires additional backend implementation.
 */
actual class SocialAuthManager {

    private var googleClientId: String? = null
    private var googleClientSecret: String? = null
    private var facebookAppId: String? = null
    private var facebookAppSecret: String? = null

    private val redirectUri = "http://localhost:8080/callback"
    private val callbackPort = 8080

    /**
     * Initialize with OAuth client IDs and secrets
     */
    fun initialize(
        googleClientId: String? = null,
        googleClientSecret: String? = null,
        facebookAppSecret: String? = null
    ) {
        this.googleClientId = googleClientId
        this.googleClientSecret = googleClientSecret
        this.facebookAppSecret = facebookAppSecret
    }

    actual suspend fun signInWithGoogle(
        onResult: (GoogleSignInResult) -> Unit
    ) {
        suspendCancellableCoroutine { continuation ->
            val clientId = googleClientId

            if (clientId == null) {
                onResult(GoogleSignInResult.Error(
                    "Google Client ID not set. Call initialize() first."
                ))
                continuation.resume(Unit)
                return@suspendCancellableCoroutine
            }

            try {
                // Google OAuth 2.0 authorization URL
                val authUrl = buildGoogleAuthUrl(clientId)

                // Start local server to receive callback
                kotlinx.coroutines.GlobalScope.launch(Dispatchers.IO) {
                    startLocalServerForCallback { code, error ->
                        when {
                            error != null -> {
                                onResult(GoogleSignInResult.Error(error))
                            }
                            code != null -> {
                                onResult(GoogleSignInResult.Error(
                                    "Desktop Google Sign-In requires backend implementation.\n" +
                                            "Authorization code received: $code\n\n" +
                                            "Next steps:\n" +
                                            "1. Send this code to your backend server\n" +
                                            "2. Exchange code for access token using client secret\n" +
                                            "3. Use access token to get user info\n" +
                                            "4. Return user data to the app"
                                ))
                            }
                            else -> {
                                onResult(GoogleSignInResult.Cancelled)
                            }
                        }

                        if (continuation.context.isActive) {
                            continuation.resume(Unit)
                        }
                    }
                }

                // Open browser for authentication
                openBrowser(authUrl)

            } catch (e: Exception) {
                onResult(GoogleSignInResult.Error("Error: ${e.message}"))
                if (continuation.context.isActive) {
                    continuation.resume(Unit)
                }
            }
        }
    }

    private fun buildGoogleAuthUrl(clientId: String): String {
        val scope = "openid email profile"
        val responseType = "code"
        val state = generateRandomState()
        val accessType = "offline"
        val prompt = "consent"

        return "https://accounts.google.com/o/oauth2/v2/auth?" +
                "client_id=$clientId&" +
                "redirect_uri=$redirectUri&" +
                "response_type=$responseType&" +
                "scope=$scope&" +
                "state=$state&" +
                "access_type=$accessType&" +
                "prompt=$prompt"
    }

    private fun generateRandomState(): String {
        return java.util.UUID.randomUUID().toString()
    }

    private fun openBrowser(url: String) {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(URI(url))
        } else {
            throw Exception("Desktop browser not supported on this system")
        }
    }

    private suspend fun startLocalServerForCallback(
        onCallback: (code: String?, error: String?) -> Unit
    ) = withContext(Dispatchers.IO) {
        var serverSocket: ServerSocket? = null
        try {
            serverSocket = ServerSocket(callbackPort)
            serverSocket.soTimeout = 120000

            val socket = serverSocket.accept()
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))

            val requestLine = reader.readLine()

            val params = parseQueryParams(requestLine)
            val code = params["code"]
            val errorParam = params["error"]

            val response = when {
                errorParam != null -> {
                    createHtmlResponse(
                        false,
                        "Authentication Error",
                        "Error: $errorParam"
                    )
                }
                code != null -> {
                    createHtmlResponse(
                        true,
                        "Authentication Successful!",
                        "You can close this window and return to the application."
                    )
                }
                else -> {
                    createHtmlResponse(
                        false,
                        "Authentication Failed",
                        "No authorization code received."
                    )
                }
            }

            socket.getOutputStream().write(response.toByteArray())
            socket.close()

            onCallback(code, errorParam)

        } catch (e: java.net.SocketTimeoutException) {
            onCallback(null, "Authentication timeout - please try again")
        } catch (e: Exception) {
            onCallback(null, "Server error: ${e.message}")
        } finally {
            try {
                serverSocket?.close()
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    private fun parseQueryParams(requestLine: String?): Map<String, String> {
        if (requestLine == null) return emptyMap()

        val queryStart = requestLine.indexOf("?")
        val queryEnd = requestLine.indexOf(" HTTP")

        if (queryStart == -1 || queryEnd == -1) return emptyMap()

        val queryString = requestLine.substring(queryStart + 1, queryEnd)

        return queryString.split("&")
            .mapNotNull { param ->
                val parts = param.split("=", limit = 2)
                if (parts.size == 2) {
                    parts[0] to java.net.URLDecoder.decode(parts[1], "UTF-8")
                } else null
            }
            .toMap()
    }

    private fun createHtmlResponse(success: Boolean, title: String, message: String): String {
        val statusCode = if (success) "200 OK" else "400 Bad Request"
        val color = if (success) "#4CAF50" else "#f44336"

        return """
            HTTP/1.1 $statusCode
            Content-Type: text/html; charset=UTF-8
            Connection: close
            
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>$title</title>
                <style>
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
                        display: flex;
                        justify-content: center;
                        align-items: center;
                        height: 100vh;
                        margin: 0;
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                    }
                    .container {
                        background: white;
                        padding: 40px;
                        border-radius: 10px;
                        box-shadow: 0 10px 40px rgba(0,0,0,0.2);
                        text-align: center;
                        max-width: 400px;
                    }
                    h1 {
                        color: $color;
                        margin-top: 0;
                    }
                    p {
                        color: #666;
                        line-height: 1.6;
                    }
                    .icon {
                        font-size: 48px;
                        margin-bottom: 20px;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="icon">${if (success) "✓" else "✗"}</div>
                    <h1>$title</h1>
                    <p>$message</p>
                </div>
                <script>
                    setTimeout(function() {
                        window.close();
                    }, 3000);
                </script>
            </body>
            </html>
        """.trimIndent()
    }

    fun cleanup() {
        googleClientId = null
        googleClientSecret = null
        facebookAppId = null
        facebookAppSecret = null
    }
}