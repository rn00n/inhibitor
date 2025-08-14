package com.rn00n.inhibitor.infrastructure.oauth2.social.client.facebook

import com.fasterxml.jackson.annotation.JsonProperty

data class FacebookPermission(
    @JsonProperty("data")
    val data: List<Permission>
) {
    data class Permission(
        @JsonProperty("permission")
        val permission: String,

        @JsonProperty("status")
        val status: String
    )
}