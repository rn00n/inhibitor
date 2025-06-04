package com.rn00n.inhibitor.application.auth.social.verifier

import com.rn00n.inhibitor.application.auth.exception.ErrorCode
import com.rn00n.inhibitor.application.auth.exception.DefaultOAuth2AuthenticationException
import com.rn00n.inhibitor.application.auth.social.client.apple.AppleClient
import com.rn00n.inhibitor.application.auth.social.model.SocialTokenPayload
import com.rn00n.inhibitor.application.auth.social.model.SocialType
import com.rn00n.inhibitor.application.auth.social.model.SocialUserInfo
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.security.oauth2.core.OAuth2ErrorCodes
import org.springframework.stereotype.Component

@Component
class AppleTokenVerifier(
    private val appleClient: AppleClient
) : SocialTokenVerifier {

    private val logger = KotlinLogging.logger {}

    override fun supports(socialType: SocialType): Boolean = socialType == SocialType.APPLE

    override fun verify(payload: SocialTokenPayload): SocialUserInfo {
        val accessToken = payload.accessToken ?: error("Apple accessToken is required")

        val profile = try {
            appleClient.fetchProfile(accessToken)
        } catch (e: Exception) {
            logger.error(e) { "Apple fetchProfile error" }
            throw DefaultOAuth2AuthenticationException(
                ErrorCode.BAD_REQUEST,
                OAuth2ErrorCodes.INVALID_REQUEST
            )
        }
        return SocialUserInfo(
            providerId = profile.sub,
            email = profile.email,
            name = listOfNotNull(profile.firstName, profile.middleName, profile.lastName).joinToString(" ").ifBlank { profile.name },
            profileImageUrl = null,
            socialType = SocialType.APPLE
        )
    }
}