package com.echo.award.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service

/**
 * @author MoonlightShen
 * @data 2024/9/3 17:13
 **/
@Component
class RedisService @Autowired constructor(
    private val redisTemplate: RedisTemplate<String, Any>
) {
    fun setValue(key: String, value: Any) {
        redisTemplate.opsForValue().set(key, value)
    }

    fun getValue(key: String): Any? {
        return redisTemplate.opsForValue().get(key)
    }
}