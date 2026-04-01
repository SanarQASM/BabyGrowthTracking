package com.example.backend_side.repositories

import com.example.backend_side.entity.ChildDevelopmentHearingSpeech
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface ChildDevelopmentHearingSpeechRepository : JpaRepository<ChildDevelopmentHearingSpeech, String> {

    fun findByBaby_BabyId(babyId: String): List<ChildDevelopmentHearingSpeech>

    fun findByBaby_BabyIdAndCheckMonth(babyId: String, checkMonth: Int): Optional<ChildDevelopmentHearingSpeech>

    fun existsByBaby_BabyIdAndCheckMonth(babyId: String, checkMonth: Int): Boolean

    fun findByBaby_BabyIdOrderByCheckMonthAsc(babyId: String): List<ChildDevelopmentHearingSpeech>
}