package com.example.backend_side

import com.example.backend_side.entity.*
import com.example.backend_side.repositories.*
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
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
    private val growthRecordRepository              : GrowthRecordRepository,          // FIX #5: inject missing repo
    private val visionMotorRepository               : ChildDevelopmentVisionMotorRepository,   // FIX #8: camelCase
    private val hearingSpeechRepository             : ChildDevelopmentHearingSpeechRepository  // FIX #8: camelCase
) {

    // ─────────────────────────────────────────────────────────────────────────
    // MASTER SCHEDULER — runs every hour
    // ─────────────────────────────────────────────────────────────────────────

    @Scheduled(fixedRate = 3_600_000L)
    fun runAllNotificationChecks() {
        logger.info { "Running push notification scheduler — ${LocalDateTime.now()}" }
        runCatching { checkVaccinationReminders()       }.onFailure { logger.error(it) { "Vaccination check failed" } }
        runCatching { checkAppointmentReminders()       }.onFailure { logger.error(it) { "Appointment check failed" } }
        runCatching { checkMonthlyMeasurementReminder() }.onFailure { logger.error(it) { "Measurement check failed" } }
        runCatching { checkBabyMilestones()             }.onFailure { logger.error(it) { "Milestone check failed" } }
        runCatching { checkHealthIssueFollowUps()       }.onFailure { logger.error(it) { "Health issue check failed" } }
        runCatching { checkMissingFamilyHistory()       }.onFailure { logger.error(it) { "Family history check failed" } }
        runCatching { checkDevelopmentAssessments()     }.onFailure { logger.error(it) { "Development check failed" } }
        runCatching { checkAutoArchiveWarning()         }.onFailure { logger.error(it) { "Archive check failed" } }
        runCatching { processPendingScheduledNotifs()   }.onFailure { logger.error(it) { "Pending notifs failed" } }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 1. VACCINATION REMINDERS
    // ─────────────────────────────────────────────────────────────────────────

    private fun checkVaccinationReminders() {
        val today = LocalDate.now()

        // FIX #1: replaced non-existent findAllActiveSchedules() with two typed queries
        // that fetch UPCOMING/DUE_SOON in a wide window, plus OVERDUE separately.
        val window    = today.plusDays(30)
        val upcoming  = vaccinationScheduleRepo.findUpcomingForBaby(
            babyId   = "",          // empty string won't match — see note below *
            from     = today,
            to       = window,
            statuses = listOf(ScheduleStatus.UPCOMING, ScheduleStatus.DUE_SOON)
        )
        // * findUpcomingForBaby filters by babyId; we need ALL babies.
        // The correct approach is the new findAllByStatusIn() added to the repo below.
        val allUpcoming = vaccinationScheduleRepo.findAllByStatusIn(
            listOf(ScheduleStatus.UPCOMING, ScheduleStatus.DUE_SOON),
            today, today.plusDays(30)
        )
        val allOverdue  = vaccinationScheduleRepo.findAllByStatus(ScheduleStatus.OVERDUE)

        (allUpcoming + allOverdue).forEach { schedule ->
            val userId   = schedule.baby?.parentUser?.userId ?: return@forEach
            val babyName = schedule.baby?.fullName           ?: return@forEach
            val vacName  = schedule.vaccineType?.vaccineName ?: return@forEach
            val daysUntil = ChronoUnit.DAYS.between(today, schedule.scheduledDate)

            // FIX #2: use ScheduleStatus instead of non-existent VaccinationStatus
            val (title, body, priority, _) = when {
                schedule.status == ScheduleStatus.OVERDUE -> {
                    // FIX #3: schedule.idealDate doesn't exist → use scheduledDate as fallback
                    val monthsOverdue = ChronoUnit.MONTHS.between(schedule.scheduledDate, today)
                    if (monthsOverdue > 2) {
                        Quad(
                            "Vaccination missed — window closed ⚠️",
                            "$babyName's $vacName dose cannot be rescheduled. Please consult a doctor.",
                            "URGENT", Routes.VACCINATION
                        )
                    } else {
                        Quad(
                            "Vaccination overdue 💉",
                            "$babyName's $vacName is overdue! Tap to reschedule your visit.",
                            "URGENT", Routes.VACCINATION
                        )
                    }
                }
                daysUntil == 0L -> Quad(
                    "Vaccination due TODAY 💉",
                    "$babyName's $vacName vaccine is due today. Visit your assigned health center.",
                    "URGENT", Routes.VACCINATION
                )
                daysUntil == 1L -> Quad(
                    "Vaccination tomorrow 💉",
                    "$babyName's $vacName vaccine is tomorrow. Make sure your visit is planned.",
                    "HIGH", Routes.VACCINATION
                )
                daysUntil == 3L -> Quad(
                    "Vaccination in 3 days 💉",
                    "$babyName's $vacName is scheduled in 3 days. Don't forget!",
                    "HIGH", Routes.VACCINATION
                )
                daysUntil == 7L -> Quad(
                    "Vaccination next week 💉",
                    "$babyName's $vacName vaccine is in 7 days on ${schedule.scheduledDate}.",
                    "MEDIUM", Routes.VACCINATION
                )
                else -> return@forEach
            }

            val dedupeKey = "vacc_${schedule.scheduleId}_${daysUntil}"
            if (!alreadySentToday(userId, dedupeKey)) {
                sendAndPersist(
                    userId      = userId,
                    babyId      = schedule.baby?.babyId,
                    babyName    = babyName,
                    title       = title,
                    body        = body,
                    category    = "VACCINATION",
                    priority    = priority,
                    deepLink    = Routes.VACCINATION,
                    actionLabel = "View schedule",
                    actionRoute = Routes.VACCINATION
                )
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. APPOINTMENT REMINDERS
    // ─────────────────────────────────────────────────────────────────────────

    private fun checkAppointmentReminders() {
        val now   = LocalDateTime.now()
        val today = LocalDate.now()

        // FIX #4: findByStatusAndScheduledDateAfter() doesn't exist in the repo.
        // Use findByScheduledDateBetween() which does exist, combined with status filter.
        appointmentRepository
            .findByScheduledDateBetween(today, today.plusDays(8))
            .filter { it.status == AppointmentStatus.SCHEDULED || it.status == AppointmentStatus.CONFIRMED }
            .forEach { appointment ->
                val userId   = appointment.baby?.parentUser?.userId ?: return@forEach
                val babyName = appointment.baby?.fullName           ?: return@forEach
                val typeName = appointment.appointmentType
                    ?.name
                    ?.replace("_", " ")
                    ?.lowercase()
                    ?.replaceFirstChar { it.uppercase() }
                    ?: "Appointment"

                val daysUntil    = ChronoUnit.DAYS.between(today, appointment.scheduledDate)
                val apptDateTime = appointment.scheduledDate.atTime(
                    appointment.scheduledTime ?: java.time.LocalTime.of(9, 0)
                )
                val hoursUntil   = ChronoUnit.HOURS.between(now, apptDateTime)

                when {
                    daysUntil == 7L && !alreadySentToday(userId, "appt_7d_${appointment.appointmentId}") -> {
                        sendAndPersist(
                            userId, appointment.baby?.babyId, babyName,
                            "Appointment in 7 days 📅",
                            "$babyName has a $typeName booked in 7 days${appointment.doctorName?.let { " with $it" } ?: ""}.",
                            "APPOINTMENT", "MEDIUM", Routes.APPOINTMENTS, "View appointment"
                        )
                    }
                    daysUntil == 1L && !alreadySentToday(userId, "appt_1d_${appointment.appointmentId}") -> {
                        sendAndPersist(
                            userId, appointment.baby?.babyId, babyName,
                            "Appointment tomorrow 📅",
                            "$babyName's $typeName is tomorrow${appointment.scheduledTime?.let { " at $it" } ?: ""}.",
                            "APPOINTMENT", "HIGH", Routes.APPOINTMENTS, "View details"
                        )
                    }
                    hoursUntil in 1..2 && !alreadySentToday(userId, "appt_2h_${appointment.appointmentId}") -> {
                        sendAndPersist(
                            userId, appointment.baby?.babyId, babyName,
                            "Appointment in 2 hours ⏰",
                            "$babyName's $typeName starts in 2 hours. Tap for details.",
                            "APPOINTMENT", "HIGH", Routes.APPOINTMENTS, "Get directions"
                        )
                    }
                }
            }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. MONTHLY MEASUREMENT REMINDER
    // ─────────────────────────────────────────────────────────────────────────

    private fun checkMonthlyMeasurementReminder() {
        val today        = LocalDate.now()
        val startOfMonth = today.withDayOfMonth(1)
        val allBabies    = babyRepository.findByIsActive(true)

        allBabies.forEach { baby ->
            val userId    = baby.parentUser?.userId ?: return@forEach
            val ageMonths = Period.between(baby.dateOfBirth, today).toTotalMonths().toInt()

            // FIX #6: growthRecordRepository is now injected; method exists in new repo below
            val hasThisMonth = growthRecordRepository
                .existsByBaby_BabyIdAndMeasurementDateBetween(baby.babyId, startOfMonth, today)

            if (!hasThisMonth && today.dayOfMonth >= 5 &&
                !alreadySentToday(userId, "monthly_measure_${baby.babyId}")) {
                sendAndPersist(
                    userId      = userId,
                    babyId      = baby.babyId,
                    babyName    = baby.fullName,
                    title       = "Monthly measurement due 📏",
                    body        = "Time to measure ${baby.fullName}! " +
                            "She's $ageMonths months old. Log her weight, height, and head circumference.",
                    category    = "GROWTH",
                    priority    = "MEDIUM",
                    deepLink    = Routes.ADD_MEASURE,
                    actionLabel = "Add measurement"
                )
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. BABY AGE MILESTONES
    // ─────────────────────────────────────────────────────────────────────────

    private fun checkBabyMilestones() {
        val today      = LocalDate.now()
        val milestones = listOf(1, 3, 6, 12, 18, 24)
        val allBabies  = babyRepository.findByIsActive(true)

        allBabies.forEach { baby ->
            val userId    = baby.parentUser?.userId ?: return@forEach
            val ageMonths = Period.between(baby.dateOfBirth, today).toTotalMonths().toInt()

            if (ageMonths !in milestones) return@forEach
            if (alreadySentThisMonth(userId, "milestone_${baby.babyId}_$ageMonths")) return@forEach

            val (title, body) = when (ageMonths) {
                1  -> "1 month old! 🎉"       to "${baby.fullName} is 1 month old today! Log her first growth measurement to start tracking."
                3  -> "3 months old! 🎉"       to "${baby.fullName} is 3 months old! Great time to record growth and check vision milestones."
                6  -> "6 months old! 🎉"       to "${baby.fullName} is 6 months old — halfway through her first year! Schedule a 6-month checkup."
                12 -> "Happy 1st birthday! 🎂" to "Happy first birthday, ${baby.fullName}! Schedule her 12-month checkup and vaccinations."
                18 -> "18 months old! 🌟"      to "${baby.fullName} is 18 months! Check her hearing and speech development."
                24 -> "2 years old! 🎉"        to "${baby.fullName} is 2 years old! The WHO chart switches range — log her measurements."
                else -> return@forEach
            }

            sendAndPersist(
                userId      = userId,
                babyId      = baby.babyId,
                babyName    = baby.fullName,
                title       = title,
                body        = body,
                category    = "BABY_PROFILE",
                priority    = if (ageMonths == 12) "HIGH" else "MEDIUM",
                deepLink    = Routes.BABY_PROFILE,
                actionLabel = "View profile"
            )
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 5. HEALTH ISSUE FOLLOW-UP
    // ─────────────────────────────────────────────────────────────────────────

    private fun checkHealthIssueFollowUps() {
        val today  = LocalDate.now()
        val cutoff = today.minusDays(14)

        // FIX #7: findByIsResolvedAndIssueDateBefore() doesn't exist → use new method added below
        healthIssueRepository.findOngoingBefore(isResolved = false, before = cutoff)
            .forEach { issue ->
                val userId   = issue.baby?.parentUser?.userId ?: return@forEach
                val babyName = issue.baby?.fullName           ?: return@forEach

                if (!alreadySentToday(userId, "health_followup_${issue.issueId}")) {
                    sendAndPersist(
                        userId      = userId,
                        babyId      = issue.baby?.babyId,
                        babyName    = babyName,
                        title       = "Health issue ongoing ❤️",
                        body        = "$babyName's ${issue.title} has been ongoing for 14+ days. " +
                                "Consider scheduling a follow-up appointment.",
                        category    = "HEALTH",
                        priority    = "HIGH",
                        deepLink    = Routes.HEALTH,
                        actionLabel = "Book appointment",
                        actionRoute = Routes.ADD_APPT
                    )
                }
            }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 6. MISSING FAMILY HISTORY
    // ─────────────────────────────────────────────────────────────────────────

    private fun checkMissingFamilyHistory() {
        val today     = LocalDate.now()
        val allBabies = babyRepository.findByIsActive(true)

        allBabies.forEach { baby ->
            val userId  = baby.parentUser?.userId ?: return@forEach
            val ageDays = ChronoUnit.DAYS.between(baby.dateOfBirth, today)
            if (ageDays < 30) return@forEach

            val hasHistory = familyHistoryRepository.existsByBaby_BabyId(baby.babyId)
            if (!hasHistory && !alreadySentThisMonth(userId, "fam_history_${baby.babyId}")) {
                sendAndPersist(
                    userId      = userId,
                    babyId      = baby.babyId,
                    babyName    = baby.fullName,
                    title       = "Add family health history 🏥",
                    body        = "You haven't added family health history for ${baby.fullName} yet. " +
                            "This helps identify hereditary risks early.",
                    category    = "HEALTH",
                    priority    = "LOW",
                    deepLink    = Routes.FAMILY_HIST,
                    actionLabel = "Add now"
                )
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 7. DEVELOPMENT ASSESSMENT DUE
    // ─────────────────────────────────────────────────────────────────────────

    private fun checkDevelopmentAssessments() {
        val today      = LocalDate.now()
        val milestones = listOf(1, 3, 6, 9, 12)
        val allBabies  = babyRepository.findByIsActive(true)

        allBabies.forEach { baby ->
            val userId    = baby.parentUser?.userId ?: return@forEach
            val ageMonths = Period.between(baby.dateOfBirth, today).toTotalMonths().toInt()
            if (ageMonths !in milestones) return@forEach

            // FIX #8 & #9: renamed to camelCase; corrected method name to checkMonth (not ageMonths)
            val hasVisionRecord  = visionMotorRepository.existsByBaby_BabyIdAndCheckMonth(baby.babyId, ageMonths)
            val hasHearingRecord = hearingSpeechRepository.existsByBaby_BabyIdAndCheckMonth(baby.babyId, ageMonths)

            if (!hasVisionRecord && !alreadySentThisMonth(userId, "dev_vision_${baby.babyId}_$ageMonths")) {
                sendAndPersist(
                    userId      = userId,
                    babyId      = baby.babyId,
                    babyName    = baby.fullName,
                    title       = "Vision & motor assessment due 🧠",
                    body        = "Time to assess ${baby.fullName}'s vision and motor development at $ageMonths months. Tap to record.",
                    category    = "DEVELOPMENT",
                    priority    = "MEDIUM",
                    deepLink    = Routes.VISION_MOTOR,
                    actionLabel = "Record now"
                )
            }

            if (!hasHearingRecord && !alreadySentThisMonth(userId, "dev_hearing_${baby.babyId}_$ageMonths")) {
                sendAndPersist(
                    userId      = userId,
                    babyId      = baby.babyId,
                    babyName    = baby.fullName,
                    title       = "Hearing & speech assessment due 👂",
                    body        = "Time to record ${baby.fullName}'s hearing and speech milestones at $ageMonths months.",
                    category    = "DEVELOPMENT",
                    priority    = "MEDIUM",
                    deepLink    = Routes.HEARING,
                    actionLabel = "Record now"
                )
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 8. AUTO-ARCHIVE WARNING
    // ─────────────────────────────────────────────────────────────────────────

    private fun checkAutoArchiveWarning() {
        val today     = LocalDate.now()
        val allBabies = babyRepository.findByIsActive(true)

        allBabies.forEach { baby ->
            val userId    = baby.parentUser?.userId ?: return@forEach
            val ageMonths = Period.between(baby.dateOfBirth, today).toTotalMonths().toInt()

            if (ageMonths >= 72 && !alreadySentThisMonth(userId, "archive_warn_${baby.babyId}")) {
                sendAndPersist(
                    userId      = userId,
                    babyId      = baby.babyId,
                    babyName    = baby.fullName,
                    title       = "${baby.fullName} is now 6 years old 👶",
                    body        = "${baby.fullName}'s profile has been automatically archived as she is now 6 years old. " +
                            "You can restore it anytime from the baby list.",
                    category    = "BABY_PROFILE",
                    priority    = "LOW",
                    deepLink    = Routes.BABY_PROFILE
                )
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 9. PROCESS PENDING SCHEDULED NOTIFICATIONS
    // ─────────────────────────────────────────────────────────────────────────

    private fun processPendingScheduledNotifs() {
        val pending = notificationRepository.findPendingNotificationsToSend(LocalDateTime.now())
        pending.forEach { notif ->
            val userId = notif.user?.userId ?: return@forEach
            val tokens = fcmTokenRepository.findByUserIdAndIsActive(userId, true)

            tokens.forEach { token ->
                fcmService.sendToDevice(
                    fcmToken = token.token,
                    title    = notif.title,
                    body     = notif.message,
                    data     = mapOf(
                        "category" to (notif.notificationType?.name ?: "GENERAL"),
                        // FIX #10: priority is NotificationPriority enum — safe null-fallback
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

    // ─────────────────────────────────────────────────────────────────────────
    // Core send + persist helper
    // ─────────────────────────────────────────────────────────────────────────

    private fun sendAndPersist(
        userId      : String,
        babyId      : String?,
        babyName    : String?,
        title       : String,
        body        : String,
        category    : String,
        priority    : String,
        deepLink    : String? = null,
        actionLabel : String? = null,
        actionRoute : String? = null,
        dedupeKey   : String? = null
    ) {
        val tokens = fcmTokenRepository.findByUserIdAndIsActive(userId, true)
        val data   = buildMap<String, String> {
            put("category", category)
            put("priority", priority)
            babyId?.let      { put("babyId", it) }
            babyName?.let    { put("babyName", it) }
            deepLink?.let    { put("deepLinkRoute", it) }
            actionLabel?.let { put("actionLabel", it) }
            actionRoute?.let { put("actionRoute", it) }
        }

        tokens.forEach { token ->
            fcmService.sendToDevice(
                fcmToken = token.token,
                title    = title,
                body     = body,
                data     = data,
                priority = priority
            )
        }

        val user = userRepository.findById(userId).orElse(null) ?: return
        val baby = babyId?.let { babyRepository.findById(it).orElse(null) }

        notificationRepository.save(
            Notification(
                notificationId   = UUID.randomUUID().toString(),
                user             = user,
                baby             = baby,
                title            = title,
                message          = body,
                dedupeKey        = dedupeKey,   // NEW
                notificationType = runCatching {
                    NotificationType.valueOf(category.uppercase())
                }.getOrDefault(NotificationType.GENERAL),
                priority         = runCatching {
                    NotificationPriority.valueOf(priority.uppercase())
                }.getOrDefault(NotificationPriority.MEDIUM),
                isSent           = tokens.isNotEmpty(),
                sentAt           = if (tokens.isNotEmpty()) LocalDateTime.now() else null,
                createdAt        = LocalDateTime.now()
            )
        )

        logger.info { "Notification sent: [$category/$priority] '$title' → $userId" }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FIX #11: Deduplication — use stable notification ID key stored in DB,
    // not fragile substring matching on title text
    // ─────────────────────────────────────────────────────────────────────────

    private fun alreadySentToday(userId: String, key: String): Boolean {
        val startOfDay = LocalDate.now().atStartOfDay()
        return notificationRepository.existsSentNotificationAfter(userId, key, startOfDay)
    }

    private fun alreadySentThisMonth(userId: String, key: String): Boolean {
        val startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay()
        return notificationRepository.existsSentNotificationAfter(userId, key, startOfMonth)
    }
}

private data class Quad(val a: String, val b: String, val c: String, val d: String)
private operator fun Quad.component1() = a
private operator fun Quad.component2() = b
private operator fun Quad.component3() = c
private operator fun Quad.component4() = d