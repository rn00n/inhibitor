package com.rn00n.inhibitor.infrastructure.security.authentication.token

import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationGrantAuthenticationToken

@Suppress("DEPRECATION")
class OAuth2OriginPasswordAuthenticationToken(
    val username: String,
    val password: String,
    private val clientPrincipal: Authentication,
    additionalParameters: Map<String, Any>
) : OAuth2AuthorizationGrantAuthenticationToken(AuthorizationGrantType.PASSWORD, clientPrincipal, additionalParameters) {

    fun getScopes(): Set<String> {
        val scope = additionalParameters["scope"] ?: return emptySet()

        return when (scope) {
            is String -> scope.split(" ").filter { it.isNotEmpty() }.toSet()
            is Collection<*> -> scope.filterIsInstance<String>().toSet()
            else -> emptySet()
        }
    }

}