package com.rn00n.inhibitor.application.auth.event

data class LoginSuccessEvent(
    val accountId: Long,
    val eventId: String,
)