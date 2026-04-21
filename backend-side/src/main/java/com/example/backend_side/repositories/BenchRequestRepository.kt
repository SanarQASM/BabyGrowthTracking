package com.example.backend_side.repositories

import com.example.backend_side.entity.BenchRequest
import com.example.backend_side.entity.BenchRequestStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface BenchRequestRepository : JpaRepository<BenchRequest, String> {

    fun findByBaby_BabyIdAndStatus(babyId: String, status: BenchRequestStatus): Optional<BenchRequest>

    fun findByBaby_BabyId(babyId: String): List<BenchRequest>

    fun findByBench_BenchIdAndStatus(benchId: String, status: BenchRequestStatus): List<BenchRequest>

    fun findByBench_BenchId(benchId: String): List<BenchRequest>

    @Query("""
        SELECT r FROM BenchRequest r
        WHERE r.baby.babyId = :babyId
        AND r.status IN ('pending','accepted')
        ORDER BY r.createdAt DESC
    """)
    fun findActiveRequestForBaby(@Param("babyId") babyId: String): Optional<BenchRequest>

    fun existsByBaby_BabyIdAndStatus(babyId: String, status: BenchRequestStatus): Boolean

    @Query("""
        SELECT r FROM BenchRequest r
        WHERE r.bench.benchId = :benchId
        AND r.status = 'pending'
        ORDER BY r.createdAt ASC
    """)
    fun findPendingForBench(@Param("benchId") benchId: String): List<BenchRequest>
}