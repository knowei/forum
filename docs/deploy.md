# 部署文档

## 服务器环境要求
- 操作系统: Linux (推荐 Ubuntu 20.04+)
- 配置: 2核4G
- 软件: JDK 17, MySQL 8.0, Nginx, Redis (可选)

## 1. 环境准备

### 1.1 安装 JDK 17
```bash
sudo apt update
sudo apt install openjdk-17-jdk -y
java -version
```

### 1.2 安装 MySQL 8.0
```bash
sudo apt install mysql-server -y
sudo mysql_secure_installation

# 创建数据库和用户
mysql -u root -p
CREATE DATABASE forum_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'forum_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON forum_db.* TO 'forum_user'@'localhost';
FLUSH PRIVILEGES;
```

### 1.3 安装 Nginx
```bash
sudo apt install nginx -y
sudo systemctl start nginx
sudo systemctl enable nginx
```

### 1.4 安装 Node.js (用于构建前端)
```bash
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt install nodejs -y
node -v
npm -v
```

## 2. 后端部署

### 2.1 配置文件
编辑 `backend/src/main/resources/application-prod.yml`:
```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/forum_db?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: forum_user
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
  
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB

jwt:
  secret: your-secret-key-change-in-production
  expiration: 604800

file:
  upload-path: /var/www/forum/uploads
```

### 2.2 打包部署
```bash
cd backend
mvn clean package -DskipTests

# 创建部署目录
sudo mkdir -p /opt/forum
sudo cp target/forum-backend.jar /opt/forum/

# 创建 systemd 服务
sudo nano /etc/systemd/system/forum-backend.service
```

服务配置文件内容:
```ini
[Unit]
Description=Forum Backend Service
After=mysql.service

[Service]
Type=simple
User=www-data
WorkingDirectory=/opt/forum
ExecStart=/usr/bin/java -jar -Xms512m -Xmx1024m -Dspring.profiles.active=prod /opt/forum/forum-backend.jar
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
```

启动服务:
```bash
sudo systemctl daemon-reload
sudo systemctl start forum-backend
sudo systemctl enable forum-backend
sudo systemctl status forum-backend
```

## 3. 前端部署

### 3.1 构建前端
```bash
cd frontend
npm install
npm run build
```

### 3.2 部署到 Nginx
```bash
sudo mkdir -p /var/www/forum
sudo cp -r dist/* /var/www/forum/
sudo chown -R www-data:www-data /var/www/forum
```

### 3.3 配置 Nginx
```bash
sudo nano /etc/nginx/sites-available/forum
```

Nginx 配置:
```nginx
server {
    listen 80;
    server_name your-domain.com;  # 修改为你的域名或IP
    
    # 前端静态文件
    location / {
        root /var/www/forum;
        index index.html;
        try_files $uri $uri/ /index.html;
    }
    
    # 后端API代理
    location /api/ {
        proxy_pass http://localhost:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
    
    # 上传文件访问
    location /uploads/ {
        alias /var/www/forum/uploads/;
        expires 30d;
    }
    
    # 文件上传大小限制
    client_max_body_size 10M;
}
```

启用站点:
```bash
sudo ln -s /etc/nginx/sites-available/forum /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx
```

## 4. 数据库初始化

开发环境默认使用 Spring Boot 自动执行：
- `backend/src/main/resources/schema.sql`
- `backend/src/main/resources/data.sql`

生产环境可手动执行：
```bash
mysql -u forum_user -p forum_db < backend/src/main/resources/schema.sql
mysql -u forum_user -p forum_db < backend/src/main/resources/data.sql
```

## 5. 文件上传目录

```bash
sudo mkdir -p /var/www/forum/uploads/{avatar,cover}
sudo chown -R www-data:www-data /var/www/forum/uploads
sudo chmod -R 755 /var/www/forum/uploads
```

## 6. 防火墙配置

```bash
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw allow 22/tcp
sudo ufw enable
```

## 7. SSL 证书 (可选)

使用 Let's Encrypt 免费证书:
```bash
sudo apt install certbot python3-certbot-nginx -y
sudo certbot --nginx -d your-domain.com
```

## 8. 日志管理

### 后端日志
```bash
# 查看日志
sudo journalctl -u forum-backend -f

# 日志配置 (application-prod.yml)
logging:
  file:
    name: /var/log/forum/backend.log
  level:
    root: INFO
    com.forum: DEBUG
```

### Nginx 日志
```bash
# 访问日志
tail -f /var/log/nginx/access.log

# 错误日志
tail -f /var/log/nginx/error.log
```

## 9. 备份策略

### 数据库备份脚本
```bash
#!/bin/bash
# /opt/forum/backup.sh

BACKUP_DIR="/opt/forum/backups"
DATE=$(date +%Y%m%d_%H%M%S)
mkdir -p $BACKUP_DIR

# 备份数据库
mysqldump -u forum_user -p'your_password' forum_db > $BACKUP_DIR/forum_db_$DATE.sql

# 删除7天前的备份
find $BACKUP_DIR -name "*.sql" -mtime +7 -delete

echo "Backup completed: forum_db_$DATE.sql"
```

添加定时任务:
```bash
crontab -e
# 每天凌晨2点备份
0 2 * * * /opt/forum/backup.sh
```

## 10. 性能优化

### MySQL 优化
```sql
-- 添加索引
ALTER TABLE resource ADD INDEX idx_hot (like_count, view_count, create_time);

-- 查询缓存 (my.cnf)
query_cache_type = 1
query_cache_size = 64M
```

### Nginx 缓存
```nginx
# 在 http 块中添加
proxy_cache_path /var/cache/nginx levels=1:2 keys_zone=api_cache:10m max_size=100m inactive=60m;

# 在 location /api/ 中添加
proxy_cache api_cache;
proxy_cache_valid 200 5m;
proxy_cache_key "$scheme$request_method$host$request_uri";
```

## 11. 监控

### 系统监控
```bash
# 安装 htop
sudo apt install htop -y

# 查看资源使用
htop
df -h
free -h
```

### 应用监控
```bash
# 检查服务状态
sudo systemctl status forum-backend
sudo systemctl status nginx
sudo systemctl status mysql

# 检查端口
sudo netstat -tlnp | grep -E '80|8080|3306'
```

## 12. 故障排查

### 后端无法启动
```bash
# 查看日志
sudo journalctl -u forum-backend -n 100

# 检查端口占用
sudo lsof -i :8080

# 检查数据库连接
mysql -u forum_user -p -h localhost forum_db
```

### 前端无法访问
```bash
# 检查 Nginx 配置
sudo nginx -t

# 查看错误日志
sudo tail -f /var/log/nginx/error.log

# 检查文件权限
ls -la /var/www/forum
```

## 13. 更新部署

### 后端更新
```bash
cd backend
git pull
mvn clean package -DskipTests
sudo systemctl stop forum-backend
sudo cp target/forum-backend.jar /opt/forum/
sudo systemctl start forum-backend
```

### 前端更新
```bash
cd frontend
git pull
npm install
npm run build
sudo rm -rf /var/www/forum/*
sudo cp -r dist/* /var/www/forum/
```
