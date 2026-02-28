package com.example.backend_side.repositories

import com.example.backend_side.entity.ChildIllnesses
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface ChildIllnessesRepository : JpaRepository<ChildIllnesses, String> {

    fun findByBaby_BabyId(babyId: String): List<ChildIllnesses>

    fun findByBaby_BabyIdAndIsActive(babyId: String, isActive: Boolean): List<ChildIllnesses>

    fun findByIsActive(isActive: Boolean): List<ChildIllnesses>

    fun findByDiagnosisDateBetween(startDate: LocalDate, endDate: LocalDate): List<ChildIllnesses>

    @Query("SELECT ci FROM ChildIllnesses ci WHERE ci.baby.babyId = :babyId AND LOWER(ci.illnessName) LIKE LOWER(CONCAT('%', :illnessName, '%'))")
    fun findByBabyIdAndIllnessName(@Param("babyId") babyId: String, @Param("illnessName") illnessName: String): List<ChildIllnesses>

    @Query("SELECT COUNT(ci) FROM ChildIllnesses ci WHERE ci.baby.babyId = :babyId AND ci.isActive = true")
    fun countActiveIllnessesByBaby(@Param("babyId") babyId: String): Long
}