package com.example.backend_side.controllers

import com.example.backend_side.*
import com.example.backend_side.repositories.UserRepository
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

// ============================================================
// VACCINATION RESCHEDULE CONTROLLER
// ============================================================

@RestController
@RequestMapping("/v1/vaccination-schedules")
@Tag(name = "Vaccination Schedules", description = "View and manage vaccination schedules per baby and bench")
class VaccinationRescheduleController(
    private val rescheduleService: VaccinationRescheduleService,
    private val userRepository   : UserRepository
) {
    /**
     * POST /v1/vaccination-schedules/baby/{babyId}/reschedule
     *
     * Reschedules ALL non-completed vaccinations for a baby in one call.
     *
     * Per-vaccine logic:
     *  - COMPLETED / MISSED        → skipped (included in response, rescheduled=false)
     *  - OVERDUE within 2-mo window → rescheduled to next valid bench day from today
     *  - OVERDUE beyond 2-mo window → marked MISSED, skipReason explains why
     *  - UPCOMING / DUE_SOON        → rescheduled to next valid bench day
     *
     * The caller (parent or team member) must supply a [shiftReason] from
     * [AdjustmentReason] and an optional free-text note.
     */
    @PostMapping("/baby/{babyId}/reschedule")
    @Operation(summary = "Reschedule all pending vaccinations for a baby")
    fun rescheduleAll(
        @PathVariable babyId: String,
        @Valid @RequestBody request: VaccinationRescheduleRequest,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<ApiResponse<VaccinationRescheduleResponse>> {
        val user = userRepository.findByEmail(userDetails.username)
            .orElseThrow { ResourceNotFoundException("User not found") }

        val result = rescheduleService.rescheduleAll(
            babyId       = babyId,
            request      = request,
            doneByUserId = user.userId
        )

        return ResponseEntity.ok(
            ApiResponse(true, result.message, result)
        )
    }
}