package com.rn00n.inhibitor.application.evnet.login.listener

import com.rn00n.inhibitor.application.auth.event.LoginSuccessEvent
import com.rn00n.inhibitor.application.evnet.model.LoginSuccessMessage
import io.github.oshai.kotlinlogging.KotlinLogging
import org.slf4j.MDC
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Async("eventTaskExecutor")
@Component
class LoginEventListener(
) {

    private val logger = KotlinLogging.logger {}

    @EventListener
    fun onLoginSuccess(event: LoginSuccessEvent) {
        MDC.put("requestId", event.eventId)
        logger.info { "Login Success Event. accountId: ${event.accountId}" }
        try {
        } finally {
            MDC.clear()
        }
    }
}

private fun LoginSuccessEvent.toMessage(): LoginSuccessMessage {
    return LoginSuccessMessage(
        accountId = this.accountId,
        eventId = eventId,
    )
}