package com.echo.award.util

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import java.util.*

/**
 * @author MoonlightShen
 * @data 2024/9/4 9:24
 **/
object JwtUtil {

    // 生成一个随机的密钥
    private val secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256)

    // 生成JWT token
    fun generateToken(username: String): String {
        val now = Date()
        val expiration = Date(now.time + 3600000) // 1小时后过期

        return Jwts.builder()
            .setSubject(username)               // 设置主题，即用户名
            .setIssuedAt(now)                   // 设置签发时间
            .setExpiration(expiration)          // 设置过期时间
            .signWith(secretKey)                // 使用密钥签名
            .compact()                          // 生成Token字符串
    }

    // 验证JWT token
    fun validateToken(token: String): Boolean {
        try {
            val claims = Jwts.parser()
                .setSigningKey(secretKey)        // 设置密钥
                .build()
                .parseClaimsJws(token)           // 解析token
            return !claims.body.expiration.before(Date())
        } catch (e: Exception) {
            return false
        }
    }

    // 从Token中提取用户名
    fun getUsernameFromToken(token: String): String? {
        return try {
            val claims = Jwts.parser()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
            claims.body.subject
        } catch (e: Exception) {
            null
        }
    }
}