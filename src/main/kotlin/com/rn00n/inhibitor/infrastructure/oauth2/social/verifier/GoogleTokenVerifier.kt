package com.rn00n.inhibitor.infrastructure.oauth2.social.verifier

import com.rn00n.inhibitor.application.auth.exception.ErrorCode
import com.rn00n.inhibitor.application.auth.exception.DefaultOAuth2AuthenticationException
import com.rn00n.inhibitor.infrastructure.oauth2.social.client.google.GoogleClient
import com.rn00n.inhibitor.application.auth.social.model.SocialTokenPayload
import com.rn00n.inhibitor.application.auth.social.model.SocialType
import com.rn00n.inhibitor.application.auth.social.model.SocialUserInfo
import com.rn00n.inhibitor.application.auth.social.verifier.SocialTokenVerifier
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.security.oauth2.core.OAuth2ErrorCodes
import org.springframework.stereotype.Component

@Component
class GoogleTokenVerifier(
    private val googleClient: GoogleClient
) : SocialTokenVerifier {

    private val logger = KotlinLogging.logger {}

    override fun supports(socialType: SocialType): Boolean = socialType == SocialType.GOOGLE

    override fun verify(payload: SocialTokenPayload): SocialUserInfo {
        val idToken = payload.accessToken ?: throw IllegalArgumentException("idToken is required")

        val profile = try {
            googleClient.fetchProfile(idToken)
        } catch (e: Exception) {
            logger.error(e) { "Google fetchProfile error" }
            throw DefaultOAuth2AuthenticationException(
                ErrorCode.BAD_REQUEST,
                OAuth2ErrorCodes.INVALID_REQUEST
            )
        }

        return SocialUserInfo(
            providerId = profile.sub,
            email = profile.email,
            name = profile.name,
            profileImageUrl = profile.picture,
            socialType = SocialType.GOOGLE
        )
    }

}