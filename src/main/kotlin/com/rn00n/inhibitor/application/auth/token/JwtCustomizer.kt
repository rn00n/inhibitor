package com.rn00n.inhibitor.application.auth.token

import com.rn00n.inhibitor.application.auth.model.principal.UserPrincipal
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.oauth2.core.ClientAuthenticationMethod
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.OAuth2ErrorCodes
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer
import org.springframework.stereotype.Component
import java.security.MessageDigest
import java.security.cert.X509Certificate
import java.util.*

/**
 * Customizes the JWT token for both access and refresh tokens.
 * 엔터프라이즈 애플리케이션에서 OAuth2 인증 단계에서 토큰에 필요한 정보를 추가하는 컴포넌트.
 */
@Component
class JwtCustomizer : OAuth2TokenCustomizer<JwtEncodingContext> {

    /**
     * Called to customize the token based on its type.
     * 주어진 토큰 유형(access_token 또는 refresh_token)에 맞게 커스터마이징 로직을 구분.
     */
    override fun customize(context: JwtEncodingContext) {
        when (context.tokenType.value) {
            "access_token" -> customizeAccessToken(context)
            "refresh_token" -> customizeRefreshToken(context)
            "id_token" -> customizeIdToken(context)
        }
    }

    /**
     * Adds custom claims to the access token.
     * 사용자 ID와 (필요 시) Mutual TLS 관련 클레임을 추가.
     */
    private fun customizeAccessToken(context: JwtEncodingContext) {
        // 사용자의 권한 정보에서 기본 클레임 추가
        val authorization = context.authorization
        if (authorization != null) {
            val attributes = authorization.attributes
            val principal = attributes["java.security.Principal"] as UsernamePasswordAuthenticationToken
            val userPrincipal = principal.principal as UserPrincipal

            context.claims.claim("providerId", userPrincipal.id.toString()) // 사용자 ID를 클레임에 추가
            context.get<Int>("refreshThreshold")?.let {
                context.claims.claim("refreshThreshold", it) // refresh 한계값
            }
        }

        // 인증 요청이 Mutual TLS로 수행되었는지 확인 후 추가 작업
        val clientAuthentication = context.get(OAuth2ClientAuthenticationToken::class.java)
        if (clientAuthentication != null && isMutualTlsAuthentication(clientAuthentication)) {
            addMutualTlsClaims(clientAuthentication, context)
        }
    }

    /**
     * Adds custom claims to the refresh token.
     * 리프레시 토큰에서만 사용하는 클레임 추가.
     */
    private fun customizeRefreshToken(context: JwtEncodingContext) {
        context.claims.claim("refresh_claim", "refresh_value") // 심플한 예제로 리프레시 전용 클레임 추가
    }

    private fun customizeIdToken(context: JwtEncodingContext) {
        val authorization = context.authorization
        if (authorization != null) {
            val attributes = authorization.attributes
            val principal = attributes["java.security.Principal"] as UsernamePasswordAuthenticationToken
            val userPrincipal = principal.principal as UserPrincipal

            context.claims.claim("userId", userPrincipal.id.toString()) // 사용자 ID를 클레임에 추가
            context.claims.claim("providerId", userPrincipal.id.toString())
        }
    }

    /**
     * Checks if the authentication is using Mutual TLS.
     * Mutual TLS 인증 방식인지 확인.
     *
     * @return true if client used Mutual TLS authentication, otherwise false
     */
    private fun isMutualTlsAuthentication(clientAuthentication: OAuth2ClientAuthenticationToken): Boolean {
        return ClientAuthenticationMethod.TLS_CLIENT_AUTH == clientAuthentication.clientAuthenticationMethod ||
                ClientAuthenticationMethod.SELF_SIGNED_TLS_CLIENT_AUTH == clientAuthentication.clientAuthenticationMethod
    }

    /**
     * Adds claims regarding Mutual TLS to the token.
     * Mutual TLS에서 사용되는 인증서 정보를 토큰 클레임에 추가 (SHA-256 해시 포함).
     *
     * @param clientAuthentication Client authentication details
     * @param context JwtEncodingContext where claims are stored
     */
    private fun addMutualTlsClaims(
        clientAuthentication: OAuth2ClientAuthenticationToken,
        context: JwtEncodingContext
    ) {
        val clientCertificateChain = clientAuthentication.credentials as? Array<*>
        if (clientCertificateChain != null) {
            try {
                val sha256Thumbprint = computeSHA256Thumbprint(clientCertificateChain[0] as X509Certificate)
                // 클레임에 인증서 해시값 ("thumbprint") 추가
                context.claims.claim("cnf", mapOf("x5t#S256" to sha256Thumbprint))
            } catch (ex: Exception) {
                // SHA-256 해시 계산 실패 시 OAuth2 예외 발생
                val error = OAuth2Error(
                    OAuth2ErrorCodes.SERVER_ERROR,
                    "Failed to compute SHA-256 Thumbprint for client X509Certificate.",
                    null
                )
                throw OAuth2AuthenticationException(error, ex)
            }
        }
    }


    /**
     * Computes the SHA-256 thumbprint of X.509 client certificates.
     * 클라이언트 인증서에서 SHA-256 해시를 계산하여 Mutual TLS 클레임에 사용.
     *
     * @param x509Certificate The X.509 certificate
     * @return SHA-256 hash of the provided certificate
     */
    private fun computeSHA256Thumbprint(x509Certificate: X509Certificate): String {
        val messageDigest = MessageDigest.getInstance("SHA-256")
        val digest = messageDigest.digest(x509Certificate.encoded)
        // Base64 URL-safe encoding으로 변환
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest)
    }
}
