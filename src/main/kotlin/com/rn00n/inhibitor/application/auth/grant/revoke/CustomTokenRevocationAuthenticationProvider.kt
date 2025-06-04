package com.rn00n.inhibitor.application.auth.grant.revoke

import com.rn00n.inhibitor.application.auth.exception.ErrorCode
import com.rn00n.inhibitor.application.auth.exception.DefaultOAuth2AuthenticationException
import com.rn00n.inhibitor.domain.oauth2.service.OAuth2AuthorizationDomainService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.OAuth2RefreshToken
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2TokenRevocationAuthenticationToken
import org.springframework.stereotype.Component

@Component
class CustomTokenRevocationAuthenticationProvider(
    private val authorizationService: OAuth2AuthorizationDomainService,
) : AuthenticationProvider {

    private val logger = KotlinLogging.logger {}

    override fun authenticate(authentication: Authentication): Authentication {
        val tokenRevocationAuthentication = authentication as OAuth2TokenRevocationAuthenticationToken
        val clientPrincipal = getAuthenticatedClientElseThrowInvalidClient(tokenRevocationAuthentication)
        val registeredClient = clientPrincipal.registeredClient
            ?: throw DefaultOAuth2AuthenticationException(ErrorCode.BAD_REQUEST, "invalid_client")
        val token = tokenRevocationAuthentication.token
        val tokenTypeHint = tokenRevocationAuthentication.tokenTypeHint

        // refresh token only
        val authorization = when (tokenTypeHint) {
            "access_token" -> null
            "refresh_token" -> authorizationService.findByRefreshTokenValue(token)
            else -> throw DefaultOAuth2AuthenticationException(ErrorCode.BAD_REQUEST, "invalid_request")
        }

        if (authorization == null) {
            if (logger.isTraceEnabled()) {
                logger.trace { "Token not found. Still returning 200 OK as per RFC 7009." }
            }
            return tokenRevocationAuthentication
        }

        if (authorization.registeredClientId != registeredClient.id) {
            throw DefaultOAuth2AuthenticationException(ErrorCode.BAD_REQUEST, "invalid_client")
        }

        authorizationService.revoke(authorization) // row 삭제

        return OAuth2TokenRevocationAuthenticationToken(OAuth2RefreshToken(token, authorization.refreshTokenIssuedAt), clientPrincipal)
    }

    override fun supports(authentication: Class<*>): Boolean {
        return OAuth2TokenRevocationAuthenticationToken::class.java.isAssignableFrom(authentication)
    }

    fun getAuthenticatedClientElseThrowInvalidClient(authentication: Authentication): OAuth2ClientAuthenticationToken {
        var clientPrincipal: OAuth2ClientAuthenticationToken? = null
        if (OAuth2ClientAuthenticationToken::class.java.isAssignableFrom(authentication.principal.javaClass)) {
            clientPrincipal = authentication.principal as OAuth2ClientAuthenticationToken
        }

        if (clientPrincipal != null && clientPrincipal.isAuthenticated) {
            return clientPrincipal
        } else {
            throw OAuth2AuthenticationException("invalid_client")
        }
    }

}
