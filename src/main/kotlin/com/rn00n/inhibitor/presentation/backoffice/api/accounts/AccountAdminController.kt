package com.rn00n.inhibitor.presentation.backoffice.api.accounts

import com.rn00n.inhibitor.application.accounts.AccountReadService
import com.rn00n.inhibitor.commons.base.PageResponse
import com.rn00n.inhibitor.domain.accounts.Account
import com.rn00n.inhibitor.presentation.backoffice.api.accounts.dto.AccountAdminResponse
import com.rn00n.inhibitor.presentation.backoffice.api.accounts.dto.SearchAccountAdminRequest
import io.swagger.v3.oas.annotations.Hidden
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Hidden
@RestController
@RequestMapping("/backoffice/api/accounts")
class AccountAdminController(
    private val accountReadService: AccountReadService,
) {

    @GetMapping
    fun search(
        searchRequest: SearchAccountAdminRequest,
        @PageableDefault(size = 10) pageable: Pageable,
    ): ResponseEntity<PageResponse<AccountAdminResponse>> {
        val resultPage: Page<Account> = accountReadService.search(searchRequest.keyword, pageable)
        return ResponseEntity.ok(PageResponse.of(resultPage.map { it.toResponse() }))
    }
}

private fun Account.toResponse(): AccountAdminResponse {
    return AccountAdminResponse(
        id = this.id,
        username = this.username,
    )
}