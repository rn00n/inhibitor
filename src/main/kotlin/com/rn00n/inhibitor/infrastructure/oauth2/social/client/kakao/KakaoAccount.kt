package com.rn00n.inhibitor.infrastructure.oauth2.social.client.kakao

data class KakaoAccount(
    val email: String?,
    val ageRange: String?,
    val phoneNumber: String?,
    val profile: KakaoProfileDetail?
)
