#!/bin/bash
# ========================================================
# 云枢 OA 协同平台 (OA-Suite) Linux 全栈一键打包脚本
# ========================================================
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo "========================================================"
echo "  云枢 OA 协同平台 (OA-Suite) 全栈一键打包输出"
echo "========================================================"

echo "[1/4] 编译打包后端 Spring Boot 3 Executable JAR..."
cd oa-backend
mvn clean package -DskipTests
cd ..

echo "[2/4] 编译打包前端 Vue 3 + TypeScript 生产静态资源..."
cd oa-frontend
npm run build
cd ..

echo "[3/4] 正在归集物理机发布包至 release 目录..."
mkdir -p release/sql
cp -f oa-backend/target/oa-backend-1.0.0.jar release/oa-backend-1.0.0.jar
cp -f sql/schema.sql release/sql/schema.sql
cp -f sql/seed-data.sql release/sql/seed-data.sql

echo "正在同步前端 dist 目录..."
rm -rf release/dist
cp -r oa-frontend/dist release/dist

echo "[4/4] 打包归集完成！"
echo "========================================================"
echo "  【交付制品就绪】"
echo "  1. 选项 A (Docker 部署): 进入 docker/ 运行 ./start.sh"
echo "  2. 选项 B (物理机/宝塔部署): 制品位于 release/ 目录"
echo "========================================================"
