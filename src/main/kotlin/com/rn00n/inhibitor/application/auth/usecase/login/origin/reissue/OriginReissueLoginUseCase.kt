package com.rn00n.inhibitor.application.auth.usecase.login.origin.reissue

import com.rn00n.inhibitor.application.auth.usecase.login.origin.OriginLoginCommand
import com.rn00n.inhibitor.application.auth.usecase.login.origin.OriginLoginOutput
import com.rn00n.inhibitor.application.auth.usecase.login.origin.OriginLoginUseCase
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class OriginReissueLoginUseCase(
    passwordEncoder: PasswordEncoder,
) : OriginLoginUseCase(
    passwordEncoder
) {

    override fun execute(command: OriginLoginCommand): OriginLoginOutput {
        return OriginLoginOutput()
    }

}