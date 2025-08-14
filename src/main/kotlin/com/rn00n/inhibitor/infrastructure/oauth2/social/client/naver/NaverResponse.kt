package com.rn00n.inhibitor.infrastructure.oauth2.social.client.naver

data class NaverResponse(
    val id: String,
    val email: String?,
    val name: String?,
    val nickname: String?,
    val profile_image: String?,
    val age: String?,
    val gender: String?,
    val birthday: String?
)
