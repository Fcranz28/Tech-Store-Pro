FROM docker.io/library/maven:3.9.16-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml ./
RUN mvn -B -ntp dependency:go-offline
COPY src ./src
RUN mvn -B -ntp verify

FROM docker.io/library/eclipse-temurin:17-jre-alpine AS runtime
WORKDIR /app
RUN addgroup -g 10001 app && adduser -D -u 10001 -G app app \
    && mkdir -p /app/uploads/productos && chown -R app:app /app
COPY --from=build --chown=app:app /workspace/target/TechStore-Pro-0.0.1-SNAPSHOT.jar /app/app.jar
ENV PORT=8080 \
    UPLOAD_DIR=/app/uploads/productos \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0 -Djava.awt.headless=true"
USER 10001:10001
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD wget -q -O /dev/null "http://127.0.0.1:${PORT}/actuator/health/readiness" || exit 1
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
