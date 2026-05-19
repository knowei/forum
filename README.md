# 柠檬网

## 项目概述
基于 Vue3 + Spring Boot + MySQL 的小型资源分享社区，支持用户发布和下载各类资源（以链接+提取码+解压码形式）。前台展示与后台管理统一放在一个 Vue 项目中，通过路由、布局和权限控制区分用户端与管理端。

## 技术栈
- 前端：Vue 3 + Element Plus + Axios + Vue Router + Pinia
- 后端：Spring Boot 3 + MyBatis Plus + JWT + Redis
- 数据库：MySQL 8.0
- 服务器：2核4G

## 核心功能模块

### 1. 用户系统
- 用户注册/登录（JWT认证）
- 个人信息管理
- 头像上传
- 密码修改

### 2. 资源管理
- 资源发布（标题、描述、分类、标签、链接、提取码、解压码）
- 资源列表（分页、排序）
- 资源详情
- 资源搜索（标题、标签）
- 分类浏览

### 3. 互动功能
- 评论系统
- 点赞/收藏
- 浏览统计

### 4. 管理后台
- 用户管理
- 资源审核
- 分类管理
- 数据统计

## 前端架构方案
- 采用单前端项目方案：用户前台和管理员后台共用一个 Vue 项目
- 前台路由示例：`/`、`/resource/:id`、`/publish`、`/profile`
- 后台路由示例：`/admin`、`/admin/users`、`/admin/resources`
- 通过不同 Layout 区分界面，通过路由守卫和角色权限限制后台访问
- 当前方案适合小型社区，后续如果后台复杂度上升，可平滑拆分为独立的 `frontend-user` 与 `frontend-admin`

## 项目结构
```
forum/
├── frontend/          # Vue3 前端项目
├── backend/           # Spring Boot 后端项目
├── docs/              # 文档
│   ├── api.md        # API接口文档
│   ├── database.md   # 数据库设计
│   └── deploy.md     # 部署文档
└── README.md
```

## 快速开始

### 本地开发默认端口
- 前端：`5174`
- 后端：`8092`
- MySQL：`3306`

### 1. 准备数据库
先在本地 MySQL 中创建数据库：

```sql
CREATE DATABASE forum_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. 配置后端数据库连接
编辑文件：`backend/src/main/resources/application-dev.yml`

默认配置：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/forum_db?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: root
```

如果你的本地 MySQL 用户名或密码不同，请改成你自己的配置。

### 3. 启动后端
```bash
cd backend
mvn spring-boot:run
```

启动成功后访问：
- `http://localhost:8092`

开发环境会自动执行：
- `backend/src/main/resources/schema.sql`
- `backend/src/main/resources/data.sql`

用于初始化表结构、默认分类和管理员账号。

### 4. 启动前端
```bash
cd frontend
npm install
npm run dev
```

启动成功后访问：
- `http://localhost:5174`

### 5. 默认管理员账号
开发环境初始化后可使用以下管理员账号登录：

- 用户名：`admin`
- 邮箱：`admin@example.com`
- 密码：`admin123`

后台地址：
- `http://localhost:5174/admin`

### 6. 本地联调测试流程
建议按下面顺序验证：

1. 注册一个普通用户
2. 普通用户登录后发布资源
3. 资源提交后进入待审核，不会立刻显示在首页
4. 使用管理员账号登录后台
5. 进入 `/admin/resources` 审核资源
6. 审核通过后，回到首页确认资源已显示
7. 打开资源详情页，确认正文内容正常渲染

### 7. 端口冲突时改哪里
如果本地端口再次冲突，可以改下面几个文件：

- 前端端口和代理目标：`frontend/vite.config.js`
- 后端端口：`backend/src/main/resources/application-dev.yml`
- 后端跨域白名单：`backend/src/main/java/com/forum/config/CorsConfig.java`

### 8. 相关文档
- 接口文档：`docs/api.md`
- 数据库设计：`docs/database.md`
- 架构设计：`docs/architecture.md`
- 开发指南：`docs/development.md`
- 部署文档：`docs/deploy.md`

