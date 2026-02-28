package com.example.backend_side.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "family_history")
data class FamilyHistory(
    @Id
    @Column(name = "history_id", columnDefinition = "VARCHAR(36)", updatable = false, nullable = false)
    var historyId: String = "",

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "baby_id", nullable = false, unique = true)
    var baby: Baby? = null,

    @Column(name = "heredity", columnDefinition = "TEXT")
    var heredity: String? = null,

    @Column(name = "blood_diseases", columnDefinition = "TEXT")
    var bloodDiseases: String? = null,

    @Column(name = "cardiovascular_diseases", columnDefinition = "TEXT")
    var cardiovascularDiseases: String? = null,

    @Column(name = "metabolic_diseases", columnDefinition = "TEXT")
    var metabolicDiseases: String? = null,

    @Column(name = "appendicitis", columnDefinition = "TEXT")
    var appendicitis: String? = null,

    @Column(name = "tuberculosis", columnDefinition = "TEXT")
    var tuberculosis: String? = null,

    @Column(name = "parkinsonism", columnDefinition = "TEXT")
    var parkinsonism: String? = null,

    @Column(name = "allergies", columnDefinition = "TEXT")
    var allergies: String? = null,

    @Column(name = "others", columnDefinition = "TEXT")
    var others: String? = null,

    ) : BaseEntity() {
    @PreUpdate
    fun preUpdate() { updatedAt = LocalDateTime.now() }
}