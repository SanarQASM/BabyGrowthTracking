package com.example.backend_side.controllers

import com.example.backend_side.*
import com.example.backend_side.entity.AppointmentStatus
import com.example.backend_side.entity.AppointmentType
import com.example.backend_side.repositories.AppointmentRepository
import com.example.backend_side.repositories.BabyRepository
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

// ============================================================
// APPOINTMENT CONTROLLER — v1
// ============================================================
//
// ROOT CAUSE FIX for:
//   "No static resource v1/appointments/baby/{babyId}"
//
// The OLD AppointmentController was mapped to "/api/appointments".
// The client calls "/v1/appointments/baby/{babyId}".
// Spring couldn't find the route and fell back to its static
// resource handler, throwing NoResourceFoundException (404).
//
// Additionally, the old controller returned raw Appointment entity
// objects with no ApiResponse wrapper, so the client's
//   resp.body<ApiListResponse<AppointmentNet>>()
// would always fail to deserialize.
//
// Fix: New controller at "/v1/appointments" returning properly
// wrapped ApiResponse<T> responses with ISO-date strings matching
// AppointmentNet fields on the client side.
//
// NOTE: Keep the old AppointmentController.kt file as-is — it
// handles "/api/appointments" which is a separate legacy path.
// Spring will route /v1/... here and /api/... to the old one.
// ============================================================

@RestController
@RequestMapping("/v1/appointments")
@Tag(name = "Appointments V1", description = "Appointment management — client-facing API")
class AppointmentV1Controller(
    private val appointmentRepository: AppointmentRepository,
    private val babyRepository: BabyRepository
) {

    // ── GET /v1/appointments/baby/{babyId} ────────────────────────────────────
    // Client: ApiService.getAppointmentsForBaby(babyId)
    // Response: ApiListResponse<AppointmentNet>
    @GetMapping("/baby/{babyId}")
    @Operation(summary = "Get all appointments for a baby")
    fun getAppointmentsByBaby(
        @PathVariable babyId: String
    ): ResponseEntity<ApiResponse<List<AppointmentClientResponse>>> {
        val appointments = appointmentRepository
            .findByBaby_BabyIdOrderByScheduledDateAsc(babyId)
            .map { it.toClientResponse() }
        return ResponseEntity.ok(ApiResponse(true, "Appointments retrieved", appointments))
    }

    // ── POST /v1/appointments ─────────────────────────────────────────────────
    // Client: ApiService.createAppointment(babyId, type, date, time, ...)
    // Body sent as Map<String, String?> with String dates
    @PostMapping
    @Operation(summary = "Create a new appointment")
    fun createAppointment(
        @RequestBody request: AppointmentStringCreateRequest
    ): ResponseEntity<ApiResponse<AppointmentClientResponse>> {
        val baby = babyRepository.findById(request.babyId)
            .orElseThrow { ResourceNotFoundException("Baby not found: ${request.babyId}") }

        val appointmentType = runCatching {
            AppointmentType.valueOf(request.appointmentType.uppercase())
        }.getOrElse { AppointmentType.CHECKUP }

        val appointment = com.example.backend_side.entity.Appointment(
            appointmentId   = UUID.randomUUID().toString(),
            baby            = baby,
            appointmentType = appointmentType,
            scheduledDate   = LocalDate.parse(request.scheduledDate),
            scheduledTime   = request.scheduledTime?.let { runCatching { LocalTime.parse(it) }.getOrNull() },
            durationMinutes = request.durationMinutes ?: 30,
            doctorName      = request.doctorName,
            location        = request.location,
            notes           = request.notes
        )
        val saved = appointmentRepository.save(appointment)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse(true, "Appointment created", saved.toClientResponse()))
    }

    // ── PATCH /v1/appointments/{appointmentId}/cancel ─────────────────────────
    // Client: ApiService.cancelAppointment(appointmentId)
    @PatchMapping("/{appointmentId}/cancel")
    @Operation(summary = "Cancel an appointment")
    fun cancelAppointment(
        @PathVariable appointmentId: String
    ): ResponseEntity<ApiResponse<AppointmentClientResponse>> {
        val appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow { ResourceNotFoundException("Appointment not found: $appointmentId") }
        appointment.status = AppointmentStatus.CANCELLED
        val saved = appointmentRepository.save(appointment)
        return ResponseEntity.ok(ApiResponse(true, "Appointment cancelled", saved.toClientResponse()))
    }

    // ── Mapper — entity → client DTO ──────────────────────────────────────────
    // Produces field names matching AppointmentNet on the client:
    //   appointmentId, babyId, babyName, appointmentType (String),
    //   scheduledDate (String), scheduledTime (String?),
    //   durationMinutes, status (String), doctorName, location, notes
    private fun com.example.backend_side.entity.Appointment.toClientResponse() =
        AppointmentClientResponse(
            appointmentId   = appointmentId,
            babyId          = baby?.babyId ?: "",
            babyName        = baby?.fullName ?: "",
            // ✅ Enum → String name e.g. "CHECKUP", "VACCINATION"
            appointmentType = appointmentType?.name ?: "CHECKUP",
            // ✅ LocalDate → "yyyy-MM-dd" string
            scheduledDate   = scheduledDate.toString(),
            // ✅ LocalTime → "HH:mm:ss" string or null
            scheduledTime   = scheduledTime?.toString(),
            durationMinutes = durationMinutes,
            // ✅ Enum → String name e.g. "SCHEDULED", "CANCELLED"
            status          = status?.name ?: "SCHEDULED",
            doctorName      = doctorName,
            location        = location,
            notes           = notes
        )
}

// ── Response DTO matching AppointmentNet on the client ────────────────────────
data class AppointmentClientResponse(
    val appointmentId  : String,
    val babyId         : String,
    val babyName       : String,
    val appointmentType: String,          // "CHECKUP" / "VACCINATION" / etc.
    val scheduledDate  : String,          // ISO "yyyy-MM-dd"
    val scheduledTime  : String? = null,  // "HH:mm:ss" or null
    val durationMinutes: Int = 30,
    val status         : String,          // "SCHEDULED" / "COMPLETED" / "CANCELLED"
    val doctorName     : String? = null,
    val location       : String? = null,
    val notes          : String? = null
)

// ── Create request DTO — accepts String dates from client Map body ─────────────
// Client sends:
//   { "babyId": "...", "appointmentType": "checkup",
//     "scheduledDate": "2025-04-01", "scheduledTime": "10:00", ... }
data class AppointmentStringCreateRequest(
    val babyId         : String,
    val appointmentType: String,          // case-insensitive, e.g. "checkup" or "CHECKUP"
    val scheduledDate  : String,          // "yyyy-MM-dd"
    val scheduledTime  : String? = null,
    val durationMinutes: Int?    = 30,
    val doctorName     : String? = null,
    val location       : String? = null,
    val notes          : String? = null
)