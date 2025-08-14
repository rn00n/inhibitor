package com.rn00n.inhibitor.application.auth.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.security.core.GrantedAuthority

@Suppress("UNUSED_PARAMETER")
abstract class UserPrincipalMixin @JsonCreator constructor(
    @JsonProperty("id") id: Long,
    @JsonProperty("username") username: String,
    @JsonProperty("password") password: String,
    @JsonProperty("authorities") authorities: Collection<GrantedAuthority>,
    @JsonProperty("status") status: String?
)