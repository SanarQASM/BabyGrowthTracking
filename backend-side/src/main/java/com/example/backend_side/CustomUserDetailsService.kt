package com.example.backend_side

import com.example.backend_side.repositories.UserRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository
) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        val user = userRepository.findByEmail(username)
            .orElseThrow { UsernameNotFoundException("User not found with email: $username") }

        // password is nullable — social-login users may not have one
        val password = user.password ?: ""

        return User(
            user.email,
            password,
            listOf(SimpleGrantedAuthority("ROLE_${user.role.name}"))
        )
    }
}