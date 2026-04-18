package com.example.backend_side

import com.example.backend_side.entity.*
import com.example.backend_side.repositories.*
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.time.temporal.ChronoUnit
import java.util.UUID

private val logger = KotlinLogging.logger {}

private object Routes {
    const val VACCINATION  = "health/vaccinations"
    const val GROWTH_CHART = "charts"
    const val APPOINTMENTS = "health/appointments"
    const val HEALTH       = "health/issues"
    const val VISION_MOTOR = "settings/vision-motor"
    const val HEARING      = "settings/hearing-speech"
    const val BABY_PROFILE = "baby-profile"
    const val MEMORIES     = "memories"
    const val SETTINGS     = "settings"
    const val ADD_MEASURE  = "add-measurement"
    const val ADD_APPT     = "add-appointment"
    const val FAMILY_HIST  = "settings/family-history"
    const val ILLNESSES    = "settings/child-illnesses"
}

@Component
class PushNotificationScheduler(
    private val fcmService                          : FCMService,
    private val fcmTokenRepository                  : FcmTokenRepository,
    private val notificationRepository              : NotificationRepository,
    private val babyRepository                      : BabyRepository,
    private val userRepository                      : UserRepository,
    private val vaccinationScheduleRepo             : VaccinationScheduleRepository,
    private val appointmentRepository               : AppointmentRepository,
    private val healthIssueRepository               : HealthIssueRepository,
    private val familyHistoryRepository             : FamilyHistoryRepository,
    private val growthRecordRepository              : GrowthRecordRepository,
    private val visionMotorRepository               : ChildDevelopmentVisionMotorRepository,
    private val hearingSpeechRepository             : ChildDevelopmentHearingSpeechRepository,
    private val prefsRepository                     : UserNotificationPreferencesRepository
) {

    // ── FIX: prefsCache is now a local variable per scheduler run ─────────────
    // PREVIOUS: prefsCache was a class-level mutableMapOf() that was never
    // thread-safe and persisted stale data between runs. On the next run,
    // cached prefs were detached from the Hibernate session, causing
    // LazyInitializationException when Hibernate tried to re-attach them.
    // FIX: Build a fresh Map at the start of each run and pass it to helpers.
    // This is safe because @Scheduled runs on a single scheduler thread.

    // ── FIX: @Transactional on the top-level run method ───────────────────────
    // PREVIOUS: No @Transactional on the scheduler entry point. Every lazy
    // relationship access (baby.parentUser, schedule.vaccineType, etc.) outside
    // a transaction caused LazyInitializationException.
    // FIX: @Transactional(readOnly = true) on the dispatcher keeps a session
    // open for the entire scheduler run. Individual write operations (sendAndPersist)
    // use REQUIRES_NEW so they commit independently.

    @Scheduled(fixedRate = 3_600_000L)
    @Transactional(readOnly = true)
    fun runAllNotificationChecks() {
        logger.info { "Running push notification scheduler — ${LocalDateTime.now()}" }

        // Build fresh prefs cache for this run from DB — no detached entities
        val allUserIds  = babyRepository.findByIsActive(true)
            .mapNotNull { it.parentUser?.userId }
            .distinct()
        val prefsCache  = prefsRepository.findAllByUserIds(allUserIds)
            .associateBy { it.userId }

        runCatching { checkVaccinationReminders(prefsCache)       }.onFailure { logger.error(it) { "Vaccination check failed" } }
        runCatching { checkAppointmentReminders(prefsCache)       }.onFailure { logger.error(it) { "Appointment check failed" } }
        runCatching { checkMonthlyMeasurementReminder(prefsCache) }.onFailure { logger.error(it) { "Measurement check failed" } }
        runCatching { checkBabyMilestones(prefsCache)             }.onFailure { logger.error(it) { "Milestone check failed" } }
        runCatching { checkHealthIssueFollowUps(prefsCache)       }.onFailure { logger.error(it) { "Health issue check failed" } }
        runCatching { checkMissingFamilyHistory(prefsCache)       }.onFailure { logger.error(it) { "Family history check failed" } }
        runCatching { checkDevelopmentAssessments(prefsCache)     }.onFailure { logger.error(it) { "Development check failed" } }
        runCatching { checkAutoArchiveWarning(prefsCache)         }.onFailure { logger.error(it) { "Archive check failed" } }
        runCatching { processPendingScheduledNotifs(prefsCache)   }.onFailure { logger.error(it) { "Pending notifs failed" } }
    }

    // ── Prefs helpers ─────────────────────────────────────────────────────────

    private fun userWants(userId: String, category: String, prefsCache: Map<String, UserNotificationPreferences>): Boolean {
        val prefs = prefsCache[userId] ?: return true  // default = allow if no prefs row yet
        return when (category.uppercase()) {
            "VACCINATION"  -> prefs.vaccination
            "GROWTH"       -> prefs.growth
            "APPOINTMENT"  -> prefs.appointment
            "HEALTH"       -> prefs.health
            "DEVELOPMENT"  -> prefs.development
            "BABY_PROFILE" -> prefs.milestones
            "MEMORIES"     -> prefs.general
            "ACCOUNT"      -> prefs.general
            else           -> prefs.general
        }
    }

    private fun reminderDays(userId: String, prefsCache: Map<String, UserNotificationPreferences>): Int =
        prefsCache[userId]?.reminderDaysBefore ?: 3

    // ── Gender pronouns ───────────────────────────────────────────────────────

    private fun subjectPronoun(baby: Baby): String =
        if (baby.gender == Gender.GIRL) "She" else "He"

    private fun possessivePronoun(baby: Baby): String =
        if (baby.gender == Gender.GIRL) "her" else "his"

    private fun objectPronoun(baby: Baby): String =
        if (baby.gender == Gender.GIRL) "her" else "him"

    // ── Vaccination reminders ─────────────────────────────────────────────────

    private fun checkVaccinationReminders(prefsCache: Map<String, UserNotificationPreferences>) {
        val today = LocalDate.now()
        val allUpcoming = vaccinationScheduleRepo.findAllByStatusIn(
            listOf(ScheduleStatus.UPCOMING, ScheduleStatus.DUE_SOON),
            today, today.plusDays(30)
        )
        val allOverdue = vaccinationScheduleRepo.findAllByStatus(ScheduleStatus.OVERDUE)

        (allUpcoming + allOverdue).forEach { schedule ->
            // FIX: Access lazy relations inside the @Transactional session — safe here
            val baby     = schedule.baby ?: return@forEach
            val userId   = baby.parentUser?.userId ?: return@forEach
            if (!userWants(userId, "VACCINATION", prefsCache)) return@forEach

            val babyName = baby.fullName
            val vacName  = schedule.vaccineType?.vaccineName ?: return@forEach
            val daysUntil = ChronoUnit.DAYS.between(today, schedule.scheduledDate)
            val userReminderDays = reminderDays(userId, prefsCache).toLong()

            val (title, body, priority, _) = when {
                schedule.status == ScheduleStatus.OVERDUE -> {
                    val monthsOverdue = ChronoUnit.MONTHS.between(schedule.scheduledDate, today)
                    if (monthsOverdue > 2) {
                        Quad("Vaccination missed — window closed ⚠️",
                            "$babyName's $vacName dose cannot be rescheduled. Please consult a doctor.",
                            "URGENT", Routes.VACCINATION)
                    } else {
                        Quad("Vaccination overdue 💉",
                            "$babyName's $vacName is overdue! Tap to reschedule your visit.",
                            "URGENT", Routes.VACCINATION)
                    }
                }
                daysUntil == 0L -> Quad("Vaccination due TODAY 💉",
                    "$babyName's $vacName vaccine is due today. Visit your assigned health center.",
                    "URGENT", Routes.VACCINATION)
                daysUntil == 1L -> Quad("Vaccination tomorrow 💉",
                    "$babyName's $vacName vaccine is tomorrow. Make sure your visit is planned.",
                    "HIGH", Routes.VACCINATION)
                daysUntil == userReminderDays -> Quad("Vaccination in $daysUntil days 💉",
                    "$babyName's $vacName is scheduled in $daysUntil days on ${schedule.scheduledDate}.",
                    "MEDIUM", Routes.VACCINATION)
                else -> return@forEach
            }

            val dedupeKey = "vacc_${schedule.scheduleId}_${daysUntil}"
            if (!alreadySentToday(userId, dedupeKey)) {
                sendAndPersist(userId, baby.babyId, babyName, title, body,
                    "VACCINATION", priority, Routes.VACCINATION, "View schedule", Routes.VACCINATION, dedupeKey)
            }
        }
    }

    // ── Appointment reminders ─────────────────────────────────────────────────

    private fun checkAppointmentReminders(prefsCache: Map<String, UserNotificationPreferences>) {
        val now   = LocalDateTime.now()
        val today = LocalDate.now()

        appointmentRepository
            .findByScheduledDateBetween(today, today.plusDays(8))
            .filter { it.status == AppointmentStatus.SCHEDULED || it.status == AppointmentStatus.CONFIRMED }
            .forEach { appointment ->
                val baby   = appointment.baby ?: return@forEach
                val userId = baby.parentUser?.userId ?: return@forEach
                if (!userWants(userId, "APPOINTMENT", prefsCache)) return@forEach

                val babyName = baby.fullName
                val typeName = appointment.appointmentType?.name
                    ?.replace("_", " ")?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Appointment"

                val daysUntil    = ChronoUnit.DAYS.between(today, appointment.scheduledDate)
                val apptDateTime = appointment.scheduledDate.atTime(
                    appointment.scheduledTime ?: java.time.LocalTime.of(9, 0))
                val hoursUntil   = ChronoUnit.HOURS.between(now, apptDateTime)
                val userReminder = reminderDays(userId, prefsCache).toLong()

                when {
                    daysUntil == userReminder && !alreadySentToday(userId, "appt_rd_${appointment.appointmentId}") ->
                        sendAndPersist(userId, baby.babyId, babyName,
                            "Appointment in $daysUntil days 📅",
                            "$babyName has a $typeName in $daysUntil days${appointment.doctorName?.let { " with $it" } ?: ""}.",
                            "APPOINTMENT", "MEDIUM", Routes.APPOINTMENTS, "View appointment",
                            dedupeKey = "appt_rd_${appointment.appointmentId}")
                    daysUntil == 1L && !alreadySentToday(userId, "appt_1d_${appointment.appointmentId}") ->
                        sendAndPersist(userId, baby.babyId, babyName,
                            "Appointment tomorrow 📅",
                            "$babyName's $typeName is tomorrow${appointment.scheduledTime?.let { " at $it" } ?: ""}.",
                            "APPOINTMENT", "HIGH", Routes.APPOINTMENTS, "View details",
                            dedupeKey = "appt_1d_${appointment.appointmentId}")
                    hoursUntil in 1..2 && !alreadySentToday(userId, "appt_2h_${appointment.appointmentId}") ->
                        sendAndPersist(userId, baby.babyId, babyName,
                            "Appointment in 2 hours ⏰",
                            "$babyName's $typeName starts in 2 hours.",
                            "APPOINTMENT", "HIGH", Routes.APPOINTMENTS, "Get directions",
                            dedupeKey = "appt_2h_${appointment.appointmentId}")
                }
            }
    }

    // ── Monthly measurement reminder ──────────────────────────────────────────

    private fun checkMonthlyMeasurementReminder(prefsCache: Map<String, UserNotificationPreferences>) {
        val today        = LocalDate.now()
        val startOfMonth = today.withDayOfMonth(1)
        babyRepository.findByIsActive(true).forEach { baby ->
            val userId = baby.parentUser?.userId ?: return@forEach
            if (!userWants(userId, "GROWTH", prefsCache)) return@forEach
            val ageMonths = Period.between(baby.dateOfBirth, today).toTotalMonths().toInt()
            val hasThisMonth = growthRecordRepository
                .existsByBaby_BabyIdAndMeasurementDateBetween(baby.babyId, startOfMonth, today)
            if (!hasThisMonth && today.dayOfMonth >= 5 &&
                !alreadySentToday(userId, "monthly_measure_${baby.babyId}")) {
                sendAndPersist(userId, baby.babyId, baby.fullName,
                    "Monthly measurement due 📏",
                    "Time to measure ${baby.fullName}! ${subjectPronoun(baby)}'s $ageMonths months old. " +
                            "Log ${possessivePronoun(baby)} weight, height, and head circumference.",
                    "GROWTH", "MEDIUM", Routes.ADD_MEASURE, "Add measurement",
                    dedupeKey = "monthly_measure_${baby.babyId}")
            }
        }
    }

    // ── Baby milestones ───────────────────────────────────────────────────────

    private fun checkBabyMilestones(prefsCache: Map<String, UserNotificationPreferences>) {
        val today      = LocalDate.now()
        val milestones = listOf(1, 3, 6, 12, 18, 24)
        babyRepository.findByIsActive(true).forEach { baby ->
            val userId = baby.parentUser?.userId ?: return@forEach
            if (!userWants(userId, "BABY_PROFILE", prefsCache)) return@forEach
            val ageMonths = Period.between(baby.dateOfBirth, today).toTotalMonths().toInt()
            if (ageMonths !in milestones) return@forEach
            if (alreadySentThisMonth(userId, "milestone_${baby.babyId}_$ageMonths")) return@forEach

            val (title, body) = when (ageMonths) {
                1  -> "1 month old! 🎉"       to "${baby.fullName} is 1 month old today!"
                3  -> "3 months old! 🎉"       to "${baby.fullName} is 3 months old!"
                6  -> "6 months old! 🎉"       to
                        "${baby.fullName} is 6 months old — halfway through ${possessivePronoun(baby)} first year!"
                12 -> "Happy 1st birthday! 🎂" to "Happy first birthday, ${baby.fullName}!"
                18 -> "18 months old! 🌟"      to
                        "${baby.fullName} is 18 months! Check ${possessivePronoun(baby)} hearing and speech development."
                24 -> "2 years old! 🎉"        to "${baby.fullName} is 2 years old!"
                else -> return@forEach
            }
            sendAndPersist(userId, baby.babyId, baby.fullName, title, body,
                "BABY_PROFILE", if (ageMonths == 12) "HIGH" else "MEDIUM",
                Routes.BABY_PROFILE, "View profile",
                dedupeKey = "milestone_${baby.babyId}_$ageMonths")
        }
    }

    // ── Health issue follow-ups ───────────────────────────────────────────────

    private fun checkHealthIssueFollowUps(prefsCache: Map<String, UserNotificationPreferences>) {
        val today  = LocalDate.now()
        val cutoff = today.minusDays(14)
        healthIssueRepository.findOngoingBefore(isResolved = false, before = cutoff)
            .forEach { issue ->
                val baby   = issue.baby ?: return@forEach
                val userId = baby.parentUser?.userId ?: return@forEach
                if (!userWants(userId, "HEALTH", prefsCache)) return@forEach
                val babyName = baby.fullName
                val key = "health_followup_${issue.issueId}"
                if (!alreadySentToday(userId, key)) {
                    sendAndPersist(userId, baby.babyId, babyName,
                        "Health issue ongoing ❤️",
                        "$babyName's ${issue.title} has been ongoing for 14+ days.",
                        "HEALTH", "HIGH", Routes.HEALTH, "Book appointment", Routes.ADD_APPT, key)
                }
            }
    }

    // ── Missing family history ────────────────────────────────────────────────

    private fun checkMissingFamilyHistory(prefsCache: Map<String, UserNotificationPreferences>) {
        val today = LocalDate.now()
        babyRepository.findByIsActive(true).forEach { baby ->
            val userId = baby.parentUser?.userId ?: return@forEach
            if (!userWants(userId, "HEALTH", prefsCache)) return@forEach
            val ageDays = ChronoUnit.DAYS.between(baby.dateOfBirth, today)
            if (ageDays < 30) return@forEach
            val hasHistory = familyHistoryRepository.existsByBaby_BabyId(baby.babyId)
            if (!hasHistory && !alreadySentThisMonth(userId, "fam_history_${baby.babyId}")) {
                sendAndPersist(userId, baby.babyId, baby.fullName,
                    "Add family health history 🏥",
                    "You haven't added family health history for ${baby.fullName} yet.",
                    "HEALTH", "LOW", Routes.FAMILY_HIST, "Add now",
                    dedupeKey = "fam_history_${baby.babyId}")
            }
        }
    }

    // ── Development assessments ───────────────────────────────────────────────

    private fun checkDevelopmentAssessments(prefsCache: Map<String, UserNotificationPreferences>) {
        val today      = LocalDate.now()
        val milestones = listOf(1, 3, 6, 9, 12)
        babyRepository.findByIsActive(true).forEach { baby ->
            val userId = baby.parentUser?.userId ?: return@forEach
            if (!userWants(userId, "DEVELOPMENT", prefsCache)) return@forEach
            val ageMonths = Period.between(baby.dateOfBirth, today).toTotalMonths().toInt()
            if (ageMonths !in milestones) return@forEach

            val hasVision  = visionMotorRepository.existsByBaby_BabyIdAndCheckMonth(baby.babyId, ageMonths)
            val hasHearing = hearingSpeechRepository.existsByBaby_BabyIdAndCheckMonth(baby.babyId, ageMonths)

            if (!hasVision && !alreadySentThisMonth(userId, "dev_vision_${baby.babyId}_$ageMonths")) {
                sendAndPersist(userId, baby.babyId, baby.fullName,
                    "Vision & motor assessment due 🧠",
                    "Time to assess ${baby.fullName}'s vision and motor development at $ageMonths months.",
                    "DEVELOPMENT", "MEDIUM", Routes.VISION_MOTOR, "Record now",
                    dedupeKey = "dev_vision_${baby.babyId}_$ageMonths")
            }
            if (!hasHearing && !alreadySentThisMonth(userId, "dev_hearing_${baby.babyId}_$ageMonths")) {
                sendAndPersist(userId, baby.babyId, baby.fullName,
                    "Hearing & speech assessment due 👂",
                    "Time to record ${baby.fullName}'s hearing and speech milestones at $ageMonths months.",
                    "DEVELOPMENT", "MEDIUM", Routes.HEARING, "Record now",
                    dedupeKey = "dev_hearing_${baby.babyId}_$ageMonths")
            }
        }
    }

    // ── Auto-archive warning ──────────────────────────────────────────────────

    private fun checkAutoArchiveWarning(prefsCache: Map<String, UserNotificationPreferences>) {
        val today = LocalDate.now()
        babyRepository.findByIsActive(true).forEach { baby ->
            val userId = baby.parentUser?.userId ?: return@forEach
            val ageMonths = Period.between(baby.dateOfBirth, today).toTotalMonths().toInt()
            if (ageMonths >= 72 && !alreadySentThisMonth(userId, "archive_warn_${baby.babyId}")) {
                sendAndPersist(userId, baby.babyId, baby.fullName,
                    "${baby.fullName} is now 6 years old 👶",
                    "${baby.fullName}'s profile has been automatically archived.",
                    "BABY_PROFILE", "LOW", Routes.BABY_PROFILE,
                    dedupeKey = "archive_warn_${baby.babyId}")
            }
        }
    }

    // ── Process pending scheduled notifications ───────────────────────────────

    private fun processPendingScheduledNotifs(prefsCache: Map<String, UserNotificationPreferences>) {
        val pending = notificationRepository.findPendingNotificationsToSend(LocalDateTime.now())
        pending.forEach { notif ->
            val userId   = notif.user?.userId ?: return@forEach
            val category = notif.notificationType?.name ?: "GENERAL"

            if (!userWants(userId, category, prefsCache)) {
                logger.debug { "Skipping scheduled notification for $userId — category $category disabled by user" }
                return@forEach
            }

            val tokens = fcmTokenRepository.findByUserIdAndIsActive(userId, true)
            tokens.forEach { token ->
                fcmService.sendToDevice(
                    fcmToken = token.token,
                    title    = notif.title,
                    body     = notif.message,
                    data     = mapOf(
                        "category" to category,
                        "priority" to (notif.priority?.name ?: NotificationPriority.MEDIUM.name)
                    ),
                    priority = notif.priority?.name ?: NotificationPriority.MEDIUM.name
                )
            }
            notif.isSent = true
            notif.sentAt = LocalDateTime.now()
            notificationRepository.save(notif)
        }
    }

    // ── Shared send + persist helper ──────────────────────────────────────────
    // FIX: @Transactional(propagation = REQUIRES_NEW) — each notification is
    // committed independently so a failure on one does not roll back others.
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    fun sendAndPersist(
        userId     : String,
        babyId     : String?,
        babyName   : String?,
        title      : String,
        body       : String,
        category   : String,
        priority   : String,
        deepLink   : String? = null,
        actionLabel: String? = null,
        actionRoute: String? = null,
        dedupeKey  : String? = null
    ) {
        val tokens = fcmTokenRepository.findByUserIdAndIsActive(userId, true)
        val data = buildMap<String, String> {
            put("category", category)
            put("priority", priority)
            babyId?.let    { put("babyId", it) }
            babyName?.let  { put("babyName", it) }
            deepLink?.let  { put("deepLinkRoute", it) }
            actionLabel?.let { put("actionLabel", it) }
            actionRoute?.let { put("actionRoute", it) }
        }
        tokens.forEach { fcmService.sendToDevice(it.token, title, body, data, priority = priority) }

        val user = userRepository.findById(userId).orElse(null) ?: return
        val baby = babyId?.let { babyRepository.findById(it).orElse(null) }
        notificationRepository.save(Notification(
            notificationId   = UUID.randomUUID().toString(),
            user             = user,
            baby             = baby,
            title            = title,
            message          = body,
            dedupeKey        = dedupeKey,
            notificationType = runCatching {
                NotificationType.valueOf(category.uppercase())
            }.getOrDefault(NotificationType.GENERAL),
            priority         = runCatching {
                NotificationPriority.valueOf(priority.uppercase())
            }.getOrDefault(NotificationPriority.MEDIUM),
            isSent    = tokens.isNotEmpty(),
            sentAt    = if (tokens.isNotEmpty()) LocalDateTime.now() else null,
            createdAt = LocalDateTime.now()
        ))
        logger.info { "Notification sent: [$category/$priority] '$title' → $userId" }
    }

    // ── Dedupe helpers ────────────────────────────────────────────────────────

    private fun alreadySentToday(userId: String, key: String) =
        notificationRepository.existsSentNotificationAfter(userId, key, LocalDate.now().atStartOfDay())

    private fun alreadySentThisMonth(userId: String, key: String) =
        notificationRepository.existsSentNotificationAfter(
            userId, key, LocalDate.now().withDayOfMonth(1).atStartOfDay())
}

private data class Quad(val a: String, val b: String, val c: String, val d: String)
private operator fun Quad.component1() = a
private operator fun Quad.component2() = b
private operator fun Quad.component3() = c
private operator fun Quad.component4() = d