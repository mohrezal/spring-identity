FROM eclipse-temurin:21-jdk-noble AS builder

WORKDIR /workspace

COPY mvnw ./mvnw
COPY .mvn ./.mvn
COPY pom.xml ./pom.xml

RUN ./mvnw dependency:go-offline -B

COPY src/main ./src/main

RUN ./mvnw clean package -DskipTests -Dcheckstyle.skip=true -Dmaven.antrun.skip=true -B

FROM eclipse-temurin:21-jre-noble

WORKDIR /app

RUN groupadd --system app && useradd --system --gid app --no-create-home --home-dir /app --shell /usr/sbin/nologin identity

COPY --from=builder --chown=identity:app /workspace/target/*.jar /app/app.jar

USER identity:app

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]