FROM eclipse-temurin:21-jdk-noble AS builder

WORKDIR /workspace

COPY mvnw ./mvnw
COPY .mvn ./.mvn

COPY pom.xml ./pom.xml
COPY shared/pom.xml ./shared/pom.xml
COPY services/identity-api/pom.xml ./services/identity-api/pom.xml

RUN ./mvnw dependency:go-offline -B

COPY shared/src ./shared/src
COPY services/identity-api/src ./services/identity-api/src

RUN ./mvnw clean package -pl services/identity-api -am -DskipTests -Dcheckstyle.skip=true -Dspotless.check.skip=true -Dmaven.antrun.skip=true -B

FROM eclipse-temurin:21-jre-noble

WORKDIR /app

RUN groupadd --system app && useradd --system --gid app --no-create-home --home-dir /app --shell /usr/sbin/nologin identity

COPY --from=builder --chown=identity:app /workspace/services/identity-api/target/*.jar /app/app.jar

USER identity:app

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]