package com.example.backend_side.controllers

import com.example.backend_side.*
import com.example.backend_side.repositories.UserRepository
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

// ============================================================
// BABY BENCH ASSIGNMENT CONTROLLER
// ============================================================

@RestController
@RequestMapping("/v1/bench-assignments")
@Tag(name = "Bench Assignments", description = "Assign vaccination benches to babies")
class BabyBenchAssignmentController(
    private val assignmentService: BabyBenchAssignmentService,
    private val userRepository: UserRepository
) {

    @PostMapping
    @Operation(summary = "Assign a bench to a baby — triggers schedule generation")
    fun assignBench(
        @Valid @RequestBody request: BabyBenchAssignRequest,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<ApiResponse<BabyBenchAssignmentResponse>> {
        val user = userRepository.findByEmail(userDetails.username)
            .orElseThrow { ResourceNotFoundException("User not found") }
        val result = assignmentService.assignBenchToBaby(user.userId, request)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse(true, "Bench assigned. Schedule generated.", result))
    }

    @GetMapping("/baby/{babyId}/active")
    @Operation(summary = "Get the currently active bench assignment for a baby")
    fun getActiveAssignment(
        @PathVariable babyId: String
    ): ResponseEntity<ApiResponse<BabyBenchAssignmentResponse?>> =
        ResponseEntity.ok(
            ApiResponse(true, "Active assignment retrieved",
                assignmentService.getActiveAssignmentForBaby(babyId))
        )

    @GetMapping("/baby/{babyId}/history")
    @Operation(summary = "Get full bench assignment history for a baby")
    fun getAssignmentHistory(
        @PathVariable babyId: String
    ): ResponseEntity<ApiResponse<List<BabyBenchAssignmentResponse>>> =
        ResponseEntity.ok(
            ApiResponse(true, "History retrieved",
                assignmentService.getAssignmentHistoryForBaby(babyId))
        )

    @GetMapping("/bench/{benchId}/babies")
    @Operation(summary = "Get all babies assigned to a bench — Team view")
    fun getBabiesForBench(
        @PathVariable benchId: String
    ): ResponseEntity<ApiResponse<List<BabyBenchAssignmentResponse>>> =
        ResponseEntity.ok(
            ApiResponse(true, "Babies retrieved",
                assignmentService.getActiveBabiesForBench(benchId))
        )

    @PutMapping("/baby/{babyId}/change-bench")
    @Operation(summary = "Change a baby's bench — deactivates old, creates new, regenerates schedule")
    fun changeBench(
        @PathVariable babyId: String,
        @Valid @RequestBody request: BabyBenchAssignRequest,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<ApiResponse<BabyBenchAssignmentResponse>> {
        val user = userRepository.findByEmail(userDetails.username)
            .orElseThrow { ResourceNotFoundException("User not found") }
        val result = assignmentService.changeBench(babyId, user.userId, request)
        return ResponseEntity.ok(ApiResponse(true, "Bench changed. Schedule regenerated.", result))
    }
}