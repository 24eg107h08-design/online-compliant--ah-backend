-- =====================================================================
-- Online Complaint Management System (CMS) Database Schema
-- Database: MySQL 8.0+
-- =====================================================================

CREATE DATABASE IF NOT EXISTS `complaint_db` 
DEFAULT CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE `complaint_db`;

-- ---------------------------------------------------------------------
-- Table 1: Users
-- Role can be 'ROLE_USER' (Citizen) or 'ROLE_ADMIN' (Administrator)
-- Passwords stored as BCrypt hashes ($2a$10$...)
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `notifications`;
DROP TABLE IF EXISTS `complaint_history`;
DROP TABLE IF EXISTS `complaints`;
DROP TABLE IF EXISTS `users`;

CREATE TABLE `users` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL,
    `email` VARCHAR(100) NOT NULL UNIQUE,
    `phone` VARCHAR(20) NOT NULL,
    `password` VARCHAR(255) NOT NULL,
    `role` VARCHAR(20) NOT NULL DEFAULT 'ROLE_USER',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_users_email` (`email`),
    INDEX `idx_users_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- Table 2: Complaints
-- Status: SUBMITTED, IN_PROGRESS, RESOLVED, REJECTED
-- Category: Electricity, Water, Roads, Sanitation, Education, Other
-- ---------------------------------------------------------------------
CREATE TABLE `complaints` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `complaint_code` VARCHAR(32) NOT NULL UNIQUE,
    `title` VARCHAR(200) NOT NULL,
    `category` VARCHAR(50) NOT NULL,
    `description` TEXT NOT NULL,
    `location` VARCHAR(255) NOT NULL,
    `attachment_path` VARCHAR(500) DEFAULT NULL,
    `attachment_name` VARCHAR(255) DEFAULT NULL,
    `status` VARCHAR(30) NOT NULL DEFAULT 'SUBMITTED',
    `user_id` BIGINT NOT NULL,
    `assigned_department` VARCHAR(100) DEFAULT NULL,
    `assigned_staff` VARCHAR(100) DEFAULT NULL,
    `resolution_remarks` TEXT DEFAULT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_complaints_user` FOREIGN KEY (`user_id`) 
        REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX `idx_complaints_code` (`complaint_code`),
    INDEX `idx_complaints_status` (`status`),
    INDEX `idx_complaints_category` (`category`),
    INDEX `idx_complaints_user` (`user_id`),
    INDEX `idx_complaints_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- Table 3: Complaint History (Audit Trail & Status Timeline)
-- ---------------------------------------------------------------------
CREATE TABLE `complaint_history` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `complaint_id` BIGINT NOT NULL,
    `previous_status` VARCHAR(30) DEFAULT NULL,
    `new_status` VARCHAR(30) NOT NULL,
    `remarks` TEXT DEFAULT NULL,
    `updated_by` VARCHAR(100) NOT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_history_complaint` FOREIGN KEY (`complaint_id`) 
        REFERENCES `complaints` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX `idx_history_complaint` (`complaint_id`),
    INDEX `idx_history_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- Table 4: Notifications (In-app Alerts for Citizens)
-- ---------------------------------------------------------------------
CREATE TABLE `notifications` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `complaint_id` BIGINT DEFAULT NULL,
    `message` VARCHAR(500) NOT NULL,
    `is_read` BOOLEAN NOT NULL DEFAULT FALSE,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_notifications_user` FOREIGN KEY (`user_id`) 
        REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_notifications_complaint` FOREIGN KEY (`complaint_id`) 
        REFERENCES `complaints` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
    INDEX `idx_notifications_user` (`user_id`),
    INDEX `idx_notifications_read` (`is_read`),
    INDEX `idx_notifications_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
