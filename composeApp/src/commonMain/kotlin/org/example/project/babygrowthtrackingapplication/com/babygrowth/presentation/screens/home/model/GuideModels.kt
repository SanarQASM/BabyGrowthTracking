package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model

import kotlinx.serialization.Serializable

// ═══════════════════════════════════════════════════════════════════════════
// GuideModels.kt
// ═══════════════════════════════════════════════════════════════════════════

@Serializable
data class LocalizedString(
    val en          : String = "",
    val ku_sorani   : String = "",
    val ku_badini   : String = "",
    val ar          : String = ""
) {
    fun get(langCode: String): String = when (langCode) {
        "ku"  -> ku_sorani.ifBlank { en }
        "ckb" -> ku_badini.ifBlank { en }
        "ar"  -> ar.ifBlank { en }
        else  -> en
    }
}

@Serializable
data class GuideTab(
    val id   : String,
    val label: LocalizedString
)

@Serializable
data class GuideMedia(
    val type            : String = "audio",
    val asset_key       : String = "",
    val duration_seconds: Int    = 0
)

@Serializable
data class GuideItem(
    val id         : String,
    val tab_id     : String?          = null,
    val title      : LocalizedString,
    val description: LocalizedString,
    val tip        : LocalizedString? = null,
    val media      : GuideMedia?      = null
)

@Serializable
data class GuideAgeRange(
    val min_months: Int,
    val max_months: Int,
    val label     : LocalizedString,
    val items     : List<GuideItem>
)

@Serializable
data class GuideStrategy(
    val id        : String,
    val icon      : String,
    val title     : LocalizedString,
    val tabs      : List<GuideTab>? = null,
    val age_ranges: List<GuideAgeRange>
) {
    fun itemsForAge(ageInMonths: Int): List<GuideItem> =
        age_ranges
            .filter { ageInMonths in it.min_months..it.max_months }
            .flatMap { it.items }

    fun itemsForAgeAndTab(ageInMonths: Int, tabId: String?): List<GuideItem> {
        val all = itemsForAge(ageInMonths)
        return if (tabId == null || tabId == "all") all
        else all.filter { it.tab_id == tabId || it.tab_id == null }
    }
}

@Serializable
data class GuideDocument(
    val guide_id  : String,
    val title     : LocalizedString,
    val subtitle  : LocalizedString,
    val strategies: List<GuideStrategy>
) {
    fun strategyById(id: String): GuideStrategy? =
        strategies.find { it.id == id }
}

// ═══════════════════════════════════════════════════════════════════════════
// Feedback / vote UI state
// ═══════════════════════════════════════════════════════════════════════════

enum class UserVote { USEFUL, USELESS, NONE }

/**
 * FIX: usefulCount changed from Int to Long to match the backend response
 * type (COUNT(*) returns Long in JPA/Kotlin).  The UI coerces to Int only
 * when displaying (Long.toInt() is safe for realistic vote counts).
 */
data class CardFeedbackState(
    val contentId  : String,
    val usefulCount: Long      = 0L,   // FIX: was Int — backend returns Long
    val userVote   : UserVote  = UserVote.NONE,
    val isLoading  : Boolean   = false
)

// ═══════════════════════════════════════════════════════════════════════════
// Lullaby player state
// ═══════════════════════════════════════════════════════════════════════════

data class LullabyPlayerState(
    val currentItemId  : String?  = null,
    val isPlaying      : Boolean  = false,
    val positionSeconds: Int      = 0,
    val durationSeconds: Int      = 0
) {
    val progress: Float
        get() = if (durationSeconds > 0) positionSeconds / durationSeconds.toFloat() else 0f

    fun formatTime(seconds: Int): String {
        val m = seconds / 60
        val s = seconds % 60
        val sPadded = if (s < 10) "0$s" else "$s"
        return "$m:$sPadded"
    }

    val positionFormatted: String get() = formatTime(positionSeconds)
    val durationFormatted: String get() = formatTime(durationSeconds)
}
