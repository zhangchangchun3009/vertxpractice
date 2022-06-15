CREATE DATABASE  IF NOT EXISTS `vertxp` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `vertxp`;
-- MySQL dump 10.13  Distrib 8.0.19, for Win64 (x86_64)
--
-- Host: localhost    Database: scm
-- ------------------------------------------------------
-- Server version	8.0.19

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `sys_async_task`
--

DROP TABLE IF EXISTS `sys_async_task`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_async_task` (
  `id` bigint NOT NULL COMMENT '事件id',
  `task_name` varchar(45) NOT NULL COMMENT '任务名称',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '任务状态。创建：0；进行中：1；正常结束：2；异常终止9',
  `event_type` tinyint NOT NULL COMMENT '事件类型：excel导入0；excel导出1；',
  `start_time` bigint DEFAULT NULL COMMENT '开始时间戳',
  `end_time` bigint DEFAULT NULL COMMENT '结束时间戳',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_n1` (`task_name`),
  KEY `idx_n2` (`created_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='异步任务结果表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_async_task`
--

LOCK TABLES `sys_async_task` WRITE;
/*!40000 ALTER TABLE `sys_async_task` DISABLE KEYS */;
/*!40000 ALTER TABLE `sys_async_task` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_excel_error`
--

DROP TABLE IF EXISTS `sys_excel_error`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_excel_error` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `event_id` bigint NOT NULL COMMENT '事件id',
  `row_index` int DEFAULT NULL COMMENT '错误数据所在行号',
  `column_index` int DEFAULT NULL COMMENT '错误数据所在列号',
  `message` varchar(256) DEFAULT NULL COMMENT '错误信息描述',
  PRIMARY KEY (`id`),
  KEY `idx_n1` (`event_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='excel处理事件错误信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_excel_error`
--

LOCK TABLES `sys_excel_error` WRITE;
/*!40000 ALTER TABLE `sys_excel_error` DISABLE KEYS */;
/*!40000 ALTER TABLE `sys_excel_error` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_resource`
--

DROP TABLE IF EXISTS `sys_resource`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_resource` (
  `id` int NOT NULL AUTO_INCREMENT,
  `resource_id` int NOT NULL,
  `module_name` varchar(200) NOT NULL,
  `service_code` varchar(45) NOT NULL,
  `service_name` varchar(200) NOT NULL,
  `method_code` varchar(45) NOT NULL,
  `method_name` varchar(200) NOT NULL,
  `batch_flag` varchar(2) DEFAULT NULL COMMENT '数据处理新旧标记',
  PRIMARY KEY (`id`),
  UNIQUE KEY `resource_id_UNIQUE` (`resource_id`),
  UNIQUE KEY `idx_u2` (`module_name`,`service_code`,`method_code`) /*!80000 INVISIBLE */
) ENGINE=InnoDB AUTO_INCREMENT=393 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='资源（权限）表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_resource`
--

LOCK TABLES `sys_resource` WRITE;
/*!40000 ALTER TABLE `sys_resource` DISABLE KEYS */;
/*!40000 ALTER TABLE `sys_resource` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_role`
--

DROP TABLE IF EXISTS `sys_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_role` (
  `id` int NOT NULL AUTO_INCREMENT,
  `role_id` int NOT NULL COMMENT '角色id',
  `role_name` varchar(200) NOT NULL COMMENT '角色名',
  `created_by` varchar(45) DEFAULT NULL,
  `created_time` datetime DEFAULT NULL,
  `last_updated_by` varchar(45) DEFAULT NULL,
  `last_updated_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `role_id_UNIQUE` (`role_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_role`
--

LOCK TABLES `sys_role` WRITE;
/*!40000 ALTER TABLE `sys_role` DISABLE KEYS */;
INSERT INTO `sys_role` VALUES (1,1,'guest','-1',NULL,NULL,NULL);
/*!40000 ALTER TABLE `sys_role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_role_resource`
--

DROP TABLE IF EXISTS `sys_role_resource`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_role_resource` (
  `id` int NOT NULL AUTO_INCREMENT,
  `role_id` int DEFAULT NULL,
  `resource_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_n1` (`role_id`)
) ENGINE=InnoDB AUTO_INCREMENT=69 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色-资源表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_role_resource`
--

LOCK TABLES `sys_role_resource` WRITE;
/*!40000 ALTER TABLE `sys_role_resource` DISABLE KEYS */;
/*!40000 ALTER TABLE `sys_role_resource` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_sequence`
--

DROP TABLE IF EXISTS `sys_sequence`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_sequence` (
  `seq_name` varchar(50) NOT NULL COMMENT '序列名',
  `current_value` bigint NOT NULL DEFAULT '0' COMMENT '当前值',
  `increment` int NOT NULL DEFAULT '1' COMMENT '步长',
  PRIMARY KEY (`seq_name`),
  UNIQUE KEY `seq_name_UNIQUE` (`seq_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='序列表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_sequence`
--

LOCK TABLES `sys_sequence` WRITE;
/*!40000 ALTER TABLE `sys_sequence` DISABLE KEYS */;
INSERT INTO `sys_sequence` VALUES ('s_common',0,1),('s_orderid',0,1),('s_pklcode',0,1),('s_resourceid',0,1),('s_roleid',1,1),('s_userid',2,1),
('s_asynctaskid',0,1);
/*!40000 ALTER TABLE `sys_sequence` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_user`
--

DROP TABLE IF EXISTS `sys_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL COMMENT '用户id',
  `user_name` varchar(45) NOT NULL COMMENT '用户名',
  `password` varchar(200) DEFAULT NULL,
  `user_type` varchar(45) DEFAULT 'default' COMMENT '用户类型：system，virtual，default',
  `created_by` varchar(45) DEFAULT NULL,
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `last_updated_by` varchar(45) DEFAULT NULL,
  `last_updated_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_id_UNIQUE` (`user_id`),
  UNIQUE KEY `user_name_UNIQUE` (`user_name`)
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_user`
--

LOCK TABLES `sys_user` WRITE;
/*!40000 ALTER TABLE `sys_user` DISABLE KEYS */;
INSERT INTO `sys_user` VALUES (1,1,'root','d1508d197bc31f1938a80ee84e8151e4','system','-1','2021-07-17 17:35:03',NULL,NULL),(2,2,'guest',NULL,'default','-1','2021-05-14 09:14:01',NULL,NULL);
/*!40000 ALTER TABLE `sys_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_user_role`
--

DROP TABLE IF EXISTS `sys_user_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user_role` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int DEFAULT NULL,
  `role_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_n1` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户-角色对应关系表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_user_role`
--

LOCK TABLES `sys_user_role` WRITE;
/*!40000 ALTER TABLE `sys_user_role` DISABLE KEYS */;
INSERT INTO `sys_user_role` VALUES (1,2,1);
/*!40000 ALTER TABLE `sys_user_role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping routines for database 'scm'
--
/*!50003 DROP FUNCTION IF EXISTS `currval` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE FUNCTION `currval`(v_seq_name VARCHAR(50)) RETURNS bigint
    READS SQL DATA
    DETERMINISTIC
BEGIN  
  
DECLARE VALUE bigint;  
  
SET VALUE = 0;  
  
SELECT current_value INTO VALUE FROM sys_sequence WHERE seq_name = v_seq_name;  
  
RETURN VALUE;  
  
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP FUNCTION IF EXISTS `nextval` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE FUNCTION `nextval`(v_seq_name VARCHAR(50)) RETURNS bigint
    DETERMINISTIC
BEGIN  
  
UPDATE sys_sequence SET current_value = current_value + increment WHERE seq_name = v_seq_name;  

RETURN currval(v_seq_name);  
  
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2022-01-22 16:08:40
