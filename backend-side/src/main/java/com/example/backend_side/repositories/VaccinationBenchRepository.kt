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

    fun findByGovernorateIgnoreCaseAndIsActiveTrue(governorate: String): List<VaccinationBench>

    // ── NEW: find the bench managed by a specific team member ──────────────
    // Returns Optional so the service can return null gracefully when the
    // team member has no bench assigned yet.
    fun findByTeamMember_UserIdAndIsActiveTrue(teamMemberId: String): Optional<VaccinationBench>

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

    @Query("SELECT DISTINCT b.governorate FROM VaccinationBench b WHERE b.isActive = true ORDER BY b.governorate")
    fun findDistinctGovernorates(): List<String>
}