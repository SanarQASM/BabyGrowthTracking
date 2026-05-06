package com.example.backend_side.controllers

import com.example.backend_side.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

// ============================================================
// BENCH HOLIDAY CONTROLLER
// ============================================================

@RestController
@RequestMapping("/v1/bench-holidays")
@Tag(name = "Bench Holidays", description = "Manage bench-specific and national holidays")
class BenchHolidayController(private val holidayService: BenchHolidayService) {

    @PostMapping
    @Operation(summary = "Add a holiday — Admin only")
    fun addHoliday(
        @Valid @RequestBody request: BenchHolidayCreateRequest
    ): ResponseEntity<ApiResponse<BenchHolidayResponse>> =
        ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse(true, "Holiday added", holidayService.addHoliday(request)))

    @GetMapping("/national")
    @Operation(summary = "Get all national holidays")
    fun getNationalHolidays(): ResponseEntity<ApiResponse<List<BenchHolidayResponse>>> =
        ResponseEntity.ok(
            ApiResponse(true, "National holidays retrieved", holidayService.getNationalHolidays())
        )

    @GetMapping("/bench/{benchId}")
    @Operation(summary = "Get holidays for a specific bench")
    fun getHolidaysForBench(
        @PathVariable benchId: String
    ): ResponseEntity<ApiResponse<List<BenchHolidayResponse>>> =
        ResponseEntity.ok(
            ApiResponse(true, "Bench holidays retrieved", holidayService.getHolidaysForBench(benchId))
        )

    @DeleteMapping("/{holidayId}")
    @Operation(summary = "Delete a holiday — Admin only")
    fun deleteHoliday(
        @PathVariable holidayId: String
    ): ResponseEntity<ApiResponse<Nothing>> {
        holidayService.deleteHoliday(holidayId)
        return ResponseEntity.ok(ApiResponse(true, "Holiday deleted"))
    }
}