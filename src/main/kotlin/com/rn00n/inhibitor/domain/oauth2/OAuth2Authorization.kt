package com.rn00n.inhibitor.domain.oauth2

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(
    name = "oauth2_authorization",
    indexes = [
        Index(name = "idx_auth_registered_client_id", columnList = "registered_client_id"),
        Index(name = "idx_auth_principal_name", columnList = "principal_name"),
        Index(name = "idx_auth_refresh_token_expires_at", columnList = "refresh_token_expires_at"),
        Index(name = "idx_auth_refresh_token_value", columnList = "refresh_token_value")
    ]
)
class OAuth2Authorization(

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: String? = null,

    @Column(name = "registered_client_id", nullable = false)
    val registeredClientId: String,

    @Column(name = "principal_name", nullable = false)
    val principalName: String,

    @Column(name = "authorization_grant_type", nullable = false)
    val authorizationGrantType: String,

    @Lob
    @Column(name = "access_token_metadata", columnDefinition = "BLOB")
    val accessTokenMetadata: String? = null,

    @Lob
    @Column(name = "attributes", columnDefinition = "BLOB")
    val attributes: String? = null,

    @Lob
    @Column(name = "refresh_token_value", columnDefinition = "BLOB")
    val refreshTokenValue: String? = null,

    @Column(name = "refresh_token_issued_at")
    val refreshTokenIssuedAt: Instant? = null,

    @Column(name = "refresh_token_expires_at")
    val refreshTokenExpiresAt: Instant? = null
)
