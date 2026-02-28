package com.example.backend_side.controllers

import com.example.backend_side.entity.Memory
import com.example.backend_side.repositories.MemoryRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.util.*

@RestController
@RequestMapping("/api/memories")
@CrossOrigin(origins = ["*"])
class MemoryController(private val memoryRepository: MemoryRepository) {

    @GetMapping
    fun getAllMemories(): ResponseEntity<List<Memory>> {
        return ResponseEntity.ok(memoryRepository.findAll())
    }

    @GetMapping("/{memoryId}")
    fun getMemoryById(@PathVariable memoryId: String): ResponseEntity<Memory> {
        return memoryRepository.findById(memoryId)
            .map { ResponseEntity.ok(it) }
            .orElse(ResponseEntity.notFound().build())
    }

    @GetMapping("/baby/{babyId}")
    fun getMemoriesByBaby(@PathVariable babyId: String): ResponseEntity<List<Memory>> {
        return ResponseEntity.ok(memoryRepository.findByBaby_BabyIdOrderByMemoryDateDesc(babyId))
    }

    @GetMapping("/parent/{parentUserId}")
    fun getMemoriesByParent(@PathVariable parentUserId: String): ResponseEntity<List<Memory>> {
        return ResponseEntity.ok(memoryRepository.findByParentUser_UserId(parentUserId))
    }

    @GetMapping("/baby/{babyId}/age/{ageInMonths}")
    fun getMemoriesByBabyAge(
        @PathVariable babyId: String,
        @PathVariable ageInMonths: Int
    ): ResponseEntity<List<Memory>> {
        return ResponseEntity.ok(memoryRepository.findByBabyIdAndAgeInMonths(babyId, ageInMonths))
    }

    @GetMapping("/baby/{babyId}/date-range")
    fun getMemoriesByBabyAndDateRange(
        @PathVariable babyId: String,
        @RequestParam startDate: String,
        @RequestParam endDate: String
    ): ResponseEntity<List<Memory>> {
        val start = LocalDate.parse(startDate)
        val end = LocalDate.parse(endDate)
        return ResponseEntity.ok(memoryRepository.findByBabyIdAndDateRange(babyId, start, end))
    }

    @GetMapping("/baby/{babyId}/count")
    fun countMemoriesByBaby(@PathVariable babyId: String): ResponseEntity<Map<String, Long>> {
        val count = memoryRepository.countMemoriesByBaby(babyId)
        return ResponseEntity.ok(mapOf("count" to count))
    }

    @GetMapping("/search")
    fun searchMemories(@RequestParam searchTerm: String): ResponseEntity<List<Memory>> {
        return ResponseEntity.ok(memoryRepository.searchMemories(searchTerm))
    }

    @PostMapping
    fun createMemory(@RequestBody memory: Memory): ResponseEntity<Memory> {
        if (memory.memoryId.isEmpty()) {
            memory.memoryId = UUID.randomUUID().toString()
        }
        val savedMemory = memoryRepository.save(memory)
        return ResponseEntity.status(HttpStatus.CREATED).body(savedMemory)
    }

    @PutMapping("/{memoryId}")
    fun updateMemory(@PathVariable memoryId: String, @RequestBody memory: Memory): ResponseEntity<Memory> {
        return if (memoryRepository.existsById(memoryId)) {
            memory.memoryId = memoryId
            ResponseEntity.ok(memoryRepository.save(memory))
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @DeleteMapping("/{memoryId}")
    fun deleteMemory(@PathVariable memoryId: String): ResponseEntity<Void> {
        return if (memoryRepository.existsById(memoryId)) {
            memoryRepository.deleteById(memoryId)
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }
}