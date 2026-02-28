package com.example.backend_side.controllers

import com.example.backend_side.entity.HealthIssue
import com.example.backend_side.entity.Severity
import com.example.backend_side.repositories.HealthIssueRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.util.*

@RestController
@RequestMapping("/api/health-issues")
@CrossOrigin(origins = ["*"])
class HealthIssueController(private val healthIssueRepository: HealthIssueRepository) {

    @GetMapping
    fun getAllHealthIssues(): ResponseEntity<List<HealthIssue>> {
        return ResponseEntity.ok(healthIssueRepository.findAll())
    }

    @GetMapping("/{issueId}")
    fun getHealthIssueById(@PathVariable issueId: String): ResponseEntity<HealthIssue> {
        return healthIssueRepository.findById(issueId)
            .map { ResponseEntity.ok(it) }
            .orElse(ResponseEntity.notFound().build())
    }

    @GetMapping("/baby/{babyId}")
    fun getHealthIssuesByBaby(@PathVariable babyId: String): ResponseEntity<List<HealthIssue>> {
        return ResponseEntity.ok(healthIssueRepository.findByBaby_BabyIdOrderByIssueDateDesc(babyId))
    }

    @GetMapping("/baby/{babyId}/unresolved")
    fun getUnresolvedHealthIssuesByBaby(@PathVariable babyId: String): ResponseEntity<List<HealthIssue>> {
        return ResponseEntity.ok(healthIssueRepository.findByBaby_BabyIdAndIsResolved(babyId, false))
    }

    @GetMapping("/baby/{babyId}/severity/{severity}")
    fun getUnresolvedBySeverity(
        @PathVariable babyId: String,
        @PathVariable severity: Severity
    ): ResponseEntity<List<HealthIssue>> {
        return ResponseEntity.ok(healthIssueRepository.findUnresolvedBySeverity(babyId, severity))
    }

    @GetMapping("/baby/{babyId}/count-unresolved")
    fun countUnresolvedIssues(@PathVariable babyId: String): ResponseEntity<Map<String, Long>> {
        val count = healthIssueRepository.countUnresolvedIssuesByBaby(babyId)
        return ResponseEntity.ok(mapOf("count" to count))
    }

    @GetMapping("/severity/{severity}")
    fun getHealthIssuesBySeverity(@PathVariable severity: Severity): ResponseEntity<List<HealthIssue>> {
        return ResponseEntity.ok(healthIssueRepository.findBySeverity(severity))
    }

    @GetMapping("/search")
    fun searchHealthIssues(@RequestParam searchTerm: String): ResponseEntity<List<HealthIssue>> {
        return ResponseEntity.ok(healthIssueRepository.searchHealthIssues(searchTerm))
    }

    @PostMapping
    fun createHealthIssue(@RequestBody healthIssue: HealthIssue): ResponseEntity<HealthIssue> {
        if (healthIssue.issueId.isEmpty()) {
            healthIssue.issueId = UUID.randomUUID().toString()
        }
        val savedIssue = healthIssueRepository.save(healthIssue)
        return ResponseEntity.status(HttpStatus.CREATED).body(savedIssue)
    }

    @PutMapping("/{issueId}")
    fun updateHealthIssue(
        @PathVariable issueId: String,
        @RequestBody healthIssue: HealthIssue
    ): ResponseEntity<HealthIssue> {
        return if (healthIssueRepository.existsById(issueId)) {
            healthIssue.issueId = issueId
            ResponseEntity.ok(healthIssueRepository.save(healthIssue))
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PatchMapping("/{issueId}/resolve")
    fun resolveHealthIssue(@PathVariable issueId: String): ResponseEntity<HealthIssue> {
        return healthIssueRepository.findById(issueId)
            .map { issue ->
                issue.isResolved = true
                issue.resolutionDate = LocalDate.now()
                ResponseEntity.ok(healthIssueRepository.save(issue))
            }
            .orElse(ResponseEntity.notFound().build())
    }

    @DeleteMapping("/{issueId}")
    fun deleteHealthIssue(@PathVariable issueId: String): ResponseEntity<Void> {
        return if (healthIssueRepository.existsById(issueId)) {
            healthIssueRepository.deleteById(issueId)
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }
}