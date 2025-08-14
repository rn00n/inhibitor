package com.rn00n.inhibitor.infrastructure.oauth2.social.client.google

import com.fasterxml.jackson.databind.ObjectMapper
import com.rn00n.inhibitor.application.auth.exception.ErrorCode
import com.rn00n.inhibitor.application.auth.exception.DefaultOAuth2AuthenticationException
import com.rn00n.inhibitor.application.auth.social.client.SocialClient
import com.rn00n.inhibitor.application.auth.social.model.SocialType
import com.rn00n.inhibitor.commons.webclient.WebClientRequestHelper
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component

@Component
class GoogleClient(
    private val webClientRequestHelper: WebClientRequestHelper,
    private val objectMapper: ObjectMapper,
    @Value("\${socials.google.host}") val host: String,
    @Value("\${socials.google.tokenInfo.path}") val tokenInfoPath: String,
    @Value("\${socials.google.tokenInfo.expectedIssValue}") val expectedIssValue: String
) : SocialClient<GoogleProfile> {

    override fun supports(socialType: SocialType): Boolean = socialType == SocialType.GOOGLE

    override fun fetchProfile(token: String): GoogleProfile {
        val response: ResponseEntity<String> = webClientRequestHelper.get(
            baseUrl = host,
            path = tokenInfoPath,
            params = mapOf("id_token" to token)
        ).block() ?: throw IllegalStateException("Google response is null")

        if (response.statusCode.isError) {
            throw DefaultOAuth2AuthenticationException(ErrorCode.BAD_REQUEST, "invalid_token")
        }

        val body = response.body ?: throw IllegalStateException("Google response body is null")

        val profile = objectMapper.readValue(body, GoogleProfile::class.java)

        // iss 검증
        if (profile.iss.isNullOrBlank() || !profile.iss.contains(expectedIssValue)) {
            throw IllegalStateException("Google iss validation failed: actual=${profile.iss}, expected=$expectedIssValue")
        }

        return profile
    }
}