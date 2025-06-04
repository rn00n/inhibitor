package com.rn00n.inhibitor.application.auth.model.token

import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationGrantAuthenticationToken

class OAuth2OriginRefreshTokenAuthenticationToken(
    private val refreshTokenValue: String,
    clientPrincipal: Authentication,
    additionalParameters: Map<String, Any>
) : OAuth2AuthorizationGrantAuthenticationToken(
    AuthorizationGrantType.REFRESH_TOKEN,
    clientPrincipal,
    additionalParameters
) {
    fun getScopes(): Set<String> {
        val scope = additionalParameters[OAuth2ParameterNames.SCOPE] ?: return emptySet()

        return when (scope) {
            is String -> scope.split(" ").filter { it.isNotBlank() }.toSet()
            is Collection<*> -> scope.filterIsInstance<String>().toSet()
            else -> emptySet()
        }
    }

    fun getRefreshToken(): String = refreshTokenValue
}
