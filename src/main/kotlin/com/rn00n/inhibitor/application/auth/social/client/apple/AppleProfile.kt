package com.rn00n.inhibitor.application.auth.social.client.apple

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class AppleProfile(
    val sub: String,
    val email: String?,
    val name: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val middleName: String? = null
)