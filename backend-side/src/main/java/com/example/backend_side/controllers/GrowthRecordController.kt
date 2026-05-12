// File: backend-side/src/main/java/com/example/backend_side/controllers/GrowthRecordController.kt
package com.example.backend_side.controllers

import com.example.backend_side.ApiResponse
import com.example.backend_side.GrowthRecordCreateRequest
import com.example.backend_side.GrowthRecordResponse
import com.example.backend_side.GrowthRecordService
import com.example.backend_side.ResourceNotFoundException
import com.example.backend_side.repositories.GrowthRecordRepository
import com.example.backend_side.repositories.UserRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("/v1/growth-records")
@Tag(name = "Growth Records", description = "APIs for managing baby growth records")
class GrowthRecordController(
    private val growthRecordService   : GrowthRecordService,
    private val growthRecordRepository: GrowthRecordRepository,
    private val userRepository        : UserRepository
) {

    // ── POST /v1/growth-records ───────────────────────────────────────────────
    @PostMapping
    @Operation(summary = "Create a new growth record")
    fun createGrowthRecord(
        @RequestHeader("X-User-Id") measuredBy: String,
        @Valid @RequestBody request: GrowthRecordCreateRequest
    ): ResponseEntity<ApiResponse<GrowthRecordResponse>> {
        logger.info { "Creating growth record for baby: ${request.babyId}, measuredBy: $measuredBy" }
        val record = growthRecordService.createGrowthRecord(measuredBy, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse(success = true, message = "Growth record created successfully", data = record)
        )
    }

    // ── GET /v1/growth-records/{recordId} ─────────────────────────────────────
    @GetMapping("/{recordId}")
    @Operation(summary = "Get growth record by ID")
    fun getGrowthRecordById(
        @PathVariable recordId: String
    ): ResponseEntity<ApiResponse<GrowthRecordResponse>> {
        val record = growthRecordService.getGrowthRecordById(recordId)
        return ResponseEntity.ok(
            ApiResponse(success = true, message = "Growth record retrieved successfully", data = record)
        )
    }

    // ── GET /v1/growth-records/baby/{babyId} ──────────────────────────────────
    // Returns ALL records (parent + team) — used by the parent's charts screen
    @GetMapping("/baby/{babyId}")
    @Operation(summary = "Get all growth records for a baby (parent + team combined)")
    fun getGrowthRecordsByBaby(
        @PathVariable babyId: String
    ): ResponseEntity<ApiResponse<List<GrowthRecordResponse>>> {
        val records = growthRecordService.getGrowthRecordsByBaby(babyId)
        return ResponseEntity.ok(
            ApiResponse(success = true, message = "Growth records retrieved successfully", data = records)
        )
    }

    // ── GET /v1/growth-records/baby/{babyId}/latest ───────────────────────────
    @GetMapping("/baby/{babyId}/latest")
    @Operation(summary = "Get latest growth record for a baby")
    fun getLatestGrowthRecord(
        @PathVariable babyId: String
    ): ResponseEntity<ApiResponse<GrowthRecordResponse?>> {
        val record = growthRecordService.getLatestGrowthRecord(babyId)
        return ResponseEntity.ok(
            ApiResponse(success = true, message = "Latest growth record retrieved successfully", data = record)
        )
    }

    // ── GET /v1/growth-records/baby/{babyId}/team-only ────────────────────────
    // NEW ENDPOINT: Returns ONLY records added by team members (isTeamMeasurement=true).
    // Used by the team vaccination screen so team members only see their own entries,
    // not the parent's private growth history.
    //
    // The parent screen calls /baby/{babyId} which returns ALL records (both parent
    // and team), giving the parent a complete combined view.
    @GetMapping("/baby/{babyId}/team-only")
    @Operation(
        summary = "Get only team-added growth records for a baby",
        description = "Returns records where isTeamMeasurement=true. " +
                "Team vaccination members see only their own entries. " +
                "Parents see all records via /baby/{babyId}."
    )
    fun getTeamGrowthRecords(
        @PathVariable babyId: String,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<ApiResponse<List<GrowthRecordResponse>>> {

        // Resolve all records, then filter to team-only
        // isTeamMeasurement is derived from whether the measuredBy user
        // is the baby's own parent or an external team member.
        val allRecords = growthRecordService.getGrowthRecordsByBaby(babyId)
        val teamRecords = allRecords.filter { it.isTeamMeasurement }

        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Team growth records retrieved",
                data    = teamRecords
            )
        )
    }

    // ── DELETE /v1/growth-records/{recordId} ──────────────────────────────────
    @DeleteMapping("/{recordId}")
    @Operation(summary = "Delete growth record")
    fun deleteGrowthRecord(
        @PathVariable recordId: String
    ): ResponseEntity<ApiResponse<Nothing>> {
        growthRecordService.deleteGrowthRecord(recordId)
        return ResponseEntity.ok(
            ApiResponse(success = true, message = "Growth record deleted successfully")
        )
    }
}