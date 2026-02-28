package com.example.backend_side.entity

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "vaccinations")
data class Vaccination(
    @Id
    @Column(name = "vaccination_id", columnDefinition = "VARCHAR(36)", updatable = false, nullable = false)
    var vaccinationId: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "baby_id", nullable = false)
    var baby: Baby? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vaccine_id", nullable = false)
    var vaccineType: VaccineType? = null,

    @Column(name = "scheduled_date", nullable = false)
    var scheduledDate: LocalDate = LocalDate.now(),

    @Column(name = "administered_date")
    var administeredDate: LocalDate? = null,

    @Column(name = "status", columnDefinition = "ENUM('scheduled','completed','missed','rescheduled')")
    var status: VaccinationStatus = VaccinationStatus.SCHEDULED,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "administered_by")
    var administeredBy: User? = null,

    @Column(name = "certificate_url", columnDefinition = "TEXT")
    var certificateUrl: String? = null,

    @Column(name = "batch_number", length = 100)
    var batchNumber: String? = null,

    @Column(name = "location")
    var location: String? = null,

    @Column(name = "notes", columnDefinition = "TEXT")
    var notes: String? = null,

    ) : BaseEntity() {
    @PreUpdate
    fun preUpdate() { updatedAt = LocalDateTime.now() }
}