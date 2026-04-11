package com.example.backend_side.entity

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

// ─────────────────────────────────────────────────────────────────────────────
// Memory entity — UPDATED
//
// ADDED two new columns:
//   • image_count  — how many photos the client stored locally for this memory
//   • captions_json — JSON array of caption strings, one per image
//
// These are stored on the backend purely as metadata so the client can
// reconstruct the gallery structure when switching devices.  The actual
// image bytes are NEVER sent to or stored by the backend.
// ─────────────────────────────────────────────────────────────────────────────

@Entity
@Table(name = "memories")
data class Memory(
    @Id
    @Column(name = "memory_id", columnDefinition = "VARCHAR(36)", updatable = false, nullable = false)
    var memoryId: String = "",

    // ── Relationships ─────────────────────────────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "baby_id", nullable = false)
    var baby: Baby? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_user_id", nullable = false)
    var parentUser: User? = null,

    // ── Core fields ───────────────────────────────────────────────────────────

    @Column(name = "title", nullable = false)
    var title: String = "",

    @Column(name = "description", columnDefinition = "TEXT")
    var description: String? = null,

    @Column(name = "memory_date", nullable = false)
    var memoryDate: LocalDate = LocalDate.now(),

    @Column(name = "age_in_months")
    var ageInMonths: Int? = null,

    @Column(name = "age_in_days")
    var ageInDays: Int? = null,

    // ── NEW: image metadata (bytes are stored on-device only) ─────────────────

    @Column(name = "image_count", nullable = false)
    var imageCount: Int = 0,

    @Column(name = "captions_json", columnDefinition = "TEXT")
    var captionsJson: String? = null,   // JSON array e.g. ["First smile","At the park"]

    // ── Images (existing one-to-many, kept for backward compat) ──────────────

    @OneToMany(mappedBy = "memory", cascade = [CascadeType.ALL], orphanRemoval = true)
    var images: MutableList<MemoryImage> = mutableListOf()

) : BaseEntity() {
    @PreUpdate
    fun preUpdate() { updatedAt = LocalDateTime.now() }
}