-- ============================================================
-- Migration: 移除所有外键约束 + 增量字段变更
-- 执行方式：在 MySQL 中 source 此文件，或逐句执行
-- ================================================q============

-- 1. 移除所有外键约束
-- 依次执行，表或约束不存在则跳过
ALTER TABLE `resource`      DROP FOREIGN KEY `fk_resource_user`;
ALTER TABLE `resource`      DROP FOREIGN KEY `fk_resource_category`;
ALTER TABLE `comment`       DROP FOREIGN KEY `fk_comment_resource`;
ALTER TABLE `comment`       DROP FOREIGN KEY `fk_comment_user`;
ALTER TABLE `resource_report` DROP FOREIGN KEY `fk_report_resource`;
ALTER TABLE `resource_report` DROP FOREIGN KEY `fk_report_user`;

-- 2. resource 表：新增 type 字段（0=资源，1=文章）
ALTER TABLE `resource`
  ADD COLUMN `type` TINYINT NOT NULL DEFAULT 0
  AFTER `comment_count`;

-- 3. 新建 comment 表（无外键约束）
CREATE TABLE IF NOT EXISTS `comment` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `resource_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `content` TEXT NOT NULL,
  `parent_id` BIGINT DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_resource_id (`resource_id`),
  INDEX idx_user_id (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. 新建 resource_report 表（无外键约束）
CREATE TABLE IF NOT EXISTS `resource_report` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `resource_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `reason` VARCHAR(500) NOT NULL,
  `status` TINYINT NOT NULL DEFAULT 0,
  `handle_note` VARCHAR(500) DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_resource_id (`resource_id`),
  INDEX idx_status (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
