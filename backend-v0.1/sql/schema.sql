CREATE DATABASE IF NOT EXISTS studyfinder;
USE studyfinder;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `activity_logs`;
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

DROP TABLE IF EXISTS `administrators`;
CREATE TABLE `administrators` (
  `user_id` int NOT NULL,
  `access_level` varchar(45) NOT NULL,
  PRIMARY KEY (`user_id`),
  CONSTRAINT `fk_administrators_users` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `amenities`;
CREATE TABLE `amenities` (
  `amenity_id` int NOT NULL AUTO_INCREMENT,
  `amenity_name` varchar(100) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `is_active` tinyint DEFAULT '1',
  PRIMARY KEY (`amenity_id`),
  UNIQUE KEY `amentity_name_UNIQUE` (`amenity_name`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `building_hours`;
CREATE TABLE `building_hours` (
  `building_id` int NOT NULL,
  `day_of_week` varchar(10) NOT NULL,
  `opening_time` time NOT NULL,
  `closing_time` time NOT NULL,
  PRIMARY KEY (`building_id`,`day_of_week`),
  CONSTRAINT `building_hours_ibfk_1` FOREIGN KEY (`building_id`) REFERENCES `Buildings` (`building_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `buildings`;
CREATE TABLE `buildings` (
  `building_id` int NOT NULL AUTO_INCREMENT,
  `acronym` varchar(10) NOT NULL,
  `building_name` varchar(150) NOT NULL,
  PRIMARY KEY (`building_id`),
  UNIQUE KEY `acronym` (`acronym`),
  UNIQUE KEY `building_name` (`building_name`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `moderates`;
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

DROP TABLE IF EXISTS `provides`;
CREATE TABLE `provides` (
  `spot_id` int NOT NULL,
  `amenity_id` int NOT NULL,
  `notes` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`spot_id`,`amenity_id`),
  KEY `fk_spotamenities_amenities_idx` (`amenity_id`),
  CONSTRAINT `fk_spotamenities_amenities` FOREIGN KEY (`amenity_id`) REFERENCES `amenities` (`amenity_id`),
  CONSTRAINT `fk_spotamenities_studyspots` FOREIGN KEY (`spot_id`) REFERENCES `study_spots` (`spot_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `redeems`;
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

DROP TABLE IF EXISTS `reports`;
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

DROP TABLE IF EXISTS `reviews`;
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

DROP TABLE IF EXISTS `rewards`;
CREATE TABLE `rewards` (
  `reward_id` int NOT NULL AUTO_INCREMENT,
  `reward_name` varchar(100) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `points_required` int NOT NULL,
  `stock_quantity` int NOT NULL,
  `is_active` tinyint NOT NULL DEFAULT '1',
  PRIMARY KEY (`reward_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `students`;
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

DROP TABLE IF EXISTS `study_spots`;
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

DROP TABLE IF EXISTS `users`;
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

DROP TABLE IF EXISTS `verifies`;
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

DROP TABLE IF EXISTS `violations`;
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

SET FOREIGN_KEY_CHECKS = 1;
