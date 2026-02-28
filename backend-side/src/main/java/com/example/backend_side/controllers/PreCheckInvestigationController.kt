package com.example.backend_side.controllers

import com.example.backend_side.entity.PreCheckInvestigation
import com.example.backend_side.repositories.PreCheckInvestigationRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/precheck-investigations")
@CrossOrigin(origins = ["*"])
class PreCheckInvestigationController(
    private val preCheckInvestigationRepository: PreCheckInvestigationRepository
) {

    @GetMapping
    fun getAllInvestigations(): ResponseEntity<List<PreCheckInvestigation>> {
        return ResponseEntity.ok(preCheckInvestigationRepository.findAll())
    }

    @GetMapping("/{investigationId}")
    fun getInvestigationById(@PathVariable investigationId: String): ResponseEntity<PreCheckInvestigation> {
        return preCheckInvestigationRepository.findById(investigationId)
            .map { ResponseEntity.ok(it) }
            .orElse(ResponseEntity.notFound().build())
    }

    @GetMapping("/baby/{babyId}")
    fun getInvestigationsByBaby(@PathVariable babyId: String): ResponseEntity<List<PreCheckInvestigation>> {
        return ResponseEntity.ok(preCheckInvestigationRepository.findByBaby_BabyIdOrderByCheckDateDesc(babyId))
    }

    @GetMapping("/baby/{babyId}/latest")
    fun getLatestInvestigationForBaby(@PathVariable babyId: String): ResponseEntity<PreCheckInvestigation> {
        return preCheckInvestigationRepository.findLatestByBabyId(babyId)
            .map { ResponseEntity.ok(it) }
            .orElse(ResponseEntity.notFound().build())
    }

    @GetMapping("/baby/{babyId}/count")
    fun countInvestigationsByBaby(@PathVariable babyId: String): ResponseEntity<Map<String, Long>> {
        val count = preCheckInvestigationRepository.countChecksByBaby(babyId)
        return ResponseEntity.ok(mapOf("count" to count))
    }

    @GetMapping("/conductor/{conductedById}")
    fun getInvestigationsByConductor(@PathVariable conductedById: String): ResponseEntity<List<PreCheckInvestigation>> {
        return ResponseEntity.ok(preCheckInvestigationRepository.findByConductedBy_UserId(conductedById))
    }

    @PostMapping
    fun createInvestigation(@RequestBody investigation: PreCheckInvestigation): ResponseEntity<PreCheckInvestigation> {
        if (investigation.investigationId.isEmpty()) {
            investigation.investigationId = UUID.randomUUID().toString()
        }
        val savedInvestigation = preCheckInvestigationRepository.save(investigation)
        return ResponseEntity.status(HttpStatus.CREATED).body(savedInvestigation)
    }

    @PutMapping("/{investigationId}")
    fun updateInvestigation(
        @PathVariable investigationId: String,
        @RequestBody investigation: PreCheckInvestigation
    ): ResponseEntity<PreCheckInvestigation> {
        return if (preCheckInvestigationRepository.existsById(investigationId)) {
            investigation.investigationId = investigationId
            ResponseEntity.ok(preCheckInvestigationRepository.save(investigation))
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @DeleteMapping("/{investigationId}")
    fun deleteInvestigation(@PathVariable investigationId: String): ResponseEntity<Void> {
        return if (preCheckInvestigationRepository.existsById(investigationId)) {
            preCheckInvestigationRepository.deleteById(investigationId)
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }
}