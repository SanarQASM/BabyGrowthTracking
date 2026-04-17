// File: backend-side/src/main/java/com/example/backend_side/controllers/GrowthRecordController.kt
package com.example.backend_side.controllers

import com.example.backend_side.ApiResponse
import com.example.backend_side.GrowthRecordCreateRequest
import com.example.backend_side.GrowthRecordResponse
import com.example.backend_side.GrowthRecordService
import io.github.oshai.kotlinlogging.KotlinLogging
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("/v1/growth-records")
@Tag(name = "Growth Records", description = "APIs for managing baby growth records")
class GrowthRecordController(
    private val growthRecordService: GrowthRecordService
) {

    // ─────────────────────────────────────────────────────────────────────────
    // POST /v1/growth-records
    //
    // The X-User-Id header carries the userId of whoever is making the request.
    // GrowthRecordService compares this against baby.parent.userId to decide
    // whether measuredByName should be null (parent) or a name (team/external).
    // ─────────────────────────────────────────────────────────────────────────

    @PostMapping
    @Operation(summary = "Create a new growth record")
    fun createGrowthRecord(
        @RequestHeader("X-User-Id") measuredBy: String,
        @Valid @RequestBody request: GrowthRecordCreateRequest
    ): ResponseEntity<ApiResponse<GrowthRecordResponse>> {
        logger.info { "Creating growth record for baby: ${request.babyId}, measuredBy: $measuredBy" }
        val record = growthRecordService.createGrowthRecord(measuredBy, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse(
                success = true,
                message = "Growth record created successfully",
                data    = record
            )
        )
    }

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

    @GetMapping("/baby/{babyId}")
    @Operation(summary = "Get all growth records for a baby")
    fun getGrowthRecordsByBaby(
        @PathVariable babyId: String
    ): ResponseEntity<ApiResponse<List<GrowthRecordResponse>>> {
        val records = growthRecordService.getGrowthRecordsByBaby(babyId)
        return ResponseEntity.ok(
            ApiResponse(success = true, message = "Growth records retrieved successfully", data = records)
        )
    }

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