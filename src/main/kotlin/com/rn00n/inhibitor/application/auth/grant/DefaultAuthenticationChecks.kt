package com.rn00n.inhibitor.application.auth.grant

import com.rn00n.inhibitor.application.auth.exception.ErrorCode
import com.rn00n.inhibitor.application.auth.exception.DefaultOAuth2AuthenticationException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsChecker

class DefaultAuthenticationChecks {
    open class DefaultPreAuthenticationChecks : UserDetailsChecker {
        private val logger = KotlinLogging.logger {}
        override fun check(userDetails: UserDetails) {
            if (!userDetails.isAccountNonLocked) {
                logger.debug { "Failed to authenticate since user account is locked" }
                throw DefaultOAuth2AuthenticationException(ErrorCode.USER_REGISTER_FAILED_UNREGISTERED_USERNAME, "User account is locked")
            }
            if (!userDetails.isEnabled) {
                logger.debug { "Failed to authenticate since user account is disabled" }
                throw DefaultOAuth2AuthenticationException(ErrorCode.USER_REGISTER_FAILED_UNREGISTERED_USERNAME, "User is disabled")
            }
            if (!userDetails.isAccountNonExpired) {
                logger.debug { "Failed to authenticate since user account has expired" }
                throw DefaultOAuth2AuthenticationException(
                    ErrorCode.USER_REGISTER_FAILED_UNREGISTERED_USERNAME, "User account has expired"
                )
            }
        }
    }

    open class DefaultPostAuthenticationChecks : UserDetailsChecker {
        private val logger = KotlinLogging.logger {}
        override fun check(userDetails: UserDetails) {
            if (!userDetails.isCredentialsNonExpired) {
                logger.debug { "Failed to authenticate since user account credentials have expired" }
                throw DefaultOAuth2AuthenticationException(
                    ErrorCode.USER_REGISTER_FAILED_UNREGISTERED_USERNAME, "User credentials have expired"
                )
            }
        }
    }
}