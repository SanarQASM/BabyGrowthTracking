package org.example.project.babygrowthtrackingapplication.notifications

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ─────────────────────────────────────────────────────────────────────────────
// Enums
// ─────────────────────────────────────────────────────────────────────────────

enum class NotificationCategory {
    VACCINATION, GROWTH, APPOINTMENT, HEALTH, DEVELOPMENT, BABY_PROFILE, MEMORIES, ACCOUNT, SYSTEM, GENERAL;

    companion object {
        fun fromString(s: String): NotificationCategory =
            entries.firstOrNull { it.name.equals(s, ignoreCase = true) } ?: GENERAL
    }
}

enum class NotificationPriorityLevel {
    URGENT, HIGH, MEDIUM, LOW;

    companion object {
        fun fromString(s: String): NotificationPriorityLevel =
            entries.firstOrNull { it.name.equals(s, ignoreCase = true) } ?: MEDIUM
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// AppNotification — matches AppNotificationDto from backend
//
// FIELDS:
//   body     — the backend sends "body" (mapped from Notification.message)
//   category — the backend sends a String like "VACCINATION", "GROWTH" etc.
//              NOT the NotificationType enum name.  We keep it as String
//              and use categoryEnum computed property for switch/when logic.
//   priority — String "URGENT" / "HIGH" / "MEDIUM" / "LOW"
// ─────────────────────────────────────────────────────────────────────────────

@Serializable
data class AppNotification(
    val notificationId   : String,
    val userId           : String,
    val babyId           : String?  = null,
    val babyName         : String?  = null,
    val title            : String,
    val body             : String,                  // "body" not "message"
    val category         : String  = "GENERAL",     // raw string from backend
    val priority         : String  = "MEDIUM",      // raw string from backend
    val isRead           : Boolean = false,
    val createdAt        : String,
    val deepLinkRoute    : String? = null,
    val imageUrl         : String? = null,
    val actionLabel      : String? = null,
    val actionRoute      : String? = null
) {
    val categoryEnum: NotificationCategory
        get() = NotificationCategory.fromString(category)

    val priorityEnum: NotificationPriorityLevel
        get() = NotificationPriorityLevel.fromString(priority)
}

// ─────────────────────────────────────────────────────────────────────────────
// NotificationListResponse — matches the backend response envelope:
// { success, notifications, unreadCount, totalCount }
// ─────────────────────────────────────────────────────────────────────────────

@Serializable
data class NotificationListResponse(
    val success       : Boolean,
    val notifications : List<AppNotification>,
    val unreadCount   : Long,
    val totalCount    : Int
)

@Serializable
data class MarkReadRequest(val notificationIds: List<String>)

@Serializable
data class RegisterFcmTokenRequest(
    val userId   : String,
    val fcmToken : String,
    val platform : String   // "android" | "ios" | "web" | "desktop"
)

// ─────────────────────────────────────────────────────────────────────────────
// NotificationPreferencesDto — matches backend NotificationPreferencesResponse
// ─────────────────────────────────────────────────────────────────────────────

@Serializable
data class NotificationPreferencesDto(
    val userId             : String,
    val vaccination        : Boolean = true,
    val growth             : Boolean = true,
    val appointment        : Boolean = true,
    val health             : Boolean = true,
    val development        : Boolean = true,
    val milestones         : Boolean = true,
    val general            : Boolean = true,
    val reminderDaysBefore : Int     = 3
)

@Serializable
data class UpdateNotificationPreferencesRequest(
    val vaccination        : Boolean? = null,
    val growth             : Boolean? = null,
    val appointment        : Boolean? = null,
    val health             : Boolean? = null,
    val development        : Boolean? = null,
    val milestones         : Boolean? = null,
    val general            : Boolean? = null,
    val reminderDaysBefore : Int?     = null
)

// ─────────────────────────────────────────────────────────────────────────────
// Deep link routes
// ─────────────────────────────────────────────────────────────────────────────

object DeepLinkRoutes {
    const val HOME            = "home"
    const val VACCINATION     = "health/vaccinations"
    const val GROWTH_CHART    = "charts"
    const val APPOINTMENTS    = "health/appointments"
    const val HEALTH_ISSUES   = "health/issues"
    const val FAMILY_HISTORY  = "settings/family-history"
    const val CHILD_ILLNESSES = "settings/child-illnesses"
    const val VISION_MOTOR    = "settings/vision-motor"
    const val HEARING_SPEECH  = "settings/hearing-speech"
    const val MEMORIES        = "memories"
    const val BABY_PROFILE    = "baby-profile"
    const val SETTINGS        = "settings"
    const val ADD_MEASUREMENT = "add-measurement"
    const val ADD_APPOINTMENT = "add-appointment"
}