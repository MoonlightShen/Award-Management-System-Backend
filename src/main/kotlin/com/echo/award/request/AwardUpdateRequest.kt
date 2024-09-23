package com.echo.award.request

/**
 * @author MoonlightShen
 * @data 2024/9/6 15:09
 **/
data class AwardUpdateRequest(val awardId:Int,val awardCategoryId:Int?, val raceLevel:Int?, val awardName:String?,
                              val awardLevel:Int?, val ranking:Int?, val acquisitionTime:Long?)