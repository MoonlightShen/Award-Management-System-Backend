package com.echo.award.common.result

import com.echo.award.util.JwtUtil
import javax.lang.model.type.NullType

/**
 * @author MoonlightShen
 * @data 2024/9/4 9:10
 **/
class Result<T>(val code:String, val data:T?) {

    companion object{
        private const val SUCCESS_CODE: String = "1000"

        fun <T> success(data: T): Result<T> {
            return Result<T>(SUCCESS_CODE, data = data)
        }

        fun successOption(): Result<NullType> {
            return Result<NullType>(SUCCESS_CODE, data = null)
        }

        fun fail(code: String): Result<NullType> {
            return Result(code, data = null)
        }
    }
}