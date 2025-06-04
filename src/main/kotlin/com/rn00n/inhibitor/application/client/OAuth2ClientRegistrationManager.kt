package com.rn00n.inhibitor.application.client

import com.rn00n.inhibitor.application.auth.service.regesteredclients.ExtendedRegisteredClientRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.ClientAuthenticationMethod
import org.springframework.security.oauth2.core.oidc.OidcScopes
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.*

@Service
class OAuth2ClientRegistrationManager(
    private val registeredClientRepository: ExtendedRegisteredClientRepository,
    private val passwordEncoder: PasswordEncoder,
) {

    fun getAvailableClient(): List<RegisteredClient> {
        val list = mutableListOf<RegisteredClient>()
        list.addAll(
            listOfNotNull(
                registeredClientRepository.findByClientId("inhibitor"),
                registeredClientRepository.findByClientId("backoffice")
            )
        )

        return list
    }

    /**
     * Register or update an OAuth2 client.
     * OAuth2 클라이언트를 초기화하거나 업데이트합니다.
     *
     * @param clientId 클라이언트 ID
     * @param clientSecret 클라이언트 시크릿
     * @param scopes 클라이언트가 액세스 가능한 스코프
     * @param accessTokenTimeToLive 액세스 토큰 만료 시간 (Default: 2시간)
     * @param refreshTokenTimeToLive 리프레시 토큰 만료 시간 (Default: 1일)
     */
    @Suppress("DEPRECATION")
    fun initRegisteredClient(
        clientId: String,
        clientSecret: String,
        scopes: Set<String>,
        accessTokenTimeToLive: Duration = Duration.ofHours(2),
        refreshTokenTimeToLive: Duration = Duration.ofDays(1),
    ) {
        // Retrieve an existing client if it exists, otherwise prepare a new client.
        // 클라이언트가 기존에 존재하면 가져오고, 없을 경우 새로 준비합니다.
        val existingClient = registeredClientRepository.findByClientId(clientId)

        // Create or update the client configuration.
        // 클라이언트를 생성하거나 업데이트합니다.
        val updatedClient =
            (existingClient?.let { RegisteredClient.from(it) } ?: RegisteredClient.withId(UUID.randomUUID().toString())).apply {
                clientId(clientId) // 클라이언트 ID 설정
                clientSecret(passwordEncoder.encode(clientSecret)) // 패스워드 암호화 및 저장
                clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC) // 클라이언트 인증 메서드
                authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE) // Authorization Code Grant 방식 지원
                authorizationGrantType(AuthorizationGrantType.PASSWORD) // Password Grant 방식 추가
                authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN) // Refresh Token Grant 방식 지원
                redirectUri("http://127.0.0.1:8080/login") // 인증 후 리다이렉션 URL
                postLogoutRedirectUri("http://127.0.0.1:8080/") // 로그인 제거 후 URL
                scope(OidcScopes.OPENID) // OpenID 스코프 설정
                scope(OidcScopes.PROFILE) // 프로필 스코프 설정
                scopes.forEach { scope(it) } // 추가 스코프 설정

                // 클라이언트 설정 (사용자 동의 필요)
                clientSettings(
                    ClientSettings.builder()
                        .requireAuthorizationConsent(true)
                        .build()
                )
                // 토큰 설정 (갱신 토큰 재사용 금지, 만료시간 설정)
                tokenSettings(
                    TokenSettings.builder()
                        .reuseRefreshTokens(false)
                        .accessTokenTimeToLive(accessTokenTimeToLive) // 엑세스토큰 만료시간 설정
                        .refreshTokenTimeToLive(refreshTokenTimeToLive) // 리프레시토큰 만료시간 설정
                        .build()
                )
            }.build()

        // Save the client to the repository.
        // 레포지토리에 클라이언트를 저장합니다.
        registeredClientRepository.save(updatedClient)
    }

    fun updateRegisteredClientAccessTokenExpiry(clientId: String, accessTokenExpiry: Long?): RegisteredClient? {
        return registeredClientRepository.findByClientId(clientId)?.let {
            RegisteredClient.from(it)
                .tokenSettings(
                    TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofSeconds(accessTokenExpiry ?: it.tokenSettings.accessTokenTimeToLive.seconds))
                        .build()
                )
                .build()
        }.apply {
            registeredClientRepository.save(this)
        } ?: throw RuntimeException("Client not found")
    }

    fun updateRegisteredClientRefreshTokenExpiry(clientId: String, refreshTokenExpiry: Long?): RegisteredClient? {
        return registeredClientRepository.findByClientId(clientId)?.let {
            RegisteredClient.from(it)
                .tokenSettings(
                    TokenSettings.builder()
                        .refreshTokenTimeToLive(Duration.ofSeconds(refreshTokenExpiry ?: it.tokenSettings.refreshTokenTimeToLive.seconds))
                        .build()
                )
                .build()
        }.apply { registeredClientRepository.save(this) } ?: throw RuntimeException("Client not found")
    }

    fun updateRegisteredClientTokenExpiry(clientId: String, accessTokenExpiry: Long?, refreshTokenExpiry: Long?): RegisteredClient? {
        return registeredClientRepository.findByClientId(clientId)?.let {
            RegisteredClient.from(it)
                .clientSettings(
                    ClientSettings.builder()
                        .requireAuthorizationConsent(true)
                        .build()
                )
                .tokenSettings(
                    TokenSettings.builder()
                        .reuseRefreshTokens(false)
                        .accessTokenTimeToLive(Duration.ofSeconds(accessTokenExpiry ?: it.tokenSettings.accessTokenTimeToLive.seconds))
                        .refreshTokenTimeToLive(Duration.ofSeconds(refreshTokenExpiry ?: it.tokenSettings.refreshTokenTimeToLive.seconds))
                        .build()
                )
                .build()
        }.apply { registeredClientRepository.save(this) } ?: throw RuntimeException("Client not found")
    }

    fun findAll(): List<RegisteredClient> {
        return registeredClientRepository.findAll()
    }

}