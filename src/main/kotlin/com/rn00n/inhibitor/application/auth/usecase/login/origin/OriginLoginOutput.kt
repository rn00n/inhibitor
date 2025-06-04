package com.rn00n.inhibitor.application.auth.usecase.login.origin

import com.rn00n.inhibitor.application.auth.usecase.login.LoginOutput

class OriginLoginOutput(
    override val success: Boolean = true,
) : LoginOutput(success)