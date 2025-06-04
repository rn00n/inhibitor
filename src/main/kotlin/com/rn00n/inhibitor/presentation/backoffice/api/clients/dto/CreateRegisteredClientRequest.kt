package com.rn00n.inhibitor.presentation.backoffice.api.clients.dto

data class CreateRegisteredClientRequest(
    val clientId: String,
    val clientSecret: String,
    val scopes: Set<String>,
    val accessTokenExpiredSecond: Long,
    val refreshTokenExpiredSecond: Long,
)
