package com.rn00n.inhibitor.application.auth.social.client.kakao

import com.fasterxml.jackson.databind.ObjectMapper
import com.rn00n.inhibitor.application.auth.exception.ErrorCode
import com.rn00n.inhibitor.application.auth.exception.DefaultOAuth2AuthenticationException
import com.rn00n.inhibitor.application.auth.social.client.SocialClient
import com.rn00n.inhibitor.application.auth.social.model.SocialType
import com.rn00n.inhibitor.commons.webclient.WebClientRequestHelper
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component

@Component
class KakaoClient(
    private val webClientRequestHelper: WebClientRequestHelper,
    private val objectMapper: ObjectMapper
) : SocialClient<KakaoProfile> {

    override fun supports(socialType: SocialType): Boolean = socialType == SocialType.KAKAO

    override fun fetchProfile(token: String): KakaoProfile {
        val response: ResponseEntity<String> = webClientRequestHelper.get(
            baseUrl = "https://kapi.kakao.com",
            path = "/v2/user/me",
            headers = mapOf(HttpHeaders.AUTHORIZATION to "Bearer $token"),
            params = mapOf("secure_resource" to "true")
        ).block() ?: throw IllegalStateException("Kakao response body is null")

        if (response.statusCode.isError) {
            throw DefaultOAuth2AuthenticationException(ErrorCode.BAD_REQUEST, "invalid_token")
        }

        val body = response.body ?: throw IllegalStateException("Kakao response body is null")
        val parsed = objectMapper.readValue(body, KakaoProfileForResponse::class.java)

        return KakaoProfile(
            id = parsed.id.toString(),
            email = parsed.kakaoAccount.email,
            name = parsed.kakaoAccount.profile?.nickName,
            pictureUrl = parsed.kakaoAccount.profile?.profileImageUrl,
            ageRange = parsed.kakaoAccount.ageRange,
            phoneNumber = parsed.kakaoAccount.phoneNumber
        )
    }
}
