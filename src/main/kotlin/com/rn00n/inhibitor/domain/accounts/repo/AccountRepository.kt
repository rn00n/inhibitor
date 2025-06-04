package com.rn00n.inhibitor.domain.accounts.repo

import com.rn00n.inhibitor.domain.accounts.Account
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.*

interface AccountRepository {
    fun findById(id: Long): Optional<Account>
    fun findByUsernameContains(keyword: String, pageable: Pageable): Page<Account>
}