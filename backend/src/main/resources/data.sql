INSERT INTO `category` (`name`, `description`, `sort`, `status`)
SELECT '软件工具', '各类实用软件和工具', 1, 1
WHERE NOT EXISTS (SELECT 1 FROM `category` WHERE `name` = '软件工具');

INSERT INTO `category` (`name`, `description`, `sort`, `status`)
SELECT '学习资料', '教程、文档、电子书等', 2, 1
WHERE NOT EXISTS (SELECT 1 FROM `category` WHERE `name` = '学习资料');

INSERT INTO `category` (`name`, `description`, `sort`, `status`)
SELECT '开发资源', '代码库、框架、插件等', 3, 1
WHERE NOT EXISTS (SELECT 1 FROM `category` WHERE `name` = '开发资源');

INSERT INTO `category` (`name`, `description`, `sort`, `status`)
SELECT '设计素材', '图片、图标、模板等', 4, 1
WHERE NOT EXISTS (SELECT 1 FROM `category` WHERE `name` = '设计素材');

INSERT INTO `category` (`name`, `description`, `sort`, `status`)
SELECT '其他资源', '其他类型资源', 99, 1
WHERE NOT EXISTS (SELECT 1 FROM `category` WHERE `name` = '其他资源');

INSERT INTO `user` (`username`, `password`, `email`, `nickname`, `role`, `status`)
SELECT 'admin', '$2a$10$7W6l5.97z0W8xI7nY9lPWOD3r0Yj8MAbQHVq2PASi6DPd7OJbRRsK', 'admin@example.com', '管理员', 1, 1
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'admin');
