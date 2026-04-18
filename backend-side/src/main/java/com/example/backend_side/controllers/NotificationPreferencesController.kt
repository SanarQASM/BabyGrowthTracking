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
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*

// ─────────────────────────────────────────────────────────────────────────────
// FIX: StaleObjectStateException + DataIntegrityViolationException (race condition)
//
// PREVIOUS PROBLEM:
//   prefsRepo.findByUser_UserId(userId).orElseGet {
//       prefsRepo.save(UserNotificationPreferences(user = user))  ← NOT transactional
//   }
//
//   Two concurrent requests (e.g. app launch + scheduler) could both evaluate
//   findByUser_UserId() as empty and both try to INSERT — one would succeed,
//   the other would throw DataIntegrityViolationException (unique constraint on
//   user_id). Also, without @Transactional, Hibernate session may be closed
//   before the save completes, causing StaleObjectStateException.
//
// FIX:
//   1. @Transactional on both endpoints — ensures a single Hibernate session
//      wraps the find-then-save, so @MapsId resolution works correctly.
//   2. Use UserNotificationPreferences.defaultsFor(user) factory method which
//      pre-populates userId before save (avoids the "" → real-id flip).
//   3. The unique constraint on user_id is the DB-level safety net for the
//      rare race — Spring will surface this as a clear 409 rather than a
//      mysterious StaleObjectStateException.
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
    @Transactional                   // ← FIX: single session for find + optional save
    @Operation(summary = "Get notification preferences for a user — creates defaults if not set")
    fun getPreferences(
        @PathVariable userId: String
    ): ResponseEntity<ApiResponse<NotificationPreferencesResponse>> {
        val prefs = findOrCreate(userId)
        return ResponseEntity.ok(ApiResponse(true, "Preferences retrieved", prefs.toResponse()))
    }

    @PutMapping("/{userId}")
    @Transactional                   // ← FIX: single session for find + save
    @Operation(summary = "Update notification preferences — called by SettingsViewModel on toggle")
    fun updatePreferences(
        @PathVariable userId: String,
        @RequestBody request: NotificationPreferencesRequest
    ): ResponseEntity<ApiResponse<NotificationPreferencesResponse>> {
        val prefs = findOrCreate(userId)

        request.vaccination?.let        { prefs.vaccination        = it }
        request.growth?.let             { prefs.growth             = it }
        request.appointment?.let        { prefs.appointment        = it }
        request.health?.let             { prefs.health             = it }
        request.development?.let        { prefs.development        = it }
        request.milestones?.let         { prefs.milestones         = it }
        request.general?.let            { prefs.general            = it }
        request.reminderDaysBefore?.let { prefs.reminderDaysBefore = it.coerceIn(1, 14) }

        // No explicit save() needed — @Transactional + dirty-checking handles it.
        // But we call save() explicitly to be safe with the @MapsId entity.
        val saved = prefsRepo.save(prefs)
        return ResponseEntity.ok(ApiResponse(true, "Preferences updated", saved.toResponse()))
    }

    // ── Private helper — single findOrCreate, always inside a transaction ──────
    private fun findOrCreate(userId: String): UserNotificationPreferences {
        return prefsRepo.findByUser_UserId(userId).orElseGet {
            val user = userRepo.findById(userId)
                .orElseThrow { ResourceNotFoundException("User not found: $userId") }
            // FIX: Use factory method that pre-populates userId before save
            prefsRepo.save(UserNotificationPreferences.defaultsFor(user))
        }
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