# Multi-stage build. Build context = repository root (the pom reads tests from ../test).
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY code/pom.xml code/pom.xml
RUN mvn -q -f code/pom.xml dependency:go-offline
COPY code code
COPY test test
RUN mvn -q -f code/pom.xml package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
RUN useradd --system --uid 1001 cinema
COPY --from=build /workspace/code/target/cinema-log.jar app.jar
USER cinema
EXPOSE 8080
ENV TZ=Asia/Bangkok
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
