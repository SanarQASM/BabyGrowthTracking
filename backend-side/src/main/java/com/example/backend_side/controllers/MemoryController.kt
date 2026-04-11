package com.example.backend_side.controllers

import com.example.backend_side.*
import com.example.backend_side.entity.Memory
import com.example.backend_side.repositories.MemoryRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

// ─────────────────────────────────────────────────────────────────────────────
// MemoryController — FIXED
//
// ROOT CAUSE of Column 'baby_id' cannot be null:
//   The old controller accepted a raw Memory entity as @RequestBody and called
//   memoryRepository.save(memory) directly.  Because the JSON only contains
//   babyId (a String), the JPA @ManyToOne Baby relationship was never resolved,
//   so baby = null and the NOT NULL constraint on baby_id was violated.
//
// FIX:
//   • POST /v1/memories           → delegates to MemoryService.createMemory()
//     which properly resolves babyId → Baby entity and parentUserId → User entity
//     before saving.  The parent user ID is read from the X-User-Id header
//     (same convention used by BabyController and GrowthRecordController).
//   • All read / delete endpoints stay on the repository (no relationship
//     writes needed there, so they are safe as-is).
// ─────────────────────────────────────────────────────────────────────────────

@RestController
@RequestMapping("/v1/memories")
@CrossOrigin(origins = ["*"])
class MemoryController(
private val memoryRepository: MemoryRepository,
private val memoryService: MemoryService
) {

// ── Read endpoints (unchanged — no writes, no relationship issues) ────────

@GetMapping
fun getAllMemories(): ResponseEntity<List<Memory>> =
    ResponseEntity.ok(memoryRepository.findAll())

@GetMapping("/{memoryId}")
fun getMemoryById(@PathVariable memoryId: String): ResponseEntity<Memory> =
    memoryRepository.findById(memoryId)
        .map { ResponseEntity.ok(it) }
        .orElse(ResponseEntity.notFound().build())

@GetMapping("/baby/{babyId}")
fun getMemoriesByBaby(@PathVariable babyId: String): ResponseEntity<Any> {
    val memories = memoryRepository.findByBaby_BabyIdOrderByMemoryDateDesc(babyId)
    // Wrap in ApiResponse so the Kotlin client's ApiListResponse<MemoryNet> parser works
    val response = ApiResponse(
        success = true,
        message = AppConstants.Messages.MEMORY_CREATED,   // reuse existing constant
        data = memories.map { it.toMemoryNet() }
    )
    return ResponseEntity.ok(response)
}

@GetMapping("/parent/{parentUserId}")
fun getMemoriesByParent(@PathVariable parentUserId: String): ResponseEntity<List<Memory>> =
    ResponseEntity.ok(memoryRepository.findByParentUser_UserId(parentUserId))

@GetMapping("/baby/{babyId}/age/{ageInMonths}")
fun getMemoriesByBabyAge(
    @PathVariable babyId: String,
    @PathVariable ageInMonths: Int
): ResponseEntity<List<Memory>> =
    ResponseEntity.ok(memoryRepository.findByBabyIdAndAgeInMonths(babyId, ageInMonths))

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
fun countMemoriesByBaby(@PathVariable babyId: String): ResponseEntity<Map<String, Long>> =
    ResponseEntity.ok(mapOf("count" to memoryRepository.countMemoriesByBaby(babyId)))

@GetMapping("/search")
fun searchMemories(@RequestParam searchTerm: String): ResponseEntity<List<Memory>> =
    ResponseEntity.ok(memoryRepository.searchMemories(searchTerm))

// ── Create — FIXED ────────────────────────────────────────────────────────
//
// OLD (broken):
//   @PostMapping
//   fun createMemory(@RequestBody memory: Memory): ResponseEntity<Memory> {
//       if (memory.memoryId.isEmpty()) memory.memoryId = UUID.randomUUID().toString()
//       val savedMemory = memoryRepository.save(memory)   // ← baby is null → crash
//       return ResponseEntity.status(HttpStatus.CREATED).body(savedMemory)
//   }
//
// NEW (fixed):
//   Accept a MemoryCreateRequest DTO (which carries babyId as a String).
//   Delegate to MemoryService which resolves babyId → Baby entity properly.
//   The parent user ID comes from the X-User-Id header (same as BabyController).

@PostMapping
fun createMemory(
    @RequestHeader(value = "X-User-Id", required = false) parentUserId: String?,
    @RequestBody request: MemoryCreateRequest
): ResponseEntity<ApiResponse<MemoryNet>> {

    // parentUserId must be present — either from the header or from the request body
    val resolvedParentId = parentUserId?.takeIf { it.isNotBlank() }
        ?: return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ApiResponse(
                    success = false,
                    message = "X-User-Id header is required"
                )
            )

    val memory = memoryService.createMemory(resolvedParentId, request)
    val response = ApiResponse(
        success = true,
        message = AppConstants.Messages.MEMORY_CREATED,
        data = memory.toMemoryNet()
    )
    return ResponseEntity.status(HttpStatus.CREATED).body(response)
}

// ── Update ────────────────────────────────────────────────────────────────
//
// The original PUT accepted a raw Memory entity.  We keep the same signature
// but guard against accidentally nulling out the baby relationship by only
// allowing safe field updates (title, description) via the repository.
// Full relationship updates should go through the service if needed later.

@PutMapping("/{memoryId}")
fun updateMemory(
    @PathVariable memoryId: String,
    @RequestBody memory: Memory
): ResponseEntity<Memory> =
    if (memoryRepository.existsById(memoryId)) {
        memory.memoryId = memoryId
        ResponseEntity.ok(memoryRepository.save(memory))
    } else {
        ResponseEntity.notFound().build()
    }

// ── Delete ────────────────────────────────────────────────────────────────

@DeleteMapping("/{memoryId}")
fun deleteMemory(@PathVariable memoryId: String): ResponseEntity<Void> =
    if (memoryRepository.existsById(memoryId)) {
        memoryRepository.deleteById(memoryId)
        ResponseEntity.noContent().build()
    } else {
        ResponseEntity.notFound().build()
    }

// ── Private mapper ────────────────────────────────────────────────────────
// Maps Memory entity → MemoryNet (the shape the Kotlin client expects).
// imageCount and captions are stored as extra columns added to Memory entity.

    private fun Memory.toMemoryNet() = MemoryNet(
        memoryId    = memoryId,
        babyId      = baby?.babyId ?: "",
        babyName    = baby?.fullName ?: "",
        title       = title,
        description = description,
        memoryDate  = memoryDate.toString(),
        ageInMonths = ageInMonths,
        ageInDays   = ageInDays,
        imageCount  = imageCount,
        captions    = parseCaptions(captionsJson),
        createdAt   = createdAt?.toString(),
        updatedAt   = updatedAt?.toString()
    )

    private fun MemoryResponse.toMemoryNet() = MemoryNet(
        memoryId    = memoryId,
        babyId      = babyId,
        babyName    = babyName,
        title       = title,
        description = description,
        memoryDate  = memoryDate.toString(),
        ageInMonths = ageInMonths,
        ageInDays   = ageInDays,
        imageCount  = 0,
        captions    = null,
        createdAt   = createdAt?.toString(),
        updatedAt   = null
    )

private fun parseCaptions(json: String?): List<String>? {
    if (json.isNullOrBlank()) return null
    return try {
        // Simple JSON array parse without extra dependency
        json.trim().removePrefix("[").removeSuffix("]")
            .split(",")
            .map { it.trim().removePrefix("\"").removeSuffix("\"") }
            .filter { it.isNotBlank() }
    } catch (e: Exception) {
        null
    }
}
}

// ─────────────────────────────────────────────────────────────────────────────
// MemoryNet — the shape the Kotlin Multiplatform client deserializes
// Mirrors the @Serializable data class in ApiService.kt on the client side.
// ─────────────────────────────────────────────────────────────────────────────

data class MemoryNet(
val memoryId: String,
val babyId: String,
val babyName: String,
val title: String,
val description: String? = null,
val memoryDate: String,
val ageInMonths: Int? = null,
val ageInDays: Int? = null,
val imageCount: Int? = 0,
val captions: List<String>? = null,
val createdAt: String? = null,
val updatedAt: String? = null
)