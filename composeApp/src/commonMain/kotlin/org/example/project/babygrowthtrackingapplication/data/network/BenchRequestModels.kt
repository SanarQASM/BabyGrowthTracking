// File: composeApp/src/commonMain/kotlin/org/example/project/babygrowthtrackingapplication/data/network/BenchRequestModels.kt

package org.example.project.babygrowthtrackingapplication.data.network

import kotlinx.serialization.Serializable

// ─────────────────────────────────────────────────────────────────────────────
// Network DTOs
// ─────────────────────────────────────────────────────────────────────────────

@Serializable
data class BenchRequestNet(
    val requestId      : String,
    val babyId         : String,
    val babyName       : String,
    val benchId        : String,
    val benchNameEn    : String,
    val benchNameAr    : String,
    val governorate    : String,
    val status         : String,            // PENDING | ACCEPTED | REJECTED | CANCELLED
    val rejectReason   : String? = null,
    val notes          : String? = null,
    val reviewedByName : String? = null,
    val reviewedAt     : String? = null,
    val createdAt      : String? = null
)

// ─────────────────────────────────────────────────────────────────────────────
// UI models
// ─────────────────────────────────────────────────────────────────────────────

enum class BenchRequestStatusUi { PENDING, ACCEPTED, REJECTED, CANCELLED }

data class BenchRequestUi(
    val requestId      : String,
    val babyId         : String,
    val babyName       : String,
    val benchId        : String,
    val benchNameEn    : String,
    val benchNameAr    : String,
    val governorate    : String,
    val status         : BenchRequestStatusUi,
    val rejectReason   : String? = null,
    val notes          : String? = null,
    val reviewedByName : String? = null,
    val reviewedAt     : String? = null,
    val createdAt      : String? = null
)

fun BenchRequestNet.toUi() = BenchRequestUi(
    requestId      = requestId,
    babyId         = babyId,
    babyName       = babyName,
    benchId        = benchId,
    benchNameEn    = benchNameEn,
    benchNameAr    = benchNameAr,
    governorate    = governorate,
    status         = when (status.uppercase()) {
        "ACCEPTED"  -> BenchRequestStatusUi.ACCEPTED
        "REJECTED"  -> BenchRequestStatusUi.REJECTED
        "CANCELLED" -> BenchRequestStatusUi.CANCELLED
        else        -> BenchRequestStatusUi.PENDING
    },
    rejectReason   = rejectReason,
    notes          = notes,
    reviewedByName = reviewedByName,
    reviewedAt     = reviewedAt,
    createdAt      = createdAt
)