# 部署文档 (Docker)

## 环境要求

- 操作系统: Linux (推荐 Ubuntu 20.04+)
- Docker: 20.10+
- Docker Compose: v2+
- 配置: 2核4G+

## 首次部署

### 1. 安装 Docker

```bash
curl -fsSL https://get.docker.com | bash -s docker
sudo usermod -aG docker $USER
# 退出重新登录使组生效
```

### 2. 克隆项目

```bash
git clone https://gitee.com/knowei/forum.git /opt/app/forum
cd /opt/app/forum
```

### 3. 配置敏感信息

创建 `.env` 文件（不要提交到 git，用于存放邮箱授权码等敏感信息）：

```bash
echo 'MAIL_PASSWORD=你的QQ邮箱授权码' > .env
```

也可同时配置其他环境变量，一行一个：
```bash
# .env 文件内容示例
MAIL_PASSWORD=你的QQ邮箱授权码
```

### 4. 启动服务

```bash
docker compose up -d
```

首次启动会自动构建镜像并下载依赖，耗时 3-5 分钟。

### 5. 访问

- 前端: `http://服务器IP:5174`
- 后端 API: `http://服务器IP:8092`

> 如需使用 80 端口访问，将 `docker-compose.yml` 中前端的端口映射改为 `"80:80"`。

### 6. 防火墙/安全组

云服务器需要放行以下端口：
- `5174` (前端)
- `8092` (后端 API，可选)
- `3307` (MySQL 远程连接，建议不对外)

---

## 日常更新

拉取最新代码后重新构建并重启：

```bash
cd /opt/app/forum

# 拉取最新代码
git pull

# 只重新构建后端（代码变动最频繁）
docker compose build backend

# 或同时构建前后端
docker compose build

# 强制重建容器（确保使用最新镜像）
docker compose up -d --force-recreate
```

> 前端代码改动只需重建 frontend 服务，后端代码改动重建 backend。
> 如果构建后发现镜像已更新但容器还是旧版本（如容器运行时间未变），加上 `--force-recreate` 强制重建容器。

---

## 常用管理命令

```bash
# 查看容器状态
docker compose ps

# 查看日志
docker compose logs -f backend     # 后端日志
docker compose logs -f frontend    # 前端日志
docker compose logs -f mysql       # 数据库日志

# 重启单个服务
docker compose restart backend

# 停止所有服务
docker compose down

# 停止并删除数据卷（会清空数据库和上传文件）
docker compose down -v
```

---

## 数据持久化

- **数据库**: Docker 命名卷 `mysql_data`，存放在 `/var/lib/docker/volumes/forum_mysql_data/`
- **上传文件**: Docker 命名卷 `uploads_data`，存放在 `/var/lib/docker/volumes/forum_uploads_data/`

### 备份数据库

```bash
# 进入 MySQL 容器导出
docker exec forum-mysql mysqldump -u forum_user -pforum_pass_2026 forum_db > backup_$(date +%Y%m%d).sql
```

### 恢复数据库

```bash
cat backup_20260514.sql | docker exec -i forum-mysql mysql -u forum_user -pforum_pass_2026 forum_db
```

---

## 绑定域名 + SSL

### 1. 修改 docker-compose.yml

```yaml
frontend:
  ports:
    - "80:80"
    - "443:443"
```

### 2. 修改 nginx.conf 添加域名和 SSL

### 3. 安装证书

```bash
docker exec forum-frontend apk add certbot
docker exec forum-frontend certbot --nginx -d your-domain.com
```

> 更推荐在服务器上单独用 Nginx 反代，不推荐在容器内管理证书。

---

## 常见问题

### 构建卡在 `mvn dependency:go-offline`

Maven 下载依赖时网络超时，Dockerfile 已内置超时参数。如果仍卡住，尝试：

```bash
docker compose build --no-cache backend
```

### 后端容器不断重启 (`Restarting`)

通常是配置文件问题。查看日志定位原因：

```bash
docker compose logs backend
```

常见原因：
- **`MAIL_PASSWORD` 未设置**: 检查 `.env` 文件是否存在，内容为 `MAIL_PASSWORD=你的QQ邮箱授权码`
- **数据库连接失败**: 确认 MySQL 容器已就绪，`application-docker.yml` 中数据库账号密码与 `docker-compose.yml` 一致

### 构建后镜像更新了但容器还是旧的

Docker Compose 默认不会重建已存在的容器。用 `--force-recreate` 强制重建：

```bash
docker compose up -d --force-recreate
```

### GitHub 推送失败

国内网络可能无法访问 GitHub，项目已配置双远程：

```bash
# 查看远程仓库
git remote -v
# 输出:
# origin  https://gitee.com/knowei/forum.git (fetch)
# origin  https://gitee.com/knowei/forum.git (push)
# origin  https://github.com/knowei/forum.git (push)

# 仅推送到 Gitee
git push origin

# 如果 GitHub 连不上不影响，Gitee 同步后即可部署
```
