package com.rn00n.inhibitor.infrastructure.security.authentication.repository

import com.rn00n.inhibitor.application.auth.service.regesteredclients.ExtendedRegisteredClientRepository
import org.springframework.jdbc.core.JdbcOperations
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient

class CustomJdbcRegisteredClientRepository(
    jdbcOperations: JdbcOperations
) : JdbcRegisteredClientRepository(jdbcOperations), ExtendedRegisteredClientRepository {

    companion object {
        private const val SELECT_ALL_SQL =
            "SELECT id, client_id, client_id_issued_at, client_secret, client_secret_expires_at, client_name, client_authentication_methods, authorization_grant_types, redirect_uris, post_logout_redirect_uris, scopes, client_settings, token_settings FROM oauth2_registered_client"
    }

    override fun findAll(): List<RegisteredClient> {
        return this.jdbcOperations.query(
            SELECT_ALL_SQL,
            this.registeredClientRowMapper
        )
    }
}