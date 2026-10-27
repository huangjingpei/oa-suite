@echo off
chcp 65001 >nul
echo 正在停止协同办公平台所有运行容器...
cd /d "%~dp0"
docker compose down
echo 容器已全部安全停止！
pause
