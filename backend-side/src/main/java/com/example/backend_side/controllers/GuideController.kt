package com.example.backend_side.controllers

import com.example.backend_side.*
import com.example.backend_side.entity.GuideType
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

/**
 * GuideController — REST API for Sleep Guide and Feeding Guide feedback.
 *
 * Base path: /api/v1/guide
 *
 * Endpoints:
 *   POST  /v1/guide/feedback          — cast or update a vote
 *   GET   /v1/guide/feedback/counts   — batch-fetch vote counts
 *
 * Authentication: JWT bearer token (resolved via Spring Security).
 * The userId is extracted from the authenticated principal, never from the
 * request body, to prevent impersonation.
 *
 * All responses follow the shared ApiResponse<T> envelope already used
 * throughout the application.
 */
@RestController
@RequestMapping("/v1/guide")
class GuideController(
    private val guideFeedbackService: GuideFeedbackService,
    private val userRepository      : com.example.backend_side.repositories.UserRepository
) {

    // ─────────────────────────────────────────────────────────────────────────
    // POST /v1/guide/feedback
    //
    // Cast or update a USEFUL / USELESS vote for a guide content card.
    //
    // Request body: { contentId, guideType, vote }
    // Response:     GuideFeedbackResponse (incl. updated usefulCount)
    // ─────────────────────────────────────────────────────────────────────────
    @PostMapping("/feedback")
    fun castVote(
        @AuthenticationPrincipal principal: UserDetails,
        @Valid @RequestBody       request  : GuideFeedbackRequest
    ): ResponseEntity<ApiResponse<GuideFeedbackResponse>> {
        val userId   = resolveUserId(principal)
        val response = guideFeedbackService.castVote(userId, request)
        return ResponseEntity.ok(
            ApiResponse(success = true, message = "Vote recorded", data = response)
        )
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /v1/guide/feedback/counts?guideType=SLEEP&contentIds=id1,id2,...
    //
    // Batch-fetch vote counts for a list of content IDs.
    // The current user's own vote for each item is included in the response.
    //
    // Query params:
    //   guideType  — SLEEP | FEEDING
    //   contentIds — comma-separated list of content IDs (max 100)
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/feedback/counts")
    fun getCounts(
        @AuthenticationPrincipal principal  : UserDetails,
        @RequestParam            guideType  : GuideType,
        @RequestParam            contentIds : List<String>
    ): ResponseEntity<ApiResponse<GuideContentCountsResponse>> {
        val userId   = resolveUserId(principal)
        val response = guideFeedbackService.getCounts(userId, contentIds, guideType)
        return ResponseEntity.ok(
            ApiResponse(success = true, message = "Counts fetched", data = response)
        )
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helper: resolve the real userId from the JWT principal.
    //
    // The JWT subject is the user's email (set in JwtUtil.generateToken).
    // We look up the User entity by email to get the stable UUID-based userId.
    // ─────────────────────────────────────────────────────────────────────────
    private fun resolveUserId(principal: UserDetails): String =
        userRepository.findByEmail(principal.username)
            .orElseThrow { ResourceNotFoundException("Authenticated user not found") }
            .userId
}