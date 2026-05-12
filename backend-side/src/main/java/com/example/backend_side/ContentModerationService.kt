// File: backend-side/src/main/java/com/example/backend_side/ContentModerationService.kt
package com.example.backend_side

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

// ─────────────────────────────────────────────────────────────────────────────
// ContentModerationService
//
// Guards the group chat from abusive content:
//   • Profanity / swearing
//   • Threats / harassment / hate speech
//   • Personally identifiable information (phone / email patterns) — optional
//
// Strategy: multi-layer pattern matching.
//
//   Layer 1 — Exact-word deny-list (English + Arabic + Kurdish transliterations)
//   Layer 2 — Regex patterns for threat phrases
//   Layer 3 — Obfuscation bypass: normalise leet-speak and repetition before
//              running Layer 1/2 (e.g. "f**k" → "fk", "@ss" → "ass")
//
// This is intentionally conservative: when in doubt, block.
// The service returns a ModerationResult so callers can surface helpful
// localised error messages instead of a generic 400.
// ─────────────────────────────────────────────────────────────────────────────

data class ModerationResult(
    val blocked : Boolean,
    val reason  : ModerationReason = ModerationReason.CLEAN
)

enum class ModerationReason(val messageKey: String) {
    CLEAN      (""),
    PROFANITY  ("chat_error_profanity"),
    THREAT     ("chat_error_threat"),
    TOO_LONG   ("chat_error_too_long"),
    EMPTY      ("chat_error_empty"),
    SPAM       ("chat_error_spam")
}

@Service
class ContentModerationService {

    companion object {
        private const val MAX_LENGTH = 1_000

        // ── Layer 1: Profanity word-list ──────────────────────────────────────
        // Kept minimal here — extend as needed. Words stored lowercase.
        private val PROFANITY_WORDS: Set<String> = setOf(
            // English profanity (common forms)
            "fuck", "fuk", "fck", "shit", "sh1t", "bitch", "btch",
            "asshole", "ass", "bastard", "damn", "cunt", "cock", "dick",
            "whore", "slut", "piss", "crap", "wank", "wanker",
            "nigger", "nigga", "faggot", "fag", "retard",
            // Arabic transliterations (common in Kurdish/Iraqi Arabic context)
            "kos", "kes", "khara", "ibn el sharmouta", "sharmouta",
            "sharmota", "ibn kalb", "kalb", "manyak", "khawal", "gazma",
            "sharr", "naik", "zibbi", "air", "ayir", "kir", "kuss",
            // Kurdish (Sorani/Badini) transliterations
            "kure mele", "gap", "qahpe", "qahba", "pelew", "gaw",
            "tlaw", "gayan", "kawthar", "berger", "kire",
            // General offensive
            "loser", "idiot", "stupid", "moron", "dumb", "hate you",
            "kill yourself", "kys", "go die"
        )

        // ── Layer 2: Threat regex patterns ────────────────────────────────────
        private val THREAT_PATTERNS: List<Regex> = listOf(
            Regex("""i\s+(will|gonna|am going to|plan to)\s+(kill|hurt|harm|attack|beat|destroy)\s+you""", RegexOption.IGNORE_CASE),
            Regex("""(kill|murder|hurt|harm|attack|beat up|destroy)\s+(you|ur|your|u)""", RegexOption.IGNORE_CASE),
            Regex("""(you('re| are)\s+)?dead\s+(to me|meat|when)""", RegexOption.IGNORE_CASE),
            Regex("""watch\s+(your|ur)\s+back""", RegexOption.IGNORE_CASE),
            Regex("""(i('ll|\s+will)\s+find\s+(you|where\s+you\s+live))""", RegexOption.IGNORE_CASE),
            Regex("""(threat|threaten|terrorize|terrorise)""", RegexOption.IGNORE_CASE),
            Regex("""(blow\s+up|bomb|explosive|grenade|weapon)\s+(you|your|the)""", RegexOption.IGNORE_CASE),
            // Arabic/transliterated threats
            Regex("""(ra7|ra7a)\s+(aqtul|aDhribak|uqtul)""", RegexOption.IGNORE_CASE),
            Regex("""(haKill|ha'Kill|bakill)\s+.*""", RegexOption.IGNORE_CASE),
        )

        // ── Layer 3: Leet-speak normalisation map ─────────────────────────────
        private val LEET_MAP = mapOf(
            '@' to 'a',
            '0' to 'o',
            '1' to 'i',
            '3' to 'e',
            '4' to 'a',
            '5' to 's',
            '6' to 'g',
            '7' to 't',
            '$' to 's',
            '!' to 'i',
            '+' to 't'
        )
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Public API
    // ─────────────────────────────────────────────────────────────────────────

    fun moderate(text: String): ModerationResult {
        // ── Basic structural checks ───────────────────────────────────────────
        val trimmed = text.trim()
        if (trimmed.isBlank()) {
            return ModerationResult(blocked = true, reason = ModerationReason.EMPTY)
        }
        if (trimmed.length > MAX_LENGTH) {
            return ModerationResult(blocked = true, reason = ModerationReason.TOO_LONG)
        }

        // Spam: same char repeated 20+ times
        if (Regex("""(.)\1{19,}""").containsMatchIn(trimmed)) {
            return ModerationResult(blocked = true, reason = ModerationReason.SPAM)
        }

        // ── Normalise for bypass detection ────────────────────────────────────
        val normalised = normalise(trimmed)

        // ── Layer 1: profanity ────────────────────────────────────────────────
        if (containsProfanity(normalised)) {
            logger.info { "[Moderation] Profanity blocked in message (${trimmed.take(30)}...)" }
            return ModerationResult(blocked = true, reason = ModerationReason.PROFANITY)
        }

        // ── Layer 2: threats ──────────────────────────────────────────────────
        if (containsThreat(trimmed) || containsThreat(normalised)) {
            logger.info { "[Moderation] Threat blocked in message (${trimmed.take(30)}...)" }
            return ModerationResult(blocked = true, reason = ModerationReason.THREAT)
        }

        return ModerationResult(blocked = false)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Normalise: lowercase → leet-decode → collapse runs of same character
     * → strip punctuation used as obfuscation (*_- between letters).
     */
    private fun normalise(text: String): String {
        val sb = StringBuilder(text.length)
        text.lowercase().forEach { ch ->
            sb.append(LEET_MAP[ch] ?: ch)
        }
        // Strip asterisks, underscores, dots when surrounded by letters (e.g. f**k → fk)
        return sb.toString()
            .replace(Regex("""(?<=[a-z])[*_.]+(?=[a-z])"""), "")
            .replace(Regex("""(.)\1{3,}"""), "$1$1") // collapse excessive repetition
    }

    private fun containsProfanity(normalisedText: String): Boolean {
        // Tokenise on non-letter boundaries to avoid false positives
        val words = normalisedText.split(Regex("""[^a-zA-Z\u0600-\u06FF]+"""))
        return words.any { word -> PROFANITY_WORDS.any { bad -> word.contains(bad) } }
    }

    private fun containsThreat(text: String): Boolean =
        THREAT_PATTERNS.any { it.containsMatchIn(text) }
}