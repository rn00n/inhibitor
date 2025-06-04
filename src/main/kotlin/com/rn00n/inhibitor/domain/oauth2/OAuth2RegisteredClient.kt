package com.rn00n.inhibitor.domain.oauth2

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "oauth2_registered_client")
class OAuth2RegisteredClient(

    @Id
    @Column(length = 100)
    val id: String,

    @Column(name = "client_id", nullable = false, length = 100)
    val clientId: String,

    @Column(name = "client_id_issued_at", nullable = false)
    val clientIdIssuedAt: Instant = Instant.now(),

    @Column(name = "client_secret", length = 200)
    val clientSecret: String? = null,

    @Column(name = "client_secret_expires_at")
    val clientSecretExpiresAt: Instant? = null,

    @Column(name = "client_name", nullable = false, length = 200)
    val clientName: String,

    @Column(name = "client_authentication_methods", nullable = false, length = 1000)
    val clientAuthenticationMethods: String,

    @Column(name = "authorization_grant_types", nullable = false, length = 1000)
    val authorizationGrantTypes: String,

    @Column(name = "redirect_uris", length = 1000)
    val redirectUris: String? = null,

    @Column(name = "post_logout_redirect_uris", length = 1000)
    val postLogoutRedirectUris: String? = null,

    @Column(nullable = false, length = 1000)
    val scopes: String,

    @Column(name = "client_settings", nullable = false, length = 2000)
    val clientSettings: String,

    @Column(name = "token_settings", nullable = false, length = 2000)
    val tokenSettings: String
)