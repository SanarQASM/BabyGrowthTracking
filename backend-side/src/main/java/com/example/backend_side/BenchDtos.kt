package com.example.backend_side

// ─────────────────────────────────────────────────────────────────────────────
// Bench-related request / response DTOs
//
// KEY FIX — workingDays / vaccinationDays / vaccinesAvailable:
//   The DB stores these as comma-separated Strings.
//   The API accepts and returns them as List<String>.
//   Jackson deserializes List<String> from a JSON array → no crash.
//   The service layer joins them back to comma strings before saving to DB.
//
//   The old code had a mismatch: the entity had @JsonIgnore on nothing,
//   so Jackson tried to deserialize "Sunday,Monday,..." (a String in the DB
//   response) as List<String> → MismatchedInputException.
//   Now the entity uses @JsonIgnore on raw fields + @JsonProperty on computed
//   list getters.  These DTOs always use List<String> for the API boundary.
// ─────────────────────────────────────────────────────────────────────────────

import com.example.backend_side.entity.BenchType
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

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

    // ── Accept as List<String> from JSON ── e.g. ["Sunday","Monday","Tuesday"]
    // The service converts them to comma-separated before saving.
    val workingDays        : List<String> = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday"),
    val vaccinationDays    : List<String> = listOf("Sunday", "Tuesday", "Thursday"),
    val workingHoursStart  : String       = "08:00",
    val workingHoursEnd    : String       = "14:00",
    val vaccinesAvailable  : List<String> = emptyList(),

    val type               : String       = "primary_health_center",

    // ── Optional: link the bench to a team member on creation ─────────────
    // When provided, the bench's teamMember is set immediately so the team
    // vaccination user can start receiving requests right away.
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
    val type               : String?       = null,
    val teamMemberId       : String?       = null
)

// ── Response (GET /v1/benches, etc.) ──────────────────────────────────────────
// Always returns List<String> for day/vaccine fields — never a raw comma string.
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
    val workingDays        : List<String>,   // ← always List in JSON
    val workingHoursStart  : String,
    val workingHoursEnd    : String,
    val vaccinationDays    : List<String>,   // ← always List in JSON
    val type               : String,
    val vaccinesAvailable  : List<String>,   // ← always List in JSON
    val isActive           : Boolean,

    // ── Team member info ───────────────────────────────────────────────────
    // Included in the response so the mobile app knows which team member
    // manages this bench (for routing bench requests correctly).
    val teamMemberId       : String?  = null,
    val teamMemberName     : String?  = null,
    val teamMemberEmail    : String?  = null
)