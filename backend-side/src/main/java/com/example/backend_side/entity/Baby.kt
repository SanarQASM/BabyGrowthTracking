package com.example.backend_side.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate

@Entity
@Table(name = "babies")
data class Baby(
    @Id
    @Column(name = "baby_id", columnDefinition = "VARCHAR(36)", updatable = false, nullable = false)
    var babyId: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_user_id", nullable = false)
    var parentUser: User? = null,

    @Column(name = "full_name", nullable = false)
    var fullName: String = "",

    @Column(name = "date_of_birth", nullable = false)
    var dateOfBirth: LocalDate = LocalDate.now(),

    @Column(name = "gender", nullable = false, columnDefinition = "ENUM('boy','girl')")
    var gender: Gender = Gender.BOY,

    @Column(name = "birth_weight", precision = 5, scale = 2)
    var birthWeight: BigDecimal? = null,

    @Column(name = "birth_height", precision = 5, scale = 2)
    var birthHeight: BigDecimal? = null,

    @Column(name = "birth_head_circumference", precision = 5, scale = 2)
    var birthHeadCircumference: BigDecimal? = null,

    @Column(name = "photo_url", columnDefinition = "TEXT")
    var photoUrl: String? = null,

    @Column(name = "is_active")
    var isActive: Boolean = true,

    @OneToOne(mappedBy = "baby", cascade = [CascadeType.ALL], orphanRemoval = true)
    var familyHistory: FamilyHistory? = null,

    @OneToMany(mappedBy = "baby", cascade = [CascadeType.ALL], orphanRemoval = true)
    var childIllnesses: MutableList<ChildIllnesses> = mutableListOf(),

    @OneToMany(mappedBy = "baby", cascade = [CascadeType.ALL], orphanRemoval = true)
    var preCheckInvestigations: MutableList<PreCheckInvestigation> = mutableListOf(),

    @OneToMany(mappedBy = "baby", cascade = [CascadeType.ALL], orphanRemoval = true)
    var memories: MutableList<Memory> = mutableListOf(),

    @OneToMany(mappedBy = "baby", cascade = [CascadeType.ALL], orphanRemoval = true)
    var growthRecords: MutableList<GrowthRecord> = mutableListOf(),

    @OneToMany(mappedBy = "baby", cascade = [CascadeType.ALL], orphanRemoval = true)
    var healthIssues: MutableList<HealthIssue> = mutableListOf(),

    @OneToMany(mappedBy = "baby", cascade = [CascadeType.ALL], orphanRemoval = true)
    var vaccinations: MutableList<Vaccination> = mutableListOf(),

    @OneToMany(mappedBy = "baby", cascade = [CascadeType.ALL], orphanRemoval = true)
    var appointments: MutableList<Appointment> = mutableListOf(),

    @OneToMany(mappedBy = "baby", cascade = [CascadeType.ALL], orphanRemoval = true)
    var notifications: MutableList<Notification> = mutableListOf()

) : BaseEntity() {
    fun getAgeInMonths(): Int =
        java.time.Period.between(dateOfBirth, LocalDate.now()).toTotalMonths().toInt()

    fun getAgeInDays(): Long =
        java.time.temporal.ChronoUnit.DAYS.between(dateOfBirth, LocalDate.now())
}