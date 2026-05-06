package com.example.backend_side.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.*

@Entity
@Table(name = "vaccination_benches")
data class VaccinationBench(

    @Id
    @Column(name = "bench_id", columnDefinition = "VARCHAR(36)", updatable = false, nullable = false)
    var benchId: String = "",

    @Column(name = "name_en", nullable = false)
    var nameEn: String = "",

    @Column(name = "name_ar", nullable = false)
    var nameAr: String = "",

    @Column(name = "governorate", nullable = false, length = 100)
    var governorate: String = "",

    @Column(name = "district", length = 100)
    var district: String = "",

    @Column(name = "address_en", columnDefinition = "TEXT")
    var addressEn: String? = null,

    @Column(name = "address_ar", columnDefinition = "TEXT")
    var addressAr: String? = null,

    @Column(name = "latitude", nullable = false)
    var latitude: Double = 0.0,

    @Column(name = "longitude", nullable = false)
    var longitude: Double = 0.0,

    @Column(name = "phone", length = 50)
    var phone: String? = null,

    @JsonIgnore
    @Column(name = "working_days", nullable = false)
    var workingDays: String = "Sunday,Monday,Tuesday,Wednesday,Thursday",

    @Column(name = "working_hours_start", length = 10)
    var workingHoursStart: String = "08:00",

    @Column(name = "working_hours_end", length = 10)
    var workingHoursEnd: String = "14:00",

    @JsonIgnore
    @Column(name = "vaccination_days", nullable = false)
    var vaccinationDays: String = "Sunday,Tuesday,Thursday",

    @Column(
        name = "type", nullable = false,
        columnDefinition = "ENUM('primary_health_center','hospital','mobile_unit','community_center','clinic')"
    )
    var type: BenchType = BenchType.PRIMARY_HEALTH_CENTER,

    @JsonIgnore
    @Column(name = "vaccines_available", columnDefinition = "TEXT")
    var vaccinesAvailable: String = "",

    @Column(name = "is_active")
    var isActive: Boolean = true,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_member_id", nullable = true)
    var teamMember: User? = null,

    @OneToMany(mappedBy = "bench", cascade = [CascadeType.ALL], orphanRemoval = true)
    var babyAssignments: MutableList<BabyBenchAssignment> = mutableListOf(),

    @OneToMany(mappedBy = "bench", cascade = [CascadeType.ALL], orphanRemoval = true)
    var holidays: MutableList<BenchHoliday> = mutableListOf()

) : BaseEntity() {

    @get:JsonProperty("workingDays")
    val workingDaysList: List<String>
        get() = workingDays.split(",").map { it.trim() }.filter { it.isNotBlank() }

    @get:JsonProperty("vaccinationDays")
    val vaccinationDaysList: List<String>
        get() = vaccinationDays.split(",").map { it.trim() }.filter { it.isNotBlank() }

    @get:JsonProperty("vaccinesAvailable")
    val vaccinesAvailableList: List<String>
        get() = vaccinesAvailable.split(",").map { it.trim() }.filter { it.isNotBlank() }
}