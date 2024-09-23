package com.echo.award.controller

import com.echo.award.common.result.Result
import com.echo.award.pojo.Award
import com.echo.award.pojo.AwardCategory
import com.echo.award.pojo.AwardCategoryDetail
import com.echo.award.pojo.AwardWithCategory
import com.echo.award.request.*
import com.echo.award.response.AwardCount
import com.echo.award.response.FileResponse
import com.echo.award.service.AwardCategoryService
import com.echo.award.service.AwardService
import com.echo.award.util.MySQLUtil
import org.springframework.core.io.FileSystemResource
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.security.MessageDigest
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Timestamp
import javax.lang.model.type.NullType

/**
 * @author MoonlightShen
 * @data 2024/9/6 11:35
 **/
@RestController
@RequestMapping("/award")
class AwardController(
    val awardService: AwardService,
) {

    @PostMapping("/getAwardCategoryDetails")
    fun getAllAwardCategoryDetails(): Any {
        return try {
            Result.success(awardService.getAllAwardCategoryDetails())
        }catch (e:Exception){
            Result.fail("0000")
        }
    }

    @PostMapping("/getTotal")
    fun getTotalNumber(): Result<AwardCount> {
        return Result.success(AwardCount(awardService.getTotalNumber()))
    }

    @PostMapping("/getByPage")
    fun getAwardByPage(@RequestBody body: GetByPageRequest): Any {
        return try {
            Result.success(awardService.getAwardByPage(body.page, body.pageSize))
        }catch (e:Exception){
            Result.fail("0000")
        }
    }

//    {
//        "awardCategoryId": 85,
//        "raceLevel": 1,
//        "awardName": "算目油该约",
//        "awardLevel": 1,
//        "ranking": 47,
//        "acquisitionTime": 1529418759505,
//        "image": "http://dummyimage.com/400x400"
//    }

    @PostMapping("/insert")
    fun insertAward(@RequestParam("awardCategoryId") awardCategoryId: Int,
                    @RequestParam("raceLevel") raceLevel: Int,
                    @RequestParam("awardName") awardName: String,
                    @RequestParam("awardLevel") awardLevel: Int,
                    @RequestParam("ranking", required = false) ranking: Int,
                    @RequestParam("acquisitionTime") acquisitionTime: Long,
                    @RequestParam("file") file: MultipartFile): Result<NullType> {
        return try {
            if(awardService.insertAward(AwardInsertRequest(awardCategoryId, raceLevel, awardName,
                    awardLevel, ranking, acquisitionTime), file)){
                Result.successOption()
            }else{
                Result.fail("0000")
            }
        } catch (e: SQLException) {
            e.printStackTrace()
            Result.fail("0000")
        }
    }

    @PostMapping("/deleteById")
    fun deleteAward(@RequestBody body: AwardDeleteRequest): Result<NullType> {
        return try {
            if(awardService.deleteAward(body.awardId)){
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
    fun updateAward(@RequestBody body: AwardUpdateRequest): Result<NullType> {
        return try {
            if(awardService.updateAward(body)){
                Result.successOption()
            }else{
                Result.fail("0000")
            }
        } catch (e: SQLException) {
            e.printStackTrace()
            Result.fail("0000")
        }
    }

    @PostMapping("/downloadFile")
    fun downloadFile(@RequestBody body: AwardDeleteRequest): Any {
        return try {
            val currentDir = System.getProperty("user.dir")

            val filesDir = "$currentDir/files/"

                val dir = File(filesDir)
                if (!dir.exists()) {
                    dir.mkdirs()
                }

            val filePath = Paths.get(filesDir + MessageDigest.getInstance("MD5").digest("file_${body.awardId}".toByteArray()).joinToString(""){ "%02x".format(it) } + ".png")
            val fileResource = FileSystemResource(filePath)

            if (!fileResource.exists()) {
                return Result.fail("文件不存在")
            }

            Result.success(FileResponse(fileResource.inputStream.readBytes()))
        } catch (e: IOException) {
            e.printStackTrace()
            Result.fail("0000")
        }
    }


    @PostMapping("/recentAwards")
    fun recentAwards(): Any {
        return try {
            Result.success(awardService.getRecentAwards())
        } catch (e: IOException) {
            e.printStackTrace()
            Result.fail("0000")
        }
    }

    @PostMapping("/thisYearAwards")
    fun thisYearAwards(): Any {
        return try {
            Result.success(awardService.getThisYearAwards())
        } catch (e: IOException) {
            e.printStackTrace()
            Result.fail("0000")
        }
    }

    @PostMapping("/exportExcel")
    fun exportExcel( @RequestBody body: ExportExcelRequest): Any {
        return try {
            Result.success(FileResponse(awardService.exportExcel(body.selectedIds)))
        } catch (e: IOException) {
            e.printStackTrace()
            Result.fail("0000")
        }
    }

    @PostMapping("/exportZip")
    fun exportZip( @RequestBody body: ExportExcelRequest): Any {
        return try {
            Result.success(FileResponse(awardService.exportZip(body.selectedIds)))
        } catch (e: IOException) {
            e.printStackTrace()
            Result.fail("0000")
        }
    }
}