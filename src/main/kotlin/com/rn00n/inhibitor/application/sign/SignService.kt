package com.rn00n.inhibitor.application.sign

import com.rn00n.inhibitor.commons.datasources.config.InhibitorDataSourceConfig
import com.rn00n.inhibitor.domain.accounts.Account
import com.rn00n.inhibitor.domain.accounts.AccountStatus
import com.rn00n.inhibitor.domain.accounts.service.AccountService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SignService(
    private val accountService: AccountService,
    private val passwordEncoder: PasswordEncoder,
) {

    @Transactional(transactionManager = InhibitorDataSourceConfig.TRANSACTION_MANAGER_BEAN)
    fun signUp(username: String, password: String, name: String): Account {
        return Account(
            username = username,
            password = passwordEncoder.encode(password),
            name = name,
            status = AccountStatus.ACTIVATE
        ).let {
            accountService.create(it)
        }
    }
}
