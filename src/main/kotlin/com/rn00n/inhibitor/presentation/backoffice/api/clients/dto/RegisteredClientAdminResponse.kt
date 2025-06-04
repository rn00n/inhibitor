package com.rn00n.inhibitor.presentation.backoffice.api.clients.dto

data class RegisteredClientAdminResponse(
    val id: String,
    val clientId: String,
    val accessTokenTimeToLive: Long? = null,
    val refreshTokenTimeToLive: Long? = null,
)
