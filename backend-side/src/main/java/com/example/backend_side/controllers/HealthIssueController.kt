package com.example.backend_side.controllers

import com.example.backend_side.*
import com.example.backend_side.entity.Severity
import com.example.backend_side.repositories.BabyRepository
import com.example.backend_side.repositories.HealthIssueRepository
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.util.*

// ============================================================
// HEALTH ISSUE CONTROLLER — v1
// ============================================================
//
// ROOT CAUSE FIX for:
//   "No static resource v1/health-issues/baby/{babyId}"
//
// The OLD HealthIssueController was mapped to "/api/health-issues".
// The client calls "/v1/health-issues/baby/{babyId}".
// Spring couldn't find the route and fell back to its static
// resource handler, throwing NoResourceFoundException (404).
//
// Additionally, the old controller returned raw entity objects
// with no ApiResponse wrapper, so the client's
//   resp.body<ApiListResponse<HealthIssueNet>>()
// would always fail to deserialize.
//
// Fix: New controller at "/v1/health-issues" returning properly
// wrapped ApiResponse<T> with ISO-date strings via HealthIssueNet-
// compatible response shapes.
//
// NOTE: Keep the old HealthIssueController.kt file as-is — it
// handles "/api/health-issues" which is a separate legacy path.
// Spring will route /v1/... here and /api/... to the old one.
// ============================================================

@RestController
@RequestMapping("/v1/health-issues")
@Tag(name = "Health Issues V1", description = "Health issue management — client-facing API")
class HealthIssueV1Controller(
    private val healthIssueRepository: HealthIssueRepository,
    private val babyRepository: BabyRepository
) {

    // ── GET /v1/health-issues/baby/{babyId} ───────────────────────────────────
    // Client: ApiService.getHealthIssuesForBaby(babyId)
    // Response: ApiListResponse<HealthIssueNet>
    @GetMapping("/baby/{babyId}")
    @Operation(summary = "Get all health issues for a baby")
    fun getHealthIssuesByBaby(
        @PathVariable babyId: String
    ): ResponseEntity<ApiResponse<List<HealthIssueClientResponse>>> {
        val issues = healthIssueRepository.findByBaby_BabyIdOrderByIssueDateDesc(babyId)
            .map { it.toClientResponse() }
        return ResponseEntity.ok(ApiResponse(true, "Health issues retrieved", issues))
    }

    // ── POST /v1/health-issues ────────────────────────────────────────────────
    // Client: ApiService.createHealthIssue(babyId, title, description, severity, issueDate)
    // Body sent as Map<String, String?> with String dates — use StringCreateRequest
    @PostMapping
    @Operation(summary = "Create a new health issue")
    fun createHealthIssue(
        @RequestBody request: HealthIssueStringCreateRequest
    ): ResponseEntity<ApiResponse<HealthIssueClientResponse>> {
        val baby = babyRepository.findById(request.babyId)
            .orElseThrow { ResourceNotFoundException("Baby not found: ${request.babyId}") }

        val issue = com.example.backend_side.entity.HealthIssue(
            issueId     = UUID.randomUUID().toString(),
            baby        = baby,
            issueDate   = LocalDate.parse(request.issueDate),
            title       = request.title,
            description = request.description,
            severity    = request.severity?.uppercase()?.let {
                runCatching { Severity.valueOf(it) }.getOrNull()
            }
        )
        val saved = healthIssueRepository.save(issue)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse(true, "Health issue created", saved.toClientResponse()))
    }

    // ── PATCH /v1/health-issues/{issueId}/resolve ─────────────────────────────
    // Client: ApiService.resolveHealthIssue(issueId)
    @PatchMapping("/{issueId}/resolve")
    @Operation(summary = "Mark a health issue as resolved")
    fun resolveHealthIssue(
        @PathVariable issueId: String
    ): ResponseEntity<ApiResponse<HealthIssueClientResponse>> {
        val issue = healthIssueRepository.findById(issueId)
            .orElseThrow { ResourceNotFoundException("Health issue not found: $issueId") }
        issue.isResolved     = true
        issue.resolutionDate = LocalDate.now()
        val saved = healthIssueRepository.save(issue)
        return ResponseEntity.ok(ApiResponse(true, "Issue resolved", saved.toClientResponse()))
    }

    // ── Mapper — entity → client DTO ──────────────────────────────────────────
    // Produces field names matching HealthIssueNet on the client:
    //   issueId, babyId, title, description, issueDate (String),
    //   severity (String?), isResolved, resolutionDate (String?), resolvedNotes
    private fun com.example.backend_side.entity.HealthIssue.toClientResponse() =
        HealthIssueClientResponse(
            issueId        = issueId,
            babyId         = baby?.babyId ?: "",
            title          = title,
            description    = description,
            // ✅ LocalDate → "yyyy-MM-dd" string via JacksonConfig / .toString()
            issueDate      = issueDate.toString(),
            severity       = severity?.name,          // MILD / MODERATE / SEVERE / null
            isResolved     = isResolved,
            resolutionDate = resolutionDate?.toString(),
            resolvedNotes  = resolvedNotes
        )
}

// ── Response DTO matching HealthIssueNet on the client ────────────────────────
data class HealthIssueClientResponse(
    val issueId       : String,
    val babyId        : String,
    val title         : String,
    val description   : String? = null,
    val issueDate     : String,           // ISO "yyyy-MM-dd"
    val severity      : String? = null,   // "MILD" / "MODERATE" / "SEVERE" / null
    val isResolved    : Boolean = false,
    val resolutionDate: String? = null,   // ISO "yyyy-MM-dd"
    val resolvedNotes : String? = null
)

// ── Create request DTO — accepts String dates from client Map body ─────────────
// Client sends: { "babyId": "...", "title": "...", "issueDate": "2025-03-14", ... }
data class HealthIssueStringCreateRequest(
    val babyId     : String,
    val title      : String,
    val issueDate  : String,              // "yyyy-MM-dd" string
    val description: String? = null,
    val severity   : String? = null      // "mild" / "MILD" — case-insensitive
)