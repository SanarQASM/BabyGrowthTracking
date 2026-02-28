package com.example.backend_side.controllers

import com.example.backend_side.entity.ChildIllnesses
import com.example.backend_side.repositories.ChildIllnessesRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/child-illnesses")
@CrossOrigin(origins = ["*"])
class ChildIllnessesController(private val childIllnessesRepository: ChildIllnessesRepository) {

    @GetMapping
    fun getAllChildIllnesses(): ResponseEntity<List<ChildIllnesses>> {
        return ResponseEntity.ok(childIllnessesRepository.findAll())
    }

    @GetMapping("/{illnessId}")
    fun getChildIllnessById(@PathVariable illnessId: String): ResponseEntity<ChildIllnesses> {
        return childIllnessesRepository.findById(illnessId)
            .map { ResponseEntity.ok(it) }
            .orElse(ResponseEntity.notFound().build())
    }

    @GetMapping("/baby/{babyId}")
    fun getIllnessesByBaby(@PathVariable babyId: String): ResponseEntity<List<ChildIllnesses>> {
        return ResponseEntity.ok(childIllnessesRepository.findByBaby_BabyId(babyId))
    }

    @GetMapping("/baby/{babyId}/active")
    fun getActiveIllnessesByBaby(
        @PathVariable babyId: String,
        @RequestParam(defaultValue = "true") isActive: Boolean
    ): ResponseEntity<List<ChildIllnesses>> {
        return ResponseEntity.ok(childIllnessesRepository.findByBaby_BabyIdAndIsActive(babyId, isActive))
    }

    @GetMapping("/baby/{babyId}/count")
    fun countActiveIllnesses(@PathVariable babyId: String): ResponseEntity<Map<String, Long>> {
        val count = childIllnessesRepository.countActiveIllnessesByBaby(babyId)
        return ResponseEntity.ok(mapOf("count" to count))
    }

    @PostMapping
    fun createChildIllness(@RequestBody childIllness: ChildIllnesses): ResponseEntity<ChildIllnesses> {
        if (childIllness.illnessId.isEmpty()) {
            childIllness.illnessId = UUID.randomUUID().toString()
        }
        val savedIllness = childIllnessesRepository.save(childIllness)
        return ResponseEntity.status(HttpStatus.CREATED).body(savedIllness)
    }

    @PutMapping("/{illnessId}")
    fun updateChildIllness(
        @PathVariable illnessId: String,
        @RequestBody childIllness: ChildIllnesses
    ): ResponseEntity<ChildIllnesses> {
        return if (childIllnessesRepository.existsById(illnessId)) {
            childIllness.illnessId = illnessId
            ResponseEntity.ok(childIllnessesRepository.save(childIllness))
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PatchMapping("/{illnessId}/deactivate")
    fun deactivateIllness(@PathVariable illnessId: String): ResponseEntity<ChildIllnesses> {
        return childIllnessesRepository.findById(illnessId)
            .map { illness ->
                illness.isActive = false
                ResponseEntity.ok(childIllnessesRepository.save(illness))
            }
            .orElse(ResponseEntity.notFound().build())
    }

    @DeleteMapping("/{illnessId}")
    fun deleteChildIllness(@PathVariable illnessId: String): ResponseEntity<Void> {
        return if (childIllnessesRepository.existsById(illnessId)) {
            childIllnessesRepository.deleteById(illnessId)
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }
}