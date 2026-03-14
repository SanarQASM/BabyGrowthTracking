package com.example.backend_side.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "vaccine_types")
data class VaccineType(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vaccine_id")
    var vaccineId: Int? = null,

    // ── International/English name (always present) ────────────────────────
    @Column(name = "vaccine_name", nullable = false)
    var vaccineName: String = "",

    // ── Arabic name ────────────────────────────────────────────────────────
    @Column(name = "vaccine_name_ar")
    var vaccineNameAr: String? = null,

    // ── Kurdish Sorani name ────────────────────────────────────────────────
    @Column(name = "vaccine_name_ku")
    var vaccineNameKu: String? = null,

    // ── Kurdish Badini name ────────────────────────────────────────────────
    @Column(name = "vaccine_name_ckb")
    var vaccineNameCkb: String? = null,

    @Column(name = "recommended_age_months", nullable = false)
    var recommendedAgeMonths: Int = 0,

    @Column(name = "dose_number", columnDefinition = "TINYINT")
    var doseNumber: Byte = 1,

    // ── English description ────────────────────────────────────────────────
    @Column(name = "description", columnDefinition = "TEXT")
    var description: String? = null,

    // ── Arabic description ─────────────────────────────────────────────────
    @Column(name = "description_ar", columnDefinition = "TEXT")
    var descriptionAr: String? = null,

    // ── Kurdish Sorani description ─────────────────────────────────────────
    @Column(name = "description_ku", columnDefinition = "TEXT")
    var descriptionKu: String? = null,

    // ── Kurdish Badini description ─────────────────────────────────────────
    @Column(name = "description_ckb", columnDefinition = "TEXT")
    var descriptionCkb: String? = null,

    @Column(name = "is_mandatory")
    var isMandatory: Boolean = true,

    @OneToMany(mappedBy = "vaccineType", cascade = [CascadeType.ALL], orphanRemoval = true)
    var vaccinations: MutableList<Vaccination> = mutableListOf()

) : BaseEntity() {
    @PreUpdate
    fun preUpdate() { updatedAt = LocalDateTime.now() }
}