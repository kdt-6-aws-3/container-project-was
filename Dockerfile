# syntax=docker/dockerfile:1
# 빌드 예시 (K8s 노드가 amd64인 경우, ARM 머신에서 빌드할 때 필수):
#   docker buildx build --platform linux/amd64 --build-arg VERSION=0.0.2 -t <image>:<tag> .
ARG VERSION=0.0.2

# Stage 1: 빌드
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build

COPY gradlew settings.gradle build.gradle ./
COPY gradle/ gradle/
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew dependencies --no-daemon -q > /dev/null

COPY src/ src/
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew bootJar --no-daemon -q \
 && cp build/libs/*.jar app.jar \
 && java -Djarmode=tools -jar app.jar extract --layers --destination extracted

# Stage 2: 서빙
FROM eclipse-temurin:21-jre-alpine
ARG VERSION

LABEL org.opencontainers.image.source="https://github.com/kdt-6-aws-3/container-project-was"

# tzdata: OS 레벨(date, 로그 수집기 등)에서도 TZ=Asia/Seoul 적용
# 숫자 UID/GID 고정: K8s runAsNonRoot 검증 통과용
RUN apk add --no-cache tzdata \
 && addgroup -S -g 10001 petclinic \
 && adduser -S -u 10001 -G petclinic petclinic
WORKDIR /app

COPY --from=builder --chown=10001:10001 /build/extracted/dependencies/ ./
COPY --from=builder --chown=10001:10001 /build/extracted/spring-boot-loader/ ./
COPY --from=builder --chown=10001:10001 /build/extracted/snapshot-dependencies/ ./
COPY --from=builder --chown=10001:10001 /build/extracted/application/ ./

# 이름 대신 숫자 UID 사용 (non-numeric user면 runAsNonRoot에서 Pod 생성 실패)
USER 10001:10001
EXPOSE 8080

# MaxRAMPercentage는 Pod에 resources.limits.memory가 설정돼 있어야 의도대로 동작
ENV APP_VERSION=${VERSION} \
    TZ=Asia/Seoul \
    SPRING_PROFILES_ACTIVE=mysql \
    LOG_LEVEL=INFO \
    APP_ENV=production \
    JAVA_OPTS="-XX:MaxRAMPercentage=75" \
    MANAGEMENT_ENDPOINT_HEALTH_PROBES_ENABLED=true

# 로컬 docker / compose용. K8s에서는 무시되므로 Deployment의 probe를 사용
HEALTHCHECK --interval=15s --timeout=5s --start-period=60s --retries=3 \
  CMD wget -qO- http://127.0.0.1:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]