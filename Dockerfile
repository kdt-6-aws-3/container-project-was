# syntax=docker/dockerfile:1
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

RUN addgroup -S petclinic && adduser -S -G petclinic petclinic
WORKDIR /app

COPY --from=builder /build/extracted/dependencies/ ./
COPY --from=builder /build/extracted/spring-boot-loader/ ./
COPY --from=builder /build/extracted/snapshot-dependencies/ ./
COPY --from=builder /build/extracted/application/ ./

USER petclinic
EXPOSE 8080

ENV APP_VERSION=${VERSION} \
    TZ=Asia/Seoul \
    SPRING_PROFILES_ACTIVE=mysql \
    LOG_LEVEL=INFO \
    APP_ENV=production \
    JAVA_OPTS="-XX:MaxRAMPercentage=75"

HEALTHCHECK --interval=15s --timeout=5s --start-period=60s --retries=3 \
  CMD wget -qO- http://127.0.0.1:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
