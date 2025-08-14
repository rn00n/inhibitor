package com.rn00n.inhibitor.infrastructure.oauth2.social.client.facebook

data class FacebookProfile(
    val id: String,
    val email: String?,
    val name: String?,
    val firstName: String?,
    val lastName: String?,
    val middleName: String?,
    val picture: PictureWrapper?,
    val ageRange: String?,
    val gender: String?,
    val locale: String?,
    val link: String?
)

data class PictureWrapper(
    val data: PictureData?
)

data class PictureData(
    val url: String?
)