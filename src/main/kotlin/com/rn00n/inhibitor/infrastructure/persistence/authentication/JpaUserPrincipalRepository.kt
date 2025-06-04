package com.rn00n.inhibitor.infrastructure.persistence.authentication

import com.rn00n.inhibitor.application.auth.service.userdetails.UserPrincipalRepository
import com.rn00n.inhibitor.commons.datasources.annotations.InhibitorRepository
import com.rn00n.inhibitor.domain.accounts.Account
import org.springframework.data.jpa.repository.JpaRepository

@InhibitorRepository
interface JpaUserPrincipalRepository : JpaRepository<Account, Long>, UserPrincipalRepository