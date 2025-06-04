package com.rn00n.inhibitor.presentation.backoffice.api.clients.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Access/Refresh Token TTL 설정 요청 DTO")
data class TokenExpiryRequest(
    @field:Schema(description = "Access Token TTL (초 단위)", example = "3600")
    val accessTokenExpiry: Long? = null,
    @field:Schema(description = "Refresh Token TTL (초 단위)", example = "15552000")
    val refreshTokenExpiry: Long? = null,
)
