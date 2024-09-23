package com.echo.award.controller

import com.echo.award.common.result.Result
import com.echo.award.request.UserPasswordUpdateRequest
import com.echo.award.request.UserRequest
import com.echo.award.response.ImageResponse
import com.echo.award.service.UserService
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException
import java.awt.Image
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.security.MessageDigest
import javax.lang.model.type.NullType

/**
 * @author MoonlightShen
 * @data 2024/9/5 15:44
 **/
@RestController
@RequestMapping("/user")
class UserController(private val userService: UserService) {

    @PostMapping("/updateUserName")
    fun updateUsername(
        @RequestBody request: UserRequest
    ): Result<NullType> {
        return try {
            userService.updateUsername(request.userName!!)
            Result.successOption()
        } catch (e: Exception) {
            Result.fail("0000")
        }
    }

    @PostMapping("/updatePassword")
    fun updatePassword(
        @RequestBody request: UserPasswordUpdateRequest
    ): Result<NullType> {
        return try {
            if(userService.getPassword() == request.oldPassword){
                userService.updatePassword(request.newPassword)
                Result.successOption()
            }else{
                Result.fail("密码错误")
            }
        } catch (e: Exception) {
            Result.fail("0000")
        }
    }


    @PostMapping("/getAvatar")
    fun getAvatar(): Any {
        return try {
            val currentDir = System.getProperty("user.dir")
            val uploadDir = "$currentDir/uploads/"
            val filePath = uploadDir + MessageDigest.getInstance("MD5").digest("avatar".toByteArray()).joinToString(""){ "%02x".format(it) } + ".png"

            if (Files.exists(Paths.get(filePath))) {
                Result.success(ImageResponse(Files.readAllBytes(Paths.get(filePath))))
            } else {
                Result.fail("文件不存在")
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Result.fail("文件读取失败")
        }
    }

    @PostMapping("/uploadAvatar")
    fun uploadAvatar(@RequestParam("file") file: MultipartFile): Result<NullType> {
        if (file.isEmpty) {
            return Result.fail("文件为空")
        }
        val currentDir = System.getProperty("user.dir")

        val uploadDir = "$currentDir/uploads/"

        try {
            val dir = File(uploadDir)
            if (!dir.exists()) {
                dir.mkdirs()
            }

            val filePath = uploadDir + MessageDigest.getInstance("MD5").digest("avatar".toByteArray()).joinToString(""){ "%02x".format(it) } + ".png"
            val dest = File(filePath)

            file.transferTo(dest)

            return Result.successOption()

        } catch (e: IOException) {
            e.printStackTrace()
            return Result.fail("0000")
        }
    }
}