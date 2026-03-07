package com.example.backend_side.entity

import jakarta.persistence.*
import java.time.LocalDateTime

// Location: entity/BabyBenchAssignment.kt
//
// Maps to: baby_bench_assignments table
// One active assignment per baby at any time (enforced by unique index + is_active flag).
// When parent changes bench → old assignment is_active = false, new row inserted.

@Entity
@Table(
    name = "baby_bench_assignments",
    indexes = [
        Index(name = "idx_bba_baby", columnList = "baby_id"),
        Index(name = "idx_bba_bench", columnList = "bench_id"),
        Index(name = "idx_bba_active", columnList = "is_active")
    ]
)
data class BabyBenchAssignment(

    @Id
    @Column(name = "assignment_id", columnDefinition = "VARCHAR(36)", updatable = false, nullable = false)
    var assignmentId: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "baby_id", nullable = false)
    var baby: Baby? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bench_id", nullable = false)
    var bench: VaccinationBench? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by")
    var assignedBy: User? = null,

    @Column(name = "assigned_at", nullable = false)
    var assignedAt: LocalDateTime = LocalDateTime.now(),

    // Only one row per baby should have is_active = true
    @Column(name = "is_active")
    var isActive: Boolean = true,

    @Column(name = "notes", columnDefinition = "TEXT")
    var notes: String? = null

) : BaseEntity()