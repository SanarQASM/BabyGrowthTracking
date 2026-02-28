package com.example.backend_side.repositories

import com.example.backend_side.entity.PreCheckInvestigation
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.*

@Repository
interface PreCheckInvestigationRepository : JpaRepository<PreCheckInvestigation, String> {

    fun findByBaby_BabyId(babyId: String): List<PreCheckInvestigation>

    fun findByBaby_BabyIdOrderByCheckDateDesc(babyId: String): List<PreCheckInvestigation>

    fun findByCheckDateBetween(startDate: LocalDate, endDate: LocalDate): List<PreCheckInvestigation>

    fun findByConductedBy_UserId(conductedById: String): List<PreCheckInvestigation>

    @Query("SELECT pci FROM PreCheckInvestigation pci WHERE pci.baby.babyId = :babyId ORDER BY pci.checkDate DESC")
    fun findLatestByBabyId(@Param("babyId") babyId: String): Optional<PreCheckInvestigation>

    @Query("SELECT COUNT(pci) FROM PreCheckInvestigation pci WHERE pci.baby.babyId = :babyId")
    fun countChecksByBaby(@Param("babyId") babyId: String): Long
}