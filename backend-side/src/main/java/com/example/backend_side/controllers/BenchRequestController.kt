package com.example.backend_side

import com.example.backend_side.entity.BenchRequest
import com.example.backend_side.entity.BenchRequestStatus
import com.example.backend_side.repositories.*
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.transaction.Transactional
import jakarta.validation.constraints.NotBlank
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import java.util.UUID

// ─────────────────────────────────────────────────────────────────────────────
// DTOs
// ─────────────────────────────────────────────────────────────────────────────

data class BenchRequestCreateRequest @JsonCreator constructor(
    @JsonProperty("babyId")  @field:NotBlank(message = "Baby ID required")  val babyId : String,
    @JsonProperty("benchId") @field:NotBlank(message = "Bench ID required") val benchId: String,
    @JsonProperty("notes")   val notes: String? = null
)

data class BenchRequestReviewRequest @JsonCreator constructor(
    @JsonProperty("action")       val action      : String,
    @JsonProperty("rejectReason") val rejectReason: String? = null
)

data class BenchRequestResponse(
    val requestId      : String,
    val babyId         : String,
    val babyName       : String,
    val benchId        : String,
    val benchNameEn    : String,
    val benchNameAr    : String,
    val governorate    : String,
    val status         : String,
    val rejectReason   : String?  = null,
    val notes          : String?  = null,
    val reviewedByName : String?  = null,
    val reviewedAt     : String?  = null,
    val createdAt      : String?  = null,
    val teamMemberId   : String?  = null,
    val teamMemberName : String?  = null
)

// ─────────────────────────────────────────────────────────────────────────────
// Service
// ─────────────────────────────────────────────────────────────────────────────

interface BenchRequestService {
    fun sendRequest(userId: String, request: BenchRequestCreateRequest): BenchRequestResponse
    fun getRequestsForBench(benchId: String): List<BenchRequestResponse>
    fun getPendingRequestsForBench(benchId: String): List<BenchRequestResponse>
    fun getRequestsForBaby(babyId: String): List<BenchRequestResponse>
    fun getActiveRequestForBaby(babyId: String): BenchRequestResponse?
    fun reviewRequest(requestId: String, reviewerId: String, action: String, rejectReason: String?): BenchRequestResponse
    fun cancelRequest(requestId: String, userId: String): BenchRequestResponse
}

@Service
@Transactional
class BenchRequestServiceImpl(
    private val benchRequestRepository  : BenchRequestRepository,
    private val babyRepository          : BabyRepository,
    private val benchRepository         : VaccinationBenchRepository,
    private val userRepository          : UserRepository,
    private val assignmentRepository    : BabyBenchAssignmentRepository,
    private val scheduleGeneratorService: ScheduleGeneratorService
) : BenchRequestService {

    override fun sendRequest(userId: String, request: BenchRequestCreateRequest): BenchRequestResponse {
        val user  = userRepository.findById(userId).orElseThrow { ResourceNotFoundException("User not found") }
        val baby  = babyRepository.findById(request.babyId).orElseThrow { ResourceNotFoundException("Baby not found: ${request.babyId}") }
        val bench = benchRepository.findById(request.benchId).orElseThrow { ResourceNotFoundException("Bench not found: ${request.benchId}") }

        // Guard: only one active request per baby
        val existing = benchRequestRepository.findActiveRequestForBaby(request.babyId)
        if (existing.isPresent) {
            throw BadRequestException("Baby already has an active bench request. Cancel it first.")
        }

        if (bench.teamMember == null) {
            throw BadRequestException(
                "This health center has no assigned vaccination team yet. " +
                        "Please contact the admin to assign a team member first."
            )
        }

        val benchRequest = BenchRequest(
            requestId   = UUID.randomUUID().toString(),
            baby        = baby,
            bench       = bench,
            requestedBy = user,
            status      = BenchRequestStatus.PENDING,
            notes       = request.notes
        )
        val saved = benchRequestRepository.save(benchRequest)
        return saved.toResponse()
    }

    override fun getRequestsForBench(benchId: String): List<BenchRequestResponse> =
        benchRequestRepository.findByBench_BenchId(benchId).map { it.toResponse() }

    override fun getPendingRequestsForBench(benchId: String): List<BenchRequestResponse> =
        benchRequestRepository.findPendingForBench(benchId).map { it.toResponse() }

    override fun getRequestsForBaby(babyId: String): List<BenchRequestResponse> =
        benchRequestRepository.findByBaby_BabyId(babyId).map { it.toResponse() }

    override fun getActiveRequestForBaby(babyId: String): BenchRequestResponse? =
        benchRequestRepository.findActiveRequestForBaby(babyId).map { it.toResponse() }.orElse(null)

    override fun reviewRequest(
        requestId   : String,
        reviewerId  : String,
        action      : String,
        rejectReason: String?
    ): BenchRequestResponse {
        val benchRequest = benchRequestRepository.findById(requestId)
            .orElseThrow { ResourceNotFoundException("Request not found: $requestId") }
        val reviewer = userRepository.findById(reviewerId)
            .orElseThrow { ResourceNotFoundException("Reviewer not found") }

        if (benchRequest.status != BenchRequestStatus.PENDING) {
            throw BadRequestException("Request is already ${benchRequest.status.name.lowercase()}")
        }

        val assignedTeamMemberId = benchRequest.bench?.teamMember?.userId
        if (assignedTeamMemberId != null && assignedTeamMemberId != reviewerId) {
            throw ForbiddenException("Only the team member assigned to this bench can review requests")
        }

        when (action.lowercase()) {
            "accept" -> {
                benchRequest.status     = BenchRequestStatus.ACCEPTED
                benchRequest.reviewedBy = reviewer
                benchRequest.reviewedAt = LocalDateTime.now()

                // Deactivate old active assignment
                assignmentRepository.findByBaby_BabyIdAndIsActiveTrue(benchRequest.baby!!.babyId)
                    .ifPresent { old ->
                        old.isActive = false
                        assignmentRepository.save(old)
                    }

                // Create new assignment
                val assignment = com.example.backend_side.entity.BabyBenchAssignment(
                    assignmentId = UUID.randomUUID().toString(),
                    baby         = benchRequest.baby,
                    bench        = benchRequest.bench,
                    assignedBy   = reviewer,
                    assignedAt   = LocalDateTime.now(),
                    isActive     = true
                )
                assignmentRepository.save(assignment)

                // FIX: check if existing schedules point to a different bench.
                // If so, regenerate (update bench + recalculate dates) rather than
                // blindly generating new rows on top of old ones.
                // generateScheduleForBaby already skips vaccines that exist — but
                // existing schedules still point to the OLD bench. We must update them.
                val newBench = benchRequest.bench!!
                scheduleGeneratorService.regenerateScheduleOnBenchChange(
                    benchRequest.baby!!.babyId, newBench
                )
                // Then generate any NEW vaccine rows not yet scheduled
                scheduleGeneratorService.generateScheduleForBaby(benchRequest.baby!!, newBench)
            }
            "reject" -> {
                if (rejectReason.isNullOrBlank()) throw BadRequestException("Reject reason is required")
                benchRequest.status       = BenchRequestStatus.REJECTED
                benchRequest.rejectReason = rejectReason
                benchRequest.reviewedBy   = reviewer
                benchRequest.reviewedAt   = LocalDateTime.now()
            }
            else -> throw BadRequestException("Invalid action '$action'. Use 'accept' or 'reject'")
        }

        val saved = benchRequestRepository.save(benchRequest)
        return saved.toResponse()
    }

    override fun cancelRequest(requestId: String, userId: String): BenchRequestResponse {
        val benchRequest = benchRequestRepository.findById(requestId)
            .orElseThrow { ResourceNotFoundException("Request not found: $requestId") }

        if (benchRequest.requestedBy?.userId != userId)
            throw ForbiddenException("Cannot cancel another user's request")
        if (benchRequest.status != BenchRequestStatus.PENDING)
            throw BadRequestException("Only pending requests can be cancelled")

        benchRequest.status = BenchRequestStatus.CANCELLED
        return benchRequestRepository.save(benchRequest).toResponse()
    }

    private fun BenchRequest.toResponse() = BenchRequestResponse(
        requestId      = requestId,
        babyId         = baby?.babyId ?: "",
        babyName       = baby?.fullName ?: "",
        benchId        = bench?.benchId ?: "",
        benchNameEn    = bench?.nameEn ?: "",
        benchNameAr    = bench?.nameAr ?: "",
        governorate    = bench?.governorate ?: "",
        status         = status.name,
        rejectReason   = rejectReason,
        notes          = notes,
        reviewedByName = reviewedBy?.fullName,
        reviewedAt     = reviewedAt?.toString(),
        createdAt      = createdAt?.toString(),
        teamMemberId   = bench?.teamMember?.userId,
        teamMemberName = bench?.teamMember?.fullName
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Controller
// ─────────────────────────────────────────────────────────────────────────────

@RestController
@RequestMapping("/v1/bench-requests")
@Tag(name = "Bench Requests", description = "Parent-to-bench join request workflow")
class BenchRequestController(
    private val benchRequestService: BenchRequestService,
    private val userRepository     : UserRepository
) {

    @PostMapping
    @Operation(summary = "Parent sends a join request to a bench")
    fun sendRequest(
        @RequestBody request: BenchRequestCreateRequest,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<ApiResponse<BenchRequestResponse>> {
        val user = userRepository.findByEmail(userDetails.username)
            .orElseThrow { ResourceNotFoundException("User not found") }
        val result = benchRequestService.sendRequest(user.userId, request)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse(true, "Request sent. Waiting for bench approval.", result))
    }

    @GetMapping("/baby/{babyId}/active")
    @Operation(summary = "Get active (pending/accepted) request for a baby")
    fun getActiveRequestForBaby(
        @PathVariable babyId: String
    ): ResponseEntity<ApiResponse<BenchRequestResponse?>> =
        ResponseEntity.ok(ApiResponse(true, "Active request retrieved", benchRequestService.getActiveRequestForBaby(babyId)))

    @GetMapping("/baby/{babyId}/history")
    @Operation(summary = "Get all requests for a baby")
    fun getRequestsForBaby(
        @PathVariable babyId: String
    ): ResponseEntity<ApiResponse<List<BenchRequestResponse>>> =
        ResponseEntity.ok(ApiResponse(true, "Requests retrieved", benchRequestService.getRequestsForBaby(babyId)))

    @GetMapping("/bench/{benchId}/pending")
    @Operation(summary = "Get pending requests for a bench — Team view")
    fun getPendingForBench(
        @PathVariable benchId: String
    ): ResponseEntity<ApiResponse<List<BenchRequestResponse>>> =
        ResponseEntity.ok(ApiResponse(true, "Pending requests retrieved", benchRequestService.getPendingRequestsForBench(benchId)))

    @GetMapping("/bench/{benchId}/all")
    @Operation(summary = "Get all requests for a bench — Team view")
    fun getAllForBench(
        @PathVariable benchId: String
    ): ResponseEntity<ApiResponse<List<BenchRequestResponse>>> =
        ResponseEntity.ok(ApiResponse(true, "All requests retrieved", benchRequestService.getRequestsForBench(benchId)))

    @PutMapping("/{requestId}/review")
    @Operation(summary = "Team vaccination reviews (accepts or rejects) a request")
    fun reviewRequest(
        @PathVariable requestId: String,
        @RequestBody  request  : BenchRequestReviewRequest,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<ApiResponse<BenchRequestResponse>> {
        val reviewer = userRepository.findByEmail(userDetails.username)
            .orElseThrow { ResourceNotFoundException("Reviewer not found") }
        val result = benchRequestService.reviewRequest(
            requestId    = requestId,
            reviewerId   = reviewer.userId,
            action       = request.action,
            rejectReason = request.rejectReason
        )
        return ResponseEntity.ok(ApiResponse(true, "Request ${request.action}ed", result))
    }

    @PutMapping("/{requestId}/cancel")
    @Operation(summary = "Parent cancels a pending request")
    fun cancelRequest(
        @PathVariable requestId: String,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<ApiResponse<BenchRequestResponse>> {
        val user = userRepository.findByEmail(userDetails.username)
            .orElseThrow { ResourceNotFoundException("User not found") }
        val result = benchRequestService.cancelRequest(requestId, user.userId)
        return ResponseEntity.ok(ApiResponse(true, "Request cancelled", result))
    }
}