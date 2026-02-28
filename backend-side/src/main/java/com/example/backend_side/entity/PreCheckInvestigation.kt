package com.example.backend_side.entity

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "pre_check_investigation")
data class PreCheckInvestigation(
    @Id
    @Column(name = "investigation_id", columnDefinition = "VARCHAR(36)", updatable = false, nullable = false)
    var investigationId: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "baby_id", nullable = false)
    var baby: Baby? = null,

    @Column(name = "check_date", nullable = false)
    var checkDate: LocalDate = LocalDate.now(),

    @Column(name = "jaundice", columnDefinition = "ENUM('yes','no','not_known')")
    var jaundice: InvestigationStatus = InvestigationStatus.NOT_KNOWN,

    @Column(name = "shortness_of_breath", columnDefinition = "ENUM('yes','no','not_known')")
    var shortnessOfBreath: InvestigationStatus = InvestigationStatus.NOT_KNOWN,

    @Column(name = "turning_blue", columnDefinition = "ENUM('yes','no','not_known')")
    var turningBlue: InvestigationStatus = InvestigationStatus.NOT_KNOWN,

    @Column(name = "marble_heart", columnDefinition = "ENUM('yes','no','not_known')")
    var marbleHeart: InvestigationStatus = InvestigationStatus.NOT_KNOWN,

    @Column(name = "inflammation_of_liver", columnDefinition = "ENUM('yes','no','not_known')")
    var inflammationOfLiver: InvestigationStatus = InvestigationStatus.NOT_KNOWN,

    @Column(name = "inflammation_of_spleen", columnDefinition = "ENUM('yes','no','not_known')")
    var inflammationOfSpleen: InvestigationStatus = InvestigationStatus.NOT_KNOWN,

    @Column(name = "hernia", columnDefinition = "ENUM('yes','no','not_known')")
    var hernia: InvestigationStatus = InvestigationStatus.NOT_KNOWN,

    @Column(name = "hydrocele_of_ear", columnDefinition = "ENUM('yes','no','not_known')")
    var hydroceleOfEar: InvestigationStatus = InvestigationStatus.NOT_KNOWN,

    @Column(name = "hip_joint_dislocation", columnDefinition = "ENUM('yes','no','not_known')")
    var hipJointDislocation: InvestigationStatus = InvestigationStatus.NOT_KNOWN,

    @Column(name = "muscles_normal", columnDefinition = "ENUM('yes','no','not_known')")
    var musclesNormal: InvestigationStatus = InvestigationStatus.NOT_KNOWN,

    @Column(name = "reactions_normal", columnDefinition = "ENUM('yes','no','not_known')")
    var reactionsNormal: InvestigationStatus = InvestigationStatus.NOT_KNOWN,

    @Column(name = "nucleus_normal", columnDefinition = "ENUM('yes','no','not_known')")
    var nucleusNormal: InvestigationStatus = InvestigationStatus.NOT_KNOWN,

    @Column(name = "genitals_normal", columnDefinition = "ENUM('yes','no','not_known')")
    var genitalsNormal: InvestigationStatus = InvestigationStatus.NOT_KNOWN,

    @Column(name = "eye_normal", columnDefinition = "ENUM('yes','no','not_known')")
    var eyeNormal: InvestigationStatus = InvestigationStatus.NOT_KNOWN,

    @Column(name = "red_reflex", columnDefinition = "ENUM('yes','no','not_known')")
    var redReflex: InvestigationStatus = InvestigationStatus.NOT_KNOWN,

    @Column(name = "reaction_to_sound", columnDefinition = "ENUM('yes','no','not_known')")
    var reactionToSound: InvestigationStatus = InvestigationStatus.NOT_KNOWN,

    @Column(name = "others", columnDefinition = "TEXT")
    var others: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conducted_by")
    var conductedBy: User? = null,

    ) : BaseEntity() {
    @PreUpdate
    fun preUpdate() { updatedAt = LocalDateTime.now() }
}