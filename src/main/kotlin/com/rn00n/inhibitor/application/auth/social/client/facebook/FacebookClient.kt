package com.rn00n.inhibitor.application.auth.social.client.facebook

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
class FacebookClient(
    private val webClientRequestHelper: WebClientRequestHelper,
    private val objectMapper: ObjectMapper,
    @Value("\${socials.facebook.host}") val host: String,
    @Value("\${socials.facebook.permission.path}") val permissionPath: String,
    @Value("\${socials.facebook.profile.path}") val profilePath: String,
    @Value("\${socials.facebook.profile.params}") val profileParams: String,
) : SocialClient<FacebookProfile> {

    override fun supports(socialType: SocialType): Boolean = socialType == SocialType.FACEBOOK

    override fun fetchProfile(token: String): FacebookProfile {
        // 권한 체크가 필요하다면 아래 주석을 해제
        // verifyPermissions(token)

        val response: ResponseEntity<String> = webClientRequestHelper.get(
            baseUrl = host,
            path = profilePath,
            params = mapOf(
                "fields" to profileParams.split("=")[1],
                "access_token" to token,
            )
        ).block() ?: error("Facebook profile response is null")

        if (response.statusCode.isError) {
            throw DefaultOAuth2AuthenticationException(ErrorCode.BAD_REQUEST, "invalid_token")
        }

        val body = response.body ?: error("Facebook profile body is null")
        return objectMapper.readValue(body, FacebookProfile::class.java)
    }

    private fun verifyPermissions(token: String) {
        val response: ResponseEntity<String> = webClientRequestHelper.get(
            baseUrl = host,
            path = permissionPath,
            params = mapOf("access_token" to token)
        ).block() ?: error("Facebook permission response is null")

        val body = response.body ?: error("Facebook permission body is null")
        val permission = objectMapper.readValue(body, FacebookPermission::class.java)

        if (permission.data.isEmpty() || permission.data.any { it.status == "declined" }) {
            error("Facebook permission declined")
        }
    }
}