package com.example.backend_side.repositories

import com.example.backend_side.entity.AssignmentStatus
import com.example.backend_side.entity.VaccinationTeamAssignment
import com.example.backend_side.entity.VisitOutcome
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface VaccinationTeamAssignmentRepository : JpaRepository<VaccinationTeamAssignment, String> {

    fun findByTeamMember_UserId(teamMemberId: String): List<VaccinationTeamAssignment>

    fun findByBaby_BabyId(babyId: String): List<VaccinationTeamAssignment>

    fun findByStatus(status: AssignmentStatus): List<VaccinationTeamAssignment>

    fun findByTeamMember_UserIdAndStatus(teamMemberId: String, status: AssignmentStatus): List<VaccinationTeamAssignment>

    fun findByOutcome(outcome: VisitOutcome): List<VaccinationTeamAssignment>

    fun findByPlannedVisitDateBetween(startDate: LocalDate, endDate: LocalDate): List<VaccinationTeamAssignment>

    fun findByActualVisitDateBetween(startDate: LocalDate, endDate: LocalDate): List<VaccinationTeamAssignment>

    @Query("SELECT vta FROM VaccinationTeamAssignment vta WHERE vta.teamMember.userId = :teamMemberId AND vta.plannedVisitDate BETWEEN :startDate AND :endDate")
    fun findTeamMemberAssignmentsInDateRange(
        @Param("teamMemberId") teamMemberId: String,
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate
    ): List<VaccinationTeamAssignment>

    @Query("SELECT COUNT(vta) FROM VaccinationTeamAssignment vta WHERE vta.teamMember.userId = :teamMemberId AND vta.status = :status")
    fun countByTeamMemberIdAndStatus(@Param("teamMemberId") teamMemberId: String, @Param("status") status: AssignmentStatus): Long

    @Query("SELECT vta FROM VaccinationTeamAssignment vta WHERE vta.status IN ('ASSIGNED', 'IN_PROGRESS') ORDER BY vta.plannedVisitDate ASC")
    fun findActiveAssignments(): List<VaccinationTeamAssignment>
}