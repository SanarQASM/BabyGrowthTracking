package com.example.backend_side.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "firebase_auth")
data class FirebaseAuth(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "auth_id")
    var authId: Int? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User? = null,

    @Column(name = "provider", nullable = false, columnDefinition = "ENUM('google','email')")
    var provider: AuthProvider = AuthProvider.EMAIL,

    @Column(name = "provider_id", nullable = false)
    var providerId: String = "",

    @Column(name = "provider_email")
    var providerEmail: String? = null,

    @Column(name = "provider_display_name")
    var providerDisplayName: String? = null,

    @Column(name = "provider_photo_url", columnDefinition = "TEXT")
    var providerPhotoUrl: String? = null,

    @Column(name = "access_token", columnDefinition = "TEXT")
    var accessToken: String? = null,

    @Column(name = "refresh_token", columnDefinition = "TEXT")
    var refreshToken: String? = null,

    ) : BaseEntity() {
    @PreUpdate
    fun preUpdate() { updatedAt = LocalDateTime.now() }
}