package com.example.backend_side.repositories

import com.example.backend_side.entity.MemoryImage
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MemoryImageRepository : JpaRepository<MemoryImage, Int> {

    fun findByMemory_MemoryId(memoryId: String): List<MemoryImage>

    fun findByMemory_MemoryIdOrderBySortOrderAsc(memoryId: String): List<MemoryImage>

    fun deleteByMemory_MemoryId(memoryId: String)
}