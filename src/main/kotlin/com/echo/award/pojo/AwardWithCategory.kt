package com.echo.award.pojo

/**
 * @author MoonlightShen
 * @data 2024/9/6 15:34
 **/
data class AwardWithCategory(
    val awardId: Int?, var awardCategory: AwardCategory?, var raceLevel: Int?,
    var awardName: String?, var awardLevel: Int?, var ranking: Int?, var acquisitionTime: Long?
)