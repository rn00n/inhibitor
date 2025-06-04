package com.rn00n.inhibitor.commons.webclient

import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import reactor.netty.resources.ConnectionProvider
import java.time.Duration

@Configuration
class WebClientConfig {

    @Bean
    fun webClient(): WebClient {
        // 커넥션 풀 설정: 풀 이름 및 동시 커넥션 수, 대기 요청 제한 등
        val connectionProvider = ConnectionProvider.builder("auth-service-webclient-pool")
            .maxConnections(300) // 동시에 유지 가능한 최대 커넥션 수
            .pendingAcquireMaxCount(1000) // 커넥션 풀이 가득 찼을 때 대기 가능한 요청 수
            .pendingAcquireTimeout(Duration.ofSeconds(5)) // 대기 요청의 최대 허용 시간
            .maxIdleTime(Duration.ofSeconds(20)) // 커넥션이 사용되지 않고 유지되는 시간
            .maxLifeTime(Duration.ofMinutes(2)) // 커넥션의 최대 생존 시간 (재사용을 막기 위함)
            .evictInBackground(Duration.ofSeconds(20)) // 유휴 커넥션 정리 주기
            .lifo() // 최근 사용된 커넥션부터 재사용 (Last-In-First-Out)
            .build()

        // Netty HttpClient 구성
        val httpClient = HttpClient.create(connectionProvider)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2000) // 서버 연결 시도 제한 시간 (ms)
            .responseTimeout(Duration.ofSeconds(10)) // 서버 응답까지 기다릴 최대 시간
            .compress(true) // gzip 압축 허용
            .keepAlive(true) // 커넥션 재사용 활성화
            .doOnConnected { conn ->
                conn.addHandlerLast(ReadTimeoutHandler(3)) // 데이터 수신 timeout
                    .addHandlerLast(WriteTimeoutHandler(3)) // 데이터 전송 timeout
            }

        return WebClient.builder()
            .clientConnector(ReactorClientHttpConnector(httpClient)) // Netty 기반 커넥터 연결
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE) // 기본 Content-Type 설정
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE) // Accept 헤더 지정
            .defaultHeader(HttpHeaders.CONNECTION, "keep-alive") // 커넥션 재사용 요청
            .build()
    }

}