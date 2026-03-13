package com.example.backend_side.repositories

import com.example.backend_side.entity.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.Optional

// ============================================================
// VACCINATION SCHEDULE REPOSITORY
//
// FIX: The original JPQL queries used raw string literals
// ('upcoming', 'due_soon', 'overdue') to compare against
// ScheduleStatus enum fields backed by an AttributeConverter.
//
// With JPA AttributeConverter, JPQL string literals in WHERE
// clauses are compared at the Java/enum level, NOT the DB level.
// Hibernate may or may not apply the converter to literal strings,
// making results unpredictable. The safe fix is to pass enum
// values as typed @Param parameters instead of string literals.
// ============================================================

@Repository
interface VaccinationScheduleRepository : JpaRepository<VaccinationSchedule, String> {

    fun findByBaby_BabyIdOrderByScheduledDateAsc(babyId: String): List<VaccinationSchedule>

    // ✅ Safe: Spring Data generates type-safe comparison via converter
    fun findByBaby_BabyIdAndStatus(babyId: String, status: ScheduleStatus): List<VaccinationSchedule>

    fun findByBench_BenchIdOrderByScheduledDateAsc(benchId: String): List<VaccinationSchedule>

    fun findByBaby_BabyIdAndVaccineType_VaccineId(babyId: String, vaccineId: Int): Optional<VaccinationSchedule>

    // ✅ FIX: Use typed enum @Param instead of raw string literals
    // OLD (BROKEN): AND s.status IN ('upcoming', 'due_soon')
    // NEW (FIXED):  AND s.status IN :statuses  ← enum values, converter applied automatically
    @Query("""
        SELECT s FROM VaccinationSchedule s 
        WHERE s.baby.babyId = :babyId 
        AND s.status IN :statuses
        AND s.scheduledDate BETWEEN :from AND :to
        ORDER BY s.scheduledDate ASC
    """)
    fun findUpcomingForBaby(
        @Param("babyId") babyId: String,
        @Param("from") from: LocalDate,
        @Param("to") to: LocalDate,
        @Param("statuses") statuses: List<ScheduleStatus> = listOf(
            ScheduleStatus.UPCOMING, ScheduleStatus.DUE_SOON
        )
    ): List<VaccinationSchedule>

    // All schedules for a bench on a specific date (team's daily list)
    @Query("""
        SELECT s FROM VaccinationSchedule s 
        WHERE s.bench.benchId = :benchId 
        AND s.scheduledDate = :date
        AND s.isVisibleToTeam = true
        ORDER BY s.baby.fullName ASC
    """)
    fun findByBenchAndDate(
        @Param("benchId") benchId: String,
        @Param("date") date: LocalDate
    ): List<VaccinationSchedule>

    // ✅ FIX: Use typed enum @Param instead of raw string literal 'overdue'
    // OLD (BROKEN): AND s.status = 'overdue'
    // NEW (FIXED):  AND s.status = :status  ← typed enum param
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

    // ✅ FIX: Use typed enum @Param for 'completed' string literal
    // OLD (BROKEN): AND s.status = 'completed'
    // NEW (FIXED):  AND s.status = :status
    @Query("SELECT COUNT(s) FROM VaccinationSchedule s WHERE s.baby.babyId = :babyId AND s.status = :status")
    fun countCompletedForBaby(
        @Param("babyId") babyId: String,
        @Param("status") status: ScheduleStatus = ScheduleStatus.COMPLETED
    ): Long

    // All schedules for a bench in a date range (team weekly/monthly view)
    @Query("""
        SELECT s FROM VaccinationSchedule s 
        WHERE s.bench.benchId = :benchId 
        AND s.scheduledDate BETWEEN :from AND :to
        AND s.isVisibleToTeam = true
        ORDER BY s.scheduledDate ASC, s.baby.fullName ASC
    """)
    fun findByBenchAndDateRange(
        @Param("benchId") benchId: String,
        @Param("from") from: LocalDate,
        @Param("to") to: LocalDate
    ): List<VaccinationSchedule>
}