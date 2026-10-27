# 10 - 盛泰协同办公平台 (OA-Suite) 生产交付部署与运维实操手册

本文档为盛泰新一代协同办公系统（`oa-dev`）的标准生产交付规范，旨在指导技术人员在各种服务器环境下顺利完成系统的投产、上线与长期稳定运行。

系统提供 **两种全套并行的交付策略**，您可以根据企业现有的 IT 基础设施和运维习惯自由选择：
* **选项 A（现代容器化）：Docker-Compose 全栈一键编排部署（推荐，极简开箱即用）**
* **选项 B（传统物理机）：独立可执行 JAR + Nginx + 本地 MySQL / 宝塔面板部署**

---

## 1. 系统运行环境基准要求

| 组件名称 | 推荐版本 | 最低要求 | 作用与说明 |
| :--- | :--- | :--- | :--- |
| **操作系统** | Ubuntu 22.04 LTS / CentOS 7.9+ / Windows Server 2019+ | 任意主流 64 位 Linux / Windows | 宿主服务器系统 |
| **Java 运行环境** | Eclipse Temurin OpenJDK 17.0.10+ | JDK 17 (LTS) | Spring Boot 3 核心运行底座 |
| **关系型数据库** | MySQL 8.0.36+ | MySQL 8.0+ | 核心业务、流程状态机与权限存储 |
| **Web 网关代理** | Nginx 1.24+ | Nginx 1.18+ | 前端静态页面托管、Gzip 压缩与 API 反向代理 |
| **服务器硬件** | 4 核 CPU / 8GB 内存 / 50GB SSD | 2 核 CPU / 4GB 内存 | 满足日常 100~1000 人并发办公协同 |

---

## 2. 选项 A：全栈 Docker-Compose 容器化一键部署（推荐）

该方案将 MySQL 8.0、Spring Boot 3 后端服务与 Nginx 前端静态容器打包在一个虚拟网络内，**全自动完成数据库建库、表结构初始化与种子数据导入**，无需手动配置环境。

### 2.1 部署包目录结构 (`oa-dev/docker/`)
```text
oa-dev/docker/
├── docker-compose.yml       # 容器编排定义文件
├── nginx.conf               # 容器内 Nginx 高性能网关配置
├── start.bat                # Windows 环境一键拉起脚本
├── start.sh                 # Linux 环境一键拉起脚本
├── stop.bat                 # Windows 停止集群脚本
└── stop.sh                  # Linux 停止集群脚本
```

### 2.2 极速启动步骤

#### 【Windows 环境】
1. 确保服务器已安装并启动 **Docker Desktop**；
2. 双击运行 `oa-dev\docker\start.bat`；
3. 脚本将自动构建后端镜像，拉取 MySQL 8 与 Nginx，并完成数据库全量脚本自动灌入。

#### 【Linux 环境】
```bash
# 1. 进入 docker 编排目录
cd oa-dev/docker

# 2. 赋予脚本执行权限并启动集群
chmod +x *.sh
./start.sh

# 3. 查看容器健康状态
docker compose ps
```

### 2.3 验证访问入口
* **PC 电脑端协同大厅**：`http://服务器IP/`（默认 80 端口）
* **手机移动端 H5 专版**：`http://服务器IP/m/`
* **默认超级管理员账号**：`admin` / 密码：`123456`
* **默认普通员工演示账号**：`zhangsan`（张三） / 密码：`123456`

---

## 3. 选项 B：传统裸机 / 物理服务器 / 宝塔面板部署

对于已有自建 MySQL 数据库、或习惯使用宝塔面板（BT-Panel）进行运维的企业，系统在 `oa-dev/release/` 目录下准备好了完整的独立分发包。

### 3.1 分发包目录结构 (`oa-dev/release/`)
```text
oa-dev/release/
├── oa-backend-1.0.0.jar      # Spring Boot 3 独立可执行 Fat JAR (38.8MB)
├── bin/
│   ├── start-backend.bat     # Windows 物理机启动脚本
│   ├── stop-backend.bat      # Windows 终止进程脚本
│   ├── start-backend.sh      # Linux 后台 nohup 守护启动脚本
│   ├── stop-backend.sh       # Linux 优雅退出停机脚本
│   └── oa-suite.service      # Linux systemd 系统服务单元文件
├── config/
│   └── application-prod.yml  # 生产环境独立外部配置文件 (可直接修改DB密码)
├── dist/                     # Vue 3 前端编译后的完整生产静态资源 (HTML/CSS/JS)
├── nginx/
│   └── oa-suite.conf         # 生产 Nginx 站点配置模板 (带代理与缓存)
└── sql/
    ├── schema.sql            # 数据库全量结构 DDL (23 张核心业务表)
    └── seed-data.sql         # 预置种子字典、流程、权限与演示数据
```

---

### 3.2 宝塔面板 (BT-Panel) 4 步极速部署流程

如果您使用宝塔面板管理 Linux 服务器，按以下 4 步即可在 5 分钟内投产上线：

#### 第 1 步：创建数据库并导入 SQL
1. 进入宝塔面板 -> **【数据库】** -> 点击 **【添加数据库】**；
2. 数据库名填写 `oa_suite`，字符集选择 `utf8mb4`，记录下数据库密码；
3. 点击右侧 **【导入】**，分别上传并执行 `release/sql/schema.sql` 和 `release/sql/seed-data.sql`。

#### 第 2 步：部署后端 Java 服务
1. 进入宝塔面板 -> **【网站】** -> **【Java 项目】**；
2. 项目类型选择 **Spring Boot**；
3. 项目 JAR 路径选择上传的 `release/oa-backend-1.0.0.jar`；
4. 选择服务器上的 **JDK 17**；
5. 运行端口填 `8080`；
6. 在“项目配置”中修改 `application-prod.yml` 内的数据库账号密码，点击【确定】启动。

#### 第 3 步：部署前端静态站点
1. 进入宝塔面板 -> **【网站】** -> **【PHP/HTML 项目】**（纯静态）；
2. 添加站点，输入您的企业域名或 IP（如 `oa.shengtai.com`），端口为 `80`；
3. 根目录设置为将 `release/dist` 上传后的目录。

#### 第 4 步：配置反向代理与 History 路由
在刚才创建的静态站点设置中：
1. 点击 **【配置文件】**，在 `server { ... }` 中粘贴以下反向代理规则：
   ```nginx
   # 1. 前端路由 History 模式防刷新 404
   location / {
       try_files $uri $uri/ /index.html;
   }

   # 2. 接口转发至后端的 8080 端口
   location /api/ {
       proxy_pass http://127.0.0.1:8080/api/;
       proxy_set_header Host $host;
       proxy_set_header X-Real-IP $remote_addr;
       proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
       proxy_set_header X-Forwarded-Proto $scheme;
   }
   ```
2. 保存并重载 Nginx，即可在浏览器中通过域名正常使用！

---

### 3.3 Linux 原生命令行部署 (Systemd 守护进程)

如果您习惯使用纯 Linux 命令行管理：

```bash
# 1. 创建部署主目录并解压
mkdir -p /opt/oa-suite
cp -r oa-dev/release/* /opt/oa-suite/
cd /opt/oa-suite

# 2. 导入 MySQL 数据库 (需已安装 MySQL 8)
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS oa_suite DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;"
mysql -u root -p oa_suite < sql/schema.sql
mysql -u root -p oa_suite < sql/seed-data.sql

# 3. 安装为系统服务 (开机自启)
cp bin/oa-suite.service /etc/systemd/system/
systemctl daemon-reload
systemctl enable --now oa-suite

# 4. 查看后端运行状态与日志
systemctl status oa-suite
tail -f logs/oa-backend.log

# 5. 配置宿主机 Nginx 
cp nginx/oa-suite.conf /etc/nginx/conf.d/
nginx -t && nginx -s reload
```

---

## 4. 关键参数优化与运维指南

### 4.1 JVM 调优参数（已内置在启动脚本中）
针对 4G 内存及以上服务器的推荐 JVM 参数：
```text
-server 
-Xms512m 
-Xmx1024m 
-XX:+UseG1GC 
-XX:MaxGCPauseMillis=200 
-XX:+HeapDumpOnOutOfMemoryError 
-XX:HeapDumpPath=/opt/oa-suite/logs/heapdump.hprof 
-Dfile.encoding=UTF-8
```

### 4.2 Nginx 附件上传大小限制
为了支持大容量红头文件、印章审批合同及凭证扫描件上传，必须在 Nginx 配置中确保包含：
```nginx
client_max_body_size 50m;
```
*(Spring Boot 后端在 `application-prod.yml` 中已同步配置 `max-file-size: 50MB`)*。

### 4.3 数据库定期自动备份建议 (Cron Job)
建议在 Linux 服务器中配置每日凌晨自动备份脚本：
```bash
# 每天凌晨 2:00 自动备份数据库并保留最近 30 天
0 2 * * * /usr/bin/mysqldump -u root -p'YOUR_PWD' oa_suite | gzip > /opt/backup/oa_suite_$(date +\%F).sql.gz
find /opt/backup/ -name "oa_suite_*.sql.gz" -mtime +30 -delete
```

---

## 5. 常见运维 FAQ 与故障排查

| 故障现象 | 常见原因 | 解决方案 |
| :--- | :--- | :--- |
| **启动提示 8080 端口已被占用** | 旧的 Java 进程未杀干净或被 Tomcat 占用 | 执行 `bin/stop-backend.sh`，或 `lsof -i:8080` 查出 PID 执行 `kill -9 <PID>`。 |
| **浏览器刷新网页提示 404** | Nginx 未配置 SPA History 回退 | 检查 Nginx 配置文件中是否包含 `try_files $uri $uri/ /index.html;`。 |
| **提示 Public Key Retrieval is not allowed** | MySQL 8.0 默认认证协议安全限制 | 数据库连接串必须加上 `&allowPublicKeyRetrieval=true&useSSL=false`。 |
| **打卡时间或公文时间慢了 8 小时** | 容器或服务器系统时区非上海时区 | 启动参数必须带 `-Duser.timezone=Asia/Shanghai`，数据库连接串带 `&serverTimezone=Asia/Shanghai`。 |
