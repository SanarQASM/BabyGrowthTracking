package com.example.backend_side.repositories

import com.example.backend_side.entity.FamilyHistory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface FamilyHistoryRepository : JpaRepository<FamilyHistory, String> {

    fun findByBaby_BabyId(babyId: String): Optional<FamilyHistory>

    fun existsByBaby_BabyId(babyId: String): Boolean
}