# 现代企业级轻量协同办公系统 (OA-Suite Modernization)

> **项目定位**：高内聚、高安全、开箱即用的现代化企业级办公与流程审批系统。  
> **演进历程**：由 2020 年代基于 xmysql + Vue 2 + JeecgBoot 旧系统全面重构现代化，业务逻辑 100% 收口至服务端事务，前端升级为 TypeScript + Vue 3 + Ant Design Vue 4。  
> **交付状态**：核心行政、人事、协同、自研轻量流程引擎、双模式工作台、企业群通知机器人已全面交付，支持 **Docker 容器编排** 与 **物理机/宝塔面板** 双模式部署。

---

## 🌟 系统核心特性与能力全景

### 1. ⚙️ 自研轻量状态机工作流引擎
- **无重型 BPMN 负担**：替代笨重的 Activity/Camunda，轻量级高效支持自由指派、会签、加签、退回、知会与历史流转记录。
- **全链路严密审计**：操作历史追踪、状态流转强事务保证（`@Transactional`），杜绝越权篡改与脏写。

### 2. 🏛️ / 💼 独创双模工作台 (Dual-Mode Dashboard)
- **💼 现代待办中枢 (Modern Hub)**：极简卡片、待办/已办聚焦、快捷发起申请，符合现代敏捷办公习惯。
- **🏛️ 传统全景服务大厅 (Classic Portal)**：一键平滑切换，提供 22 项全景业务服务卡片、五维效能雷达图（考勤准时、流转提效、公文活跃、规范借阅、用印履约）及快速服务直达，完美兼容政企老干部与传统组织习惯。

### 3. 📋 完整行政与人事业务闭环
- **行政用印**：公章用印、合同用印、资质申报印章申请，支持防伪水印与高保真纸质打印排版。
- **红头公文与档案**：红头公文密级/下发流转、档案借阅与保密归还追踪。
- **考勤与请假**：多类型请假（年假/病假/事假/调休等）、考勤月度统计、外出打卡报备、补卡申请。
- **公告协同与矩阵**：通知公告下发、已读/未读员工精准追踪矩阵、一键提醒未读人员。

### 4. 🤖 开放式群通知机器人 (IM Webhook)
- 支持**企业微信、钉钉、飞书**群机器人自定义 Webhook 接入。
- 任务指派、审批同意、审批驳回、加签催办实时推送至群，即时触达移动端。

---

## 📂 仓库目录结构

```text
oa-suite/
├── oa-dev/                           # 现代化核心工程（主力开发与发布根目录）
│   ├── oa-backend/                   # Spring Boot 3 + Java 17 + Sa-Token 核心后端服务
│   ├── oa-frontend/                  # Vue 3 + Vite 5 + Ant Design Vue 4 前端工程
│   ├── docker/                       # Option A: Docker Compose 一键容器化运行套件
│   ├── release/                      # Option B: 传统物理机 / 宝塔面板（BT-Panel）交付包
│   ├── docs/                         # 全周期技术规范、业务接口与运维指南 (00 ~ 10)
│   ├── sql/                          # MySQL 8.x 数据库 DDL (schema.sql) 与初始数据 (seed-data.sql)
│   ├── build-all.bat                 # Windows 一键全栈打包脚本
│   └── build-all.sh                  # Linux / macOS 一键全栈打包脚本
├── oa-back-service/                  # 【旧系统归档】JeecgBoot 2.1.3 + Java 8 + Shiro 历史资产
├── oa-front-service/                 # 【旧系统归档】Vue 2.6 + xmysql 历史资产
└── oa-vuchat-service-master/         # 【旧系统归档】早期移动端原型资产
```

---

## 🚀 交付部署方案（双选项支持）

根据生产环境差异，系统原生提供两种交付部署方式，均已通过全量联调与生产验证：

| 方案 | 适用场景 | 核心特点 | 详细指引 |
| :--- | :--- | :--- | :--- |
| **方案 A：Docker 容器编排** | 云原生环境、标准 Linux 服务器、具备 Docker 环境 | 一键拉起 MySQL 8 + JDK 17 + Nginx 容器，自动初始化数据库与网络拓扑 | 见 `oa-dev/docker/` 及文档指南 |
| **方案 B：物理机 / 宝塔面板** | 传统物理主机、宝塔面板 (BT-Panel)、Windows Server | 独立 Spring Boot Fat JAR + Nginx 静态站点反代，资源占用低，支持 systemd 守护进程 | 见 `oa-dev/release/` 及文档指南 |

### 快速一键构建所有产物
```bash
# Windows
cd oa-dev
.\build-all.bat

# Linux / macOS
cd oa-dev
chmod +x build-all.sh
./build-all.sh
```
执行后将自动生成：
- 前端生产静态包：`oa-dev/release/dist/`
- 后端独立运行 JAR：`oa-dev/release/oa-backend-1.0.0.jar`
- 生产环境配置文件与启停脚本：`oa-dev/release/config/` 与 `oa-dev/release/bin/`

---

## 📖 核心文档导航

系统设计文档存放在 [`oa-dev/docs/`](oa-dev/docs/) 目录下：
1. **[00-项目研发总纲领与实施任务书](oa-dev/docs/)**：重构背景、技术选型与里程碑规划。
2. **[01-数据模型清洗与数据库基准规范](oa-dev/docs/)**：数据库表结构范式、审计字段与 DDL 规划。
3. **[02-后端工程脚手架与流程引擎设计](oa-dev/docs/)**：自研轻量工作流引擎状态机与事务实现。
4. **[03-行政与人事业务接口实现文档](oa-dev/docs/)**：用印、红头公文、档案借阅、请假外出业务 API。
5. **[04-前端架构与工作台审批开发文档](oa-dev/docs/)**：Pinia 状态管理、路由守卫与双模工作台实现。
6. **[05-业务表单与纸质打印排版移植文档](oa-dev/docs/)**：高保真打印样式与防伪水印方案。
7. **[06-组织架构与RBAC权限字典开发文档](oa-dev/docs/)**：组织架构树、用户角色授权与数据字典。
8. **[07-移动端与全链路交付文档](oa-dev/docs/)**：移动端响应式适配与端到端验收用例。
9. **[08-全要素资产比对与系统迁移矩阵](oa-dev/docs/08-legacy-to-modern-comparison-matrix.md)**：老系统功能点 100% 映射与现代化提升对齐表。
10. **[09-企微/钉钉/飞书审批待办机器人集成指南](oa-dev/docs/09-im-robot-notification-guide.md)**：IM 提醒配置与开放式 Webhook 实操。
11. **[10-生产交付与双模式运维手册](oa-dev/docs/10-deployment-and-operations-guide.md)**：Docker-Compose 与宝塔面板详细落地部署与排障方案。

---

## 📅 后续演进规划

- **阶段一 & 阶段二（已完成）**：行政、人事、自研流程、双模工作台、IM 机器人、生产双交付包与运维文档全面验收。
- **阶段三（待排期）**：
  - 财务报销与采购资产进销存模块；
  - 线下物理考勤打卡机硬件物联同步；
  - 依据业务实际发展需求逐步扩展实施。
