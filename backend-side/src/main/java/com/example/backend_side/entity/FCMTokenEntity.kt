package com.example.backend_side.entity

import jakarta.persistence.*

@Entity
@Table(name = "fcm_tokens",
    uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "platform"])])
data class FcmToken(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id       : Long?   = null,

    @Column(name = "user_id", nullable = false)
    val userId   : String,

    @Column(name = "fcm_token", nullable = false, columnDefinition = "TEXT")
    var token    : String,

    @Column(name = "platform", nullable = false, length = 20)
    val platform : String,    // "android" | "ios" | "web"

    @Column(name = "is_active")
    var isActive : Boolean = true
) : BaseEntity()