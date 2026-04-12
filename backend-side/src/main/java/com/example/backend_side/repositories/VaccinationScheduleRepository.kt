package com.example.backend_side.repositories

import com.example.backend_side.entity.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.Optional

@Repository
interface VaccinationScheduleRepository : JpaRepository<VaccinationSchedule, String> {

    fun findByBaby_BabyIdOrderByScheduledDateAsc(babyId: String): List<VaccinationSchedule>

    fun findByBaby_BabyIdAndStatus(babyId: String, status: ScheduleStatus): List<VaccinationSchedule>

    fun findByBench_BenchIdOrderByScheduledDateAsc(benchId: String): List<VaccinationSchedule>

    fun findByBaby_BabyIdAndVaccineType_VaccineId(babyId: String, vaccineId: Int): Optional<VaccinationSchedule>

    @Query("""
        SELECT s FROM VaccinationSchedule s
        WHERE s.baby.babyId = :babyId
        AND s.status IN :statuses
        AND s.scheduledDate BETWEEN :from AND :to
        ORDER BY s.scheduledDate ASC
    """)
    fun findUpcomingForBaby(
        @Param("babyId")    babyId   : String,
        @Param("from")      from     : LocalDate,
        @Param("to")        to       : LocalDate,
        @Param("statuses")  statuses : List<ScheduleStatus> = listOf(
            ScheduleStatus.UPCOMING, ScheduleStatus.DUE_SOON
        )
    ): List<VaccinationSchedule>

    // NEW: global upcoming query (all babies) — used by PushNotificationScheduler
    @Query("""
        SELECT s FROM VaccinationSchedule s
        WHERE s.status IN :statuses
        AND s.scheduledDate BETWEEN :from AND :to
        ORDER BY s.scheduledDate ASC
    """)
    fun findAllByStatusIn(
        @Param("statuses") statuses : List<ScheduleStatus>,
        @Param("from")     from     : LocalDate,
        @Param("to")       to       : LocalDate
    ): List<VaccinationSchedule>

    // NEW: global overdue query (all babies) — used by PushNotificationScheduler
    @Query("""
        SELECT s FROM VaccinationSchedule s
        WHERE s.status = :status
        ORDER BY s.scheduledDate ASC
    """)
    fun findAllByStatus(
        @Param("status") status: ScheduleStatus
    ): List<VaccinationSchedule>

    @Query("""
        SELECT s FROM VaccinationSchedule s
        WHERE s.bench.benchId = :benchId
        AND s.scheduledDate = :date
        AND s.isVisibleToTeam = true
        ORDER BY s.baby.fullName ASC
    """)
    fun findByBenchAndDate(
        @Param("benchId") benchId: String,
        @Param("date")    date   : LocalDate
    ): List<VaccinationSchedule>

    @Query("""
        SELECT s FROM VaccinationSchedule s
        WHERE s.baby.babyId = :babyId
        AND s.status = :status
        ORDER BY s.scheduledDate ASC
    """)
    fun findOverdueForBaby(
        @Param("babyId") babyId: String,
        @Param("status") status: ScheduleStatus = ScheduleStatus.OVERDUE
    ): List<VaccinationSchedule>

    @Query("SELECT COUNT(s) FROM VaccinationSchedule s WHERE s.baby.babyId = :babyId AND s.status = :status")
    fun countCompletedForBaby(
        @Param("babyId") babyId: String,
        @Param("status") status: ScheduleStatus = ScheduleStatus.COMPLETED
    ): Long

    @Query("""
        SELECT s FROM VaccinationSchedule s
        WHERE s.bench.benchId = :benchId
        AND s.scheduledDate BETWEEN :from AND :to
        AND s.isVisibleToTeam = true
        ORDER BY s.scheduledDate ASC, s.baby.fullName ASC
    """)
    fun findByBenchAndDateRange(
        @Param("benchId") benchId: String,
        @Param("from")    from   : LocalDate,
        @Param("to")      to     : LocalDate
    ): List<VaccinationSchedule>
}