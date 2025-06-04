package com.rn00n.inhibitor.application.auth.handler

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.OAuth2ErrorCodes
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken
import org.springframework.security.oauth2.server.authorization.web.authentication.OAuth2AccessTokenResponseAuthenticationSuccessHandler
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import java.time.Duration
import java.time.Instant

/**
 * OAuth2TokenConcurrencyHandler
 *
 * OAuth2 인증 성공 시 트랜잭션 관리를 통해 토큰 서브시스템과
 * Redis 기반으로 사용자 세션 동시 접속을 제한하는 핸들러입니다.
 */
//@Component
class OAuth2TokenConcurrencyHandler(
    private val redisTemplate: RedisTemplate<String, String>,
    private val authorizationService: OAuth2AuthorizationService,
) : AuthenticationSuccessHandler {

    private val logger = KotlinLogging.logger {}
    private val objectMapper = jacksonObjectMapper()

    // Spring Security 기본 토큰 인증 성공 핸들러
    val defaultHandler = OAuth2AccessTokenResponseAuthenticationSuccessHandler()

    /**
     * onAuthenticationSuccess
     *
     * 인증 성공 시 호출되며, Redis에 토큰을 관리하고 동시 세션을 제한합니다.
     *
     * @param request 클라이언트의 HTTP 요청
     * @param response 클라이언트로의 HTTP 응답
     * @param authentication 인증 객체
     */
    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        // 인증 타입 검증 (OAuth2AccessTokenAuthenticationToken만 처리 가능)
        if (authentication !is OAuth2AccessTokenAuthenticationToken) {
            if (this.logger.isErrorEnabled()) {
                this.logger.error {
                    (Authentication::class.java.simpleName + " must be of type "
                        + OAuth2AccessTokenAuthenticationToken::class.java.name + " but was "
                        + authentication.javaClass.name
                        )
                }
            }
            val error = OAuth2Error(
                OAuth2ErrorCodes.SERVER_ERROR,
                "Unable to process the access token response.", null
            )
            throw OAuth2AuthenticationException(error)
        }

        val accessToken = authentication.accessToken
        val accessTokenValue: String = accessToken.tokenValue
        val refreshTokenValue: String = authentication.refreshToken!!.tokenValue
        val additionalParameters: Map<String, Any> = authentication.additionalParameters

        val userId = additionalParameters["userId"]
        val clientId = additionalParameters["clientId"]
        val createdAt = Instant.now().toString()

        // 유효한 userId와 clientId가 있는 경우에만 처리
        if (userId != null && clientId != null) {
            // 클라이언트별 최대 세션 개수 정의
            val maxSessions = when (clientId) {
                "inhibitor" -> 1
                "backoffice" -> 5
                else -> 3 // 기본값
            }

            val key = "user:$userId:$clientId:tokens"
            val redisOps = redisTemplate.opsForList()

            // 현재 저장된 토큰 개수 확인
            val existingTokens = redisOps.range(key, 0, -1) ?: listOf()
            if (existingTokens.size >= maxSessions) {
                val tokensToRemove = existingTokens.size - maxSessions + 1  // 삭제해야 할 토큰 개수

                // 초과된 세션 삭제 (Redis List의 왼쪽에서 pop)
                repeat(tokensToRemove) {
                    val leftPop: String? = redisOps.leftPop(key)
                    if (leftPop != null) {
                        val leftPopMap = objectMapper.readValue(leftPop, Map::class.java)
                        val leftRefreshToken = leftPopMap["refresh_token"] as String

                        // 삭제할 Refresh Token을 OAuth2AuthorizationService에서 제거
                        val leftAuthentication = authorizationService.findByToken(leftRefreshToken, OAuth2TokenType.REFRESH_TOKEN)
                        logger.info { "Removing leftAuthentication: $leftAuthentication" }

                        if (leftAuthentication != null) {
                            authorizationService.remove(leftAuthentication)
                        }
                    }
                }
            }

            // 새 토큰 Redis에 저장
            val tokenData = mapOf(
                "access_token" to accessTokenValue,
                "refresh_token" to refreshTokenValue,
                "created_at" to createdAt
            )
            redisOps.rightPush(key, objectMapper.writeValueAsString(tokenData))

            // 만료 시간 설정
            val issuedAt: Instant? = accessToken.issuedAt
            val expiresAt: Instant? = accessToken.expiresAt

            if (issuedAt != null && expiresAt != null) {
                val ttl = Duration.between(issuedAt, expiresAt).seconds
                redisTemplate.expire(key, Duration.ofSeconds(ttl))
            } else {
                // 기본 TTL 값 적용 (예: 60초)
                redisTemplate.expire(key, Duration.ofSeconds(60))
            }
        }

        // 기본 핸들러 호출하여 추가적인 성공 처리를 위임
        defaultHandler.onAuthenticationSuccess(request, response, authentication)
    }
}