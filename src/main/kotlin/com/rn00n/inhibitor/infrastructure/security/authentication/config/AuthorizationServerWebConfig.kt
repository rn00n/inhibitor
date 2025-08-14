package com.rn00n.inhibitor.infrastructure.security.authentication.config

import com.rn00n.inhibitor.infrastructure.security.authentication.grant.registry.AuthenticationGrantBinding
import com.rn00n.inhibitor.infrastructure.security.authentication.grant.registry.GrantTypeRegistry
import com.rn00n.inhibitor.infrastructure.security.authentication.grant.revoke.CustomTokenRevocationAuthenticationProvider
import com.rn00n.inhibitor.infrastructure.security.authentication.handler.DefaultOAuth2AuthenticationFailureHandler
import com.rn00n.inhibitor.infrastructure.security.client.handler.OriginOAuth2ClientAuthenticationFailureHandler
import com.rn00n.inhibitor.infrastructure.security.authentication.oidc.OidcUserInfoMapper
import com.rn00n.inhibitor.commons.filters.RequestTracingFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.context.SecurityContextHolderFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

/**
 * AuthorizationServerWebConfig
 *
 * OAuth2 인증 서버의 설정을 관리하는 주요 구성 클래스입니다.
 * 인증 서버 보안 설정, CORS, CSRF 무시 규칙, 토큰 발급 핸들러 등을 정의합니다.
 */
@Configuration
class AuthorizationServerWebConfig(
    private val authenticationFailureHandler: DefaultOAuth2AuthenticationFailureHandler,
    private val clientAuthenticationFailureHandler: OriginOAuth2ClientAuthenticationFailureHandler,
    private val tokenRevocationAuthenticationProvider: CustomTokenRevocationAuthenticationProvider,
    private val grantTypeRegistry: GrantTypeRegistry,
    private val oidcUserInfoMapper: OidcUserInfoMapper,
) {

    /**
     * CORS 설정
     * API 접근 제어를 위해 CORS 허용 규칙을 정의합니다.
     *
     * Allowed Origins 패턴과 허용 메서드를 맞춰 필요에 따라 수정하세요.
     *
     * @return UrlBasedCorsConfigurationSource
     */
    @Bean
    fun corsConfigurationSource(): UrlBasedCorsConfigurationSource {
        val source = UrlBasedCorsConfigurationSource()
        val config = CorsConfiguration()
        config.allowedOriginPatterns = listOf(
            "http://localhost:8081",
            "http://localhost:8080",
            "https://*.rn00n.com"
        ) // 허용 URL 패턴
        config.allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS") // 허용 메서드
        config.allowedHeaders = listOf("Authorization", "Content-Type") // 허용 헤더
        config.allowCredentials = true // 쿠키 허용
        source.registerCorsConfiguration("/**", config)
        return source
    }

    /**
     * Authorization Server 보안 필터 체인
     * OAuth2 인증 및 보안 필터 체인을 설정합니다.
     *
     * @param http HttpSecurity 객체
     * @return SecurityFilterChain
     */
    @Bean
    @Order(0)
    fun authorizationServerSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        val authorizationServerConfigurer = OAuth2AuthorizationServerConfigurer()

        val bindings = grantTypeRegistry.getBindings(http)
        applyTokenGrants(authorizationServerConfigurer, *bindings.toTypedArray())

        http {
            securityMatcher(authorizationServerConfigurer.endpointsMatcher) // OAuth2 엔드포인트 매칭
            cors { corsConfigurationSource() } // CORS 설정 적용
            csrf { ignoringRequestMatchers("/oauth2/token", "/oauth2/revoke") } // 특정 요청에 대해 CSRF 보호 비활성화
            apply(authorizationServerConfigurer) // OAuth2 인증 서버 구성

            with(authorizationServerConfigurer) {
                oidc(Customizer.withDefaults()) // OpenID Connect (OIDC) 설정
                tokenRevocationEndpoint {
                    it.authenticationProvider(tokenRevocationAuthenticationProvider)
                }
            }

            authorizationServerConfigurer.tokenEndpoint {
                it.errorResponseHandler(authenticationFailureHandler) // 인증 실패 응답 FailureHandler 설정
            }

            authorizationServerConfigurer.oidc {
                it.userInfoEndpoint { endpoint ->
                    endpoint.userInfoMapper(oidcUserInfoMapper)
                }
            }

            authorizationServerConfigurer.clientAuthentication {
                it.errorResponseHandler(clientAuthenticationFailureHandler)
            }
        }

        http.addFilterBefore(RequestTracingFilter(), SecurityContextHolderFilter::class.java)

        return http.build()
    }

    /**
     * Password Grant 방식 설정
     *
     * Authorization Server에서 Password Grant 인증 흐름을 활성화합니다.
     * Access Token 요청 시 추가적인 Converter 및 AuthenticationProvider를 설정합니다.
     *
     * @param authorizationServerConfigurer OAuth2AuthorizationServerConfigurer 객체
     * @param passwordGrantProvider Password Grant 방식의 AuthenticationProvider
     */
    fun applyTokenGrants(
        authorizationServerConfigurer: OAuth2AuthorizationServerConfigurer, vararg configurers: AuthenticationGrantBinding
    ) {
        // 토큰 엔드포인트에 Password Authentication Converter 및 Provider 등록
        authorizationServerConfigurer.tokenEndpoint { tokenEndpoint ->
            configurers.forEach { configurer ->
                tokenEndpoint.accessTokenRequestConverter(configurer.converter)
                tokenEndpoint.authenticationProvider(configurer.provider)
            }
        }
    }
}