# ==============================================================================
# DOCKERFILE MULTI-STAGE BUILD CHO DỰ ÁN BOOKSTORE CHAIN SPRING BOOT
# Stage 1: Build & đóng gói ứng dụng với Maven và JDK 21 (Temurin)
# Stage 2: Runtime tối ưu dung lượng siêu nhẹ với JRE 21 Alpine & Non-root User
# ==============================================================================

# ------------------------------------------------------------------------------
# STAGE 1: BUILD STAGE
# ------------------------------------------------------------------------------
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# 1. Tận dụng Docker layer caching: Copy file định nghĩa dependency trước
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw mvnw

# Tải trước các thư viện cần thiết để tận dụng cache giữa các lần build
RUN mvn dependency:go-offline -B || true

# 2. Copy toàn bộ mã nguồn vào image và thực hiện build jar
COPY src src

# Đóng gói file JAR (bỏ qua unit test khi build image để tối ưu thời gian)
RUN mvn clean package -DskipTests -B

# ------------------------------------------------------------------------------
# STAGE 2: RUNTIME STAGE (PRODUCTION-READY)
# ------------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine AS runner

LABEL maintainer="Team Elechain Bookstore"
LABEL description="Chuỗi Cửa Hàng Bán Sách Cũ Trực Tuyến - Spring Boot 3"

# 1. Cài đặt tzdata để đồng bộ múi giờ Việt Nam và curl cho Healthcheck
RUN apk add --no-cache tzdata curl \
    && cp /usr/share/zoneinfo/Asia/Ho_Chi_Minh /etc/localtime \
    && echo "Asia/Ho_Chi_Minh" > /etc/timezone

WORKDIR /app

# 2. Tạo User không có quyền root (Non-root user) để tăng cường bảo mật container
RUN addgroup -S spring && adduser -S spring -G spring

# 3. Copy file JAR đã đóng gói từ Stage 1
COPY --from=builder /build/target/bookstore-0.0.1-SNAPSHOT.jar app.jar

# Phân quyền thư mục cho user spring
RUN chown -R spring:spring /app

# Chuyển sang user không phải root
USER spring:spring

# Khai báo port lắng nghe
EXPOSE 8080

# Cấu hình biến môi trường JVM tối ưu bộ nhớ trong môi trường Docker Container
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Dfile.encoding=UTF-8 -Duser.timezone=Asia/Ho_Chi_Minh"
ENV SPRING_PROFILES_ACTIVE="docker"

# Healthcheck kiểm tra trạng thái hoạt động của Spring Boot
HEALTHCHECK --interval=30s --timeout=5s --start-period=45s --retries=3 \
    CMD curl -f http://localhost:8080/ || exit 1

# Khởi chạy ứng dụng
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
