package com.rn00n.inhibitor.presentation.inhibitor.api.internal

import com.rn00n.inhibitor.application.auth.model.principal.PrincipalUser
import com.rn00n.inhibitor.application.tokens.OAuth2AuthorizationService
import com.rn00n.inhibitor.domain.accounts.Account
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 인증된 사용자의 토큰을 만료시키는 책임을 가지는 컨트롤러입니다.
 *
 * 이 클래스는 authenticated user가 자신의 access token 및 refresh token을 직접 만료(revoke)할 수 있도록 하는 기능을 담당합니다.
 * 주로 로그아웃, 세션 초기화, 계정 보호 등의 목적으로 사용됩니다.
 *
 * Terminology:
 * - Authenticated User: 현재 access token으로 인증된 사용자
 * - Token Revocation: access token 및 refresh token을 서버 측 저장소에서 제거하는 보안 행위
 */
@Tag(name = "My Tokens", description = "인증된 사용자의 토큰 만료 API")
@RestController
@RequestMapping("/api/internal/me/tokens")
class AuthenticatedUserTokenController(
    private val tokenRevokeService: OAuth2AuthorizationService,
) {

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "사용자 토큰 만료",
        description = """
            현재 인증된 사용자의 access token 및 refresh token을 만료시킵니다.
        
            이 API는 사용자가 직접 자신의 세션을 종료하거나 로그아웃을 수행할 때 사용됩니다.
            호출을 위해서는 유효한 Access Token이 필요합니다.
        """,
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "토큰 만료 성공"
            ),
            ApiResponse(
                responseCode = "401",
                description = "인증 실패 또는 토큰 없음"
            )
        ]
    )
    @DeleteMapping
    fun revokeUserTokens(
        @Parameter(hidden = true) @PrincipalUser account: Account,
    ): ResponseEntity<Any> {
        tokenRevokeService.revokeTokensByUsername(account.username)
        return ResponseEntity.ok().build()
    }
}
