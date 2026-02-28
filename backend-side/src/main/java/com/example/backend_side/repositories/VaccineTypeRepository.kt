package com.example.backend_side.repositories

import com.example.backend_side.entity.VaccineType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface VaccineTypeRepository : JpaRepository<VaccineType, Int> {

    fun findByIsMandatory(isMandatory: Boolean): List<VaccineType>

    fun findByRecommendedAgeMonths(recommendedAgeMonths: Int): List<VaccineType>

    fun findByRecommendedAgeMonthsLessThanEqual(recommendedAgeMonths: Int): List<VaccineType>

    fun findByRecommendedAgeMonthsBetween(minMonths: Int, maxMonths: Int): List<VaccineType>

    @Query("SELECT vt FROM VaccineType vt WHERE LOWER(vt.vaccineName) = LOWER(:vaccineName) AND vt.doseNumber = :doseNumber")
    fun findByVaccineNameAndDoseNumber(
        @Param("vaccineName") vaccineName: String,
        @Param("doseNumber") doseNumber: Int
    ): Optional<VaccineType>

    @Query("SELECT vt FROM VaccineType vt WHERE LOWER(vt.vaccineName) LIKE LOWER(CONCAT('%', :vaccineName, '%'))")
    fun findByVaccineNameContaining(@Param("vaccineName") vaccineName: String): List<VaccineType>

    @Query("SELECT vt FROM VaccineType vt ORDER BY vt.recommendedAgeMonths, vt.doseNumber")
    fun findAllOrderedByAgeAndDose(): List<VaccineType>
}