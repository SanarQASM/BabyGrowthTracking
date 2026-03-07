
package com.example.backend_side.repositories

import com.example.backend_side.entity.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
@Repository
interface ScheduleAdjustmentLogRepository : JpaRepository<ScheduleAdjustmentLog, String> {

    fun findBySchedule_ScheduleIdOrderByAdjustedAtDesc(scheduleId: String): List<ScheduleAdjustmentLog>

    fun findByBaby_BabyIdOrderByAdjustedAtDesc(babyId: String): List<ScheduleAdjustmentLog>

    fun findByReason(reason: AdjustmentReason): List<ScheduleAdjustmentLog>
}