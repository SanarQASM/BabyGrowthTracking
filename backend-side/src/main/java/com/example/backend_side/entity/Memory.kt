package com.example.backend_side.entity

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "memories")
data class Memory(
    @Id
    @Column(name = "memory_id", columnDefinition = "VARCHAR(36)", updatable = false, nullable = false)
    var memoryId: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "baby_id", nullable = false)
    var baby: Baby? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_user_id", nullable = false)
    var parentUser: User? = null,

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

    @OneToMany(mappedBy = "memory", cascade = [CascadeType.ALL], orphanRemoval = true)
    var images: MutableList<MemoryImage> = mutableListOf()

) : BaseEntity() {
    @PreUpdate
    fun preUpdate() { updatedAt = LocalDateTime.now() }
}