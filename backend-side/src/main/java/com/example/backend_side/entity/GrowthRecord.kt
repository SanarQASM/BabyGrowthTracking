package com.example.backend_side.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "growth_records")
data class GrowthRecord(
    @Id
    @Column(name = "record_id", columnDefinition = "VARCHAR(36)", updatable = false, nullable = false)
    var recordId: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "baby_id", nullable = false)
    var baby: Baby? = null,

    @Column(name = "measurement_date", nullable = false)
    var measurementDate: LocalDate = LocalDate.now(),

    @Column(name = "age_in_months", nullable = false)
    var ageInMonths: Int = 0,

    @Column(name = "age_in_days")
    var ageInDays: Int? = null,

    @Column(name = "weight", precision = 5, scale = 2)
    var weight: BigDecimal? = null,

    @Column(name = "height", precision = 5, scale = 2)
    var height: BigDecimal? = null,

    @Column(name = "head_circumference", precision = 5, scale = 2)
    var headCircumference: BigDecimal? = null,

    // ✅ TINYINT in SQL → Byte? in Kotlin
    @Column(name = "weight_percentile", columnDefinition = "TINYINT")
    var weightPercentile: Byte? = null,

    @Column(name = "height_percentile", columnDefinition = "TINYINT")
    var heightPercentile: Byte? = null,

    @Column(name = "head_circumference_percentile", columnDefinition = "TINYINT")
    var headCircumferencePercentile: Byte? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "measured_by")
    var measuredBy: User? = null,

    ) : BaseEntity() {
    @PreUpdate
    fun preUpdate() { updatedAt = LocalDateTime.now() }
}