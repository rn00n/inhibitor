package com.rn00n.inhibitor.infrastructure.security.authentication.grant.registry

import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.web.authentication.AuthenticationConverter

data class AuthenticationGrantBinding(
    val converter: AuthenticationConverter,
    val provider: AuthenticationProvider
)