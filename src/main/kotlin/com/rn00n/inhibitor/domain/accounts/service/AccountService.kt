package com.rn00n.inhibitor.domain.accounts.service

import com.rn00n.inhibitor.domain.accounts.Account
import com.rn00n.inhibitor.domain.accounts.repo.AccountRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class AccountService(
    private val repository: AccountRepository,
) {
    fun findById(id: Long): Account {
        return repository.findById(id).orElseThrow { throw UsernameNotFoundException("User not found") }
    }

    fun findByUsernameContains(keyword: String, pageable: Pageable): Page<Account> {
        return repository.findByUsernameContains(keyword, pageable)
    }
}