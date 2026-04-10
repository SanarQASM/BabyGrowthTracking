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
 * FIX: getCounts() now accepts contentIds as a comma-separated single string
 * parameter ("id1,id2,id3") in addition to repeated params ("contentIds=a&contentIds=b").
 * The client sends a joined string; Spring's List<String> binding only works
 * for repeated params, so we split manually here.
 *
 * castVote() response now correctly reflects toggle-off (vote=null) so the
 * client can clear the button highlight state.
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
    // FIX: The response now includes vote=null when a vote was toggled off
    // (same vote tapped twice). The client reads this to clear the highlight.
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
    // GET /v1/guide/feedback/counts?guideType=SLEEP&contentIds=id1,id2,id3
    //
    // FIX: Accept contentIds as a single comma-separated string param OR as
    // repeated params. The Kotlin client sends a single joined string via
    // parameter("contentIds", idsParam), so we split on "," here.
    // Spring's List<String> binding works for ?contentIds=a&contentIds=b
    // but NOT for ?contentIds=a,b — that arrives as a single string "a,b".
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/feedback/counts")
    fun getCounts(
        @AuthenticationPrincipal principal : UserDetails,
        @RequestParam            guideType : GuideType,
        @RequestParam            contentIds: String        // comma-separated or single id
    ): ResponseEntity<ApiResponse<GuideContentCountsResponse>> {
        val userId  = resolveUserId(principal)
        val idList  = contentIds.split(",").map { it.trim() }.filter { it.isNotBlank() }
        val response = guideFeedbackService.getCounts(userId, idList, guideType)
        return ResponseEntity.ok(
            ApiResponse(success = true, message = "Counts fetched", data = response)
        )
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helper
    // ─────────────────────────────────────────────────────────────────────────
    private fun resolveUserId(principal: UserDetails): String =
        userRepository.findByEmail(principal.username)
            .orElseThrow { ResourceNotFoundException("Authenticated user not found") }
            .userId
}