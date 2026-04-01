package com.example.backend_side.entity

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

// ─────────────────────────────────────────────────────────────────────────────
// ChildDevelopmentVisionMotor  (Screen 1: بینین + جووڵە  — Seeing + Moving)
//
// Maps to: child_development_vision_motor table
// One record per baby per check_month.
// Only months ≤ baby's current age in months are meaningful.
// ─────────────────────────────────────────────────────────────────────────────

@Entity
@Table(
    name = "child_development_vision_motor",
    uniqueConstraints = [UniqueConstraint(
        name        = "uq_vision_motor_baby_month",
        columnNames = ["baby_id", "check_month"]
    )]
)
data class ChildDevelopmentVisionMotor(

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
    /** 1. سەری دەجووڵێنێت و چاوەکانی بەلای رووناکی دەبات */
    @Column(name = "m1_head_moves_follows_light")
    var m1HeadMovesFollowsLight: Boolean? = null,

    /** 2. سەیری کەسەکان دەکات و سەری بەلای شتە جووڵاومەکان دەجووڵێنێت */
    @Column(name = "m1_tracks_people_objects")
    var m1TracksPeopleObjects: Boolean? = null,

    /** 3. ئەدووری یەک پێی وەدوای رووناکی لایتێکی فەڵەمی دەکەوێت */
    @Column(name = "m1_follows_flashlight")
    var m1FollowsFlashlight: Boolean? = null,

    // ── MONTH 3 ──────────────────────────────────────────────────────────────
    /** 1. منداڵەکە سەری بە گۆشەی (180) پلە بەلای شتە جووڵاومەکان دەجووڵێنێت */
    @Column(name = "m3_head_180_tracking")
    var m3Head180Tracking: Boolean? = null,

    /** 2. منداڵەکە هۆشی تەواوی هەیە و بە وووردی سەیری دەم و چاوەکان دەکات */
    @Column(name = "m3_attentive_face_tracking")
    var m3AttentiveFaceTracking: Boolean? = null,

    /** 3. بە وووردی سەیری جووڵەی دەستی خۆی دەکات */
    @Column(name = "m3_watches_own_hands")
    var m3WatchesOwnHands: Boolean? = null,

    /** 4. سینگی دایکی خۆی دەناسێتەوە و بە بینینی خۆشهاڵە */
    @Column(name = "m3_recognizes_mother")
    var m3RecognizesMother: Boolean? = null,

    /** 5. دەستەکانی کراینەوە دانە خراوە هەروەک هەڵمانگی ژیانی */
    @Column(name = "m3_hands_open_reflex")
    var m3HandsOpenReflex: Boolean? = null,

    // ── MONTH 6 ──────────────────────────────────────────────────────────────
    /** 1. زۆر نەجووڵیٰ چاوو سەری بەهەموو لایەکدا ئەجووڵێنیٰ کاتێک سەرنجی رابکێشیٰ */
    @Column(name = "m6_eyes_head_full_range")
    var m6EyesHeadFullRange: Boolean? = null,

    /** 2. دوای جووڵەی کەسی گەورە ئەکەوێت بەدەرێژایی ژوورەکە بەووریاپیەوە */
    @Column(name = "m6_follows_person_across_room")
    var m6FollowsPersonAcrossRoom: Boolean? = null,

    /** 3. پێدەکەنێت و دەنگ هەڵدەبرێت لەدەمەمەوە کاتێک خۆی لە ناوێنەدا دەبینێت */
    @Column(name = "m6_smiles_at_mirror")
    var m6SmilesAtMirror: Boolean? = null,

    /** 4. لە کاتی کەوتنە خوارەمەوی (شەفشەفە) لە دەستیدا هەوڵ دەدات هەڵیگرێتەوە */
    @Column(name = "m6_reaches_for_dropped_object")
    var m6ReachesForDroppedObject: Boolean? = null,

    /** 5. ئەگەر شتێکی لە نێو دەستیدا بوو ئەوا فەرمی دەدات بۆ لەدەستگرتنی شتێکی تر */
    @Column(name = "m6_transfers_objects")
    var m6TransfersObjects: Boolean? = null,

    // ── MONTH 9 ──────────────────────────────────────────────────────────────
    /** 1. سەیری شتەکان و کەسەکان دەکات بە ئاگاهی و وووردیەکی زیاتر */
    @Column(name = "m9_keen_visual_attention")
    var m9KeenVisualAttention: Boolean? = null,

    /** 2. شتەکان لە نێو پەنجەکانی دەگرێت لەگەڵ پەنجەی ئەسپێکوژە */
    @Column(name = "m9_pincer_grasp")
    var m9PincerGrasp: Boolean? = null,

    /** 3. کاتێک دەمیەوێت شتێک ببات هەوڵ دەدات بۆ لای خۆی رابکێشێت */
    @Column(name = "m9_reaches_desired_objects")
    var m9ReachesDesiredObjects: Boolean? = null,

    /** 4. جەخت کردنەوەی لەسەر شتەکان دەمکاتە مەودای (١٠ - ١٢) م */
    @Column(name = "m9_attention_span_10_12m")
    var m9AttentionSpan: Boolean? = null,

    // ── MONTH 12 ─────────────────────────────────────────────────────────────
    /** 1. پەنجەی شایەتومانی (دۆشاو مژە) و پە نجەی گەورە بەکاردێنێت بۆگرتنی شتەکان */
    @Column(name = "m12_neat_pincer_grasp")
    var m12NeatPincerGrasp: Boolean? = null,

    /** 2. ریوی خۆی دەکاتە یاریەکان */
    @Column(name = "m12_plays_with_toys")
    var m12PlaysWithToys: Boolean? = null,

    /** 3. بەرە بەرە شتەکان جێدەهێڵێت */
    @Column(name = "m12_releases_objects")
    var m12ReleasesObjects: Boolean? = null,

    /** 4. کەسەکانی دەور و بەری دەناسێتەوە */
    @Column(name = "m12_recognizes_familiar_people")
    var m12RecognizesFamiliarPeople: Boolean? = null,

    /** 5. جل و بەرگی کەسانی تر ئەرادەهکێشێت بۆ ئەومی سەرنجیان بۆ لای خۆی رابکێشێت */
    @Column(name = "m12_gets_attention_by_tugging")
    var m12GetsAttentionByTugging: Boolean? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recorded_by")
    var recordedBy: User? = null,

    @Column(name = "notes", columnDefinition = "TEXT")
    var notes: String? = null,

    ) : BaseEntity() {
    @PreUpdate
    fun preUpdate() { updatedAt = LocalDateTime.now() }
}