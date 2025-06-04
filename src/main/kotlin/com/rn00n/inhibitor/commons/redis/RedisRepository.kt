package com.rn00n.inhibitor.commons.redis

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.redis.core.StringRedisTemplate
import java.time.Duration

//@Component
class RedisRepository(
    private val redisTemplate: StringRedisTemplate,
    private val objectMapper: ObjectMapper
) {

    /**
     * 데이터를 Redis에 저장 (제네릭 타입 지원)
     * @param key Redis 키 값
     * @param value 저장할 객체 (T)
     * @param duration 데이터 만료 시간 (TTL)
     */
    fun <T> save(key: String, value: T, duration: Duration) {
        val jsonValue = objectMapper.writeValueAsString(value)
        redisTemplate.opsForValue().set(key, jsonValue, duration)
    }

    /**
     * Redis에서 데이터를 조회 후 자동으로 변환 (제네릭 타입 지원)
     * @param key Redis 키 값
     * @param clazz 변환할 클래스 타입
     * @return 변환된 객체 (T?) | 존재하지 않으면 null 반환
     */
    fun <T> find(key: String, clazz: Class<T>): T? {
        val jsonValue = redisTemplate.opsForValue().get(key) ?: return null
        return objectMapper.readValue(jsonValue, clazz)
    }

    /**
     * Redis에서 데이터를 삭제
     * @param key Redis 키 값
     */
    fun delete(key: String) {
        redisTemplate.delete(key)
    }
}