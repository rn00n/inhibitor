package com.rn00n.inhibitor.application.auth.grant.registry

import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.web.authentication.AuthenticationConverter

data class AuthenticationGrantBinding(
    val converter: AuthenticationConverter,
    val provider: AuthenticationProvider
)