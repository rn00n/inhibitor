package com.rn00n.inhibitor.application.auth.config

import com.rn00n.inhibitor.application.auth.grant.origin.password.OAuth2OriginPasswordGrantAuthenticationProvider
import com.rn00n.inhibitor.application.auth.grant.origin.refresh.OAuth2OriginRefreshTokenAuthenticationProvider
import com.rn00n.inhibitor.application.auth.grant.origin.reissue.OAuth2OriginReissueTokenAuthenticationProvider
import com.rn00n.inhibitor.application.auth.service.userdetails.ExtendedUserDetailsService
import com.rn00n.inhibitor.application.auth.support.CustomOAuth2ConfigurerSupport
import com.rn00n.inhibitor.application.auth.usecase.login.origin.password.OriginPasswordLoginUseCase
import com.rn00n.inhibitor.application.auth.usecase.login.origin.refresh.OriginRefreshLoginUseCase
import com.rn00n.inhibitor.application.auth.usecase.login.origin.reissue.OriginReissueLoginUseCase
import com.rn00n.inhibitor.commons.webclient.WebClientRequestHelper
import org.springframework.context.ApplicationEventPublisher
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.stereotype.Component

@Component
class CustomOAuth2TokenEndpointConfigurer(
    private val userDetailsService: ExtendedUserDetailsService,
    private val passwordLoginUseCase: OriginPasswordLoginUseCase,
    private val refreshLoginUseCase: OriginRefreshLoginUseCase,
    private val reissueLoginUseCase: OriginReissueLoginUseCase,
    private val webClientRequestHelper: WebClientRequestHelper,
    private val eventPublisher: ApplicationEventPublisher,
) {

    fun createOAuth2PasswordGrantAuthenticationProvider(
        http: HttpSecurity
    ): OAuth2OriginPasswordGrantAuthenticationProvider {
        val authorizationService = CustomOAuth2ConfigurerSupport.getAuthorizationService(http)
        val tokenGenerator = CustomOAuth2ConfigurerSupport.getTokenGenerator(http)

        return OAuth2OriginPasswordGrantAuthenticationProvider(
            userDetailsService,
            authorizationService,
            passwordLoginUseCase,
            tokenGenerator,
            eventPublisher,
        )
    }

    fun createOAuth2RefreshTokenGrantAuthenticationProvider(
        http: HttpSecurity
    ): OAuth2OriginRefreshTokenAuthenticationProvider {
        val authorizationService = CustomOAuth2ConfigurerSupport.getAuthorizationService(http)
        val tokenGenerator = CustomOAuth2ConfigurerSupport.getTokenGenerator(http)

        return OAuth2OriginRefreshTokenAuthenticationProvider(
            userDetailsService,
            authorizationService,
            refreshLoginUseCase,
            tokenGenerator,
        )
    }

    fun createOAuth2ReissueTokenGrantAuthenticationProvider(
        http: HttpSecurity
    ): OAuth2OriginReissueTokenAuthenticationProvider {
        val authorizationService = CustomOAuth2ConfigurerSupport.getAuthorizationService(http)
        val tokenGenerator = CustomOAuth2ConfigurerSupport.getTokenGenerator(http)

        return OAuth2OriginReissueTokenAuthenticationProvider(
            userDetailsService,
            authorizationService,
            reissueLoginUseCase,
            tokenGenerator,
        )
    }
}
