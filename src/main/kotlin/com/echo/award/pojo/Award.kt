package com.echo.award.pojo

/**
 * @author MoonlightShen
 * @data 2024/9/3 16:11
 **/
data class Award(
    val awardId: Int?, var awardCategoryId: Int?, var raceLevel: Int?,
    var awardName: String?, var awardLevel: Int?, var ranking: Int?, var acquisitionTime: Long?
)