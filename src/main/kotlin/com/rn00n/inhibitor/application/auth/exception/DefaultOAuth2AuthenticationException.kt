package com.rn00n.inhibitor.application.auth.exception

import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.OAuth2Error

class DefaultOAuth2AuthenticationException @JvmOverloads constructor(
    error: ErrorCode,
    description: String = "",
    override val cause: Throwable? = null
) : OAuth2AuthenticationException(
    OAuth2Error(error.name, description, null), description, cause
)