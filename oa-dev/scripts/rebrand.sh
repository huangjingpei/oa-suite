#!/usr/bin/env bash
# ==============================================================================
# 启航协同系统 · 企业级白标/客户品牌一键定制重塑脚本 (White-Label Rebrand Script)
# 用途：为不同客户实施交付时，一键将系统内外所有品牌、公司全称、简称、公文字号
#       替换为客户指定的名称，彻底抹除源码原始品牌印记。
#
# 使用方式：
# 1. 交互模式运行：
#    bash rebrand.sh
# 2. 命令行参数直接传入：
#    bash rebrand.sh "XX集团协同平台" "XX实业控股集团有限公司" "XX集团" "XX发"
# ==============================================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TARGET_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"

echo "================================================================="
echo "  🚀 企业协同办公系统 · 客户白标品牌一键定制脚本"
echo "================================================================="

# 读取参数或交互式输入
SYSTEM_NAME="$1"
COMPANY_FULL="$2"
COMPANY_SHORT="$3"
DOC_PREFIX="$4"

if [ -z "$SYSTEM_NAME" ]; then
    echo ""
    echo "请输入您想要为客户定制的品牌信息（直接回车使用默认推荐值）:"
    read -p " [1] 系统显示名称 (默认: 启航 OA 协同平台): " INPUT_SYS
    SYSTEM_NAME="${INPUT_SYS:-启航 OA 协同平台}"
fi

if [ -z "$COMPANY_FULL" ]; then
    read -p " [2] 客户公司全称 (默认: 启航智慧协同科技有限公司): " INPUT_FULL
    COMPANY_FULL="${INPUT_FULL:-启航智慧协同科技有限公司}"
fi

if [ -z "$COMPANY_SHORT" ]; then
    read -p " [3] 客户企业简称 (默认: 启航协同): " INPUT_SHORT
    COMPANY_SHORT="${INPUT_SHORT:-启航协同}"
fi

if [ -z "$DOC_PREFIX" ]; then
    read -p " [4] 红头公文字号代字 (默认: 启航发): " INPUT_PREFIX
    DOC_PREFIX="${INPUT_PREFIX:-启航发}"
fi

echo ""
echo "-----------------------------------------------------------------"
echo " 即将执行品牌全量定制："
echo "   - 系统名称   : ${SYSTEM_NAME}"
echo "   - 公司全称   : ${COMPANY_FULL}"
echo "   - 企业简称   : ${COMPANY_SHORT}"
echo "   - 公文字号   : ${DOC_PREFIX}〔2026〕XX号"
echo "   - 目标工作区 : ${TARGET_DIR}"
echo "-----------------------------------------------------------------"

# 优先尝试使用 python3 执行高精度全量替换
if command -v python3 &>/dev/null; then
    python3 "${SCRIPT_DIR}/rebrand.py" \
        --system-name "${SYSTEM_NAME}" \
        --company-full "${COMPANY_FULL}" \
        --company-short "${COMPANY_SHORT}" \
        --doc-prefix "${DOC_PREFIX}" \
        --target-dir "${TARGET_DIR}"
elif command -v python &>/dev/null; then
    python "${SCRIPT_DIR}/rebrand.py" \
        --system-name "${SYSTEM_NAME}" \
        --company-full "${COMPANY_FULL}" \
        --company-short "${COMPANY_SHORT}" \
        --doc-prefix "${DOC_PREFIX}" \
        --target-dir "${TARGET_DIR}"
else
    # 纯 Bash 模式 (sed 替换兜底)
    echo "未检测到 Python 环境，切换为纯 Bash sed 引擎替换..."
    
    # 递归遍历目标文件进行 sed 替换
    find "${TARGET_DIR}" -type f \( \
        -name "*.vue" -o -name "*.ts" -o -name "*.js" -o -name "*.java" \
        -o -name "*.sql" -o -name "*.md" -o -name "*.bat" -o -name "*.sh" \
        -o -name "*.yml" -o -name "*.html" \) \
        ! -path "*/.git/*" ! -path "*/node_modules/*" ! -path "*/target/*" ! -path "*/dist/*" \
        | while read -r file; do
            sed -i "s/启航智慧协同科技有限公司/${COMPANY_FULL}/g" "$file" 2>/dev/null || true
            sed -i "s/启航智慧协同科技有限公司/${COMPANY_FULL}/g" "$file" 2>/dev/null || true
            sed -i "s/启航协同集团总部/${COMPANY_SHORT}集团总部/g" "$file" 2>/dev/null || true
            sed -i "s/启航协同科技园/${COMPANY_SHORT}科技园/g" "$file" 2>/dev/null || true
            sed -i "s/启航协同财务与薪酬中心/${COMPANY_SHORT}财务与薪酬中心/g" "$file" 2>/dev/null || true
            sed -i "s/启航协同大厦A座/${COMPANY_SHORT}大厦A座/g" "$file" 2>/dev/null || true
            sed -i "s/启航协同大厦/${COMPANY_SHORT}大厦/g" "$file" 2>/dev/null || true
            sed -i "s/启航发〔/${DOC_PREFIX}〔/g" "$file" 2>/dev/null || true
            sed -i "s/启航发/${DOC_PREFIX}/g" "$file" 2>/dev/null || true
            sed -i "s/启航 OA 协同平台/${SYSTEM_NAME}/g" "$file" 2>/dev/null || true
            sed -i "s/启航 OA 协同平台/${SYSTEM_NAME}/g" "$file" 2>/dev/null || true
            sed -i "s/启航 OA 协同平台/${SYSTEM_NAME}/g" "$file" 2>/dev/null || true
            sed -i "s/启航协同/${COMPANY_SHORT}/g" "$file" 2>/dev/null || true
            sed -i "s/启航协同/${COMPANY_SHORT}/g" "$file" 2>/dev/null || true
            sed -i "s/启航协同/${COMPANY_SHORT}/g" "$file" 2>/dev/null || true
        done
    echo " [✓] 纯 Bash 模式替换完毕！"
fi

echo ""
echo "================================================================="
echo " 🎉 客户品牌定制完成！"
echo " 提示：如果需要立即生成部署包，请在 oa-dev 目录下执行 build-all 脚本！"
echo "================================================================="
