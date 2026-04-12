package com.example.backend_side.repositories

import com.example.backend_side.entity.Appointment
import com.example.backend_side.entity.AppointmentStatus
import com.example.backend_side.entity.AppointmentType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface AppointmentRepository : JpaRepository<Appointment, String> {

    fun findByBaby_BabyId(babyId: String): List<Appointment>

    fun findByBaby_BabyIdOrderByScheduledDateAsc(babyId: String): List<Appointment>

    fun findByStatus(status: AppointmentStatus): List<Appointment>

    fun findByAppointmentType(appointmentType: AppointmentType): List<Appointment>

    fun findByBaby_BabyIdAndStatus(babyId: String, status: AppointmentStatus): List<Appointment>

    fun findByScheduledDateBetween(startDate: LocalDate, endDate: LocalDate): List<Appointment>

    // FIX: replaced raw string literals 'SCHEDULED','CONFIRMED' with typed :statuses param
    @Query("""
        SELECT a FROM Appointment a
        WHERE a.scheduledDate >= :startDate
        AND a.status IN :statuses
        ORDER BY a.scheduledDate, a.scheduledTime
    """)
    fun findUpcomingAppointments(
        @Param("startDate") startDate: LocalDate,
        @Param("statuses")  statuses : List<AppointmentStatus> = listOf(
            AppointmentStatus.SCHEDULED, AppointmentStatus.CONFIRMED
        )
    ): List<Appointment>

    // FIX: replaced raw string literals with typed :statuses param
    @Query("""
        SELECT a FROM Appointment a
        WHERE a.baby.babyId = :babyId
        AND a.scheduledDate >= CURRENT_DATE
        AND a.status IN :statuses
        ORDER BY a.scheduledDate, a.scheduledTime
    """)
    fun findUpcomingAppointmentsForBaby(
        @Param("babyId")   babyId  : String,
        @Param("statuses") statuses: List<AppointmentStatus> = listOf(
            AppointmentStatus.SCHEDULED, AppointmentStatus.CONFIRMED
        )
    ): List<Appointment>

    // FIX: replaced raw string literals with typed :statuses param
    @Query("""
        SELECT a FROM Appointment a
        WHERE a.scheduledDate = :date
        AND a.reminderSent = false
        AND a.status IN :statuses
    """)
    fun findAppointmentsNeedingReminder(
        @Param("date")     date    : LocalDate,
        @Param("statuses") statuses: List<AppointmentStatus> = listOf(
            AppointmentStatus.SCHEDULED, AppointmentStatus.CONFIRMED
        )
    ): List<Appointment>

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.baby.babyId = :babyId AND a.status = :status")
    fun countByBabyIdAndStatus(
        @Param("babyId") babyId: String,
        @Param("status") status: AppointmentStatus
    ): Long
}