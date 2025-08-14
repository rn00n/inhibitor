package com.rn00n.inhibitor.infrastructure.security.authentication.oidc

import org.springframework.security.oauth2.core.oidc.OidcIdToken
import org.springframework.security.oauth2.core.oidc.OidcUserInfo
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization.Token.CLAIMS_METADATA_NAME
import org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcUserInfoAuthenticationContext
import org.springframework.stereotype.Component
import java.util.function.Function

/**
 * OIDC UserInfoMapper
 *
 * - OIDC UserInfo를 생성하기 위한 매퍼 클래스
 * - 인증 컨텍스트에서 ID 토큰의 클레임 정보를 기반으로 OidcUserInfo 생성
 * - 해당 UserInfo에서 사용할 클레임은 JwtCustomizer에서 ID_TOKEN 토큰 타입에 대해 등록해야 함
 *   예: context.claims.claim("providerId", userPrincipal.providerId)
 */
@Component
class OidcUserInfoMapper : Function<OidcUserInfoAuthenticationContext, OidcUserInfo> {

    /**
     * OIDC UserInfo 매핑 함수
     *
     * - 인증 컨텍스트에서 Authorization 객체를 가져옵니다.
     * - Authorization 객체에서 ID 토큰 메타데이터를 추출합니다.
     * - 메타데이터에 포함된 클레임 정보를 기반으로 OidcUserInfo를 생성하여 반환합니다.
     *
     * @param context OidcUserInfoAuthenticationContext 컨텍스트
     * @return OidcUserInfo OIDC 사용자 정보 객체
     */
    @Suppress("UNCHECKED_CAST")
    override fun apply(context: OidcUserInfoAuthenticationContext): OidcUserInfo {
        val authorization = context.authorization

        val idToken = authorization.getToken(OidcIdToken::class.java)

        val metadata = idToken?.metadata

        val claims = metadata?.get(CLAIMS_METADATA_NAME) as? Map<String, Any> ?: emptyMap()
        return OidcUserInfo(claims)
    }
}