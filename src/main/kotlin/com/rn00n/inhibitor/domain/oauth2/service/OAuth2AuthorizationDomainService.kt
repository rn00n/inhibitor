package com.rn00n.inhibitor.domain.oauth2.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.rn00n.inhibitor.domain.oauth2.OAuth2Authorization
import com.rn00n.inhibitor.domain.oauth2.repo.OAuth2AuthorizationRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration

@Service
class OAuth2AuthorizationDomainService(
    private val repository: OAuth2AuthorizationRepository,
    private val objectMapper: ObjectMapper,
    private val redisTemplate: RedisTemplate<String, String>,
) {

    private val logger = KotlinLogging.logger {}
    private val blacklistTtl: Duration = Duration.ofDays(1)

    @Transactional(transactionManager = "comicsUserTransactionManager")
    fun revoke(oAuth2Authorization: OAuth2Authorization) {
        repository.delete(oAuth2Authorization)
    }

    @Transactional(transactionManager = "comicsUserTransactionManager")
    fun revokeRefreshToken(token: String) {
        repository.deleteByRefreshTokenValue(token)
    }

    fun findByRefreshTokenValue(token: String): OAuth2Authorization? {
        return repository.findByRefreshTokenValue(token)
    }

    @Transactional(transactionManager = "comicsUserTransactionManager")
    fun revokeTokensByPrincipalName(principalName: String) {
        val authorizations = repository.findByPrincipalName(principalName)

        val jtis = authorizations.mapNotNull { extractJti(it.accessTokenMetadata) }

        if (jtis.isNotEmpty()) {
            addToBlacklistBulk(jtis)
        }

        val deletedCount = repository.deleteAllByPrincipalName(principalName)
        logger.info { "Revoked $deletedCount tokens for principal $principalName" }
    }

    private fun addToBlacklistBulk(jtiList: List<String>) {
        val serializer = redisTemplate.stringSerializer

        redisTemplate.executePipelined { connection ->
            jtiList.forEach { jti ->
                val key = "jti:$jti"
                connection.stringCommands().setEx(
                    serializer.serialize(key)!!,
                    blacklistTtl.seconds,
                    serializer.serialize("true")!!
                )
            }
            null
        }

        logger.info { "Blacklisted ${jtiList.size} jti(s) in bulk (TTL=$blacklistTtl)" }
    }

    private fun extractJti(metadataJson: String?): String? {
        if (metadataJson.isNullOrBlank()) return null
        return try {
            val root = objectMapper.readTree(metadataJson)
            root["metadata.token.claims"]?.get("jti")?.asText()
        } catch (e: Exception) {
            logger.warn(e) { "Failed to extract jti from metadata" }
            null
        }
    }
}
