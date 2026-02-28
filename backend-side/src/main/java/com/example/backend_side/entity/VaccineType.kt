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

    @Column(name = "vaccine_name", nullable = false)
    var vaccineName: String = "",

    @Column(name = "recommended_age_months", nullable = false)
    var recommendedAgeMonths: Int = 0,

    // ✅ TINYINT in SQL → Byte in Kotlin
    @Column(name = "dose_number", columnDefinition = "TINYINT")
    var doseNumber: Byte = 1,

    @Column(name = "description", columnDefinition = "TEXT")
    var description: String? = null,

    @Column(name = "is_mandatory")
    var isMandatory: Boolean = true,

    @OneToMany(mappedBy = "vaccineType", cascade = [CascadeType.ALL], orphanRemoval = true)
    var vaccinations: MutableList<Vaccination> = mutableListOf()

) : BaseEntity() {
    @PreUpdate
    fun preUpdate() { updatedAt = LocalDateTime.now() }
}