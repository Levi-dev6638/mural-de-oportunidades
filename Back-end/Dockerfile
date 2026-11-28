FROM eclipse-temurin:25-jdk AS build

WORKDIR /workspace/Back-end
COPY Back-end/mvnw ./mvnw
COPY Back-end/.mvn ./.mvn
COPY Back-end/pom.xml ./pom.xml
COPY Back-end/src/main ./src/main
COPY backend/data/seed.json /workspace/backend/data/seed.json

RUN chmod +x ./mvnw && ./mvnw -B -Pproduction -DskipTests package

FROM eclipse-temurin:25-jre

RUN useradd --system --uid 10001 --create-home appuser
WORKDIR /app
COPY --from=build --chown=appuser:appuser /workspace/Back-end/target/nexthub-api-1.0.0.jar ./app.jar

ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=60.0 -XX:+UseSerialGC"
EXPOSE 8080
USER appuser
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
