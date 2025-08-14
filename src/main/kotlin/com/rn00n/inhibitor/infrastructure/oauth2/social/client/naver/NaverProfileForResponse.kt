package com.rn00n.inhibitor.infrastructure.oauth2.social.client.naver

data class NaverProfileForResponse(
    val resultcode: String?,
    val message: String?,
    val response: NaverResponse?
)
