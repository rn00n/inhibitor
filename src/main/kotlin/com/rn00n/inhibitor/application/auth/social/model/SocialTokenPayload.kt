package com.rn00n.inhibitor.application.auth.social.model

data class SocialTokenPayload(
    val socialType: SocialType,
    val accessToken: String? = null,
    val idToken: String? = null,
    val refreshToken: String? = null,
    val expiresIn: Long? = null,
    val oauthToken: String? = null,
    val oauthTokenSecret: String? = null
)