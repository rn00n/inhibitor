package com.rn00n.inhibitor.application.auth.social.model

enum class SocialType {
    KAKAO, GOOGLE, APPLE, FACEBOOK, NAVER, LINE, TWITTER, YAHOOJAPAN;

    fun lowerString(): String {
        return name.lowercase()
    }

    companion object {
        fun valuesOfString(type: String?): SocialType? = entries.firstOrNull {
            it.name.equals(type, ignoreCase = true)
        }
    }
}