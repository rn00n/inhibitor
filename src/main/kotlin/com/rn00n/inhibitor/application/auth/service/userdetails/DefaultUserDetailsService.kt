package com.rn00n.inhibitor.application.auth.service.userdetails

import com.rn00n.inhibitor.application.auth.model.principal.UserPrincipal
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class DefaultUserDetailsService(
    private val userPrincipalRepository: UserPrincipalRepository,
) : ExtendedUserDetailsService {

    private val logger = KotlinLogging.logger {}

    override fun loadUserByUsername(username: String): UserDetails {
        val user = userPrincipalRepository.findByUsername(username)
            ?: throw UsernameNotFoundException("User not found")

        return UserPrincipal.Companion.fromAccount(user)
    }

    override fun loadUserById(id: Long): UserDetails {
        val user = userPrincipalRepository.findById(id)
            .orElseThrow { UsernameNotFoundException("User not found") }

        return UserPrincipal.Companion.fromAccount(user)
    }
}