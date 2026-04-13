package com.example.backend_side

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.*
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import java.io.FileInputStream

private val logger = KotlinLogging.logger {}

// ─────────────────────────────────────────────────────────────────────────────
// FCMService — wraps Firebase Admin SDK
// ─────────────────────────────────────────────────────────────────────────────

@Service
class FCMService(
    @Value("\${firebase.service-account-path}") private val serviceAccountPath: String
) {

    // ── Initialize Firebase Admin SDK on startup ───────────────────────────────

    @EventListener(ApplicationReadyEvent::class)
    fun initializeFirebase() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                val serviceAccount = javaClass.classLoader
                    .getResourceAsStream(serviceAccountPath)
                    ?: throw IllegalStateException("File not found: $serviceAccountPath")

                val options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build()
                FirebaseApp.initializeApp(options)
                logger.info { "Firebase Admin SDK initialized successfully" }
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to initialize Firebase Admin SDK" }
        }
    }

    // ── Send to a single FCM token ─────────────────────────────────────────────

    fun sendToDevice(
        fcmToken     : String,
        title        : String,
        body         : String,
        data         : Map<String, String> = emptyMap(),
        imageUrl     : String? = null,
        priority     : String = "MEDIUM"
    ): Boolean {
        return try {
            val androidConfig = AndroidConfig.builder()
                .setPriority(
                    if (priority in listOf("URGENT", "HIGH"))
                        AndroidConfig.Priority.HIGH
                    else
                        AndroidConfig.Priority.NORMAL
                )
                .setNotification(
                    AndroidNotification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .apply { imageUrl?.let { setImage(it) } }
                        .setChannelId(mapPriorityToChannel(priority, data["category"]))
                        .build()
                )
                .build()

            val apnsConfig = ApnsConfig.builder()
                .setAps(
                    Aps.builder()
                        .setAlert(ApsAlert.builder().setTitle(title).setBody(body).build())
                        .setSound("default")
                        .setBadge(1)
                        .build()
                )
                .build()

            val message = Message.builder()
                .setToken(fcmToken)
                .setNotification(
                    Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .apply { imageUrl?.let { setImage(it) } }
                        .build()
                )
                .setAndroidConfig(androidConfig)
                .setApnsConfig(apnsConfig)
                .putAllData(data)
                .build()

            FirebaseMessaging.getInstance().send(message)
            logger.info { "FCM message sent to token: ${fcmToken.take(20)}..." }
            true
        } catch (e: FirebaseMessagingException) {
            logger.error(e) { "FCM send failed for token: ${fcmToken.take(20)}... — ${e.messagingErrorCode}" }
            false
        } catch (e: Exception) {
            logger.error(e) { "Unexpected error sending FCM" }
            false
        }
    }

    // ── Send to multiple tokens (batch) ───────────────────────────────────────

    fun sendToMultipleDevices(
        fcmTokens    : List<String>,
        title        : String,
        body         : String,
        data         : Map<String, String> = emptyMap(),
        imageUrl     : String? = null
    ): Int {
        if (fcmTokens.isEmpty()) return 0
        return try {
            val multicastMessage = MulticastMessage.builder()
                .addAllTokens(fcmTokens.take(500)) // FCM max 500 per batch
                .setNotification(
                    Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .apply { imageUrl?.let { setImage(it) } }
                        .build()
                )
                .putAllData(data)
                .build()

            val response = FirebaseMessaging.getInstance().sendEachForMulticast(multicastMessage)
            logger.info { "Batch FCM: ${response.successCount} sent, ${response.failureCount} failed" }
            response.successCount
        } catch (e: Exception) {
            logger.error(e) { "Batch FCM failed" }
            0
        }
    }

    private fun mapPriorityToChannel(priority: String?, category: String?): String = when (priority) {
        "URGENT" -> "baby_urgent"
        else     -> when (category) {
            "VACCINATION"  -> "baby_vaccination"
            "GROWTH"       -> "baby_growth"
            "APPOINTMENT"  -> "baby_appointment"
            "HEALTH",
            "DEVELOPMENT"  -> "baby_health"
            else           -> "baby_general"
        }
    }
}