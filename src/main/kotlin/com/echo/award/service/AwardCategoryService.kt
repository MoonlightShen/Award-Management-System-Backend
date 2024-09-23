package com.echo.award.service

import com.echo.award.common.result.Result
import com.echo.award.pojo.AwardCategory
import com.echo.award.request.AwardInsertRequest
import com.echo.award.util.MySQLUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.security.MessageDigest
import java.sql.SQLException
import java.sql.Timestamp

/**
 * @author MoonlightShen
 * @data 2024/9/3 16:23
 **/
@Service
open class AwardCategoryService(private val mySQLUtil: MySQLUtil) {

    @Transactional
    open fun getAllAwardCategories(): ArrayList<AwardCategory> {
        val query = "SELECT award_category_id, award_category_name, has_award_level FROM award_category"
        val resultSet = mySQLUtil.executeQuery(query)
        val awardCategories = ArrayList<AwardCategory>()
        resultSet?.let {
            while (resultSet.next()) {
                val awardCategory = AwardCategory(
                    resultSet.getInt("award_category_id"),
                    resultSet.getString("award_category_name"),
                    resultSet.getInt("has_award_level"),
                    null
                )

                val currentDir = System.getProperty("user.dir")
                val uploadDir = "$currentDir/uploads/"
                val filePath = uploadDir + MessageDigest.getInstance("MD5").digest(("award_category_icon${resultSet.getInt("award_category_id")}").toByteArray()).joinToString(""){ "%02x".format(it) } + ".png"

                awardCategory.icon = Files.readAllBytes(Paths.get(filePath))
                awardCategories.add(awardCategory)
            }
        }
        resultSet?.close()
        return awardCategories
    }

    @Transactional
    open fun updateAwardCategory(awardCategory: AwardCategory): Boolean {
        val resultSet = mySQLUtil.executeQuery(
            String.format(
                "SELECT award_category_name, has_award_level FROM award_category WHERE award_category_id = %d",
                awardCategory.awardCategoryId!!
            )
        )
        val current: AwardCategory? = if (resultSet != null && resultSet.next()) {
            AwardCategory(
                awardCategory.awardCategoryId,
                resultSet.getString("award_category_name"),
                resultSet.getInt("has_award_level"),
                null
            )
        } else {
            null
        }
        if (current == null) {
            throw Exception("奖励不存在" + awardCategory.awardCategoryId)
        } else {
            awardCategory.awardCategoryName.let {
                current.awardCategoryName = awardCategory.awardCategoryName
            }
            awardCategory.hasAwardLevel.let { current.hasAwardLevel = awardCategory.hasAwardLevel }
            val rowsAffected = mySQLUtil.executeUpdate(
                String.format(
                    "UPDATE award_category SET award_category_name = %s, has_award_level = %s WHERE award_category_id = %s",
                    awardCategory.awardCategoryName!!,
                    awardCategory.hasAwardLevel!!,
                    awardCategory.awardCategoryId
                )
            )
            if (rowsAffected > 0) {
                return true
            } else {
                throw Exception("更新失败" + awardCategory.awardCategoryId)
            }
        }
    }

    @Transactional
    open fun insertAwardCategory(awardCategory: AwardCategory): Boolean {
        val key = mySQLUtil.executeUpdateWithGeneratedKey(
            String.format(
                "INSERT INTO award_category (award_category_name, has_award_level) VALUES (\"%s\", %s)",
                awardCategory.awardCategoryName, awardCategory.hasAwardLevel
            )
        )
        var path: Path? = null
        if(key!=null){
            val currentDir = System.getProperty("user.dir")
            val uploadDir = "$currentDir/uploads/"
            val filePath = uploadDir + MessageDigest.getInstance("MD5").digest(("award_category_icon$key").toByteArray()).joinToString(""){ "%02x".format(it) } + ".png"

            path = Paths.get(filePath)
            Files.write(path!!, awardCategory.icon!!)
        }
        return key!=null&&Files.exists(path!!)
    }

    @Transactional
    open fun deleteAwardCategory(awardCategory: AwardCategory): Boolean {
        val rowsAffected = mySQLUtil.executeUpdate(
            String.format(
                "DELETE FROM award_category WHERE award_category_id = %s",
                awardCategory.awardCategoryId!!
            )
        )
        if (rowsAffected > 0) {
            return true
        } else {
            throw Exception("奖励不存在" + awardCategory.awardCategoryId)
        }
    }

}