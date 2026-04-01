package com.example.backend_side.controllers

import com.example.backend_side.ChildIllnessRequest
import com.example.backend_side.entity.ChildIllnesses
import com.example.backend_side.repositories.BabyRepository
import com.example.backend_side.repositories.ChildIllnessesRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/v1/child-illnesses")
@CrossOrigin(origins = ["*"])
class ChildIllnessesController(
    private val childIllnessesRepository: ChildIllnessesRepository,
    private val babyRepository: BabyRepository          // inject baby repo to resolve entity
) {

    // ── helpers ──────────────────────────────────────────────────────────────

    private fun ok(body: Any)   = ResponseEntity.ok(body)
    private fun notFound()      = ResponseEntity.notFound().build<Any>()
    private fun badRequest(msg: String) =
        ResponseEntity.badRequest().body(mapOf("error" to msg))

    private fun ChildIllnessRequest.toEntity(existingId: String? = null): ChildIllnesses {
        val baby = babyRepository.findById(babyId).orElseThrow {
            NoSuchElementException("Baby '$babyId' not found")
        }
        return ChildIllnesses(
            illnessId     = existingId ?: UUID.randomUUID().toString(),
            baby          = baby,
            illnessName   = illnessName.trim(),
            diagnosisDate = parsedDiagnosisDate(),   // safe parse
            notes         = notes?.trim(),
            isActive      = isActive
        )
    }

    // ── endpoints ─────────────────────────────────────────────────────────────

    @GetMapping
    fun getAllChildIllnesses() = ok(childIllnessesRepository.findAll())

    @GetMapping("/{illnessId}")
    fun getChildIllnessById(@PathVariable illnessId: String): ResponseEntity<*> =
        childIllnessesRepository.findById(illnessId)
            .map { ok(it) }.orElse(notFound())

    @GetMapping("/baby/{babyId}")
    fun getIllnessesByBaby(@PathVariable babyId: String) =
        ok(childIllnessesRepository.findByBaby_BabyId(babyId))

    @GetMapping("/baby/{babyId}/active")
    fun getActiveIllnessesByBaby(
        @PathVariable babyId: String,
        @RequestParam(defaultValue = "true") isActive: Boolean
    ) = ok(childIllnessesRepository.findByBaby_BabyIdAndIsActive(babyId, isActive))

    @GetMapping("/baby/{babyId}/count")
    fun countActiveIllnesses(@PathVariable babyId: String) =
        ok(mapOf("count" to childIllnessesRepository.countActiveIllnessesByBaby(babyId)))

    @PostMapping
    fun createChildIllness(@RequestBody request: ChildIllnessRequest): ResponseEntity<*> {
        return try {
            val entity = request.toEntity()
            ResponseEntity.status(HttpStatus.CREATED).body(childIllnessesRepository.save(entity))
        } catch (e: IllegalArgumentException) {
            badRequest(e.message ?: "Invalid request")
        } catch (e: NoSuchElementException) {
            badRequest(e.message ?: "Baby not found")
        }
    }

    @PutMapping("/{illnessId}")
    fun updateChildIllness(
        @PathVariable illnessId: String,
        @RequestBody request: ChildIllnessRequest
    ): ResponseEntity<*> {
        if (!childIllnessesRepository.existsById(illnessId)) return notFound()
        return try {
            val entity = request.toEntity(existingId = illnessId)
            ok(childIllnessesRepository.save(entity))
        } catch (e: IllegalArgumentException) {
            badRequest(e.message ?: "Invalid request")
        }
    }

    @PatchMapping("/{illnessId}/deactivate")
    fun deactivateIllness(@PathVariable illnessId: String): ResponseEntity<*> =
        childIllnessesRepository.findById(illnessId).map { illness ->
            illness.isActive = false
            ok(childIllnessesRepository.save(illness))
        }.orElse(notFound())

    @DeleteMapping("/{illnessId}")
    fun deleteChildIllness(@PathVariable illnessId: String): ResponseEntity<Void> {
        if (!childIllnessesRepository.existsById(illnessId))
            return ResponseEntity.notFound().build()
        childIllnessesRepository.deleteById(illnessId)
        return ResponseEntity.noContent().build()
    }

    // ── global date-format error handler for this controller ──────────────────
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleBadDate(e: IllegalArgumentException) =
        ResponseEntity.badRequest().body(mapOf("error" to (e.message ?: "Bad request")))
}