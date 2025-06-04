package com.rn00n.inhibitor.application.auth.support

import com.rn00n.inhibitor.application.auth.exception.DefaultOAuth2AuthenticationException
import com.rn00n.inhibitor.application.auth.exception.ErrorCode
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.*
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext

/**
 * Utility class for handling OAuth2 authentication related processes.
 * OAuth2 인증 과정을 처리하는 유틸리티 클래스.
 */
class OAuth2OriginAuthenticationProviderSupport {

    companion object {

        /**
         * Returns the authenticated client or throws an exception if invalid.
         * 인증된 클라이언트를 반환하거나 유효하지 않은 경우 예외를 던집니다.
         *
         * @param authentication the current authentication object (현재 인증 객체)
         * @return authenticated OAuth2ClientAuthenticationToken
         * @throws OAuth2AuthenticationException if the client is invalid
         */
        fun getAuthenticatedClientElseThrowInvalidClient(authentication: Authentication): OAuth2ClientAuthenticationToken {
            var clientPrincipal: OAuth2ClientAuthenticationToken? = null
            // Check if the principal is an instance of OAuth2ClientAuthenticationToken
            // 인증 주체가 OAuth2ClientAuthenticationToken인지 확인
            if (OAuth2ClientAuthenticationToken::class.java.isAssignableFrom(authentication.principal.javaClass)) {
                clientPrincipal = authentication.principal as OAuth2ClientAuthenticationToken
            }
            // 인증 상태 확인 (Valid하면 반환)
            if (clientPrincipal != null && clientPrincipal.isAuthenticated) {
                return clientPrincipal
            }
            // 인증 실패시 INVALID_CLIENT 오류 발생
            throw DefaultOAuth2AuthenticationException(ErrorCode.BAD_REQUEST, OAuth2ErrorCodes.INVALID_CLIENT)
        }

        /**
         * Creates and returns an OAuth2 access token.
         * OAuth2 액세스 토큰을 생성 및 반환합니다.
         *
         * @param builder the authorization builder (OAuth2 인증 빌더)
         * @param token the raw token object (원시 토큰 객체)
         * @param accessTokenContext the context for access token creation (액세스 토큰 생성 컨텍스트)
         * @return OAuth2AccessToken generated token
         */
        fun <T : OAuth2Token> accessToken(
            builder: OAuth2Authorization.Builder,
            token: T,
            accessTokenContext: OAuth2TokenContext
        ): OAuth2AccessToken {
            val accessToken = OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER, // BEARER 타입으로 토큰 생성
                token.tokenValue,
                token.issuedAt,
                token.expiresAt,
                accessTokenContext.authorizedScopes // 권한 범위 설정
            )

            val accessTokenFormat = accessTokenContext.registeredClient
                .tokenSettings
                .accessTokenFormat // 액세스 토큰 포맷 가져오기

            // 빌더에 토큰과 메타데이터 추가
            builder.token(accessToken) { metadata ->
                if (token is ClaimAccessor) {
                    // 사용자 정보(Claims)를 포함
                    metadata[OAuth2Authorization.Token.CLAIMS_METADATA_NAME] = token.claims
                }
                metadata[OAuth2Authorization.Token.INVALIDATED_METADATA_NAME] = false // 토큰이 무효화되지 않음을 표시
                metadata[OAuth2TokenFormat::class.java.name] = accessTokenFormat.value // 토큰 포맷 메타데이터 설정
            }

            return accessToken // 생성된 액세스 토큰 반환
        }

    }
}