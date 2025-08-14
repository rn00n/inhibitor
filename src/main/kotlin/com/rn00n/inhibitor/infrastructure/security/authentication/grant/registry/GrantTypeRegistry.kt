package com.rn00n.inhibitor.infrastructure.security.authentication.grant.registry

import com.rn00n.inhibitor.infrastructure.security.authentication.config.CustomOAuth2TokenEndpointConfigurer
import com.rn00n.inhibitor.infrastructure.security.authentication.grant.origin.password.OAuth2OriginPasswordAuthenticationConverter
import com.rn00n.inhibitor.infrastructure.security.authentication.grant.origin.refresh.OAuth2OriginRefreshTokenAuthenticationConverter
import com.rn00n.inhibitor.infrastructure.security.authentication.grant.origin.reissue.OAuth2OriginReissueTokenAuthenticationConverter
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.stereotype.Component

@Component
class GrantTypeRegistry(
    private val customTokenEndpointConfigurer: CustomOAuth2TokenEndpointConfigurer
) {
    fun getBindings(http: HttpSecurity): List<AuthenticationGrantBinding> {
        return listOf(
            AuthenticationGrantBinding(
                OAuth2OriginPasswordAuthenticationConverter(),
                customTokenEndpointConfigurer.createOAuth2PasswordGrantAuthenticationProvider(http)
            ),
            AuthenticationGrantBinding(
                OAuth2OriginRefreshTokenAuthenticationConverter(),
                customTokenEndpointConfigurer.createOAuth2RefreshTokenGrantAuthenticationProvider(http)
            ),
            AuthenticationGrantBinding(
                OAuth2OriginReissueTokenAuthenticationConverter(),
                customTokenEndpointConfigurer.createOAuth2ReissueTokenGrantAuthenticationProvider(http)
            ),
        )
    }
}