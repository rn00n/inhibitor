package com.rn00n.inhibitor.infrastructure.persistence.accounts

import com.rn00n.inhibitor.commons.datasources.annotations.InhibitorRepository
import com.rn00n.inhibitor.domain.accounts.Account
import com.rn00n.inhibitor.domain.accounts.repo.AccountRepository
import org.springframework.data.jpa.repository.JpaRepository

@InhibitorRepository
interface JpaAccountRepository : JpaRepository<Account, Long>, AccountRepository {
}