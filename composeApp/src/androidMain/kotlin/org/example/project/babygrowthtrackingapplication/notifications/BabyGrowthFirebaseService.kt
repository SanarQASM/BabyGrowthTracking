package org.example.project.babygrowthtrackingapplication.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class BabyGrowthFirebaseService : FirebaseMessagingService() {

    companion object {
        const val CHANNEL_VACCINATION  = "baby_vaccination"
        const val CHANNEL_GROWTH       = "baby_growth"
        const val CHANNEL_APPOINTMENT  = "baby_appointment"
        const val CHANNEL_HEALTH       = "baby_health"
        const val CHANNEL_GENERAL      = "baby_general"
        const val CHANNEL_URGENT       = "baby_urgent"

        fun createChannels(context: Context) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
            val manager = context.getSystemService(NotificationManager::class.java)

            listOf(
                NotificationChannel(CHANNEL_URGENT,      "Urgent Alerts",       NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Critical vaccination and health alerts"
                    enableVibration(true)
                },
                NotificationChannel(CHANNEL_VACCINATION, "Vaccination Reminders", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Vaccine schedule reminders and updates"
                },
                NotificationChannel(CHANNEL_GROWTH,      "Growth Tracking",     NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = "Growth measurement reminders and alerts"
                },
                NotificationChannel(CHANNEL_APPOINTMENT, "Appointments",        NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Doctor appointment reminders"
                },
                NotificationChannel(CHANNEL_HEALTH,      "Health Alerts",       NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Health issue updates and development milestones"
                },
                NotificationChannel(CHANNEL_GENERAL,     "General",             NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = "General app notifications"
                }
            ).forEach { manager.createNotificationChannel(it) }
        }
    }

    // ── Token refreshed — re-register with backend ────────────────────────────

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Store locally and re-register with backend on next app launch
        getSharedPreferences("fcm_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("fcm_token", token)
            .putBoolean("token_needs_sync", true)
            .apply()
    }

    // ── Foreground message handling ───────────────────────────────────────────

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val data     = message.data
        val category = data["category"] ?: "GENERAL"
        val priority = data["priority"] ?: "MEDIUM"
        val route    = data["deepLinkRoute"]
        val babyName = data["babyName"]

        val channelId = when (priority) {
            "URGENT" -> CHANNEL_URGENT
            else     -> when (category) {
                "VACCINATION"  -> CHANNEL_VACCINATION
                "GROWTH"       -> CHANNEL_GROWTH
                "APPOINTMENT"  -> CHANNEL_APPOINTMENT
                "HEALTH",
                "DEVELOPMENT"  -> CHANNEL_HEALTH
                else           -> CHANNEL_GENERAL
            }
        }

        val notifPriority = when (priority) {
            "URGENT", "HIGH" -> NotificationCompat.PRIORITY_HIGH
            "LOW"            -> NotificationCompat.PRIORITY_LOW
            else             -> NotificationCompat.PRIORITY_DEFAULT
        }

        // Build deep-link intent
        val intent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            route?.let { putExtra("notification_route", it) }
        }

        val pendingIntent = PendingIntent.getActivity(
            this, System.currentTimeMillis().toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = message.notification
        val title = notification?.title ?: data["title"] ?: "Baby Growth Tracker"
        val body  = notification?.body  ?: data["body"]  ?: ""

        // Category emoji prefix
        val emojiPrefix = when (category) {
            "VACCINATION"  -> "💉 "
            "GROWTH"       -> "📏 "
            "APPOINTMENT"  -> "📅 "
            "HEALTH"       -> "❤️ "
            "DEVELOPMENT"  -> "🧠 "
            "BABY_PROFILE" -> "👶 "
            "MEMORIES"     -> "📸 "
            "ACCOUNT"      -> "🔐 "
            else           -> "🔔 "
        }

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Replace: R.drawable.ic_notification_baby
            .setContentTitle("$emojiPrefix$title")
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(notifPriority)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .apply {
                if (priority == "URGENT") {
                    setVibrate(longArrayOf(0, 500, 200, 500))
                }
                babyName?.let { setSubText(it) }
            }

        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
}