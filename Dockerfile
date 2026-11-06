FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && useradd --system --uid 10001 --home-dir /app nexus \
    && mkdir -p /app/uploads \
    && chown -R nexus:nexus /app

COPY --from=build --chown=nexus:nexus /build/target/backend-*.jar /app/app.jar

USER nexus

ENV SPRING_PROFILES_ACTIVE=prod \
    UPLOAD_DIR=/app/uploads \
    JAVA_OPTS="-XX:MaxRAMPercentage=75"

VOLUME ["/app/uploads"]
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD curl -fsS http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
