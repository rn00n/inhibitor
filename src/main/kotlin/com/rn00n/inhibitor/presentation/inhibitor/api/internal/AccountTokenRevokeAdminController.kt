package com.rn00n.inhibitor.presentation.inhibitor.api.internal

import com.rn00n.inhibitor.application.tokens.OAuth2AuthorizationService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 관리자가 특정 사용자의 토큰을 강제 만료시키는 책임을 가지는 컨트롤러입니다.
 *
 * 이 컨트롤러는 서비스 운영을 위한 백오피스 등에서, 특정 사용자 계정에 대한 세션 정리, 강제 로그아웃 등을 수행할 수 있도록 제공합니다.
 *
 * 본 API는 일반 사용자 토큰이 아닌, 내부 시스템 간 통신을 위한 Super Token으로 호출되어야 합니다.
 *
 * Terminology:
 * - Super Token: 외부 노출되지 않는 관리자 시스템 간의 고정 인증 토큰
 * - Token Revocation: 서버 측 저장소에서 access/refresh token을 제거하는 행위
 * - Admin Operation: 관리자 또는 시스템에 의해 수행되는 강제 행위
 */
@Tag(name = "Admin - User Token Control", description = "관리자용 사용자 토큰 강제 만료 API")
@RestController
@RequestMapping("/api/internal/accounts")
class AccountTokenRevokeAdminController(
    private val tokenRevokeService: OAuth2AuthorizationService,
) {

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "사용자 토큰 강제 만료",
        description = """
            관리자가 특정 사용자의 access token 및 refresh token을 모두 강제 만료시킵니다.

            이 API는 백오피스 또는 내부 시스템에서 호출되며, 일반 사용자 토큰이 아닌 Super Token (bearerAuth)을 통해 인증되어야 합니다.
            보안상 외부에 노출되지 않는 고정 토큰 또는 내부 시스템 토큰만 허용됩니다.
        """,
        responses = [
            ApiResponse(responseCode = "200", description = "토큰 만료 성공"),
            ApiResponse(responseCode = "401", description = "인증 실패 또는 토큰 없음"),
            ApiResponse(responseCode = "403", description = "관리자 권한 없음")
        ]
    )
    @DeleteMapping("/{accountId}/tokens")
    fun revokeAccountTokens(
        @PathVariable accountId: Long,
    ): ResponseEntity<Any> {
        tokenRevokeService.revokeTokensByAccountId(accountId)
        return ResponseEntity.ok().build()
    }
}
