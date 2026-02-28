package com.example.backend_side.controllers

import com.example.backend_side.entity.Appointment
import com.example.backend_side.entity.AppointmentStatus
import com.example.backend_side.entity.AppointmentType
import com.example.backend_side.repositories.AppointmentRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.util.*

@RestController
@RequestMapping("/api/appointments")
@CrossOrigin(origins = ["*"])
class AppointmentController(private val appointmentRepository: AppointmentRepository) {

    @GetMapping
    fun getAllAppointments(): ResponseEntity<List<Appointment>> {
        return ResponseEntity.ok(appointmentRepository.findAll())
    }

    @GetMapping("/{appointmentId}")
    fun getAppointmentById(@PathVariable appointmentId: String): ResponseEntity<Appointment> {
        return appointmentRepository.findById(appointmentId)
            .map { ResponseEntity.ok(it) }
            .orElse(ResponseEntity.notFound().build())
    }

    @GetMapping("/baby/{babyId}")
    fun getAppointmentsByBaby(@PathVariable babyId: String): ResponseEntity<List<Appointment>> {
        return ResponseEntity.ok(appointmentRepository.findByBaby_BabyIdOrderByScheduledDateAsc(babyId))
    }

    @GetMapping("/baby/{babyId}/upcoming")
    fun getUpcomingAppointmentsForBaby(@PathVariable babyId: String): ResponseEntity<List<Appointment>> {
        return ResponseEntity.ok(appointmentRepository.findUpcomingAppointmentsForBaby(babyId))
    }

    @GetMapping("/upcoming")
    fun getUpcomingAppointments(): ResponseEntity<List<Appointment>> {
        return ResponseEntity.ok(appointmentRepository.findUpcomingAppointments(LocalDate.now()))
    }

    @GetMapping("/status/{status}")
    fun getAppointmentsByStatus(@PathVariable status: AppointmentStatus): ResponseEntity<List<Appointment>> {
        return ResponseEntity.ok(appointmentRepository.findByStatus(status))
    }

    @GetMapping("/type/{appointmentType}")
    fun getAppointmentsByType(@PathVariable appointmentType: AppointmentType): ResponseEntity<List<Appointment>> {
        return ResponseEntity.ok(appointmentRepository.findByAppointmentType(appointmentType))
    }

    @GetMapping("/reminders-needed")
    fun getAppointmentsNeedingReminder(@RequestParam date: String): ResponseEntity<List<Appointment>> {
        val targetDate = LocalDate.parse(date)
        return ResponseEntity.ok(appointmentRepository.findAppointmentsNeedingReminder(targetDate))
    }

    @PostMapping
    fun createAppointment(@RequestBody appointment: Appointment): ResponseEntity<Appointment> {
        if (appointment.appointmentId.isEmpty()) {
            appointment.appointmentId = UUID.randomUUID().toString()
        }
        val savedAppointment = appointmentRepository.save(appointment)
        return ResponseEntity.status(HttpStatus.CREATED).body(savedAppointment)
    }

    @PutMapping("/{appointmentId}")
    fun updateAppointment(
        @PathVariable appointmentId: String,
        @RequestBody appointment: Appointment
    ): ResponseEntity<Appointment> {
        return if (appointmentRepository.existsById(appointmentId)) {
            appointment.appointmentId = appointmentId
            ResponseEntity.ok(appointmentRepository.save(appointment))
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PatchMapping("/{appointmentId}/status")
    fun updateAppointmentStatus(
        @PathVariable appointmentId: String,
        @RequestParam status: AppointmentStatus
    ): ResponseEntity<Appointment> {
        return appointmentRepository.findById(appointmentId)
            .map { appointment ->
                appointment.status = status
                ResponseEntity.ok(appointmentRepository.save(appointment))
            }
            .orElse(ResponseEntity.notFound().build())
    }

    @PatchMapping("/{appointmentId}/send-reminder")
    fun markReminderSent(@PathVariable appointmentId: String): ResponseEntity<Appointment> {
        return appointmentRepository.findById(appointmentId)
            .map { appointment ->
                appointment.reminderSent = true
                ResponseEntity.ok(appointmentRepository.save(appointment))
            }
            .orElse(ResponseEntity.notFound().build())
    }

    @DeleteMapping("/{appointmentId}")
    fun deleteAppointment(@PathVariable appointmentId: String): ResponseEntity<Void> {
        return if (appointmentRepository.existsById(appointmentId)) {
            appointmentRepository.deleteById(appointmentId)
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }
}