package com.rn00n.inhibitor.infrastructure.oauth2.social.client.kakao

import com.fasterxml.jackson.annotation.JsonProperty

data class KakaoProfileForResponse(
    val id: Long,
    @JsonProperty("kakao_account")
    val kakaoAccount: KakaoAccount
)
