package com.echo.award.service

import jakarta.annotation.Resource
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * @author MoonlightShen
 * @data 2024/9/5 15:58
 **/
@Service
open class UserService(private val redisService: RedisService) {

    private val USERNAME_KEY: String = "userName"  // Redis中存储用户名的键
    private val PASSWORD_KEY: String = "password"  // Redis中存储用户名的键

    @Transactional
    open fun getUsername(): Any {
        return redisService.getValue(USERNAME_KEY)
            ?: throw Exception("key not found in Redis")
    }

    @Transactional
    open fun updateUsername(newUsername: String) {
        redisService.setValue(USERNAME_KEY, newUsername)
    }


    @Transactional
    open fun getPassword(): Any {
        return redisService.getValue(PASSWORD_KEY)
            ?: throw Exception("key not found in Redis")
    }

    @Transactional
    open fun updatePassword(newPassword: String) {
        redisService.setValue(PASSWORD_KEY, newPassword)
    }

    @Transactional
    open fun validatePassword(password: String): Boolean {
        return redisService.getValue(PASSWORD_KEY) == password
    }
}