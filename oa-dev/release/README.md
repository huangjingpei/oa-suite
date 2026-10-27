# OA-Suite 生产交付离线部署包 (Release Package)

本目录为面向传统物理机、云主机虚拟机及宝塔面板（BT-Panel）的原生部署交付结构。

---

## 目录结构说明

```text
oa-suite/oa-dev/release/
├── bin/                          # 服务启停与系统管理脚本
│   ├── oa-suite.service          # Linux systemd 守护进程单元模板
│   ├── start-backend.bat         # Windows 后端前台/后台启动脚本
│   ├── stop-backend.bat          # Windows 后端停止脚本
│   ├── start-backend.sh          # Linux 后端 nohup 后台启动脚本（含 PID 管理）
│   └── stop-backend.sh           # Linux 后端优雅停机脚本
├── config/                       # 生产环境外部化配置
│   └── application-prod.yml      # 生产数据库连接、端口、文件上传与 Sa-Token 配置
├── nginx/                        # 反向代理配置
│   └── oa-suite.conf             # 生产级 Nginx 配置（含前端路由重定向、50MB大文件上传、静态缓存）
├── sql/                          # 数据库初始化脚本
│   ├── schema.sql                # 核心表结构定义（含字符集、完整业务表）
│   └── seed-data.sql             # 基础数据字典、管理员账号与初始流程模板
├── dist/                         # 前端编译静态资源产物（执行 build-all 脚本自动生成）
└── oa-backend-1.0.0.jar          # 后端独立可执行 Fat JAR（执行 build-all 脚本自动生成）
```

---

## 快速打包生成产物

在项目根目录或 `oa-dev` 目录下运行一键打包脚本：
- **Windows**: 双击或在 PowerShell 中执行 `oa-dev\build-all.bat`
- **Linux / macOS**: 执行 `bash oa-dev/build-all.sh`

脚本将自动执行：
1. 前端 TypeScript 严格校验与生产压缩构建（`vite build`），产物输出至 `release/dist`；
2. 后端 Maven 构建打包（`mvn clean package`），生成独立 Spring Boot Fat JAR 到 `release/oa-backend-1.0.0.jar`；
3. 同步最新配置与 SQL 脚本至本发布目录。

---

## 部署运行快速指引

- **宝塔面板快速部署**：
  1. 在宝塔面板创建 MySQL 数据库并导入 `sql/schema.sql` 与 `sql/seed-data.sql`；
  2. 使用“Java 项目管理器”，添加 Spring Boot 项目并指向 `oa-backend-1.0.0.jar`，选用 JDK 17；
  3. 创建 Nginx 静态站点，根目录指向 `dist/`，并将 `nginx/oa-suite.conf` 中的反向代理规则配置进站点。
- **Linux 独立运行**：
  进入 `bin/` 目录执行 `bash start-backend.sh`，或注册为系统服务 `systemctl start oa-suite`。

详细运维部署及排障文档请查阅：[`docs/10-deployment-and-operations-guide.md`](../docs/10-deployment-and-operations-guide.md)。
