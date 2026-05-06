package com.example.backend_side

import com.example.backend_side.entity.*
import com.example.backend_side.repositories.*
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.util.UUID

private val logger = KotlinLogging.logger {}

private const val MAX_OVERSHOOT_MONTHS = 2

private val DAY_NAME_MAP_RS = mapOf(
    "Sunday"    to DayOfWeek.SUNDAY,
    "Monday"    to DayOfWeek.MONDAY,
    "Tuesday"   to DayOfWeek.TUESDAY,
    "Wednesday" to DayOfWeek.WEDNESDAY,
    "Thursday"  to DayOfWeek.THURSDAY,
    "Friday"    to DayOfWeek.FRIDAY,
    "Saturday"  to DayOfWeek.SATURDAY
)

interface VaccinationRescheduleService {
    fun rescheduleAll(
        babyId      : String,
        request     : VaccinationRescheduleRequest,
        doneByUserId: String? = null
    ): VaccinationRescheduleResponse
}

@Service
@Transactional
class VaccinationRescheduleServiceImpl(
    private val scheduleRepository     : VaccinationScheduleRepository,
    private val babyRepository         : BabyRepository,
    private val userRepository         : UserRepository,
    private val holidayRepository      : BenchHolidayRepository,
    private val adjustmentLogRepository: ScheduleAdjustmentLogRepository
) : VaccinationRescheduleService {

    override fun rescheduleAll(
        babyId      : String,
        request     : VaccinationRescheduleRequest,
        doneByUserId: String?
    ): VaccinationRescheduleResponse {
        logger.info { "Rescheduling all vaccinations for baby $babyId, reason=${request.shiftReason}" }

        val baby = babyRepository.findById(babyId)
            .orElseThrow { ResourceNotFoundException("Baby not found: $babyId") }

        val doneBy = doneByUserId?.let {
            userRepository.findById(it).orElse(null)
        }

        val allSchedules = scheduleRepository
            .findByBaby_BabyIdOrderByScheduledDateAsc(babyId)

        val activeBench = allSchedules.firstOrNull()?.bench
            ?: throw ResourceNotFoundException(
                "No vaccination schedule found for baby $babyId. " +
                        "Please assign a health center first."
            )

        val vaccinationDays = activeBench.vaccinationDaysList  // ✅ was activeBench.getVaccinationDaysList()
            .mapNotNull { DAY_NAME_MAP_RS[it] }
            .toSet()

        val holidayDates = loadHolidayDates(
            activeBench.benchId,
            LocalDate.now(),
            LocalDate.now().plusYears(3)
        )

        val today  = LocalDate.now()
        val results = mutableListOf<VaccinationRescheduleItemResult>()

        var rescheduledCount = 0
        var skippedCount     = 0
        var tooLateCount     = 0

        for (schedule in allSchedules) {
            val vaccineType = schedule.vaccineType ?: continue
            val vaccineName = vaccineType.vaccineName

            if (schedule.status == ScheduleStatus.COMPLETED ||
                schedule.status == ScheduleStatus.MISSED) {
                results.add(
                    VaccinationRescheduleItemResult(
                        scheduleId           = schedule.scheduleId,
                        vaccineName          = vaccineName,
                        vaccineNameAr        = vaccineType.vaccineNameAr,
                        vaccineNameKu        = vaccineType.vaccineNameKu,
                        vaccineNameCkb       = vaccineType.vaccineNameCkb,
                        recommendedAgeMonths = vaccineType.recommendedAgeMonths,
                        doseNumber           = vaccineType.doseNumber.toInt(),
                        oldScheduledDate     = schedule.scheduledDate,
                        newScheduledDate     = null,
                        status               = schedule.status,
                        rescheduled          = false,
                        skipReason           = "Already ${schedule.status.name.lowercase()}"
                    )
                )
                skippedCount++
                continue
            }

            if (schedule.status == ScheduleStatus.OVERDUE) {
                if (!request.rescheduleOverdue) {
                    results.add(
                        VaccinationRescheduleItemResult(
                            scheduleId           = schedule.scheduleId,
                            vaccineName          = vaccineName,
                            vaccineNameAr        = vaccineType.vaccineNameAr,
                            vaccineNameKu        = vaccineType.vaccineNameKu,
                            vaccineNameCkb       = vaccineType.vaccineNameCkb,
                            recommendedAgeMonths = vaccineType.recommendedAgeMonths,
                            doseNumber           = vaccineType.doseNumber.toInt(),
                            oldScheduledDate     = schedule.scheduledDate,
                            newScheduledDate     = null,
                            status               = schedule.status,
                            rescheduled          = false,
                            skipReason           = "Overdue rescheduling was not requested"
                        )
                    )
                    skippedCount++
                    continue
                }

                val idealDate     = schedule.idealDate
                val monthsOverdue = Period.between(idealDate, today).toTotalMonths().toInt()
                val isTooLate     = monthsOverdue > MAX_OVERSHOOT_MONTHS

                if (isTooLate) {
                    val oldDate = schedule.scheduledDate
                    schedule.status = ScheduleStatus.MISSED
                    scheduleRepository.save(schedule)

                    adjustmentLogRepository.save(
                        ScheduleAdjustmentLog(
                            logId      = UUID.randomUUID().toString(),
                            schedule   = schedule,
                            baby       = baby,
                            oldDate    = oldDate,
                            newDate    = oldDate,
                            reason     = AdjustmentReason.PARENT_MISSED,
                            notes      = "Vaccination window exceeded — ideal date was $idealDate, " +
                                    "$monthsOverdue months overdue (max allowed $MAX_OVERSHOOT_MONTHS months). " +
                                    request.notes.orEmpty(),
                            adjustedBy = doneBy,
                            adjustedAt = LocalDateTime.now()
                        )
                    )

                    results.add(
                        VaccinationRescheduleItemResult(
                            scheduleId           = schedule.scheduleId,
                            vaccineName          = vaccineName,
                            vaccineNameAr        = vaccineType.vaccineNameAr,
                            vaccineNameKu        = vaccineType.vaccineNameKu,
                            vaccineNameCkb       = vaccineType.vaccineNameCkb,
                            recommendedAgeMonths = vaccineType.recommendedAgeMonths,
                            doseNumber           = vaccineType.doseNumber.toInt(),
                            oldScheduledDate     = oldDate,
                            newScheduledDate     = null,
                            status               = ScheduleStatus.MISSED,
                            rescheduled          = false,
                            skipReason           = "Vaccination window exceeded — $monthsOverdue months late " +
                                    "(max $MAX_OVERSHOOT_MONTHS months). Marked as Missed."
                        )
                    )
                    tooLateCount++
                    continue
                }
            }

            val startFrom = maxOf(today, schedule.idealDate)
            val (newDate, shiftReason, shiftDays) = findNextValidDate(
                from            = startFrom,
                vaccinationDays = vaccinationDays,
                holidayDates    = holidayDates
            )

            val oldDate = schedule.scheduledDate

            schedule.scheduledDate = newDate
            schedule.shiftReason   = mapAdjustmentToShiftReason(request.shiftReason)
            schedule.shiftDays     = Period.between(schedule.idealDate, newDate).days.coerceAtLeast(0)
            schedule.status        = computeStatus(newDate)
            scheduleRepository.save(schedule)

            adjustmentLogRepository.save(
                ScheduleAdjustmentLog(
                    logId      = UUID.randomUUID().toString(),
                    schedule   = schedule,
                    baby       = baby,
                    oldDate    = oldDate,
                    newDate    = newDate,
                    reason     = request.shiftReason,
                    notes      = request.notes,
                    adjustedBy = doneBy,
                    adjustedAt = LocalDateTime.now()
                )
            )

            results.add(
                VaccinationRescheduleItemResult(
                    scheduleId           = schedule.scheduleId,
                    vaccineName          = vaccineName,
                    vaccineNameAr        = vaccineType.vaccineNameAr,
                    vaccineNameKu        = vaccineType.vaccineNameKu,
                    vaccineNameCkb       = vaccineType.vaccineNameCkb,
                    recommendedAgeMonths = vaccineType.recommendedAgeMonths,
                    doseNumber           = vaccineType.doseNumber.toInt(),
                    oldScheduledDate     = oldDate,
                    newScheduledDate     = newDate,
                    status               = schedule.status,
                    rescheduled          = true,
                    skipReason           = null
                )
            )
            rescheduledCount++
        }

        val message = buildString {
            append("Rescheduled $rescheduledCount vaccination(s).")
            if (tooLateCount > 0) append(" $tooLateCount vaccination(s) are too late and marked as Missed.")
            if (skippedCount > 0) append(" $skippedCount vaccination(s) were skipped (completed/missed).")
        }

        logger.info { "Reschedule complete for baby $babyId — $message" }

        return VaccinationRescheduleResponse(
            babyId           = babyId,
            babyName         = baby.fullName,
            totalVaccines    = allSchedules.size,
            rescheduledCount = rescheduledCount,
            skippedCount     = skippedCount,
            tooLateCount     = tooLateCount,
            results          = results,
            message          = message
        )
    }

    private fun computeStatus(scheduledDate: LocalDate): ScheduleStatus {
        val today = LocalDate.now()
        return when {
            scheduledDate.isBefore(today)       -> ScheduleStatus.OVERDUE
            scheduledDate <= today.plusDays(14) -> ScheduleStatus.DUE_SOON
            else                                -> ScheduleStatus.UPCOMING
        }
    }

    private fun findNextValidDate(
        from           : LocalDate,
        vaccinationDays: Set<DayOfWeek>,
        holidayDates   : Set<LocalDate>
    ): Triple<LocalDate, ShiftReason, Int> {
        var candidate = from
        var shift     = 0
        var safeGuard = 0
        while (safeGuard++ < 365) {
            val isVacDay  = candidate.dayOfWeek in vaccinationDays
            val isHoliday = candidate in holidayDates
            if (isVacDay && !isHoliday) {
                return Triple(candidate, ShiftReason.NONE, shift)
            }
            candidate = candidate.plusDays(1)
            shift++
        }
        return Triple(from, ShiftReason.NONE, 0)
    }

    private fun loadHolidayDates(benchId: String, from: LocalDate, to: LocalDate): Set<LocalDate> =
        holidayRepository.findHolidaysForBenchInRange(benchId, from, to)
            .map { it.holidayDate }.toSet()

    private fun mapAdjustmentToShiftReason(reason: AdjustmentReason): ShiftReason = when (reason) {
        AdjustmentReason.HOLIDAY            -> ShiftReason.HOLIDAY
        AdjustmentReason.BENCH_CLOSED       -> ShiftReason.BENCH_CLOSED
        AdjustmentReason.PARENT_MISSED      -> ShiftReason.MISSED
        AdjustmentReason.PARENT_RESCHEDULED -> ShiftReason.RESCHEDULED
        AdjustmentReason.TEAM_RESCHEDULED   -> ShiftReason.RESCHEDULED
        AdjustmentReason.BENCH_CHANGED      -> ShiftReason.BENCH_CLOSED
    }
}