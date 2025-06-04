package com.rn00n.inhibitor.application.auth.service.userdetails

import com.rn00n.inhibitor.domain.accounts.Account
import java.util.*

interface UserPrincipalRepository {
    fun findByUsername(username: String): Account?
    fun findById(id: Long): Optional<Account>
}