package com.rn00n.inhibitor.presentation.backoffice.api.clients

import com.rn00n.inhibitor.application.client.OAuth2ClientRegistrationManager
import com.rn00n.inhibitor.commons.base.PageResponse
import com.rn00n.inhibitor.presentation.backoffice.api.clients.dto.CreateRegisteredClientRequest
import com.rn00n.inhibitor.presentation.backoffice.api.clients.dto.RegisteredClientTokenExpiryResponse
import com.rn00n.inhibitor.presentation.backoffice.api.clients.dto.TokenExpiryRequest
import io.swagger.v3.oas.annotations.Hidden
import org.springframework.http.ResponseEntity
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient
import org.springframework.web.bind.annotation.*
import java.time.Duration

@Hidden
@RestController
@RequestMapping("/backoffice/api/registered-clients")
class RegisteredClientAdminController(
    private val clientRegistrationManager: OAuth2ClientRegistrationManager,
) {

    @PostMapping
    fun create(
        @RequestBody createRegisteredClientRequest: CreateRegisteredClientRequest,
    ): ResponseEntity<Any> {
        clientRegistrationManager.initRegisteredClient(
            createRegisteredClientRequest.clientId,
            createRegisteredClientRequest.clientSecret,
            createRegisteredClientRequest.scopes,
            Duration.ofSeconds(createRegisteredClientRequest.accessTokenExpiredSecond),
            Duration.ofSeconds(createRegisteredClientRequest.refreshTokenExpiredSecond),
        )
        return ResponseEntity.ok().build()
    }

    @GetMapping
    fun findAll(
    ): ResponseEntity<PageResponse<RegisteredClientTokenExpiryResponse>> {
        val clients: List<RegisteredClient> = clientRegistrationManager.findAll()
        return ResponseEntity.ok(PageResponse.ofList(clients.map { it.toExpiryResponse() }))
    }

    @PatchMapping("/{clientId}/refresh-token-expiry")
    fun updateRegisteredClientRefreshTokenExpiry(
        @PathVariable clientId: String,
        @RequestBody tokenExpiry: TokenExpiryRequest,
    ): ResponseEntity<RegisteredClientTokenExpiryResponse> {
        val registeredClient = clientRegistrationManager.updateRegisteredClientRefreshTokenExpiry(clientId, tokenExpiry.refreshTokenExpiry)
        return ResponseEntity.ok(registeredClient?.toExpiryResponse())
    }

    @PatchMapping("/{clientId}/token-expiry")
    fun updateRegisteredClientTokenExpiry(
        @PathVariable clientId: String,
        @RequestBody tokenExpiry: TokenExpiryRequest,
    ): ResponseEntity<RegisteredClientTokenExpiryResponse> {
        val accessTokenExpiry = tokenExpiry.accessTokenExpiry
        val refreshTokenExpiry = tokenExpiry.refreshTokenExpiry
        val registeredClient = clientRegistrationManager.updateRegisteredClientTokenExpiry(clientId, accessTokenExpiry, refreshTokenExpiry)
        return ResponseEntity.ok(registeredClient?.toExpiryResponse())
    }

}

private fun RegisteredClient.toExpiryResponse(): RegisteredClientTokenExpiryResponse {
    return RegisteredClientTokenExpiryResponse(
        clientId = this.clientId,
        accessTokenExpiry = this.tokenSettings.accessTokenTimeToLive.seconds,
        refreshTokenExpiry = this.tokenSettings.refreshTokenTimeToLive.seconds,
    )
}
