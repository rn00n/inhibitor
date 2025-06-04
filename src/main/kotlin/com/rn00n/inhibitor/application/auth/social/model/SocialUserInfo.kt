package com.rn00n.inhibitor.application.auth.social.model

data class SocialUserInfo(
    val providerId: String,
    val email: String?,
    val name: String?,
    val profileImageUrl: String?,
    val socialType: SocialType
)