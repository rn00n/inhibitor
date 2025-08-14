package com.rn00n.inhibitor.infrastructure.security.authentication.support

import com.rn00n.inhibitor.infrastructure.security.authentication.token.CustomOAuth2TokenCustomizers
import com.rn00n.inhibitor.infrastructure.security.authentication.token.DefaultJwtGenerator
import com.nimbusds.jose.jwk.source.JWKSource
import com.nimbusds.jose.proc.SecurityContext
import org.springframework.beans.factory.BeanFactoryUtils
import org.springframework.beans.factory.NoUniqueBeanDefinitionException
import org.springframework.context.ApplicationContext
import org.springframework.core.ResolvableType
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.oauth2.core.OAuth2Token
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationService
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService
import org.springframework.security.oauth2.server.authorization.token.*
import org.springframework.util.StringUtils

/**
 * Utility class for configuring custom components in OAuth2 서버 환경에서 사용되는 구성 요소를 초기화하고 관리하는 유틸리티 클래스입니다.
 */
class CustomOAuth2ConfigurerSupport {

    companion object {

        /**
         * Retrieves or initializes an OAuth2AuthorizationService.
         * OAuth2 인증 서비스 객체를 가져오거나 초기화합니다.
         *
         * @param httpSecurity Spring Security 환경 설정 객체.
         * @return OAuth2AuthorizationService 객체.
         */
        fun getAuthorizationService(httpSecurity: HttpSecurity): OAuth2AuthorizationService {
            // HTTP 공유 객체에서 OAuth2AuthorizationService 가져오기
            var authorizationService = httpSecurity.getSharedObject(OAuth2AuthorizationService::class.java)
            if (authorizationService == null) {
                // 빈(Bean) 옵션에서 검색, 없으면 InMemoryOAuth2AuthorizationService로 초기화
                authorizationService = getOptionalBean(httpSecurity, OAuth2AuthorizationService::class.java)
                    ?: InMemoryOAuth2AuthorizationService()
                httpSecurity.setSharedObject(OAuth2AuthorizationService::class.java, authorizationService)
            }
            return authorizationService
        }

        /**
         * Retrieves or initializes an OAuth2TokenGenerator.
         * OAuth2 토큰 생성기를 가져오거나 초기화합니다.
         *
         * @param httpSecurity Spring Security 환경 설정 객체.
         * @return OAuth2TokenGenerator<out OAuth2Token> 객체.
         */
        fun getTokenGenerator(httpSecurity: HttpSecurity): OAuth2TokenGenerator<out OAuth2Token> {
            // HTTP 공유 객체에서 OAuth2TokenGenerator 가져오기
            var tokenGenerator = httpSecurity.getSharedObject(OAuth2TokenGenerator::class.java)
            if (tokenGenerator == null) {
                // 빈(Bean) 옵션에서 검색. 없으면 구성 요소 초기화 및 설정
                tokenGenerator = getOptionalBean(httpSecurity, OAuth2TokenGenerator::class.java)
                if (tokenGenerator == null) {
                    val jwtGenerator = getJwtGenerator(httpSecurity) // JWT 토큰 생성기 초기화
                    val accessTokenGenerator = OAuth2AccessTokenGenerator().apply {
                        this.setAccessTokenCustomizer(getAccessTokenCustomizer(httpSecurity)) // AccessToken 커스터마이저 설정
                    }
                    val refreshTokenGenerator = OAuth2RefreshTokenGenerator() // RefreshToken 생성기
                    tokenGenerator = jwtGenerator?.let {
                        DelegatingOAuth2TokenGenerator(it, accessTokenGenerator, refreshTokenGenerator)
                    } ?: DelegatingOAuth2TokenGenerator(accessTokenGenerator, refreshTokenGenerator)
                }
                httpSecurity.setSharedObject(OAuth2TokenGenerator::class.java, tokenGenerator)
            }
            return tokenGenerator
        }

        /**
         * Searches for an optional Spring bean and retrieves it if available.
         * 특정 타입의 Bean을 검색하여 반환합니다. Bean이 다수일 경우 예외를 발생시킵니다.
         *
         * @param httpSecurity Spring Security 환경설정 객체.
         * @param type 검색할 Bean의 타입.
         * @return 검색된 Bean 객체 또는 null.
         */
        private fun <T : Any> getOptionalBean(httpSecurity: HttpSecurity, type: Class<T>): T? {
            val applicationContext: ApplicationContext = httpSecurity.getSharedObject(ApplicationContext::class.java)
            val beansMap: Map<String, T> = BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, type)

            // 다수의 Bean이 일치하는 경우 오류 발생
            if (beansMap.size > 1) {
                throw NoUniqueBeanDefinitionException(
                    type, beansMap.size,
                    "Expected single matching bean of type '" + type.name + "' but found " + beansMap.size + ": "
                            + StringUtils.collectionToCommaDelimitedString(beansMap.keys)
                )
            }
            return beansMap.values.firstOrNull()
        }

        /**
         * AccessToken 커스터마이저의 기본 설정을 가져옵니다.
         * 또한 Optional Bean으로 설정된 커스터마이저를 포함하여 동작을 확장합니다.
         */
        private fun getAccessTokenCustomizer(httpSecurity: HttpSecurity): OAuth2TokenCustomizer<OAuth2TokenClaimsContext> {
            val defaultAccessTokenCustomizer =
                CustomOAuth2TokenCustomizers.accessTokenCustomizer()

            val type: ResolvableType = ResolvableType.forClassWithGenerics(
                OAuth2TokenCustomizer::class.java,
                OAuth2TokenClaimsContext::class.java
            )

            val accessTokenCustomizer = getOptionalBean(httpSecurity, type) as? OAuth2TokenCustomizer<OAuth2TokenClaimsContext>

            // 기본 커스터마이저와 사용자 정의 커스터마이저를 묶어서 동작시키기 위한 래핑
            return accessTokenCustomizer?.let { customizer ->
                OAuth2TokenCustomizer { context ->
                    defaultAccessTokenCustomizer.customize(context)
                    customizer.customize(context)
                }
            } ?: defaultAccessTokenCustomizer
        }

        /**
         * Searches for an optional bean using a specific ResolvableType.
         * Generic 타입을 포함한 Bean 검색용 메서드.
         */
        private fun <T> getOptionalBean(httpSecurity: HttpSecurity, type: ResolvableType): T? {
            val context: ApplicationContext = httpSecurity.getSharedObject(ApplicationContext::class.java)
            val beanNames: Array<String> = context.getBeanNamesForType(type)

            if (beanNames.size > 1) {
                throw NoUniqueBeanDefinitionException(type.toClass(), *beanNames)
            }

            return if (beanNames.size == 1) {
                @Suppress("UNCHECKED_CAST")
                context.getBean(beanNames[0]) as T
            } else {
                null
            }
        }

        /**
         * JWT Generator를 가져오거나 초기화하여 반환합니다.
         */
        fun getJwtGenerator(httpSecurity: HttpSecurity): OAuth2TokenGenerator<Jwt>? {
            val jwtGenerator = httpSecurity.getSharedObject(JwtGenerator::class.java)

            val jwtEncoder = getJwtEncoder(httpSecurity)
            if (jwtEncoder != null) {
                val defaultJwtGenerator = DefaultJwtGenerator(jwtEncoder, getJwtCustomizer(httpSecurity))
                httpSecurity.setSharedObject(DefaultJwtGenerator::class.java, defaultJwtGenerator)
                return defaultJwtGenerator
            }

            return jwtGenerator
        }

        /**
         * JWT Encoder를 가져오거나 초기화합니다.
         * @param httpSecurity Spring Security 환경 설정 객체.
         * @return JwtEncoder 객체 또는 null.
         */
        fun getJwtEncoder(httpSecurity: HttpSecurity): JwtEncoder? {
            var jwtEncoder = httpSecurity.getSharedObject(JwtEncoder::class.java)
            if (jwtEncoder == null) {
                val jwkSource = getJwkSource(httpSecurity) // JWK 소스 가져오기
                if (jwkSource != null) {
                    jwtEncoder = NimbusJwtEncoder(jwkSource)
                }
                if (jwtEncoder != null) {
                    httpSecurity.setSharedObject(JwtEncoder::class.java, jwtEncoder)
                }
            }
            return jwtEncoder
        }

        /**
         * JWK (JSON Web Key) 소스를 가져오거나 초기화합니다.
         * @return JWKSource<SecurityContext> 객체.
         */
        fun getJwkSource(httpSecurity: HttpSecurity): JWKSource<SecurityContext>? {
            val sharedJwkSource = httpSecurity.getSharedObject(JWKSource::class.java)
            if (sharedJwkSource is JWKSource<*>) {
                @Suppress("UNCHECKED_CAST")
                return sharedJwkSource as JWKSource<SecurityContext>
            }

            val type = ResolvableType.forClassWithGenerics(JWKSource::class.java, SecurityContext::class.java)
            val optionalBean = getOptionalBean(httpSecurity, type) as? JWKSource<SecurityContext>
            if (optionalBean is JWKSource<*>) {
                httpSecurity.setSharedObject(JWKSource::class.java, optionalBean)
                return optionalBean
            }

            return null
        }

        /**
         * JWT 커스터마이저를 가져오며, 기본 커스터마이저와 확장 커스터마이저를 포함합니다.
         */
        private fun getJwtCustomizer(httpSecurity: HttpSecurity): OAuth2TokenCustomizer<JwtEncodingContext> {
            val defaultJwtCustomizer = CustomOAuth2TokenCustomizers.jwtCustomizer()
            val type = ResolvableType.forClassWithGenerics(OAuth2TokenCustomizer::class.java, JwtEncodingContext::class.java)

            val jwtCustomizer: OAuth2TokenCustomizer<JwtEncodingContext>? = getOptionalBean(httpSecurity, type)

            // 기본 커스터마이저와 사용자 정의 커스터마이저를 함께 동작
            return if (jwtCustomizer == null) {
                defaultJwtCustomizer
            } else {
                OAuth2TokenCustomizer { context ->
                    defaultJwtCustomizer.customize(context)
                    jwtCustomizer.customize(context)
                }
            }
        }
    }
}