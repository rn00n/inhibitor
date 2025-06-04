package com.rn00n.inhibitor.presentation.inhibitor.api.auth

import com.fasterxml.jackson.databind.ObjectMapper
import com.rn00n.inhibitor.commons.webclient.WebClientRequestHelper
import com.rn00n.inhibitor.presentation.inhibitor.api.auth.dto.GrantTokenRequest
import com.rn00n.inhibitor.presentation.inhibitor.api.auth.dto.TokenResponse
import io.github.oshai.kotlinlogging.KotlinLogging
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
 * 이는 OAuth 2.0 명세 (RFC 6749) Section 4.3.2에 따라,
 * **토큰 요청은 반드시 `application/x-www-form-urlencoded` 형식의 요청 본문으로 전송되어야 한다**는
 * 스펙을 준수하기 위함이다. JSON 형식은 스펙에 명시되지 않은 방식으로,
 * 내부 시스템에서의 일관된 REST API 요청 형식을 유지하기 위해 어댑터 계층으로 처리된다.
 *
 * > RFC 6749 - Section 4.3.2:
 * > “The client makes a request to the token endpoint by adding the following parameters
 * > using the `application/x-www-form-urlencoded` format per Appendix B in the HTTP request entity-body.”
 *
 * 이 컨트롤러는 인증 도메인의 경계를 유지하면서도,
 * 다양한 내부 클라이언트(Web, iOS, Android 등)의 요청 형식을 표준에 맞게 적절히 변환하여
 * 인증 흐름의 안정성과 유연성을 동시에 보장하는 전략적 어댑터 역할을 수행한다.
 *
 * @see https://datatracker.ietf.org/doc/html/rfc6749#section-4.3.2
 */
@Tag(
    name = "Token Adapter",
    description = """
    **OAuth2 인증 서버에 토큰 발급 요청을 중계하는 어댑터입니다.**
    
    - 클라이언트 요청은 `application/json` 형식으로 받습니다.
    - 내부 인증 서버에는 `x-www-form-urlencoded` 형식으로 전달됩니다.
    - 다양한 `grant_type`에 따라 요청 예시를 참고하십시오.
    """
)
@RestController
@RequestMapping("/oauth2/token")
class OAuth2TokenAdapterController(
    private val objectMapper: ObjectMapper,
    private val requestHelper: WebClientRequestHelper,
) {

    private val logger = KotlinLogging.logger {}
    private val oauthTokenUrl = "/oauth2/token"

    @SecurityRequirement(name = "basicAuth")
    @Operation(
        summary = "OAuth2 토큰 발급 어댑터",
        description = """
            - 요청은 JSON 형식으로 받으며, 내부적으로 x-www-form-urlencoded 형태로 변환되어 전달됩니다.
            - 인증 서버는 `Authorization: Basic base64(clientId:clientSecret)` 헤더를 요구합니다.
        """,
        parameters = [
            Parameter(
                name = HttpHeaders.AUTHORIZATION,
                `in` = ParameterIn.HEADER,
                required = true,
                description = "Basic 인증 헤더 (Base64 encoded: clientId:clientSecret)",
                example = "Basic bGV6aGluLWlvczpLdCM3NGJtWnhVcU53MjltRnlkTC1XRTNxOXZNSnBYdA=="
            ), Parameter(
                name = "X-LZ-Platform",
                `in` = ParameterIn.HEADER,
                required = false,
                description = "",
                example = "KG, KN, KT, KP, KX, KA"
            ), Parameter(
                name = "platform",
                `in` = ParameterIn.HEADER,
                required = false,
                description = "X-LZ-Platform 안쓰고 이거 쓰셔도 됩니다.",
                example = "web, android, ios"
            ), Parameter(
                name = "store",
                `in` = ParameterIn.HEADER,
                required = false,
                description = "",
                example = "[web, plus...]"
            ), Parameter(
                name = "deviceId",
                `in` = ParameterIn.HEADER,
                required = false,
                description = ")",
                example = "device code"
            ), Parameter(
                name = "X-LZ-Locale",
                `in` = ParameterIn.HEADER,
                required = false,
                description = "",
                example = "ko-KR, ja-JP, en-US"
            ), Parameter(
                name = "locale",
                `in` = ParameterIn.HEADER,
                required = false,
                description = "X-LZ-Locale 안쓰고 이것쓰셔도 됩니다.",
                example = "ko-KR, ja-JP, en-US"
            ), Parameter(
                name = "countryCode",
                `in` = ParameterIn.HEADER,
                required = false,
                description = "",
                example = "KR"
            )
        ],
        requestBody = SwaggerRequestBody(
            required = true,
            content = [
                Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = GrantTokenRequest::class),
                    examples = [
                        ExampleObject(
                            name = "origin_password",
                            summary = "Password Grant",
                            value = """{
                                "grant_type": "origin_password",
                                "username": "user@rn00n.com",
                                "password": "secret",
                                "scope": "read write"
                            }"""
                        ),
                        ExampleObject(
                            name = "origin_social",
                            summary = "Social Grant",
                            value = """{
                                "grant_type": "origin_social",
                                "idToken": "line", 
                                "accessToken": "naver, kakao, line, facebook, google, apple, yahoojapan",
                                "refreshToken": "naver, kakao, line, facebook, google, apple, yahoojapan",
                                "expires": 3600,
                                "oauthToken": "twitter", 
                                "oauthTokenSecret": "twitter",
                                "socialType": "kakao",
                                "scope": "read write"
                            }"""
                        ),
                        ExampleObject(
                            name = "origin_refresh_token",
                            summary = "Refresh Token Grant",
                            value = """{
                                "grant_type": "origin_refresh_token",
                                "refresh_token": "abcdef123456",
                                "scope": "read write"
                            }"""
                        ),
                        ExampleObject(
                            name = "origin_reissue_token",
                            summary = "Reissue Token Grant",
                            value = """{
                                "grant_type": "origin_reissue_token",
                                "refresh_token": "abcdef123456",
                                "scope": "read write"
                            }"""
                        )
                    ]
                )
            ]
        ),
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "토큰 발급 성공",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = TokenResponse::class),
                        examples = [
                            ExampleObject(
                                name = "success",
                                value = """{
                                    "access_token": "{{jwt}}",
                                    "refresh_token": "{{base64}}",
                                    "clientId": "[inhibitor]",
                                    "scope": "read write",
                                    "token_type": "Bearer",
                                    "expires_in": 7199,
                                    "userId": 6433700666671104,
                                    "isPasswordChangeRequired": false
                                }"""
                            )
                        ]
                    )
                ]
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 인증 정보 (예: 비밀번호 틀림)",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        examples = [
                            ExampleObject(
                                name = "invalid_grant",
                                value = """{
                                    "status": "400",
                                    "error": "1104",
                                    "description": "description of error",
                                    "code": "1104"
                                }"""
                            )
                        ]
                    )
                ]
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
        ],
    )
    @PostMapping
    fun proxyTokenRequest(
        @RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String,
        @RequestBody grantTokenRequest: GrantTokenRequest,
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
            GrantTokenRequest::class.memberProperties
                .mapNotNull { prop -> prop.get(grantTokenRequest)?.let { prop.name to it.toString() } }
                .forEach { (key, value) -> add(key, value) }
        }

        try {
            val request = mapOf("headers" to headers, "body" to formBody)
            objectMapper.writeValueAsString(request).let { logger.info { "request: $it" } }
        } catch (e: Exception) {
            logger.warn { "request: ${e.message}" }
        }

        return requestHelper.post(
            baseUrl = baseUrl,
            path = oauthTokenUrl,
            headers = headers,
            body = formBody
        )
    }

    // 확장 함수: Enumeration<String> → List<String>
    private fun Enumeration<String>.toList(): List<String> =
        buildList { while (this@toList.hasMoreElements()) add(this@toList.nextElement()) }
}