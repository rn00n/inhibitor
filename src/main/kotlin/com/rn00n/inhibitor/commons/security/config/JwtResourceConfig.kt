package com.rn00n.inhibitor.commons.security.config

import com.rn00n.inhibitor.commons.security.token.CustomJwtAuthenticationConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.core.userdetails.UserDetailsService

@Configuration
class JwtResourceConfig {

    /**
     * 사용자 인증 정보(JWT)를 Spring Security로 변환하는 Bean
     */
    @Bean
    fun jwtAuthenticationConverter(defaultUserDetailsService: UserDetailsService) =
        CustomJwtAuthenticationConverter(defaultUserDetailsService)
}