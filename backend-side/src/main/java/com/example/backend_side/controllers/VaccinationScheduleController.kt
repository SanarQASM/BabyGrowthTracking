package com.example.backend_side.controllers

import com.example.backend_side.*
import com.example.backend_side.repositories.UserRepository
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
// ============================================================
// VACCINATION SCHEDULE CONTROLLER
// ============================================================

@RestController
@RequestMapping("/v1/vaccination-schedules")
@Tag(name = "Vaccination Schedules", description = "View and manage vaccination schedules per baby and bench")
class VaccinationScheduleController(
    private val scheduleService: VaccinationScheduleService,
    private val userRepository: UserRepository
) {

    @GetMapping("/baby/{babyId}")
    @Operation(summary = "Get full vaccination schedule for a baby — parent view")
    fun getScheduleForBaby(@PathVariable babyId: String): ResponseEntity<ApiResponse<List<VaccinationScheduleResponse>>> =
        ResponseEntity.ok(ApiResponse(true, "Schedule retrieved", scheduleService.getScheduleForBaby(babyId)))

    @GetMapping("/baby/{babyId}/upcoming")
    @Operation(summary = "Get upcoming vaccinations within N days")
    fun getUpcomingForBaby(
        @PathVariable babyId: String,
        @RequestParam(defaultValue = "30") daysAhead: Int
    ): ResponseEntity<ApiResponse<List<VaccinationScheduleResponse>>> =
        ResponseEntity.ok(ApiResponse(true, "Upcoming retrieved", scheduleService.getUpcomingForBaby(babyId, daysAhead)))

    @GetMapping("/baby/{babyId}/overdue")
    @Operation(summary = "Get overdue vaccinations for a baby")
    fun getOverdueForBaby(@PathVariable babyId: String): ResponseEntity<ApiResponse<List<VaccinationScheduleResponse>>> =
        ResponseEntity.ok(ApiResponse(true, "Overdue retrieved", scheduleService.getOverdueForBaby(babyId)))

    @GetMapping("/bench/{benchId}/day")
    @Operation(summary = "Get all babies scheduled at a bench on a date — Team daily view")
    fun getBenchDaySchedule(
        @PathVariable benchId: String,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate
    ): ResponseEntity<ApiResponse<BenchDayScheduleResponse>> =
        ResponseEntity.ok(ApiResponse(true, "Daily schedule retrieved", scheduleService.getScheduleByBenchAndDate(benchId, date)))

    @GetMapping("/bench/{benchId}/range")
    @Operation(summary = "Get schedules for a bench in a date range — Team weekly/monthly view")
    fun getBenchRangeSchedule(
        @PathVariable benchId: String,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) from: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) to: LocalDate
    ): ResponseEntity<ApiResponse<List<VaccinationScheduleResponse>>> =
        ResponseEntity.ok(ApiResponse(true, "Range schedule retrieved", scheduleService.getScheduleByBenchAndRange(benchId, from, to)))

    @PutMapping("/{scheduleId}/status")
    @Operation(summary = "Update schedule status — mark completed, missed, etc.")
    fun updateStatus(
        @PathVariable scheduleId: String,
        @Valid @RequestBody request: VaccinationScheduleUpdateRequest
    ): ResponseEntity<ApiResponse<VaccinationScheduleResponse>> =
        ResponseEntity.ok(ApiResponse(true, "Status updated", scheduleService.updateScheduleStatus(scheduleId, request)))

    @PutMapping("/{scheduleId}/adjust")
    @Operation(summary = "Manually adjust a scheduled date — parent or team member")
    fun adjustDate(
        @PathVariable scheduleId: String,
        @Valid @RequestBody request: ScheduleAdjustmentRequest,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<ApiResponse<VaccinationScheduleResponse>> {
        val user = userRepository.findByEmail(userDetails.username)
            .orElseThrow { ResourceNotFoundException("User not found") }
        val result = scheduleService.adjustScheduleDate(
            user.userId,
            request.copy(scheduleId = scheduleId)
        )
        return ResponseEntity.ok(ApiResponse(true, "Schedule adjusted", result))
    }

    @GetMapping("/{scheduleId}/history")
    @Operation(summary = "Get date adjustment history for a schedule")
    fun getAdjustmentHistory(@PathVariable scheduleId: String): ResponseEntity<ApiResponse<List<ScheduleAdjustmentLogResponse>>> =
        ResponseEntity.ok(ApiResponse(true, "History retrieved", scheduleService.getAdjustmentHistory(scheduleId)))
}