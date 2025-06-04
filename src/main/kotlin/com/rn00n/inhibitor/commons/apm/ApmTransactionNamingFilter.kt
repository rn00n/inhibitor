package com.rn00n.inhibitor.commons.apm

import co.elastic.apm.api.ElasticApm
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.GenericFilterBean

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class ApmTransactionNamingFilter : GenericFilterBean() {

    override fun doFilter(
        request: ServletRequest,
        response: ServletResponse,
        chain: FilterChain
    ) {
        if (request is HttpServletRequest) {
            val method = request.method
            val path = request.requestURI

            try {
                ElasticApm.currentTransaction().setName("$method $path")
            } catch (e: Exception) {
                logger.warn("APM Transaction Name Setting Error", e)
            }
        }

        chain.doFilter(request, response)
    }
}
