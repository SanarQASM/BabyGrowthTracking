package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model

import kotlinx.serialization.Serializable

// ─────────────────────────────────────────────────────────────────────────────
// Reschedule request / response models (mirrors backend DTOs)
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Adjustment reasons that the user / team member can choose from when
 * rescheduling vaccinations.  Mirrors backend AdjustmentReason enum.
 */
enum class RescheduleReason(val displayEn: String, val emoji: String) {
    HOLIDAY            ("National / Public Holiday",   "🎉"),
    BENCH_CLOSED       ("Health Center Closed",        "🏥"),
    PARENT_MISSED      ("Parent Missed the Date",      "📅"),
    PARENT_RESCHEDULED ("Parent Requested Reschedule", "👨‍👩‍👧"),
    TEAM_RESCHEDULED   ("Team Rescheduled",            "🩺")
}

/** Per-vaccine result item returned by the reschedule API. */
@Serializable
data class RescheduleItemResultNet(
    val scheduleId           : String,
    val vaccineName          : String,
    val vaccineNameAr        : String? = null,
    val vaccineNameKu        : String? = null,
    val vaccineNameCkb       : String? = null,
    val recommendedAgeMonths : Int,
    val doseNumber           : Int,
    val oldScheduledDate     : String,
    val newScheduledDate     : String? = null,
    val status               : String,
    val rescheduled          : Boolean,
    val skipReason           : String? = null
)

/** Full reschedule response from the backend. */
@Serializable
data class RescheduleResponseNet(
    val babyId          : String,
    val babyName        : String,
    val totalVaccines   : Int,
    val rescheduledCount: Int,
    val skippedCount    : Int,
    val tooLateCount    : Int,
    val results         : List<RescheduleItemResultNet>,
    val message         : String
)

/** UI-layer model for a single vaccine reschedule outcome (shown in dialog). */
data class RescheduleItemUi(
    val scheduleId          : String,
    val vaccineName         : String,
    val doseNumber          : Int,
    val recommendedAgeMonths: Int,
    val oldDate             : String,
    val newDate             : String?,
    val rescheduled         : Boolean,
    val tooLate             : Boolean,       // window exceeded → marked Missed
    val skipReason          : String?
)

/** Aggregated UI state after a reschedule operation. */
data class RescheduleResultUi(
    val totalVaccines   : Int,
    val rescheduledCount: Int,
    val skippedCount    : Int,
    val tooLateCount    : Int,
    val items           : List<RescheduleItemUi>,
    val message         : String
)

fun RescheduleItemResultNet.toUi() = RescheduleItemUi(
    scheduleId           = scheduleId,
    vaccineName          = vaccineName,
    doseNumber           = doseNumber,
    recommendedAgeMonths = recommendedAgeMonths,
    oldDate              = oldScheduledDate,
    newDate              = newScheduledDate,
    rescheduled          = rescheduled,
    tooLate              = !rescheduled && skipReason?.contains("too late", ignoreCase = true) == true
            || status.equals("MISSED", ignoreCase = true) && newScheduledDate == null
            && skipReason?.contains("window exceeded", ignoreCase = true) == true,
    skipReason           = skipReason
)

fun RescheduleResponseNet.toUi() = RescheduleResultUi(
    totalVaccines    = totalVaccines,
    rescheduledCount = rescheduledCount,
    skippedCount     = skippedCount,
    tooLateCount     = tooLateCount,
    items            = results.map { it.toUi() },
    message          = message
)