package com.rn00n.inhibitor.presentation.inhibitor.api.sign

import com.rn00n.inhibitor.application.sign.SignService
import com.rn00n.inhibitor.domain.accounts.Account
import com.rn00n.inhibitor.presentation.inhibitor.api.sign.dto.SignUpRequest
import com.rn00n.inhibitor.presentation.inhibitor.api.sign.dto.SignUpResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/sign")
class SignController(
    private val signService: SignService,
) {

    @PostMapping("/up")
    fun signUp(
        @RequestBody signUpRequest: SignUpRequest,
    ): ResponseEntity<SignUpResponse> {
        val account: Account = signService.signUp(signUpRequest.username, signUpRequest.password, signUpRequest.name)
        return ResponseEntity.ok(account.toResponse())
    }
}

private fun Account.toResponse(): SignUpResponse = SignUpResponse(
    id = this.id,
    username = this.username,
    name = this.name,
)