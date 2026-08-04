FROM eclipse-temurin:21-jre-noble

RUN apt-get update -qq && apt-get install -y -qq --no-install-recommends curl && rm -rf /var/lib/apt/lists/*

WORKDIR /app

RUN groupadd --system app && useradd --system --gid app --no-create-home --home-dir /app --shell /usr/sbin/nologin identity

COPY --chown=identity:app target/*.jar app.jar

USER identity:app

EXPOSE 8080 8082

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD curl -sf "http://localhost:${APP_MANAGEMENT_PORT:-8082}/actuator/health" || \
        curl -sf http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
