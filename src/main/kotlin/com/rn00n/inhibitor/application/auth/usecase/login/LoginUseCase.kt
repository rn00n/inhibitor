package com.rn00n.inhibitor.application.auth.usecase.login

import co.elastic.apm.api.CaptureSpan
import com.rn00n.inhibitor.application.auth.exception.DefaultOAuth2AuthenticationException
import com.rn00n.inhibitor.application.auth.exception.ErrorCode
import org.springframework.security.crypto.password.PasswordEncoder

abstract class LoginUseCase(
    protected val passwordEncoder: PasswordEncoder,
) {

    @CaptureSpan("LoginUseCase#validatePassword")
    protected fun validatePassword(encodedPassword: String?, rawPassword: String?) {
        if (encodedPassword.isNullOrBlank()) {
            throw DefaultOAuth2AuthenticationException(
                ErrorCode.USER_INCORRECT_PASSWORD,
                "encoded password is null or blank"
            )
        }
        if (rawPassword.isNullOrBlank()) {
            throw DefaultOAuth2AuthenticationException(
                ErrorCode.USER_INCORRECT_PASSWORD,
                "raw password is null or blank"
            )
        }

        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw DefaultOAuth2AuthenticationException(
                ErrorCode.USER_INCORRECT_PASSWORD,
                "do not match password"
            )
        }
    }
}
