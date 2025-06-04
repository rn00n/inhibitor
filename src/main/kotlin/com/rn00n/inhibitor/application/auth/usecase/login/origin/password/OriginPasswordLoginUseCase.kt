package com.rn00n.inhibitor.application.auth.usecase.login.origin.password

import com.rn00n.inhibitor.application.auth.usecase.login.origin.OriginLoginCommand
import com.rn00n.inhibitor.application.auth.usecase.login.origin.OriginLoginOutput
import com.rn00n.inhibitor.application.auth.usecase.login.origin.OriginLoginUseCase
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class OriginPasswordLoginUseCase(
    passwordEncoder: PasswordEncoder,
) : OriginLoginUseCase(
    passwordEncoder
) {

    override fun execute(command: OriginLoginCommand): OriginLoginOutput {
        val user = command.user!!

        validatePassword(user.password, command.password) // 비밀번호 확인
        return OriginLoginOutput()
    }
}
