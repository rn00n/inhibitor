package com.rn00n.inhibitor.infrastructure.oauth2.social.verifier

import com.rn00n.inhibitor.application.auth.exception.ErrorCode
import com.rn00n.inhibitor.application.auth.exception.DefaultOAuth2AuthenticationException
import com.rn00n.inhibitor.infrastructure.oauth2.social.client.facebook.FacebookClient
import com.rn00n.inhibitor.application.auth.social.model.SocialTokenPayload
import com.rn00n.inhibitor.application.auth.social.model.SocialType
import com.rn00n.inhibitor.application.auth.social.model.SocialUserInfo
import com.rn00n.inhibitor.application.auth.social.verifier.SocialTokenVerifier
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.security.oauth2.core.OAuth2ErrorCodes
import org.springframework.stereotype.Component

@Component
class FacebookTokenVerifier(
    private val facebookClient: FacebookClient
) : SocialTokenVerifier {

    private val logger = KotlinLogging.logger {}

    override fun supports(socialType: SocialType): Boolean = socialType == SocialType.FACEBOOK

    override fun verify(payload: SocialTokenPayload): SocialUserInfo {
        val accessToken = payload.accessToken ?: error("Facebook accessToken is required")

        val profile = try {
            facebookClient.fetchProfile(accessToken)
        } catch (e: Exception) {
            logger.error(e) { "Facebook fetchProfile error" }
            throw DefaultOAuth2AuthenticationException(
                ErrorCode.BAD_REQUEST,
                OAuth2ErrorCodes.INVALID_REQUEST
            )
        }

        return SocialUserInfo(
            providerId = profile.id,
            email = profile.email,
            name = profile.name,
            profileImageUrl = profile.picture?.data?.url,
            socialType = SocialType.FACEBOOK
        )
    }
}