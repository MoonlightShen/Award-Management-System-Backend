package com.echo.award.controller

import org.springframework.web.bind.annotation.*
import com.echo.award.common.result.Result
import com.echo.award.request.UserRequest
import com.echo.award.service.RedisService
import com.echo.award.service.UserService
import com.echo.award.util.JwtUtil
import org.springframework.data.redis.core.StringRedisTemplate

/**
 * @author MoonlightShen
 * @data 2024/9/4 8:52
 **/
@RestController
@RequestMapping("/auth")
class AuthController(
    private val userService: UserService,
    private val redisService: RedisService
) {

    @PostMapping("/login")
    fun login(@RequestBody data: UserRequest): Result<out Any> {
        return if (userService.validatePassword(data.password!!)) {
            val userName: String = userService.getUsername().toString()
            val token = JwtUtil.generateToken(userName)
            redisService.setValue("token", token)
            Result.success(UserRequest(userName, token, null))
        } else {
            Result.fail("0000")
        }
    }
}