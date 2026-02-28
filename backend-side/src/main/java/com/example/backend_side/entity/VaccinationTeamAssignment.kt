package com.example.backend_side.entity

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "vaccination_team_assignments")
data class VaccinationTeamAssignment(
    @Id
    @Column(name = "assignment_id", columnDefinition = "VARCHAR(36)", updatable = false, nullable = false)
    var assignmentId: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_member_id", nullable = false)
    var teamMember: User? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "baby_id", nullable = false)
    var baby: Baby? = null,

    @Column(name = "assigned_date", nullable = false)
    var assignedDate: LocalDate = LocalDate.now(),

    @Column(name = "status", columnDefinition = "ENUM('assigned','in_progress','completed','cancelled')")
    var status: AssignmentStatus = AssignmentStatus.ASSIGNED,

    @Column(name = "planned_visit_date")
    var plannedVisitDate: LocalDate? = null,

    @Column(name = "actual_visit_date")
    var actualVisitDate: LocalDate? = null,

    @Column(name = "outcome", columnDefinition = "ENUM('vaccinated','not_home','refused','rescheduled')")
    var outcome: VisitOutcome? = null,

    @Column(name = "notes", columnDefinition = "TEXT")
    var notes: String? = null,

    ) : BaseEntity() {
    @PreUpdate
    fun preUpdate() { updatedAt = LocalDateTime.now() }
}