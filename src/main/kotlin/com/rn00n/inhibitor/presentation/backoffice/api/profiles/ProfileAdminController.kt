package com.rn00n.inhibitor.presentation.backoffice.api.profiles

import com.rn00n.inhibitor.application.auth.model.principal.PrincipalUser
import com.rn00n.inhibitor.domain.accounts.Account
import io.swagger.v3.oas.annotations.Hidden
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Hidden
@RestController
@RequestMapping("/backoffice/api/profiles")
class ProfileAdminController {

    @GetMapping("/me")
    fun me(@PrincipalUser account: Account): ResponseEntity<Account> {
        return ResponseEntity.ok(account)
    }
}
