#!/bin/bash
# ========================================================
# 启航 OA 协同平台 (OA-Suite) Linux 后台守护启动脚本
# ========================================================
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BASE_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$BASE_DIR"

JAR_NAME="oa-backend-1.0.0.jar"
LOG_DIR="$BASE_DIR/logs"
PID_FILE="$BASE_DIR/backend.pid"

mkdir -p "$LOG_DIR"

if [ -f "$PID_FILE" ] && kill -0 "$(cat "$PID_FILE")" 2>/dev/null; then
    echo "[提示] OA 后端服务已在运行中 (PID: $(cat "$PID_FILE"))，无需重复启动。"
    exit 0
fi

if ! command -v java >/dev/null 2>&1; then
    echo "[错误] 未检测到 Java 17 环境，请先安装 JDK 17 (apt/yum install openjdk-17-jdk)！"
    exit 1
fi

JAVA_OPTS="-server -Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Dfile.encoding=UTF-8 -Dspring.profiles.active=prod"

echo "正在启动 $JAR_NAME ..."
nohup java $JAVA_OPTS -jar "$JAR_NAME" > "$LOG_DIR/console.log" 2>&1 &
PID=$!
echo $PID > "$PID_FILE"

sleep 2
if kill -0 $PID 2>/dev/null; then
    echo "[成功] OA 后端服务已成功启动！(PID: $PID)"
    echo "日志监控: tail -f $LOG_DIR/console.log"
else
    echo "[失败] 服务启动异常，请检查 $LOG_DIR/console.log 日志！"
    exit 1
fi
