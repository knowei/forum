# 数据库设计

## 用户表 (user)
```sql
CREATE TABLE `user` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
  `password` VARCHAR(100) NOT NULL COMMENT '密码(加密)',
  `email` VARCHAR(100) UNIQUE COMMENT '邮箱',
  `nickname` VARCHAR(50) COMMENT '昵称',
  `avatar` VARCHAR(255) COMMENT '头像URL',
  `role` TINYINT DEFAULT 0 COMMENT '角色: 0-普通用户 1-管理员',
  `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用 1-正常',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_username (`username`),
  INDEX idx_email (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
```

## 分类表 (category)
```sql
CREATE TABLE `category` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
  `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
  `description` VARCHAR(200) COMMENT '分类描述',
  `sort` INT DEFAULT 0 COMMENT '排序',
  `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  UNIQUE KEY uk_name (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分类表';
```

## 资源表 (resource)
```sql
CREATE TABLE `resource` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '资源ID',
  `user_id` BIGINT NOT NULL COMMENT '发布用户ID',
  `category_id` INT NOT NULL COMMENT '分类ID',
  `title` VARCHAR(200) NOT NULL COMMENT '资源标题',
  `description` VARCHAR(500) COMMENT '资源简介(用于列表展示)',
  `content` LONGTEXT NOT NULL COMMENT '资源正文(支持富文本/Markdown)',
  `cover` VARCHAR(255) COMMENT '封面图',
  `download_link` VARCHAR(500) NOT NULL COMMENT '下载链接',
  `extract_code` VARCHAR(50) COMMENT '提取码',
  `unzip_password` VARCHAR(50) COMMENT '解压密码',
  `tags` VARCHAR(200) COMMENT '标签(逗号分隔)',
  `view_count` INT DEFAULT 0 COMMENT '浏览次数',
  `like_count` INT DEFAULT 0 COMMENT '点赞数',
  `collect_count` INT DEFAULT 0 COMMENT '收藏数',
  `comment_count` INT DEFAULT 0 COMMENT '评论数',
  `status` TINYINT DEFAULT 1 COMMENT '状态: 0-待审核 1-已发布 2-已下架',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_user_id (`user_id`),
  INDEX idx_category_id (`category_id`),
  INDEX idx_status (`status`),
  INDEX idx_create_time (`create_time`),
  FULLTEXT idx_title_tags (`title`, `tags`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源表';
```

**字段说明：**
- `description`: 简短描述（200-500字），用于列表页卡片展示
- `content`: 详细正文内容，支持 Markdown 或富文本格式，用于详情页展示
  - 可包含：资源介绍、使用说明、安装步骤、注意事项、更新日志等
  - 建议使用 Markdown 格式，前端渲染时转换为 HTML

## 评论表 (comment)
```sql
CREATE TABLE `comment` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评论ID',
  `resource_id` BIGINT NOT NULL COMMENT '资源ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `parent_id` BIGINT DEFAULT 0 COMMENT '父评论ID(0表示一级评论)',
  `content` TEXT NOT NULL COMMENT '评论内容',
  `like_count` INT DEFAULT 0 COMMENT '点赞数',
  `status` TINYINT DEFAULT 1 COMMENT '状态: 0-已删除 1-正常',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX idx_resource_id (`resource_id`),
  INDEX idx_user_id (`user_id`),
  INDEX idx_parent_id (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';
```

## 点赞表 (user_like)
```sql
CREATE TABLE `user_like` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `target_id` BIGINT NOT NULL COMMENT '目标ID(资源或评论)',
  `target_type` TINYINT NOT NULL COMMENT '类型: 1-资源 2-评论',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  UNIQUE KEY uk_user_target (`user_id`, `target_id`, `target_type`),
  INDEX idx_target (`target_id`, `target_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='点赞表';
```

## 收藏表 (user_collect)
```sql
CREATE TABLE `user_collect` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `resource_id` BIGINT NOT NULL COMMENT '资源ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  UNIQUE KEY uk_user_resource (`user_id`, `resource_id`),
  INDEX idx_resource_id (`resource_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';
```

## 初始化数据

### 默认分类
```sql
INSERT INTO `category` (`name`, `description`, `sort`) VALUES
('软件工具', '各类实用软件和工具', 1),
('学习资料', '教程、文档、电子书等', 2),
('开发资源', '代码库、框架、插件等', 3),
('设计素材', '图片、图标、模板等', 4),
('其他资源', '其他类型资源', 99);
```

### 管理员账号
```sql
-- 密码: admin123 (需要后端加密后再插入)
INSERT INTO `user` (`username`, `password`, `email`, `nickname`, `role`) VALUES
('admin', '$2a$10$...', 'admin@example.com', '管理员', 1);
```
