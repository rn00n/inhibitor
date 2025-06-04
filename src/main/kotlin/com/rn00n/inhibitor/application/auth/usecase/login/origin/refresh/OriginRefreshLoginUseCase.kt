package com.rn00n.inhibitor.application.auth.usecase.login.origin.refresh

import com.rn00n.inhibitor.application.auth.usecase.login.origin.OriginLoginCommand
import com.rn00n.inhibitor.application.auth.usecase.login.origin.OriginLoginOutput
import com.rn00n.inhibitor.application.auth.usecase.login.origin.OriginLoginUseCase
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class OriginRefreshLoginUseCase(
    passwordEncoder: PasswordEncoder,
) : OriginLoginUseCase(
    passwordEncoder
) {

    override fun execute(command: OriginLoginCommand): OriginLoginOutput {
        return OriginLoginOutput()
    }
}
