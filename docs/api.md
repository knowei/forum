# API 接口文档

## 基础说明
- 基础路径: `/api`
- 认证方式: JWT Token (请求头: `Authorization: Bearer {token}`)
- 响应格式:
```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

## 1. 用户模块

### 1.1 用户注册
- **POST** `/api/auth/register`
- 请求体:
```json
{
  "username": "string",
  "password": "string",
  "email": "string",
  "nickname": "string"
}
```

### 1.2 用户登录
- **POST** `/api/auth/login`
- 请求体:
```json
{
  "username": "string",
  "password": "string"
}
```
- 响应:
```json
{
  "token": "string",
  "userInfo": {
    "id": 1,
    "username": "string",
    "nickname": "string",
    "avatar": "string",
    "role": 0
  }
}
```

### 1.3 获取当前用户信息
- **GET** `/api/user/info`
- 需要认证

### 1.4 更新用户信息
- **PUT** `/api/user/info`
- 需要认证
- 请求体:
```json
{
  "nickname": "string",
  "email": "string",
  "avatar": "string"
}
```

### 1.5 修改密码
- **PUT** `/api/user/password`
- 需要认证
- 请求体:
```json
{
  "oldPassword": "string",
  "newPassword": "string"
}
```

## 2. 资源模块

### 2.1 发布资源
- **POST** `/api/resource`
- 需要认证
- 请求体:
```json
{
  "categoryId": 1,
  "title": "string",
  "description": "string",
  "content": "string",
  "cover": "string",
  "downloadLink": "string",
  "extractCode": "string",
  "unzipPassword": "string",
  "tags": "tag1,tag2"
}
```
- 字段说明:
  - `description`: 简短描述（最多 500 字），用于列表展示
  - `content`: 详细正文内容（支持 Markdown），用于详情页展示
- 当前首版行为:
  - 普通用户提交后默认进入待审核状态 `status=0`
  - 资源不会立刻出现在前台列表页

### 2.2 资源列表
- **GET** `/api/resource/list`
- 查询参数:
  - `page`: 页码 (默认1)
  - `size`: 每页数量 (默认10)
  - `categoryId`: 分类ID (可选)
  - `keyword`: 搜索关键词 (可选)
  - `orderBy`: 排序方式 (latest/hot/view, 默认latest)
- 响应:
```json
{
  "total": 100,
  "list": [
    {
      "id": 1,
      "title": "string",
      "description": "string",
      "cover": "string",
      "tags": "tag1,tag2",
      "viewCount": 100,
      "likeCount": 10,
      "collectCount": 5,
      "commentCount": 3,
      "author": {
        "id": 1,
        "nickname": "string",
        "avatar": "string"
      },
      "createTime": "2026-05-12 10:00:00"
    }
  ]
}
```
- 注意: 列表接口不返回 `content` 字段，仅返回 `description`

### 2.3 资源详情
- **GET** `/api/resource/{id}`
- 自动增加浏览次数
- 访问规则:
  - 已发布资源可公开访问
  - 待审核/已驳回资源仅作者本人和管理员可访问
- 响应:
```json
{
  "id": 1,
  "title": "string",
  "description": "string",
  "content": "string",
  "cover": "string",
  "downloadLink": "string",
  "extractCode": "string",
  "unzipPassword": "string",
  "tags": "tag1,tag2",
  "viewCount": 100,
  "likeCount": 10,
  "collectCount": 5,
  "commentCount": 3,
  "status": 1,
  "authorId": 1,
  "authorNickname": "string",
  "authorAvatar": "string",
  "createTime": "2026-05-12T10:00:00",
  "updateTime": "2026-05-12T10:00:00"
}
```
- 注意: 当前实现直接返回作者字段，不嵌套 `author` 对象

### 2.4 更新资源
- **PUT** `/api/resource/{id}`
- 需要认证 (仅作者或管理员)

### 2.5 删除资源
- **DELETE** `/api/resource/{id}`
- 需要认证 (仅作者或管理员)

### 2.6 我的资源列表
- **GET** `/api/resource/my`
- 需要认证
- 查询参数: `page`, `size`

## 3. 分类模块

### 3.1 分类列表
- **GET** `/api/category/list`

### 3.2 创建分类
- **POST** `/api/category`
- 需要管理员权限

### 3.3 更新分类
- **PUT** `/api/category/{id}`
- 需要管理员权限

### 3.4 删除分类
- **DELETE** `/api/category/{id}`
- 需要管理员权限

## 4. 评论模块

### 4.1 发表评论
- **POST** `/api/comment`
- 需要认证
- 请求体:
```json
{
  "resourceId": 1,
  "parentId": 0,
  "content": "string"
}
```

### 4.2 评论列表
- **GET** `/api/comment/list`
- 查询参数:
  - `resourceId`: 资源ID
  - `page`: 页码
  - `size`: 每页数量

### 4.3 删除评论
- **DELETE** `/api/comment/{id}`
- 需要认证 (仅作者或管理员)

## 5. 互动模块

### 5.1 点赞/取消点赞
- **POST** `/api/like`
- 需要认证
- 请求体:
```json
{
  "targetId": 1,
  "targetType": 1
}
```

### 5.2 收藏/取消收藏
- **POST** `/api/collect`
- 需要认证
- 请求体:
```json
{
  "resourceId": 1
}
```

### 5.3 我的收藏列表
- **GET** `/api/collect/my`
- 需要认证
- 查询参数: `page`, `size`

## 6. 管理模块

### 6.1 用户管理
- **GET** `/api/admin/user/list` - 用户列表
- **PUT** `/api/admin/user/{id}/status` - 修改用户状态
- 需要管理员权限

### 6.2 资源审核
- **GET** `/api/admin/resource/pending` - 待审核资源
- **PUT** `/api/admin/resource/{id}/status` - 审核资源
- 需要管理员权限
- 审核请求体:
```json
{
  "status": 1
}
```
- 状态说明:
  - `1`: 审核通过并发布
  - `2`: 驳回 / 下架

### 6.3 数据统计
- **GET** `/api/admin/statistics` - 获取统计数据
- 需要管理员权限
- 响应:
```json
{
  "userCount": 100,
  "resourceCount": 500,
  "pendingResourceCount": 12
}
```

## 错误码说明
- 200: 成功
- 400: 请求参数错误
- 401: 未认证
- 403: 无权限
- 404: 资源不存在
- 409: 资源冲突
- 500: 服务器错误
