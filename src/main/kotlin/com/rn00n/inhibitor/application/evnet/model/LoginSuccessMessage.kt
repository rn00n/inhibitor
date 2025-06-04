package com.rn00n.inhibitor.application.evnet.model

data class LoginSuccessMessage(
    val accountId: Long,
    val eventId: String,
) : EventPayload