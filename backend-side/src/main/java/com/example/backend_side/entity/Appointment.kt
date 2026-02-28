package com.example.backend_side.entity

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Entity
@Table(name = "appointments")
data class Appointment(
    @Id
    @Column(name = "appointment_id", columnDefinition = "VARCHAR(36)", updatable = false, nullable = false)
    var appointmentId: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "baby_id", nullable = false)
    var baby: Baby? = null,

    @Column(name = "appointment_type", columnDefinition = "ENUM('checkup','vaccination','consultation','emergency','follow_up')")
    var appointmentType: AppointmentType? = null,

    @Column(name = "scheduled_date", nullable = false)
    var scheduledDate: LocalDate = LocalDate.now(),

    @Column(name = "scheduled_time")
    var scheduledTime: LocalTime? = null,

    @Column(name = "duration_minutes")
    var durationMinutes: Int = 30,

    @Column(name = "status", columnDefinition = "ENUM('scheduled','confirmed','completed','cancelled','no_show')")
    var status: AppointmentStatus = AppointmentStatus.SCHEDULED,

    @Column(name = "doctor_name")
    var doctorName: String? = null,

    @Column(name = "location")
    var location: String? = null,

    @Column(name = "notes", columnDefinition = "TEXT")
    var notes: String? = null,

    @Column(name = "reminder_sent")
    var reminderSent: Boolean = false,

    ) : BaseEntity() {
    @PreUpdate
    fun preUpdate() { updatedAt = LocalDateTime.now() }
}