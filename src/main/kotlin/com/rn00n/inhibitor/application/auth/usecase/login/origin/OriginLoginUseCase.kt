package com.rn00n.inhibitor.application.auth.usecase.login.origin

import com.rn00n.inhibitor.application.auth.usecase.login.LoginUseCase
import org.springframework.security.crypto.password.PasswordEncoder

abstract class OriginLoginUseCase(
    passwordEncoder: PasswordEncoder,
) : LoginUseCase(passwordEncoder) {

    abstract fun execute(command: OriginLoginCommand): OriginLoginOutput
}
