package com.rn00n.inhibitor.application.auth.grant.origin.refresh

import com.rn00n.inhibitor.application.auth.grant.ExtendedAuthorizationGrantType
import com.rn00n.inhibitor.application.auth.model.token.OAuth2OriginRefreshTokenAuthenticationToken
import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.OAuth2ErrorCodes
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames
import org.springframework.security.web.authentication.AuthenticationConverter
import org.springframework.util.StringUtils

@Suppress("DEPRECATION")
class OAuth2OriginRefreshTokenAuthenticationConverter : AuthenticationConverter {

    private val supportedGrantTypes = setOf(
        ExtendedAuthorizationGrantType.ORIGIN_REFRESH_TOKEN.value,
    )

    override fun convert(request: HttpServletRequest): Authentication? {
        val parameters = request.parameterMap.mapValues { it.value.toList() }

        val grantType = parameters[OAuth2ParameterNames.GRANT_TYPE]?.firstOrNull()
        if (!supportedGrantTypes.contains(grantType)) {
            return null
        }

        val clientPrincipal = SecurityContextHolder.getContext().authentication

        val refreshToken = parameters[OAuth2ParameterNames.REFRESH_TOKEN]?.firstOrNull() ?: "invalid token"
        if (!StringUtils.hasText(refreshToken) || parameters[OAuth2ParameterNames.REFRESH_TOKEN]?.size != 1) {
            throw OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_REQUEST)
        }

        val scope = parameters[OAuth2ParameterNames.SCOPE]?.firstOrNull()
        if (scope != null && parameters[OAuth2ParameterNames.SCOPE]?.size != 1) {
            throw OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_REQUEST)
        }

        val additionalParameters = HashMap<String, Any>()
        request.parameterMap.forEach { (key, value) ->
            // username과 password를 제외한 모든 파라미터를 추가
            if (key != OAuth2ParameterNames.GRANT_TYPE && key != OAuth2ParameterNames.REFRESH_TOKEN) {
                additionalParameters[key] = if (value.size == 1) value[0] else value
            }
        }

        return OAuth2OriginRefreshTokenAuthenticationToken(
            refreshToken, clientPrincipal, additionalParameters
        )
    }
}
