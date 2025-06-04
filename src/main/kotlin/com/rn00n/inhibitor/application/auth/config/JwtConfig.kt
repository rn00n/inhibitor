package com.rn00n.inhibitor.application.auth.config

import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey.Builder
import com.nimbusds.jose.jwk.source.ImmutableJWKSet
import com.nimbusds.jose.jwk.source.JWKSource
import com.nimbusds.jose.proc.SecurityContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.security.KeyFactory
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*

/**
 * JWT 관련 설정 정의
 *
 * - RSA 키 관리 및 변환 로직 포함
 * - Spring Security와의 연계를 위한 설정 제공
 */
@Configuration
class JwtConfig {

    companion object {
        private const val RSA_ALGORITHM = "RSA"
        private const val BEGIN_PRIVATE_KEY = "-----BEGIN PRIVATE KEY-----"
        private const val END_PRIVATE_KEY = "-----END PRIVATE KEY-----"
        private const val BEGIN_PUBLIC_KEY = "-----BEGIN PUBLIC KEY-----"
        private const val END_PUBLIC_KEY = "-----END PUBLIC KEY-----"
    }

    @Value("\${spring.security.oauth2.authorizationserver.jwt.key-id}")
    private lateinit var keyID: String // RSA 키 ID

    @Value("\${spring.security.oauth2.authorizationserver.jwt.private-key}")
    private lateinit var privateKeyString: String // RSA 개인 키 (PEM 형식)

    @Value("\${spring.security.oauth2.authorizationserver.jwt.public-key}")
    private lateinit var publicKeyString: String // RSA 공개 키 (PEM 형식)

    /**
     * JWK(Jose Web Key) 소스 생성
     *
     * - RSA 공개 키 및 개인 키를 기반으로 JWK 생성
     * - Authorization Server가 이를 통해 JWT 서명 정보를 제공
     */
    @Bean
    fun jwkSource(): JWKSource<SecurityContext> {
        val rsaPublicKey = loadPublicKey(publicKeyString)
        val rsaPrivateKey = loadPrivateKey(privateKeyString)

        // RSAPublicKey와 RSAPrivateKey를 사용하여 JWK 생성
        val rsaKey = Builder(rsaPublicKey)
            .privateKey(rsaPrivateKey)
            .keyID(keyID)
            .build()

        return ImmutableJWKSet(JWKSet(rsaKey)) // JWK 소스 반환
    }

    /**
     * 개인 키(Private Key)를 로드하는 메서드
     *
     * - 입력된 PEM 형식 키를 정리하고 RSA Key 객체로 변환
     */
    private fun loadPrivateKey(key: String): RSAPrivateKey {
        val cleanKey = cleanKeyString(key, BEGIN_PRIVATE_KEY, END_PRIVATE_KEY)
        return generateRSAKey(cleanKey) { keyFactory, decodedKey ->
            keyFactory.generatePrivate(PKCS8EncodedKeySpec(decodedKey)) as RSAPrivateKey
        }
    }

    /**
     * 공개 키(Public Key)를 로드하는 메서드
     *
     * - 입력된 PEM 형식 키를 정리하고 RSA Key 객체로 변환
     */
    private fun loadPublicKey(key: String): RSAPublicKey {
        val cleanKey = cleanKeyString(key, BEGIN_PUBLIC_KEY, END_PUBLIC_KEY)
        return generateRSAKey(cleanKey) { keyFactory, decodedKey ->
            keyFactory.generatePublic(X509EncodedKeySpec(decodedKey)) as RSAPublicKey
        }
    }

    /**
     * PEM 형식 키를 클린 업
     *
     * - 키에서 불필요한 정보들(헤더, 공백 등)을 제거
     */
    private fun cleanKeyString(key: String, beginMarker: String, endMarker: String): String =
        key.replace(beginMarker, "")
            .replace(endMarker, "")
            .replace("\\s".toRegex(), "") // 공백 제거

    /**
     * RSA 키 객체 생성
     *
     * - Base64로 인코딩된 키 데이터를 디코딩한 후
     * - RSA KeyFactory를 통해 RSAPrivateKey 또는 RSAPublicKey 생성
     */
    private fun <T> generateRSAKey(cleanKey: String, keyGenerator: (KeyFactory, ByteArray) -> T): T {
        val decoded = Base64.getDecoder().decode(cleanKey) // Base64 디코딩
        val keyFactory = KeyFactory.getInstance(RSA_ALGORITHM) // RSA 키팩토리 생성
        return keyGenerator(keyFactory, decoded) // KeyFactory를 사용하여 Key 생성
    }
}