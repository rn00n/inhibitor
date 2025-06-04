package com.rn00n.inhibitor.presentation.inhibitor.api.auth.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "OAuth2 토큰 발급 요청 DTO (grant_type에 따라 조건부로 필드를 사용합니다)")
data class GrantTokenRequest(
    @Schema(
        description = "Grant 타입",
        required = true,
        example = "origin_password",
        allowableValues = ["origin_password", "origin_social", "origin_refresh_token"]
    )
    val grant_type: String,

    @Schema(
        description = "사용자 이메일 (grant_type=origin_password 일 때 필수)",
        example = "user@rn00n.com"
    )
    val username: String? = null,

    @Schema(
        description = "사용자 비밀번호 (grant_type=origin_password 일 때 필수)",
        example = "secret"
    )
    val password: String? = null,

    @Schema(
        description = "소셜 로그인 idToken (grant_type=origin_social 일 때 사용)",
        example = "id-token-abc"
    )
    val idToken: String? = null,

    @Schema(
        description = "소셜 accessToken (grant_type=origin_social 일 때 사용)",
        example = "access-token-abc"
    )
    val accessToken: String? = null,

    @Schema(
        description = "소셜 refreshToken (grant_type=origin_social 일 때 사용)",
        example = "refresh-token-abc"
    )
    val refreshToken: String? = null,

    @Schema(
        description = "소셜 accessToken 만료 시간 (초 단위)",
        example = "3600"
    )
    val expires: Long? = null,

    @Schema(
        description = "OAuth 1.0 방식용 oauthToken (grant_type=origin_social 일 때 사용)",
        example = "oauth-token-abc"
    )
    val oauthToken: String? = null,

    @Schema(
        description = "OAuth 1.0 방식용 oauthTokenSecret (grant_type=origin_social 일 때 사용)",
        example = "oauth-token-secret"
    )
    val oauthTokenSecret: String? = null,

    @Schema(
        description = "소셜 로그인 종류 (예: kakao, apple 등)",
        example = "kakao"
    )
    val socialType: String? = null,

    // ===== refresh_token 전용 =====
    @Schema(
        description = "Refresh Token (grant_type=refresh_token 일 때 필수)",
        example = "QWY2OEZUMDZNUjM3MjlRU0NKS0IzWFI5a3pB..."
    )
    val refresh_token: String? = null,

    // ===== 공통 필드 =====
    @Schema(
        description = "권한 범위 (예: admin)",
        example = "admin"
    )
    val scope: String? = null,

    @Schema(
        description = "요청 플랫폼 (web, mweb, appweb, android, ios)",
        example = "web"
    )
    val platform: String? = null,

    @Schema(
        description = "스토어 정보 (예: google, apple)",
        example = "google"
    )
    val store: String? = null,

    @Schema(
        description = "디바이스 식별자",
        example = "device123"
    )
    val deviceId: String? = null,

    @Schema(
        description = "언어 설정 (예: ko-KR, en-US)",
        example = "ko-KR"
    )
    val locale: String? = null,

    @Schema(
        description = "국가 코드 (예: KR, US)",
        example = "KR"
    )
    val countryCode: String? = null
)