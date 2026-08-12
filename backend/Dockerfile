# syntax=docker/dockerfile:1

# ---- Build stage ----
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /workspace

# 의존성 캐시를 위해 wrapper/Maven 설정 파일만 먼저 복사
COPY .mvn ./.mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw \
    && ./mvnw --batch-mode -DskipTests dependency:go-offline > /dev/null 2>&1 || true

# 소스 복사 후 빌드 (테스트 제외 — 테스트는 CI 단계에서 별도 실행)
COPY src ./src
RUN ./mvnw --batch-mode clean package -DskipTests

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre-jammy AS runtime
WORKDIR /app

RUN groupadd --system spring && useradd --system --gid spring spring \
    && mkdir -p /app/uploads \
    && chown -R spring:spring /app

COPY --from=build --chown=spring:spring /workspace/target/*.jar app.jar

USER spring:spring

EXPOSE 8080

VOLUME ["/app/uploads"]

# JVM 옵션은 JAVA_TOOL_OPTIONS 환경변수로 주입 가능
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
