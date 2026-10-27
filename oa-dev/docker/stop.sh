#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo "正在停止协同办公平台所有运行容器..."
docker compose down
echo "容器已全部安全停止！"
