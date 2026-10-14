/*
Run this only when setting up the database for the first time.
 It drops(DELETES) and recreates the tables.
*/




CREATE DATABASE  IF NOT EXISTS `studyfinder` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `studyfinder`;
-- MySQL dump 10.13  Distrib 8.0.46, for macos15 (arm64)
--
-- Host: 127.0.0.1    Database: studyfinder
-- ------------------------------------------------------
-- Server version	26.7.0

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
SET @MYSQLDUMP_TEMP_LOG_BIN = @@SESSION.SQL_LOG_BIN;
SET @@SESSION.SQL_LOG_BIN= 0;

--
-- GTID state at the beginning of the backup 
--

SET @@GLOBAL.GTID_PURGED=/*!80000 '+'*/ '0bedf63c-a57c-11f1-8bbe-787b0295e6c3:1-218';

--
-- Table structure for table `activity_logs`
--

DROP TABLE IF EXISTS `activity_logs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `activity_logs` (
  `log_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `action_type` varchar(45) DEFAULT NULL,
  `target_type` varchar(45) DEFAULT NULL,
  `target_id` int DEFAULT NULL,
  `details` varchar(255) DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  PRIMARY KEY (`log_id`),
  KEY `fk_activitylogs_users_idx` (`user_id`),
  CONSTRAINT `fk_activitylogs_users` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `administrators`
--

DROP TABLE IF EXISTS `administrators`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `administrators` (
  `user_id` int NOT NULL,
  `access_level` varchar(45) NOT NULL,
  PRIMARY KEY (`user_id`),
  CONSTRAINT `fk_administrators_users` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `amenities`
--

DROP TABLE IF EXISTS `amenities`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `amenities` (
  `amenity_id` int NOT NULL AUTO_INCREMENT,
  `amenity_name` varchar(100) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `is_active` tinyint DEFAULT '1',
  PRIMARY KEY (`amenity_id`),
  UNIQUE KEY `amentity_name_UNIQUE` (`amenity_name`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `building_hours`
--

DROP TABLE IF EXISTS `building_hours`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `building_hours` (
  `building_id` int NOT NULL,
  `day_of_week` varchar(10) NOT NULL,
  `opening_time` time NOT NULL,
  `closing_time` time NOT NULL,
  PRIMARY KEY (`building_id`,`day_of_week`),
  CONSTRAINT `building_hours_ibfk_1` FOREIGN KEY (`building_id`) REFERENCES `Buildings` (`building_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `buildings`
--

DROP TABLE IF EXISTS `buildings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `buildings` (
  `building_id` int NOT NULL AUTO_INCREMENT,
  `acronym` varchar(10) NOT NULL,
  `building_name` varchar(150) NOT NULL,
  PRIMARY KEY (`building_id`),
  UNIQUE KEY `acronym` (`acronym`),
  UNIQUE KEY `building_name` (`building_name`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `moderates`
--

DROP TABLE IF EXISTS `moderates`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `moderates` (
  `moderation_id` int NOT NULL AUTO_INCREMENT,
  `administrator_id` int NOT NULL,
  `report_id` int NOT NULL,
  `action` varchar(30) NOT NULL,
  `reason` text,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`moderation_id`),
  KEY `administrator_id` (`administrator_id`),
  KEY `report_id` (`report_id`),
  CONSTRAINT `moderates_ibfk_1` FOREIGN KEY (`administrator_id`) REFERENCES `administrators` (`user_id`),
  CONSTRAINT `moderates_ibfk_2` FOREIGN KEY (`report_id`) REFERENCES `reports` (`report_id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `provides`
--

DROP TABLE IF EXISTS `provides`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `provides` (
  `spot_id` int NOT NULL,
  `amenity_id` int NOT NULL,
  `notes` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`spot_id`,`amenity_id`),
  KEY `fk_spotamenities_amenities_idx` (`amenity_id`),
  CONSTRAINT `fk_spotamenities_amenities` FOREIGN KEY (`amenity_id`) REFERENCES `amenities` (`amenity_id`),
  CONSTRAINT `fk_spotamenities_studyspots` FOREIGN KEY (`spot_id`) REFERENCES `study_spots` (`spot_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `redeems`
--

DROP TABLE IF EXISTS `redeems`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `redeems` (
  `redemption_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `reward_id` int NOT NULL,
  `points_spent` int NOT NULL,
  `status` varchar(45) NOT NULL,
  `redeemed_at` datetime DEFAULT NULL,
  PRIMARY KEY (`redemption_id`),
  KEY `fk_redeems_students_idx` (`user_id`),
  KEY `fk_redeems_rewards_idx` (`reward_id`),
  CONSTRAINT `fk_redeems_rewards` FOREIGN KEY (`reward_id`) REFERENCES `rewards` (`reward_id`),
  CONSTRAINT `fk_redeems_students` FOREIGN KEY (`user_id`) REFERENCES `students` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `reports`
--

DROP TABLE IF EXISTS `reports`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reports` (
  `report_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `spot_id` int NOT NULL,
  `noise_level` varchar(45) NOT NULL,
  `crowdedness` varchar(45) NOT NULL,
  `outlets_available` tinyint DEFAULT NULL,
  `smell_level` varchar(45) DEFAULT NULL,
  `notes` varchar(255) DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `distance_from_spot` decimal(6,2) DEFAULT NULL,
  PRIMARY KEY (`report_id`),
  KEY `fk_reports_students_idx` (`user_id`),
  KEY `fk_reports_studyspots` (`spot_id`),
  CONSTRAINT `fk_reports_students` FOREIGN KEY (`user_id`) REFERENCES `students` (`user_id`),
  CONSTRAINT `fk_reports_studyspots` FOREIGN KEY (`spot_id`) REFERENCES `study_spots` (`spot_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `reviews`
--

DROP TABLE IF EXISTS `reviews`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reviews` (
  `review_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `spot_id` int NOT NULL,
  `rating` varchar(20) DEFAULT NULL,
  `review_text` varchar(255) DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `is_visible` tinyint NOT NULL DEFAULT '1',
  PRIMARY KEY (`review_id`),
  UNIQUE KEY `uq_reviews_user_spot` (`user_id`,`spot_id`),
  KEY `fk_reviews_students_idx` (`user_id`),
  KEY `fk_reviews_studyspots_idx` (`spot_id`),
  CONSTRAINT `fk_reviews_students` FOREIGN KEY (`user_id`) REFERENCES `students` (`user_id`),
  CONSTRAINT `fk_reviews_studyspots` FOREIGN KEY (`spot_id`) REFERENCES `study_spots` (`spot_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `rewards`
--

DROP TABLE IF EXISTS `rewards`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rewards` (
  `reward_id` int NOT NULL AUTO_INCREMENT,
  `reward_name` varchar(100) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `points_required` int NOT NULL,
  `stock_quantity` int NOT NULL,
  `is_active` tinyint NOT NULL DEFAULT '1',
  PRIMARY KEY (`reward_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `students`
--

DROP TABLE IF EXISTS `students`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `students` (
  `user_id` int NOT NULL,
  `student_id` varchar(20) NOT NULL,
  `location_permission` tinyint NOT NULL DEFAULT '0',
  `points` int NOT NULL DEFAULT '0',
  `trust_score` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `student_id_UNIQUE` (`student_id`),
  CONSTRAINT `fk_students_users` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `study_spots`
--

DROP TABLE IF EXISTS `study_spots`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `study_spots` (
  `spot_id` int NOT NULL AUTO_INCREMENT,
  `building_id` int DEFAULT NULL,
  `spot_name` varchar(100) NOT NULL,
  `location_details` varchar(100) NOT NULL,
  `access_type` varchar(45) NOT NULL,
  `capacity` int DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `is_active` tinyint DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `latitude` decimal(9,6) DEFAULT NULL,
  `longitude` decimal(9,6) DEFAULT NULL,
  `photo_path` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`spot_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `user_id` int NOT NULL AUTO_INCREMENT,
  `full_name` varchar(100) NOT NULL,
  `email` varchar(100) NOT NULL,
  `email_verified` tinyint DEFAULT NULL,
  `password_hash` varchar(255) DEFAULT NULL,
  `account_status` varchar(45) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `email_UNIQUE` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=29 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `verifies`
--

DROP TABLE IF EXISTS `verifies`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `verifies` (
  `report_id` int NOT NULL,
  `user_id` int NOT NULL,
  `result` varchar(45) NOT NULL,
  `verified_at` datetime DEFAULT NULL,
  PRIMARY KEY (`report_id`,`user_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `verifies_ibfk_1` FOREIGN KEY (`report_id`) REFERENCES `reports` (`report_id`),
  CONSTRAINT `verifies_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `students` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `violations`
--

DROP TABLE IF EXISTS `violations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `violations` (
  `violation_id` int NOT NULL AUTO_INCREMENT,
  `report_id` int DEFAULT NULL,
  `user_id` int NOT NULL,
  `administrator_id` int NOT NULL,
  `type` varchar(50) DEFAULT NULL,
  `reason` text NOT NULL,
  `severity` varchar(20) NOT NULL,
  PRIMARY KEY (`violation_id`),
  KEY `report_id` (`report_id`),
  KEY `user_id` (`user_id`),
  KEY `administrator_id` (`administrator_id`),
  CONSTRAINT `violations_ibfk_1` FOREIGN KEY (`report_id`) REFERENCES `reports` (`report_id`),
  CONSTRAINT `violations_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `students` (`user_id`),
  CONSTRAINT `violations_ibfk_3` FOREIGN KEY (`administrator_id`) REFERENCES `administrators` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
SET @@SESSION.SQL_LOG_BIN = @MYSQLDUMP_TEMP_LOG_BIN;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-10-14  9:07:37
