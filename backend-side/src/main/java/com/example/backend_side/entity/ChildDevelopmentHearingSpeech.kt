package com.example.backend_side.entity

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

// ─────────────────────────────────────────────────────────────────────────────
// ChildDevelopmentHearingSpeech  (Screen 2: بیستن + ئاغاوتن — Hearing + Talking)
//
// Maps to: child_development_hearing_speech table
// One record per baby per check_month.
// ─────────────────────────────────────────────────────────────────────────────

@Entity
@Table(
    name = "child_development_hearing_speech",
    uniqueConstraints = [UniqueConstraint(
        name        = "uq_hearing_speech_baby_month",
        columnNames = ["baby_id", "check_month"]
    )]
)
data class ChildDevelopmentHearingSpeech(

    @Id
    @Column(name = "record_id", columnDefinition = "VARCHAR(36)", updatable = false, nullable = false)
    var recordId: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "baby_id", nullable = false)
    var baby: Baby? = null,

    /** Age-milestone month (1, 3, 6, 9, or 12) */
    @Column(name = "check_month", nullable = false)
    var checkMonth: Int = 1,

    @Column(name = "check_date")
    var checkDate: LocalDate? = null,

    // ── MONTH 1 ──────────────────────────────────────────────────────────────
    /** 1. بەدەنگی کتوپر رانە پەرێت، چاوەکانی ئەتروکێنیٰ، دیقەت دەدات بەچاوەکانی */
    @Column(name = "m1_startles_fixates_attentive")
    var m1StartlesFixatesAttentive: Boolean? = null,

    /** 2. جوڵەی بۆ ماوەیەک راڵەگرێت بۆ ماوەی (٣-٥) چرکە کاتی کە گوێی لەزدنگێک دەبێت که لەدووری (٣-٥) پێی لە گوێچکەی */
    @Column(name = "m1_turns_to_sound_brief")
    var m1TurnsToSoundBrief: Boolean? = null,

    /** 3. بەهێز دەمگرێت کاتێک مەکە بە برسیەتی یاخود ئارەحەتی دەکات */
    @Column(name = "m1_cries_hunger_discomfort")
    var m1CriesHungerDiscomfort: Boolean? = null,

    /** 4. گرنگی بە کەسەکان دەدات زیاتر لە دەنگەکان */
    @Column(name = "m1_prefers_voices_over_sounds")
    var m1PrefersVoicesOverSounds: Boolean? = null,

    // ── MONTH 3 ──────────────────────────────────────────────────────────────
    /** 1. دەنگی بەرز ئارامی پێدەبەخشێت و چاوەکانی دەنووفێنێت و سەیری شتەکان دەکات */
    @Column(name = "m3_calm_with_loud_sound")
    var m3CalmWithLoudSound: Boolean? = null,

    /** 2. کاتێک گوێی لە دایکی دەبێت ئارام دەبێتەوە و پێدەکەنێت */
    @Column(name = "m3_calms_with_mothers_voice")
    var m3CalmsWithMothersVoice: Boolean? = null,

    /** 3. سەری و چاوەکانی رەوە سەرچاوەی دەنگ دەجووڵێنێت */
    @Column(name = "m3_localizes_sound_source")
    var m3LocalizesSoundSource: Boolean? = null,

    /** 4. ئیومەکانی دەمزێت یان ئایدەکێشێتە سەر یەکاتری لە کاتی خواردنەوەمی شیر */
    @Column(name = "m3_vocal_during_feeding")
    var m3VocalDuringFeeding: Boolean? = null,

    /** 5. هەستی خۆی دەمردەبرێت کاتێک لە دەنگێکی نزیک دەبێت */
    @Column(name = "m3_responds_to_nearby_sound")
    var m3RespondsToNearbySound: Boolean? = null,

    // ── MONTH 6 ──────────────────────────────────────────────────────────────
    /** 1. لە ژووردا یەکسەر ئاوڕ ناور لەو شوێنە دەداتەوە کە دەنگی دایکی لێوە دێت */
    @Column(name = "m6_locates_mothers_voice")
    var m6LocatesMothersVoice: Boolean? = null,

    /** 2. هەندێک دەنگ دەمردەبرێت لە شێوەی ئاواز، یاخود وەلام دەداتەوە بە چەند برگە ووەک( دا، تە .... هتد ) */
    @Column(name = "m6_vocalizes_sounds_babbles")
    var m6VocalizesSoundsBabbles: Boolean? = null,

    /** 3. پێدەکەنێت و زمانی دەمردینێت وەک چاولیکردن لە قسەی گەورەیان */
    @Column(name = "m6_smiles_imitates_speech")
    var m6SmilesImitateSpeech: Boolean? = null,

    // ── MONTH 9 ──────────────────────────────────────────────────────────────
    /** 1. هۆشیارە و دلخۆشە بە دەنگانەی رۆژانە دەمبیستێت */
    @Column(name = "m9_aware_of_daily_sounds")
    var m9AwareOfDailySounds: Boolean? = null,

    /** 2. هەوڵ دەدات بەهێمنی و بەرە بەرە قسە لەگەڵ دەمرووبەری بکات */
    @Column(name = "m9_attempts_reciprocal_talking")
    var m9AttemptsReciprocalTalking: Boolean? = null,

    /** 3. هاوار دەکات و دەمتێژێنێت بۆ ئەوی سەرنجی دەور و بەری بۆ خۆی رابکێشێت */
    @Column(name = "m9_calls_for_attention")
    var m9CallsForAttention: Boolean? = null,

    /** 4. هەندێک دەنگ دووبارە دەکاتەوە وەک( دادا، بابا .... هتد ) */
    @Column(name = "m9_reduplicated_babble")
    var m9ReduplicatedBabble: Boolean? = null,

    /** 5. وەلامی هەندێک پرسیار دەداتەوە وەک کوا بابە ؟ کوا کاتژمێرەکە ؟ */
    @Column(name = "m9_responds_to_simple_questions")
    var m9RespondsToSimpleQuestions: Boolean? = null,

    // ── MONTH 12 ─────────────────────────────────────────────────────────────
    /** 1. کاتێک گوێی لە ناوی خۆی دەبێت یەکسەر ئاوڕ دەداتەوە */
    @Column(name = "m12_responds_to_own_name")
    var m12RespondsToOwnName: Boolean? = null,

    /** 2. هەندێک ووشەی مانا بە خۆش دەمردەبرێت */
    @Column(name = "m12_meaningful_words")
    var m12MeaningfulWords: Boolean? = null,

    /** 3. لە هەندێک پرسیار تێدەگات وەک( کوا چاوت، کوا سەرت، .... ) */
    @Column(name = "m12_understands_simple_commands")
    var m12UnderstandsSimpleCommands: Boolean? = null,

    /** 4. حەز بە وەرگرتنی شت و بەخشینی شت دەکات بە گەوران کاتێک داوای لێدەمکرێت */
    @Column(name = "m12_gives_takes_on_request")
    var m12GivesTakesOnRequest: Boolean? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recorded_by")
    var recordedBy: User? = null,

    @Column(name = "notes", columnDefinition = "TEXT")
    var notes: String? = null,

    ) : BaseEntity() {
    @PreUpdate
    fun preUpdate() { updatedAt = LocalDateTime.now() }
}