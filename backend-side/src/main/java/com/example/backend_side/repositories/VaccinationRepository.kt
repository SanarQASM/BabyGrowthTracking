package com.example.backend_side.repositories

import com.example.backend_side.entity.Vaccination
import com.example.backend_side.entity.VaccinationStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.*

@Repository
interface VaccinationRepository : JpaRepository<Vaccination, String> {

    fun findByBaby_BabyId(babyId: String): List<Vaccination>

    fun findByBaby_BabyIdOrderByScheduledDateAsc(babyId: String): List<Vaccination>

    fun findByStatus(status: VaccinationStatus): List<Vaccination>

    fun findByBaby_BabyIdAndStatus(babyId: String, status: VaccinationStatus): List<Vaccination>

    fun findByScheduledDateBetween(startDate: LocalDate, endDate: LocalDate): List<Vaccination>

    fun findByAdministeredDateBetween(startDate: LocalDate, endDate: LocalDate): List<Vaccination>

    fun findByAdministeredBy_UserId(administeredById: String): List<Vaccination>

    @Query("SELECT v FROM Vaccination v WHERE v.baby.babyId = :babyId AND v.vaccineType.vaccineId = :vaccineId")
    fun findByBabyIdAndVaccineId(
        @Param("babyId") babyId: String,
        @Param("vaccineId") vaccineId: Int
    ): Optional<Vaccination>

    @Query("SELECT v FROM Vaccination v WHERE v.status = :status AND v.scheduledDate BETWEEN :startDate AND :endDate")
    fun findUpcomingVaccinations(
        @Param("status") status: VaccinationStatus,
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate
    ): List<Vaccination>

    @Query("SELECT COUNT(v) FROM Vaccination v WHERE v.baby.babyId = :babyId AND v.status = :status")
    fun countByBabyIdAndStatus(@Param("babyId") babyId: String, @Param("status") status: VaccinationStatus): Long

    @Query("SELECT v FROM Vaccination v WHERE v.baby.babyId = :babyId AND v.status = 'SCHEDULED' ORDER BY v.scheduledDate ASC")
    fun findNextScheduledVaccinationForBaby(@Param("babyId") babyId: String): Optional<Vaccination>
}