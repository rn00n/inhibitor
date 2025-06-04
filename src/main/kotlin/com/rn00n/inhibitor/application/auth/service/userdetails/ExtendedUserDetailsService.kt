package com.rn00n.inhibitor.application.auth.service.userdetails

import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException

interface ExtendedUserDetailsService : UserDetailsService {
    @Throws(UsernameNotFoundException::class)
    fun loadUserById(id: Long): UserDetails
}