package com.echo.award.service

import com.echo.award.common.result.Result
import com.echo.award.pojo.Award
import com.echo.award.pojo.AwardCategory
import com.echo.award.pojo.AwardCategoryDetail
import com.echo.award.pojo.AwardWithCategory
import com.echo.award.request.AwardInsertRequest
import com.echo.award.request.AwardUpdateRequest
import com.echo.award.util.MySQLUtil
import org.apache.commons.io.output.ByteArrayOutputStream
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.core.io.InputStreamResource
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile
import java.io.*
import java.nio.file.Files
import java.nio.file.Paths
import java.security.MessageDigest
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * @author MoonlightShen
 * @data 2024/9/9 8:55
 **/
@Service
open class AwardService(private val mySQLUtil: MySQLUtil) {

    @Transactional
    open fun getAllAwardCategoryDetails(): ArrayList<AwardCategoryDetail> {
        val awardsResult = mySQLUtil.executeQuery("SELECT * FROM award")

        val details = HashMap<Int, Int>()

        val awardCategoryResult = mySQLUtil.executeQuery("SELECT * FROM award_category")

        awardCategoryResult?.let {
            while (awardCategoryResult.next()) {
                details[awardCategoryResult.getInt("award_category_id")] = 0
            }
        }

        awardsResult?.let {
            while (awardsResult.next()) {
                details[awardsResult.getInt("award_category_id")] =
                   details[awardsResult.getInt("award_category_id")]!! + 1
            }
        }

        awardsResult?.close()

        val awardCategoryDetails = ArrayList<AwardCategoryDetail>()

        for (key in details.keys) {
            val awardCategoryResult =
                mySQLUtil.executeQuery(
                    String.format(
                        "SELECT award_category_name FROM award_category WHERE award_category_id = %s",
                        key
                    )
                )
            val currentDir = System.getProperty("user.dir")
            val uploadDir = "$currentDir/uploads/"
            val filePath =
                uploadDir + MessageDigest.getInstance("MD5").digest(("award_category_icon$key").toByteArray())
                    .joinToString("") { "%02x".format(it) } + ".png"
            val awardCategory: AwardCategory? = if (awardCategoryResult != null && awardCategoryResult.next()) {
                AwardCategory(
                    key,
                    awardCategoryResult.getString("award_category_name"),
                    null,
                    Files.readAllBytes(Paths.get(filePath))
                )
            } else {
                null
            }
            awardCategoryDetails.add(AwardCategoryDetail(awardCategory!!, details[key]!!))
            awardCategoryResult?.close()
        }
        return awardCategoryDetails
    }


    @Transactional
    open fun getTotalNumber(): Int {
        val awardsResult = mySQLUtil.executeQuery("SELECT * FROM award")
        var count = 0
        awardsResult?.let {
            while (awardsResult.next()) {
                count++
            }
        }
        return count
    }

    @Transactional
    open fun getAwardByPage(page: Int, pageSize: Int): ArrayList<AwardWithCategory> {
        val offset = (page - 1) * pageSize
        val awards = ArrayList<AwardWithCategory>()

        val resultSet: ResultSet? =
            mySQLUtil.executeQuery(String.format("SELECT * FROM award LIMIT %s OFFSET %s", pageSize, offset))
        while (resultSet?.next() == true) {
            val awardCategoryResult =
                mySQLUtil.executeQuery(
                    String.format(
                        "SELECT award_category_name, has_award_level FROM award_category WHERE award_category_id = %s",
                        resultSet.getInt("award_category_id")
                    )
                )
            val awardCategory: AwardCategory? = if (awardCategoryResult != null && awardCategoryResult.next()) {
                AwardCategory(
                    resultSet.getInt("award_category_id"),
                    awardCategoryResult.getString("award_category_name"),
                    awardCategoryResult.getInt("has_award_level"),
                    null
                )
            } else {
                null
            }
            awards.add(
                AwardWithCategory(
                    resultSet.getInt("award_id"), awardCategory!!, resultSet.getInt("race_level"),
                    resultSet.getString("award_name"), resultSet.getInt("award_level"), resultSet.getInt("ranking"),
                    resultSet.getTimestamp("acquisition_time").time
                )
            )
        }

        return awards
    }


    @Transactional
    open fun insertAward(requestBody: AwardInsertRequest, file: MultipartFile): Boolean {
        if (file.isEmpty) {
            throw Exception("文件为空")
        }
        val currentDir = System.getProperty("user.dir")
        val fileDir = "$currentDir/files/"

        val key = mySQLUtil.executeUpdateWithGeneratedKey(
            String.format(
                "INSERT INTO award (award_category_id, race_level, award_name, award_level, ranking, acquisition_time) VALUES (%s, %s, '%s',%s,%s,'%s')",
                requestBody.awardCategoryId,
                requestBody.raceLevel,
                requestBody.awardName,
                requestBody.awardLevel,
                requestBody.ranking,
                Timestamp(requestBody.acquisitionTime)
            )
        )

        val dir = File(fileDir)
        if (!dir.exists()) {
            dir.mkdirs()
        }

        val filePath = fileDir +
                MessageDigest.getInstance("MD5").digest("file_$key".toByteArray())
            .joinToString("") { "%02x".format(it) } + ".png"
        val dest = File(filePath)

        file.transferTo(dest)


        return key!=null
    }


    @Transactional
    open fun deleteAward(awardId: Int): Boolean {
        val rowsAffected = mySQLUtil.executeUpdate(String.format("DELETE FROM award WHERE award_id = %s", awardId))
        return rowsAffected > 0
    }


    @Transactional
    open fun updateAward(body: AwardUpdateRequest): Boolean {
        val resultSet = mySQLUtil.executeQuery(String.format("SELECT * FROM award WHERE award_id = %s", body.awardId))
        val award: Award? = if (resultSet != null && resultSet.next()) {
            Award(
                body.awardId,
                resultSet.getInt("award_category_id"),
                resultSet.getInt("race_level"),
                resultSet.getString("award_name"),
                resultSet.getInt("award_level"),
                resultSet.getInt("ranking"),
                resultSet.getTimestamp("acquisition_time").time
            )
        } else {
            null
        }
        if (award == null) {
            throw Exception("奖励不存在")
        } else {
            body.awardCategoryId.let {
                award.awardCategoryId = body.awardCategoryId!!
            }
            body.raceLevel.let {
                award.raceLevel = body.raceLevel!!
            }
            body.awardName.let {
                award.awardName = body.awardName!!
            }
            body.awardLevel.let {
                award.awardLevel = body.awardLevel!!
            }
            body.acquisitionTime.let {
                award.ranking = body.ranking!!
            }
            val rowsAffected = mySQLUtil.executeUpdate(
                String.format(
                    "UPDATE award SET award_category_id = %s, race_level = %s, award_name = '%s', award_level = %s, ranking = %s, acquisition_time = '%s' WHERE award_id = %s",
                    award.awardCategoryId!!,
                    award.raceLevel!!,
                    award.awardName!!,
                    award.awardLevel!!,
                    award.ranking!!,
                    Timestamp(award.acquisitionTime!!),
                    award.awardId!!
                )
            )
            return rowsAffected > 0
        }
    }

    @Transactional
    open fun getRecentAwards(): ArrayList<AwardWithCategory> {
        val awards = ArrayList<AwardWithCategory>()
        val resultSet: ResultSet? =
            mySQLUtil.executeQuery(String.format("SELECT * FROM award ORDER BY acquisition_time DESC LIMIT 3;"))
        while (resultSet?.next() == true) {
            val awardCategoryResult =
                mySQLUtil.executeQuery(
                    String.format(
                        "SELECT award_category_name, has_award_level FROM award_category WHERE award_category_id = %s",
                        resultSet.getInt("award_category_id")
                    )
                )
            val awardCategory: AwardCategory? = if (awardCategoryResult != null && awardCategoryResult.next()) {
                AwardCategory(
                    resultSet.getInt("award_category_id"),
                    awardCategoryResult.getString("award_category_name"),
                    awardCategoryResult.getInt("has_award_level"),
                    null
                )
            } else {
                null
            }
            awards.add(
                AwardWithCategory(
                    resultSet.getInt("award_id"), awardCategory!!, resultSet.getInt("race_level"),
                    resultSet.getString("award_name"), resultSet.getInt("award_level"), resultSet.getInt("ranking"),
                    resultSet.getTimestamp("acquisition_time").time
                )
            )
        }

        return awards
    }


    @Transactional
    open fun getThisYearAwards(): ArrayList<AwardWithCategory> {
        val awards = ArrayList<AwardWithCategory>()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val currentDateTime = LocalDateTime.now()
        val oneYearLater = currentDateTime.plus(-1, ChronoUnit.YEARS)
        val resultSet: ResultSet? =
            mySQLUtil.executeQuery(String.format("SELECT * FROM award WHERE acquisition_time BETWEEN '%s' AND '%s';",oneYearLater.format(formatter),currentDateTime.format(formatter)))
        while (resultSet?.next() == true) {
            val awardCategoryResult =
                mySQLUtil.executeQuery(
                    String.format(
                        "SELECT award_category_name, has_award_level FROM award_category WHERE award_category_id = %s",
                        resultSet.getInt("award_category_id")
                    )
                )
            val awardCategory: AwardCategory? = if (awardCategoryResult != null && awardCategoryResult.next()) {
                AwardCategory(
                    resultSet.getInt("award_category_id"),
                    awardCategoryResult.getString("award_category_name"),
                    awardCategoryResult.getInt("has_award_level"),
                    null
                )
            } else {
                null
            }
            awards.add(
                AwardWithCategory(
                    resultSet.getInt("award_id"), awardCategory!!, resultSet.getInt("race_level"),
                    resultSet.getString("award_name"), resultSet.getInt("award_level"), resultSet.getInt("ranking"),
                    resultSet.getTimestamp("acquisition_time").time
                )
            )
        }

        return awards
    }


    @Transactional
    open fun exportExcel(selectedIdes: ArrayList<Int>): ByteArray {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("data")

        val headerRow = sheet.createRow(0)
        headerRow.createCell(0).setCellValue("奖励种类")
        headerRow.createCell(1).setCellValue("竞赛等级")
        headerRow.createCell(2).setCellValue("奖励名称")
        headerRow.createCell(3).setCellValue("奖励等级")
        headerRow.createCell(4).setCellValue("团队排名")
        headerRow.createCell(5).setCellValue("获奖时间")

        var count = 1
        for(id in selectedIdes){
            val resultSet :ResultSet = mySQLUtil.executeQuery(String.format("SELECT * FROM award WHERE award_id = %s", id))!!
            resultSet.next()

            val awardCategory = mySQLUtil.executeQuery(String.format("SELECT award_category_name, has_award_level FROM award_category WHERE award_category_id = %s", resultSet.getInt("award_category_id")))!!
            awardCategory.next()

            val row = sheet.createRow(count)
            row.createCell(0).setCellValue(awardCategory.getString("award_category_name"))
            val raceLevelName = arrayOf("未知","国际级","国家级","省部级","市局级","校级","院级")
            row.createCell(1).setCellValue(raceLevelName[resultSet.getInt("race_level")])
            row.createCell(2).setCellValue(resultSet.getString("award_name"))
            val awardLevelName = arrayOf("未知","一等奖","二等奖","三等奖","优胜奖")
            row.createCell(3).setCellValue(awardLevelName[resultSet.getInt("award_level")])
            if(awardCategory.getInt("has_award_level")==1&&resultSet.getInt("ranking")>0){
                row.createCell(4).setCellValue(resultSet.getInt("ranking").toString())
            }else{
                row.createCell(4).setCellValue("")
            }
            val localDateTime = resultSet.getTimestamp("acquisition_time").toLocalDateTime()
            row.createCell(5).setCellValue(localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))

            count++
        }

        val outputStream = ByteArrayOutputStream()
        workbook.write(outputStream)
        workbook.close()

//        val inputStream = ByteArrayInputStream(outputStream.toByteArray())
//        val resource = InputStreamResource(inputStream)

        return outputStream.toByteArray()
    }

    fun deleteDirectory(directory: File): Boolean {
        if (directory.isDirectory) {
            val children = directory.listFiles() ?: return false
            for (file in children) {
                if (file.isDirectory) {
                    deleteDirectory(file)
                } else {
                    file.delete()
                }
            }
        }
        return directory.delete()
    }

    private fun zipFolder(fileToZip: File, fileName: String, zipOut: ZipOutputStream) {
        if (fileToZip.isHidden) {
            return
        }

        if (fileToZip.isDirectory) {
            val children = fileToZip.listFiles() ?: return
            if (!fileName.endsWith("/")) {
                zipOut.putNextEntry(ZipEntry("$fileName/"))
                zipOut.closeEntry()
            }
            for (childFile in children) {
                zipFolder(childFile, "$fileName/${childFile.name}", zipOut)
            }
        } else {
            FileInputStream(fileToZip).use { fis ->
                val zipEntry = ZipEntry(fileName)
                zipOut.putNextEntry(zipEntry)
                fis.copyTo(zipOut)
            }
        }
    }

    @Transactional
    open fun exportZip(selectedIdes: ArrayList<Int>): ByteArray {
        val currentDir = System.getProperty("user.dir")
        val fileDir = "$currentDir/files/"
        val cacheDir = "$currentDir/cache/"

        val dir = File(cacheDir)
        if (dir.exists()) {
            deleteDirectory(dir)
        }
        dir.mkdirs()

        for(selectedId in selectedIdes){
            val resultSet :ResultSet = mySQLUtil.executeQuery(String.format("SELECT * FROM award WHERE award_id = %s", selectedId))!!
            resultSet.next()

            val awardCategory = mySQLUtil.executeQuery(String.format("SELECT award_category_name, has_award_level FROM award_category WHERE award_category_id = %s", resultSet.getInt("award_category_id")))!!
            awardCategory.next()

//            if(awardCategory.getInt("has_award_level")==1){
//                row.createCell(4).setCellValue(resultSet.getInt("ranking").toString())
//            }else{
//                row.createCell(4).setCellValue("")
//            }

            val filePath = fileDir + MessageDigest.getInstance("MD5").digest("file_${resultSet.getInt("award_id")}".toByteArray())
                .joinToString("") { "%02x".format(it) } + ".png"

            val sourceFile = File(filePath)

            val raceLevelName = arrayOf("未知","国际级","国家级","省部级","市局级","校级","院级")
            val awardLevelName = arrayOf("未知","一等奖","二等奖","三等奖","优胜奖")
            val localDateTime = resultSet.getTimestamp("acquisition_time").toLocalDateTime()
            val copiedFile = File(cacheDir + awardCategory.getString("award_category_name") +
                    "-${raceLevelName[resultSet.getInt("race_level")]}" +
                    "-${resultSet.getString("award_name")}" +
                    "-${awardLevelName[resultSet.getInt("award_level")]}" +
                    "-${localDateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd"))}" + ".png")
            sourceFile.copyTo(copiedFile, overwrite = true)
        }

        val byteArrayOutputStream = ByteArrayOutputStream()

        ZipOutputStream(byteArrayOutputStream).use { zipOut ->
            zipFolder(File(cacheDir), "data", zipOut)
        }

        return byteArrayOutputStream.toByteArray()
    }


}