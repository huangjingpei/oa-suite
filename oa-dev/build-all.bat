@echo off
chcp 65001 >nul
echo ========================================================
echo   启航 OA 协同平台 (OA-Suite) 全栈一键打包输出
echo   (自动生成 Docker 容器镜像源与物理机独立分发包)
echo ========================================================

cd /d "%~dp0"

echo [1/4] 编译打包后端 Spring Boot 3 Executable JAR...
cd oa-backend
call mvn clean package -DskipTests
if %errorlevel% neq 0 (
    echo [错误] 后端打包失败！
    pause
    exit /b 1
)
cd ..

echo [2/4] 编译打包前端 Vue 3 + TypeScript 生产静态资源...
cd oa-frontend
call npm.cmd run build
if %errorlevel% neq 0 (
    echo [错误] 前端打包失败！
    pause
    exit /b 1
)
cd ..

echo [3/4] 正在归集物理机发布包至 release 目录...
if not exist "release\sql" mkdir "release\sql"
copy /y "oa-backend\target\oa-backend-1.0.0.jar" "release\oa-backend-1.0.0.jar" >nul
copy /y "sql\schema.sql" "release\sql\schema.sql" >nul
copy /y "sql\seed-data.sql" "release\sql\seed-data.sql" >nul

echo 正在同步前端 dist 目录...
if exist "release\dist" rd /s /q "release\dist"
xcopy /e /i /y "oa-frontend\dist" "release\dist" >nul

echo [4/4] 打包归集完成！
echo ========================================================
echo   【交付制品就绪】
echo   1. 选项 A (Docker 部署):
echo      目录: docker\
echo      启动: 双击 docker\start.bat 或 bash docker/start.sh
echo   2. 选项 B (物理机/宝塔部署):
echo      目录: release\
echo      核心: release\oa-backend-1.0.0.jar
echo            release\dist\ (前端)
echo            release\bin\start-backend.bat (启动脚本)
echo ========================================================
pause
