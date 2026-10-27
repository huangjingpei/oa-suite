@echo off
chcp 65001 >nul
:: ==============================================================================
:: 启航协同系统 · 企业级白标/客户品牌一键定制重塑脚本 (Windows Batch 版)
:: ==============================================================================

set SCRIPT_DIR=%~dp0
set TARGET_DIR=%SCRIPT_DIR%..

echo =================================================================
echo   🚀 企业协同办公系统 · 客户白标品牌一键定制工具
echo =================================================================

set "SYSTEM_NAME=%~1"
set "COMPANY_FULL=%~2"
set "COMPANY_SHORT=%~3"
set "DOC_PREFIX=%~4"

if "%SYSTEM_NAME%"=="" (
    echo.
    echo 请输入您想要为客户定制的品牌信息（直接回车使用默认推荐值）:
    set /p "SYSTEM_NAME= [1] 系统显示名称 (默认: 启航 OA 协同平台): "
)
if "%SYSTEM_NAME%"=="" set "SYSTEM_NAME=启航 OA 协同平台"

if "%COMPANY_FULL%"=="" (
    set /p "COMPANY_FULL= [2] 客户公司全称 (默认: 启航智慧协同科技有限公司): "
)
if "%COMPANY_FULL%"=="" set "COMPANY_FULL=启航智慧协同科技有限公司"

if "%COMPANY_SHORT%"=="" (
    set /p "COMPANY_SHORT= [3] 客户企业简称 (默认: 启航协同): "
)
if "%COMPANY_SHORT%"=="" set "COMPANY_SHORT=启航协同"

if "%DOC_PREFIX%"=="" (
    set /p "DOC_PREFIX= [4] 红头公文字号代字 (默认: 启航发): "
)
if "%DOC_PREFIX%"=="" set "DOC_PREFIX=启航发"

echo.
echo -----------------------------------------------------------------
echo  即将执行品牌全量定制：
echo    - 系统名称   : %SYSTEM_NAME%
echo    - 公司全称   : %COMPANY_FULL%
echo    - 企业简称   : %COMPANY_SHORT%
echo    - 公文字号   : %DOC_PREFIX%〔2026〕XX号
echo    - 目标工作区 : %TARGET_DIR%
echo -----------------------------------------------------------------

python "%SCRIPT_DIR%rebrand.py" --system-name "%SYSTEM_NAME%" --company-full "%COMPANY_FULL%" --company-short "%COMPANY_SHORT%" --doc-prefix "%DOC_PREFIX%" --target-dir "%TARGET_DIR%"

echo.
echo =================================================================
echo  🎉 客户品牌定制完成！
echo  提示：如果需要立即生成部署包，请在 oa-dev 目录下执行 build-all.bat！
echo =================================================================
pause
