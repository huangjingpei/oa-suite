@echo off
chcp 65001 >nul
echo ========================================================
echo   启航 OA 协同平台 (OA-Suite) 后端服务启动脚本 (Windows)
echo ========================================================

cd /d "%~dp0.."

where java >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 未检测到 Java 运行环境，请先安装 JDK 17 并配置环境变量！
    pause
    exit /b 1
)

echo 正在启动 Spring Boot 3 后端服务 (端口: 8080)...
set JAVA_OPTS=-Xms512m -Xmx1024m -XX:+UseG1GC -Dfile.encoding=UTF-8 -Dspring.profiles.active=prod
start "OA-Suite-Backend" java %JAVA_OPTS% -jar oa-backend-1.0.0.jar

echo 服务已在后台拉起！
echo 查看日志请打开 logs/oa-backend.log 或控制台窗口。
pause
