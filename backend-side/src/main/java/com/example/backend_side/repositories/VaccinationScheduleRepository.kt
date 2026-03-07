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
// ============================================================

@Repository
interface VaccinationScheduleRepository : JpaRepository<VaccinationSchedule, String> {

    fun findByBaby_BabyIdOrderByScheduledDateAsc(babyId: String): List<VaccinationSchedule>

    fun findByBaby_BabyIdAndStatus(babyId: String, status: ScheduleStatus): List<VaccinationSchedule>

    fun findByBench_BenchIdOrderByScheduledDateAsc(benchId: String): List<VaccinationSchedule>

    fun findByBaby_BabyIdAndVaccineType_VaccineId(babyId: String, vaccineId: Int): Optional<VaccinationSchedule>

    // Schedules due within the next N days for a baby (for notifications)
    @Query("""
        SELECT s FROM VaccinationSchedule s 
        WHERE s.baby.babyId = :babyId 
        AND s.status IN ('upcoming', 'due_soon')
        AND s.scheduledDate BETWEEN :from AND :to
        ORDER BY s.scheduledDate ASC
    """)
    fun findUpcomingForBaby(
        @Param("babyId") babyId: String,
        @Param("from") from: LocalDate,
        @Param("to") to: LocalDate
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

    // Overdue schedules for a baby
    @Query("""
        SELECT s FROM VaccinationSchedule s 
        WHERE s.baby.babyId = :babyId 
        AND s.status = 'overdue'
        ORDER BY s.scheduledDate ASC
    """)
    fun findOverdueForBaby(@Param("babyId") babyId: String): List<VaccinationSchedule>

    // Count completed vaccinations for a baby
    @Query("SELECT COUNT(s) FROM VaccinationSchedule s WHERE s.baby.babyId = :babyId AND s.status = 'completed'")
    fun countCompletedForBaby(@Param("babyId") babyId: String): Long

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
