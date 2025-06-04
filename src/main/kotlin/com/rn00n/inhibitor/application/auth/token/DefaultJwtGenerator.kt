package com.rn00n.inhibitor.application.auth.token

import com.rn00n.inhibitor.application.auth.model.principal.UserPrincipal
import org.springframework.security.core.Authentication
import org.springframework.security.core.session.SessionInformation
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames
import org.springframework.security.oauth2.core.oidc.OidcIdToken
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames
import org.springframework.security.oauth2.jose.jws.JwsAlgorithm
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm
import org.springframework.security.oauth2.jwt.*
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationGrantAuthenticationToken
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator
import org.springframework.util.CollectionUtils
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

class DefaultJwtGenerator(
    private val jwtEncoder: JwtEncoder,
    private val jwtCustomizer: OAuth2TokenCustomizer<JwtEncodingContext>? = null
) : OAuth2TokenGenerator<Jwt> {

    override fun generate(context: OAuth2TokenContext?): Jwt? {
        if (context == null ||
            context.tokenType == null ||
            (
                context.tokenType != OAuth2TokenType.ACCESS_TOKEN &&
                    context.tokenType.value != OidcParameterNames.ID_TOKEN
                )
        ) return null

        if (context.tokenType == OAuth2TokenType.ACCESS_TOKEN &&
            context.registeredClient.tokenSettings.accessTokenFormat != OAuth2TokenFormat.SELF_CONTAINED
        ) return null

        val registeredClient = context.registeredClient
        val issuer = context.authorizationServerContext?.issuer
        val issuedAt = Instant.now()
        val expiresAt = if (context.tokenType.value == OidcParameterNames.ID_TOKEN) {
            issuedAt.plus(30, ChronoUnit.MINUTES) // 기본값, 필요 시 변경 가능
        } else {
            issuedAt.plus(registeredClient.tokenSettings.accessTokenTimeToLive)
        }

        val principal = context.getPrincipal<Authentication>().principal
        val userPrincipal = principal as UserPrincipal
        val claimsBuilder = JwtClaimsSet.builder().apply {
            issuer?.let { issuer(it) }
            subject(userPrincipal.username)
            audience(listOf(registeredClient.clientId))
            issuedAt(issuedAt)
            expiresAt(expiresAt)
            id(UUID.randomUUID().toString())

            if (context.tokenType == OAuth2TokenType.ACCESS_TOKEN) {
                notBefore(issuedAt)
                if (!CollectionUtils.isEmpty(context.authorizedScopes)) {
                    claim(OAuth2ParameterNames.SCOPE, context.authorizedScopes)
                }
            } else if (context.tokenType.value == OidcParameterNames.ID_TOKEN) {
                claim(IdTokenClaimNames.AZP, registeredClient.clientId)

                if (context.authorizationGrantType == AuthorizationGrantType.AUTHORIZATION_CODE) {
                    val authRequest = context.authorization?.getAttribute<OAuth2AuthorizationRequest>(
                        OAuth2AuthorizationRequest::class.java.name
                    )
                    val nonce = authRequest?.additionalParameters?.get(OidcParameterNames.NONCE) as? String
                    if (!nonce.isNullOrBlank()) {
                        claim(IdTokenClaimNames.NONCE, nonce)
                    }

                    val sessionInfo = context.get(SessionInformation::class.java)
                    if (sessionInfo != null) {
                        claim("sid", sessionInfo.sessionId)
                        claim(IdTokenClaimNames.AUTH_TIME, sessionInfo.lastRequest)
                    }
                } else if (context.authorizationGrantType == AuthorizationGrantType.REFRESH_TOKEN) {
                    val currentIdToken = context.authorization?.getToken(OidcIdToken::class.java)?.token
                    currentIdToken?.getClaim<String>("sid")?.let { claim("sid", it) }
                    currentIdToken?.getClaim<Date>(IdTokenClaimNames.AUTH_TIME)?.let {
                        claim(IdTokenClaimNames.AUTH_TIME, it)
                    }
                }
            }
        }

        val jwsAlg: JwsAlgorithm =
            if (context.tokenType.value == OidcParameterNames.ID_TOKEN &&
                registeredClient.tokenSettings.idTokenSignatureAlgorithm != null
            ) {
                registeredClient.tokenSettings.idTokenSignatureAlgorithm
            } else {
                SignatureAlgorithm.RS256
            }

        val jwsHeaderBuilder = JwsHeader.with(jwsAlg)

        jwtCustomizer?.let {
            val jwtContextBuilder = JwtEncodingContext.with(jwsHeaderBuilder, claimsBuilder)
                .registeredClient(registeredClient)
                .principal(context.getPrincipal())
                .authorizationServerContext(context.authorizationServerContext)
                .authorizedScopes(context.authorizedScopes)
                .tokenType(context.tokenType)
                .authorizationGrantType(context.authorizationGrantType)

            context.authorization?.let { authorization -> jwtContextBuilder.authorization(authorization) }
            context.getAuthorizationGrant<OAuth2AuthorizationGrantAuthenticationToken>()
                ?.let { authorizationGrant -> jwtContextBuilder.authorizationGrant(authorizationGrant) }

            if (context.tokenType.value == OidcParameterNames.ID_TOKEN) {
                context.get(SessionInformation::class.java)?.let { sessionInformation ->
                    jwtContextBuilder.put(SessionInformation::class.java, sessionInformation)
                }
            }

            // custom payload only string type
            context.get<Int>("refreshThreshold")
                ?.let { refreshThreshold -> jwtContextBuilder.put("refreshThreshold", refreshThreshold) }

            val jwtContext = jwtContextBuilder.build()
            it.customize(jwtContext)
        }

        val jwsHeader = jwsHeaderBuilder.build()
        val claims = claimsBuilder.build()
        return jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims))
    }
}
