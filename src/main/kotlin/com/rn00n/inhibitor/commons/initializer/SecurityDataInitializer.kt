package com.rn00n.inhibitor.commons.initializer

import com.rn00n.inhibitor.application.client.OAuth2ClientRegistrationManager
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import java.time.Duration

/**
 * Initializes and registers OAuth2 clients when the application starts.
 * OAuth2 클라이언트를 애플리케이션 시작 시 초기화 및 등록합니다.
 */
@Suppress("DEPRECATION")
@Component
class SecurityDataInitializer(
    private val clientRegistrationManager: OAuth2ClientRegistrationManager,
    @Value("\${initialization.security}") private val isSecurityInitializationEnabled: Boolean,
) : ApplicationRunner {

    /**
     * This method is called during application startup.
     * 애플리케이션 시작 시 클라이언트를 등록합니다.
     *
     * @param args Application arguments
     */
    override fun run(args: ApplicationArguments?) {
        if (!isSecurityInitializationEnabled) return

        // 특정 클라이언트들의 엑세스 및 리프레시 토큰 만료시간 설정
        clientRegistrationManager.initRegisteredClient(
            "inhibitor",
            "1234",
            setOf("read", "write"),
            Duration.ofSeconds(60),
            Duration.ofSeconds(120)
        )
        clientRegistrationManager.initRegisteredClient(
            "admin",
            "1234",
            setOf("read", "write"),
            Duration.ofSeconds(60),
            Duration.ofSeconds(180)
        )
    }

}