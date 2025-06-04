package com.rn00n.inhibitor.application.auth.social.verifier

import com.rn00n.inhibitor.application.auth.social.model.SocialTokenPayload
import com.rn00n.inhibitor.application.auth.social.model.SocialUserInfo
import org.springframework.stereotype.Component

@Component
class SocialTokenVerifierDispatcher(
    private val verifiers: List<SocialTokenVerifier>
) {
    fun verify(payload: SocialTokenPayload): SocialUserInfo {
        return verifiers.find { it.supports(payload.socialType) }
            ?.verify(payload)
            ?: throw IllegalArgumentException("Unsupported social type: ${payload.socialType}")
    }
}