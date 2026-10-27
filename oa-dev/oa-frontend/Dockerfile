# ==========================================
# 现代企业协同办公系统 (OA-Suite) 前端 Dockerfile
# 基于 Nginx Alpine 轻量化高性能镜像
# ==========================================

FROM nginx:1.25-alpine

LABEL maintainer="OA Modernization Team <graddu@oa.com>"
LABEL version="1.0.0"
LABEL description="Vue 3 + Vite 5 + Ant Design 4 前端静态托管与反向代理"

# 替换默认 Nginx 静态文件
RUN rm -rf /usr/share/nginx/html/*

# 复制前端编译打包好的生产产物
COPY dist /usr/share/nginx/html

# 复制生产级 Nginx 配置
COPY ../docker/nginx.conf /etc/nginx/nginx.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
