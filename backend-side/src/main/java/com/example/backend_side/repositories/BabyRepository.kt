package com.example.backend_side.repositories

import com.example.backend_side.entity.Baby
import com.example.backend_side.entity.Gender
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.*

@Repository
interface BabyRepository : JpaRepository<Baby, String> {

    fun findByParentUser_UserId(parentUserId: String): List<Baby>

    fun findByParentUser_UserIdAndIsActive(parentUserId: String, isActive: Boolean): List<Baby>

    fun findByIsActive(isActive: Boolean): List<Baby>

    fun findByGender(gender: Gender): List<Baby>

    fun findByDateOfBirthBetween(startDate: LocalDate, endDate: LocalDate): List<Baby>

    @Query("SELECT b FROM Baby b WHERE b.parentUser.userId = :parentUserId AND b.babyId = :babyId")
    fun findByParentAndBabyId(@Param("parentUserId") parentUserId: String, @Param("babyId") babyId: String): Optional<Baby>

    @Query("SELECT b FROM Baby b WHERE TIMESTAMPDIFF(MONTH, b.dateOfBirth, CURRENT_DATE) BETWEEN :minMonths AND :maxMonths")
    fun findByAgeRangeInMonths(@Param("minMonths") minMonths: Int, @Param("maxMonths") maxMonths: Int): List<Baby>

    @Query("SELECT COUNT(b) FROM Baby b WHERE b.parentUser.userId = :parentUserId AND b.isActive = true")
    fun countActiveBabiesByParent(@Param("parentUserId") parentUserId: String): Long

    @Query("SELECT b FROM Baby b WHERE LOWER(b.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    fun searchByName(@Param("searchTerm") searchTerm: String): List<Baby>
}