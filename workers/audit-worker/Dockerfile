FROM eclipse-temurin:21-jdk-noble AS builder

WORKDIR /workspace

COPY mvnw ./mvnw
COPY .mvn ./.mvn

COPY pom.xml ./pom.xml
COPY shared/pom.xml ./shared/pom.xml
COPY workers/audit-worker/pom.xml ./workers/audit-worker/pom.xml

RUN ./mvnw dependency:go-offline -B

COPY shared/src ./shared/src
COPY workers/audit-worker/src ./workers/audit-worker/src

RUN ./mvnw clean package -pl workers/audit-worker -am -DskipTests -Dcheckstyle.skip=true -Dspotless.check.skip=true -B

FROM eclipse-temurin:21-jre-noble

WORKDIR /app

RUN groupadd --system app && useradd --system --gid app --no-create-home --home-dir /app --shell /usr/sbin/nologin audit-worker

COPY --from=builder --chown=audit-worker:app /workspace/workers/audit-worker/target/*.jar /app/app.jar

USER audit-worker:app

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]