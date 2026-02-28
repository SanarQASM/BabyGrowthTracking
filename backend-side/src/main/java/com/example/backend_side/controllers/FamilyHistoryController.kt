package com.example.backend_side.controllers

import com.example.backend_side.entity.FamilyHistory
import com.example.backend_side.repositories.FamilyHistoryRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/family-history")
@CrossOrigin(origins = ["*"])
class FamilyHistoryController(private val familyHistoryRepository: FamilyHistoryRepository) {

    @GetMapping
    fun getAllFamilyHistories(): ResponseEntity<List<FamilyHistory>> {
        return ResponseEntity.ok(familyHistoryRepository.findAll())
    }

    @GetMapping("/{historyId}")
    fun getFamilyHistoryById(@PathVariable historyId: String): ResponseEntity<FamilyHistory> {
        return familyHistoryRepository.findById(historyId)
            .map { ResponseEntity.ok(it) }
            .orElse(ResponseEntity.notFound().build())
    }

    @GetMapping("/baby/{babyId}")
    fun getFamilyHistoryByBabyId(@PathVariable babyId: String): ResponseEntity<FamilyHistory> {
        return familyHistoryRepository.findByBaby_BabyId(babyId)
            .map { ResponseEntity.ok(it) }
            .orElse(ResponseEntity.notFound().build())
    }

    @PostMapping
    fun createFamilyHistory(@RequestBody familyHistory: FamilyHistory): ResponseEntity<Any> {
        if (familyHistory.historyId.isEmpty()) {
            familyHistory.historyId = UUID.randomUUID().toString()
        }

        // Check if family history already exists for this baby
        if (familyHistory.baby?.babyId?.let { familyHistoryRepository.existsByBaby_BabyId(it) } == true) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(mapOf("error" to "Family history already exists for this baby"))
        }

        val savedHistory = familyHistoryRepository.save(familyHistory)
        return ResponseEntity.status(HttpStatus.CREATED).body(savedHistory)
    }

    @PutMapping("/{historyId}")
    fun updateFamilyHistory(
        @PathVariable historyId: String,
        @RequestBody familyHistory: FamilyHistory
    ): ResponseEntity<FamilyHistory> {
        return if (familyHistoryRepository.existsById(historyId)) {
            familyHistory.historyId = historyId
            ResponseEntity.ok(familyHistoryRepository.save(familyHistory))
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @DeleteMapping("/{historyId}")
    fun deleteFamilyHistory(@PathVariable historyId: String): ResponseEntity<Void> {
        return if (familyHistoryRepository.existsById(historyId)) {
            familyHistoryRepository.deleteById(historyId)
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }
}