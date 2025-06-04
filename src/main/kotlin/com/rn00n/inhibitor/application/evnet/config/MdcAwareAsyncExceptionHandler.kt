package com.rn00n.inhibitor.application.evnet.config

import com.rn00n.inhibitor.application.auth.event.LoginSuccessEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import org.slf4j.MDC
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler
import java.lang.reflect.Method

class MdcAwareAsyncExceptionHandler : AsyncUncaughtExceptionHandler {

    val logger = KotlinLogging.logger {}

    override fun handleUncaughtException(ex: Throwable, method: Method, vararg params: Any?) {
        // 예를 들어, 이벤트에 traceId가 있다면 params[0]으로 받을 수 있음
        val event = params.getOrNull(0)
        if (event is LoginSuccessEvent) {
            MDC.put("requestId", event.eventId)
        }

        logger.error(ex) { "Async method ${method.name} threw an exception" }

        MDC.clear()
    }
}
