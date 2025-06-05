package com.rn00n.inhibitor.application.auth.grant.origin.refresh

import com.rn00n.inhibitor.application.auth.model.principal.UserPrincipal
import com.rn00n.inhibitor.application.auth.model.token.OAuth2OriginRefreshTokenAuthenticationToken
import com.rn00n.inhibitor.application.auth.service.userdetails.ExtendedUserDetailsService
import com.rn00n.inhibitor.application.auth.support.OAuth2OriginAuthenticationProviderSupport
import com.rn00n.inhibitor.application.auth.usecase.login.origin.OriginLoginCommand
import com.rn00n.inhibitor.application.auth.usecase.login.origin.OriginLoginUseCase
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.*
import org.springframework.security.oauth2.core.oidc.OidcIdToken
import org.springframework.security.oauth2.core.oidc.OidcScopes
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContextHolder
import org.springframework.security.oauth2.server.authorization.token.DefaultOAuth2TokenContext
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator
import java.security.Principal

class OAuth2OriginRefreshTokenAuthenticationProvider(
    private val userDetailsService: ExtendedUserDetailsService, // 유저 정보를 로드하는 서비스
    private val authorizationService: OAuth2AuthorizationService,
    private val originLoginUseCase: OriginLoginUseCase, // use case
    private val tokenGenerator: OAuth2TokenGenerator<out OAuth2Token>
) : AuthenticationProvider {

    private val logger = LoggerFactory.getLogger(this::class.java)
    private val ID_TOKEN_TOKEN_TYPE = OAuth2TokenType(OidcParameterNames.ID_TOKEN)

    override fun authenticate(authentication: Authentication): Authentication {
        val refreshTokenAuthentication = authentication as OAuth2OriginRefreshTokenAuthenticationToken

        val now = System.currentTimeMillis()

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

        val refreshToken = authorization.refreshToken
            ?: throw OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_GRANT)

        if (!refreshToken.isActive) {
            throw OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_GRANT)
        }

        var scopes = refreshTokenAuthentication.getScopes()
        val authorizedScopes = authorization.authorizedScopes
        if (scopes.isEmpty()) scopes = authorizedScopes
        if (!authorizedScopes.containsAll(scopes)) {
            throw OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_SCOPE)
        }

        val principal = authorization.getAttribute<Authentication>(Principal::class.java.name)

        val userPrincipal =
            (principal?.principal as? UserPrincipal) ?: throw OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_REQUEST)

        val currentUserDetails: Authentication = UsernamePasswordAuthenticationToken(
            userDetailsService.loadUserById(userPrincipal.id),
            principal.credentials,
            principal.authorities
        )

        val loginOutput = originLoginUseCase.execute(
            OriginLoginCommand(
                user = null,
                now = now,
            )
        )

        val tokenContextBuilder = DefaultOAuth2TokenContext.builder()
            .registeredClient(registeredClient)
            .principal(currentUserDetails)
            .authorizationServerContext(AuthorizationServerContextHolder.getContext())
            .authorization(authorization)
            .authorizedScopes(authorization.authorizedScopes)
            .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
            .authorizationGrant(refreshTokenAuthentication)

        val authorizationBuilder = OAuth2Authorization.from(authorization).principalName(currentUserDetails.name)

        // Access Token
        var tokenContext = tokenContextBuilder.tokenType(OAuth2TokenType.ACCESS_TOKEN)
            .build()

        val generatedAccessToken = tokenGenerator.generate(tokenContext)
            ?: throw OAuth2AuthenticationException(
                OAuth2Error(OAuth2ErrorCodes.SERVER_ERROR, "Failed to generate access token", null)
            )
        val accessToken = OAuth2OriginAuthenticationProviderSupport.accessToken(
            authorizationBuilder, generatedAccessToken, tokenContext
        )

        // Refresh Token (renew if not reuse)
        var currentRefreshToken = refreshToken.token
        if (!registeredClient.tokenSettings.isReuseRefreshTokens) {
            tokenContext = tokenContextBuilder.tokenType(OAuth2TokenType.REFRESH_TOKEN).build()
            val generatedRefreshToken = tokenGenerator.generate(tokenContext)
            if (generatedRefreshToken !is OAuth2RefreshToken) {
                throw OAuth2AuthenticationException(
                    OAuth2Error(OAuth2ErrorCodes.SERVER_ERROR, "Failed to generate refresh token", null)
                )
            }
            currentRefreshToken = generatedRefreshToken
            authorizationBuilder.refreshToken(currentRefreshToken)
        }

        // ID Token if applicable
        val idToken: OidcIdToken? = if (authorizedScopes.contains(OidcScopes.OPENID)) {
            tokenContextBuilder.tokenType(ID_TOKEN_TOKEN_TYPE)
                .authorization(authorizationBuilder.build())
                .build().let { ctx ->
                    val generatedIdToken = tokenGenerator.generate(ctx) as? Jwt
                        ?: throw OAuth2AuthenticationException(
                            OAuth2Error(OAuth2ErrorCodes.SERVER_ERROR, "Failed to generate ID token", null)
                        )
                    OidcIdToken(
                        generatedIdToken.tokenValue,
                        generatedIdToken.issuedAt,
                        generatedIdToken.expiresAt,
                        generatedIdToken.claims
                    ).also {
                        authorizationBuilder.token(it) { meta ->
                            meta[OAuth2Authorization.Token.CLAIMS_METADATA_NAME] = it.claims
                        }
                    }
                }
        } else null

        authorizationService.save(authorizationBuilder.build())

        val additionalParameters: MutableMap<String, Any> = mutableMapOf()
        if (idToken != null) {
            additionalParameters[OidcParameterNames.ID_TOKEN] = idToken.tokenValue
        }
        additionalParameters.put("provider_id", userPrincipal.id)

        return OAuth2AccessTokenAuthenticationToken(
            registeredClient, clientPrincipal, accessToken, currentRefreshToken, additionalParameters
        )
    }

    override fun supports(authentication: Class<*>): Boolean {
        return OAuth2OriginRefreshTokenAuthenticationToken::class.java.isAssignableFrom(authentication)
    }
}
