package com.rn00n.inhibitor.infrastructure.oauth2.social.client.google

data class GoogleProfile(
    val sub: String,
    val email: String?,
    val name: String?,
    val givenName: String?,
    val familyName: String?,
    val picture: String?,
    val locale: String?,
    val iss: String?
)