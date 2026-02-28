package com.example.backend_side.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "memory_images")
data class MemoryImage(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    var imageId: Int? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memory_id", nullable = false)
    var memory: Memory? = null,

    @Column(name = "image_url", nullable = false, columnDefinition = "TEXT")
    var imageUrl: String = "",

    @Column(name = "caption", length = 500)
    var caption: String? = null,

    @Column(name = "sort_order", nullable = false)
    var sortOrder: Int = 0,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
)