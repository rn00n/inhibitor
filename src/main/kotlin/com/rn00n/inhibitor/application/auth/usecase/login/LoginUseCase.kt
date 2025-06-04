package com.rn00n.inhibitor.application.auth.usecase.login

import co.elastic.apm.api.CaptureSpan
import com.rn00n.inhibitor.application.auth.exception.ErrorCode
import com.rn00n.inhibitor.application.auth.exception.DefaultOAuth2AuthenticationException
import org.springframework.security.crypto.password.PasswordEncoder

abstract class LoginUseCase(
    protected val passwordEncoder: PasswordEncoder,
) {

    @CaptureSpan("LoginUseCase#validatePassword")
    protected fun validatePassword(encodedPassword: String?, rawPassword: String?) {
        if (encodedPassword.isNullOrBlank() || rawPassword.isNullOrBlank()) return

        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw DefaultOAuth2AuthenticationException(
                ErrorCode.USER_INCORRECT_PASSWORD,
                "do not match password"
            )
        }
    }
}
