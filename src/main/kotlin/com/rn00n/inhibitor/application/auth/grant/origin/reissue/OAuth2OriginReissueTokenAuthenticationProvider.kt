package com.rn00n.inhibitor.application.auth.grant.origin.reissue

import com.rn00n.inhibitor.application.auth.model.token.OAuth2OriginReissueTokenAuthenticationToken
import com.rn00n.inhibitor.application.auth.service.userdetails.ExtendedUserDetailsService
import com.rn00n.inhibitor.application.auth.support.OAuth2OriginAuthenticationProviderSupport
import com.rn00n.inhibitor.application.auth.usecase.login.origin.OriginLoginCommand
import com.rn00n.inhibitor.application.auth.usecase.login.origin.OriginLoginUseCase
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.OAuth2ErrorCodes
import org.springframework.security.oauth2.core.OAuth2Token
import org.springframework.security.oauth2.core.oidc.OidcIdToken
import org.springframework.security.oauth2.core.oidc.OidcScopes
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator
import java.security.Principal

class OAuth2OriginReissueTokenAuthenticationProvider(
    private val userDetailsService: ExtendedUserDetailsService, // 유저 정보를 로드하는 서비스
    private val authorizationService: OAuth2AuthorizationService,
    private val originLoginUseCase: OriginLoginUseCase, // use case
    private val tokenGenerator: OAuth2TokenGenerator<out OAuth2Token>
) : AuthenticationProvider {

    private val logger = LoggerFactory.getLogger(this::class.java)
    private val ID_TOKEN_TOKEN_TYPE = OAuth2TokenType(OidcParameterNames.ID_TOKEN)

    override fun authenticate(authentication: Authentication): Authentication {
        val refreshTokenAuthentication = authentication as OAuth2OriginReissueTokenAuthenticationToken
        val clientPrincipal =
            OAuth2OriginAuthenticationProviderSupport.getAuthenticatedClientElseThrowInvalidClient(refreshTokenAuthentication)
        val registeredClient = clientPrincipal.registeredClient!!

        val authorization: OAuth2Authorization = authorizationService.findByToken(
            refreshTokenAuthentication.getRefreshToken(), OAuth2TokenType.REFRESH_TOKEN
        ) ?: throw OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_GRANT)

        if (registeredClient.id != authorization.registeredClientId) {
            throw OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_GRANT)
        }

        if (!registeredClient.authorizationGrantTypes.contains(AuthorizationGrantType.REFRESH_TOKEN)) {
            throw OAuth2AuthenticationException(OAuth2ErrorCodes.UNAUTHORIZED_CLIENT)
        }

        val accessToken = authorization.accessToken.token
            ?: throw OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_GRANT)

        val refreshToken = authorization.refreshToken?.token
            ?: throw OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_GRANT)

        val authorizedScopes = authorization.authorizedScopes

        val userDetails = authorization.getAttribute<Authentication>(Principal::class.java.name)

        val loginOutput = originLoginUseCase.execute(
            OriginLoginCommand(
                user = null,
            )
        )

        // ID Token if applicable
        val idToken: OidcIdToken? = if (authorizedScopes.contains(OidcScopes.OPENID)) {
            // 추후 필요하면 발급
            null
        } else null

        val additionalParameters: MutableMap<String, Any> = mutableMapOf()
        if (idToken != null) {
            additionalParameters[OidcParameterNames.ID_TOKEN] = idToken.tokenValue
        }

        return OAuth2AccessTokenAuthenticationToken(
            registeredClient, clientPrincipal, accessToken, refreshToken, additionalParameters
        )
    }

    override fun supports(authentication: Class<*>): Boolean {
        return OAuth2OriginReissueTokenAuthenticationToken::class.java.isAssignableFrom(authentication)
    }
}
