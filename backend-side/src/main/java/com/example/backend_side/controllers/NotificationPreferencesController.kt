package com.example.backend_side.controllers

import com.example.backend_side.ApiResponse
import com.example.backend_side.ResourceNotFoundException
import com.example.backend_side.entity.UserNotificationPreferences
import com.example.backend_side.repositories.UserNotificationPreferencesRepository
import com.example.backend_side.repositories.UserRepository
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

// ─────────────────────────────────────────────────────────────────────────────
// NotificationPreferencesController
//
// GET  /v1/notification-preferences/{userId}   → fetch prefs (creates defaults if missing)
// PUT  /v1/notification-preferences/{userId}   → update prefs
//
// The Android SettingsViewModel calls PUT whenever the user toggles a switch.
// PushNotificationScheduler calls the repository directly before sending.
// ─────────────────────────────────────────────────────────────────────────────

@RestController
@RequestMapping("/v1/notification-preferences")
@CrossOrigin(origins = ["*"])
@Tag(name = "Notification Preferences", description = "Per-user notification category toggles")
class NotificationPreferencesController(
    private val prefsRepo: UserNotificationPreferencesRepository,
    private val userRepo : UserRepository
) {

    @GetMapping("/{userId}")
    @Operation(summary = "Get notification preferences for a user — creates defaults if not set")
    fun getPreferences(
        @PathVariable userId: String
    ): ResponseEntity<ApiResponse<NotificationPreferencesResponse>> {
        val prefs = prefsRepo.findByUser_UserId(userId).orElseGet {
            val user = userRepo.findById(userId)
                .orElseThrow { ResourceNotFoundException("User not found: $userId") }
            prefsRepo.save(UserNotificationPreferences(user = user))
        }
        return ResponseEntity.ok(ApiResponse(true, "Preferences retrieved", prefs.toResponse()))
    }

    @PutMapping("/{userId}")
    @Operation(summary = "Update notification preferences — called by SettingsViewModel on toggle")
    fun updatePreferences(
        @PathVariable userId: String,
        @RequestBody request: NotificationPreferencesRequest
    ): ResponseEntity<ApiResponse<NotificationPreferencesResponse>> {
        val user = userRepo.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found: $userId") }

        val prefs = prefsRepo.findByUser_UserId(userId).orElseGet {
            prefsRepo.save(UserNotificationPreferences(user = user))
        }

        request.vaccination?.let    { prefs.vaccination    = it }
        request.growth?.let         { prefs.growth         = it }
        request.appointment?.let    { prefs.appointment    = it }
        request.health?.let         { prefs.health         = it }
        request.development?.let    { prefs.development    = it }
        request.milestones?.let     { prefs.milestones     = it }
        request.general?.let        { prefs.general        = it }
        request.reminderDaysBefore?.let { prefs.reminderDaysBefore = it.coerceIn(1, 14) }

        val saved = prefsRepo.save(prefs)
        return ResponseEntity.ok(ApiResponse(true, "Preferences updated", saved.toResponse()))
    }

    private fun UserNotificationPreferences.toResponse() = NotificationPreferencesResponse(
        userId             = userId,
        vaccination        = vaccination,
        growth             = growth,
        appointment        = appointment,
        health             = health,
        development        = development,
        milestones         = milestones,
        general            = general,
        reminderDaysBefore = reminderDaysBefore
    )
}

data class NotificationPreferencesRequest @JsonCreator constructor(
    @JsonProperty("vaccination")        val vaccination        : Boolean? = null,
    @JsonProperty("growth")             val growth             : Boolean? = null,
    @JsonProperty("appointment")        val appointment        : Boolean? = null,
    @JsonProperty("health")             val health             : Boolean? = null,
    @JsonProperty("development")        val development        : Boolean? = null,
    @JsonProperty("milestones")         val milestones         : Boolean? = null,
    @JsonProperty("general")            val general            : Boolean? = null,
    @JsonProperty("reminderDaysBefore") val reminderDaysBefore : Int?     = null
)

data class NotificationPreferencesResponse(
    val userId             : String,
    val vaccination        : Boolean,
    val growth             : Boolean,
    val appointment        : Boolean,
    val health             : Boolean,
    val development        : Boolean,
    val milestones         : Boolean,
    val general            : Boolean,
    val reminderDaysBefore : Int
)