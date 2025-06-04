package com.rn00n.inhibitor.commons.security.config

import com.rn00n.inhibitor.commons.security.token.CustomJwtAuthenticationConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class WebSecurityConfig(
    private val customJwtAuthenticationConverter: CustomJwtAuthenticationConverter,
) {

    @Bean
    fun webSecurityCustomizer(): WebSecurityCustomizer {
        return WebSecurityCustomizer { web ->
            web.ignoring().requestMatchers(
                "/css/**",
                "/js/**",
                "/img/**",
                "/lottie/**",
                "/webjars/**",
                "/favicon.ico"
            )
        }
    }

    @Bean
    @Order(0)
    fun userInfoSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            securityMatcher("/userinfo")
            authorizeHttpRequests {
                authorize(anyRequest, authenticated)
            }
            oauth2ResourceServer {
                jwt {}
            }
        }
        return http.build()
    }

    @Bean
    @Order(1)
    fun apiSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            csrf { disable() }
            securityMatcher("/api/**")
            authorizeHttpRequests {
                authorize("/api", "/api/", permitAll)
                authorize("/api/sign/up", permitAll)
                authorize("/api/internal/me/**", hasRole("USER"))
                authorize("/api/internal/accounts/**", hasRole("USER"))
                authorize(anyRequest, permitAll)
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
    @Order(99)
    fun webSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            csrf { disable() }
            securityMatcher("/**")
            authorizeHttpRequests {
                authorize(anyRequest, permitAll)
            }
        }
        return http.build()
    }
}