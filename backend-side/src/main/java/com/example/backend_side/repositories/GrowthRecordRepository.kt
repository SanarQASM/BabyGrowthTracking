package com.example.backend_side.repositories
import com.example.backend_side.entity.GrowthRecord
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.Optional

@Repository
interface GrowthRecordRepository : JpaRepository<GrowthRecord, String>, JpaSpecificationExecutor<GrowthRecord> {

    // Keep your existing methods
    fun findByBabyBabyIdOrderByMeasurementDateDesc(babyId: String): List<GrowthRecord>
    fun findByBabyBabyId(babyId: String): List<GrowthRecord>
    fun findTopByBabyBabyIdOrderByMeasurementDateDesc(babyId: String): Optional<GrowthRecord>

    // ADD these aliases for the service
    fun findByBaby_BabyIdOrderByMeasurementDateDesc(babyId: String): List<GrowthRecord> {
        return findByBabyBabyIdOrderByMeasurementDateDesc(babyId)
    }

    @Query("SELECT gr FROM GrowthRecord gr WHERE gr.baby.babyId = :babyId ORDER BY gr.measurementDate DESC LIMIT 1")
    fun findLatestByBabyId(@Param("babyId") babyId: String): Optional<GrowthRecord>

    @Query("SELECT gr FROM GrowthRecord gr WHERE gr.baby.babyId = :babyId AND gr.measurementDate BETWEEN :startDate AND :endDate")
    fun findByBabyAndDateRange(
        @Param("babyId") babyId: String,
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate
    ): List<GrowthRecord>
}