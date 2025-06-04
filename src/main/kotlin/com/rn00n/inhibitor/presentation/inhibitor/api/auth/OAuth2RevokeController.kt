package com.rn00n.inhibitor.presentation.inhibitor.api.auth

import com.rn00n.inhibitor.commons.webclient.WebClientRequestHelper
import com.rn00n.inhibitor.presentation.inhibitor.api.auth.dto.RevokeTokenRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.util.*
import kotlin.reflect.full.memberProperties
import io.swagger.v3.oas.annotations.parameters.RequestBody as SwaggerRequestBody

/**
 * Swagger 문서용 엔드포인트
 * 실제로 도달하지 않는다.
 * SecurityFilterChain 에서 응답까지 완성.
 *
 * 이는 OAuth 2.0 명세 (RFC 7009)에 따라,
 * **토큰 폐기 요청은 반드시 `application/x-www-form-urlencoded` 형식의 요청 본문으로 전송되어야 한다**는
 * 스펙을 준수하기 위함이다.
 *
 * > RFC 7009:
 * > "The authorization server responds with HTTP status code 200 if the token has been revoked successfully
 * > or if the client submitted an invalid token."
 *
 * 이 컨트롤러는 인증 도메인의 경계를 유지하면서도,
 * 다양한 내부 클라이언트(Web, iOS, Android 등)의 요청 형식을 표준에 맞게 적절히 변환하여
 * 토큰 폐기 요청의 안정성과 유연성을 동시에 보장하는 전략적 어댑터 역할을 수행한다.
 *
 * @see https://datatracker.ietf.org/doc/html/rfc7009
 */
@Tag(
    name = "Token Revocation Adapter",
    description = """
    **OAuth2 인증 서버에 토큰 폐기 요청을 중계하는 어댑터입니다.**
    
    - 클라이언트 요청은 `application/json` 형식으로 받습니다.
    - 내부 인증 서버에는 `x-www-form-urlencoded` 형식으로 전달됩니다.
    - 현재는 `refresh_token`만 폐기 대상으로 처리됩니다.
    """
)
@RestController
@RequestMapping("/oauth2/revoke")
class OAuth2RevokeController(
    private val requestHelper: WebClientRequestHelper,
) {

    private val oauthTokenUrl = "/oauth2/revoke"

    @SecurityRequirement(name = "basicAuth")
    @Operation(
        summary = "OAuth2 토큰 폐기 어댑터",
        description = """
            - 요청은 JSON 형식으로 받으며, 내부적으로 `application/x-www-form-urlencoded` 형태로 변환되어 전달됩니다.
            - 인증 서버는 `Authorization: Basic base64(clientId:clientSecret)` 헤더를 요구합니다.
            - RFC 7009에 따라 토큰이 없더라도 항상 200 OK를 반환합니다.
        """,
        parameters = [
            Parameter(
                name = HttpHeaders.AUTHORIZATION,
                `in` = ParameterIn.HEADER,
                required = true,
                description = "Basic 인증 헤더 (Base64 encoded: clientId:clientSecret)",
                example = "Basic bGV6aGluLWlvczpLdCM3NGJtWnhVcU53MjltRnlkTC1XRTNxOXZNSnBYdA=="
            )
        ],
        requestBody = SwaggerRequestBody(
            required = true,
            content = [
                Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = RevokeTokenRequest::class),
                    examples = [
                        ExampleObject(
                            name = "refresh_token",
                            summary = "리프레시 토큰 폐기",
                            value = """{
                                "token": "{{refresh_token}}",
                                "token_type_hint": "refresh_token"
                            }"""
                        )
                    ]
                )
            ]
        ),
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "항상 성공 응답 반환",
                content = [Content()]
            ),
            ApiResponse(
                responseCode = "401",
                description = "클라이언트 인증 실패 (예: Basic 인증 오류)",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        examples = [
                            ExampleObject(
                                name = "invalid_client",
                                value = """{
                                    "status": "401",
                                    "error": "401",
                                    "description": "invalid_client",
                                    "code": "401"
                                }"""
                            )
                        ]
                    )
                ]
            )
        ]
    )
    @PostMapping
    fun revoke(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION) authorization: String?,
        @RequestBody revokeTokenRequest: RevokeTokenRequest,
        request: HttpServletRequest,
    ): Mono<ResponseEntity<String>> {
        val baseUrl = "${request.scheme}://${request.serverName}:${request.serverPort}"

        val headers = mutableMapOf<String, String>().apply {
            request.headerNames.toList().forEach { name ->
                request.getHeaders(name).toList().forEach { value ->
                    put(name, value) // 마지막 값만 유지
                }
            }
        }

        // application/x-www-form-urlencoded 전송용으로 MultiValueMap 생성
        val formBody = LinkedMultiValueMap<String, String>().apply {
            RevokeTokenRequest::class.memberProperties
                .mapNotNull { prop -> prop.get(revokeTokenRequest)?.let { prop.name to it.toString() } }
                .forEach { (key, value) -> add(key, value) }
        }

        return requestHelper.post(
            baseUrl = baseUrl,
            path = oauthTokenUrl,
            headers = headers,
            body = formBody
        )
    }

    private fun Enumeration<String>.toList(): List<String> =
        buildList { while (this@toList.hasMoreElements()) add(this@toList.nextElement()) }
}
