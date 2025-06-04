package com.rn00n.inhibitor.application.auth.handler

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.stereotype.Component

@Component
class OriginOAuth2ClientAuthenticationFailureHandler : AuthenticationFailureHandler {
    override fun onAuthenticationFailure(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exception: AuthenticationException
    ) {
        SecurityContextHolder.clearContext()

        val errorCode = when (exception) {
            is OAuth2AuthenticationException -> exception.error.errorCode
            else -> "unknown_error"
        }

        val status = when (errorCode) {
            "invalid_client" -> HttpStatus.UNAUTHORIZED
            else -> HttpStatus.BAD_REQUEST
        }

        response.status = status.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE

        response.writer.write(
            """
            {
              "status": "${status.value()}",
              "error": "${status.value()}",
              "description": "$errorCode",
              "code": ${status.value()},
            }
            """.trimIndent()
        )
    }
}