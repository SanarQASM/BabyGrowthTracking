package com.example.backend_side.repositories

import com.example.backend_side.entity.VaccinationBench
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface VaccinationBenchRepository : JpaRepository<VaccinationBench, String> {

    fun findByIsActiveTrue(): List<VaccinationBench>

    // FIX: was findByGovernorateAndIsActiveTrue (exact case) — must match IgnoreCase variant
    fun findByGovernorateIgnoreCaseAndIsActiveTrue(governorate: String): List<VaccinationBench>

    fun findByTeamMember_UserIdAndIsActiveTrue(teamMemberId: String): Optional<VaccinationBench>

    // FIX ADDED: was missing — called by loadBenchesFromJson in BenchServices.kt
    fun existsByNameEn(nameEn: String): Boolean

    @Query("""
        SELECT b FROM VaccinationBench b
        WHERE b.isActive = true AND (
            LOWER(b.nameEn)      LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(b.nameAr)      LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(b.governorate) LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(b.district)    LIKE LOWER(CONCAT('%', :q, '%'))
        )
    """)
    fun searchBenches(@Param("q") query: String): List<VaccinationBench>

    // FIX: was findDistinctGovernorates — renamed to findAllGovernorates to match service call
    // ALSO keep the old name as alias so both compile
    @Query("SELECT DISTINCT b.governorate FROM VaccinationBench b WHERE b.isActive = true ORDER BY b.governorate")
    fun findDistinctGovernorates(): List<String>

    // FIX ADDED: alias used by VaccinationBenchServiceImpl.getGovernorates()
    @Query("SELECT DISTINCT b.governorate FROM VaccinationBench b WHERE b.isActive = true ORDER BY b.governorate")
    fun findAllGovernorates(): List<String>
}