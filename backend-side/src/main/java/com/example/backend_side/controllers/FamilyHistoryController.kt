package com.example.backend_side.controllers

import com.example.backend_side.*
import com.example.backend_side.entity.FamilyHistory
import com.example.backend_side.repositories.BabyRepository
import com.example.backend_side.repositories.FamilyHistoryRepository
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

// ============================================================
// FAMILY HISTORY V1 CONTROLLER — client-facing API
// ============================================================
//
// ROOT CAUSE FIX for potential "No static resource v1/family-history"
//
// Old FamilyHistoryController was at "/api/family-history".
// Client calls "/v1/family-history/baby/{babyId}".
// This new controller bridges the gap with proper ApiResponse wrapping.
//
// NOTE: Keep the old FamilyHistoryController.kt as-is — it handles
// "/api/family-history" which is the legacy path.
// ============================================================

@RestController
@RequestMapping("/v1/family-history")
@Tag(name = "Family History V1", description = "Family medical history — client-facing API")
class FamilyHistoryV1Controller(
    private val familyHistoryRepository: FamilyHistoryRepository,
    private val babyRepository: BabyRepository
) {

    // ── GET /v1/family-history/baby/{babyId} ──────────────────────────────────
    // Client: ApiService.getFamilyHistory(babyId)
    @GetMapping("/baby/{babyId}")
    @Operation(summary = "Get family history for a baby")
    fun getFamilyHistoryByBaby(
        @PathVariable babyId: String
    ): ResponseEntity<ApiResponse<FamilyHistoryClientResponse?>> {
        val history = familyHistoryRepository.findByBaby_BabyId(babyId)
            .map { it.toClientResponse() }
            .orElse(null)
        return ResponseEntity.ok(ApiResponse(true, "Family history retrieved", history))
    }

    // ── POST /v1/family-history ────────────────────────────────────────────────
    // Client: ApiService.createFamilyHistory(request)
    @PostMapping
    @Operation(summary = "Create family history for a baby")
    fun createFamilyHistory(
        @RequestBody request: FamilyHistoryClientRequest
    ): ResponseEntity<ApiResponse<FamilyHistoryClientResponse>> {
        val baby = babyRepository.findById(request.babyId)
            .orElseThrow { ResourceNotFoundException("Baby not found: ${request.babyId}") }

        // Check if already exists
        if (familyHistoryRepository.existsByBaby_BabyId(request.babyId)) {
            // Update instead
            val existing = familyHistoryRepository.findByBaby_BabyId(request.babyId)
                .orElseThrow { ResourceNotFoundException("Family history not found") }
            existing.apply {
                heredity               = request.heredity
                bloodDiseases          = request.bloodDiseases
                cardiovascularDiseases = request.cardiovascularDiseases
                metabolicDiseases      = request.metabolicDiseases
                appendicitis           = request.appendicitis
                tuberculosis           = request.tuberculosis
                parkinsonism           = request.parkinsonism
                allergies              = request.allergies
                others                 = request.others
            }
            val saved = familyHistoryRepository.save(existing)
            return ResponseEntity.ok(ApiResponse(true, "Family history updated", saved.toClientResponse()))
        }

        val history = FamilyHistory(
            historyId              = UUID.randomUUID().toString(),
            baby                   = baby,
            heredity               = request.heredity,
            bloodDiseases          = request.bloodDiseases,
            cardiovascularDiseases = request.cardiovascularDiseases,
            metabolicDiseases      = request.metabolicDiseases,
            appendicitis           = request.appendicitis,
            tuberculosis           = request.tuberculosis,
            parkinsonism           = request.parkinsonism,
            allergies              = request.allergies,
            others                 = request.others
        )
        val saved = familyHistoryRepository.save(history)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse(true, "Family history created", saved.toClientResponse()))
    }

    // ── PUT /v1/family-history/{historyId} ────────────────────────────────────
    // Client: ApiService.updateFamilyHistory(historyId, request)
    @PutMapping("/{historyId}")
    @Operation(summary = "Update family history")
    fun updateFamilyHistory(
        @PathVariable historyId: String,
        @RequestBody request: FamilyHistoryClientRequest
    ): ResponseEntity<ApiResponse<FamilyHistoryClientResponse>> {
        val history = familyHistoryRepository.findById(historyId)
            .orElseThrow { ResourceNotFoundException("Family history not found: $historyId") }

        history.apply {
            heredity               = request.heredity
            bloodDiseases          = request.bloodDiseases
            cardiovascularDiseases = request.cardiovascularDiseases
            metabolicDiseases      = request.metabolicDiseases
            appendicitis           = request.appendicitis
            tuberculosis           = request.tuberculosis
            parkinsonism           = request.parkinsonism
            allergies              = request.allergies
            others                 = request.others
        }
        val saved = familyHistoryRepository.save(history)
        return ResponseEntity.ok(ApiResponse(true, "Family history updated", saved.toClientResponse()))
    }

    // ── DELETE /v1/family-history/{historyId} ─────────────────────────────────
    // Client: ApiService.deleteFamilyHistory(historyId)
    @DeleteMapping("/{historyId}")
    @Operation(summary = "Delete family history")
    fun deleteFamilyHistory(
        @PathVariable historyId: String
    ): ResponseEntity<ApiResponse<Nothing>> {
        if (!familyHistoryRepository.existsById(historyId)) {
            throw ResourceNotFoundException("Family history not found: $historyId")
        }
        familyHistoryRepository.deleteById(historyId)
        return ResponseEntity.ok(ApiResponse(true, "Family history deleted"))
    }

    // ── Mapper ────────────────────────────────────────────────────────────────
    private fun FamilyHistory.toClientResponse() = FamilyHistoryClientResponse(
        historyId              = historyId,
        babyId                 = baby?.babyId ?: "",
        heredity               = heredity,
        bloodDiseases          = bloodDiseases,
        cardiovascularDiseases = cardiovascularDiseases,
        metabolicDiseases      = metabolicDiseases,
        appendicitis           = appendicitis,
        tuberculosis           = tuberculosis,
        parkinsonism           = parkinsonism,
        allergies              = allergies,
        others                 = others
    )
}

// ── Response DTO ──────────────────────────────────────────────────────────────
data class FamilyHistoryClientResponse(
    val historyId             : String,
    val babyId                : String,
    val heredity              : String? = null,
    val bloodDiseases         : String? = null,
    val cardiovascularDiseases: String? = null,
    val metabolicDiseases     : String? = null,
    val appendicitis          : String? = null,
    val tuberculosis          : String? = null,
    val parkinsonism          : String? = null,
    val allergies             : String? = null,
    val others                : String? = null
)

// ── Request DTO ───────────────────────────────────────────────────────────────
data class FamilyHistoryClientRequest(
    val babyId                : String,
    val heredity              : String? = null,
    val bloodDiseases         : String? = null,
    val cardiovascularDiseases: String? = null,
    val metabolicDiseases     : String? = null,
    val appendicitis          : String? = null,
    val tuberculosis          : String? = null,
    val parkinsonism          : String? = null,
    val allergies             : String? = null,
    val others                : String? = null
)