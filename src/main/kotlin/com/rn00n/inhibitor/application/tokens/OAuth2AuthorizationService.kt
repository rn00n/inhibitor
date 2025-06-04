package com.rn00n.inhibitor.application.tokens

import com.rn00n.inhibitor.domain.accounts.service.AccountService
import com.rn00n.inhibitor.domain.oauth2.service.OAuth2AuthorizationDomainService
import org.springframework.stereotype.Service

@Service
class OAuth2AuthorizationService(
    private val accountService: AccountService,
    private val authorizationService: OAuth2AuthorizationDomainService,
) {

    fun revoke(token: String, tokenTypeHint: String) {
        when (tokenTypeHint) {
            "refresh_token" -> {
                authorizationService.revokeRefreshToken(token)
            }

            "access_token" -> {}
        }
    }

    fun revokeTokensByUsername(username: String) {
        authorizationService.revokeTokensByPrincipalName(username)
    }

    fun revokeTokensByAccountId(accountId: Long) {
        val account = accountService.findById(accountId)
        authorizationService.revokeTokensByPrincipalName(account.username)
    }
}
