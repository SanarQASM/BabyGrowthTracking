package com.example.backend_side.repositories

import com.example.backend_side.entity.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.Optional

// ============================================================
// VACCINATION BENCH REPOSITORY
// ============================================================

@Repository
interface VaccinationBenchRepository : JpaRepository<VaccinationBench, String> {

    fun findByIsActiveTrue(): List<VaccinationBench>

    fun findByGovernorateAndIsActiveTrue(governorate: String): List<VaccinationBench>

    fun findByGovernorate(governorate: String): List<VaccinationBench>

    fun findByType(type: BenchType): List<VaccinationBench>

    fun existsByBenchId(benchId: String): Boolean

    @Query("""
        SELECT b FROM VaccinationBench b 
        WHERE b.isActive = true 
        AND (LOWER(b.nameEn) LIKE LOWER(CONCAT('%', :query, '%'))
          OR LOWER(b.nameAr) LIKE LOWER(CONCAT('%', :query, '%'))
          OR LOWER(b.district) LIKE LOWER(CONCAT('%', :query, '%'))
          OR LOWER(b.governorate) LIKE LOWER(CONCAT('%', :query, '%')))
    """)
    fun searchBenches(@Param("query") query: String): List<VaccinationBench>

    @Query("SELECT DISTINCT b.governorate FROM VaccinationBench b WHERE b.isActive = true ORDER BY b.governorate")
    fun findAllGovernorates(): List<String>
}