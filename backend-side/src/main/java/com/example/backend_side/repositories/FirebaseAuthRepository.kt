package com.example.backend_side.repositories

import com.example.backend_side.entity.AuthProvider
import com.example.backend_side.entity.FirebaseAuth
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface FirebaseAuthRepository : JpaRepository<FirebaseAuth, Int> {

    fun findByProviderId(providerId: String): Optional<FirebaseAuth>

    fun findByProviderAndProviderId(provider: AuthProvider, providerId: String): Optional<FirebaseAuth>

    fun findByUser_UserId(userId: String): List<FirebaseAuth>

    fun findByProviderEmail(providerEmail: String): Optional<FirebaseAuth>

    fun existsByProviderAndProviderId(provider: AuthProvider, providerId: String): Boolean
}