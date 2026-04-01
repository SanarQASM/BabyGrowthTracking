package com.example.backend_side.repositories

import com.example.backend_side.entity.ChildDevelopmentVisionMotor
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface ChildDevelopmentVisionMotorRepository : JpaRepository<ChildDevelopmentVisionMotor, String> {

    fun findByBaby_BabyId(babyId: String): List<ChildDevelopmentVisionMotor>

    fun findByBaby_BabyIdAndCheckMonth(babyId: String, checkMonth: Int): Optional<ChildDevelopmentVisionMotor>

    fun existsByBaby_BabyIdAndCheckMonth(babyId: String, checkMonth: Int): Boolean

    fun findByBaby_BabyIdOrderByCheckMonthAsc(babyId: String): List<ChildDevelopmentVisionMotor>
}