package com.rn00n.inhibitor.presentation.backoffice.api.clients.dto

data class RegisteredClientTokenExpiryResponse(
    val clientId: String,
    val accessTokenExpiry: Long? = null,
    val refreshTokenExpiry: Long? = null,
)