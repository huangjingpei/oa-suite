@echo off
chcp 65001 >nul
echo ========================================================
echo   启航 OA 协同平台 (OA-Suite) 现代化容器编排一键启动
echo ========================================================

cd /d "%~dp0"

echo [1/3] 检查 Docker 运行环境...
docker info >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 未检测到 Docker 守护进程，请先启动 Docker Desktop！
    pause
    exit /b 1
)

echo [2/3] 启动 MySQL 8 + Spring Boot 3 + Vue 3 容器集群...
docker compose up -d

echo [3/3] 正在拉起服务容器...
docker compose ps

echo ========================================================
echo   恭喜！协同办公平台容器集群已成功在后台启动！
echo   PC 电脑端工作台地址：  http://localhost
echo   移动端 H5 专版地址：   http://localhost/m
echo   后端 Swagger 接口文档： http://localhost:8080/api/swagger-ui.html
echo ========================================================
pause
