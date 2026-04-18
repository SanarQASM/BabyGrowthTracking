package com.example.backend_side

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthFilter(
    private val jwtUtil            : JwtUtil,
    private val userDetailsService : UserDetailsService
) : OncePerRequestFilter() {

    // ─── Public paths — filter skips these completely ─────────────────────────
    // These paths must match your SecurityConfig permitAll() list exactly.
    // Any request whose path STARTS WITH one of these strings is passed through
    // without any token validation — so /api/v1/auth/register, /api/v1/auth/login,
    // /api/v1/auth/send-verification, /api/v1/auth/verify-account all bypass the
    // filter entirely. This is what was missing and caused the crash on signup:
    // the filter tried to load a user that didn't exist yet.
    private val publicPaths = listOf(
        "/api/v1/auth/",
        "/api/actuator/",
        "/api/api-docs",
        "/api/swagger-ui",
        "/api/v3/api-docs",
        "/api/v1/admin-setup/"
    )

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val path = request.requestURI
        // Return true = skip this filter for the given path
        return publicPaths.any { path.startsWith(it) }
    }

    override fun doFilterInternal(
        request     : HttpServletRequest,
        response    : HttpServletResponse,
        filterChain : FilterChain
    ) {
        val authHeader = request.getHeader("Authorization")

        // No token — pass through, Spring Security will handle 401 if needed
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }

        val token = authHeader.substring(7)

        // Invalid or expired token — pass through without crashing
        if (!jwtUtil.isTokenValid(token)) {
            filterChain.doFilter(request, response)
            return
        }

        // Extract email from token
        val email = try {
            jwtUtil.extractEmail(token)
        } catch (e: Exception) {
            // Malformed token — skip silently
            filterChain.doFilter(request, response)
            return
        }

        // Only set authentication if not already set
        if (SecurityContextHolder.getContext().authentication == null) {
            val userDetails = try {
                userDetailsService.loadUserByUsername(email)
            } catch (e: UsernameNotFoundException) {
                // Token references a user that no longer exists in DB
                // (e.g. deleted account, or old token from before registration)
                // Skip silently instead of crashing with 500
                filterChain.doFilter(request, response)
                return
            }

            val authToken = UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.authorities
            )
            authToken.details = WebAuthenticationDetailsSource().buildDetails(request)
            SecurityContextHolder.getContext().authentication = authToken
        }

        filterChain.doFilter(request, response)
    }
}