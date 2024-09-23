

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `award_category`;
CREATE TABLE `award_category`  (
  `award_category_id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `award_category_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '奖励种类名称',
  `has_award_level` int NULL DEFAULT NULL COMMENT '奖励有等级',
  PRIMARY KEY (`award_category_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `award`;
CREATE TABLE `award`  (
                                   `award_id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
                                   `award_category_id` int NULL COMMENT '奖励种类ID',
                                   `race_level` int NULL COMMENT '赛事等级',
                                   `award_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '奖励名称',
                                   `award_level` int NULL COMMENT '奖励等级',
                                   `ranking` int NULL COMMENT '排名',
                                   `acquisition_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '获奖时间',
                                   PRIMARY KEY (`award_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;
