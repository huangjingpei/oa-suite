@echo off
chcp 65001 >nul
echo 正在停止占用 8080 端口的 OA 后端进程...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr ":8080" ^| findstr "LISTENING"') do (
    taskkill /f /pid %%a >nul 2>&1
    echo 已成功终止进程 PID: %%a
)
echo 后端服务已安全停止！
pause
