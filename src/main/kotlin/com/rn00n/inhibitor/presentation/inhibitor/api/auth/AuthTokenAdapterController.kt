package com.rn00n.inhibitor.presentation.inhibitor.api.auth

import com.fasterxml.jackson.databind.ObjectMapper
import com.rn00n.inhibitor.commons.webclient.WebClientRequestHelper
import com.rn00n.inhibitor.presentation.inhibitor.api.auth.dto.GrantTokenRequest
import io.github.oshai.kotlinlogging.KotlinLogging
import io.swagger.v3.oas.annotations.Hidden
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.util.*
import kotlin.reflect.full.memberProperties

@Hidden
@RestController
@RequestMapping("/auth/token")
class AuthTokenAdapterController(
    private val objectMapper: ObjectMapper,
    private val requestHelper: WebClientRequestHelper,
) {

    private val logger = KotlinLogging.logger {}
    private val oauthTokenUrl = "/oauth2/token"

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