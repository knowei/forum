# 开发指南

## 快速开始

### 1. 克隆项目
```bash
cd D:\pro\work\proj\forum
```

### 2. 后端开发

#### 2.1 创建 Spring Boot 项目
使用 Spring Initializr 或 IDE 创建项目，添加以下依赖:
- Spring Web
- Spring Security
- MyBatis Plus
- MySQL Driver
- Lombok
- JWT (io.jsonwebtoken)
- Validation

#### 2.2 配置文件
`application-dev.yml`:
```yaml
server:
  port: 8092

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/forum_db?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true

jwt:
  secret: change-this-secret-key-change-this-secret-key
  expiration: 604800000
```

#### 2.3 启动后端
```bash
cd backend
mvn spring-boot:run
```

访问: http://localhost:8092

### 3. 前端开发

#### 3.1 创建 Vue3 项目
```bash
npm create vite@latest frontend -- --template vue
cd frontend
npm install
```

#### 3.2 安装依赖
```bash
npm install vue-router@4 pinia axios element-plus
npm install @element-plus/icons-vue
npm install sass -D
```

#### 3.3 配置环境变量
`.env.development`:
```
VITE_API_BASE_URL=/api
```

`.env.production`:
```
VITE_API_BASE_URL=/api
```

#### 3.4 配置 Vite
`vite.config.js`:
```javascript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  server: {
    port: 5174,
    proxy: {
      '/api': {
        target: 'http://localhost:8092',
        changeOrigin: true
      }
    }
  }
})
```

#### 3.5 启动前端
```bash
npm run dev
```

访问: http://localhost:5174

### 3.6 当前本地开发默认端口
- 前端开发服务器: `5174`
- 后端开发接口: `8092`
- MySQL: `3306`

如果这些端口后续又冲突，可以改这里：
- 前端端口：`frontend/vite.config.js`
- 前端代理目标：`frontend/vite.config.js`
- 后端端口：`backend/src/main/resources/application-dev.yml`
- 后端跨域白名单：`backend/src/main/java/com/forum/config/CorsConfig.java`

### 3.7 本地启动步骤
1. 先准备 MySQL 数据库：
```sql
CREATE DATABASE forum_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 修改后端数据库账号密码：
- 文件：`backend/src/main/resources/application-dev.yml`
- 默认值现在是：
  - `username: root`
  - `password: root`

3. 启动后端：
```bash
cd backend
mvn spring-boot:run
```
启动成功后访问：`http://localhost:8092`

4. 启动前端：
```bash
cd frontend
npm install
npm run dev
```
启动成功后访问：`http://localhost:5174`

5. 默认管理员账号：
- 用户名：`admin`
- 邮箱：`admin@example.com`
- 密码：`admin123`

6. 联调验证建议：
- 先注册一个普通用户
- 用普通用户登录并发布资源
- 资源会进入待审核，不会立刻显示在首页
- 再用管理员登录 `/admin`
- 到“资源审核”页通过该资源
- 回到首页确认资源已显示

## 开发规范

### 代码规范

#### 后端规范
1. **命名规范**
   - 类名: 大驼峰 (UserController)
   - 方法名: 小驼峰 (getUserInfo)
   - 常量: 大写下划线 (MAX_SIZE)
   - 包名: 小写 (com.forum.service)

2. **分层规范**
   - Controller: 接收请求、参数校验、返回响应
   - Service: 业务逻辑处理
   - Mapper: 数据库操作
   - Entity: 数据库实体
   - DTO: 数据传输对象
   - VO: 视图对象

3. **注释规范**
   - 类和方法必须有注释
   - 复杂逻辑必须注释
   - 使用 JavaDoc 格式

#### 前端规范
1. **命名规范**
   - 组件名: 大驼峰 (UserProfile.vue)
   - 方法名: 小驼峰 (getUserInfo)
   - 常量: 大写下划线 (API_BASE_URL)
   - CSS类名: 短横线 (user-card)

2. **组件规范**
   - 单文件组件 (.vue)
   - 使用 Composition API
   - Props 定义类型
   - Emit 事件命名清晰

3. **代码组织**
   - 按功能模块组织
   - 公共组件抽离
   - 工具函数统一管理

### Git 规范

#### 分支管理
- `main`: 主分支，生产环境
- `develop`: 开发分支
- `feature/*`: 功能分支
- `bugfix/*`: 修复分支

#### 提交规范
```
<type>(<scope>): <subject>

<body>

<footer>
```

类型 (type):
- feat: 新功能
- fix: 修复bug
- docs: 文档更新
- style: 代码格式
- refactor: 重构
- test: 测试
- chore: 构建/工具

示例:
```
feat(resource): 添加资源发布功能

- 实现资源表单提交
- 添加文件上传
- 完成数据验证

Closes #123
```

## 开发流程

### 1. 功能开发流程
1. 创建功能分支
2. 设计数据库表 (如需要)
3. 编写后端接口
4. 测试接口 (Postman/Apifox)
5. 编写前端页面
6. 联调测试
7. 代码审查
8. 合并到 develop

### 2. 接口开发流程
1. 定义接口文档
2. 创建 Controller
3. 实现 Service 逻辑
4. 编写 Mapper SQL
5. 参数校验
6. 异常处理
7. 单元测试

### 3. 页面开发流程
1. 设计页面原型
2. 创建 Vue 组件
3. 编写页面结构
4. 实现交互逻辑
5. 调用后端接口
6. 样式美化
7. 测试各种场景

## 常用工具

### 后端工具
- **IDE**: IntelliJ IDEA
- **API测试**: Postman / Apifox
- **数据库**: Navicat / DBeaver
- **版本控制**: Git
- **构建工具**: Maven

### 前端工具
- **IDE**: VS Code
- **浏览器**: Chrome + Vue DevTools
- **调试**: Vue DevTools
- **包管理**: npm / pnpm
- **构建工具**: Vite

## 调试技巧

### 后端调试
1. **日志调试**
```java
@Slf4j
public class UserService {
    public void test() {
        log.debug("调试信息");
        log.info("普通信息");
        log.error("错误信息", e);
    }
}
```

2. **断点调试**
   - IDEA 中设置断点
   - Debug 模式启动
   - 查看变量值

3. **SQL 调试**
```yaml
mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
```

### 前端调试
1. **Console 调试**
```javascript
console.log('变量值:', data)
console.table(list)
console.error('错误:', error)
```

2. **Vue DevTools**
   - 查看组件树
   - 查看状态
   - 查看路由

3. **Network 调试**
   - 查看请求响应
   - 检查状态码
   - 查看请求参数

## 常见问题

### 后端问题

**Q: 跨域问题**
```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000")
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
```

**Q: 文件上传失败**
- 检查文件大小限制
- 检查上传目录权限
- 检查文件类型限制

**Q: JWT Token 失效**
- 检查 Token 是否过期
- 检查 Secret 是否一致
- 检查请求头格式

### 前端问题

**Q: 路由跳转失败**
```javascript
// 使用 router.push
import { useRouter } from 'vue-router'
const router = useRouter()
router.push('/resource/1')
```

**Q: 状态不更新**
```javascript
// 确保使用响应式数据
import { ref, reactive } from 'vue'
const count = ref(0)
const user = reactive({ name: '' })
```

**Q: 接口请求失败**
- 检查 API 地址
- 检查请求方法
- 检查参数格式
- 查看 Network 面板

## 性能优化建议

### 后端优化
1. 使用索引优化查询
2. 避免 N+1 查询
3. 使用缓存 (Redis)
4. 分页查询
5. 异步处理耗时操作

### 前端优化
1. 路由懒加载
2. 组件按需引入
3. 图片懒加载
4. 防抖节流
5. 虚拟滚动 (长列表)

## 测试

### 后端测试
```java
@SpringBootTest
class UserServiceTest {
    @Autowired
    private UserService userService;
    
    @Test
    void testGetUser() {
        User user = userService.getById(1L);
        assertNotNull(user);
    }
}
```

### 前端测试
```javascript
// 使用 Vitest
import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import UserCard from '@/components/UserCard.vue'

describe('UserCard', () => {
  it('renders properly', () => {
    const wrapper = mount(UserCard, {
      props: { user: { name: 'Test' } }
    })
    expect(wrapper.text()).toContain('Test')
  })
})
```

## 参考资料

### 官方文档
- [Vue 3](https://cn.vuejs.org/)
- [Element Plus](https://element-plus.org/zh-CN/)
- [Spring Boot](https://spring.io/projects/spring-boot)
- [MyBatis Plus](https://baomidou.com/)

### 学习资源
- Vue 3 教程
- Spring Boot 实战
- MySQL 优化
- Nginx 配置
