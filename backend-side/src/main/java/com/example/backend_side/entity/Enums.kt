package com.example.backend_side.entity

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

// ── Generic base converter ────────────────────────────────────────────────────

abstract class LowercaseEnumConverter<E>(
    private val enumClass: Class<E>
) : AttributeConverter<E, String> where E : Enum<E>, E : HasDbValue {

    override fun convertToDatabaseColumn(attribute: E?): String =
        attribute?.dbValue ?: ""

    override fun convertToEntityAttribute(dbData: String?): E =
        enumClass.enumConstants
            .first { it.dbValue == dbData }
}

interface HasDbValue { val dbValue: String }

// ── Existing enums (unchanged) ────────────────────────────────────────────────

enum class Gender(override val dbValue: String) : HasDbValue {
    BOY("boy"), GIRL("girl");
    override fun toString() = dbValue
}

enum class UserRole(override val dbValue: String) : HasDbValue {
    PARENT("parent"), ADMIN("admin"), VACCINATION_TEAM("vaccination_team");
    override fun toString() = dbValue
}

enum class VaccinationStatus(override val dbValue: String) : HasDbValue {
    SCHEDULED("scheduled"), COMPLETED("completed"),
    MISSED("missed"), RESCHEDULED("rescheduled");
    override fun toString() = dbValue
}

enum class AppointmentType(override val dbValue: String) : HasDbValue {
    CHECKUP("checkup"), VACCINATION("vaccination"), CONSULTATION("consultation"),
    EMERGENCY("emergency"), FOLLOW_UP("follow_up");
    override fun toString() = dbValue
}

enum class AppointmentStatus(override val dbValue: String) : HasDbValue {
    SCHEDULED("scheduled"), CONFIRMED("confirmed"), COMPLETED("completed"),
    CANCELLED("cancelled"), NO_SHOW("no_show");
    override fun toString() = dbValue
}

enum class NotificationType(override val dbValue: String) : HasDbValue {
    VACCINATION_REMINDER("vaccination_reminder"), GROWTH_ALERT("growth_alert"),
    APPOINTMENT_REMINDER("appointment_reminder"), MILESTONE("milestone"),
    HEALTH_ALERT("health_alert"), GENERAL("general");
    override fun toString() = dbValue
}

enum class NotificationPriority(override val dbValue: String) : HasDbValue {
    LOW("low"), MEDIUM("medium"), HIGH("high"), URGENT("urgent");
    override fun toString() = dbValue
}

enum class Severity(override val dbValue: String) : HasDbValue {
    MILD("mild"), MODERATE("moderate"), SEVERE("severe");
    override fun toString() = dbValue
}

enum class AuthProvider(override val dbValue: String) : HasDbValue {
    GOOGLE("google"), EMAIL("email");
    override fun toString() = dbValue
}

enum class AssignmentStatus(override val dbValue: String) : HasDbValue {
    ASSIGNED("assigned"), IN_PROGRESS("in_progress"),
    COMPLETED("completed"), CANCELLED("cancelled");
    override fun toString() = dbValue
}

enum class VisitOutcome(override val dbValue: String) : HasDbValue {
    VACCINATED("vaccinated"), NOT_HOME("not_home"),
    REFUSED("refused"), RESCHEDULED("rescheduled");
    override fun toString() = dbValue
}

enum class InvestigationStatus(override val dbValue: String) : HasDbValue {
    YES("yes"), NO("no"), NOT_KNOWN("not_known");
    override fun toString() = dbValue
}

// ── NEW enums for Bench + Schedule feature ────────────────────────────────────

enum class BenchType(override val dbValue: String) : HasDbValue {
    PRIMARY_HEALTH_CENTER("primary_health_center"),
    HOSPITAL("hospital"),
    MOBILE_UNIT("mobile_unit"),
    COMMUNITY_CENTER("community_center"),
    CLINIC("clinic");
    override fun toString() = dbValue
}

enum class ScheduleStatus(override val dbValue: String) : HasDbValue {
    UPCOMING("upcoming"),
    DUE_SOON("due_soon"),
    OVERDUE("overdue"),
    COMPLETED("completed"),
    MISSED("missed"),
    RESCHEDULED("rescheduled");
    override fun toString() = dbValue
}

enum class ShiftReason(override val dbValue: String) : HasDbValue {
    NONE("none"),
    WEEKEND("weekend"),
    HOLIDAY("holiday"),
    BENCH_CLOSED("bench_closed"),
    MISSED("missed"),
    RESCHEDULED("rescheduled");
    override fun toString() = dbValue
}

enum class AdjustmentReason(override val dbValue: String) : HasDbValue {
    HOLIDAY("holiday"),
    BENCH_CLOSED("bench_closed"),
    PARENT_MISSED("parent_missed"),
    PARENT_RESCHEDULED("parent_rescheduled"),
    TEAM_RESCHEDULED("team_rescheduled"),
    BENCH_CHANGED("bench_changed");
    override fun toString() = dbValue
}

// ── All converters ─────────────────────────────────────────────────────────────

@Converter(autoApply = true)
class GenderConverter : LowercaseEnumConverter<Gender>(Gender::class.java)

@Converter(autoApply = true)
class UserRoleConverter : LowercaseEnumConverter<UserRole>(UserRole::class.java)

@Converter(autoApply = true)
class VaccinationStatusConverter : LowercaseEnumConverter<VaccinationStatus>(VaccinationStatus::class.java)

@Converter(autoApply = true)
class AppointmentTypeConverter : LowercaseEnumConverter<AppointmentType>(AppointmentType::class.java)

@Converter(autoApply = true)
class AppointmentStatusConverter : LowercaseEnumConverter<AppointmentStatus>(AppointmentStatus::class.java)

@Converter(autoApply = true)
class NotificationTypeConverter : LowercaseEnumConverter<NotificationType>(NotificationType::class.java)

@Converter(autoApply = true)
class NotificationPriorityConverter : LowercaseEnumConverter<NotificationPriority>(NotificationPriority::class.java)

@Converter(autoApply = true)
class SeverityConverter : LowercaseEnumConverter<Severity>(Severity::class.java)

@Converter(autoApply = true)
class AuthProviderConverter : LowercaseEnumConverter<AuthProvider>(AuthProvider::class.java)

@Converter(autoApply = true)
class AssignmentStatusConverter : LowercaseEnumConverter<AssignmentStatus>(AssignmentStatus::class.java)

@Converter(autoApply = true)
class VisitOutcomeConverter : LowercaseEnumConverter<VisitOutcome>(VisitOutcome::class.java)

@Converter(autoApply = true)
class InvestigationStatusConverter : LowercaseEnumConverter<InvestigationStatus>(InvestigationStatus::class.java)

@Converter(autoApply = true)
class BenchTypeConverter : LowercaseEnumConverter<BenchType>(BenchType::class.java)

@Converter(autoApply = true)
class ScheduleStatusConverter : LowercaseEnumConverter<ScheduleStatus>(ScheduleStatus::class.java)

@Converter(autoApply = true)
class ShiftReasonConverter : LowercaseEnumConverter<ShiftReason>(ShiftReason::class.java)

@Converter(autoApply = true)
class AdjustmentReasonConverter : LowercaseEnumConverter<AdjustmentReason>(AdjustmentReason::class.java)