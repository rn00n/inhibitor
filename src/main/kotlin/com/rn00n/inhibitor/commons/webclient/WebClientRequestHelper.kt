package com.rn00n.inhibitor.commons.webclient

import io.github.oshai.kotlinlogging.KotlinLogging
import org.slf4j.MDC
import org.springframework.http.*
import org.springframework.stereotype.Component
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.util.retry.Retry
import java.io.IOException
import java.util.*

/**
 * WebClientRequestHelper
 * ------------------------
 * WebClient 기반의 HTTP 요청을 유연하게 처리하는 공용 헬퍼 클래스입니다.
 * 이 클래스는 WebFlux 환경이 아닌 일반 MVC 기반에서도 사용 가능하도록 설계되었으며,
 * 모든 HTTP 메서드에 대한 표준화된 요청 전송 기능을 제공합니다.
 *
 * - requestId 자동 주입
 * - JSON/Form 전송 자동 처리
 * - 에러 응답 통일
 * - WebClient 재사용
 */
@Component
class WebClientRequestHelper(
    private val builder: WebClient.Builder
) {

    private val logger = KotlinLogging.logger {}

    // 공통 요청 처리 메서드
    private fun execute(
        baseUrl: String,
        method: HttpMethod,
        path: String,
        headers: Map<String, String> = emptyMap(),
        params: Map<String, String> = emptyMap(),
        body: Any? = null
    ): Mono<ResponseEntity<String>> {
        val client = builder.baseUrl(baseUrl).build()

        // requestId 자동 생성 및 헤더에 삽입
        val requestId = MDC.get("requestId") ?: UUID.randomUUID().toString()
        val httpHeaders = HttpHeaders().apply {
            headers.forEach { (k, v) -> add(k, v) }
            this["X-LZ-Request-Id"] = requestId
        }

        // URI 및 파라미터 세팅
        val uriSpec = client.method(method)
        val requestSpec = uriSpec.uri { uriBuilder ->
            uriBuilder.path(path)
            params.forEach { (key, value) -> uriBuilder.queryParam(key, value) }
            uriBuilder.build()
        }.headers { it.addAll(httpHeaders) }

        // 바디 처리 (JSON 또는 FORM 자동 분기)
        val bodySpec = when (body) {
            is MultiValueMap<*, *> -> requestSpec
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(body)

            is Map<*, *> -> requestSpec
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)

            is String -> requestSpec
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)

            null -> requestSpec
            else -> requestSpec.bodyValue(body)
        }

        return bodySpec
            .exchangeToMono { response ->
                response.bodyToMono(String::class.java)
                    .map { responseBody ->
                        logger.info { "response: $responseBody" }
                        ResponseEntity
                            .status(response.statusCode())
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(responseBody)
                    }
            }
            .retryWhen(
                Retry.max(1)
                    .filter { ex ->
                        ex is IOException ||
                                ex.message?.contains("Connection reset by peer", ignoreCase = true) == true ||
                                ex.message?.contains("Connection refused") == true
                    }
                    .doBeforeRetry { signal ->
                        logger.warn { "Retrying due to peer reset: attempt=${signal.totalRetries() + 1}" }
                    }
            )
            .onErrorResume { ex ->
                val message = ex.message ?: "Unknown error"
                Mono.just(
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("{\"error\": \"$message\"}")
                )
            }
    }

    // GET 요청 전송
    fun get(
        baseUrl: String,
        path: String,
        headers: Map<String, String> = emptyMap(),
        params: Map<String, String> = emptyMap()
    ): Mono<ResponseEntity<String>> =
        execute(baseUrl, HttpMethod.GET, path, headers, params, null)

    // POST 요청 전송 (유연한 바디 처리)
    fun post(
        baseUrl: String,
        path: String,
        headers: Map<String, String> = emptyMap(),
        params: Map<String, String> = emptyMap(),
        body: Any? = null
    ): Mono<ResponseEntity<String>> =
        execute(baseUrl, HttpMethod.POST, path, headers, params, body)

    // PUT 요청 전송
    fun put(
        baseUrl: String,
        path: String,
        headers: Map<String, String> = emptyMap(),
        params: Map<String, String> = emptyMap(),
        body: Any? = null
    ): Mono<ResponseEntity<String>> =
        execute(baseUrl, HttpMethod.PUT, path, headers, params, body)

    // PATCH 요청 전송
    fun patch(
        baseUrl: String,
        path: String,
        headers: Map<String, String> = emptyMap(),
        params: Map<String, String> = emptyMap(),
        body: Any? = null
    ): Mono<ResponseEntity<String>> =
        execute(baseUrl, HttpMethod.PATCH, path, headers, params, body)

    // DELETE 요청 전송
    fun delete(
        baseUrl: String,
        path: String,
        headers: Map<String, String> = emptyMap(),
        params: Map<String, String> = emptyMap()
    ): Mono<ResponseEntity<String>> =
        execute(baseUrl, HttpMethod.DELETE, path, headers, params, null)
}
