// ─────────────────────────────────────────────────────────────────────────────
// FILE: composeApp/src/commonMain/kotlin/org/example/project/
//       babygrowthtrackingapplication/notifications/NotificationModels.kt
// ─────────────────────────────────────────────────────────────────────────────

package org.example.project.babygrowthtrackingapplication.notifications

import kotlinx.serialization.Serializable

// ─────────────────────────────────────────────────────────────────────────────
// Enums matching backend AppConstants / Enums.kt
// ─────────────────────────────────────────────────────────────────────────────

enum class NotificationCategory {
    VACCINATION, GROWTH, APPOINTMENT, HEALTH, DEVELOPMENT, BABY_PROFILE, MEMORIES, ACCOUNT, SYSTEM
}

enum class NotificationPriorityLevel { URGENT, HIGH, MEDIUM, LOW }

// ─────────────────────────────────────────────────────────────────────────────
// Data models
// ─────────────────────────────────────────────────────────────────────────────

@Serializable
data class AppNotification(
    val notificationId   : String,
    val userId           : String,
    val babyId           : String?                    = null,
    val babyName         : String?                    = null,
    val title            : String,
    val body             : String,
    val category         : NotificationCategory,
    val priority         : NotificationPriorityLevel  = NotificationPriorityLevel.MEDIUM,
    val isRead           : Boolean                    = false,
    val createdAt        : String,                    // ISO-8601 string
    val deepLinkRoute    : String?                    = null,  // e.g. "health/vaccinations"
    val imageUrl         : String?                    = null,
    val actionLabel      : String?                    = null,  // e.g. "View Schedule"
    val actionRoute      : String?                    = null
)

@Serializable
data class NotificationListResponse(
    val success               : Boolean,
    val notifications         : List<AppNotification>,
    val unreadCount           : Long,
    val totalCount            : Int
)

@Serializable
data class MarkReadRequest(val notificationIds: List<String>)

@Serializable
data class RegisterFcmTokenRequest(
    val userId   : String,
    val fcmToken : String,
    val platform : String   // "android" | "ios" | "web"
)

// ─────────────────────────────────────────────────────────────────────────────
// Deep link routes — matches your navigation graph
// ─────────────────────────────────────────────────────────────────────────────

object DeepLinkRoutes {
    const val HOME                = "home"
    const val VACCINATION         = "health/vaccinations"
    const val GROWTH_CHART        = "charts"
    const val APPOINTMENTS        = "health/appointments"
    const val HEALTH_ISSUES       = "health/issues"
    const val FAMILY_HISTORY      = "settings/family-history"
    const val CHILD_ILLNESSES     = "settings/child-illnesses"
    const val VISION_MOTOR        = "settings/vision-motor"
    const val HEARING_SPEECH      = "settings/hearing-speech"
    const val MEMORIES            = "memories"
    const val BABY_PROFILE        = "baby-profile"
    const val SETTINGS            = "settings"
    const val ADD_MEASUREMENT     = "add-measurement"
    const val ADD_APPOINTMENT     = "add-appointment"
}