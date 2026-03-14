package com.example.backend_side

import com.example.backend_side.entity.VaccineType
import com.example.backend_side.repositories.VaccineTypeRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

private val logger = KotlinLogging.logger {}

@Component
class DataInitializer(
    private val vaccineTypeRepository: VaccineTypeRepository
) {

    @PostConstruct
    @Transactional
    fun init() {
        if (vaccineTypeRepository.count() > 0) {
            logger.info { "✅ vaccine_types already seeded (${vaccineTypeRepository.count()} rows)" }
            return
        }
        logger.info { "🌱 Seeding vaccine_types with multilingual data..." }
        vaccineTypeRepository.saveAll(buildVaccineList())
        logger.info { "✅ Seeded ${vaccineTypeRepository.count()} vaccine types" }
    }

    // ── Helper to create a VaccineType with all 4 language names ──────────
    private fun v(
        nameEn : String,
        nameAr : String,
        nameKu : String,
        nameCkb: String,
        ageMonths: Int,
        dose: Int,
        descEn : String,
        descAr : String,
        descKu : String,
        descCkb: String,
        mandatory: Boolean = true
    ) = VaccineType(
        vaccineName    = nameEn,
        vaccineNameAr  = nameAr,
        vaccineNameKu  = nameKu,
        vaccineNameCkb = nameCkb,
        recommendedAgeMonths = ageMonths,
        doseNumber     = dose.toByte(),
        description    = descEn,
        descriptionAr  = descAr,
        descriptionKu  = descKu,
        descriptionCkb = descCkb,
        isMandatory    = mandatory
    )

    private fun buildVaccineList(): List<VaccineType> = listOf(

        // ── Birth (0 months) ──────────────────────────────────────────────
        v("BCG", "BCG", "BCG", "BCG",
            0, 1,
            "Tuberculosis prevention — given at birth",
            "الوقاية من السل — تُعطى عند الولادة",
            "پارێزگاری لە تووبێرکوڵۆز — لە کاتی لەدایکبوون دەدرێت",
            "پاراستنا ji tuberkulozê — di dem jidayikbûnê de tê dayîn",
            true),

        v("Hepatitis B", "التهاب الكبد B", "هێپاتیتی B", "Hepatît B",
            0, 1,
            "Hepatitis B — birth dose",
            "التهاب الكبد B — الجرعة عند الولادة",
            "هێپاتیتی B — ژەمەی لەدایکبوون",
            "Hepatît B — doza jidayikbûnê",
            true),

        v("OPV", "شلل الأطفال (فموي)", "فلیجی منداڵان (دەمی)", "Felcbûna zarokan (devkî)",
            0, 1,
            "Oral Polio Vaccine — birth dose",
            "لقاح شلل الأطفال الفموي — جرعة الولادة",
            "کوتانی فلیجی منداڵان بە دەم — ژەمەی لەدایکبوون",
            "Derziya felcbûna zarokan bi devkî — doza jidayikbûnê",
            true),

        // ── 2 months ──────────────────────────────────────────────────────
        v("Pentavalent", "الخماسي", "پێنجانە", "Pêncanî",
            2, 1,
            "DTP + Hib + Hepatitis B — dose 1",
            "الخماسي (الدفتيريا والتيتانوس والسعال الديكي + المستدمية + التهاب الكبد B) — الجرعة 1",
            "پێنجانە (دیفتریا، تێتانۆس، کۆخە + هیب + هێپاتیت B) — ژەمەی 1",
            "Pêncanî (diftirî, tetanos, kuxe + Hib + Hepatît B) — doza 1",
            true),

        v("OPV", "شلل الأطفال (فموي)", "فلیجی منداڵان (دەمی)", "Felcbûna zarokan (devkî)",
            2, 2,
            "Oral Polio Vaccine — dose 2",
            "لقاح شلل الأطفال الفموي — الجرعة 2",
            "کوتانی فلیجی منداڵان بە دەم — ژەمەی 2",
            "Derziya felcbûna zarokan bi devkî — doza 2",
            true),

        v("PCV", "المكورات الرئوية", "پێنسەلۆکۆک", "Pneumokok",
            2, 1,
            "Pneumococcal Conjugate Vaccine — dose 1",
            "لقاح المكورات الرئوية المقترن — الجرعة 1",
            "کوتانی پێنسەلۆکۆک — ژەمەی 1",
            "Derziya pneumokokê — doza 1",
            true),

        v("Rotavirus", "الروتا فيروس", "رۆتافایرەس", "Rotavirus",
            2, 1,
            "Rotavirus gastroenteritis — dose 1",
            "التهاب المعدة والأمعاء بالروتا فيروس — الجرعة 1",
            "گڕوچکەی سەرچاوەی رۆتافایرەس — ژەمەی 1",
            "Înfeksiyona rûviyê ya rotavirusê — doza 1",
            true),

        // ── 4 months ──────────────────────────────────────────────────────
        v("Pentavalent", "الخماسي", "پێنجانە", "Pêncanî",
            4, 2,
            "DTP + Hib + Hepatitis B — dose 2",
            "الخماسي — الجرعة 2",
            "پێنجانە — ژەمەی 2",
            "Pêncanî — doza 2",
            true),

        v("OPV", "شلل الأطفال (فموي)", "فلیجی منداڵان (دەمی)", "Felcbûna zarokan (devkî)",
            4, 3,
            "Oral Polio Vaccine — dose 3",
            "لقاح شلل الأطفال الفموي — الجرعة 3",
            "کوتانی فلیجی منداڵان بە دەم — ژەمەی 3",
            "Derziya felcbûna zarokan bi devkî — doza 3",
            true),

        v("PCV", "المكورات الرئوية", "پێنسەلۆکۆک", "Pneumokok",
            4, 2,
            "Pneumococcal Conjugate Vaccine — dose 2",
            "لقاح المكورات الرئوية المقترن — الجرعة 2",
            "کوتانی پێنسەلۆکۆک — ژەمەی 2",
            "Derziya pneumokokê — doza 2",
            true),

        v("Rotavirus", "الروتا فيروس", "رۆتافایرەس", "Rotavirus",
            4, 2,
            "Rotavirus gastroenteritis — dose 2",
            "التهاب المعدة والأمعاء بالروتا فيروس — الجرعة 2",
            "گڕوچکەی سەرچاوەی رۆتافایرەس — ژەمەی 2",
            "Înfeksiyona rûviyê ya rotavirusê — doza 2",
            true),

        // ── 6 months ──────────────────────────────────────────────────────
        v("Pentavalent", "الخماسي", "پێنجانە", "Pêncanî",
            6, 3,
            "DTP + Hib + Hepatitis B — dose 3",
            "الخماسي — الجرعة 3",
            "پێنجانە — ژەمەی 3",
            "Pêncanî — doza 3",
            true),

        v("OPV", "شلل الأطفال (فموي)", "فلیجی منداڵان (دەمی)", "Felcbûna zarokan (devkî)",
            6, 4,
            "Oral Polio Vaccine — dose 4",
            "لقاح شلل الأطفال الفموي — الجرعة 4",
            "کوتانی فلیجی منداڵان بە دەم — ژەمەی 4",
            "Derziya felcbûna zarokan bi devkî — doza 4",
            true),

        v("Hepatitis B", "التهاب الكبد B", "هێپاتیتی B", "Hepatît B",
            6, 3,
            "Hepatitis B — dose 3",
            "التهاب الكبد B — الجرعة 3",
            "هێپاتیتی B — ژەمەی 3",
            "Hepatît B — doza 3",
            true),

        // ── 9 months ──────────────────────────────────────────────────────
        v("MMR", "الحصبة والنكاف والحصبة الألمانية", "سووری، قۆسەخوارەو سووری ئەڵمانی", "Sorî, Kulîlka baranê û Rubella",
            9, 1,
            "Measles, Mumps, Rubella — dose 1",
            "لقاح الحصبة والنكاف والحصبة الألمانية — الجرعة 1",
            "کوتانی سووری، قۆسەخوارەو سووری ئەڵمانی — ژەمەی 1",
            "Derziya sorî, kulîlka baranê û rubellayê — doza 1",
            true),

        // ── 12 months ─────────────────────────────────────────────────────
        v("PCV", "المكورات الرئوية", "پێنسەلۆکۆک", "Pneumokok",
            12, 3,
            "Pneumococcal Conjugate Vaccine — booster dose",
            "لقاح المكورات الرئوية المقترن — جرعة التعزيز",
            "کوتانی پێنسەلۆکۆک — ژەمەی بەهێزکەرەوە",
            "Derziya pneumokokê — doza xurtkirinê",
            true),

        v("Varicella", "جدري الماء", "مریشکانە", "Mirişkane",
            12, 1,
            "Chickenpox — dose 1",
            "لقاح جدري الماء — الجرعة 1",
            "کوتانی مریشکانە — ژەمەی 1",
            "Derziya mirişkaneyê — doza 1",
            false),

        // ── 15 months ─────────────────────────────────────────────────────
        v("MMR", "الحصبة والنكاف والحصبة الألمانية", "سووری، قۆسەخوارەو سووری ئەڵمانی", "Sorî, Kulîlka baranê û Rubella",
            15, 2,
            "Measles, Mumps, Rubella — dose 2",
            "لقاح الحصبة والنكاف والحصبة الألمانية — الجرعة 2",
            "کوتانی سووری، قۆسەخوارەو سووری ئەڵمانی — ژەمەی 2",
            "Derziya sorî, kulîlka baranê û rubellayê — doza 2",
            true),

        // ── 18 months ─────────────────────────────────────────────────────
        v("DTP booster", "معزز الدفتيريا والتيتانوس والسعال الديكي", "بەهێزکەرەوەی DTP", "Xurtkirina DTP",
            18, 1,
            "Diphtheria, Tetanus, Pertussis booster — 18 months",
            "معزز الدفتيريا والتيتانوس والسعال الديكي — 18 شهر",
            "بەهێزکەرەوەی دیفتریا، تێتانۆس، کۆخە — 18 مانگ",
            "Xurtkirina diftirî, tetanos, kuxeyê — 18 meh",
            true),

        v("OPV", "شلل الأطفال (فموي)", "فلیجی منداڵان (دەمی)", "Felcbûna zarokan (devkî)",
            18, 5,
            "Oral Polio Vaccine — 18 month booster",
            "لقاح شلل الأطفال الفموي — جرعة تعزيز 18 شهر",
            "کوتانی فلیجی منداڵان بە دەم — بەهێزکەرەوەی 18 مانگ",
            "Derziya felcbûna zarokan bi devkî — xurtkirina 18 mehî",
            true),

        v("Hepatitis A", "التهاب الكبد A", "هێپاتیتی A", "Hepatît A",
            18, 1,
            "Hepatitis A — dose 1",
            "التهاب الكبد A — الجرعة 1",
            "هێپاتیتی A — ژەمەی 1",
            "Hepatît A — doza 1",
            false),

        // ── 24 months ─────────────────────────────────────────────────────
        v("Hepatitis A", "التهاب الكبد A", "هێپاتیتی A", "Hepatît A",
            24, 2,
            "Hepatitis A — dose 2",
            "التهاب الكبد A — الجرعة 2",
            "هێپاتیتی A — ژەمەی 2",
            "Hepatît A — doza 2",
            false),

        v("Varicella", "جدري الماء", "مریشکانە", "Mirişkane",
            24, 2,
            "Chickenpox — dose 2",
            "لقاح جدري الماء — الجرعة 2",
            "کوتانی مریشکانە — ژەمەی 2",
            "Derziya mirişkaneyê — doza 2",
            false),

        // ── 48 months (4 years) ───────────────────────────────────────────
        v("DTP booster", "معزز الدفتيريا والتيتانوس والسعال الديكي", "بەهێزکەرەوەی DTP", "Xurtkirina DTP",
            48, 2,
            "Diphtheria, Tetanus, Pertussis booster — pre-school",
            "معزز الدفتيريا والتيتانوس والسعال الديكي — قبل المدرسة",
            "بەهێزکەرەوەی دیفتریا، تێتانۆس، کۆخە — پێش قوتابخانە",
            "Xurtkirina diftirî, tetanos, kuxeyê — beriya dibistanê",
            true),

        v("OPV", "شلل الأطفال (فموي)", "فلیجی منداڵان (دەمی)", "Felcbûna zarokan (devkî)",
            48, 6,
            "Oral Polio Vaccine — pre-school booster",
            "لقاح شلل الأطفال الفموي — جرعة تعزيز ما قبل المدرسة",
            "کوتانی فلیجی منداڵان بە دەم — بەهێزکەرەوەی پێش قوتابخانە",
            "Derziya felcbûna zarokan bi devkî — xurtkirina beriya dibistanê",
            true),

        v("MMR", "الحصبة والنكاف والحصبة الألمانية", "سووری، قۆسەخوارەو سووری ئەڵمانی", "Sorî, Kulîlka baranê û Rubella",
            48, 3,
            "Measles, Mumps, Rubella — pre-school dose",
            "لقاح الحصبة والنكاف والحصبة الألمانية — جرعة ما قبل المدرسة",
            "کوتانی سووری، قۆسەخوارەو سووری ئەڵمانی — ژەمەی پێش قوتابخانە",
            "Derziya sorî, kulîlka baranê û rubellayê — doza beriya dibistanê",
            false)
    )
}