package com.echo.award.controller

import com.echo.award.common.result.Result
import com.echo.award.pojo.AwardCategory
import com.echo.award.request.AwardInsertRequest
import com.echo.award.request.UserRequest
import com.echo.award.service.AwardCategoryService
import com.echo.award.service.RedisService
import com.echo.award.service.UserService
import com.echo.award.util.JwtUtil
import com.echo.award.util.MySQLUtil
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.sql.SQLException
import javax.lang.model.type.NullType

/**
 * @author MoonlightShen
 * @data 2024/9/6 10:53
 **/

@RestController
@RequestMapping("/awardCategory")
class AwardCategoryController(
    private val awardCategoryService: AwardCategoryService,
) {

    @PostMapping("/getAll")
    fun getAllAwardCategories(): Any {
        return try {
            Result.success(awardCategoryService.getAllAwardCategories())
        }catch (e:Exception){
            Result.fail("0000")
        }
    }

    @PostMapping("/insert")
    fun insertAwardCategory(@RequestBody body: AwardCategory): Result<NullType> {
        return try {
            if(awardCategoryService.insertAwardCategory(body)){
                Result.successOption()
            }else{
                Result.fail("0000")
            }
        } catch (e: SQLException) {
            e.printStackTrace()
            Result.fail("0000")
        }
    }

    @PostMapping("/updateById")
    fun updateAwardCategory(@RequestBody body:AwardCategory): Result<NullType> {
        return try {
            val result = awardCategoryService.updateAwardCategory(body)
            if(result){
                Result.successOption()
            }else{
                Result.fail("0000")
            }
        }catch (e:Exception){
            Result.fail("0000")
        }
    }


    @PostMapping("/deleteById")
    fun deleteAwardCategory(@RequestBody body:AwardCategory): Result<NullType> {
        return try{
            if(awardCategoryService.deleteAwardCategory(body)){
                Result.successOption()
            }else{
                Result.fail("0000")
            }
        }catch (e:Exception){
            Result.fail("0000")
        }
    }
}