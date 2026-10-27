# ==========================================
# 现代企业协同办公系统 (OA-Suite) 后端 Dockerfile
# 基于 Eclipse Temurin OpenJDK 17 LTS
# ==========================================

# 运行镜像阶段
FROM eclipse-temurin:17-jre-jammy

LABEL maintainer="OA Modernization Team <graddu@oa.com>"
LABEL version="1.0.0"
LABEL description="Spring Boot 3 企业协同办公服务端运行容器"

# 设置时区与本地化
ENV TZ=Asia/Shanghai
ENV LANG=C.UTF-8
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

WORKDIR /app

# 挂载临时文件与日志卷
VOLUME /tmp
VOLUME /app/logs
VOLUME /app/uploads

# 复制 Maven 编译打包好的 Jar 包
COPY target/*.jar app.jar

# 暴露 Spring Boot 3 默认端口
EXPOSE 8080

# JVM 优化参数配置 (针对容器环境调优)
ENV JAVA_OPTS="-server -Xms256m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/app/logs/heapdump.hprof -Dfile.encoding=UTF-8 -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
