package org.example.project.babygrowthtrackingapplication.com.babygrowth.presentation.screens.home.model

import kotlinx.serialization.Serializable

// ═══════════════════════════════════════════════════════════════════════════
// GuideModels.kt
//
// Shared data classes that mirror the JSON structure of
//   sleep_guide_content.json  and  feeding_guide_content.json
//
// All fields use multilingual maps keyed by language code:
//   "en" | "ku_sorani" | "ku_badini" | "ar"
// ═══════════════════════════════════════════════════════════════════════════

// ── Localised string helper ──────────────────────────────────────────────

@Serializable
data class LocalizedString(
    val en          : String = "",
    val ku_sorani   : String = "",
    val ku_badini   : String = "",
    val ar          : String = ""
) {
    /**
     * Returns the best available string for the given language code.
     * Falls back to English if the translation is absent.
     */
    fun get(langCode: String): String = when (langCode) {
        "ku"  -> ku_sorani.ifBlank { en }
        "ckb" -> ku_badini.ifBlank { en }
        "ar"  -> ar.ifBlank { en }
        else  -> en
    }
}

// ── Tab descriptor ───────────────────────────────────────────────────────

@Serializable
data class GuideTab(
    val id   : String,
    val label: LocalizedString
)

// ── Media attachment (lullabies) ─────────────────────────────────────────

@Serializable
data class GuideMedia(
    val type            : String = "audio",
    val asset_key       : String = "",
    val duration_seconds: Int    = 0
)

// ── Single guide content item ────────────────────────────────────────────

@Serializable
data class GuideItem(
    val id         : String,
    val tab_id     : String?          = null,   // null → belongs to all tabs
    val title      : LocalizedString,
    val description: LocalizedString,
    val tip        : LocalizedString? = null,
    val media      : GuideMedia?      = null    // only lullabies have this
)

// ── Age range bucket ─────────────────────────────────────────────────────

@Serializable
data class GuideAgeRange(
    val min_months: Int,
    val max_months: Int,
    val label     : LocalizedString,
    val items     : List<GuideItem>
)

// ── Top-level strategy (one of the 4 Sleep or 5 Feeding categories) ──────

@Serializable
data class GuideStrategy(
    val id        : String,
    val icon      : String,
    val title     : LocalizedString,
    val tabs      : List<GuideTab>? = null,     // null → no sub-tabs
    val age_ranges: List<GuideAgeRange>
) {
    /**
     * Returns all items for [ageInMonths], across all age-range buckets that
     * contain the given age, merged into a single flat list.
     */
    fun itemsForAge(ageInMonths: Int): List<GuideItem> =
        age_ranges
            .filter { ageInMonths in it.min_months..it.max_months }
            .flatMap { it.items }

    /**
     * Returns items filtered both by age AND by tab.
     * If [tabId] is "all" (or null), all items for the age are returned.
     */
    fun itemsForAgeAndTab(ageInMonths: Int, tabId: String?): List<GuideItem> {
        val all = itemsForAge(ageInMonths)
        return if (tabId == null || tabId == "all") all
        else all.filter { it.tab_id == tabId || it.tab_id == null }
    }
}

// ── Root guide document ───────────────────────────────────────────────────

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
 * Per-card feedback state stored in the ViewModel.
 * [usefulCount] comes from the backend.
 * [userVote]    is the vote the current user cast (NONE if not voted yet).
 * [isLoading]   true while an API call is in flight.
 */
data class CardFeedbackState(
    val contentId  : String,
    val usefulCount: Int       = 0,
    val userVote   : UserVote  = UserVote.NONE,
    val isLoading  : Boolean   = false
)

// ═══════════════════════════════════════════════════════════════════════════
// Lullaby player state
// ═══════════════════════════════════════════════════════════════════════════

data class LullabyPlayerState(
    val currentItemId       : String?  = null,
    val isPlaying           : Boolean  = false,
    val positionSeconds     : Int      = 0,
    val durationSeconds     : Int      = 0
) {
    val progress: Float
        get() = if (durationSeconds > 0) positionSeconds / durationSeconds.toFloat() else 0f

    fun formatTime(seconds: Int): String {
        val m = seconds / 60
        val s = seconds % 60
        return "%d:%02d".format(m, s)
    }

    val positionFormatted: String get() = formatTime(positionSeconds)
    val durationFormatted: String get() = formatTime(durationSeconds)
}