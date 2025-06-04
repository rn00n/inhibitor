package com.rn00n.inhibitor.commons.filters

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.*

@Component
class RequestTracingFilter : OncePerRequestFilter() {

    private val log = KotlinLogging.logger {}

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        val requestId = request.getHeader("X-LZ-Request-Id")?.takeIf { it.isNotBlank() }
            ?: UUID.randomUUID().toString()
        MDC.put("requestId", requestId)

        val method = request.method
        val requestUrl = request.requestURL.toString()
        val queryString = request.queryString?.let { "?$it" } ?: ""
        val fullUrl = requestUrl + queryString
        val userAgent = request.getHeader("User-Agent") ?: "N/A"

        log.info {
            "$method $fullUrl | UA=$userAgent"
        }

        try {
            filterChain.doFilter(request, response)
        } finally {
            MDC.clear()
        }

    }
}