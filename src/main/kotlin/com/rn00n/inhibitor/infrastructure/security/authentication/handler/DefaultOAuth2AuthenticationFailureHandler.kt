package com.rn00n.inhibitor.infrastructure.security.authentication.handler

import com.fasterxml.jackson.databind.ObjectMapper
import com.rn00n.inhibitor.application.auth.exception.ErrorCode
import com.rn00n.inhibitor.infrastructure.security.authentication.convertor.DefaultOAuth2ErrorHttpMessageConverter
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.apache.commons.logging.LogFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.security.core.AuthenticationException
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.OAuth2ErrorCodes
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets

@Component
class DefaultOAuth2AuthenticationFailureHandler(
    private val objectMapper: ObjectMapper // 빈 주입으로 통일
) : AuthenticationFailureHandler {

    private val logger = LogFactory.getLog(this::class.java)
    private var errorResponseConverter: HttpMessageConverter<OAuth2Error> = DefaultOAuth2ErrorHttpMessageConverter()

    override fun onAuthenticationFailure(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authenticationException: AuthenticationException
    ) {
        val error = when (authenticationException) {
            is OAuth2AuthenticationException -> authenticationException.error
            else -> {
                logger.warn(
                    "${AuthenticationException::class.java.simpleName} must be of type ${OAuth2AuthenticationException::class.java.name} but was ${authenticationException::class.java.name}"
                )
                OAuth2Error(OAuth2ErrorCodes.INVALID_REQUEST, "인증에 실패했습니다", null)
            }
        }

        val errorCodeValue = error.errorCode
        val errorCode = enumValues<ErrorCode>().find { it.name == errorCodeValue } ?: ErrorCode.BAD_REQUEST
        val httpStatus = if (errorCode.isSuccess()) HttpStatus.OK else HttpStatus.BAD_REQUEST

        val responseBody = mapOf(
            "status" to httpStatus.value().toString(),
            "error" to errorCode.getCode(),
            "description" to (error.description ?: ""),
            "code" to errorCode.getCode()
        )

        response.status = httpStatus.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = StandardCharsets.UTF_8.name()
        objectMapper.writeValue(response.writer, responseBody)
    }

}