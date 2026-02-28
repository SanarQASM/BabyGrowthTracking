package com.example.backend_side.entity

import jakarta.persistence.*

@Entity
@Table(name = "users")
data class User(
    @Id
    @Column(name = "user_id", columnDefinition = "VARCHAR(36)", updatable = false, nullable = false)
    var userId: String = "",

    @Column(name = "full_name", nullable = false)
    var fullName: String = "",

    @Column(name = "email", nullable = false, unique = true)
    var email: String = "",

    @Column(name = "password")
    var password: String? = null,

    @Column(name = "phone", length = 50)
    var phone: String? = null,

    @Column(name = "city", length = 100)
    var city: String? = null,

    @Column(name = "address", columnDefinition = "TEXT")
    var address: String? = null,

    @Column(name = "profile_image_url", columnDefinition = "TEXT")
    var profileImageUrl: String? = null,

    // ✅ No import needed — UserRoleConverter is in the same package now
    // ✅ autoApply = true means @Convert annotation is optional here too
    @Column(name = "role", nullable = false, columnDefinition = "ENUM('parent','admin','vaccination_team')")
    var role: UserRole = UserRole.PARENT,

    @Column(name = "is_active")
    var isActive: Boolean = true,

    @OneToMany(mappedBy = "parentUser", cascade = [CascadeType.ALL], orphanRemoval = true)
    var babies: MutableList<Baby> = mutableListOf(),

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    var firebaseAuths: MutableList<FirebaseAuth> = mutableListOf(),

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    var notifications: MutableList<Notification> = mutableListOf()

) : BaseEntity()