package com.rn00n.inhibitor.application.auth.social.verifier

import com.rn00n.inhibitor.application.auth.social.model.SocialTokenPayload
import com.rn00n.inhibitor.application.auth.social.model.SocialType
import com.rn00n.inhibitor.application.auth.social.model.SocialUserInfo

interface SocialTokenVerifier {
    fun supports(socialType: SocialType): Boolean
    fun verify(payload: SocialTokenPayload): SocialUserInfo
}