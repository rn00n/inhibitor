package com.rn00n.inhibitor.infrastructure.oauth2.social.client.apple

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rn00n.inhibitor.application.auth.social.client.SocialClient
import com.rn00n.inhibitor.application.auth.social.model.SocialType
import org.springframework.stereotype.Component
import java.util.*

@Component
class AppleClient : SocialClient<AppleProfile> {

    override fun supports(socialType: SocialType): Boolean = socialType == SocialType.APPLE

    override fun fetchProfile(token: String): AppleProfile {
        val payload = token.split(".")[1]
        val decoded = String(Base64.getDecoder().decode(payload))
        return jacksonObjectMapper().readValue(decoded, AppleProfile::class.java)
    }
}