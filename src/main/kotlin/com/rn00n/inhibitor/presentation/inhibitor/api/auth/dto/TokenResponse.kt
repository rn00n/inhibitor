package com.rn00n.inhibitor.presentation.inhibitor.api.auth.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "토큰 발급 성공 시 응답 모델")
data class TokenResponse(
    @Schema(description = "Access Token (JWT)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    val access_token: String,

    @Schema(description = "Refresh Token (opaque string)", example = "abc123...xyz")
    val refresh_token: String,

    @Schema(description = "클라이언트 ID", example = "admin")
    val clientId: String,

    @Schema(description = "요청된 권한 범위", example = "admin")
    val scope: String,

    @Schema(description = "토큰 타입", example = "Bearer")
    val token_type: String,

    @Schema(description = "access_token 만료까지 남은 초", example = "7199")
    val expires_in: Long,

    @Schema(description = "유저 ID", example = "6433700666671104")
    val userId: Long
)
