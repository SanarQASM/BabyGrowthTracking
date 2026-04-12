package com.example.backend_side.repositories

import com.example.backend_side.entity.HealthIssue
import com.example.backend_side.entity.Severity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface HealthIssueRepository : JpaRepository<HealthIssue, String> {

    fun findByBaby_BabyId(babyId: String): List<HealthIssue>

    fun findByBaby_BabyIdAndIsResolved(babyId: String, isResolved: Boolean): List<HealthIssue>

    fun findByBaby_BabyIdOrderByIssueDateDesc(babyId: String): List<HealthIssue>

    fun findByIsResolved(isResolved: Boolean): List<HealthIssue>

    fun findBySeverity(severity: Severity): List<HealthIssue>

    fun findByIssueDateBetween(startDate: LocalDate, endDate: LocalDate): List<HealthIssue>

    fun findByCreatedBy_UserId(createdById: String): List<HealthIssue>

    @Query("""
        SELECT hi FROM HealthIssue hi
        WHERE hi.baby.babyId = :babyId
        AND hi.isResolved = false
        AND hi.severity = :severity
    """)
    fun findUnresolvedBySeverity(
        @Param("babyId")   babyId  : String,
        @Param("severity") severity: Severity
    ): List<HealthIssue>

    @Query("SELECT COUNT(hi) FROM HealthIssue hi WHERE hi.baby.babyId = :babyId AND hi.isResolved = false")
    fun countUnresolvedIssuesByBaby(@Param("babyId") babyId: String): Long

    @Query("""
        SELECT hi FROM HealthIssue hi
        WHERE LOWER(hi.title) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
        OR LOWER(hi.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
    """)
    fun searchHealthIssues(@Param("searchTerm") searchTerm: String): List<HealthIssue>

    // NEW: replaces non-existent findByIsResolvedAndIssueDateBefore()
    // used by PushNotificationScheduler.checkHealthIssueFollowUps()
    @Query("""
        SELECT hi FROM HealthIssue hi
        WHERE hi.isResolved = :isResolved
        AND hi.issueDate < :before
        ORDER BY hi.issueDate ASC
    """)
    fun findOngoingBefore(
        @Param("isResolved") isResolved: Boolean,
        @Param("before")     before    : LocalDate
    ): List<HealthIssue>
}