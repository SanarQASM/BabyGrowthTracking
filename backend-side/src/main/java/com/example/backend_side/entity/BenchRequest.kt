package com.example.backend_side.entity

import jakarta.persistence.*
import java.time.LocalDateTime

// ─────────────────────────────────────────────────────────────────────────────
// BenchRequest — parent sends a request to join a bench (team vaccination).
// The team vaccination (bench) can accept or reject it with a reason.
// One active pending/accepted request per baby at any time.
// ─────────────────────────────────────────────────────────────────────────────

@Entity
@Table(
    name = "bench_requests",
    indexes = [
        Index(name = "idx_br_baby",   columnList = "baby_id"),
        Index(name = "idx_br_bench",  columnList = "bench_id"),
        Index(name = "idx_br_status", columnList = "status")
    ]
)
data class BenchRequest(

    @Id
    @Column(name = "request_id", columnDefinition = "VARCHAR(36)", updatable = false, nullable = false)
    var requestId: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "baby_id", nullable = false)
    var baby: Baby? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bench_id", nullable = false)
    var bench: VaccinationBench? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by", nullable = false)
    var requestedBy: User? = null,

    @Column(
        name   = "status",
        nullable = false,
        columnDefinition = "ENUM('pending','accepted','rejected','cancelled')"
    )
    var status: BenchRequestStatus = BenchRequestStatus.PENDING,

    @Column(name = "reject_reason", columnDefinition = "TEXT")
    var rejectReason: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    var reviewedBy: User? = null,

    @Column(name = "reviewed_at")
    var reviewedAt: LocalDateTime? = null,

    @Column(name = "notes", columnDefinition = "TEXT")
    var notes: String? = null

) : BaseEntity()

enum class BenchRequestStatus(override val dbValue: String) : HasDbValue {
    PENDING("pending"),
    ACCEPTED("accepted"),
    REJECTED("rejected"),
    CANCELLED("cancelled");
    override fun toString() = dbValue
}

@jakarta.persistence.Converter(autoApply = true)
class BenchRequestStatusConverter :
    LowercaseEnumConverter<BenchRequestStatus>(BenchRequestStatus::class.java)