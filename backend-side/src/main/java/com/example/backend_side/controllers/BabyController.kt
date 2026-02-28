package com.example.backend_side.controllers

import com.example.backend_side.*
import com.example.backend_side.entity.Gender
import jakarta.validation.Valid
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

// ─────────────────────────────────────────────────────────────────────────────
// BabyController
//
// Base path: /v1/babies
// (Spring Boot context-path is /api, so full URL is /api/v1/babies)
//
// All responses are wrapped in ApiResponse<T> so the Android client's
// makeRequest() can deserialize { success, message, data } correctly.
// ─────────────────────────────────────────────────────────────────────────────

@RestController
@RequestMapping("/v1/babies")   // ✅ was "/api/babies" — wrong when context-path=/api
class BabyController(
    private val babyService: BabyService
) {

    // ── POST /v1/babies ───────────────────────────────────────────────────────
    // Frontend: ApiService.createBaby(parentUserId, request)
    //   header("X-User-Id", parentUserId)
    //   setBody(CreateBabyRequest)
    @PostMapping
    fun createBaby(
        @RequestHeader("X-User-Id") parentUserId: String,
        @RequestBody @Valid request: BabyCreateRequest
    ): ResponseEntity<ApiResponse<BabyResponse>> {
        val baby = babyService.createBaby(parentUserId, request)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse(success = true, message = "Baby created successfully", data = baby))
    }

    // ── GET /v1/babies/{babyId} ───────────────────────────────────────────────
    // Frontend: ApiService.getBaby(babyId)
    @GetMapping("/{babyId}")
    fun getBabyById(
        @PathVariable babyId: String
    ): ResponseEntity<ApiResponse<BabyResponse>> {
        val baby = babyService.getBabyById(babyId)
        return ResponseEntity.ok(
            ApiResponse(success = true, message = "Baby retrieved", data = baby)
        )
    }

    // ── GET /v1/babies/parent/{parentUserId} ──────────────────────────────────
    // Frontend: ApiService.getBabiesByParent(parentUserId)
    @GetMapping("/parent/{parentUserId}")
    fun getBabiesByParent(
        @PathVariable parentUserId: String
    ): ResponseEntity<ApiResponse<List<BabyResponse>>> {
        val babies = babyService.getBabiesByParent(parentUserId)
        return ResponseEntity.ok(
            ApiResponse(success = true, message = "Babies retrieved", data = babies)
        )
    }

    // ── PUT /v1/babies/{babyId} ───────────────────────────────────────────────
    // Frontend: ApiService.updateBaby(babyId, request)
    @PutMapping("/{babyId}")
    fun updateBaby(
        @PathVariable babyId: String,
        @RequestBody request: BabyUpdateRequest
    ): ResponseEntity<ApiResponse<BabyResponse>> {
        val baby = babyService.updateBaby(babyId, request)
        return ResponseEntity.ok(
            ApiResponse(success = true, message = "Baby updated successfully", data = baby)
        )
    }

    // ── PATCH /v1/babies/{babyId}/status ─────────────────────────────────────
    // Frontend: ApiService.updateBabyStatus(babyId, status)
    //   setBody(ArchiveBabyRequest(status)) where status = "INACTIVE" or "ACTIVE"
    @PatchMapping("/{babyId}/status")
    fun updateBabyStatus(
        @PathVariable babyId: String,
        @RequestBody request: BabyStatusRequest   // { "status": "INACTIVE" | "ACTIVE" }
    ): ResponseEntity<ApiResponse<BabyResponse>> {
        val isActive = request.status.equals("ACTIVE", ignoreCase = true)
        val baby = babyService.updateBaby(
            babyId,
            BabyUpdateRequest(isActive = isActive)
        )
        return ResponseEntity.ok(
            ApiResponse(success = true, message = "Baby status updated", data = baby)
        )
    }

    // ── DELETE /v1/babies/{babyId} ────────────────────────────────────────────
    // Frontend: ApiService.deleteBaby(babyId)
    @DeleteMapping("/{babyId}")
    fun deleteBaby(
        @PathVariable babyId: String
    ): ResponseEntity<ApiResponse<Unit>> {
        babyService.deleteBaby(babyId)
        return ResponseEntity.ok(
            ApiResponse(success = true, message = "Baby deleted successfully")
        )
    }

    // ── GET /v1/babies (paginated, admin use) ─────────────────────────────────
    @GetMapping
    fun getAllBabies(
        @RequestParam(defaultValue = "0")  page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<ApiResponse<Any>> {
        val babies = babyService.getAllBabies(PageRequest.of(page, size))
        return ResponseEntity.ok(
            ApiResponse(success = true, message = "All babies retrieved", data = babies)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Extra DTO needed by the status endpoint
// Add this to Dtos.kt if you prefer — kept here for clarity
// ─────────────────────────────────────────────────────────────────────────────

data class BabyStatusRequest(
    val status: String   // "ACTIVE" or "INACTIVE"
)