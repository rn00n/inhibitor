package com.rn00n.inhibitor.presentation.inhibitor.api.clients

import com.rn00n.inhibitor.application.client.OAuth2ClientRegistrationManager
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient
import org.springframework.web.bind.annotation.*
import io.swagger.v3.oas.annotations.parameters.RequestBody as SwaggerRequestBody

@Tag(
    name = "Registered Client (임시 QA API)",
    description = """
    **등록된 OAuth2 클라이언트의 토큰 만료 시간을 확인 및 수정할 수 있는 임시 관리 API입니다.**
    
    - QA 테스트 및 클라이언트 상태 점검 용도로만 사용되며, 정식 배포 이후 제거될 예정입니다.
    - 토큰 TTL(Time-To-Live) 확인 및 수정 기능을 제공합니다.
    """
)
@RestController
@RequestMapping("/api/registered-clients")
class RegisteredClientController(
    private val clientRegistrationManager: OAuth2ClientRegistrationManager,
) {

    @Operation(
        summary = "등록된 클라이언트 목록 조회",
        description = """
            OAuth2 인증 서버에 등록된 RegisteredClient 목록과 현재 설정된 Access/Refresh Token 만료 시간을 조회합니다.
            
            > ⚠️ **운영 편의를 위한 임시 API입니다. 배포 이후 제거될 수 있습니다.**
        """,
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "RegisteredClient 목록",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = RegisteredClientTokenExpiryResponse::class)
                    )
                ]
            )
        ]
    )
    @GetMapping
    fun findAll(): ResponseEntity<List<RegisteredClientTokenExpiryResponse>> {
        val registeredClients: List<RegisteredClient> = clientRegistrationManager.getAvailableClient()
        return ResponseEntity.ok(registeredClients.map { it.toResponse() })
    }

    @Operation(
        summary = "Access Token 만료시간 수정",
        description = """
            특정 RegisteredClient의 Access Token TTL(Time-To-Live)을 수정합니다.
            
            > ⚠️ 임시 관리용 API로, 배포 이후 제거될 수 있습니다.
        """,
        parameters = [
            Parameter(name = "clientId", `in` = ParameterIn.PATH, required = true, description = "RegisteredClient의 clientId")
        ],
        requestBody = SwaggerRequestBody(
            required = true,
            content = [
                Content(
                    schema = Schema(implementation = TokenExpiry::class),
                    examples = [
                        ExampleObject(
                            name = "accessTokenUpdate",
                            summary = "Access Token TTL 수정",
                            value = """{ "accessTokenExpiry": 3600 }"""
                        )
                    ]
                )
            ]
        ),
        responses = [
            ApiResponse(responseCode = "200", description = "수정된 RegisteredClient 정보 반환"),
            ApiResponse(responseCode = "404", description = "존재하지 않는 clientId")
        ]
    )
    @PatchMapping("/{clientId}/access-token-expiry")
    fun updateRegisteredClientAccessTokenExpiry(
        @PathVariable clientId: String,
        @RequestBody tokenExpiry: TokenExpiry,
    ): ResponseEntity<RegisteredClientTokenExpiryResponse> {
        val registeredClient = clientRegistrationManager.updateRegisteredClientAccessTokenExpiry(clientId, tokenExpiry.accessTokenExpiry)
        return ResponseEntity.ok(registeredClient?.toResponse())
    }

    @Operation(
        summary = "Refresh Token 만료시간 수정",
        description = """
            특정 RegisteredClient의 Refresh Token TTL(Time-To-Live)을 수정합니다.
            
            > ⚠️ 임시 관리용 API로, 배포 이후 제거될 수 있습니다.
        """,
        parameters = [
            Parameter(name = "clientId", `in` = ParameterIn.PATH, required = true, description = "RegisteredClient의 clientId")
        ],
        requestBody = SwaggerRequestBody(
            required = true,
            content = [
                Content(
                    schema = Schema(implementation = TokenExpiry::class),
                    examples = [
                        ExampleObject(
                            name = "refreshTokenUpdate",
                            summary = "Refresh Token TTL 수정",
                            value = """{ "refreshTokenExpiry": 15552000 }"""
                        )
                    ]
                )
            ]
        ),
        responses = [
            ApiResponse(responseCode = "200", description = "수정된 RegisteredClient 정보 반환"),
            ApiResponse(responseCode = "404", description = "존재하지 않는 clientId")
        ]
    )
    @PatchMapping("/{clientId}/refresh-token-expiry")
    fun updateRegisteredClientRefreshTokenExpiry(
        @PathVariable clientId: String,
        @RequestBody tokenExpiry: TokenExpiry,
    ): ResponseEntity<RegisteredClientTokenExpiryResponse> {
        val registeredClient = clientRegistrationManager.updateRegisteredClientRefreshTokenExpiry(clientId, tokenExpiry.refreshTokenExpiry)
        return ResponseEntity.ok(registeredClient?.toResponse())
    }

    @Operation(
        summary = "Access/Refresh Token 만료시간 동시 수정",
        description = """
            특정 RegisteredClient의 Access Token 및 Refresh Token TTL을 한 번에 수정합니다.
            
            > ⚠️ 임시 관리용 API로, 배포 이후 제거될 수 있습니다.
        """,
        parameters = [
            Parameter(name = "clientId", `in` = ParameterIn.PATH, required = true, description = "RegisteredClient의 clientId")
        ],
        requestBody = SwaggerRequestBody(
            required = true,
            content = [
                Content(
                    schema = Schema(implementation = TokenExpiry::class),
                    examples = [
                        ExampleObject(
                            name = "tokenTTLUpdate",
                            summary = "Access/Refresh Token TTL 동시 수정",
                            value = """{ "accessTokenExpiry": 3600, "refreshTokenExpiry": 15552000 }"""
                        )
                    ]
                )
            ]
        ),
        responses = [
            ApiResponse(responseCode = "200", description = "수정된 RegisteredClient 정보 반환"),
            ApiResponse(responseCode = "404", description = "존재하지 않는 clientId")
        ]
    )
    @PatchMapping("/{clientId}/token-expiry")
    fun updateRegisteredClientTokenExpiry(
        @PathVariable clientId: String,
        @RequestBody tokenExpiry: TokenExpiry,
    ): ResponseEntity<RegisteredClientTokenExpiryResponse> {
        val accessTokenExpiry = tokenExpiry.accessTokenExpiry
        val refreshTokenExpiry = tokenExpiry.refreshTokenExpiry
        val registeredClient = clientRegistrationManager.updateRegisteredClientTokenExpiry(clientId, accessTokenExpiry, refreshTokenExpiry)
        return ResponseEntity.ok(registeredClient?.toResponse())
    }

    @Schema(description = "Access/Refresh Token TTL 설정 요청 DTO")
    data class TokenExpiry(
        @field:Schema(description = "Access Token TTL (초 단위)", example = "3600")
        val accessTokenExpiry: Long? = null,
        @field:Schema(description = "Refresh Token TTL (초 단위)", example = "15552000")
        val refreshTokenExpiry: Long? = null,
    )

    @Schema(description = "RegisteredClient의 Token TTL 응답 DTO")
    data class RegisteredClientTokenExpiryResponse(
        @field:Schema(description = "클라이언트 ID", example = "inhibitor")
        val clientId: String,
        @field:Schema(description = "Access Token TTL (초 단위)", example = "3600")
        val accessTokenExpiry: Long? = null,
        @field:Schema(description = "Refresh Token TTL (초 단위)", example = "15552000")
        val refreshTokenExpiry: Long? = null,
    )
}

private fun RegisteredClient.toResponse(): RegisteredClientController.RegisteredClientTokenExpiryResponse {
    return RegisteredClientController.RegisteredClientTokenExpiryResponse(
        clientId = this.clientId,
        accessTokenExpiry = this.tokenSettings.accessTokenTimeToLive.seconds,
        refreshTokenExpiry = this.tokenSettings.refreshTokenTimeToLive.seconds,
    )
}
