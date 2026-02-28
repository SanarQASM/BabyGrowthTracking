package com.example.backend_side.entity

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "child_illnesses")
data class ChildIllnesses(
    @Id
    @Column(name = "illness_id", columnDefinition = "VARCHAR(36)", updatable = false, nullable = false)
    var illnessId: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "baby_id", nullable = false)
    var baby: Baby? = null,

    @Column(name = "illness_name", nullable = false)
    var illnessName: String = "",

    @Column(name = "diagnosis_date")
    var diagnosisDate: LocalDate? = null,

    @Column(name = "notes", columnDefinition = "TEXT")
    var notes: String? = null,

    @Column(name = "is_active")
    var isActive: Boolean = true,

    ) : BaseEntity() {
    @PreUpdate
    fun preUpdate() { updatedAt = LocalDateTime.now() }
}