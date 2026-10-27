#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
云枢协同系统 · 企业级白标/客户品牌一键定制重塑工具 (White-Label Rebranding Tool)
支持通过命令行参数或交互式输入，对前端、后端、数据库脚本、文档与运维脚本进行全量品牌替换。
支持任意次重复调用，将当前系统名称无缝转换为任意客户指定的品牌。
"""

import os
import sys
import argparse

if sys.stdout.encoding.lower() != 'utf-8':
    try:
        sys.stdout.reconfigure(encoding='utf-8')
    except Exception:
        pass

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
PROJECT_ROOT = os.path.dirname(SCRIPT_DIR)

EXCLUDE_DIRS = {'.git', 'node_modules', 'target', 'dist', '.idea', '.vscode'}
TARGET_EXTS = {'.vue', '.ts', '.js', '.java', '.sql', '.md', '.bat', '.sh', '.yml', '.yaml', '.html', '.json', '.xml'}

def parse_args():
    parser = argparse.ArgumentParser(description="企业协同办公系统品牌名称白标定制工具")
    parser.add_argument("--system-name", "-s", default="云枢 OA 协同平台", help="新系统显示名称，例如：'极光协同办公平台' 或 '某某集团协同工作台'")
    parser.add_argument("--company-full", "-c", default="云枢智慧协同科技有限公司", help="客户公司全称，例如：'某某智能科技集团有限公司'")
    parser.add_argument("--company-short", "-b", default="云枢协同", help="客户企业简称/品牌名，例如：'某某科技' 或 '某某协同'")
    parser.add_argument("--doc-prefix", "-d", default="云枢发", help="公文字号代字，例如：'华发' 或 '京工发'")
    parser.add_argument("--from-keyword", "-f", default="云枢", help="原品牌关键词 (默认自动兼容 '云枢' 与 '盛泰')")
    parser.add_argument("--target-dir", "-t", default=PROJECT_ROOT, help="目标替换目录 (默认 oa-dev 根目录)")
    return parser.parse_args()

def rebrand(target_dir, system_name, company_full, company_short, doc_prefix, from_kw):
    print("=" * 65)
    print(" 🚀 企业协同办公系统 · 白标品牌定制与客户化重塑引擎")
    print("=" * 65)
    print(f" [1] 扫描根目录   : {target_dir}")
    print(f" [2] 新系统名称   : {system_name}")
    print(f" [3] 客户公司全称 : {company_full}")
    print(f" [4] 客户企业简称 : {company_short}")
    print(f" [5] 红头公文字号 : {doc_prefix}〔2026〕XX号")
    print("-" * 65)

    # 替换规则映射：按从具体到笼统的顺序排列
    # 同时兼容历史可能存在的词以及当前基准词
    replacement_rules = [
        # 1. 公司全称与组织
        (f"{from_kw}智慧协同集团有限公司", company_full),
        (f"{from_kw}智慧协同科技有限公司", company_full),
        (f"{from_kw}智慧协同科技集团有限公司", company_full),
        (f"{from_kw}智慧协同集团总部", f"{company_short}集团总部"),
        (f"{from_kw}智慧协同产业园", f"{company_short}科技园"),
        (f"{from_kw}智慧协同集团总裁办公室", f"{company_short}总裁办公室"),
        (f"{from_kw}财务与薪酬审计中心", f"{company_short}财务与薪酬中心"),
        (f"{from_kw}财务与薪酬中心", f"{company_short}财务与薪酬中心"),
        (f"{from_kw}集团总部", f"{company_short}集团总部"),
        (f"{from_kw}科技园", f"{company_short}科技园"),
        (f"{from_kw}大厦A座", f"{company_short}大厦A座"),
        (f"{from_kw}大厦", f"{company_short}大厦"),
        # 2. 公文发文代字
        (f"{from_kw}发〔", f"{doc_prefix}〔"),
        (f"{from_kw}发", doc_prefix),
        # 3. 系统平台名称
        (f"{from_kw} OA 协同平台", system_name),
        (f"{from_kw} OA 协同办公平台", system_name),
        (f"{from_kw}协同办公平台", system_name),
        (f"{from_kw}协同", company_short),
        (f"{from_kw}科技", company_short),
        (f"{from_kw}新一代协同办公系统", f"{system_name}系统"),
        # 4. 兜底通用替换
        (from_kw, company_short)
    ]

    # 如果原词不是“盛泰”，也顺带把可能残留的历史词抹除
    if from_kw != "盛泰":
        replacement_rules.extend([
            ("盛泰智慧协同集团有限公司", company_full),
            ("盛泰智慧协同科技有限公司", company_full),
            ("盛泰智慧协同集团总部", f"{company_short}集团总部"),
            ("盛泰智慧协同产业园", f"{company_short}科技园"),
            ("盛泰大厦A座", f"{company_short}大厦A座"),
            ("盛泰大厦", f"{company_short}大厦"),
            ("盛泰发〔", f"{doc_prefix}〔"),
            ("盛泰发", doc_prefix),
            ("盛泰 OA 协同平台", system_name),
            ("盛泰 OA 协同办公平台", system_name),
            ("盛泰协同办公平台", system_name),
            ("盛泰协同", company_short),
            ("盛泰科技", company_short),
            ("盛泰", company_short)
        ])

    total_scanned = 0
    total_modified = 0
    modified_files = []

    for root, dirs, files in os.walk(target_dir):
        dirs[:] = [d for d in dirs if d not in EXCLUDE_DIRS]

        for file in files:
            ext = os.path.splitext(file)[1].lower()
            if ext not in TARGET_EXTS:
                continue

            file_path = os.path.join(root, file)
            if os.path.abspath(file_path) == os.path.abspath(__file__):
                continue

            total_scanned += 1
            try:
                with open(file_path, 'r', encoding='utf-8') as f:
                    content = f.read()
            except UnicodeDecodeError:
                try:
                    with open(file_path, 'r', encoding='gbk') as f:
                        content = f.read()
                except Exception:
                    continue
            except Exception:
                continue

            new_content = content
            file_changed = False
            for old_text, new_text in replacement_rules:
                if old_text in new_content and old_text != new_text:
                    new_content = new_content.replace(old_text, new_text)
                    file_changed = True

            if file_changed:
                try:
                    with open(file_path, 'w', encoding='utf-8') as f:
                        f.write(new_content)
                    total_modified += 1
                    rel_path = os.path.relpath(file_path, target_dir)
                    modified_files.append(rel_path)
                    print(f" [✓] 已重塑: {rel_path}")
                except Exception as e:
                    print(f" [!] 写入失败: {file_path}, 错误: {e}")

    print("-" * 65)
    print(f" 🎉 品牌重塑执行完毕！共扫描 {total_scanned} 个文件，成功重塑 {total_modified} 个文件。")
    print("=" * 65)

if __name__ == "__main__":
    args = parse_args()
    rebrand(args.target_dir, args.system_name, args.company_full, args.company_short, args.doc_prefix, args.from_keyword)
