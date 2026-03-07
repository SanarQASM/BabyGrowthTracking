package com.example.backend_side.entity

import jakarta.persistence.*
import java.time.LocalDate

// Location: entity/BenchHoliday.kt
//
// Maps to: bench_holidays table
// bench_id = NULL means the holiday applies to ALL benches (national holiday).
// bench_id = specific ID means only that bench is closed that day.

@Entity
@Table(
    name = "bench_holidays",
    indexes = [
        Index(name = "idx_bh_bench", columnList = "bench_id"),
        Index(name = "idx_bh_date", columnList = "holiday_date"),
        Index(name = "idx_bh_national", columnList = "is_national")
    ]
)
data class BenchHoliday(

    @Id
    @Column(name = "holiday_id", columnDefinition = "VARCHAR(36)", updatable = false, nullable = false)
    var holidayId: String = "",

    // NULL = applies to all benches (national holiday)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bench_id", nullable = true)
    var bench: VaccinationBench? = null,

    @Column(name = "holiday_date", nullable = false)
    var holidayDate: LocalDate = LocalDate.now(),

    @Column(name = "reason", nullable = false)
    var reason: String = "",

    // true = automatically applies to every bench, even if bench_id is NULL
    @Column(name = "is_national")
    var isNational: Boolean = false

) : BaseEntity()