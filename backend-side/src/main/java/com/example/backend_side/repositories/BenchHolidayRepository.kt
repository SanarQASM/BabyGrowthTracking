package com.example.backend_side.repositories

import com.example.backend_side.entity.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate

// ============================================================
// BENCH HOLIDAY REPOSITORY
// ============================================================

@Repository
interface BenchHolidayRepository : JpaRepository<BenchHoliday, String> {

    fun findByIsNationalTrue(): List<BenchHoliday>

    fun findByBench_BenchId(benchId: String): List<BenchHoliday>

    fun findByHolidayDateBetween(start: LocalDate, end: LocalDate): List<BenchHoliday>

    // Check if a specific date is a holiday for a bench (bench-specific OR national)
    @Query("""
        SELECT CASE WHEN COUNT(h) > 0 THEN true ELSE false END 
        FROM BenchHoliday h 
        WHERE h.holidayDate = :date 
        AND (h.isNational = true OR h.bench.benchId = :benchId)
    """)
    fun isHolidayForBench(
        @Param("date") date: LocalDate,
        @Param("benchId") benchId: String
    ): Boolean

    // Get all holidays for a bench in a date range (bench-specific + national)
    @Query("""
        SELECT h FROM BenchHoliday h 
        WHERE h.holidayDate BETWEEN :start AND :end 
        AND (h.isNational = true OR h.bench.benchId = :benchId)
        ORDER BY h.holidayDate ASC
    """)
    fun findHolidaysForBenchInRange(
        @Param("benchId") benchId: String,
        @Param("start") start: LocalDate,
        @Param("end") end: LocalDate
    ): List<BenchHoliday>
}