# Build backend and include frontend static files from /backend/src/main/resources/static
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY backend/pom.xml ./pom.xml
COPY backend/src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/target/backend-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
# Start JVM with server.port set from PORT env var (Render provides PORT)
ENTRYPOINT ["sh","-c","java -Dserver.port=${PORT:-8080} -jar /app/app.jar"]
