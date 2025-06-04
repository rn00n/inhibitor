package com.rn00n.inhibitor.application.auth.social.client.kakao

data class KakaoProfile(
    val id: String,
    val email: String?,
    val name: String?,
    val pictureUrl: String?,
    val ageRange: String?,
    val phoneNumber: String?
)