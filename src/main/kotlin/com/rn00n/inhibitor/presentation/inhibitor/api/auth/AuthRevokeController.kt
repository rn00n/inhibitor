package com.rn00n.inhibitor.presentation.inhibitor.api.auth

import com.rn00n.inhibitor.commons.webclient.WebClientRequestHelper
import com.rn00n.inhibitor.presentation.inhibitor.api.auth.dto.RevokeTokenRequest
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
@RequestMapping("/auth/revoke")
class AuthRevokeController(
    private val requestHelper: WebClientRequestHelper,
) {

    private val oauthTokenUrl = "/oauth2/revoke"

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
