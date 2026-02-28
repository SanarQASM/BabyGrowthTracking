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

// ── Update all enums to implement HasDbValue ──────────────────────────────────

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

// ── Converters (autoApply = true means no @Convert needed on entity fields) ──

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