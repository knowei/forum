# 系统架构设计

## 整体架构

```
┌─────────────────────────────────────────────────────────┐
│                      用户浏览器                          │
└────────────────────┬────────────────────────────────────┘
                     │ HTTP/HTTPS
┌────────────────────▼────────────────────────────────────┐
│                      Nginx                               │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │  静态资源    │  │  API 代理    │  │  文件服务    │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└────────────────────┬────────────────────────────────────┘
                     │
        ┌────────────┴────────────┐
        │                         │
┌───────▼────────┐       ┌────────▼────────┐
│  Vue3 前端     │       │  Spring Boot    │
│  ┌──────────┐  │       │  ┌──────────┐   │
│  │ 路由管理 │  │       │  │ 控制层   │   │
│  │ 状态管理 │  │       │  │ 服务层   │   │
│  │ 组件库   │  │       │  │ 数据层   │   │
│  └──────────┘  │       │  └──────────┘   │
└────────────────┘       └────────┬────────┘
                                  │
                         ┌────────┴────────┐
                         │                 │
                  ┌──────▼──────┐   ┌──────▼──────┐
                  │   MySQL     │   │   Redis     │
                  │  (主数据库)  │   │  (缓存)     │
                  └─────────────┘   └─────────────┘
```

## 前端架构

### 架构选择
- 当前采用**单前端项目**方案：前台展示站和后台管理台放在同一个 Vue 项目中
- 通过 **Vue Router 路由分区** 区分用户端与管理端
- 通过 **Layout 分层** 区分页面结构和视觉风格
- 通过 **角色权限控制** 限制 `/admin` 路由仅管理员可访问
- 后续如果管理后台复杂度明显上升，可拆分为两个独立前端项目，当前目录设计尽量保持可拆分

### 技术选型
- **框架**: Vue 3 (Composition API)
- **UI库**: Element Plus
- **状态管理**: Pinia
- **路由**: Vue Router 4
- **HTTP**: Axios
- **构建工具**: Vite

### 目录结构
```
frontend/
├── public/                 # 静态资源
├── src/
│   ├── api/               # API 接口封装
│   │   ├── user.js
│   │   ├── resource.js
│   │   ├── comment.js
│   │   └── admin.js
│   ├── assets/            # 资源文件
│   │   ├── images/
│   │   └── styles/
│   ├── components/        # 公共组件
│   │   ├── common/
│   │   ├── front/
│   │   └── admin/
│   ├── layouts/           # 布局组件
│   │   ├── FrontLayout.vue
│   │   └── AdminLayout.vue
│   ├── views/             # 页面组件
│   │   ├── front/
│   │   │   ├── Home.vue
│   │   │   ├── ResourceDetail.vue
│   │   │   ├── Publish.vue
│   │   │   └── Profile.vue
│   │   └── admin/
│   │       ├── Dashboard.vue
│   │       ├── UserManage.vue
│   │       ├── ResourceManage.vue
│   │       └── CategoryManage.vue
│   ├── router/            # 路由配置
│   │   └── index.js
│   ├── stores/            # 状态管理
│   │   ├── user.js
│   │   └── app.js
│   ├── utils/             # 工具函数
│   │   ├── request.js
│   │   ├── auth.js
│   │   └── validate.js
│   ├── App.vue
│   └── main.js
├── .env.development       # 开发环境配置
├── .env.production        # 生产环境配置
├── vite.config.js
└── package.json
```

### 核心页面
1. **首页** - 资源列表、分类导航、搜索
2. **资源详情** - 资源信息、下载链接、评论区
3. **发布资源** - 表单提交
4. **个人中心** - 我的资源、收藏、设置
5. **管理后台** - 用户管理、资源审核、统计

## 后端架构

### 技术选型
- **框架**: Spring Boot 3.x
- **ORM**: MyBatis Plus
- **安全**: Spring Security + JWT
- **缓存**: Redis (可选)
- **文件存储**: 本地存储
- **数据库**: MySQL 8.0

### 目录结构
```
backend/
├── src/main/java/com/forum/
│   ├── ForumApplication.java
│   ├── config/              # 配置类
│   │   ├── SecurityConfig.java
│   │   ├── MyBatisPlusConfig.java
│   │   ├── CorsConfig.java
│   │   └── WebMvcConfig.java
│   ├── controller/          # 控制器
│   │   ├── AuthController.java
│   │   ├── UserController.java
│   │   ├── ResourceController.java
│   │   ├── CommentController.java
│   │   ├── CategoryController.java
│   │   └── AdminController.java
│   ├── service/             # 服务层
│   │   ├── UserService.java
│   │   ├── ResourceService.java
│   │   ├── CommentService.java
│   │   └── FileService.java
│   ├── mapper/              # 数据访问层
│   │   ├── UserMapper.java
│   │   ├── ResourceMapper.java
│   │   └── CommentMapper.java
│   ├── entity/              # 实体类
│   │   ├── User.java
│   │   ├── Resource.java
│   │   ├── Comment.java
│   │   └── Category.java
│   ├── dto/                 # 数据传输对象
│   │   ├── LoginDTO.java
│   │   ├── ResourceDTO.java
│   │   └── PageDTO.java
│   ├── vo/                  # 视图对象
│   │   ├── UserVO.java
│   │   └── ResourceVO.java
│   ├── common/              # 公共类
│   │   ├── Result.java      # 统一响应
│   │   ├── ResultCode.java  # 响应码
│   │   └── PageResult.java  # 分页响应
│   ├── exception/           # 异常处理
│   │   ├── BusinessException.java
│   │   └── GlobalExceptionHandler.java
│   ├── security/            # 安全相关
│   │   ├── JwtUtil.java
│   │   ├── JwtFilter.java
│   │   └── UserDetailsServiceImpl.java
│   └── utils/               # 工具类
│       ├── MD5Util.java
│       └── FileUtil.java
├── src/main/resources/
│   ├── mapper/              # MyBatis XML
│   ├── application.yml      # 配置文件
│   ├── application-dev.yml
│   └── application-prod.yml
└── pom.xml
```

### 核心模块

#### 1. 认证授权
- JWT Token 生成和验证
- Spring Security 配置
- 用户权限控制

#### 2. 资源管理
- 资源 CRUD
- 分页查询
- 搜索功能
- 统计更新

#### 3. 文件上传
- 本地文件存储
- 文件类型验证
- 文件大小限制
- 图片压缩 (可选)

#### 4. 缓存策略
- 热门资源缓存
- 分类列表缓存
- 用户信息缓存

## 数据库设计要点

### 索引策略
- 主键索引: 所有表的 id
- 唯一索引: username, email
- 普通索引: user_id, category_id, status, create_time
- 全文索引: title, tags (用于搜索)
- 组合索引: (user_id, target_id, target_type) 用于点赞/收藏

### 性能优化
- 使用 InnoDB 引擎
- 合理使用索引
- 避免 SELECT *
- 分页查询优化
- 统计字段冗余 (view_count, like_count 等)

## 安全设计

### 1. 认证安全
- 密码 BCrypt 加密
- JWT Token 有效期控制
- Token 刷新机制

### 2. 接口安全
- CORS 跨域配置
- XSS 防护
- SQL 注入防护 (MyBatis 参数化)
- 文件上传类型限制

### 3. 数据安全
- 敏感信息加密
- 定期数据备份
- 操作日志记录

## 性能优化

### 前端优化
- 路由懒加载
- 图片懒加载
- 组件按需引入
- 打包压缩
- CDN 加速

### 后端优化
- 数据库连接池
- Redis 缓存
- 分页查询
- 异步处理
- 接口限流

### 服务器优化
- Nginx 静态资源缓存
- Gzip 压缩
- HTTP/2
- 数据库查询优化

## 扩展性设计

### 水平扩展
- 前端: CDN 分发
- 后端: 负载均衡 (Nginx)
- 数据库: 读写分离、分库分表

### 功能扩展
- 消息通知系统
- 积分系统
- 会员等级
- 付费资源
- 推荐算法
- 全文搜索 (Elasticsearch)

## 监控与运维

### 日志管理
- 应用日志 (Logback)
- 访问日志 (Nginx)
- 错误日志
- 操作日志

### 监控指标
- 服务器资源 (CPU、内存、磁盘)
- 应用性能 (响应时间、QPS)
- 数据库性能 (慢查询)
- 错误率统计

### 告警机制
- 服务宕机告警
- 资源使用告警
- 错误率告警
