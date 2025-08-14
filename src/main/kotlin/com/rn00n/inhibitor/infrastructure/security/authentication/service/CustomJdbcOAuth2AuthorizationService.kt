package com.rn00n.inhibitor.infrastructure.security.authentication.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.jdbc.core.JdbcOperations
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository

/**
 * CustomJdbcOAuth2AuthorizationService
 *
 * This class extends JdbcOAuth2AuthorizationService to introduce a custom row mapper,
 * enabling the customization of OAuth2 authorization handling with support for additional configurations.
 * OAuth2AuthorizationRowMapper를 커스터마이징하여 OAuth2 인증 정보 처리를 확장함.
 */
class CustomJdbcOAuth2AuthorizationService(
    jdbcOperations: JdbcOperations,
    registeredClientRepository: RegisteredClientRepository,
    objectMapper: ObjectMapper
) : JdbcOAuth2AuthorizationService(jdbcOperations, registeredClientRepository) {

    init {
        // OAuth2AuthorizationRowMapper 초기화
        val customRowMapper = OAuth2AuthorizationRowMapper(registeredClientRepository)

        // ObjectMapper를 설정하여 JSON 직렬화 및 역직렬화 처리
        customRowMapper.setObjectMapper(objectMapper)

        // JdbcOAuth2AuthorizationService에 커스텀 RowMapper 설정
        this.authorizationRowMapper = customRowMapper
    }
}