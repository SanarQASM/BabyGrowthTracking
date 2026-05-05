package com.example.backend_side

import com.example.backend_side.entity.BenchType
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.LocalDateTime

// ─────────────────────────────────────────────────────────────────────────────
// Bench-related request / response DTOs  — FIXED
//
// FIX 1 — VaccinationBenchCreateRequest.type:
//   Was: type: String = "primary_health_center"
//   The service passed this String directly to VaccinationBench.type which is
//   typed as BenchType enum. The String was silently ignored because the entity
//   used its own default. Now typed as BenchType so Jackson deserializes it
//   through the registered BenchTypeConverter correctly.
//
// FIX 2 — VaccinationBenchUpdateRequest.type:
//   Same problem as above — was String?, now BenchType? so it can be applied
//   in the service update block.
//
// FIX 3 — VaccinationBenchResponse.createdAt:
//   Added LocalDateTime? field so the response carries the entity's audit
//   timestamp (used by the admin bench card in the UI).
// ─────────────────────────────────────────────────────────────────────────────

// ── Create request (POST /v1/benches) ─────────────────────────────────────────
@JsonIgnoreProperties(ignoreUnknown = true)
data class VaccinationBenchCreateRequest(
    val nameEn            : String,
    val nameAr            : String,
    val governorate       : String,
    val district          : String       = "",
    val addressEn         : String?      = null,
    val addressAr         : String?      = null,
    val latitude          : Double,
    val longitude         : Double,
    val phone             : String?      = null,

    // Accept as List<String> from JSON — e.g. ["Sunday","Monday","Tuesday"]
    val workingDays        : List<String> = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday"),
    val vaccinationDays    : List<String> = listOf("Sunday", "Tuesday", "Thursday"),
    val workingHoursStart  : String       = "08:00",
    val workingHoursEnd    : String       = "14:00",
    val vaccinesAvailable  : List<String> = emptyList(),

    // FIX 1: was String — now BenchType so Jackson uses the registered converter
    // and the service can assign it directly to VaccinationBench.type
    val type               : BenchType    = BenchType.PRIMARY_HEALTH_CENTER,

    // Optional: link the bench to a team member on creation
    val teamMemberId       : String?      = null
)

// ── Update request (PUT /v1/benches/{id}) ─────────────────────────────────────
@JsonIgnoreProperties(ignoreUnknown = true)
data class VaccinationBenchUpdateRequest(
    val nameEn            : String?       = null,
    val nameAr            : String?       = null,
    val governorate       : String?       = null,
    val district          : String?       = null,
    val addressEn         : String?       = null,
    val addressAr         : String?       = null,
    val latitude          : Double?       = null,
    val longitude         : Double?       = null,
    val phone             : String?       = null,
    val workingDays        : List<String>? = null,
    val vaccinationDays    : List<String>? = null,
    val workingHoursStart  : String?       = null,
    val workingHoursEnd    : String?       = null,
    val vaccinesAvailable  : List<String>? = null,
    val isActive           : Boolean?      = null,
    // FIX 2: was String? — now BenchType? so the service update block can assign it
    val type               : BenchType?    = null,
    val teamMemberId       : String?       = null
)

// ── Response (GET /v1/benches, etc.) ──────────────────────────────────────────
data class VaccinationBenchResponse(
    val benchId            : String,
    val nameEn             : String,
    val nameAr             : String,
    val governorate        : String,
    val district           : String,
    val addressEn          : String?,
    val addressAr          : String?,
    val latitude           : Double,
    val longitude          : Double,
    val phone              : String?,
    val workingDays        : List<String>,
    val workingHoursStart  : String,
    val workingHoursEnd    : String,
    val vaccinationDays    : List<String>,
    // FIX 2 (response): expose type as String (enum name) for the client
    val type               : String,
    val vaccinesAvailable  : List<String>,
    val isActive           : Boolean,
    // Team member info
    val teamMemberId       : String?        = null,
    val teamMemberName     : String?        = null,
    val teamMemberEmail    : String?        = null,
    // FIX 3: expose audit timestamp
    val createdAt          : LocalDateTime? = null
)