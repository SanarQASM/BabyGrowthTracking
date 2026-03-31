package com.example.backend_side.entity

import jakarta.persistence.*
import java.time.LocalDateTime

enum class GuideType { SLEEP, FEEDING }
enum class VoteType  { USEFUL, USELESS }

@Entity
@Table(
    name = "guide_feedback",
    uniqueConstraints = [UniqueConstraint(
        name       = "uq_user_content_guide",
        columnNames = ["user_id", "content_id", "guide_type"]
    )]
)
class GuideFeedback(

    @Id
    @Column(name = "feedback_id", length = 36, nullable = false)
    val feedbackId: String,

    /** FK → users.user_id */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name                 = "user_id",
        nullable             = false,
        foreignKey           = ForeignKey(name = "fk_guide_feedback_user")
    )
    val user: User,

    /**
     * Unique identifier of the guide card/item taken straight from the JSON
     * content file (e.g. "ss_79_1", "lul_ku_1", "mf_bf_01_1").
     */
    @Column(name = "content_id", length = 100, nullable = false)
    val contentId: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "guide_type", nullable = false)
    val guideType: GuideType,

    @Enumerated(EnumType.STRING)
    @Column(name = "vote", nullable = false)
    var vote: VoteType,

    @Column(name = "voted_at", nullable = false)
    var votedAt: LocalDateTime = LocalDateTime.now()
)