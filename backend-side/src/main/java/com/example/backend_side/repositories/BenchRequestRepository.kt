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

    // FIX: replaced raw string literals ('pending','accepted') with typed enum parameter
    // Raw JPQL string literals bypass the LowercaseEnumConverter and are fragile —
    // if the converter changes, the query silently breaks.
    @Query("""
        SELECT r FROM BenchRequest r
        WHERE r.baby.babyId = :babyId
        AND r.status IN :statuses
        ORDER BY r.createdAt DESC
    """)
    fun findActiveRequestForBaby(
        @Param("babyId") babyId: String,
        @Param("statuses") statuses: List<BenchRequestStatus> = listOf(
            BenchRequestStatus.PENDING,
            BenchRequestStatus.ACCEPTED
        )
    ): Optional<BenchRequest>

    fun existsByBaby_BabyIdAndStatus(babyId: String, status: BenchRequestStatus): Boolean

    // FIX: replaced raw string literal ('pending') with typed enum parameter
    @Query("""
        SELECT r FROM BenchRequest r
        WHERE r.bench.benchId = :benchId
        AND r.status = :status
        ORDER BY r.createdAt ASC
    """)
    fun findPendingForBench(
        @Param("benchId") benchId: String,
        @Param("status") status: BenchRequestStatus = BenchRequestStatus.PENDING
    ): List<BenchRequest>
}