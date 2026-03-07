package com.example.backend_side.repositories

import com.example.backend_side.entity.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional
// ============================================================
// BABY BENCH ASSIGNMENT REPOSITORY
// ============================================================

@Repository
interface BabyBenchAssignmentRepository : JpaRepository<BabyBenchAssignment, String> {

    // The one currently active assignment for a baby
    fun findByBaby_BabyIdAndIsActiveTrue(babyId: String): Optional<BabyBenchAssignment>

    fun findByBaby_BabyId(babyId: String): List<BabyBenchAssignment>

    fun findByBench_BenchIdAndIsActiveTrue(benchId: String): List<BabyBenchAssignment>

    fun existsByBaby_BabyIdAndIsActiveTrue(babyId: String): Boolean

    // All babies currently assigned to a specific bench (for team view)
    @Query("""
        SELECT a FROM BabyBenchAssignment a 
        WHERE a.bench.benchId = :benchId AND a.isActive = true
        ORDER BY a.assignedAt DESC
    """)
    fun findActiveBabiesForBench(@Param("benchId") benchId: String): List<BabyBenchAssignment>
}