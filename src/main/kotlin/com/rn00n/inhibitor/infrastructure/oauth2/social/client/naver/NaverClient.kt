package com.rn00n.inhibitor.infrastructure.oauth2.social.client.naver

import com.fasterxml.jackson.databind.ObjectMapper
import com.rn00n.inhibitor.application.auth.exception.ErrorCode
import com.rn00n.inhibitor.application.auth.exception.DefaultOAuth2AuthenticationException
import com.rn00n.inhibitor.application.auth.social.client.SocialClient
import com.rn00n.inhibitor.application.auth.social.model.SocialType
import com.rn00n.inhibitor.commons.webclient.WebClientRequestHelper
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component

@Component
class NaverClient(
    private val webClientRequestHelper: WebClientRequestHelper,
    private val objectMapper: ObjectMapper,
    @Value("\${socials.naver.host}") val host: String,
    @Value("\${socials.naver.profile.path}") val profilePath: String,
) : SocialClient<NaverProfile> {

    override fun supports(socialType: SocialType): Boolean = socialType == SocialType.NAVER

    override fun fetchProfile(token: String): NaverProfile {
        val response: ResponseEntity<String> = webClientRequestHelper.get(
            baseUrl = host,
            path = profilePath,
            headers = mapOf(HttpHeaders.AUTHORIZATION to "Bearer $token")
        ).block() ?: throw IllegalStateException("Naver response is null")

        if (response.statusCode.isError) {
            throw DefaultOAuth2AuthenticationException(ErrorCode.BAD_REQUEST, "invalid_token")
        }

        val body = response.body ?: throw IllegalStateException("Naver response body is null")
        val parsed = objectMapper.readValue(body, NaverProfileForResponse::class.java)

        val r = parsed.response ?: throw IllegalStateException("Naver response missing")

        return NaverProfile(
            id = r.id,
            email = r.email,
            name = r.name,
            nickName = r.nickname,
            profileImageUrl = r.profile_image,
            age = r.age,
            gender = r.gender,
            birth = r.birthday
        )
    }
}