#!/bin/bash
set -e

echo "========================================================"
echo "  启航 OA 协同平台 (OA-Suite) 现代化容器编排一键启动"
echo "========================================================"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

if ! docker info >/dev/null 2>&1; then
    echo "[错误] 未检测到 Docker 守护进程，请先启动 Docker 服务！"
    exit 1
fi

echo "[1/2] 启动 MySQL 8 + Spring Boot 3 + Vue 3 容器集群..."
docker compose up -d

echo "[2/2] 服务集群运行状态："
docker compose ps

echo "========================================================"
echo "  恭喜！协同办公平台容器集群已成功在后台启动！"
echo "  PC 电脑端工作台地址：  http://localhost"
echo "  移动端 H5 专版地址：   http://localhost/m"
echo "  后端 Swagger 接口文档： http://localhost:8080/api/swagger-ui.html"
echo "========================================================"
