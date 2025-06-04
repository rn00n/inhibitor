package com.rn00n.inhibitor.commons.redis

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class RedisConfig {

    /**
     * RedisTemplate<String, Any> 등록
     * @param redisConnectionFactory Spring Boot가 제공하는 기본 커넥션 팩토리 사용
     */
    @Bean
    fun redisTemplate(
        redisConnectionFactory: RedisConnectionFactory,
        objectMapper: ObjectMapper
    ): RedisTemplate<String, Any> {
        val template = RedisTemplate<String, Any>()
        template.connectionFactory = redisConnectionFactory

        // Key 직렬화는 String
        template.keySerializer = StringRedisSerializer()

        // Value 직렬화는 JSON (Spring 표준)
        val genericSerializer = GenericJackson2JsonRedisSerializer(objectMapper)
        template.valueSerializer = genericSerializer

        // Hash에 대한 직렬화도 통일
        template.hashKeySerializer = StringRedisSerializer()
        template.hashValueSerializer = genericSerializer

        template.afterPropertiesSet()
        return template
    }

    /**
     * StringRedisTemplate 등록 (단순 String <-> String 저장 용도)
     */
    @Bean
    fun stringRedisTemplate(redisConnectionFactory: RedisConnectionFactory): StringRedisTemplate {
        return StringRedisTemplate(redisConnectionFactory)
    }
}