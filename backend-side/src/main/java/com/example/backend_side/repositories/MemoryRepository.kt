package com.example.backend_side.repositories

import com.example.backend_side.entity.Memory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface MemoryRepository : JpaRepository<Memory, String> {

    fun findByBaby_BabyId(babyId: String): List<Memory>

    fun findByBaby_BabyIdOrderByMemoryDateDesc(babyId: String): List<Memory>

    fun findByParentUser_UserId(parentUserId: String): List<Memory>

    fun findByMemoryDateBetween(startDate: LocalDate, endDate: LocalDate): List<Memory>

    @Query("SELECT m FROM Memory m WHERE m.baby.babyId = :babyId AND m.memoryDate BETWEEN :startDate AND :endDate ORDER BY m.memoryDate DESC")
    fun findByBabyIdAndDateRange(
        @Param("babyId") babyId: String,
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate
    ): List<Memory>

    @Query("SELECT m FROM Memory m WHERE m.baby.babyId = :babyId AND m.ageInMonths = :ageInMonths")
    fun findByBabyIdAndAgeInMonths(@Param("babyId") babyId: String, @Param("ageInMonths") ageInMonths: Int): List<Memory>

    @Query("SELECT COUNT(m) FROM Memory m WHERE m.baby.babyId = :babyId")
    fun countMemoriesByBaby(@Param("babyId") babyId: String): Long

    @Query("SELECT m FROM Memory m WHERE LOWER(m.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(m.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    fun searchMemories(@Param("searchTerm") searchTerm: String): List<Memory>
}