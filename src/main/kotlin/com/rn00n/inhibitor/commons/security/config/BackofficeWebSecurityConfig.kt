package com.rn00n.inhibitor.commons.security.config

import com.rn00n.inhibitor.commons.security.token.CustomJwtAuthenticationConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class BackofficeWebSecurityConfig(
    private val customJwtAuthenticationConverter: CustomJwtAuthenticationConverter,
) {

    @Bean
    @Order(2)
    fun backofficeApiSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            csrf { disable() }
            securityMatcher("/backoffice/api/**")
            authorizeHttpRequests {
                authorize(anyRequest, authenticated)
            }
            oauth2ResourceServer {
                jwt {
                    jwtAuthenticationConverter = customJwtAuthenticationConverter
                }
            }
        }

        return http.build()
    }

    @Bean
    @Order(89)
    fun backofficeWebSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            csrf { disable() }
            securityMatcher("/backoffice/**")
            authorizeHttpRequests {
                authorize("/backoffice/login", permitAll)
                authorize("/backoffice/account", permitAll)
                authorize("/backoffice/client", permitAll)
                authorize("/backoffice", permitAll)
                authorize(anyRequest, authenticated)
            }
        }
        return http.build()
    }
}