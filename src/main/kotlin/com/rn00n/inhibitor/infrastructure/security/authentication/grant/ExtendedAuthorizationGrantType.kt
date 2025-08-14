package com.rn00n.inhibitor.infrastructure.security.authentication.grant

/**
 * 참고 org.springframework.security.oauth2.core.AuthorizationGrantType
 */
enum class ExtendedAuthorizationGrantType(
    val value: String
) {
    ORIGIN_PASSWORD("origin_password"),
    ORIGIN_SOCIAL("origin_social"),
    ORIGIN_REFRESH_TOKEN("origin_refresh_token"),
    ORIGIN_REISSUE_TOKEN("origin_reissue_token"),
    BACKOFFICE("backoffice"),
    PUBLIC("public")
}