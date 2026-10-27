#!/bin/bash
# ========================================================
# 盛泰协同办公平台 (OA-Suite) Linux 停止服务脚本
# ========================================================
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BASE_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
PID_FILE="$BASE_DIR/backend.pid"

if [ -f "$PID_FILE" ]; then
    PID=$(cat "$PID_FILE")
    if kill -0 "$PID" 2>/dev/null; then
        echo "正在优雅停止 OA 后端服务 (PID: $PID)..."
        kill "$PID"
        for i in {1..15}; do
            if ! kill -0 "$PID" 2>/dev/null; then
                echo "[成功] 服务已安全停止！"
                rm -f "$PID_FILE"
                exit 0
            fi
            sleep 1
        done
        echo "服务未在规定时间内退出，强制终止..."
        kill -9 "$PID" 2>/dev/null || true
        rm -f "$PID_FILE"
        echo "[成功] 服务已强制停止！"
        exit 0
    else
        echo "未检测到运行中的进程，清理过期的 pid 文件。"
        rm -f "$PID_FILE"
    fi
else
    # 尝试按端口兜底
    PID=$(lsof -t -i:8080 2>/dev/null || true)
    if [ -n "$PID" ]; then
        echo "发现占用 8080 端口的进程 (PID: $PID)，正在终止..."
        kill -9 "$PID"
        echo "[成功] 进程已终止！"
    else
        echo "当前没有运行中的 OA 后端服务。"
    fi
fi
