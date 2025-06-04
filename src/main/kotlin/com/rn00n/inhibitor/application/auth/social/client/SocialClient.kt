package com.rn00n.inhibitor.application.auth.social.client

import com.rn00n.inhibitor.application.auth.social.model.SocialType

interface SocialClient<T> {
    fun supports(socialType: SocialType): Boolean
    fun fetchProfile(token: String): T
}