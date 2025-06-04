package com.rn00n.inhibitor.application.auth.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.rn00n.inhibitor.application.auth.model.principal.UserPrincipal
import com.rn00n.inhibitor.application.auth.model.principal.UserPrincipalMixin
import com.rn00n.inhibitor.application.auth.service.authorizations.CustomJdbcOAuth2AuthorizationService
import com.rn00n.inhibitor.application.auth.service.regesteredclients.CustomJdbcRegisteredClientRepository
import com.rn00n.inhibitor.application.auth.service.regesteredclients.ExtendedRegisteredClientRepository
import com.rn00n.inhibitor.commons.datasources.DataSourceBeanNames.AUTH_JDBC_TEMPLATE
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcOperations
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.jackson2.SecurityJackson2Modules
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings

/**
 * Authorization Server 관련 설정을 정의하는 클래스입니다.
 * OAuth2 Authorization 서버와 관련된 다양한 Bean을 구성합니다.
 */
@Configuration
class AuthorizationServerConfig {

    /**
     * CustomJdbcOAuth2AuthorizationService를 생성합니다.
     *
     * @param jdbcOperations 인증 관련 데이터 접근을 위한 JdbcTemplate
     * @param registeredClientRepository 등록된 OAuth2 클라이언트를 관리하는 리포지토리
     * @param objectMapper JSON 직렬화 및 역직렬화를 위한 ObjectMapper
     * @return OAuth2AuthorizationService 구현체
     *
     * Note: 커스텀 로직에 따라 UserPrincipal 및 관련 Mixin을 SecurityJackson2Modules에 추가합니다.
     */
    @Bean
    fun customJdbcOAuth2AuthorizationService(
        @Qualifier(AUTH_JDBC_TEMPLATE) jdbcOperations: JdbcTemplate,
        registeredClientRepository: RegisteredClientRepository,
        objectMapper: ObjectMapper
    ): OAuth2AuthorizationService {
        return CustomJdbcOAuth2AuthorizationService(
            jdbcOperations,
            registeredClientRepository,
            ObjectMapper().apply {
                // Security 관련 Jackson 역직렬화 모듈을 등록합니다.
                registerModules(SecurityJackson2Modules.getModules(this::class.java.classLoader))
                // UserPrincipal 값 직렬화를 위한 Mixin 설정
                addMixIn(UserPrincipal::class.java, UserPrincipalMixin::class.java)
            }
        )
    }

    /**
     * JdbcRegisteredClientRepository Bean을 생성합니다.
     *
     * @param jdbcOperations 클라이언트 정보를 저장 및 조회하기 위한 JdbcOperations
     * @return RegisteredClientRepository 등록된 클라이언트 정보를 처리하는 Repository Bean
     *
     * 클라이언트 등록과 관련된 데이터베이스 작업을 처리합니다.
     */
    @Bean
    fun registeredClientRepository(@Qualifier(AUTH_JDBC_TEMPLATE) jdbcOperations: JdbcOperations): ExtendedRegisteredClientRepository {
        return CustomJdbcRegisteredClientRepository(jdbcOperations)
    }

    /**
     * AuthorizationServerSettings Bean을 생성합니다.
     *
     * @return AuthorizationServerSettings OAuth2 Authorization 서버 설정을 나타내는 Bean
     *
     * 기본 Authorization Server 설정을 반환하며, 추후 요구사항에 따라 확장 가능합니다.
     */
    @Bean
    fun authorizationServerSettings(): AuthorizationServerSettings {
        return AuthorizationServerSettings.builder().build()
    }

}