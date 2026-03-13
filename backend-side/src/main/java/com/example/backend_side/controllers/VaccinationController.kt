package com.example.backend_side.controllers

import com.example.backend_side.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import com.example.backend_side.repositories.UserRepository
import java.time.LocalDate

// ============================================================
// VACCINATION CONTROLLER
// ============================================================

@RestController
@RequestMapping("/v1/vaccinations")
@Tag(name = "Vaccinations", description = "Manage individual vaccination records for babies")
class VaccinationController(
    private val vaccinationService: VaccinationService,
    private val userRepository: UserRepository
) {

    // ── POST /v1/vaccinations ─────────────────────────────────────────────────
    // Client: ApiService.createVaccination(request)
    @PostMapping
    @Operation(summary = "Create a new vaccination record")
    fun createVaccination(
        @Valid @RequestBody request: VaccinationCreateRequest
    ): ResponseEntity<ApiResponse<VaccinationResponse>> {
        val result = vaccinationService.createVaccination(request)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse(true, "Vaccination created successfully", result))
    }

    // ── GET /v1/vaccinations/{vaccinationId} ──────────────────────────────────
    @GetMapping("/{vaccinationId}")
    @Operation(summary = "Get vaccination by ID")
    fun getVaccinationById(
        @PathVariable vaccinationId: String
    ): ResponseEntity<ApiResponse<VaccinationResponse>> =
        ResponseEntity.ok(
            ApiResponse(true, "Vaccination retrieved", vaccinationService.getVaccinationById(vaccinationId))
        )

    // ── GET /v1/vaccinations/baby/{babyId} ────────────────────────────────────
    // Client: ApiService.getVaccinations(babyId)
    @GetMapping("/baby/{babyId}")
    @Operation(summary = "Get all vaccinations for a baby")
    fun getVaccinationsByBaby(
        @PathVariable babyId: String
    ): ResponseEntity<ApiResponse<List<VaccinationResponse>>> =
        ResponseEntity.ok(
            ApiResponse(true, "Vaccinations retrieved", vaccinationService.getVaccinationsByBaby(babyId))
        )

    // ── GET /v1/vaccinations/baby/{babyId}/upcoming ───────────────────────────
    // FIX: This endpoint was MISSING — client was calling it but got 404.
    // Client: ApiService.getUpcomingVaccinations(babyId)
    @GetMapping("/baby/{babyId}/upcoming")
    @Operation(summary = "Get upcoming (SCHEDULED) vaccinations for a baby")
    fun getUpcomingVaccinations(
        @PathVariable babyId: String
    ): ResponseEntity<ApiResponse<List<VaccinationResponse>>> =
        ResponseEntity.ok(
            ApiResponse(true, "Upcoming vaccinations retrieved", vaccinationService.getUpcomingVaccinations(babyId))
        )

    // ── PUT /v1/vaccinations/{vaccinationId} ──────────────────────────────────
    @PutMapping("/{vaccinationId}")
    @Operation(summary = "Update a vaccination record")
    fun updateVaccination(
        @PathVariable vaccinationId: String,
        @Valid @RequestBody request: VaccinationUpdateRequest
    ): ResponseEntity<ApiResponse<VaccinationResponse>> =
        ResponseEntity.ok(
            ApiResponse(true, "Vaccination updated", vaccinationService.updateVaccination(vaccinationId, request))
        )

    // ── POST /v1/vaccinations/{vaccinationId}/complete ────────────────────────
    @PostMapping("/{vaccinationId}/complete")
    @Operation(summary = "Mark a vaccination as completed")
    fun markAsCompleted(
        @PathVariable vaccinationId: String,
        @RequestParam administeredDate: String,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<ApiResponse<VaccinationResponse>> {
        val user = userRepository.findByEmail(userDetails.username)
            .orElseThrow { ResourceNotFoundException("User not found") }
        val result = vaccinationService.markAsCompleted(
            vaccinationId,
            user.userId,
            LocalDate.parse(administeredDate)
        )
        return ResponseEntity.ok(ApiResponse(true, "Vaccination marked as completed", result))
    }
}