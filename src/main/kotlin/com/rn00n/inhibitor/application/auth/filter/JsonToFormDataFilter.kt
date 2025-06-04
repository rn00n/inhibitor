package com.rn00n.inhibitor.application.auth.filter

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.MediaType
import org.springframework.stereotype.Component

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class JsonToFormDataFilter : Filter {

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val httpRequest = request as HttpServletRequest

        val isTargetRequest = httpRequest.requestURI in listOf("/oauth2/token", "/oauth2/revoke") &&
            httpRequest.method.equals("POST", ignoreCase = true) &&
            httpRequest.contentType?.contains(MediaType.APPLICATION_JSON_VALUE) == true

        if (isTargetRequest) {
            val wrappedRequest = JsonToFormDataRequestWrapper(httpRequest)
            chain.doFilter(wrappedRequest, response)
        } else {
            chain.doFilter(request, response)
        }
    }
}