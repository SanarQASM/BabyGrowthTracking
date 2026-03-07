package com.example.backend_side.controllers

import com.example.backend_side.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

// ============================================================
// VACCINATION BENCH CONTROLLER
// ============================================================

@RestController
@RequestMapping("/v1/benches")
@Tag(name = "Vaccination Benches", description = "Ministry of Health vaccination bench management")
class VaccinationBenchController(
    private val benchService: VaccinationBenchService
) {

    @GetMapping
    @Operation(summary = "Get all active benches")
    fun getAllBenches(): ResponseEntity<ApiResponse<List<VaccinationBenchResponse>>> =
        ResponseEntity.ok(ApiResponse(true, "Benches retrieved", benchService.getAllBenches()))

    @GetMapping("/governorate/{governorate}")
    @Operation(summary = "Get benches by governorate")
    fun getBenchesByGovernorate(@PathVariable governorate: String): ResponseEntity<ApiResponse<List<VaccinationBenchResponse>>> =
        ResponseEntity.ok(ApiResponse(true, "Benches retrieved", benchService.getBenchesByGovernorate(governorate)))

    @GetMapping("/{benchId}")
    @Operation(summary = "Get bench by ID")
    fun getBenchById(@PathVariable benchId: String): ResponseEntity<ApiResponse<VaccinationBenchResponse>> =
        ResponseEntity.ok(ApiResponse(true, "Bench retrieved", benchService.getBenchById(benchId)))

    @GetMapping("/search")
    @Operation(summary = "Search benches by name, district or governorate")
    fun searchBenches(@RequestParam query: String): ResponseEntity<ApiResponse<List<VaccinationBenchResponse>>> =
        ResponseEntity.ok(ApiResponse(true, "Search results", benchService.searchBenches(query)))

    @GetMapping("/governorates")
    @Operation(summary = "Get all governorates that have benches")
    fun getGovernorates(): ResponseEntity<ApiResponse<List<String>>> =
        ResponseEntity.ok(ApiResponse(true, "Governorates retrieved", benchService.getGovernorates()))

    @PostMapping
    @Operation(summary = "Create a new bench — Admin only")
    fun createBench(@Valid @RequestBody request: VaccinationBenchCreateRequest): ResponseEntity<ApiResponse<VaccinationBenchResponse>> =
        ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse(true, "Bench created", benchService.createBench(request)))

    @PutMapping("/{benchId}")
    @Operation(summary = "Update bench — Admin only")
    fun updateBench(@PathVariable benchId: String, @Valid @RequestBody request: VaccinationBenchUpdateRequest): ResponseEntity<ApiResponse<VaccinationBenchResponse>> =
        ResponseEntity.ok(ApiResponse(true, "Bench updated", benchService.updateBench(benchId, request)))

    @DeleteMapping("/{benchId}")
    @Operation(summary = "Deactivate a bench — Admin only")
    fun deactivateBench(@PathVariable benchId: String): ResponseEntity<ApiResponse<Nothing>> {
        benchService.deactivateBench(benchId)
        return ResponseEntity.ok(ApiResponse(true, "Bench deactivated"))
    }

    @PostMapping("/load-json")
    @Operation(summary = "Bulk load benches from JSON — Admin one-time import")
    fun loadFromJson(@RequestBody benches: List<VaccinationBenchCreateRequest>): ResponseEntity<ApiResponse<String>> {
        val count = benchService.loadBenchesFromJson(benches)
        return ResponseEntity.ok(ApiResponse(true, "Loaded $count benches"))
    }
}