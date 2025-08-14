package com.rn00n.inhibitor.infrastructure.security.authentication.grant.origin.password

import com.rn00n.inhibitor.application.auth.event.LoginSuccessEvent
import com.rn00n.inhibitor.application.auth.exception.DefaultOAuth2AuthenticationException
import com.rn00n.inhibitor.application.auth.exception.ErrorCode
import com.rn00n.inhibitor.infrastructure.security.authentication.grant.DefaultAuthenticationChecks
import com.rn00n.inhibitor.application.auth.model.UserPrincipal
import com.rn00n.inhibitor.infrastructure.security.authentication.token.OAuth2OriginPasswordAuthenticationToken
import com.rn00n.inhibitor.application.auth.service.userdetails.ExtendedUserDetailsService
import com.rn00n.inhibitor.infrastructure.security.authentication.support.OAuth2OriginAuthenticationProviderSupport
import com.rn00n.inhibitor.application.auth.usecase.login.origin.OriginLoginCommand
import com.rn00n.inhibitor.application.auth.usecase.login.origin.OriginLoginUseCase
import io.github.oshai.kotlinlogging.KotlinLogging
import org.slf4j.MDC
import org.springframework.context.ApplicationEventPublisher
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetailsChecker
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.oauth2.core.*
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames
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
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator
import java.security.Principal
import java.util.*

/**
 * OAuth2 Password Grant 인증 제공자.
 * - 사용자 이름/비밀번호를 기반으로 인증 수행
 * - 클라이언트 자격 증명 확인 및 액세스, 리프레시, ID 토큰 생성
 */
@Suppress("DEPRECATION")
class OAuth2OriginPasswordGrantAuthenticationProvider(
    private val userDetailsService: ExtendedUserDetailsService, // 유저 정보를 로드하는 서비스
    private val authorizationService: OAuth2AuthorizationService, // OAuth2 인증정보 관리 서비스
    private val originLoginUseCase: OriginLoginUseCase,
    private val tokenGenerator: OAuth2TokenGenerator<out OAuth2Token>, // 토큰 생성기
    private val eventPublisher: ApplicationEventPublisher,
) : AuthenticationProvider {

    private val logger = KotlinLogging.logger {}
    private var preAuthenticationChecks: UserDetailsChecker = DefaultAuthenticationChecks.DefaultPreAuthenticationChecks()
    private var postAuthenticationChecks: UserDetailsChecker = DefaultAuthenticationChecks.DefaultPostAuthenticationChecks()

    companion object {
        private const val ERROR_URI = "https://datatracker.ietf.org/doc/html/rfc6749#section-5.2"
        private val ID_TOKEN_TOKEN_TYPE = OAuth2TokenType(OidcParameterNames.ID_TOKEN)
    }

    /**
     * 사용자 인증 로직.
     * - 사용자 이름과 비밀번호를 기반으로 토큰 생성 및 반환
     */
    override fun authenticate(authentication: Authentication): Authentication {
        // 캐스팅된 인증 토큰에서 사용자의 아이디와 비밀번호 획득
        val passwordTokenAuthentication = authentication as OAuth2OriginPasswordAuthenticationToken
        val username = passwordTokenAuthentication.username
        val password = passwordTokenAuthentication.password
        val requestScopes = passwordTokenAuthentication.getScopes()

        val now = System.currentTimeMillis()

        // 클라이언트 유효성 검증
        val clientPrincipal = OAuth2OriginAuthenticationProviderSupport.getAuthenticatedClientElseThrowInvalidClient(authentication)
        val registeredClient = clientPrincipal.registeredClient ?: throw OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_CLIENT)

        val authorizedScopes = registeredClient.scopes
        if (!authorizedScopes.containsAll(requestScopes)) {
            throw OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_SCOPE)
        }

        // 사용자 정보 로드 및 검증
        val userPrincipal: UserPrincipal = try {
            userDetailsService.loadUserByUsername(username) as UserPrincipal
        } catch (usernameNotFoundException: UsernameNotFoundException) {
            throw DefaultOAuth2AuthenticationException(ErrorCode.USER_NOT_FOUND, "[USER_SERVICE] USER_NOT_FOUND")
        }

        val principal: Authentication = UsernamePasswordAuthenticationToken(userPrincipal, password, userPrincipal.authorities)

        // 검증 시작
        preAuthenticationChecks.check(userPrincipal)

        originLoginUseCase.execute(
            OriginLoginCommand(
                userPrincipal.account,
                password,
                now = now,
            )
        )

        postAuthenticationChecks.check(userPrincipal)
        // 검증 종료

        // 인증 요청 생성
        val authorizationRequest = OAuth2AuthorizationRequest.authorizationCode()
            .clientId(registeredClient.clientId)
            .authorizationUri("/oauth2/token") // 인증 토큰 발급 URI
            .redirectUri(registeredClient.redirectUris.firstOrNull() ?: "")
            .scopes(requestScopes)
            .attributes(
                mapOf(
                    OAuth2ParameterNames.GRANT_TYPE to AuthorizationGrantType.PASSWORD.value,
                    "username" to username,
                    Principal::class.java.name to principal
                )
            )
            .build()

        // 인증 객체 생성
        val authorization: OAuth2Authorization = OAuth2Authorization.withRegisteredClient(registeredClient)
            .id(UUID.randomUUID().toString()) // 랜덤 인증 ID 설정
            .principalName(userPrincipal.username)
            .authorizationGrantType(AuthorizationGrantType.PASSWORD)
            .authorizedScopes(requestScopes)
            .attribute(Principal::class.java.name, principal) // 해당 사용자 정보를 추가
            .build()

        // 토큰 컨텍스트 생성
        val tokenContextBuilder = DefaultOAuth2TokenContext.builder()
            .registeredClient(registeredClient)
            .principal(principal)
            .authorizationServerContext(AuthorizationServerContextHolder.getContext())
            .authorization(authorization)
            .authorizedScopes(authorization.authorizedScopes)
            .authorizationGrantType(AuthorizationGrantType.PASSWORD)
            .authorizationGrant(passwordTokenAuthentication)

        val authorizationBuilder: OAuth2Authorization.Builder = OAuth2Authorization.from(authorization)

        // 액세스 토큰 생성
        var tokenContext: OAuth2TokenContext = tokenContextBuilder.tokenType(OAuth2TokenType.ACCESS_TOKEN).build()

        val generatedAccessToken: OAuth2Token? = tokenGenerator.generate(tokenContext)
        if (generatedAccessToken == null) {
            val error = OAuth2Error(
                OAuth2ErrorCodes.SERVER_ERROR,
                "The token generator failed to generate the access token.", ERROR_URI
            )
            throw OAuth2AuthenticationException(error)
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace { "Generated access token" }
        }

        // 액세스 토큰 객체를 생성 및 설정
        val accessToken: OAuth2AccessToken =
            OAuth2OriginAuthenticationProviderSupport.accessToken(authorizationBuilder, generatedAccessToken, tokenContext)

        // 리프레시 토큰 생성
        var refreshToken: OAuth2RefreshToken?
        tokenContext = tokenContextBuilder.tokenType(OAuth2TokenType.REFRESH_TOKEN).build()

        val generatedRefreshToken = tokenGenerator.generate(tokenContext)

        if (generatedRefreshToken is OAuth2RefreshToken) {
            if (this.logger.isTraceEnabled()) {
                this.logger.trace { "Generated refresh token" }
            }
            refreshToken = generatedRefreshToken
            authorizationBuilder.refreshToken(refreshToken)
        } else {
            val error = OAuth2Error(
                OAuth2ErrorCodes.SERVER_ERROR,
                "The token generator failed to generate a valid refresh token.",
                ERROR_URI
            )
            throw OAuth2AuthenticationException(error)
        }

        // OAuth2Authorization 저장
        val finalAuthorization = authorizationBuilder.build()
        this.authorizationService.save(finalAuthorization)

        // ID 토큰 생성 (OpenID Connect 스코프가 요청된 경우)
        val idToken: OidcIdToken? = if (authorizationRequest.scopes.contains(OidcScopes.OPENID)) {
            tokenContextBuilder.tokenType(ID_TOKEN_TOKEN_TYPE).build().let { ctx ->
                val generatedIdToken = tokenGenerator.generate(ctx) as? Jwt ?: throw OAuth2AuthenticationException(
                    OAuth2Error(OAuth2ErrorCodes.SERVER_ERROR, "The token generator failed to generate the ID token.", "")
                )
                OidcIdToken(
                    generatedIdToken.tokenValue,
                    generatedIdToken.issuedAt,
                    generatedIdToken.expiresAt,
                    generatedIdToken.claims
                ).apply {
                    authorizationBuilder.token(this) { metadata ->
                        metadata[OAuth2Authorization.Token.CLAIMS_METADATA_NAME] = claims
                    }
                }
            }
        } else {
            null
        }

        // 추가 응답 파라미터 설정
        val additionalParameters: MutableMap<String, Any> = idToken?.let {
            mutableMapOf(OidcParameterNames.ID_TOKEN to it.tokenValue)
        } ?: mutableMapOf()
        additionalParameters.put("provider_id", userPrincipal.id)

        if (this.logger.isTraceEnabled()) {
            this.logger.trace { "Generated refresh token" }
        }

        eventPublisher.publishEvent(
            LoginSuccessEvent(
                accountId = userPrincipal.id,
                eventId = MDC.get("requestId"),
            )
        )

        // 최종적으로 OAuth2AccessTokenAuthenticationToken 반환
        return OAuth2AccessTokenAuthenticationToken(
            registeredClient, clientPrincipal, accessToken,
            refreshToken, additionalParameters
        )
    }

    /**
     * 해당 AuthenticationProvider가 처리할 인증 토큰 타입 지원 여부
     */
    override fun supports(authentication: Class<*>): Boolean {
        return OAuth2OriginPasswordAuthenticationToken::class.java.isAssignableFrom(authentication)
    }
}