package com.rn00n.inhibitor.application.auth.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.rn00n.inhibitor.domain.accounts.Account
import com.rn00n.inhibitor.domain.accounts.AccountStatus
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class UserPrincipal @JsonCreator constructor(
    @JsonProperty("id") val id: Long,
    @JsonProperty("username") private val username: String,
    @JsonProperty("password") private val password: String,
    @JsonProperty("authorities") private val authorities: Collection<GrantedAuthority>,
    @JsonProperty("status") val status: String,
) : UserDetails {

    @JsonIgnore
    var account: Account? = null

    companion object {
        fun fromAccount(user: Account): UserPrincipal {
            return UserPrincipal(
                id = user.id,
                username = user.username,
                password = user.password,
                authorities = listOf(SimpleGrantedAuthority("ROLE_USER")),
                status = user.status.name
            ).apply { this.account = user }
        }
    }

    override fun getUsername(): String = username

    override fun getPassword(): String = password

    override fun getAuthorities(): Collection<GrantedAuthority> = authorities

    @JsonIgnore
    override fun isAccountNonExpired(): Boolean = status != AccountStatus.BLOCK.name

    @JsonIgnore
    override fun isAccountNonLocked(): Boolean = status != AccountStatus.BLOCK.name

    @JsonIgnore
    override fun isCredentialsNonExpired(): Boolean = true

    @JsonIgnore
    override fun isEnabled(): Boolean = status != AccountStatus.BLOCK.name

}