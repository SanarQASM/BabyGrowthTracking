package com.example.backend_side

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Date

@Component
class JwtUtil(
    @Value("\${jwt.secret}") private val secret: String,
    @Value("\${jwt.expiration}") private val expiration: Long
) {
    private val key by lazy { Keys.hmacShaKeyFor(secret.toByteArray()) }

    fun generateToken(email: String, role: String): String = Jwts.builder()
        .subject(email)
        .claim("role", role)
        .issuedAt(Date())
        .expiration(Date(System.currentTimeMillis() + expiration))
        .signWith(key)
        .compact()

    fun extractEmail(token: String): String = getClaims(token).subject

    fun extractRole(token: String): String = getClaims(token)["role"] as String

    fun isTokenValid(token: String): Boolean = try {
        getClaims(token).expiration.after(Date())
    } catch (e: Exception) {
        false
    }

    private fun getClaims(token: String): Claims =
        Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
}