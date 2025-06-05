package com.rn00n.inhibitor.application.auth.grant.origin.password

import com.rn00n.inhibitor.application.auth.grant.ExtendedAuthorizationGrantType
import com.rn00n.inhibitor.application.auth.model.token.OAuth2OriginPasswordAuthenticationToken
import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.convert.converter.Converter
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationGrantAuthenticationToken
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken
import org.springframework.security.web.authentication.AuthenticationConverter

/**
 * OAuth2PasswordAuthenticationConverter
 *
 * HttpServletRequest를 받아 Password Grant 방식의 인증 토큰을 생성하는 클래스.
 * 클라이언트 인증 정보와 추가 파라미터를 추출하여 OAuth2PasswordAuthenticationToken을 반환합니다.
 *
 * Spring Security OAuth2 인증 흐름 중 Password Grant Type을 처리하는 역할을 합니다.
 */
@Suppress("DEPRECATION")
class OAuth2OriginPasswordAuthenticationConverter
    : Converter<HttpServletRequest, OAuth2AuthorizationGrantAuthenticationToken>, AuthenticationConverter {

    private val supportedGrantTypes = setOf(
        ExtendedAuthorizationGrantType.ORIGIN_PASSWORD.value,
    )
    /**
     * 주어진 HttpServletRequest에서 OAuth2 토큰 인증 정보를 추출하여 반환합니다.
     *
     * @param request HttpServletRequest에서 인증 요청 정보를 추출
     * @return OAuth2AuthorizationGrantAuthenticationToken 또는 null
     * (Password Grant가 아닌 경우 null 반환)
     */
    override fun convert(request: HttpServletRequest): OAuth2AuthorizationGrantAuthenticationToken? {
        // grant_type 파라미터를 확인하여 password grant 여부 판단
        val grantType: String = request.getParameter(OAuth2ParameterNames.GRANT_TYPE)
        if (!supportedGrantTypes.contains(grantType)) {
            return null
        }

        // 사용자 이름과 비밀번호를 요청 파라미터에서 추출
        val username = request.getParameter("username")
        val password = request.getParameter("password")

        // 클라이언트 인증 정보 확인 (HttpServletRequest 또는 SecurityContext에서 가져옴)
        val clientPrincipal = request.userPrincipal as? OAuth2ClientAuthenticationToken
            ?: SecurityContextHolder.getContext().authentication as? OAuth2ClientAuthenticationToken
            ?: return null // 클라이언트 인증 실패 시 null 반환

        // 추가적인 요청 파라미터를 처리하여 맵핑
        val additionalParameters = HashMap<String, Any>()
        request.parameterMap.forEach { (key, value) ->
            // username과 password를 제외한 모든 파라미터를 추가
            if (key != "username" && key != "password") {
                additionalParameters[key] = if (value.size == 1) value[0] else value
            }
        }

        // 최종적으로 OAuth2PasswordAuthenticationToken 생성 및 반환
        return OAuth2OriginPasswordAuthenticationToken(username, password, clientPrincipal, additionalParameters)
    }
}