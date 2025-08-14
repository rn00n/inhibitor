package com.rn00n.inhibitor.infrastructure.oauth2.social.client.naver

data class NaverProfile(
    val id: String,
    val email: String?,
    val name: String?,
    val nickName: String?,
    val profileImageUrl: String?,
    val age: String?,
    val gender: String?,
    val birth: String?
)
