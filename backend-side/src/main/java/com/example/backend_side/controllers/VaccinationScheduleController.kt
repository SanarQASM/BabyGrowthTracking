package com.example.backend_side.controllers

import com.example.backend_side.*
import com.example.backend_side.entity.ScheduleStatus
import com.example.backend_side.entity.UserRole
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
//
// UPDATED:
//  1. @PatchMapping on updateStatus (was @PutMapping — HTTP method fix kept)
//  2. New endpoint: PATCH /v1/vaccination-schedules/{scheduleId}/team-status
//     → Only for VACCINATION_TEAM role
//     → Only allows COMPLETED or SKIPPED
//     → Rejects if current status is MISSED (locked)
// ============================================================

@RestController
@RequestMapping("/v1/vaccination-schedules")
@Tag(name = "Vaccination Schedules", description = "View and manage vaccination schedules per baby and bench")
class VaccinationScheduleController(
    private val scheduleService: VaccinationScheduleService,
    private val userRepository : UserRepository
) {

    @GetMapping("/baby/{babyId}")
    @Operation(summary = "Get full vaccination schedule for a baby — parent view")
    fun getScheduleForBaby(
        @PathVariable babyId: String
    ): ResponseEntity<ApiResponse<List<VaccinationScheduleResponse>>> =
        ResponseEntity.ok(
            ApiResponse(true, "Schedule retrieved", scheduleService.getScheduleForBaby(babyId))
        )

    @GetMapping("/baby/{babyId}/upcoming")
    @Operation(summary = "Get upcoming vaccinations within N days")
    fun getUpcomingForBaby(
        @PathVariable babyId: String,
        @RequestParam(defaultValue = "30") daysAhead: Int
    ): ResponseEntity<ApiResponse<List<VaccinationScheduleResponse>>> =
        ResponseEntity.ok(
            ApiResponse(true, "Upcoming retrieved", scheduleService.getUpcomingForBaby(babyId, daysAhead))
        )

    @GetMapping("/baby/{babyId}/overdue")
    @Operation(summary = "Get overdue vaccinations for a baby")
    fun getOverdueForBaby(
        @PathVariable babyId: String
    ): ResponseEntity<ApiResponse<List<VaccinationScheduleResponse>>> =
        ResponseEntity.ok(
            ApiResponse(true, "Overdue retrieved", scheduleService.getOverdueForBaby(babyId))
        )

    @GetMapping("/bench/{benchId}/day")
    @Operation(summary = "Get all babies scheduled at a bench on a date — Team daily view")
    fun getBenchDaySchedule(
        @PathVariable benchId: String,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate
    ): ResponseEntity<ApiResponse<BenchDayScheduleResponse>> =
        ResponseEntity.ok(
            ApiResponse(true, "Daily schedule retrieved",
                scheduleService.getScheduleByBenchAndDate(benchId, date))
        )

    @GetMapping("/bench/{benchId}/range")
    @Operation(summary = "Get schedules for a bench in a date range — Team weekly/monthly view")
    fun getBenchRangeSchedule(
        @PathVariable benchId: String,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) from: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) to: LocalDate
    ): ResponseEntity<ApiResponse<List<VaccinationScheduleResponse>>> =
        ResponseEntity.ok(
            ApiResponse(true, "Range schedule retrieved",
                scheduleService.getScheduleByBenchAndRange(benchId, from, to))
        )

    // FIX: was @PutMapping — client sends PATCH, so this must be @PatchMapping.
    @PatchMapping("/{scheduleId}/status")
    @Operation(summary = "Update schedule status — general (admin / internal)")
    fun updateStatus(
        @PathVariable scheduleId: String,
        @Valid @RequestBody request: VaccinationScheduleUpdateRequest
    ): ResponseEntity<ApiResponse<VaccinationScheduleResponse>> =
        ResponseEntity.ok(
            ApiResponse(true, "Status updated",
                scheduleService.updateScheduleStatus(scheduleId, request))
        )

    // ── NEW: Team-restricted status update ────────────────────────────────────
    //
    // PATCH /v1/vaccination-schedules/{scheduleId}/team-status
    //
    // Rules enforced SERVER-SIDE:
    //   1. Caller must be VACCINATION_TEAM role
    //   2. Allowed target statuses: COMPLETED, SKIPPED only
    //   3. Current status MUST NOT be MISSED — missed vaccinations are locked
    //      and cannot be changed by the team (they are system/parent records)
    //   4. Current status MUST NOT be COMPLETED — once completed, locked
    //
    // The UI also enforces these rules, but the server is the source of truth.
    // ─────────────────────────────────────────────────────────────────────────

    @PatchMapping("/{scheduleId}/team-status")
    @Operation(summary = "Team vaccination updates schedule status — only COMPLETED or SKIPPED allowed; MISSED is locked")
    fun updateTeamStatus(
        @PathVariable scheduleId: String,
        @Valid @RequestBody request: TeamStatusUpdateRequest,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<ApiResponse<VaccinationScheduleResponse>> {

        val user = userRepository.findByEmail(userDetails.username)
            .orElseThrow { ResourceNotFoundException("Authenticated user not found") }

        // Rule 1: only VACCINATION_TEAM members may call this endpoint
        if (user.role != UserRole.VACCINATION_TEAM) {
            throw ForbiddenException("Only vaccination team members can update schedule status via this endpoint")
        }

        val result = scheduleService.updateTeamScheduleStatus(
            scheduleId     = scheduleId,
            newStatus      = request.status,
            completedByUser = user.userId,
            completedDate  = request.completedDate,
            notes          = request.notes
        )

        return ResponseEntity.ok(ApiResponse(true, "Status updated by team", result))
    }

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
    fun getAdjustmentHistory(
        @PathVariable scheduleId: String
    ): ResponseEntity<ApiResponse<List<ScheduleAdjustmentLogResponse>>> =
        ResponseEntity.ok(
            ApiResponse(true, "History retrieved",
                scheduleService.getAdjustmentHistory(scheduleId))
        )
}

// ─────────────────────────────────────────────────────────────────────────────
// Team status update request DTO
// Only COMPLETED and SKIPPED are accepted — anything else is rejected by the
// service layer before touching the database.
// ─────────────────────────────────────────────────────────────────────────────

data class TeamStatusUpdateRequest(
    val status       : String,        // "COMPLETED" or "SKIPPED"
    val completedDate: LocalDate? = null,
    val notes        : String?    = null
)