package com.example.backend_side

import com.example.backend_side.entity.*
import com.example.backend_side.repositories.*
import jakarta.transaction.Transactional
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

private val logger = KotlinLogging.logger {}

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
// BENCH SERVICE
// ============================================================

interface VaccinationBenchService {
    fun getAllBenches(): List<VaccinationBenchResponse>
    fun getBenchesByGovernorate(governorate: String): List<VaccinationBenchResponse>
    fun getBenchById(benchId: String): VaccinationBenchResponse
    fun getBenchByTeamMember(teamMemberId: String): VaccinationBenchResponse?
    fun searchBenches(query: String): List<VaccinationBenchResponse>
    fun getGovernorates(): List<String>
    fun createBench(request: VaccinationBenchCreateRequest): VaccinationBenchResponse
    fun updateBench(benchId: String, request: VaccinationBenchUpdateRequest): VaccinationBenchResponse
    fun assignTeamMember(benchId: String, teamMemberId: String): VaccinationBenchResponse
    fun deactivateBench(benchId: String)
    fun loadBenchesFromJson(benches: List<VaccinationBenchCreateRequest>): Int
}

@Service
@Transactional
class VaccinationBenchServiceImpl(
    private val benchRepository: VaccinationBenchRepository,
    private val userRepository : UserRepository
) : VaccinationBenchService {

    override fun getAllBenches(): List<VaccinationBenchResponse> =
        benchRepository.findByIsActiveTrue().map { it.toResponse() }

    // FIX: was findByGovernorateAndIsActiveTrue (exact case) — repository declares
    // findByGovernorateIgnoreCaseAndIsActiveTrue. Calling the wrong name caused
    // Spring Data JPA NoSuchMethodException at startup.
    override fun getBenchesByGovernorate(governorate: String): List<VaccinationBenchResponse> =
        benchRepository.findByGovernorateIgnoreCaseAndIsActiveTrue(governorate).map { it.toResponse() }

    override fun getBenchById(benchId: String): VaccinationBenchResponse =
        benchRepository.findById(benchId)
            .orElseThrow { ResourceNotFoundException("Bench not found: $benchId") }
            .toResponse()

    override fun getBenchByTeamMember(teamMemberId: String): VaccinationBenchResponse? =
        benchRepository.findByTeamMember_UserIdAndIsActiveTrue(teamMemberId)
            .map { it.toResponse() }
            .orElse(null)

    override fun searchBenches(query: String): List<VaccinationBenchResponse> =
        benchRepository.searchBenches(query).map { it.toResponse() }

    // FIX: was benchRepository.findAllGovernorates() — repository declares findAllGovernorates()
    // as a @Query alias. Both are now present in the repository to satisfy both call sites.
    override fun getGovernorates(): List<String> =
        benchRepository.findAllGovernorates()

    override fun createBench(request: VaccinationBenchCreateRequest): VaccinationBenchResponse {
        val teamMember: User? = request.teamMemberId?.let { tmId ->
            val user = userRepository.findById(tmId)
                .orElseThrow { ResourceNotFoundException("Team member not found: $tmId") }
            if (user.role != UserRole.VACCINATION_TEAM) {
                throw BadRequestException("User $tmId is not a vaccination_team member")
            }
            user
        }

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
            // FIX: request.type is now BenchType (was String) — assign directly
            type              = request.type,
            vaccinesAvailable = request.vaccinesAvailable.joinToString(","),
            teamMember        = teamMember
        )
        return benchRepository.save(bench).toResponse()
    }

    override fun updateBench(benchId: String, request: VaccinationBenchUpdateRequest): VaccinationBenchResponse {
        val bench = benchRepository.findById(benchId)
            .orElseThrow { ResourceNotFoundException("Bench not found: $benchId") }

        request.nameEn?.let            { bench.nameEn            = it }
        request.nameAr?.let            { bench.nameAr            = it }
        request.governorate?.let       { bench.governorate       = it }
        request.district?.let          { bench.district          = it }
        request.addressEn?.let         { bench.addressEn         = it }
        request.addressAr?.let         { bench.addressAr         = it }
        request.latitude?.let          { bench.latitude          = it }
        request.longitude?.let         { bench.longitude         = it }
        request.phone?.let             { bench.phone             = it }
        request.workingDays?.let       { bench.workingDays       = it.joinToString(",") }
        request.workingHoursStart?.let { bench.workingHoursStart = it }
        request.workingHoursEnd?.let   { bench.workingHoursEnd   = it }
        request.vaccinationDays?.let   { bench.vaccinationDays   = it.joinToString(",") }
        request.vaccinesAvailable?.let { bench.vaccinesAvailable = it.joinToString(",") }
        request.isActive?.let          { bench.isActive          = it }
        // FIX: request.type is now BenchType? — assign directly
        request.type?.let              { bench.type              = it }

        request.teamMemberId?.let { tmId ->
            val user = userRepository.findById(tmId)
                .orElseThrow { ResourceNotFoundException("Team member not found: $tmId") }
            if (user.role != UserRole.VACCINATION_TEAM) {
                throw BadRequestException("User $tmId is not a vaccination_team member")
            }
            bench.teamMember = user
        }

        return benchRepository.save(bench).toResponse()
    }

    override fun assignTeamMember(benchId: String, teamMemberId: String): VaccinationBenchResponse {
        val bench = benchRepository.findById(benchId)
            .orElseThrow { ResourceNotFoundException("Bench not found: $benchId") }
        val user = userRepository.findById(teamMemberId)
            .orElseThrow { ResourceNotFoundException("Team member not found: $teamMemberId") }

        if (user.role != UserRole.VACCINATION_TEAM) {
            throw BadRequestException("User $teamMemberId is not a vaccination_team member")
        }

        bench.teamMember = user
        return benchRepository.save(bench).toResponse()
    }

    override fun deactivateBench(benchId: String) {
        val bench = benchRepository.findById(benchId)
            .orElseThrow { ResourceNotFoundException("Bench not found: $benchId") }
        bench.isActive = false
        benchRepository.save(bench)
    }

    override fun loadBenchesFromJson(benches: List<VaccinationBenchCreateRequest>): Int {
        var loaded = 0
        for (request in benches) {
            // FIX: existsByNameEn is now declared in VaccinationBenchRepository
            if (benchRepository.existsByNameEn(request.nameEn)) continue
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
                // FIX: request.type is now BenchType
                type              = request.type,
                vaccinesAvailable = request.vaccinesAvailable.joinToString(",")
            )
            benchRepository.save(bench)
            loaded++
        }
        return loaded
    }

    // FIX: toResponse now includes type as String (enum name) and createdAt
    private fun VaccinationBench.toResponse() = VaccinationBenchResponse(
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
        type              = type.name,
        vaccinesAvailable = getVaccinesAvailableList(),
        isActive          = isActive,
        teamMemberId      = teamMember?.userId,
        teamMemberName    = teamMember?.fullName,
        teamMemberEmail   = teamMember?.email,
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
    private val assignmentRepository    : BabyBenchAssignmentRepository,
    private val babyRepository          : BabyRepository,
    private val benchRepository         : VaccinationBenchRepository,
    private val userRepository          : UserRepository,
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

        assignmentRepository.findByBaby_BabyIdAndIsActiveTrue(request.babyId).ifPresent { old ->
            old.isActive = false
            assignmentRepository.save(old)
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
    ): BabyBenchAssignmentResponse =
        assignBenchToBaby(assignedByUserId, request.copy(babyId = babyId))

    private fun BabyBenchAssignment.toResponse() = BabyBenchAssignmentResponse(
        assignmentId = assignmentId,
        babyId       = baby?.babyId ?: "",
        babyName     = baby?.fullName ?: "",
        benchId      = bench?.benchId ?: "",
        benchNameEn  = bench?.nameEn ?: "",
        benchNameAr  = bench?.nameAr ?: "",
        governorate  = bench?.governorate ?: "",
        assignedAt   = assignedAt.toString(),
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
    private val benchRepository  : VaccinationBenchRepository
) : BenchHolidayService {

    override fun addHoliday(request: BenchHolidayCreateRequest): BenchHolidayResponse {
        val bench = request.benchId?.let {
            benchRepository.findById(it).orElseThrow { ResourceNotFoundException("Bench not found: $it") }
        }
        return holidayRepository.save(BenchHoliday(
            holidayId   = UUID.randomUUID().toString(),
            bench       = bench,
            holidayDate = request.holidayDate,
            reason      = request.reason,
            isNational  = request.isNational
        )).toResponse()
    }

    override fun getHolidaysForBench(benchId: String) =
        holidayRepository.findByBench_BenchId(benchId).map { it.toResponse() }

    override fun getNationalHolidays() =
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

interface ScheduleGeneratorService {
    fun generateScheduleForBaby(baby: Baby, bench: VaccinationBench)
    fun regenerateScheduleOnBenchChange(babyId: String, newBench: VaccinationBench)
    fun updateStatusesForBaby(babyId: String)
}

@Service
@Transactional
class ScheduleGeneratorServiceImpl(
    private val scheduleRepository     : VaccinationScheduleRepository,
    private val vaccineTypeRepository  : VaccineTypeRepository,
    private val holidayRepository      : BenchHolidayRepository,
    private val adjustmentLogRepository: ScheduleAdjustmentLogRepository
) : ScheduleGeneratorService {

    override fun generateScheduleForBaby(baby: Baby, bench: VaccinationBench) {
        logger.info { "Generating schedule for baby ${baby.babyId} at bench ${bench.benchId}" }

        val allVaccines = vaccineTypeRepository.findAll()

        if (allVaccines.isEmpty()) {
            logger.error {
                "vaccine_types table is EMPTY — cannot generate schedule for baby ${baby.babyId}."
            }
            return
        }

        val vaccinationDays = bench.getVaccinationDaysList()
            .mapNotNull { DAY_NAME_MAP[it] }
            .toSet()

        val holidayDates = loadHolidayDates(
            bench.benchId,
            baby.dateOfBirth,
            baby.dateOfBirth.plusYears(3)
        )

        var created = 0
        for (vaccine in allVaccines) {
            val existing = scheduleRepository.findByBaby_BabyIdAndVaccineType_VaccineId(
                baby.babyId, vaccine.vaccineId ?: continue
            )
            if (existing.isPresent) continue

            val idealDate = baby.dateOfBirth.plusMonths(vaccine.recommendedAgeMonths.toLong())
            val (scheduledDate, shiftReason, shiftDays) = findNextValidDate(
                idealDate, vaccinationDays, holidayDates
            )

            val schedule = VaccinationSchedule(
                scheduleId    = UUID.randomUUID().toString(),
                baby          = baby,
                bench         = bench,
                vaccineType   = vaccine,
                idealDate     = idealDate,
                scheduledDate = scheduledDate,
                shiftReason   = shiftReason,
                shiftDays     = shiftDays,
                status        = computeStatus(scheduledDate)
            )
            scheduleRepository.save(schedule)
            created++

            if (shiftDays > 0) {
                adjustmentLogRepository.save(ScheduleAdjustmentLog(
                    logId      = UUID.randomUUID().toString(),
                    schedule   = schedule,
                    baby       = baby,
                    oldDate    = idealDate,
                    newDate    = scheduledDate,
                    reason     = shiftReasonToAdjustmentReason(shiftReason),
                    notes      = "Auto-adjusted by schedule generator",
                    adjustedBy = null,
                    adjustedAt = LocalDateTime.now()
                ))
            }
        }
        logger.info { "Schedule generation complete — created $created schedules for baby ${baby.babyId}" }
    }

    override fun regenerateScheduleOnBenchChange(babyId: String, newBench: VaccinationBench) {
        val upcomingSchedules =
            scheduleRepository.findByBaby_BabyIdAndStatus(babyId, ScheduleStatus.UPCOMING) +
                    scheduleRepository.findByBaby_BabyIdAndStatus(babyId, ScheduleStatus.DUE_SOON)

        val vaccinationDays = newBench.getVaccinationDaysList()
            .mapNotNull { DAY_NAME_MAP[it] }.toSet()
        val baby = upcomingSchedules.firstOrNull()?.baby ?: return
        val holidayDates = loadHolidayDates(
            newBench.benchId, LocalDate.now(), LocalDate.now().plusYears(3)
        )

        for (schedule in upcomingSchedules) {
            val oldDate = schedule.scheduledDate
            val (scheduledDate, shiftReason, shiftDays) = findNextValidDate(
                schedule.idealDate, vaccinationDays, holidayDates
            )
            schedule.bench         = newBench
            schedule.scheduledDate = scheduledDate
            schedule.shiftReason   = shiftReason
            schedule.shiftDays     = shiftDays
            scheduleRepository.save(schedule)

            adjustmentLogRepository.save(ScheduleAdjustmentLog(
                logId      = UUID.randomUUID().toString(),
                schedule   = schedule,
                baby       = baby,
                oldDate    = oldDate,
                newDate    = scheduledDate,
                reason     = AdjustmentReason.BENCH_CHANGED,
                notes      = "Regenerated due to bench change to ${newBench.nameEn}",
                adjustedBy = null,
                adjustedAt = LocalDateTime.now()
            ))
        }
    }

    override fun updateStatusesForBaby(babyId: String) {
        val schedules = scheduleRepository.findByBaby_BabyIdOrderByScheduledDateAsc(babyId)
        for (schedule in schedules) {
            if (schedule.status == ScheduleStatus.COMPLETED ||
                schedule.status == ScheduleStatus.MISSED) continue
            schedule.status = computeStatus(schedule.scheduledDate)
            scheduleRepository.save(schedule)
        }
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
        from: LocalDate,
        vaccinationDays: Set<DayOfWeek>,
        holidayDates: Set<LocalDate>
    ): Triple<LocalDate, ShiftReason, Int> {

        if (vaccinationDays.isEmpty()) {
            logger.warn { "vaccinationDays is empty — returning idealDate unchanged" }
            return Triple(from, ShiftReason.NONE, 0)
        }

        var candidate   = from
        var shiftDays   = 0
        var firstReason = ShiftReason.NONE

        while (shiftDays <= 365) {
            val isWeekend = candidate.dayOfWeek == DayOfWeek.FRIDAY ||
                    candidate.dayOfWeek == DayOfWeek.SATURDAY
            val isHoliday = candidate in holidayDates
            val isVacDay  = candidate.dayOfWeek in vaccinationDays

            when {
                isWeekend -> {
                    if (firstReason == ShiftReason.NONE) firstReason = ShiftReason.WEEKEND
                    candidate = candidate.plusDays(1)
                    shiftDays++
                }
                isHoliday -> {
                    if (firstReason == ShiftReason.NONE) firstReason = ShiftReason.HOLIDAY
                    candidate = candidate.plusDays(1)
                    shiftDays++
                }
                !isVacDay -> {
                    candidate = candidate.plusDays(1)
                    shiftDays++
                }
                else -> return Triple(candidate, firstReason, shiftDays)
            }
        }

        logger.warn {
            "Could not find a valid vaccination date within 365 days of $from — returning original date"
        }
        return Triple(from, ShiftReason.NONE, 0)
    }

    private fun loadHolidayDates(benchId: String, from: LocalDate, to: LocalDate): Set<LocalDate> =
        holidayRepository.findHolidaysForBenchInRange(benchId, from, to)
            .map { it.holidayDate }.toSet()

    private fun shiftReasonToAdjustmentReason(reason: ShiftReason): AdjustmentReason = when (reason) {
        ShiftReason.HOLIDAY      -> AdjustmentReason.HOLIDAY
        ShiftReason.BENCH_CLOSED -> AdjustmentReason.BENCH_CLOSED
        ShiftReason.WEEKEND      -> AdjustmentReason.BENCH_CLOSED
        ShiftReason.MISSED       -> AdjustmentReason.PARENT_MISSED
        ShiftReason.RESCHEDULED  -> AdjustmentReason.PARENT_RESCHEDULED
        ShiftReason.NONE         -> AdjustmentReason.BENCH_CLOSED
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
    private val scheduleRepository     : VaccinationScheduleRepository,
    private val userRepository         : UserRepository,
    private val vaccinationRepository  : VaccinationRepository,
    private val adjustmentLogRepository: ScheduleAdjustmentLogRepository,
    private val benchRepository        : VaccinationBenchRepository,
    private val holidayRepository      : BenchHolidayRepository
) : VaccinationScheduleService {

    override fun getScheduleForBaby(babyId: String): List<VaccinationScheduleResponse> =
        scheduleRepository.findByBaby_BabyIdOrderByScheduledDateAsc(babyId)
            .map { it.toResponse() }

    override fun getUpcomingForBaby(babyId: String, daysAhead: Int): List<VaccinationScheduleResponse> {
        val from = LocalDate.now()
        val to   = from.plusDays(daysAhead.toLong())
        return scheduleRepository.findUpcomingForBaby(
            babyId   = babyId,
            from     = from,
            to       = to,
            statuses = listOf(ScheduleStatus.UPCOMING, ScheduleStatus.DUE_SOON)
        ).map { it.toResponse() }
    }

    override fun getOverdueForBaby(babyId: String): List<VaccinationScheduleResponse> =
        scheduleRepository.findOverdueForBaby(babyId, ScheduleStatus.OVERDUE).map { it.toResponse() }

    override fun getScheduleByBenchAndDate(benchId: String, date: LocalDate): BenchDayScheduleResponse {
        val bench = benchRepository.findById(benchId)
            .orElseThrow { ResourceNotFoundException("Bench not found: $benchId") }
        val items = scheduleRepository.findByBenchAndDate(benchId, date).map { it.toResponse() }
        return BenchDayScheduleResponse(benchId, bench.nameEn, date, items.size, items)
    }

    override fun getScheduleByBenchAndRange(benchId: String, from: LocalDate, to: LocalDate) =
        scheduleRepository.findByBenchAndDateRange(benchId, from, to).map { it.toResponse() }

    override fun updateScheduleStatus(
        scheduleId: String,
        request: VaccinationScheduleUpdateRequest
    ): VaccinationScheduleResponse {
        val schedule = scheduleRepository.findById(scheduleId)
            .orElseThrow { ResourceNotFoundException("Schedule not found: $scheduleId") }
        request.status?.let            { schedule.status        = it }
        request.completedDate?.let     { schedule.completedDate = it }
        request.completedByUserId?.let { schedule.completedBy   = userRepository.findById(it).orElse(null) }
        request.vaccinationId?.let     { schedule.vaccination   = vaccinationRepository.findById(it).orElse(null) }
        return scheduleRepository.save(schedule).toResponse()
    }

    override fun adjustScheduleDate(
        adjustedByUserId: String,
        request: ScheduleAdjustmentRequest
    ): VaccinationScheduleResponse {
        val schedule   = scheduleRepository.findById(request.scheduleId)
            .orElseThrow { ResourceNotFoundException("Schedule not found: ${request.scheduleId}") }
        val adjustedBy = userRepository.findById(adjustedByUserId).orElse(null)
        adjustmentLogRepository.save(ScheduleAdjustmentLog(
            logId      = UUID.randomUUID().toString(),
            schedule   = schedule,
            baby       = schedule.baby,
            oldDate    = schedule.scheduledDate,
            newDate    = request.newDate,
            reason     = request.reason,
            notes      = request.notes,
            adjustedBy = adjustedBy,
            adjustedAt = LocalDateTime.now()
        ))
        schedule.scheduledDate = request.newDate
        schedule.shiftReason   = ShiftReason.RESCHEDULED
        return scheduleRepository.save(schedule).toResponse()
    }

    override fun getAdjustmentHistory(scheduleId: String): List<ScheduleAdjustmentLogResponse> =
        adjustmentLogRepository.findBySchedule_ScheduleIdOrderByAdjustedAtDesc(scheduleId)
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
        vaccineNameAr        = vaccineType?.vaccineNameAr,
        vaccineNameKu        = vaccineType?.vaccineNameKu,
        vaccineNameCkb       = vaccineType?.vaccineNameCkb,
        description          = vaccineType?.description,
        descriptionAr        = vaccineType?.descriptionAr,
        descriptionKu        = vaccineType?.descriptionKu,
        descriptionCkb       = vaccineType?.descriptionCkb,
        doseNumber           = vaccineType?.doseNumber?.toInt() ?: 1,
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
        createdAt            = createdAt?.toString(),
        updatedAt            = updatedAt?.toString()
    )

    private fun ScheduleAdjustmentLog.toLogResponse() = ScheduleAdjustmentLogResponse(
        logId          = logId,
        scheduleId     = schedule?.scheduleId ?: "",
        babyId         = baby?.babyId ?: "",
        babyName       = baby?.fullName ?: "",
        oldDate        = oldDate,
        newDate        = newDate,
        reason         = reason,
        notes          = notes,
        adjustedByName = adjustedBy?.fullName,
        adjustedAt     = adjustedAt
    )
}