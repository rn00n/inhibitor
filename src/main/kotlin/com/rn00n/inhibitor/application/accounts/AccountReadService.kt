package com.rn00n.inhibitor.application.accounts

import com.rn00n.inhibitor.domain.accounts.Account
import com.rn00n.inhibitor.domain.accounts.service.AccountService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class AccountReadService(
    private val accountService: AccountService,
) {
    fun search(keyword: String, pageable: Pageable): Page<Account> {
        return accountService.findByUsernameContains(keyword, pageable)
    }
}
