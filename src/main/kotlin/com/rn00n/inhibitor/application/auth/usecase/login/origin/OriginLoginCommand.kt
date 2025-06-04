package com.rn00n.inhibitor.application.auth.usecase.login.origin

import com.rn00n.inhibitor.application.auth.usecase.login.LoginCommand
import com.rn00n.inhibitor.domain.accounts.Account

class OriginLoginCommand(
    val user: Account?,
    override val password: String? = null,
    val now: Long = System.currentTimeMillis()
) : LoginCommand(password)