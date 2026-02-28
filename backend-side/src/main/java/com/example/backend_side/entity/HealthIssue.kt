package com.example.backend_side.entity

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "health_issues")
data class HealthIssue(
    @Id
    @Column(name = "issue_id", columnDefinition = "VARCHAR(36)", updatable = false, nullable = false)
    var issueId: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "baby_id", nullable = false)
    var baby: Baby? = null,

    @Column(name = "issue_date", nullable = false)
    var issueDate: LocalDate = LocalDate.now(),

    @Column(name = "title", nullable = false)
    var title: String = "",

    @Column(name = "description", columnDefinition = "TEXT")
    var description: String? = null,

    @Column(name = "severity", columnDefinition = "ENUM('mild','moderate','severe')")
    var severity: Severity? = null,

    @Column(name = "is_resolved")
    var isResolved: Boolean = false,

    @Column(name = "resolution_date")
    var resolutionDate: LocalDate? = null,

    @Column(name = "resolved_notes", columnDefinition = "TEXT")
    var resolvedNotes: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    var createdBy: User? = null,

    ) : BaseEntity() {
    @PreUpdate
    fun preUpdate() { updatedAt = LocalDateTime.now() }
}