package org.example.project.babygrowthtrackingapplication.data.network

import kotlinx.serialization.Serializable

// ─────────────────────────────────────────────────────────────────────────────
// Memory Network DTOs
// ─────────────────────────────────────────────────────────────────────────────

@Serializable
data class CreateMemoryRequest(
    val babyId      : String,
    val title       : String,
    val description : String?      = null,
    val memoryDate  : String,      // "YYYY-MM-DD"
    val imageCount  : Int          = 0,    // number of images stored locally
    val captions    : List<String>? = null
)

@Serializable
data class MemoryNet(
    val memoryId    : String,
    val babyId      : String,
    val babyName    : String,
    val title       : String,
    val description : String?      = null,
    val memoryDate  : String,
    val ageInMonths : Int?         = null,
    val ageInDays   : Int?         = null,
    val imageCount  : Int?         = 0,    // how many images stored locally
    val captions    : List<String>? = null,
    val createdAt   : String?      = null,
    val updatedAt   : String?      = null
)