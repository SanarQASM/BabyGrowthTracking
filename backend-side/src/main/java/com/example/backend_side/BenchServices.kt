package com.example.backend_side

import com.example.backend_side.entity.*
import com.example.backend_side.repositories.*
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.util.UUID

private val logger = KotlinLogging.logger {}

// Day name → Java DayOfWeek mapping (Iraqi calendar uses Arabic day names in English)
private val DAY_NAME_MAP = mapOf(
    "Sunday"    to DayOfWeek.SUNDAY,
    "Monday"    to DayOfWeek.MONDAY,
    "Tuesday"   to DayOfWeek.TUESDAY,
    "Wednesday" to DayOfWeek.WEDNESDAY,
    "Thursday"  to DayOfWeek.THURSDAY,
    "Friday"    to DayOfWeek.FRIDAY,
    "Saturday"  to DayOfWeek.SATURDAY
)

// ============================================================
// VACCINATION BENCH SERVICE
// ============================================================

interface VaccinationBenchService {
    fun getAllBenches(): List<VaccinationBenchResponse>
    fun getBenchesByGovernorate(governorate: String): List<VaccinationBenchResponse>
    fun getBenchById(benchId: String): VaccinationBenchResponse
    fun searchBenches(query: String): List<VaccinationBenchResponse>
    fun getGovernorates(): List<String>
    fun createBench(request: VaccinationBenchCreateRequest): VaccinationBenchResponse
    fun updateBench(benchId: String, request: VaccinationBenchUpdateRequest): VaccinationBenchResponse
    fun deactivateBench(benchId: String)
    fun loadBenchesFromJson(benches: List<VaccinationBenchCreateRequest>): Int
}

@Service
@Transactional
class VaccinationBenchServiceImpl(
    private val benchRepository: VaccinationBenchRepository
) : VaccinationBenchService {

    override fun getAllBenches(): List<VaccinationBenchResponse> =
        benchRepository.findByIsActiveTrue().map { it.toResponse() }

    override fun getBenchesByGovernorate(governorate: String): List<VaccinationBenchResponse> =
        benchRepository.findByGovernorateAndIsActiveTrue(governorate).map { it.toResponse() }

    override fun getBenchById(benchId: String): VaccinationBenchResponse {
        val bench = benchRepository.findById(benchId)
            .orElseThrow { ResourceNotFoundException("Bench not found: $benchId") }
        return bench.toResponse()
    }

    override fun searchBenches(query: String): List<VaccinationBenchResponse> =
        benchRepository.searchBenches(query).map { it.toResponse() }

    override fun getGovernorates(): List<String> =
        benchRepository.findAllGovernorates()

    override fun createBench(request: VaccinationBenchCreateRequest): VaccinationBenchResponse {
        val bench = VaccinationBench(
            benchId         = UUID.randomUUID().toString(),
            nameEn          = request.nameEn,
            nameAr          = request.nameAr,
            governorate     = request.governorate,
            district        = request.district,
            addressEn       = request.addressEn,
            addressAr       = request.addressAr,
            latitude        = request.latitude,
            longitude       = request.longitude,
            phone           = request.phone,
            workingDays     = request.workingDays.joinToString(","),
            workingHoursStart = request.workingHoursStart,
            workingHoursEnd   = request.workingHoursEnd,
            vaccinationDays = request.vaccinationDays.joinToString(","),
            type            = request.type,
            vaccinesAvailable = request.vaccinesAvailable.joinToString(",")
        )
        return benchRepository.save(bench).toResponse()
    }

    override fun updateBench(benchId: String, request: VaccinationBenchUpdateRequest): VaccinationBenchResponse {
        val bench = benchRepository.findById(benchId)
            .orElseThrow { ResourceNotFoundException("Bench not found: $benchId") }

        request.nameEn?.let { bench.nameEn = it }
        request.nameAr?.let { bench.nameAr = it }
        request.phone?.let { bench.phone = it }
        request.workingDays?.let { bench.workingDays = it.joinToString(",") }
        request.workingHoursStart?.let { bench.workingHoursStart = it }
        request.workingHoursEnd?.let { bench.workingHoursEnd = it }
        request.vaccinationDays?.let { bench.vaccinationDays = it.joinToString(",") }
        request.vaccinesAvailable?.let { bench.vaccinesAvailable = it.joinToString(",") }
        request.isActive?.let { bench.isActive = it }

        return benchRepository.save(bench).toResponse()
    }

    override fun deactivateBench(benchId: String) {
        val bench = benchRepository.findById(benchId)
            .orElseThrow { ResourceNotFoundException("Bench not found: $benchId") }
        bench.isActive = false
        benchRepository.save(bench)
        logger.info { "Bench deactivated: $benchId" }
    }

    override fun loadBenchesFromJson(benches: List<VaccinationBenchCreateRequest>): Int {
        var loaded = 0
        for (request in benches) {
            val bench = VaccinationBench(
                benchId           = UUID.randomUUID().toString(),
                nameEn            = request.nameEn,
                nameAr            = request.nameAr,
                governorate       = request.governorate,
                district          = request.district,
                addressEn         = request.addressEn,
                addressAr         = request.addressAr,
                latitude          = request.latitude,
                longitude         = request.longitude,
                phone             = request.phone,
                workingDays       = request.workingDays.joinToString(","),
                workingHoursStart = request.workingHoursStart,
                workingHoursEnd   = request.workingHoursEnd,
                vaccinationDays   = request.vaccinationDays.joinToString(","),
                type              = request.type,
                vaccinesAvailable = request.vaccinesAvailable.joinToString(",")
            )
            benchRepository.save(bench)
            loaded++
        }
        logger.info { "Loaded $loaded benches from JSON" }
        return loaded
    }

    fun VaccinationBench.toResponse() = VaccinationBenchResponse(
        benchId           = benchId,
        nameEn            = nameEn,
        nameAr            = nameAr,
        governorate       = governorate,
        district          = district,
        addressEn         = addressEn,
        addressAr         = addressAr,
        latitude          = latitude,
        longitude         = longitude,
        phone             = phone,
        workingDays       = getWorkingDaysList(),
        workingHoursStart = workingHoursStart,
        workingHoursEnd   = workingHoursEnd,
        vaccinationDays   = getVaccinationDaysList(),
        type              = type,
        vaccinesAvailable = getVaccinesAvailableList(),
        isActive          = isActive,
        createdAt         = createdAt
    )
}

// ============================================================
// BABY BENCH ASSIGNMENT SERVICE
// ============================================================

interface BabyBenchAssignmentService {
    fun assignBenchToBaby(assignedByUserId: String, request: BabyBenchAssignRequest): BabyBenchAssignmentResponse
    fun getActiveAssignmentForBaby(babyId: String): BabyBenchAssignmentResponse?
    fun getAssignmentHistoryForBaby(babyId: String): List<BabyBenchAssignmentResponse>
    fun getActiveBabiesForBench(benchId: String): List<BabyBenchAssignmentResponse>
    fun changeBench(babyId: String, assignedByUserId: String, request: BabyBenchAssignRequest): BabyBenchAssignmentResponse
}

@Service
@Transactional
class BabyBenchAssignmentServiceImpl(
    private val assignmentRepository: BabyBenchAssignmentRepository,
    private val babyRepository: BabyRepository,
    private val benchRepository: VaccinationBenchRepository,
    private val userRepository: UserRepository,
    private val scheduleGeneratorService: ScheduleGeneratorService
) : BabyBenchAssignmentService {

    override fun assignBenchToBaby(
        assignedByUserId: String,
        request: BabyBenchAssignRequest
    ): BabyBenchAssignmentResponse {
        val baby = babyRepository.findById(request.babyId)
            .orElseThrow { ResourceNotFoundException("Baby not found: ${request.babyId}") }

        val bench = benchRepository.findById(request.benchId)
            .orElseThrow { ResourceNotFoundException("Bench not found: ${request.benchId}") }

        val assignedBy = userRepository.findById(assignedByUserId)
            .orElseThrow { ResourceNotFoundException("User not found: $assignedByUserId") }

        // Deactivate any existing active assignment
        val existing = assignmentRepository.findByBaby_BabyIdAndIsActiveTrue(request.babyId)
        existing.ifPresent { old ->
            old.isActive = false
            assignmentRepository.save(old)
            logger.info { "Deactivated old assignment ${old.assignmentId} for baby ${request.babyId}" }
        }

        val assignment = BabyBenchAssignment(
            assignmentId = UUID.randomUUID().toString(),
            baby         = baby,
            bench        = bench,
            assignedBy   = assignedBy,
            assignedAt   = LocalDateTime.now(),
            isActive     = true,
            notes        = request.notes
        )
        val saved = assignmentRepository.save(assignment)
        logger.info { "Assigned bench ${bench.benchId} to baby ${baby.babyId}" }

        // Generate vaccination schedule for this baby based on the new bench
        scheduleGeneratorService.generateScheduleForBaby(baby, bench)

        return saved.toResponse()
    }

    override fun getActiveAssignmentForBaby(babyId: String): BabyBenchAssignmentResponse? =
        assignmentRepository.findByBaby_BabyIdAndIsActiveTrue(babyId)
            .map { it.toResponse() }.orElse(null)

    override fun getAssignmentHistoryForBaby(babyId: String): List<BabyBenchAssignmentResponse> =
        assignmentRepository.findByBaby_BabyId(babyId).map { it.toResponse() }

    override fun getActiveBabiesForBench(benchId: String): List<BabyBenchAssignmentResponse> =
        assignmentRepository.findActiveBabiesForBench(benchId).map { it.toResponse() }

    override fun changeBench(
        babyId: String,
        assignedByUserId: String,
        request: BabyBenchAssignRequest
    ): BabyBenchAssignmentResponse {
        // changeBench is the same as assign — just re-assign
        // ScheduleGeneratorService will regenerate all upcoming schedules
        return assignBenchToBaby(assignedByUserId, request.copy(babyId = babyId))
    }

    private fun BabyBenchAssignment.toResponse() = BabyBenchAssignmentResponse(
        assignmentId = assignmentId,
        babyId       = baby?.babyId ?: "",
        babyName     = baby?.fullName ?: "",
        benchId      = bench?.benchId ?: "",
        benchNameEn  = bench?.nameEn ?: "",
        benchNameAr  = bench?.nameAr ?: "",
        governorate  = bench?.governorate ?: "",
        assignedAt   = assignedAt,
        isActive     = isActive,
        notes        = notes
    )
}

// ============================================================
// BENCH HOLIDAY SERVICE
// ============================================================

interface BenchHolidayService {
    fun addHoliday(request: BenchHolidayCreateRequest): BenchHolidayResponse
    fun getHolidaysForBench(benchId: String): List<BenchHolidayResponse>
    fun getNationalHolidays(): List<BenchHolidayResponse>
    fun deleteHoliday(holidayId: String)
    fun isHoliday(benchId: String, date: LocalDate): Boolean
}

@Service
@Transactional
class BenchHolidayServiceImpl(
    private val holidayRepository: BenchHolidayRepository,
    private val benchRepository: VaccinationBenchRepository
) : BenchHolidayService {

    override fun addHoliday(request: BenchHolidayCreateRequest): BenchHolidayResponse {
        val bench = request.benchId?.let {
            benchRepository.findById(it)
                .orElseThrow { ResourceNotFoundException("Bench not found: $it") }
        }

        val holiday = BenchHoliday(
            holidayId   = UUID.randomUUID().toString(),
            bench       = bench,
            holidayDate = request.holidayDate,
            reason      = request.reason,
            isNational  = request.isNational
        )
        return holidayRepository.save(holiday).toResponse()
    }

    override fun getHolidaysForBench(benchId: String): List<BenchHolidayResponse> =
        holidayRepository.findByBench_BenchId(benchId).map { it.toResponse() }

    override fun getNationalHolidays(): List<BenchHolidayResponse> =
        holidayRepository.findByIsNationalTrue().map { it.toResponse() }

    override fun deleteHoliday(holidayId: String) {
        if (!holidayRepository.existsById(holidayId))
            throw ResourceNotFoundException("Holiday not found: $holidayId")
        holidayRepository.deleteById(holidayId)
    }

    override fun isHoliday(benchId: String, date: LocalDate): Boolean =
        holidayRepository.isHolidayForBench(date, benchId)

    private fun BenchHoliday.toResponse() = BenchHolidayResponse(
        holidayId   = holidayId,
        benchId     = bench?.benchId,
        benchNameEn = bench?.nameEn,
        holidayDate = holidayDate,
        reason      = reason,
        isNational  = isNational,
        createdAt   = createdAt
    )
}

// ============================================================
// SCHEDULE GENERATOR SERVICE
// ============================================================
// Core algorithm:
//   1. For each VaccineType → calculate ideal_date = baby.DOB + recommended_age_months
//   2. Starting from ideal_date, find the next date that:
//      a. Is one of the bench's vaccination_days
//      b. Is NOT in bench_holidays (bench-specific or national)
//   3. Save to vaccination_schedules
//   4. Log every shift to schedule_adjustment_logs
// ============================================================

interface ScheduleGeneratorService {
    fun generateScheduleForBaby(baby: Baby, bench: VaccinationBench)
    fun regenerateScheduleOnBenchChange(babyId: String, newBench: VaccinationBench)
    fun updateStatusesForBaby(babyId: String)
}

@Service
@Transactional
class ScheduleGeneratorServiceImpl(
    private val scheduleRepository: VaccinationScheduleRepository,
    private val vaccineTypeRepository: VaccineTypeRepository,
    private val holidayRepository: BenchHolidayRepository,
    private val adjustmentLogRepository: ScheduleAdjustmentLogRepository
) : ScheduleGeneratorService {

    override fun generateScheduleForBaby(baby: Baby, bench: VaccinationBench) {
        logger.info { "Generating schedule for baby ${baby.babyId} at bench ${bench.benchId}" }

        val allVaccines = vaccineTypeRepository.findAll()
        val vaccinationDays = bench.getVaccinationDaysList()
            .mapNotNull { DAY_NAME_MAP[it] }
            .toSet()

        // Preload holidays for the next 3 years to avoid N+1 queries
        val holidayDates = loadHolidayDates(bench.benchId, baby.dateOfBirth, baby.dateOfBirth.plusYears(3))

        for (vaccine in allVaccines) {
            // Skip if schedule already exists for this baby+vaccine
            val existing = scheduleRepository.findByBaby_BabyIdAndVaccineType_VaccineId(
                baby.babyId, vaccine.vaccineId ?: continue
            )
            if (existing.isPresent) continue

            val idealDate = baby.dateOfBirth.plusMonths(vaccine.recommendedAgeMonths.toLong())
            val (scheduledDate, shiftReason, shiftDays) = findNextValidDate(
                idealDate, vaccinationDays, holidayDates
            )

            val schedule = VaccinationSchedule(
                scheduleId        = UUID.randomUUID().toString(),
                baby              = baby,
                bench             = bench,
                vaccineType       = vaccine,
                idealDate         = idealDate,
                scheduledDate     = scheduledDate,
                shiftReason       = shiftReason,
                shiftDays         = shiftDays,
                status            = computeStatus(scheduledDate)
            )
            scheduleRepository.save(schedule)

            // Log only if the date was actually shifted
            if (shiftDays > 0) {
                val log = ScheduleAdjustmentLog(
                    logId        = UUID.randomUUID().toString(),
                    schedule     = schedule,
                    baby         = baby,
                    oldDate      = idealDate,
                    newDate      = scheduledDate,
                    reason       = shiftReasonToAdjustmentReason(shiftReason),
                    notes        = "Auto-adjusted by schedule generator",
                    adjustedBy   = null,   // system adjustment
                    adjustedAt   = LocalDateTime.now()
                )
                adjustmentLogRepository.save(log)
            }
        }

        logger.info { "Schedule generation complete for baby ${baby.babyId}" }
    }

    override fun regenerateScheduleOnBenchChange(babyId: String, newBench: VaccinationBench) {
        logger.info { "Regenerating upcoming schedules for baby $babyId due to bench change" }

        val upcomingSchedules = scheduleRepository
            .findByBaby_BabyIdAndStatus(babyId, ScheduleStatus.UPCOMING) +
                scheduleRepository.findByBaby_BabyIdAndStatus(babyId, ScheduleStatus.DUE_SOON)

        val vaccinationDays = newBench.getVaccinationDaysList()
            .mapNotNull { DAY_NAME_MAP[it] }
            .toSet()

        val now = LocalDate.now()
        val holidayDates = loadHolidayDates(newBench.benchId, now, now.plusYears(3))

        for (schedule in upcomingSchedules) {
            val oldDate = schedule.scheduledDate
            val (newDate, shiftReason, shiftDays) = findNextValidDate(
                schedule.idealDate, vaccinationDays, holidayDates
            )

            if (newDate != oldDate) {
                val log = ScheduleAdjustmentLog(
                    logId      = UUID.randomUUID().toString(),
                    schedule   = schedule,
                    baby       = schedule.baby,
                    oldDate    = oldDate,
                    newDate    = newDate,
                    reason     = AdjustmentReason.BENCH_CHANGED,
                    notes      = "Bench changed to ${newBench.nameEn}",
                    adjustedBy = null,
                    adjustedAt = LocalDateTime.now()
                )
                adjustmentLogRepository.save(log)
            }

            schedule.bench         = newBench
            schedule.scheduledDate = newDate
            schedule.shiftReason   = shiftReason
            schedule.shiftDays     = shiftDays
            schedule.status        = computeStatus(newDate)
            scheduleRepository.save(schedule)
        }
    }

    override fun updateStatusesForBaby(babyId: String) {
        val schedules = scheduleRepository.findByBaby_BabyIdOrderByScheduledDateAsc(babyId)
        val today = LocalDate.now()
        for (schedule in schedules) {
            if (schedule.status == ScheduleStatus.COMPLETED || schedule.status == ScheduleStatus.MISSED) continue
            schedule.status = computeStatus(schedule.scheduledDate)
            scheduleRepository.save(schedule)
        }
    }

    // ── Core date-finding algorithm ────────────────────────────────────────────
    private fun findNextValidDate(
        from: LocalDate,
        vaccinationDays: Set<DayOfWeek>,
        holidayDates: Set<LocalDate>
    ): Triple<LocalDate, ShiftReason, Int> {

        if (vaccinationDays.isEmpty()) {
            return Triple(from, ShiftReason.NONE, 0)
        }

        var candidate = from
        var shiftReason = ShiftReason.NONE
        var iterations = 0

        while (iterations < 365) {   // safety limit — max 1 year forward
            val isVaccinationDay = candidate.dayOfWeek in vaccinationDays
            val isHoliday = candidate in holidayDates

            if (isVaccinationDay && !isHoliday) {
                val shiftDays = (candidate.toEpochDay() - from.toEpochDay()).toInt()
                return Triple(candidate, shiftReason, shiftDays)
            }

            // Move to next day and update reason
            if (!isVaccinationDay && shiftReason == ShiftReason.NONE) {
                shiftReason = ShiftReason.WEEKEND
            }
            if (isHoliday) {
                shiftReason = ShiftReason.HOLIDAY
            }

            candidate = candidate.plusDays(1)
            iterations++
        }

        // Fallback: return the ideal date with no shift
        logger.warn { "Could not find valid vaccination date within 365 days from $from" }
        return Triple(from, ShiftReason.NONE, 0)
    }

    private fun loadHolidayDates(benchId: String, from: LocalDate, to: LocalDate): Set<LocalDate> {
        return holidayRepository
            .findHolidaysForBenchInRange(benchId, from, to)
            .map { it.holidayDate }
            .toSet()
    }

    private fun computeStatus(scheduledDate: LocalDate): ScheduleStatus {
        val today = LocalDate.now()
        return when {
            scheduledDate.isBefore(today)                         -> ScheduleStatus.OVERDUE
            scheduledDate.isBefore(today.plusDays(14))            -> ScheduleStatus.DUE_SOON
            else                                                   -> ScheduleStatus.UPCOMING
        }
    }

    private fun shiftReasonToAdjustmentReason(reason: ShiftReason): AdjustmentReason =
        when (reason) {
            ShiftReason.HOLIDAY      -> AdjustmentReason.HOLIDAY
            ShiftReason.BENCH_CLOSED -> AdjustmentReason.BENCH_CLOSED
            else                     -> AdjustmentReason.HOLIDAY
        }
}

// ============================================================
// VACCINATION SCHEDULE SERVICE
// ============================================================

interface VaccinationScheduleService {
    fun getScheduleForBaby(babyId: String): List<VaccinationScheduleResponse>
    fun getUpcomingForBaby(babyId: String, daysAhead: Int): List<VaccinationScheduleResponse>
    fun getOverdueForBaby(babyId: String): List<VaccinationScheduleResponse>
    fun getScheduleByBenchAndDate(benchId: String, date: LocalDate): BenchDayScheduleResponse
    fun getScheduleByBenchAndRange(benchId: String, from: LocalDate, to: LocalDate): List<VaccinationScheduleResponse>
    fun updateScheduleStatus(scheduleId: String, request: VaccinationScheduleUpdateRequest): VaccinationScheduleResponse
    fun adjustScheduleDate(adjustedByUserId: String, request: ScheduleAdjustmentRequest): VaccinationScheduleResponse
    fun getAdjustmentHistory(scheduleId: String): List<ScheduleAdjustmentLogResponse>
}

@Service
@Transactional
class VaccinationScheduleServiceImpl(
    private val scheduleRepository: VaccinationScheduleRepository,
    private val userRepository: UserRepository,
    private val vaccinationRepository: VaccinationRepository,
    private val adjustmentLogRepository: ScheduleAdjustmentLogRepository,
    private val benchRepository: VaccinationBenchRepository,
    private val holidayRepository: BenchHolidayRepository
) : VaccinationScheduleService {

    override fun getScheduleForBaby(babyId: String): List<VaccinationScheduleResponse> =
        scheduleRepository.findByBaby_BabyIdOrderByScheduledDateAsc(babyId)
            .map { it.toResponse() }

    override fun getUpcomingForBaby(babyId: String, daysAhead: Int): List<VaccinationScheduleResponse> {
        val from = LocalDate.now()
        val to = from.plusDays(daysAhead.toLong())
        return scheduleRepository.findUpcomingForBaby(babyId, from, to).map { it.toResponse() }
    }

    override fun getOverdueForBaby(babyId: String): List<VaccinationScheduleResponse> =
        scheduleRepository.findOverdueForBaby(babyId).map { it.toResponse() }

    override fun getScheduleByBenchAndDate(benchId: String, date: LocalDate): BenchDayScheduleResponse {
        val bench = benchRepository.findById(benchId)
            .orElseThrow { ResourceNotFoundException("Bench not found: $benchId") }
        val items = scheduleRepository.findByBenchAndDate(benchId, date).map { it.toResponse() }
        return BenchDayScheduleResponse(
            benchId     = benchId,
            benchNameEn = bench.nameEn,
            date        = date,
            totalBabies = items.size,
            items       = items
        )
    }

    override fun getScheduleByBenchAndRange(
        benchId: String, from: LocalDate, to: LocalDate
    ): List<VaccinationScheduleResponse> =
        scheduleRepository.findByBenchAndDateRange(benchId, from, to).map { it.toResponse() }

    override fun updateScheduleStatus(
        scheduleId: String,
        request: VaccinationScheduleUpdateRequest
    ): VaccinationScheduleResponse {
        val schedule = scheduleRepository.findById(scheduleId)
            .orElseThrow { ResourceNotFoundException("Schedule not found: $scheduleId") }

        request.status?.let { schedule.status = it }
        request.completedDate?.let { schedule.completedDate = it }
        request.completedByUserId?.let { userId ->
            schedule.completedBy = userRepository.findById(userId).orElse(null)
        }
        request.vaccinationId?.let { vId ->
            schedule.vaccination = vaccinationRepository.findById(vId).orElse(null)
        }

        return scheduleRepository.save(schedule).toResponse()
    }

    override fun adjustScheduleDate(
        adjustedByUserId: String,
        request: ScheduleAdjustmentRequest
    ): VaccinationScheduleResponse {
        val schedule = scheduleRepository.findById(request.scheduleId)
            .orElseThrow { ResourceNotFoundException("Schedule not found: ${request.scheduleId}") }

        val adjustedBy = userRepository.findById(adjustedByUserId).orElse(null)
        val oldDate = schedule.scheduledDate

        // Log the change
        val log = ScheduleAdjustmentLog(
            logId      = UUID.randomUUID().toString(),
            schedule   = schedule,
            baby       = schedule.baby,
            oldDate    = oldDate,
            newDate    = request.newDate,
            reason     = request.reason,
            notes      = request.notes,
            adjustedBy = adjustedBy,
            adjustedAt = LocalDateTime.now()
        )
        adjustmentLogRepository.save(log)

        // Apply the change
        schedule.scheduledDate = request.newDate
        schedule.shiftReason   = ShiftReason.RESCHEDULED
        schedule.shiftDays     = (request.newDate.toEpochDay() - schedule.idealDate.toEpochDay()).toInt()
        schedule.status        = when {
            request.newDate.isBefore(LocalDate.now())               -> ScheduleStatus.OVERDUE
            request.newDate.isBefore(LocalDate.now().plusDays(14))  -> ScheduleStatus.DUE_SOON
            else                                                     -> ScheduleStatus.UPCOMING
        }

        logger.info { "Schedule ${schedule.scheduleId} adjusted from $oldDate to ${request.newDate} by $adjustedByUserId" }
        return scheduleRepository.save(schedule).toResponse()
    }

    override fun getAdjustmentHistory(scheduleId: String): List<ScheduleAdjustmentLogResponse> =
        adjustmentLogRepository
            .findBySchedule_ScheduleIdOrderByAdjustedAtDesc(scheduleId)
            .map { it.toLogResponse() }

    private fun VaccinationSchedule.toResponse() = VaccinationScheduleResponse(
        scheduleId           = scheduleId,
        babyId               = baby?.babyId ?: "",
        babyName             = baby?.fullName ?: "",
        benchId              = bench?.benchId ?: "",
        benchNameEn          = bench?.nameEn ?: "",
        benchNameAr          = bench?.nameAr ?: "",
        vaccineId            = vaccineType?.vaccineId ?: 0,
        vaccineName          = vaccineType?.vaccineName ?: "",
        doseNumber           = vaccineType?.doseNumber ?: 1,
        recommendedAgeMonths = vaccineType?.recommendedAgeMonths ?: 0,
        idealDate            = idealDate,
        scheduledDate        = scheduledDate,
        shiftReason          = shiftReason,
        shiftDays            = shiftDays,
        status               = status,
        completedDate        = completedDate,
        completedByName      = completedBy?.fullName,
        isVisibleToParent    = isVisibleToParent,
        isVisibleToTeam      = isVisibleToTeam,
        createdAt            = createdAt,
        updatedAt            = updatedAt
    )

    private fun ScheduleAdjustmentLog.toLogResponse() = ScheduleAdjustmentLogResponse(
        logId           = logId,
        scheduleId      = schedule?.scheduleId ?: "",
        babyId          = baby?.babyId ?: "",
        babyName        = baby?.fullName ?: "",
        oldDate         = oldDate,
        newDate         = newDate,
        reason          = reason,
        notes           = notes,
        adjustedByName  = adjustedBy?.fullName,
        adjustedAt      = adjustedAt
    )
}