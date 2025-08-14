package com.rn00n.inhibitor.infrastructure.security.authentication.token

import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.ClientAuthenticationMethod
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.OAuth2ErrorCodes
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2TokenExchangeCompositeAuthenticationToken
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenClaimNames
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenClaimsContext
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer
import java.security.MessageDigest
import java.security.cert.X509Certificate
import java.util.Base64
import java.util.Collections
import java.util.HashMap
import java.util.LinkedHashMap

/**
 * Custom OAuth2 Token Customizers
 * OAuth2 인증 및 토큰 커스터마이징 로직을 처리하는 클래스입니다.
 * JWT와 Access Token의 claims 커스터마이징을 지원합니다.
 */
class CustomOAuth2TokenCustomizers {

    /**
     * jwtCustomizer 메서드
     * JWT 토큰의 claims를 커스터마이징하기 위한 로직을 정의합니다.
     * @return OAuth2TokenCustomizer<JwtEncodingContext>
     */
    fun jwtCustomizer(): OAuth2TokenCustomizer<JwtEncodingContext> {
        return OAuth2TokenCustomizer { context: JwtEncodingContext ->
            context.claims
                .claims { claims: MutableMap<String, Any> ->
                    customize(
                        context, // 토큰 컨텍스트
                        claims  // JWT 클레임
                    )
                }
        }
    }

    companion object {
        /**
         * customize 메서드
         * Access Token 및 Composite Token의 클레임을 커스터마이징합니다.
         * - X509Certificate를 통한 SHA256 Thumbprint 생성
         * - Composite Token의 actors를 활용하여 act claim 추가
         * @param tokenContext OAuth2TokenContext - 토큰 컨텍스트
         * @param claims MutableMap<String, Any> - 커스터마이징할 클레임
         */
        fun customize(tokenContext: OAuth2TokenContext, claims: MutableMap<String, Any>) {
            // Access Token 요청 시 X509 인증서를 기반으로 cnf (confirmation) 클레임 추가
            if (OAuth2TokenType.ACCESS_TOKEN == tokenContext.tokenType
                && tokenContext.getAuthorizationGrant<Authentication?>() != null
                && tokenContext.getAuthorizationGrant<Authentication>().principal is OAuth2ClientAuthenticationToken
            ) {
                val clientAuthentication = tokenContext.getAuthorizationGrant<Authentication>().principal as OAuth2ClientAuthenticationToken

                // 인증 방식이 TLS 또는 Self-Signed TLS이고 X509 인증서 바인딩 토큰 설정이 활성화되어 있는 경우
                if ((ClientAuthenticationMethod.TLS_CLIENT_AUTH == clientAuthentication.clientAuthenticationMethod ||
                            ClientAuthenticationMethod.SELF_SIGNED_TLS_CLIENT_AUTH == clientAuthentication.clientAuthenticationMethod)
                    && tokenContext.registeredClient.tokenSettings.isX509CertificateBoundAccessTokens
                ) {
                    val clientCertificateChain = clientAuthentication.credentials as Array<*>
                    try {
                        // X509 인증서의 Thumbprint(SHA-256) 생성
                        val sha256Thumbprint =
                            computeSHA256Thumbprint(
                                clientCertificateChain[0] as X509Certificate
                            )
                        val x5tClaim: MutableMap<String, Any> = HashMap()
                        x5tClaim["x5t#S256"] = sha256Thumbprint
                        claims["cnf"] = x5tClaim // cnf (confirmation) 클레임 설정
                    } catch (ex: Exception) {
                        val error = OAuth2Error(
                            OAuth2ErrorCodes.SERVER_ERROR,
                            "Failed to compute SHA-256 Thumbprint for client X509Certificate.", null
                        )
                        throw OAuth2AuthenticationException(error, ex)
                    }
                }
            }

            // Composite Token 요청 시 act(claims of the current actor) 클레임을 추가
            if (tokenContext.getPrincipal<Authentication>() is OAuth2TokenExchangeCompositeAuthenticationToken) {
                val compositeAuthenticationToken =
                    tokenContext.getPrincipal<Authentication>() as OAuth2TokenExchangeCompositeAuthenticationToken
                var currentClaims: MutableMap<String, Any> = claims
                for (actor in compositeAuthenticationToken.actors) {
                    val actorClaims = actor.claims
                    val actClaim: MutableMap<String, Any> = LinkedHashMap()
                    actClaim[OAuth2TokenClaimNames.ISS] = actorClaims[OAuth2TokenClaimNames.ISS] as Any // issuer
                    actClaim[OAuth2TokenClaimNames.SUB] = actorClaims[OAuth2TokenClaimNames.SUB]!! // subject
                    currentClaims["act"] = Collections.unmodifiableMap(actClaim) // act 클레임 설정
                    currentClaims = actClaim
                }
            }
        }

        /**
         * JWT 클레임을 커스터마이징하는 커스터마이저
         * @return OAuth2TokenCustomizer<JwtEncodingContext>
         */
        fun jwtCustomizer(): OAuth2TokenCustomizer<JwtEncodingContext> {
            return OAuth2TokenCustomizer { context ->
                context.claims.claims { claims ->
                    customize(context, claims)
                }
            }
        }

        /**
         * Access Token 클레임을 커스터마이징하는 커스터마이저
         * @return OAuth2TokenCustomizer<OAuth2TokenClaimsContext>
         */
        fun accessTokenCustomizer(): OAuth2TokenCustomizer<OAuth2TokenClaimsContext> {
            return OAuth2TokenCustomizer { context ->
                context.claims.claims { claims ->
                    customize(context, claims)
                }
            }
        }

        /**
         * X509 인증서의 SHA-256 Thumbprint를 생성합니다.
         * @param x509Certificate X509Certificate - 클라이언트 인증서
         * @return String - SHA-256 해시 값의 Base64 URL-safe 인코딩
         * @throws Exception - 해시 생성 실패 시 발생
         */
        @Throws(java.lang.Exception::class)
        private fun computeSHA256Thumbprint(x509Certificate: X509Certificate): String {
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(x509Certificate.encoded)
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest)
        }
    }
}